package iut.r504.projet.springkotlin.controller

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import iut.r504.projet.springkotlin.domain.Article
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import iut.r504.projet.springkotlin.controller.dto.ArticleDTO
import iut.r504.projet.springkotlin.repository.ArticleRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import java.time.LocalDate

@SpringBootTest
class ArticleAdminControllerTest {
    @MockkBean
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var articleController: AdminArticleController

    @Nested
    inner class UpdateTests {
        @Test
        fun `update valid`() {

            every { articleRepository.update(any()) } returns Result.success(Article(10, "Ballon", 42.10f, 42, LocalDate.now()))
            val update = ArticleDTO(10, "Ballon", 42.10f, 42, LocalDate.now())

            val result = articleController.update(10, update)

            assertThat(result).isEqualTo(ResponseEntity.ok(update))
        }
        @Test
        fun `update a non-existing user`() {

            every { articleRepository.update(any()) } returns Result.failure(Exception("Nope"))
            val update = ArticleDTO(10, "Ballon", 42.10f, 42, LocalDate.now())

            val result = articleController.update(10, update)

            assertThat(result).isEqualTo(ResponseEntity.badRequest().body("Nope"))
        }

        @Test
        fun `update with two id`() {

            val update = ArticleDTO(1, "Ballon", 42.10f, 42, LocalDate.now())



            assertFailure {

                articleController.update(10, update)
            }




        }

    }

}