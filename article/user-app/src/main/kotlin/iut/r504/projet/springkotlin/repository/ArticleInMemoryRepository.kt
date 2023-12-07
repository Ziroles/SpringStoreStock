package iut.r504.projet.springkotlin.repository

import iut.r504.projet.springkotlin.domain.Article

//@Repository
class ArticleInMemoryRepository : ArticleRepository {

    private val map = mutableMapOf<String, Article>()

    override fun create(article: Article): Result<Article> {
        val previous = map.putIfAbsent(article.id.toString(), article)
        return if (previous == null) {
            Result.success(article)
        } else {
            Result.failure(Exception("Article already exit"))
        }
    }

    override fun list(name: String?) = if (name == null) {
        map.values.toList()
    } else {
        map.values.filter { it.name == name }
    }

    override fun get(id: Int) = map[id.toString()]

    override fun update(article: Article): Result<Article> {
        val updated = map.replace(article.id.toString(), article)
        return if (updated == null) {
            Result.failure(Exception("Article doesn't exit"))
        } else {
            Result.success(article)
        }
    }

    override fun delete(id: Int) = map.remove(id.toString())
}