
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
        call.respondHtmlTemplate(CooklogTemplate(this)) {
            contents {
                nav {
                    ol("breadcrumb") {
                        li("breadcrumb-item active") {
                            +"みんなの新着記事"
                        }
                    }
                }

                appendArticles()
            }
        }
    }

    get("/category/{index}") {
        val index = call.parameters["index"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val category = RecipeCategory.values().getOrNull(index)
            ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondHtmlTemplate(CooklogTemplate(this)) {
            title = "${category.description} カテゴリ | Cooklog"
            contents {
                nav {
                    ol("breadcrumb") {
                        li("breadcrumb-item active") {
                            +"${category.description} カテゴリ"
                        }
                    }
                }

                appendArticles { Articles.category eq category }
            }
        }
    }

    get("/nutrition/{index}") {
        val index = call.parameters["index"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val nutrition = Nutrition.values().getOrNull(index)
            ?: return@get call.respond(HttpStatusCode.NotFound)

        call.respondHtmlTemplate(CooklogTemplate(this)) {
            title = "${nutrition.description} カテゴリ | Cooklog"
            contents {
                nav {
                    ol("breadcrumb") {
                        li("breadcrumb-item active") {
                            +"${nutrition.description} カテゴリ"
                        }
                    }
                }

                appendArticles {
                    when (nutrition) {
                        Nutrition.Carbohydrates -> Articles.carbohydrates eq true
                        Nutrition.Lipid -> Articles.lipid eq true
                        Nutrition.Protein -> Articles.protein eq true
                        Nutrition.Mineral -> Articles.mineral eq true
                        Nutrition.Vitamin -> Articles.vitamin eq true
                    }
                }
            }
        }
    }

    get("/user/{username}") {
        val username = call.parameters["username"]
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val user = transaction(db) {
            Users.select { Users.username eq username }.single()
        }

        call.respondHtmlTemplate(CooklogTemplate(this)) {
            val name = "${user[Users.name]} @${user[Users.username]}"

            title = "$name さんのページ | Cooklog"
            contents {
                nav {
                    ol("breadcrumb") {
                        li("breadcrumb-item active") {
                            +"$name さんのページ"
                        }
                    }
                }

                div {
                    canvas {
                        id = "nutrition-chart"
                    }
                    canvas {
                        id = "category-chart"
                    }

                    script(src = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.3/Chart.bundle.min.js") {}
                    script {
                        unsafe {
                            val nutritions = Nutrition.values().joinToString(", ") { "\"${it.description}\"" }
                            val result = transaction(db) {
                                Articles.select { Articles.authorId eq user[Users.id].value }.toList()
                            }.map {
                                listOf(it[Articles.carbohydrates], it[Articles.lipid], it[Articles.protein], it[Articles.mineral], it[Articles.vitamin])
                            }
                            val carbohydrates = result.count { it[0] }
                            val lipid = result.count { it[1] }
                            val protein = result.count { it[2] }
                            val mineral = result.count { it[3] }
                            val vitamin = result.count { it[4] }

                            // language=JS
                            +"""
new Chart(document.getElementById("nutrition-chart"), {
    type: "radar",
    data: {
        labels: [$nutritions],
        datasets: [{
            label: "投稿された料理の栄養価チャート",
            data: [$carbohydrates, $lipid, $protein, $mineral, $vitamin],
            fill: true,
            backgroundColor: "rgba(255, 99, 132, 0.2)",
            borderColor: "rgb(255, 99, 132)",
            pointBackgroundColor: "rgb(255, 99, 132)",
            pointBorderColor: "#fff",
            pointHoverBackgroundColor: "#fff",
            pointHoverBorderColor: "rgb(255, 99, 132)"
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
new Chart(document.getElementById("category-chart"), {
    type: "radar",
    data: {
        labels: [${categories}],
        datasets: [{
            label: "投稿された料理のカテゴリチャート",
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
