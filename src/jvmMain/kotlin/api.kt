
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.api() {
    route("/api") {
        route("/article") {
            post("/create") {
                call.respondRedirect("/my")
            }
        }

        get("/image/{id}") {
            val imageId = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val image = transaction(db) {
                Images.select { Images.id eq imageId }.single()
            }

            call.respondBytes(image[Images.body], ContentType.Image.JPEG)
        }
    }
}
