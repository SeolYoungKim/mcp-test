package kakao.mcp

import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ReadResourceRequest
import java.io.InputStream
import java.io.OutputStream

/**
 * MCP 클라이언트 예제 구현
 * Model Context Protocol의 QuickStart 예제를 바탕으로 구현
 */
class MCPClient(
    private val inputStream: InputStream,
    private val outputStream: OutputStream
) {
    // 클라이언트 인스턴스 생성
    private val client = Client(
        clientInfo = Implementation(
            name = "example-client",
            version = "1.0.0"
        )
    )

    /**
     * MCP 서버에 연결하고 리소스를 조회하는 예제
     */
    suspend fun connectAndInteract() {
        println("MCP 클라이언트 시작...")

        // 표준 입출력(STDIO) 기반 Transport 생성
        val transport = StdioClientTransport(
            inputStream = inputStream,
            outputStream = outputStream
        )

        // 서버에 연결
        println("서버에 연결 중...")
        client.connect(transport)
        println("서버 연결 완료")

        // 사용 가능한 리소스 목록 조회
        println("리소스 목록 조회 중...")
        val resources = client.listResources()
        println("사용 가능한 리소스: ${resources.resources.size}개")
        resources.resources.forEach { resource ->
            println("- ${resource.name}: ${resource.uri}")
        }

        // 특정 리소스 읽기
        if (resources.resources.isNotEmpty()) {
            val resourceUri = resources.resources.first().uri
            println("첫 번째 리소스 읽기: $resourceUri")
            val resourceContent = client.readResource(
                ReadResourceRequest(uri = resourceUri)
            )
            
            println("리소스 내용:")
            resourceContent.contents.forEach { content ->
                when (content) {
                    is io.modelcontextprotocol.kotlin.sdk.TextResourceContents -> {
                        println(content.text)
                    }
                    else -> {
                        println("지원되지 않는 컨텐츠 유형: ${content.javaClass.simpleName}")
                    }
                }
            }
        }

        // 연결 종료
        println("연결 종료 중...")
        client.disconnect()
        println("MCP 클라이언트 종료")
    }
}

/**
 * 실행하기 위한 main 함수
 * 실제 사용 시에는 적절한 입출력 스트림을 전달해야 함
 */
suspend fun main() {
    println("이 예제는 실제 MCP 서버와 연결하기 위한 코드 샘플입니다.")
    println("실행 시 적절한 스트림을 전달해야 합니다.")
    
    // 여기서는 실제 연결을 하지 않고 예제 코드만 제공
    // val client = MCPClient(serverProcess.inputStream, serverProcess.outputStream)
    // client.connectAndInteract()
}
