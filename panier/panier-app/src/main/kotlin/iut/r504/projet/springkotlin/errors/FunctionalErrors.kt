package iut.r504.projet.springkotlin.errors

sealed class FunctionalErrors(message: String = "", cause: Exception? = null) :
    Exception(message, cause)

class PanierNotFoundError(panierid:String) : FunctionalErrors(message = "Panier $panierid not found")

class Ensufficientquantity(panierid:String) : FunctionalErrors(message = "For panier $panierid each article are not available in sufficient quantity")