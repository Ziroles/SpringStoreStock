package iut.r504.projet.springkotlin.controller


import iut.r504.projet.springkotlin.errors.PanierNotFoundError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses



import iut.r504.projet.springkotlin.controller.dto.PanierDTO

import iut.r504.projet.springkotlin.controller.dto.toDomain
import iut.r504.projet.springkotlin.controller.dto.toPanierDTO
import iut.r504.projet.springkotlin.repository.PanierRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
class PanierController(private val panierRepository: PanierRepository) {

    @Operation(summary = "Create panier")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Panier created",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class)
                )]),
        ApiResponse(responseCode = "409", description = "Panier already exists",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PostMapping("/api/paniers")
    fun create(@RequestBody @Valid panierDTO: PanierDTO): ResponseEntity<PanierDTO> =
            panierRepository.create(panierDTO.toDomain()).fold(
                    { success -> ResponseEntity.status(HttpStatus.CREATED).body(panierDTO) },
                    { failure -> ResponseEntity.status(HttpStatus.CONFLICT).build() })

    @Operation(summary = "List paniers")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List paniers",
                content = [Content(mediaType = "application/json",
                        array = ArraySchema(
                                schema = Schema(implementation = PanierDTO::class))
                )])])
    @GetMapping("/api/paniers")
    fun list(): ResponseEntity<List<PanierDTO>> =
            panierRepository.list()
                    .map { it.toPanierDTO() }
                    .let {
                        ResponseEntity.ok(it)
                    }

    @Operation(summary = "Get panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "The panier",
                content = [
                    Content(mediaType = "application/json",
                            schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @GetMapping("/api/paniers/{id}")
    fun findOne(@PathVariable id: String): ResponseEntity<PanierDTO> {
        val panier = panierRepository.get(id.toLong())
        return if (panier != null) {
            ResponseEntity.ok(panier.toPanierDTO())
        } else {
            throw PanierNotFoundError("Panier not found")
        }
    }

    @Operation(summary = "Update a panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Panier updated",
                content = [Content(mediaType = "application/json",
                        schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid request",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PutMapping("/api/paniers/{id}")
    fun updatePanier(@PathVariable id: Long, @RequestBody @Valid panierDTO: PanierDTO): ResponseEntity<Any> {
        val existingPanier = panierRepository.get(id)

        return if (existingPanier == null) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Panier not found")
        } else {
            // Assurez-vous que l'email dans le DTO correspond à l'utilisateur du panier
            if (panierDTO.panierId != existingPanier.id) {
                ResponseEntity.badRequest().body("Invalid email")
            } else {
                val updatedPanier = panierRepository.update(panierDTO.toDomain()).fold(
                        { success -> success },
                        { failure -> return ResponseEntity.badRequest().body(failure.message) }
                )
                ResponseEntity.ok(updatedPanier.toPanierDTO())
            }
        }
    }

    @Operation(summary = "Delete panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Panier deleted"),
        ApiResponse(responseCode = "400", description = "Panier not found",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @DeleteMapping("/api/paniers/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Any> {
        val deleted = panierRepository.delete(id.toLong())
        return if (deleted == null) {
            ResponseEntity.badRequest().body("Panier not found")
        } else {
            ResponseEntity.noContent().build()
        }
    }
}
