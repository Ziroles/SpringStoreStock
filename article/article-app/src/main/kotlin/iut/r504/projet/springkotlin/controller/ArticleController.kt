package bzh.zomzog.prez.springkotlin.controller

import bzh.zomzog.prez.springkotlin.controller.dto.ArticleDTO
import bzh.zomzog.prez.springkotlin.controller.dto.UserDTO
import bzh.zomzog.prez.springkotlin.controller.dto.asArticleDTO
import bzh.zomzog.prez.springkotlin.controller.dto.asUserDTO
import bzh.zomzog.prez.springkotlin.repository.ArticleRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

class ArticleController(val articleRepository: ArticleRepository) {

    @Operation(summary = "Create user")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Article created",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = ArticleDTO::class)
                )]),
        ApiResponse(responseCode = "409", description = "User already exist",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PostMapping("/api/users")
    fun create(@RequestBody @Valid article: ArticleDTO): ResponseEntity<ArticleDTO> =
            articleRepository.create(article.asArticle()).fold(
                    { success -> ResponseEntity.status(HttpStatus.CREATED).body(success.asArticleDTO()) },
                    { failure -> ResponseEntity.status(HttpStatus.CONFLICT).build() })
}