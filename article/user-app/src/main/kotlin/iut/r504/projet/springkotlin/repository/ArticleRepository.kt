package iut.r504.projet.springkotlin.repository

import iut.r504.projet.springkotlin.domain.Article

interface ArticleRepository {
    fun create(article: Article): Result<Article>
    fun list(name: String? = null): List<Article>
    fun get(id: Int): Article?
    fun update(article: Article): Result<Article>
    fun delete(id: Int): Article?
}