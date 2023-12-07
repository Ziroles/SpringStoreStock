package iut.r504.projet.springkotlin.repository

import iut.r504.projet.springkotlin.domain.Panier

class PanierInMemoryRepository : PanierRepository {

    private val map = mutableMapOf<Long, Panier>()

    override fun create(panier: Panier): Result<Panier> {
        val previous = map.putIfAbsent(panier.id, panier)
        return if (previous == null) {
            Result.success(panier)
        } else {
            Result.failure(Exception("Panier already exists"))
        }
    }

    override fun list(): List<Panier> = map.values.toList()

    override fun get(id: Long): Panier? = map[id]

    override fun update(panier: Panier): Result<Panier> {
        val updated = map.replace(panier.id, panier)
        return if (updated == null) {
            Result.failure(Exception("Panier doesn't exist"))
        } else {
            Result.success(panier)
        }
    }

    override fun delete(id: Long): Panier? = map.remove(id)
}
