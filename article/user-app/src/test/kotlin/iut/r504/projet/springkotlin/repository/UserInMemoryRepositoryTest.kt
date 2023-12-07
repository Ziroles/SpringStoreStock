package iut.r504.projet.springkotlin.repository

import iut.r504.projet.springkotlin.repository.UserInMemoryRepository
import org.junit.jupiter.api.BeforeEach

class UserInMemoryRepositoryTest: UserDatabaseTest() {

    @BeforeEach
    fun setUp() {
        repository = UserInMemoryRepository()
    }
}