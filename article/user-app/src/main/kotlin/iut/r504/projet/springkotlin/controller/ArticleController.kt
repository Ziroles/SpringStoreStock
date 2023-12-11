package iut.r504.projet.springkotlin.controller

import iut.r504.projet.springkotlin.errors.ArticleNotFoundError
import iut.r504.projet.springkotlin.controller.dto.ArticleDTO
import iut.r504.projet.springkotlin.controller.dto.asArticleDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import iut.r504.projet.springkotlin.repository.ArticleRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@Validated
class ArticleController(val articleRepository: ArticleRepository) {



    @Operation(summary = "List articles")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List articles",
            content = [Content(mediaType = "application/json",
                array = ArraySchema(
                    schema = Schema(implementation = ArticleDTO::class))
            )])])
    @GetMapping("/article")
    fun list(@RequestParam(required = false) name: String?) =
        articleRepository.list(name)
            .map { it.asArticleDTO() }
            .let {
                ResponseEntity.ok(it)
            }

    @Operation(summary = "Get article by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "The article",
            content = [
                Content(mediaType = "application/json",
                    schema = Schema(implementation = ArticleDTO::class))]),
        ApiResponse(responseCode = "404", description = "Article not found")
    ])
    @GetMapping("/article/{id}")
    fun findOne(@PathVariable id: Int): ResponseEntity<ArticleDTO> {
        val article = articleRepository.get(id)
        return if (article != null) {
            ResponseEntity.ok(article.asArticleDTO())
        } else {
            throw ArticleNotFoundError(id)
        }
    }


    @Operation(summary = "Add quantity to an article by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Quantity added successfully",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ArticleDTO::class))]),
        ApiResponse(responseCode = "404", description = "Article not found")
    ])
    @PatchMapping("/article/{id}/add-quantity")
    fun addQuantity(@PathVariable id: Int, @RequestParam @Min(1) quantity: Int): ResponseEntity<Any> {
        val article = articleRepository.get(id)
        return if (articleRepository.get(id) != null) {
            val updatedArticle = article!!.copy(quantity = article.quantity + quantity, lastUpdate = LocalDate.now())
            articleRepository.update(updatedArticle)
                .fold(
                    { success -> ResponseEntity.ok(success.asArticleDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            throw ArticleNotFoundError(id)
        }}
    @Operation(summary = "remove quantity to an article by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Quantity remove successfully",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ArticleDTO::class))]),
        ApiResponse(responseCode = "404", description = "Article not found")
    ])
    @PutMapping("/article/{id}/remove-quantity")
    fun removeQuantity(@PathVariable id: Int, @RequestParam @Min(1) quantity: Int): ResponseEntity<Any> {
        val article = articleRepository.get(id)
        return if (articleRepository.get(id) != null) {
            val updatedArticle = article!!.copy(quantity = article.quantity - quantity, lastUpdate = LocalDate.now())
            articleRepository.update(updatedArticle)
                .fold(
                    { success -> ResponseEntity.ok(success.asArticleDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            throw ArticleNotFoundError(id)
        }}
    @Operation(summary = "Check if quantity is enough")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Quantity is enough",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ArticleDTO::class))]),
        ApiResponse(responseCode = "404", description = "Article not found")
    ])
    @GetMapping("/article/{id}/check-quantity")
    fun checkQuantity(@PathVariable id: Int, @RequestParam @Min(1) quantity: Int): ResponseEntity<Any> {
        println("check quantity")
        val article = articleRepository.get(id)
        println(article!!.quantity)
        println(quantity)
        return if (articleRepository.get(id) != null) {
            println(article!!.quantity)
            println(quantity)
            if (article!!.quantity >= quantity){
                ResponseEntity.ok(article.asArticleDTO())
            }else{
                ResponseEntity.badRequest().body("Not enough quantity")
            }
        } else {
            throw ArticleNotFoundError(id)
        }}


}
