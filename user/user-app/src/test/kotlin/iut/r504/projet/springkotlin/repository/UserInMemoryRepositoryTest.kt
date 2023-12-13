package iut.r504.projet.springkotlin.repository

import assertk.assertThat
import assertk.assertions.containsExactly
import iut.r504.projet.springkotlin.domain.User
import iut.r504.projet.springkotlin.repository.UserInMemoryRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UserInMemoryRepositoryTest: UserDatabaseTest() {

    @BeforeEach
    fun setUp() {
        repository = UserInMemoryRepository()
    }

    @Test
    fun `list all return items filtered by age`() {
        // GIVEN
        repository.create(User("a", "b", "c", 42, "d", true, LocalDate.of(2023,1,1)))
        repository.create(User("aa", "bb", "cc", 42, "dd", false, LocalDate.of(2023,8,12)))
        repository.create(User("nop", "bb", "cc", 24,"ddd", true, LocalDate.of(2022,12,6)))
        // WHEN
        val result = repository.list(42)
        // THEN
        assertThat(result).containsExactly(
                User("a", "b", "c", 42, "d", true, LocalDate.of(2023,1,1)),
                User("aa", "bb", "cc", 42, "dd", false, LocalDate.of(2023,8,12)),
        )
    }
}