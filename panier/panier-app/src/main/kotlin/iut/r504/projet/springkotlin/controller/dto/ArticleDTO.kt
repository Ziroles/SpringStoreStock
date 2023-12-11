package iut.r504.projet.springkotlin.controller.dto



import iut.r504.projet.springkotlin.domain.ArticleQuantite

data class ArticleQuantiteDTO(
        val articleId: Long,
        var quantite: Int
)
fun ArticleQuantiteDTO.toDomain(): ArticleQuantite {
    return ArticleQuantite(
            articleId = this.articleId,
            quantite = this.quantite
    )
}

fun ArticleQuantite.toArticleQuantiteDTO(): ArticleQuantiteDTO {
    return ArticleQuantiteDTO(
            articleId = this.articleId,
            quantite = this.quantite
    )
}