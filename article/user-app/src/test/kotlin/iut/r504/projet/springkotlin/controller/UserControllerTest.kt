package iut.r504.projet.springkotlin.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import iut.r504.projet.springkotlin.domain.Article
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import iut.r504.projet.springkotlin.controller.ArticleController
import iut.r504.projet.springkotlin.controller.dto.ArticleDTO
import iut.r504.projet.springkotlin.repository.ArticleRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import java.time.LocalDate

@SpringBootTest
class UserControllerTest {
    @MockkBean
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var articleController: ArticleController

    @Nested
    inner class UpdateTests {
        @Test
        fun `update valid`() {
            // GIVEN
            every { articleRepository.update(any()) } returns Result.success(Article("email@email.com", "first", "last", 42))
            val update = ArticleDTO("email@email.com", "first", "last", 42)
            // WHEN
            val result = articleController.update("email@email.com", update)
            // THEN
            assertThat(result).isEqualTo(ResponseEntity.ok(update))
        }
        @Test
        fun `update a non-existing user`() {
            // GIVEN
            every { articleRepository.update(any()) } returns Result.failure(Exception("Nope"))
            val update = ArticleDTO("email@email.com", "first", "last", 42)
            // WHEN
            val result = articleController.update("email@email.com", update)
            // THEN
            assertThat(result).isEqualTo(ResponseEntity.badRequest().body("Nope"))
        }

        @Test
        fun `update with two emails`() {
            // GIVEN
            val update = ArticleDTO(10, "Ballon", 42.10f, 42, LocalDate(2023,12,7))
            // WHEN
            val result = articleController.update("another@email.com", update)
            // THEN
            assertThat(result).isEqualTo(ResponseEntity.badRequest().body("Invalid email"))
        }
    }

}