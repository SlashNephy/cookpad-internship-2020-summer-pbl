enum class RecipeCategory {
    Vegitable, Meat, Noodle, Fish, Other;

    val description: String
        get() = when (this) {
            Vegitable -> "野菜"
            Meat -> "肉"
            Noodle -> "麺類"
            Fish -> "魚介"
            Other -> "その他"
        }
}
