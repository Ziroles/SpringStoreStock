package iut.r504.projet.springkotlin.repository.entity

import iut.r504.projet.springkotlin.domain.ArticleQuantite
import iut.r504.projet.springkotlin.domain.Panier
import jakarta.persistence.*

@Entity
@Table(name = "paniers")
class PanierEntity(

        @Id val userEmail: String,
        @ElementCollection
        @CollectionTable(name = "panier_items", joinColumns = [JoinColumn(name = "panier_id")])
        var items: MutableList<ArticleQuantiteEntity> = mutableListOf(),
) {

    fun asPanier(): Panier {
        return Panier(userEmail, items.map { it.toDomain() }.toMutableList())
    }


}
fun Panier.asEntity(): PanierEntity {
    return PanierEntity(this.userEmail, this.items.map { it.toEntity() }.toMutableList())
}

fun ArticleQuantite.toEntity(): ArticleQuantiteEntity {
    return ArticleQuantiteEntity(this.articleId, this.quantite)
}

@Embeddable
data class ArticleQuantiteEntity(
        val articleId: Long,
        val quantite: Int
){
    fun toDomain(): iut.r504.projet.springkotlin.domain.ArticleQuantite {
        return iut.r504.projet.springkotlin.domain.ArticleQuantite(
                articleId = this.articleId,
                quantite = this.quantite
        )
    }
}
