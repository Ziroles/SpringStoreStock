package iut.r504.projet.springkotlin.controller

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import iut.r504.projet.springkotlin.controller.dto.ArticleDTO
import iut.r504.projet.springkotlin.controller.dto.asArticleDTO
import iut.r504.projet.springkotlin.domain.Article
import iut.r504.projet.springkotlin.repository.ArticleRepository
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.time.LocalDate

class ArticleControllerTest {

    @Test
    fun `list articles`() {
        // Given
        val articleRepository = mockk<ArticleRepository>()
        val articleController = ArticleController(articleRepository)

        val articles = listOf(
            Article(id = 1, name = "Article 1", price = 10.0f, quantity = 5, lastUpdate = LocalDate.now()),
            Article(id = 2, name = "Article 2", price = 15.0f, quantity = 8, lastUpdate = LocalDate.now())
        )

        every { articleRepository.list() } returns articles


        val result: ResponseEntity<Any> = articleController.list()


        assertThat(result.statusCodeValue).isEqualTo(200)
        assertThat(result.body).isEqualTo(articles.map { it.asArticleDTO() })
    }

    @Test
    fun `get article by id`() {

        val articleRepository = mockk<ArticleRepository>()
        val articleController = ArticleController(articleRepository)
        val articleId = 1

        val article = Article(id = articleId, name = "Test Article", price = 10.0f, quantity = 10, lastUpdate = LocalDate.now())

        every { articleRepository.get(articleId) } returns article


        val result: ResponseEntity<ArticleDTO> = articleController.findOne(articleId)


        assertThat(result).isEqualTo(ResponseEntity.ok(article.asArticleDTO()))
        assertThat(result.body).isEqualTo(article.asArticleDTO())
    }

    @Test
    fun `remove quantity from an article by id`() {

        val articleRepository = mockk<ArticleRepository>()
        val articleController = ArticleController(articleRepository)
        val articleId = 1
        val quantityToRemove = 3

        val originalArticle = Article(id = articleId, name = "Test Article", price = 10.0f, quantity = 10, lastUpdate = LocalDate.now())
        val updatedArticle = originalArticle.copy(quantity = originalArticle.quantity - quantityToRemove)

        every { articleRepository.get(articleId) } returns originalArticle
        every { articleRepository.update(updatedArticle) } returns Result.success(updatedArticle)


        val result: ResponseEntity<Any> = articleController.removeQuantity(articleId, quantityToRemove)


        assertThat(result).isEqualTo(ResponseEntity.ok(updatedArticle.asArticleDTO()))
        assertThat(result.body).isEqualTo(updatedArticle.asArticleDTO())
    }

    @Test
    fun `check quantity for an article`() {

        val articleRepository = mockk<ArticleRepository>()
        val articleController = ArticleController(articleRepository)
        val articleId = 1
        val requiredQuantity = 5

        val article = Article(id = articleId, name = "Test Article", price = 10.0f, quantity = 10, lastUpdate = LocalDate.now())

        every { articleRepository.get(articleId) } returns article


        val result: ResponseEntity<Any> = articleController.checkQuantity(articleId, requiredQuantity)


        assertThat(result).isEqualTo(ResponseEntity.ok(article.asArticleDTO()))
        assertThat(result.body).isEqualTo(article.asArticleDTO())
    }

    @Test
    fun `add quantity to a non-existing article`() {

        val articleRepository = mockk<ArticleRepository>()
        val articleController = ArticleController(articleRepository)
        val articleId = 1
        val quantityToAdd = 5

        every { articleRepository.get(articleId) } returns null

        assertFailure {
            val result: ResponseEntity<Any> = articleController.addQuantity(articleId, quantityToAdd)
        }


    }

    @Test
    fun `remove quantity from a non-existing article`() {

        val articleRepository = mockk<ArticleRepository>()
        val articleController = ArticleController(articleRepository)
        val articleId = 1
        val quantityToRemove = 3

        every { articleRepository.get(articleId) } returns null

        assertFailure {
            articleController.removeQuantity(articleId, quantityToRemove)
        }


    }



}
