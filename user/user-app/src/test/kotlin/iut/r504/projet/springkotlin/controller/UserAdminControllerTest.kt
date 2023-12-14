package iut.r504.projet.springkotlin.controller

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import iut.r504.projet.springkotlin.controller.dto.UserDTO
import iut.r504.projet.springkotlin.domain.User
import iut.r504.projet.springkotlin.repository.UserRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import iut.r504.projet.springkotlin.errors.UserNotFoundError
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import java.time.LocalDate

@SpringBootTest
class UserAdminControllerTest {
    @MockkBean
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userController: UserController

    @Autowired
    lateinit var userAdminController : UserAdminController

    @Nested
    inner class UpdateTests {
        @Test
        fun `update valid`() {
            val updatedUser = User("email@email.com", "first", "last", 41, "2 rue des sables, 44500 Saint Sébastien", true, LocalDate.of(2023,1,1))
            every { userRepository.update(any()) } returns Result.success(updatedUser)

            val update = UserDTO("email@email.com", "first", "last", 41, "2 rue des sables, 44500 Saint Sébastien", true, LocalDate.of(2023,1,1))

            val result = userAdminController.update("email@email.com", update)

            assertThat(result).isEqualTo(ResponseEntity.ok(update))
        }

        @Test
        fun `update a non-existing user`() {

            every { userRepository.update(any()) } returns Result.failure(Exception("Nope"))
            val update = UserDTO("email@email.com", "first", "last", 42, "1 rue des sables, 44500 Saint Sébastien", true, LocalDate.of(2023,1,1))

            val result = userAdminController.update("email@email.com", update)

            assertThat(result).isEqualTo(ResponseEntity.badRequest().body("Nope"))
        }

        @Test
        fun `update with two emails`() {

            val update = UserDTO("email@email.com", "first", "last", 42, "1 rue des sables, 44500 Saint Sébastien", true, LocalDate.of(2023,1,1))
            assertFailure {
                userAdminController.update("another@email.com", update)
            }

        }
    }

}