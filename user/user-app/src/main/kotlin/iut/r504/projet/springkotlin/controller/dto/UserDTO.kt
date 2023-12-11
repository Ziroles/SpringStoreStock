package iut.r504.projet.springkotlin.controller.dto

import iut.r504.projet.springkotlin.domain.User
import jakarta.persistence.Id
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UserDTO(
    @Id  val email: String,
    @field:Size(min = 1, max = 30) val firstName: String,
    @field:Size(min = 1, max = 30) val lastName: String,
    @field:Min(15) @field:Max(120) val age: Int,
    val adresseDeLivraison : String,
    val newsletterfollower: Boolean,
    val lastpurchase: LocalDate
) {

    fun asUser() = User(email, firstName, lastName, age,adresseDeLivraison, newsletterfollower,lastpurchase )
}

fun User.asUserDTO() = UserDTO(this.email, this.firstName, this.lastName, this.age, this.adresseDeLivraison,this.newsletterfollower,this.lastpurchase)
