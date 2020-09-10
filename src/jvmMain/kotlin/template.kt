import io.ktor.html.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class CooklogTemplate: Template<HTML> {
    var title = "Cooklog"
    val contents = Placeholder<BODY>()

    override fun HTML.apply() {
        head {
            title(this@CooklogTemplate.title)
        }

        body {
            h1 {
                a("/") {
                    +"Cooklog"
                }
            }

            insert(contents)
        }
    }
}

fun BODY.appendSessionUserHeader(session: LoginSession) {
    div {
        p {
            +"${session.username} さん、こんにちは。"

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
        }
    }
}

fun BODY.appendAnonymousUserHeader() {
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
}

fun BODY.appendCategoryNavBar() {
    div {
        nav {
            ul {
                RecipeCategory.values().forEach {
                    li {
                        a(href = "/category/${it.ordinal}") {
                            +it.description
                        }
                    }
                }
            }
        }
    }
}

fun BODY.appendArticles(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) {
    div {
        ul {
            transaction(db) {
                where.let {
                    if (it != null) {
                        Articles.select(it)
                    } else {
                        Articles.selectAll()
                    }
                }.limit(10)
                    .orderBy(Articles.updatedAt to SortOrder.ASC)
                    .toList()
            }.forEach {
                li {
                    img(classes = "image", src = "/api/image/${it[Articles.imageId]}")

                    h3 {
                        +it[Articles.title]
                    }
                    p {
                        +"by "
                        val author = transaction(db) {
                            Users.select { Users.id eq it[Articles.authorId] }.single()
                        }

                        a("/user/${author[Users.username]}") {
                            +author[Users.name]
                        }
                    }

                    p {
                        +it[Articles.description]
                    }
                }
            }
        }
    }
}
