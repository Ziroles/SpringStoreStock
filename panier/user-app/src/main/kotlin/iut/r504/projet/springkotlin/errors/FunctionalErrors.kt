package iut.r504.projet.springkotlin.errors

sealed class FunctionalErrors(message: String = "", cause: Exception? = null) :
    Exception(message, cause)

class PanierNotFoundError(panierid:String) : FunctionalErrors(message = "User $panierid not found")

