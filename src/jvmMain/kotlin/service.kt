
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
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
            val name = "${user[Users.name]} @${user[Users.username]}"

            title = "$name さんのページ | Cooklog"
            contents {
                requireSession { session ->
                    appendSessionUserHeader(session)
                } or {
                    appendAnonymousUserHeader()
                }

                appendCategoryNavBar()

                hr()


                h2 { +"$name さんのページ" }

                div {
                    canvas {
                        id = "category-chart"
                    }
                    script(src = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.3/Chart.bundle.min.js") {}
                    script {
                        unsafe {
                            val targetCategories = RecipeCategory.values().filter { it != RecipeCategory.Other }
                            val categories = targetCategories.joinToString(", ") { "\"${it.description}\"" }
                            val result = transaction(db) {
                                Articles.select { Articles.authorId eq user[Users.id].value }.toList()
                            }.map {
                                it[Articles.category]
                            }.groupBy { it }
                            val data = targetCategories.map {
                                result[it]?.size ?: 0
                            }.joinToString(", ")

                            // language=JS
                            +"""
const element = document.getElementById("category-chart");

new Chart(element, {
    type: "radar",
    data: {
        labels: [${categories}],
        datasets: [{
            label: "投稿された料理のカテゴリ",
            data: [$data],
            fill: true,
            backgroundColor: "rgba(54, 162, 235, 0.2)",
            borderColor: "rgb(54, 162, 235)",
            pointBackgroundColor: "rgb(54, 162, 235)",
            pointBorderColor: "#fff",
            pointHoverBackgroundColor: "#fff",
            pointHoverBorderColor: "rgb(54, 162, 235)"
        }]
    },
    options: {
        title: {
            display: false
        },
        scale: {
            ticks: {
                suggestedMin: 0,
                suggestedMax: 5,
                stepSize: 1
            }
        }
    }
});
                            """
                        }
                    }
                }

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
