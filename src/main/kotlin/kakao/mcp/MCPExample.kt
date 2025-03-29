package kakao.mcp

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * MCP 클라이언트와 서버를 연결하는 예제
 * PipedInputStream/OutputStream을 사용하여 두 프로세스 간 통신을 시뮬레이션
 */
object MCPExample {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // 클라이언트 -> 서버 파이프 생성
        val clientToServerOutput = PipedOutputStream()
        val clientToServerInput = PipedInputStream(clientToServerOutput)

        // 서버 -> 클라이언트 파이프 생성
        val serverToClientOutput = PipedOutputStream()
        val serverToClientInput = PipedInputStream(serverToClientOutput)

        println("===== MCP 예제 시작 =====")
        println("클라이언트와 서버 간 파이프 연결을 통한 통신 테스트")

        // 서버 인스턴스 생성 및 실행 (백그라운드에서)
        val serverJob = launch(Dispatchers.IO) {
            val server = MCPServerSimple(clientToServerInput, serverToClientOutput)
            server.start()
        }

        // 서버가 초기화될 시간 부여
        delay(500)

        // 클라이언트 인스턴스 생성 및 실행
        val clientDeferred = async(Dispatchers.IO) {
            val client = MCPClientSimple(serverToClientInput, clientToServerOutput)
            client.connectAndInteract()
        }

        // 클라이언트 작업 완료 대기
        clientDeferred.await()
        
        // 서버 작업 취소
        serverJob.cancel()
        
        println("===== MCP 예제 종료 =====")
    }
}

/**
 * 간소화된 MCP 서버 구현
 * 파이프된 입출력 스트림을 사용하여 통신
 */
class MCPServerSimple(
    private val inputStream: PipedInputStream,
    private val outputStream: PipedOutputStream
) {
    private val server = Server(
        serverInfo = Implementation(
            name = "simple-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                resources = ServerCapabilities.Resources(
                    subscribe = true,
                    listChanged = true
                )
            )
        )
    )

    suspend fun start() {
        println("[서버] 시작 중...")

        // 간단한 리소스 추가
        server.addResource(
            uri = "file:///example.txt",
            name = "예제 텍스트 파일",
            description = "MCP 테스트를 위한 예제 텍스트 파일",
            mimeType = "text/plain"
        ) { request ->
            println("[서버] 리소스 요청 받음: ${request.uri}")
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        text = "이것은 예제 리소스의 내용입니다. MCP로 MCP 만들기!",
                        uri = request.uri,
                        mimeType = "text/plain"
                    )
                )
            )
        }

        // 커스텀 입출력 스트림을 사용하는 Transport 생성
        val transport = object : StdioServerTransport() {
            override val inputStream = this@MCPServerSimple.inputStream
            override val outputStream = this@MCPServerSimple.outputStream
        }

        server.connect(transport)
        println("[서버] 연결 대기 중...")

        // 서버는 연결이 종료될 때까지 대기
        // 실제 구현에서는 이 부분을 적절히 처리해야 함
        try {
            while (true) {
                delay(1000)
            }
        } catch (e: Exception) {
            println("[서버] 종료 중: ${e.message}")
        } finally {
            server.disconnect()
        }
    }
}

/**
 * 간소화된 MCP 클라이언트 구현
 * 파이프된 입출력 스트림을 사용하여 통신
 */
class MCPClientSimple(
    private val inputStream: PipedInputStream,
    private val outputStream: PipedOutputStream
) {
    private val client = Client(
        clientInfo = Implementation(
            name = "simple-client",
            version = "1.0.0"
        )
    )

    suspend fun connectAndInteract() {
        println("[클라이언트] 시작 중...")

        // 커스텀 입출력 스트림을 사용하는 Transport 생성
        val transport = object : StdioClientTransport(
            inputStream = inputStream,
            outputStream = outputStream
        ) {}

        // 서버에 연결
        println("[클라이언트] 서버에 연결 중...")
        client.connect(transport)
        println("[클라이언트] 연결 성공")

        // 리소스 목록 조회
        println("[클라이언트] 리소스 목록 조회 중...")
        val resources = client.listResources()
        println("[클라이언트] 사용 가능한 리소스: ${resources.resources.size}개")
        
        resources.resources.forEach { resource ->
            println("[클라이언트] - ${resource.name}: ${resource.uri}")
        }

        // 리소스가 있으면 첫 번째 리소스 읽기
        if (resources.resources.isNotEmpty()) {
            val resourceUri = resources.resources.first().uri
            println("[클라이언트] 리소스 읽기: $resourceUri")
            
            val resourceContent = client.readResource(
                ReadResourceRequest(uri = resourceUri)
            )
            
            println("[클라이언트] 리소스 내용:")
            resourceContent.contents.forEach { content ->
                when (content) {
                    is TextResourceContents -> {
                        println("[클라이언트] ${content.text}")
                    }
                    else -> {
                        println("[클라이언트] 지원되지 않는 컨텐츠 유형: ${content.javaClass.simpleName}")
                    }
                }
            }
        }

        // 연결 종료
        println("[클라이언트] 연결 종료 중...")
        client.disconnect()
        println("[클라이언트] 종료 완료")
    }
}
