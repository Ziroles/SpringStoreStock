package iut.r504.projet.springkotlin.repository

import iut.r504.projet.springkotlin.domain.Article
import iut.r504.projet.springkotlin.repository.entity.ArticleEntity
import iut.r504.projet.springkotlin.repository.entity.asEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class ArticleDatabaseRepository(private val jpa: ArticleJpaRepository) : ArticleRepository {
    override fun create(article: Article): Result<Article> = if (jpa.findById(article.id).isPresent) {
        Result.failure(Exception("Article already in DB"))
    } else {
        val saved = jpa.save(article.asEntity())
        Result.success(saved.asArticle())
    }

    override fun list(name: String?): List<Article> {
        return if (name == null) {
            jpa.findAll().map { it.asArticle() }
        } else {
            jpa.findAllByName(name).map { it.asArticle() }
        }
    }

    override fun get(id: Int): Article? {
        return jpa.findById(id)
                .map { it.asArticle() }
                .getOrNull()
    }

    override fun update(article: Article): Result<Article> = if (jpa.findById(article.id).isPresent) {
        val saved = jpa.save(article.asEntity())
        Result.success(saved.asArticle())
    } else {
        Result.failure(Exception("User not in DB"))
    }

    override fun delete(id: Int): Article? {
        return jpa.findById(id)
                .also { jpa.deleteById(id) }
                .map { it.asArticle() }
                .getOrNull()
    }

}

interface ArticleJpaRepository : JpaRepository<ArticleEntity, Int> {
    fun findAllByName(name: String): List<ArticleEntity>
}


