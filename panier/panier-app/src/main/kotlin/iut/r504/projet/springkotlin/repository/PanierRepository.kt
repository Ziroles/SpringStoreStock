package iut.r504.projet.springkotlin.repository


import iut.r504.projet.springkotlin.domain.Panier
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PanierRepository {
    fun create(panier: Panier): Result<Panier>
    fun list(): List<Panier>
    fun get(id: String): Panier?
    fun update(panier: Panier): Result<Panier>
    fun delete(id: String): Panier?


}