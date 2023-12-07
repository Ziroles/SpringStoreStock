package bzh.zomzog.prez.springkotlin.repository

import bzh.zomzog.prez.springkotlin.domain.Article

interface ArticleRepository {
    fun create(article: Article): Result<Article>
    fun list(age: Int? = null): List<Article> //TODO
    fun get(id: Int): Article?
    fun update(article: Article): Result<Article>
    fun delete(id: Int): Article?
}