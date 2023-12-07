package iut.r504.projet.springkotlin.repository

import assertk.assertThat
import assertk.assertions.containsExactly
import iut.r504.projet.springkotlin.domain.Article
import iut.r504.projet.springkotlin.repository.UserDatabaseRepository
import iut.r504.projet.springkotlin.repository.UserJpaRepository
import iut.r504.projet.springkotlin.repository.entity.UserEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class UserDatabaseRepositoryTest: UserDatabaseTest() {

    @Autowired
    private lateinit var jpa: UserJpaRepository

    @BeforeEach
    fun setUp() {
        repository = UserDatabaseRepository(jpa)
        jpa.deleteAll()
    }

    @Test
    fun `list all return items filtered by age`() {
        // GIVEN
        jpa.save(UserEntity("a", "b", "c", 42))
        jpa.save(UserEntity("aa", "bb", "cc", 42))
        jpa.save(UserEntity("nop", "bb", "cc", 24))
        // WHEN
        val result = repository.list(42)
        // THEN
        assertThat(result).containsExactly(
            Article("a", "b", "c", 42),
            Article("aa", "bb", "cc", 42),
        )
    }
}