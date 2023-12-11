package iut.r504.projet.springkotlin.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class User(val email: String,
                val firstName: String,
                val lastName: String,
                val age: Int,
                val adresseDeLivraison : String,
                val newsletterfollower : Boolean,
                val lastpurchase : LocalDate
)