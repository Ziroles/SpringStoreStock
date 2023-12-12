package iut.r504.projet.springkotlin.repository.entity

import iut.r504.projet.springkotlin.domain.User
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import java.time.LocalDate

@Entity
@Table(name = "users")
class UserEntity(
    @Id @Email val email: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val adresseDeLivraison :String,
    val newsletterfollower : Boolean,
    val lastpurchase : LocalDate
) {
    fun asUser() = User(this.email, this.firstName, this.lastName, this.age, this.adresseDeLivraison, this.newsletterfollower,this.lastpurchase)
}
fun User.asEntity() = UserEntity(this.email, this.firstName, this.lastName, this.age , this.adresseDeLivraison,this.newsletterfollower,this.lastpurchase)
