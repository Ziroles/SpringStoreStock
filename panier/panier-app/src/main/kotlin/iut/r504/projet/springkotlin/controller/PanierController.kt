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
import iut.r504.projet.springkotlin.errors.ArticleNotFoundError
import iut.r504.projet.springkotlin.errors.Ensufficientquantity
import iut.r504.projet.springkotlin.repository.PanierRepository
import jakarta.validation.constraints.Min
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

@RestController
@Validated
class PanierController(private val panierRepository: PanierRepository) {
    val urlarticle = URL("http://localhost:8083/articleapi/article/")
    private val logger: Logger = LoggerFactory.getLogger(PanierController::class.java)

    @Operation(summary = "List paniers")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List paniers",
                content = [Content(mediaType = "application/json",
                        array = ArraySchema(
                                schema = Schema(implementation = PanierDTO::class))
                )])])
    @GetMapping("/paniers")
    fun list(): ResponseEntity<List<Any>> {
        logger.info("Request for listing all panier")
        panierRepository.list()
            .map { it.toPanierDTO() }
            .let {return  ResponseEntity.ok(it)
            }

    }
    @Operation(summary = "Get panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "The panier",
                content = [
                    Content(mediaType = "application/json",
                            schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @GetMapping("/paniers/{email}")
    fun findOne(@PathVariable email: String): ResponseEntity<PanierDTO> {
        logger.info("Request for getting one panier")
        val panier = panierRepository.get(email.replace("%40", "@"))
        return if (panier != null) {
            logger.info("Panier found")
            ResponseEntity.ok(panier.toPanierDTO())
        } else {
            throw PanierNotFoundError("Panier not found")
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
    @PutMapping("/paniers/validate/{email}")
    fun validate(@PathVariable email: String): ResponseEntity<Any> {
        logger.info("Request to validate a user panier")
        try {
            var panier = panierRepository.get(email.replace("%40", "@"))

            panier!!.items.forEach {
                var url = urlarticle.toString() + it.articleId + "/check-quantity?quantity=" + it.quantite

                var connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                try {

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {

                        throw Ensufficientquantity(it.articleId.toString())
                    }else{
                        if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                            throw ArticleNotFoundError(it.articleId.toString())
                        }else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                            return ResponseEntity.badRequest().body("Error: $responseCode")
                        }
                    }
                } finally {

                    connection.disconnect()
                }
            }
            logger.info("Each article is in enough quantity")
            panier!!.items.forEach(){
                var url = urlarticle.toString() + it.articleId + "/remove-quantity?quantity=" + it.quantite
                var connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"

                try {

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {

                        throw Ensufficientquantity(it.articleId.toString())
                    }else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                        throw ArticleNotFoundError(it.articleId.toString())
                    }else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                        return ResponseEntity.badRequest().body("Error: $responseCode")
                    }
                } finally {
                    // Ferme la connexion
                    connection.disconnect()
                }
            }
            panierRepository.update(panier.copy(items = mutableListOf()))
            logger.info("Panier of $email is validate")
            return ResponseEntity.status(HttpStatus.OK).body(panier.toPanierDTO())
        } catch (e: ConnectException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connection refused: ${e.message}")
        }
    }



    @Operation(summary = "Add article to panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Article added successfully",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PutMapping("/paniers/{email}/add-article")
    fun addArticle(@PathVariable email: String, @RequestParam @Min(1) articleId: Long, @RequestParam @Min(1) quantite: Int): ResponseEntity<Any> {
        logger.info("Request to add article number $articleId to $email panier ")
        var panier = panierRepository.get(email.replace("%40", "@"))

        try{
        return if (panierRepository.get(email) != null) {


            val url = urlarticle.toString() + articleId + "/check-quantity?quantity=" + (quantite+if(panier!!.items.find { it.articleId == articleId } != null) panier!!.items.find { it.articleId == articleId }!!.quantite else 0)
            val connection = URL(url).openConnection() as HttpURLConnection


            connection.requestMethod = "GET"

            try {

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    println("Article not found")
                    throw ArticleNotFoundError(articleId.toString())
                }else{
                    if(responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE){
                        throw Ensufficientquantity( email)
                    }else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                        return ResponseEntity.badRequest().body("Error: $responseCode")
                    }
                }
            } finally {

                connection.disconnect()
            }
            if (panier.items.find { it.articleId == articleId } != null) {
                panier.items.map { if (it.articleId == articleId) it.quantite += quantite }
            }else{
                panier.items.add( ArticleQuantite(articleId, quantite))
            }

            logger.info("The quantity as been add")
            panierRepository.update(panier)
                    .fold(
                            { success -> ResponseEntity.ok(success.toPanierDTO()) },
                            { failure -> ResponseEntity.badRequest().body(failure.message) }
                    )
        } else {

            throw PanierNotFoundError(email)
        }}catch (e: ConnectException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connection refused: ${e.message}")
        }
    }
    @Operation(summary = "Remove article to panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Article removed successfully",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PutMapping("/paniers/{email}/remove-article")
    fun removeArticle(@PathVariable email: String, @RequestParam @Min(1) articleId: Long, @RequestParam @Min(1) quantite: Int): ResponseEntity<Any> {
        logger.info("Request to remove article number $articleId  from $email panier")
        val panier = panierRepository.get(email.replace("%40", "@"))
        return if (panierRepository.get(email) != null) {
            if (panier!!.items.find { it.articleId == articleId } != null) {
                panier.items.map { if (it.articleId == articleId) it.quantite -= quantite }}
            else{
                throw ArticleNotFoundError(articleId.toString())
            }
            if (panier!!.items.find { (it.articleId == articleId) && it.quantite <= 0 } != null){
                logger.info("Article removed")
                panier.items.removeIf { it.articleId == articleId }
            }


            panierRepository.update(panier)
                    .fold(
                            { success ->
                                logger.info("Request for listing all panier")
                                ResponseEntity.ok(success.toPanierDTO()) },
                            { failure -> ResponseEntity.badRequest().body(failure.message) }
                    )
        } else {
            throw PanierNotFoundError(email)
        }
    }

}
