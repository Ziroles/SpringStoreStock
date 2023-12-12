package iut.r504.projet.springkotlin.errors

sealed class FunctionalErrors(message: String = "", cause: Exception? = null) :
    Exception(message, cause)

class UserNotFoundError(email: String) : FunctionalErrors(message = "User $email not found")

class Ensufficientquantity(panierid:String) : FunctionalErrors(message = "For panier $panierid each article are not available in sufficient quantity")

class ArticleNotFoundError(id: String) : FunctionalErrors(message = "One article from panier ${id} is not found")