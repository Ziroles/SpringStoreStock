package iut.r504.projet.springkotlin.repository.entity

import iut.r504.projet.springkotlin.domain.Article
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "article")
class ArticleEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)  val id: Int = 1,
        val name: String,
        val price: Float,
        val quantity: Int,
        val lastUpdate : LocalDate
) {
    fun asArticle() = Article(this.id, this.name, this.price, this.quantity, this.lastUpdate)
}
fun Article.asEntity() = ArticleEntity(this.id, this.name, this.price, this.quantity, this.lastUpdate)