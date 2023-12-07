package iut.r504.projet.springkotlin.repository.entity

import iut.r504.projet.springkotlin.domain.Panier
import jakarta.persistence.*

@Entity
@Table(name = "paniers")
class PanierEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Id val userEmail: String,
        @ElementCollection
        @CollectionTable(name = "panier_items", joinColumns = [JoinColumn(name = "panier_id")])
        val items: List<ArticleQuantiteEntity> = emptyList(),
) {
    // Constructors, methods, etc.

    // If you need to convert PanierEntity to Panier
    fun asPanier(): Panier {
        return Panier(id!!, userEmail, items.map { it.toDomain() })
    }
    // Fonction d'extension pour la conversion de Panier à PanierEntity

}
fun Panier.asEntity(): PanierEntity {
    return PanierEntity(this.id, this.userEmail, /* autres champs */)
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
