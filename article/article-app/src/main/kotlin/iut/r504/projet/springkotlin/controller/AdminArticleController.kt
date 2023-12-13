package iut.r504.projet.springkotlin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag;
import iut.r504.projet.springkotlin.controller.dto.ArticleDTO
import iut.r504.projet.springkotlin.controller.dto.asArticleDTO
import iut.r504.projet.springkotlin.errors.ArticleNotFoundError
import iut.r504.projet.springkotlin.repository.ArticleRepository
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
@Tag(name = "Administration", description = "Operations related to article administration")
public class AdminArticleController(val articleRepository: ArticleRepository) {
    private val logger: Logger = LoggerFactory.getLogger(AdminArticleController::class.java)
    @Operation(summary = "Create article")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Article created",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ArticleDTO::class)
            )]),
        ApiResponse(responseCode = "409", description = "Article already exist",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PostMapping("/article")
    fun create(@RequestBody @Valid article: ArticleDTO): ResponseEntity<ArticleDTO> {
        logger.info("Request to creat Article : ${article.name}")
        return articleRepository.create(article.copy(lastUpdate = LocalDate.now()).asArticle()).fold(
            { success -> logger.info("Article created : ${success.name}")
                ResponseEntity.status(HttpStatus.CREATED).body(success.asArticleDTO()) },
            { failure -> ResponseEntity.status(HttpStatus.CONFLICT).build() })}
    @Operation(summary = "Delete article by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Article deleted"),
        ApiResponse(responseCode = "400", description = "Article not found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @DeleteMapping("/article/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Any> {
        logger.info("Request to delet Article : $id")
        val deleted = articleRepository.delete(id)
        return if (deleted == null) {
            ResponseEntity.badRequest().body("User not found")
        } else {
            logger.info("Article deleted : $id")
            ResponseEntity.noContent().build()
        }
    }
    @Operation(summary = "Update an article by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Article updated",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = ArticleDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid request",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PutMapping("/article/{id}")
    fun update(@PathVariable id: Int, @RequestBody @Valid article: ArticleDTO): ResponseEntity<Any> {
        logger.info("Request to update Article : ${article.name}")
        if (id != article.id) {
            throw ArticleNotFoundError(id)
        } else {

            articleRepository.update(article.copy(lastUpdate = LocalDate.now()).asArticle()).fold(
                { success -> ResponseEntity.ok(success.asArticleDTO()) },
                { failure -> ResponseEntity.badRequest().body(failure.message) }
            )
        }
        logger.info("Article updated : ${article.name}")
        return ResponseEntity.ok(article)
    }
}
