package iut.r504.projet.springkotlin.controller


import iut.r504.projet.springkotlin.errors.PanierNotFoundError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses



import iut.r504.projet.springkotlin.controller.dto.PanierDTO

import iut.r504.projet.springkotlin.controller.dto.toDomain
import iut.r504.projet.springkotlin.controller.dto.toPanierDTO
import iut.r504.projet.springkotlin.domain.ArticleQuantite
import iut.r504.projet.springkotlin.errors.Ensufficientquantity
import iut.r504.projet.springkotlin.repository.PanierRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@RestController
@Validated
class PanierController(private val panierRepository: PanierRepository) {
    val urlarticle = URL("http://localhost:8083/articleapi/article/")
    @Operation(summary = "Create panier")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Panier created",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class)
                )]),
        ApiResponse(responseCode = "409", description = "Panier already exists",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PostMapping("/paniers")
    fun create(@RequestBody @Valid panierDTO: PanierDTO): ResponseEntity<PanierDTO> =
            panierRepository.create(panierDTO.toDomain()).fold(
                    { success -> ResponseEntity.status(HttpStatus.CREATED).body(panierDTO) },
                    { failure -> ResponseEntity.status(HttpStatus.CONFLICT).build() })

    @Operation(summary = "List paniers")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List paniers",
                content = [Content(mediaType = "application/json",
                        array = ArraySchema(
                                schema = Schema(implementation = PanierDTO::class))
                )])])
    @GetMapping("/paniers")
    fun list(): ResponseEntity<List<PanierDTO>> =
            panierRepository.list()
                    .map { it.toPanierDTO() }
                    .let {
                        ResponseEntity.ok(it)
                    }

    @Operation(summary = "Get panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "The panier",
                content = [
                    Content(mediaType = "application/json",
                            schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @GetMapping("/paniers/{id}")
    fun findOne(@PathVariable id: String): ResponseEntity<PanierDTO> {
        val panier = panierRepository.get(id)
        return if (panier != null) {
            ResponseEntity.ok(panier.toPanierDTO())
        } else {
            throw PanierNotFoundError("Panier not found")
        }
    }

    @Operation(summary = "Update a panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Panier updated",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid request",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PutMapping("/paniers/{id}")
    fun updatePanier(@PathVariable id: String, @RequestBody @Valid panierDTO: PanierDTO): ResponseEntity<Any> {
        val existingPanier = panierRepository.get(id)

        return if (existingPanier == null) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Panier not found")
        } else {
            // Assurez-vous que l'email dans le DTO correspond à l'utilisateur du panier
            if (panierDTO.userEmail != existingPanier.userEmail) {
                ResponseEntity.badRequest().body("Invalid email")
            } else {
                val updatedPanier = panierRepository.update(panierDTO.toDomain()).fold(
                        { success -> success },
                        { failure -> return ResponseEntity.badRequest().body(failure.message) }
                )
                ResponseEntity.ok(updatedPanier.toPanierDTO())
            }
        }
    }
    @Operation(summary = "Validate panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Panier validated",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid request",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @PutMapping("/paniers/validate/{id}")
    fun validate(@PathVariable id: String): ResponseEntity<Any> {
        println("debut validation")
        try {
            var panier = panierRepository.get(id)
            if (id.contains("%40")) {
                panier = panierRepository.get(id.replace("%40", "@"))
            }
            panier!!.items.forEach {
                var url = urlarticle.toString() + it.articleId + "/check-quantity?quantity=" + it.quantite

                var connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                try {

                    val responseCode = connection.responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {

                        throw Ensufficientquantity("Erreur : $responseCode")
                    }
                } finally {

                    connection.disconnect()
                }
            }
            println("debut supp")
            panier!!.items.forEach(){
                var url = urlarticle.toString() + it.articleId + "/remove-quantity?quantity=" + it.quantite
                var connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                println(url)
                try {

                    val responseCode = connection.responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {

                        throw Ensufficientquantity("Erreur : $responseCode")
                    }
                } finally {
                    // Ferme la connexion
                    connection.disconnect()
                }
            }
            panierRepository.update(panier.copy(items = mutableListOf()))

            return ResponseEntity.status(HttpStatus.OK).body(panier.toPanierDTO())
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request")
        }
    }

    @Operation(summary = "Delete panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Panier deleted"),
        ApiResponse(responseCode = "400", description = "Panier not found",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @DeleteMapping("/paniers/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Any> {

        var deleteid =  id.replace("%40", "@")


        val deleted = panierRepository.delete(deleteid)
        return if (deleted == null) {
            ResponseEntity.badRequest().body("Panier not found")
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @Operation(summary = "Add article to panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Article added successfully",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PutMapping("/paniers/{id}/add-article")
    fun addArticle(@PathVariable id: String, @RequestParam @Min(1) articleId: Long, @RequestParam @Min(1) quantite: Int): ResponseEntity<Any> {

        var panier = panierRepository.get(id)
        if (id.contains("%40")) {
            panier = panierRepository.get(id.replace("%40", "@"))
        }

        return if (panierRepository.get(id) != null) {


            val url = urlarticle.toString() + articleId + "/check-quantity?quantity=" + (quantite+if(panier!!.items.find { it.articleId == articleId } != null) panier!!.items.find { it.articleId == articleId }!!.quantite else 0)
            val connection = URL(url).openConnection() as HttpURLConnection


            connection.requestMethod = "GET"

            try {
                // Récupère la réponse
                val responseCode = connection.responseCode

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    // Gestion des erreurs
                    throw Ensufficientquantity("Erreur : $responseCode")
                }
            } finally {
                // Ferme la connexion
                connection.disconnect()
            }
            if (panier.items.find { it.articleId == articleId } != null) {
                panier.items.map { if (it.articleId == articleId) it.quantite += quantite }
            }else{
                panier.items.add( ArticleQuantite(articleId, quantite))
            }


            panierRepository.update(panier)
                    .fold(
                            { success -> ResponseEntity.ok(success.toPanierDTO()) },
                            { failure -> ResponseEntity.badRequest().body(failure.message) }
                    )
        } else {

            throw PanierNotFoundError("Panier not found")
        }
    }
    @Operation(summary = "Remove article to panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Article removed successfully",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PutMapping("/paniers/{id}/remove-article")
    fun removeArticle(@PathVariable id: String, @RequestParam @Min(1) articleId: Long, @RequestParam @Min(1) quantite: Int): ResponseEntity<Any> {
        val panier = panierRepository.get(id)
        return if (panierRepository.get(id) != null) {
            if (panier!!.items.find { it.articleId == articleId } != null) {
                panier.items.map { if (it.articleId == articleId) it.quantite -= quantite }}
            if (panier!!.items.find { (it.articleId == articleId) && it.quantite <= 0 } != null){
                panier.items.removeIf { it.articleId == articleId }
            }


            panierRepository.update(panier)
                    .fold(
                            { success -> ResponseEntity.ok(success.toPanierDTO()) },
                            { failure -> ResponseEntity.badRequest().body(failure.message) }
                    )
        } else {
            throw PanierNotFoundError("Panier not found")
        }
    }

}
