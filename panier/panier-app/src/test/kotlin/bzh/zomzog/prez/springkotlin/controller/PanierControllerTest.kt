package iut.r504.projet.springkotlin.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import iut.r504.projet.springkotlin.controller.dto.PanierDTO
import iut.r504.projet.springkotlin.controller.dto.ArticleQuantiteDTO
import iut.r504.projet.springkotlin.domain.Panier
import iut.r504.projet.springkotlin.repository.PanierRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity

@SpringBootTest
class PanierControllerTest {
    @MockkBean
    lateinit var panierRepository: PanierRepository

    @Autowired
    lateinit var panierController: PanierController

    @Nested
    inner class UpdateTests {
        @Test
        fun `update valid panier`() {
            // GIVEN
            every { panierRepository.update(any()) } returns Result.success(Panier(1, "email@email.com", emptyList()))
            val update = PanierDTO(1, "email@email.com", listOf(ArticleQuantiteDTO(1, 2)))
            // WHEN
            val result = panierController.updatePanier(1, update)
            // THEN
            assertThat(result).isEqualTo(ResponseEntity.ok(update))
        }

        @Test
        fun `update a non-existing panier`() {
            // GIVEN
            every { panierRepository.update(any()) } returns Result.failure(Exception("Nope"))
            val update = PanierDTO(1, "email@email.com", emptyList())
            // WHEN
            val result = panierController.updatePanier(1, update)
            // THEN
            assertThat(result).isEqualTo(ResponseEntity.badRequest().body("Nope"))
        }

        @Test
        fun `update with two panier IDs`() {
            // GIVEN
            val update = PanierDTO(1, "email@email.com", emptyList())
            // WHEN
            val result = panierController.updatePanier(2, update)
            // THEN
            assertThat(result).isEqualTo(ResponseEntity.badRequest().body("Invalid panier ID"))
        }
    }
}
