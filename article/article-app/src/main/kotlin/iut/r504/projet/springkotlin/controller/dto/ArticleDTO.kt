package iut.r504.projet.springkotlin.controller.dto

import iut.r504.projet.springkotlin.domain.Article
import jakarta.persistence.Id
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ArticleDTO(
        @field:Id val id: Int,
        @field:Size(min = 1, max = 30) val name: String,
        @field:Min(0) val price: Float,
        @field:Min(15) @field:Max(120) val quantity: Int,
        @field:Valid val lastUpdate: LocalDate
) {

    fun asArticle() = Article(id, name, price, quantity, lastUpdate)
}

fun Article.asArticleDTO() = ArticleDTO(this.id, this.name, this.price, this.quantity, this.lastUpdate)
