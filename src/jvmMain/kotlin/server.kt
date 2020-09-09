import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*


fun main() {
    embeddedServer(Netty, port = 8090, host = "127.0.0.1") {
        routing {
            blog()
            session()

//            static("/static") {
//                resources()
//            }
        }

        install(Locations)
        install(Sessions) {
            cookie<LoginSession>("user")
        }
    }.start(wait = true)
}
