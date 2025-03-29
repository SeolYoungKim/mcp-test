package kakao.mcp

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport

/**
 * MCP 서버 예제 구현
 * Model Context Protocol의 QuickStart 예제를 바탕으로 구현
 */
class MCPServer {
    // 서버 인스턴스 생성
    private val server = Server(
        serverInfo = Implementation(
            name = "example-server",
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

    /**
     * 리소스를 추가하고 서버를 시작
     */
    suspend fun start() {
        println("MCP 서버 초기화 중...")

        // 예제 리소스 추가
        server.addResource(
            uri = "file:///example.txt",
            name = "예제 텍스트 파일",
            description = "MCP 테스트를 위한 예제 텍스트 파일",
            mimeType = "text/plain"
        ) { request ->
            println("리소스 요청 받음: ${request.uri}")
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        text = "이것은 예제 리소스의 내용입니다. MCP로 MCP 만들기 예제입니다.",
                        uri = request.uri,
                        mimeType = "text/plain"
                    )
                )
            )
        }

        // 두 번째 리소스 추가
        server.addResource(
            uri = "file:///hello.txt",
            name = "인사 메시지",
            description = "간단한 인사 메시지가 포함된 텍스트 파일",
            mimeType = "text/plain"
        ) { request ->
            println("리소스 요청 받음: ${request.uri}")
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        text = "안녕하세요! Model Context Protocol을 사용해 보세요!",
                        uri = request.uri,
                        mimeType = "text/plain"
                    )
                )
            )
        }

        // 표준 입출력(STDIO) 기반 Transport 생성 및 서버 시작
        println("MCP 서버 시작 중...")
        val transport = StdioServerTransport()
        server.connect(transport)
        println("MCP 서버가 시작되었습니다. STDIO 트랜스포트 사용 중...")

        // 실제 구현에서는 여기서 서버 프로세스를 계속 실행
    }

    /**
     * 서버 종료
     */
    fun stop() {
        println("MCP 서버를 종료합니다...")
        server.disconnect()
        println("MCP 서버가 종료되었습니다.")
    }
}

/**
 * 실행하기 위한 main 함수
 */
suspend fun main() {
    println("MCP 서버 예제를 시작합니다...")
    
    val server = MCPServer()
    server.start()
    
    // 실제 사용 시에는 서버를 계속 실행하기 위한 코드가 필요
    // 예제에서는 5초 후 종료
    println("5초 후 서버가 종료됩니다.")
    kotlinx.coroutines.delay(5000)
    
    server.stop()
    println("MCP 서버 예제가 종료되었습니다.")
}
