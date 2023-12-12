package iut.r504.projet.springkotlin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import iut.r504.projet.springkotlin.controller.dto.PanierDTO
import iut.r504.projet.springkotlin.controller.dto.toDomain
import iut.r504.projet.springkotlin.controller.dto.toPanierDTO
import iut.r504.projet.springkotlin.errors.PanierNotFoundError
import iut.r504.projet.springkotlin.repository.PanierRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URL

@RestController
@RequestMapping("/admin")
@Tag(name = "Administration", description = "Operations related to panier administration")
class PanierAdminController(private val panierRepository: PanierRepository) {
    val urlarticle = URL("http://localhost:8083/articleapi/article/")
    @Operation(summary = "Create panier")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Panier created",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = PanierDTO::class)
            )]),
        ApiResponse(responseCode = "409", description = "Panier already exists",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PostMapping("/paniers")
    fun create(@RequestBody @Valid panierDTO: PanierDTO): ResponseEntity<PanierDTO> =
        panierRepository.create(panierDTO.toDomain()).fold(
            { success -> ResponseEntity.status(HttpStatus.CREATED).body(panierDTO) },
            { failure -> ResponseEntity.status(HttpStatus.CONFLICT).build() })

    @Operation(summary = "Update a panier by id")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Panier updated",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = PanierDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid request",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PutMapping("/paniers/{email}")
    fun updatePanier(@PathVariable email: String, @RequestBody @Valid panierDTO: PanierDTO): ResponseEntity<Any> {
        val existingPanier = panierRepository.get(email.replace("%40", "@"))

        return if (existingPanier == null) {
            throw PanierNotFoundError(email)
        } else {
            // Assurez-vous que l'email dans le DTO correspond à l'utilisateur du panier
            if (panierDTO.userEmail != existingPanier.userEmail) {
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
    @DeleteMapping("/paniers/{email}")
    fun delete(@PathVariable email: String): ResponseEntity<Any> {
        if (panierRepository.get(email.replace("%40", "@")) == null) {
            throw PanierNotFoundError(email)
        }else{
            panierRepository.update(panierRepository.get(email.replace("%40", "@"))!!.copy(items = mutableListOf()))
            panierRepository.delete(email.replace("%40", "@"))

            return ResponseEntity.ok("Panier deleted")

        }

    }

}