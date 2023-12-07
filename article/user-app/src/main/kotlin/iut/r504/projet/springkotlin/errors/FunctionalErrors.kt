package iut.r504.projet.springkotlin.errors

sealed class FunctionalErrors(message: String = "", cause: Exception? = null) :
    Exception(message, cause)

class ArticleNotFoundError(id: Int) : FunctionalErrors(message = "Article ID n°$id not found")

