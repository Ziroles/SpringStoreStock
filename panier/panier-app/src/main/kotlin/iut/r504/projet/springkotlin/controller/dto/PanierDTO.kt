

package iut.r504.projet.springkotlin.controller.dto

import iut.r504.projet.springkotlin.domain.Panier
import iut.r504.projet.springkotlin.domain.ArticleQuantite
data class PanierDTO(

        val userEmail: String,
        val items: MutableList<ArticleQuantiteDTO>
)



// Si vous avez besoin de créer une instance de Panier à partir du DTO
fun PanierDTO.toDomain(): Panier {
    return Panier(

            userEmail = this.userEmail,
            items = this.items.map { it.toDomain() }.toMutableList()
    )
}
fun Panier.toPanierDTO(): PanierDTO {
    return PanierDTO(

            userEmail = this.userEmail,
            items = this.items.map { it.toArticleQuantiteDTO() }.toMutableList()
    )
}





