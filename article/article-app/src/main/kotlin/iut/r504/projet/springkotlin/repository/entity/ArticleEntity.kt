package bzh.zomzog.prez.springkotlin.repository.entity

import bzh.zomzog.prez.springkotlin.domain.Article
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "users")
class ArticleEntity(
        @Id val id: Int,
        val name: String,
        val price: Float,
        val quantity: Int,
        val lastUpdate : LocalDate
) {
    fun asArticle() = Article(this.id, this.name, this.price, this.quantity, this.lastUpdate)
}
fun Article.asEntity() = ArticleEntity(this.id, this.name, this.price, this.quantity, this.lastUpdate)