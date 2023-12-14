import iut.r504.projet.springkotlin.domain.Article
import iut.r504.projet.springkotlin.repository.ArticleDatabaseRepository
import iut.r504.projet.springkotlin.repository.entity.ArticleEntity
import iut.r504.projet.springkotlin.repository.entity.asEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDate

@DataJpaTest
class ArticleDatabaseRepositoryTest {


    lateinit var articleRepository: ArticleDatabaseRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun testCreateArticle() {
        val article = Article(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now())

        val result = articleRepository.create(article)

        assertTrue(result.isSuccess)
        assertEquals(article, result.getOrNull())
    }

    @Test
    fun testCreateDuplicateArticle() {
        val existingArticle = ArticleEntity(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now())
        entityManager.persist(existingArticle)

        val article = Article(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now())

        val result = articleRepository.create(article)

        assertTrue(result.isFailure)
        assertEquals("Article already in DB", result.exceptionOrNull()?.message)
    }

    @Test
    fun testListArticles() {
        val articles = listOf(
            ArticleEntity(1, "Test Article 1", (15.5).toFloat(), 1, LocalDate.now()),
            ArticleEntity(2, "Test Article 2", (15.5).toFloat(), 1, LocalDate.now()),
            ArticleEntity(3, "Test Article 3", (15.5).toFloat(), 1, LocalDate.now())
        )

        articles.forEach { entityManager.persist(it) }

        val result = articleRepository.list()

        assertEquals(articles.map { it.asArticle() }, result)
    }

    @Test
    fun testListArticlesByName() {
        val articles = listOf(
            ArticleEntity(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now()),
            ArticleEntity(2, "Test Article", (15.5).toFloat(), 1, LocalDate.now()),
            ArticleEntity(3, "Another Article", (15.5).toFloat(), 1, LocalDate.now())
        )

        articles.forEach { entityManager.persist(it) }

        val result = articleRepository.list("Test Article")

        assertEquals(articles.filter { it.name == "Test Article" }.map { it.asArticle() }, result)
    }

    @Test
    fun testGetArticleById() {
        val article = ArticleEntity(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now())
        entityManager.persist(article)

        val result = articleRepository.get(1)

        assertEquals(article.asArticle(), result)
    }

    @Test
    fun testUpdateArticle() {
        val existingArticle = ArticleEntity(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now())
        entityManager.persist(existingArticle)

        val updatedArticle = Article(1, "Updated Article", (15.5).toFloat(), 1, LocalDate.now())

        val result = articleRepository.update(updatedArticle)

        assertTrue(result.isSuccess)
        assertEquals(updatedArticle, result.getOrNull())
    }

    @Test
    fun testUpdateNonExistingArticle() {
        val article = Article(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now())

        val result = articleRepository.update(article)

        assertTrue(result.isFailure)
        assertEquals("User not in DB", result.exceptionOrNull()?.message)
    }

    @Test
    fun testDeleteArticle() {
        val article = ArticleEntity(1, "Test Article", (15.5).toFloat(), 1, LocalDate.now())
        entityManager.persist(article)

        val result = articleRepository.delete(1)

        assertEquals(article.asArticle(), result)
        assertNull(articleRepository.get(1))
    }
}
