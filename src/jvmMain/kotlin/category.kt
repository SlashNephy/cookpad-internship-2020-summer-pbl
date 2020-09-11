enum class RecipeCategory {
    Vegitable, Meat, Noodle, Fish, Other;

    val description: String
        get() = when (this) {
            Vegitable -> "野菜"
            Meat -> "肉料理"
            Noodle -> "麺類"
            Fish -> "魚介"
            Other -> "その他"
        }
}

enum class Nutrition {
    Carbohydrates, Lipid, Protein, Mineral, Vitamin;

    val description: String
        get() = when (this) {
            Carbohydrates -> "炭水化物"
            Lipid -> "脂質"
            Protein -> "たんぱく質"
            Mineral -> "ミネラル"
            Vitamin -> "ビタミン"
        }
}
