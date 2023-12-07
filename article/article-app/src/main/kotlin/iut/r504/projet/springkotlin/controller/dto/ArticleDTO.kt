package bzh.zomzog.prez.springkotlin.controller.dto

import bzh.zomzog.prez.springkotlin.domain.Article
import jakarta.persistence.Id
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ArticleDTO(
        @field:Id val id: Int,
        @field:Size(min = 1, max = 30) val name: String,
        @field:Min(0) @field:Min(9999) val price: Float,
        @field:Min(15) @field:Max(120) val quantity: Int,
        @field:Min(15) @field:Max(120) val lastUpdate: LocalDate
) {

    fun asArticle() = Article(email, firstName, lastName, age)
}

fun Article.asArticleDTO() = UserDTO(this.email, this.firstName, this.lastName, this.age)