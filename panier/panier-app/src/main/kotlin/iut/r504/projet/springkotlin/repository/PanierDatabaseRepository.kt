package iut.r504.projet.springkotlin.repository

import iut.r504.projet.springkotlin.domain.Panier
import iut.r504.projet.springkotlin.repository.entity.PanierEntity
import iut.r504.projet.springkotlin.repository.entity.asEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class PanierDatabaseRepository(private val jpa: PanierJpaRepository) : PanierRepository {
    override fun create(panier: Panier): Result<Panier> = if (jpa.findById(panier.userEmail).isPresent ) {
        println("normalement erreur")
        Result.failure(Exception("Panier already in DB"))
    } else {
        val saved = jpa.save(panier.asEntity())
        Result.success(saved.asPanier())
    }

    override fun list(): List<Panier> {
        return jpa.findAll().map { it.asPanier() }
    }

    override fun get(id: String): Panier? {
        return jpa.findById(id)
                .map { it.asPanier() }
                .getOrNull()
    }

    override fun update(panier: Panier): Result<Panier> = if (jpa.findById(panier.userEmail).isPresent ) {
        println(panier.items.size)
        val saved = jpa.save(panier.asEntity())
        Result.success(saved.asPanier())
    } else {
        Result.failure(Exception("Panier not in DB"))
    }

    override fun delete(id: String): Panier? {
        return jpa.findById(id)
                .also { jpa.deleteById(id) }
                .map { it.asPanier() }
                .getOrNull()
    }

}

interface PanierJpaRepository : JpaRepository<PanierEntity, String>

