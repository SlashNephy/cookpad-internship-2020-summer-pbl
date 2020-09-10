import io.ktor.application.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

data class LoginSession(val name: String)

val ExampleSession = LoginSession("testuser")
private const val salt = "c68e64b2d83502b6ff1150db31b6dd28ea6944a3c9f9c3af2f18b9ce9f05b04dc53080be9a5c3c9cc3048efa35086d124441ab81bdda79f534605f6c9a5effc1"

fun hashPassword(password: String): String {
    val result = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return DatatypeConverter.printHexBinary(result).toLowerCase()
}

fun Routing.session() {
    route("/api") {
        route("/session") {
            post("/signup") {
                call.sessions.set(ExampleSession)
                call.respondRedirect("/")
            }

            post("/signin") {
                call.sessions.set(ExampleSession)
                call.respondRedirect("/")
            }

            get("/signout") {
                call.sessions.clear<LoginSession>()
                call.respondRedirect("/")
            }
        }
    }

    get("/signup") {
        call.respondHtml {
            head {
                title("サインアップ | すごいブログサービス")
            }
            body {
                div {
                    form(action = "/api/session/signup", method = FormMethod.post) {
                        ul {
                            li {
                                label {
                                    attributes["for"] = "username"
                                    +"Username:"
                                }
                                input(type = InputType.text) {
                                    id = "username"
                                    name = "username"
                                }
                            }

                            li {
                                label {
                                    attributes["for"] = "password"
                                    +"Password:"
                                }
                                input(type = InputType.password) {
                                    id = "password"
                                    name = "password"
                                }
                            }

                            li {
                                button(type = ButtonType.submit) {
                                    +"登録"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/signin") {
        call.respondHtml {
            head {
                title("サインイン | すごいブログサービス")
            }
            body {
                div {
                    form(action = "/api/session/signin", method = FormMethod.post) {
                        ul {
                            li {
                                label {
                                    attributes["for"] = "username"
                                    +"Username:"
                                }
                                input(type = InputType.text) {
                                    id = "username"
                                    name = "username"
                                }
                            }

                            li {
                                label {
                                    attributes["for"] = "password"
                                    +"Password:"
                                }
                                input(type = InputType.password) {
                                    id = "password"
                                    name = "password"
                                }
                            }

                            li {
                                button(type = ButtonType.submit) {
                                    +"サインイン"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/signout") {
        call.respondRedirect("/api/session/signout")
    }
}
