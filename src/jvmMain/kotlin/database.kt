
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

val db = Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")

object Users: IntIdTable() {
    val name = varchar("name", 128)
    val username = varchar("username", 16)
    val passwordHash = varchar("password_hash", 128)

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Images: IntIdTable() {
    val filename = varchar("filename", 128)
    val body = binary("body", 10 * 1024 * 1024)

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Articles: IntIdTable() {
    val title = varchar("title", 128)
    val description = varchar("description", 2048)
    val category = enumeration("category", RecipeCategory::class)
    val authorId = integer("author_id").references(Users.id)
    val imageId = integer("image_id").references(Images.id)

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

fun seed() {
    transaction(db) {
        SchemaUtils.drop(Users, Articles)
        SchemaUtils.create(Users, Articles)

        val users = (0..10).map { i ->
            Users.insertAndGetId {
                it[name] = "User #$i"
                it[username] = "user_$i"
                it[passwordHash] = hashPassword("password")

                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }

        val images = (1..7).map {  i ->
            Images.insertAndGetId {
                val name = "$i.jpg"
                it[filename] = name
                it[body] = this::class.java.classLoader.getResourceAsStream(name).readBytes()

                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }

        listOf(
            "ツナとキノコのトマトパスタ" to RecipeCategory.Noodle,
            "肉じゃが" to RecipeCategory.Meat,
            "明太子パスタ" to RecipeCategory.Noodle,
            "れんこんサラダ" to RecipeCategory.Vegitable,
            "鮭の和風ステーキ" to RecipeCategory.Fish,
            "グリル野菜のバルサミコマリネ" to RecipeCategory.Vegitable,
            "牛肉しぐれ煮" to RecipeCategory.Meat
        ).forEachIndexed { i, (name, genre) ->
            Articles.insert {
                it[title] = name
                it[description] = "かんたんに作れる $name です！"
                it[category] = genre
                it[authorId] = users.random().value

                it[imageId] = images[i].value

                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }
}