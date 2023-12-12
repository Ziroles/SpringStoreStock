package iut.r504.projet.springkotlin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag;
import iut.r504.projet.springkotlin.controller.dto.ArticleDTO
import iut.r504.projet.springkotlin.controller.dto.asArticleDTO
import iut.r504.projet.springkotlin.repository.ArticleRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
@Tag(name = "Administration", description = "Operations related to article administration")
public class AdminArticleController(val articleRepository: ArticleRepository) {
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
        return articleRepository.create(article.copy(lastUpdate = LocalDate.now()).asArticle()).fold(
            { success -> ResponseEntity.status(HttpStatus.CREATED).body(success.asArticleDTO()) },
            { failure -> ResponseEntity.status(HttpStatus.CONFLICT).build() })}
    @Operation(summary = "Delete article by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Article deleted"),
        ApiResponse(responseCode = "400", description = "Article not found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @DeleteMapping("/article/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Any> {
        val deleted = articleRepository.delete(id)
        return if (deleted == null) {
            ResponseEntity.badRequest().body("User not found")
        } else {
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
    fun update(@PathVariable id: Int, @RequestBody @Valid article: ArticleDTO): ResponseEntity<Any> =
        if (id != article.id) {
            ResponseEntity.badRequest().body("Invalid id")
        } else {

            articleRepository.update(article.copy(lastUpdate = LocalDate.now()).asArticle()).fold(
                { success -> ResponseEntity.ok(success.asArticleDTO()) },
                { failure -> ResponseEntity.badRequest().body(failure.message) }
            )
        }
}
