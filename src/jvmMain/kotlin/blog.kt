import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*

fun Routing.blog() {
    get("/") {
        val session = call.sessions.get<LoginSession>()

        if (session == null) {
            call.respondHtml(HttpStatusCode.OK, HTML::index)
        } else {
            call.respondHtml(HttpStatusCode.OK) {
                userIndex(session)
            }
        }
    }
}

fun HTML.index() {
    head {
        title("すごいブログサービス")
    }
    body {
        h1 { +"すごいブログサービス" }

        div {
            span {
                a(href = "/signup") {
                    +"サインアップ"
                }
            }
            span {
                +" | "
            }
            span {
                a(href = "/signin") {
                    +"サインイン"
                }
            }
        }

//        div {
//            id = "root"
//        }
//        script(src = "/static/output.js") {}
    }
}

fun HTML.userIndex(session: LoginSession) {
    head {
        title("すごいブログサービス")
    }
    body {
        div {
            p {
                +"${session.name} さん、こんにちは。"

                a(href = "/api/session/signout") {
                    +"サインアウト"
                }
            }
        }
    }
}
