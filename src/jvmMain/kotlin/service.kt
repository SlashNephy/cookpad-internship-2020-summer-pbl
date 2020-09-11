
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

                requireSession {
                    if (it.username != user[Users.username]) {
                        return@requireSession
                    }

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
                    div("alert alert-warning") {
                        val minNutrition = when (minOf(carbohydrates, lipid, protein, mineral, vitamin)) {
                            vitamin -> Nutrition.Vitamin
                            mineral -> Nutrition.Mineral
                            carbohydrates -> Nutrition.Carbohydrates
                            protein -> Nutrition.Protein
                            else -> Nutrition.Lipid
                        }

                        +"「"
                        a("/nutrition/${minNutrition.ordinal}", classes = "alert-link") {
                            +minNutrition.description
                        }
                        +"」の栄養価が少ない傾向があります。"

                        val category = minNutrition.suggestedCategory
                        a("/category/${category.ordinal}", classes = "alert-link") {
                            +category.description
                        }
                        +" カテゴリの料理がおすすめです！"
                    }

                    script(src = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.3/Chart.bundle.min.js") {}
                    canvas {
                        id = "nutrition-chart"
                    }
                    canvas {
                        id = "category-chart"
                    }

                    script {
                        unsafe {
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

                nav {
                    ol("breadcrumb") {
                        li("breadcrumb-item active") {
                            +"$name さんの投稿"
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

    get("/article/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.NotFound)
        val article = transaction(db) {
            Articles.select { Articles.id eq id }.single()
        }
    }

    get("/create") {
        requireSession { session ->
            call.respondHtmlTemplate(CooklogTemplate(this)) {
                title = "投稿 | Cooklog"

                contents {
                    form(action = "/api/article/create", method = FormMethod.post) {
                        div("form-group") {
                            label {
                                attributes["for"] = "title"
                                +"タイトル"
                            }
                            input(classes = "form-control", type = InputType.text) {
                                id = "title"
                                name = "title"
                            }
                        }

                        div("form-group") {
                            label {
                                attributes["for"] = "description"
                                +"本文:"
                            }
                            textArea(classes = "form-control") {
                                id = "description"
                                name = "description"
                            }
                        }

                        div {
                            RecipeCategory.values().forEach {
                                div("form-check form-check-inline") {
                                    input(InputType.radio, classes = "form-check-input", name = "category") {
                                        id = "category-${it.ordinal}"
                                    }
                                    label("form-check-label") {
                                        attributes["for"] = "category-${it.ordinal}"
                                        +it.description
                                    }
                                }
                            }
                        }

                        div {
                            Nutrition.values().forEach {
                                div("form-check form-check-inline") {
                                    input(InputType.checkBox, classes = "form-check-input", name = "nutrition") {
                                        id = "nutrition-${it.ordinal}"
                                    }
                                    label("form-check-label") {
                                        attributes["for"] = "nutrition-${it.ordinal}"
                                        +it.description
                                    }
                                }
                            }
                        }

                        div("custom-file") {
                            input(InputType.file, classes = "custom-file-input") {
                                id = "image"
                            }
                            label("custom-file-label") {
                                attributes["for"] = "image"
                                +"画像を選択"
                            }
                        }

                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"投稿"
                        }
                    }
                }
            }
        } or {
            call.respondRedirect("/signin")
        }
    }
}
