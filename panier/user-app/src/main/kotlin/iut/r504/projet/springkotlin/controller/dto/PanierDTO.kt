

package iut.r504.projet.springkotlin.controller.dto

import iut.r504.projet.springkotlin.domain.Panier
import iut.r504.projet.springkotlin.domain.ArticleQuantite
data class PanierDTO(
        val panierId: Long,
        val userEmail: String,
        val items: List<ArticleQuantiteDTO>
)



// Si vous avez besoin de créer une instance de Panier à partir du DTO
fun PanierDTO.toDomain(): Panier {
    return Panier(
            id = this.panierId,
            userEmail = this.userEmail,
            items = this.items.map { it.toDomain() }
    )
}
fun Panier.toPanierDTO(): PanierDTO {
    return PanierDTO(
            panierId = this.id,
            userEmail = this.userEmail,
            items = this.items.map { it.toArticleQuantiteDTO() }
    )
}





