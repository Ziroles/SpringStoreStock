package iut.r504.projet.springkotlin.errors

sealed class FunctionalErrors(message: String = "", cause: Exception? = null) :
    Exception(message, cause)

class PanierNotFoundError(panierid:String) : FunctionalErrors(message = "Panier $panierid not found")

class Ensufficientquantity(panierid:String) : FunctionalErrors(message = "Artlicle number $panierid is not available in sufficient quantity")

class ArticleNotFoundError(id: String) : FunctionalErrors(message = "Article ID n°$id not found")