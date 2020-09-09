import io.ktor.application.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.html.*

data class LoginSession(val name: String)

val ExampleSession = LoginSession("testuser")

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
}
