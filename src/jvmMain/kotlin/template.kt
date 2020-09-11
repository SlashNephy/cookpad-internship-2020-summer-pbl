import io.ktor.application.*
import io.ktor.html.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class CooklogTemplate(private val context: PipelineContext<*, ApplicationCall>): Template<HTML> {
    var title = "Cooklog"
    val contents = Placeholder<DIV>()

    override fun HTML.apply() {
        lang = "ja"

        head {
            meta(charset = "utf-8")
            title(this@CooklogTemplate.title)

            meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
            link(rel = "stylesheet", href = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css")
        }

        body {
            nav("navbar navbar-expand-lg navbar-light bg-light") {
                a("/", classes = "navbar-brand") {
                    +"Cooklog"
                }

                button(classes = "navbar-toggler", type = ButtonType.button) {
                    attributes["data-toggle"] = "collapse"
                    attributes["data-target"] = "#navbar-inner"

                    span(classes = "navbar-toggler-icon")
                }

                div("collapse navbar-collapse") {
                    id = "navbar-inner"

                    ul("navbar-nav mr-auto") {
                        RecipeCategory.values().forEach {
                            li("nav-item") {
                                a("/category/${it.ordinal}", classes = "nav-link") {
                                    +it.description
                                }
                            }
                        }

                        Nutrition.values().forEach {
                            li("nav-item") {
                                a("/nutrition/${it.ordinal}", classes = "nav-link") {
                                    +it.description
                                }
                            }
                        }
                    }

                    span("navbar-text") {
                        context.requireSession { session ->
                            +"@${session.username} さん、こんにちは。"

                            span {
                                a(href = "/my") {
                                    +"マイページ"
                                }
                            }
                            span {
                                +" | "
                            }
                            span {
                                a(href = "/signout") {
                                    +"サインアウト"
                                }
                            }
                        } or {
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
                    }
                }
            }

            script(src = "https://code.jquery.com/jquery-3.5.1.slim.min.js") {}
            script(src = "https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js") {}
            script(src = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js") {}

            div("container") {
                insert(contents)
            }
        }
    }
}

fun DIV.appendArticles(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) {
    div("row row-cols-1 row-cols-md-3") {

        transaction(db) {
            where.let {
                if (it != null) {
                    Articles.select(it)
                } else {
                    Articles.selectAll()
                }
            }.limit(20)
                    .orderBy(Articles.updatedAt to SortOrder.DESC)
                    .toList()
        }.forEach {
            div("col mb-3") {
                div("card") {
                    style = "width: 18rem;"

                    img(classes = "card-img-top", src = "/api/image/${it[Articles.imageId]}")

                    div("card-body") {
                        h5("card-title") {
                            +it[Articles.title]
                        }
                        h6("card-subtitle mb-2 text-muted") {
                            +"by "
                            val author = transaction(db) {
                                Users.select { Users.id eq it[Articles.authorId] }.single()
                            }

                            a("/user/${author[Users.username]}") {
                                +author[Users.name]
                            }
                        }

                        p("card-text") {
                            +it[Articles.description]
                        }
                    }
                }
            }
        }
    }
}
