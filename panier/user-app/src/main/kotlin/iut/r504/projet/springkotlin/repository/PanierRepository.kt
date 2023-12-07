package iut.r504.projet.springkotlin.repository


import iut.r504.projet.springkotlin.domain.Panier

interface PanierRepository {
    fun create(panier: Panier): Result<Panier>
    fun list(): List<Panier>
    fun get(id: Long): Panier?
    fun update(panier: Panier): Result<Panier>
    fun delete(id: Long): Panier?
}