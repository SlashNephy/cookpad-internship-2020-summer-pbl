
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

    val carbohydrates = bool("carbohydrates")
    val lipid = bool("lipid")
    val protein = bool("protein")
    val mineral = bool("mineral")
    val vitamin = bool("vitamin")

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

private data class Article(
        val index: Int,
        val name: String,
        val category: RecipeCategory,
        val carbohydrates: Boolean,
        val lipid: Boolean,
        val protein: Boolean,
        val mineral: Boolean,
        val vitamin: Boolean
)

fun seed() {
    transaction(db) {
        SchemaUtils.drop(Users, Images, Articles)
        SchemaUtils.create(Users, Images, Articles)

        val users = (0..5).map { i ->
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

                val resource = this::class.java.classLoader.getResourceAsStream(name)
                checkNotNull(resource)
                it[body] = resource.readBytes()

                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }

        val articles = listOf(
            Article(0, "ツナとキノコのトマトパスタ", RecipeCategory.Noodle, true, false, true, false, true),
            Article(1, "肉じゃが", RecipeCategory.Meat, true, true, true, false, true),
            Article(2, "明太子パスタ", RecipeCategory.Noodle, true, false, false, false, false),
            Article(3, "れんこんサラダ", RecipeCategory.Vegitable, false, false, false, true, true),
            Article(4, "鮭の和風ステーキ", RecipeCategory.Fish, false, false, true, false, false),
            Article(5, "グリル野菜のバルサミコマリネ", RecipeCategory.Vegitable, false, false, false, true, true),
            Article(6, "牛肉しぐれ煮", RecipeCategory.Meat, false, true, true, false, true)
        )

        repeat(100) { i ->
            val article = articles.random()

            Articles.insert {
                it[title] = "${article.name} #$i"
                it[description] = "かんたんに作れる ${article.name} です！"
                it[category] = article.category
                it[authorId] = users.random().value
                it[carbohydrates] = article.carbohydrates
                it[lipid] = article.lipid
                it[protein] = article.protein
                it[mineral] = article.mineral
                it[vitamin] = article.vitamin

                it[imageId] = images[article.index].value

                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }
}
