
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*


fun main() {
    embeddedServer(Netty, port = 8090, host = "127.0.0.1") {
        install(CallLogging)
        install(Sessions) {
            cookie<LoginSession>("user")
        }

        routing {
            service()
            api()
            session()

//            static("/static") {
//                resources()
//            }
        }
    }.start(wait = true)
}
