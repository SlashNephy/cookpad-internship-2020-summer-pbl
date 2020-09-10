
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.h2
import kotlinx.html.hr
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.service() {
    get("/") {
        call.respondHtmlTemplate(CooklogTemplate()) {
            contents {
                requireSession { session ->
                    appendSessionUserHeader(session)
                } or {
                    appendAnonymousUserHeader()
                }

                appendCategoryNavBar()

                hr()

                appendArticles()

//                div {
//                    id = "root"
//                }
//                script(src = "/static/output.js") {}

            }
        }
    }

    get("/category/{index}") {
        val index = call.parameters["index"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val category = RecipeCategory.values().getOrNull(index)
            ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondHtmlTemplate(CooklogTemplate()) {
            title = "${category.description} カテゴリ | Cooklog"
            contents {
                requireSession { session ->
                    appendSessionUserHeader(session)
                } or {
                    appendAnonymousUserHeader()
                }

                appendCategoryNavBar()

                hr()

                h2 { +"${category.description} カテゴリ" }

                appendArticles { Articles.category eq category }
            }
        }
    }

    get("/user/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val user = transaction(db) {
            Users.select { Users.username eq username }.single()
        }

        call.respondHtmlTemplate(CooklogTemplate()) {
            title = "${user[Users.name]} @${user[Users.username]} さんのページ | Cooklog"
            contents {
                requireSession { session ->
                    appendSessionUserHeader(session)
                } or {
                    appendAnonymousUserHeader()
                }

                appendCategoryNavBar()

                hr()

                h2 { +"${user[Users.name]} @${user[Users.username]} さんのページ" }

                appendArticles { Articles.authorId eq user[Users.id].value }
            }
        }
    }

    get("/my") {
        requireSession { session ->
            call.respondRedirect("/user/${session.username}")
        } or {
            call.respondRedirect("/signin")
        }
    }
}
