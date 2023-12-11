package iut.r504.projet.springkotlin.controller

import iut.r504.projet.springkotlin.errors.UserNotFoundError
import iut.r504.projet.springkotlin.controller.dto.UserDTO
import iut.r504.projet.springkotlin.controller.dto.asUserDTO
import iut.r504.projet.springkotlin.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import iut.r504.projet.springkotlin.errors.Ensufficientquantity
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate


@RestController
@Validated
class UserController(val userRepository: UserRepository) {
    val urlpanier = URL("http://localhost:8082/panierapi/paniers")
    @Operation(summary = "Create user with cart")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "User created",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = UserDTO::class)
            )]),
        ApiResponse(responseCode = "409", description = "User already exists",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PostMapping("/users")
    fun create(@RequestBody @Valid user: UserDTO): ResponseEntity<UserDTO> {
        return userRepository.create(user.asUser()).fold(
            { success ->
                // Créer un panier pour le nouvel utilisateur

                val connection = urlpanier.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonBody = """
                {
                  "userEmail": "${user.email}",
                  "items": []
                }
                """.trimIndent()

                val outputStream: OutputStream = connection.outputStream
                outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
                outputStream.flush()

                val responseCode = connection.responseCode
                val responseBody = connection.inputStream.bufferedReader().readText()



                connection.disconnect()

                ResponseEntity.status(HttpStatus.CREATED).body(success.asUserDTO())
            },
            { failure -> ResponseEntity.status(HttpStatus.CONFLICT).build() })
    }


    @Operation(summary = "List users")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List users",
            content = [Content(mediaType = "application/json",
                array = ArraySchema(
                    schema = Schema(implementation = UserDTO::class))
            )])])
    @GetMapping("/users")
    fun list(@RequestParam(required = false) @Min(15) age: Int?) =
        userRepository.list(age)
            .map { it.asUserDTO() }
            .let {
                ResponseEntity.ok(it)
            }

    @Operation(summary = "Get user by email")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "The user",
            content = [
                Content(mediaType = "application/json",
                    schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "404", description = "User not found")
    ])
    @GetMapping("/users/{email}")
    fun findOne(@PathVariable @Email email: String): ResponseEntity<UserDTO> {
        val user = userRepository.get(email)
        return if (user != null) {
            ResponseEntity.ok(user.asUserDTO())
        } else {
            throw UserNotFoundError(email)
        }
    }

    @Operation(summary = "Update a user by email")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User updated",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "400", description = "Invalid request",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PutMapping("/users/{email}")
    fun update(@PathVariable @Email email: String, @RequestBody @Valid user: UserDTO): ResponseEntity<Any> =
        if (email != user.email) {
            ResponseEntity.badRequest().body("Invalid email")
        } else {
            userRepository.update(user.asUser()).fold(
                { success -> ResponseEntity.ok(success.asUserDTO()) },
                { failure -> ResponseEntity.badRequest().body(failure.message) }
            )
        }

    @Operation(summary = "Delete user by email")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "User deleted"),
        ApiResponse(responseCode = "400", description = "User not found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @DeleteMapping("/users/{email}")
    fun delete(@PathVariable @Email email: String): ResponseEntity<Any> {
        val deleted = userRepository.delete(email)
        return if (deleted == null) {
            ResponseEntity.badRequest().body("User not found")
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @Operation(summary = "Validate user panier")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User validated",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "400", description = "Bad Request",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]),
        ApiResponse(responseCode = "404", description = "User not found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @PutMapping("/users/validate/{email}")
    fun validate(@PathVariable @Email email: String): ResponseEntity<Any> {
        val user = userRepository.get(email)

        return if (user != null) {
            val url = "$urlpanier/validate/${user.email}"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    // Handle errors
                    throw Ensufficientquantity("Error: $responseCode")
                }
            } finally {
                // Close the connection
                connection.disconnect()
            }

            val updatedUser = user.copy(lastpurchase = LocalDate.now())
            userRepository.update(updatedUser)
                .fold(
                    { success -> ResponseEntity.ok(success.asUserDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")
        }
    }
    @Operation(summary = "Add article to user panier")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Article added",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "400", description = "Bad Request",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]),
        ApiResponse(responseCode = "404", description = "User not found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @PutMapping("/users/addarticle/{email}")
    fun addarticle(
        @PathVariable @Email email: String,
        @RequestParam @Min(1) articleid: Int,
        @RequestParam @Min(1) quantity: Int
    ): ResponseEntity<Any> {
        val user = userRepository.get(email)

        return if (user != null) {
            val url = "$urlpanier/${user.email}/add-article?articleId=${articleid.toLong()}&quantite=$quantity"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            try {
                val responseCode = connection.responseCode
                println(responseCode)
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    // Handle errors
                    throw Ensufficientquantity("Error: $responseCode")
                }
            } finally {
                // Close the connection
                connection.disconnect()
            }

            // Add any additional logic or response based on success

            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")
        }
    }

    @Operation(summary = "Remove article to user panier")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Article removed",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "400", description = "Bad Request",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))]),
        ApiResponse(responseCode = "404", description = "User not found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])
    ])
    @PutMapping("/users/removearticle/{email}")
    fun removearticle(
        @PathVariable @Email email: String,
        @RequestParam @Min(1) articleid: Int,
        @RequestParam @Min(1) quantity: Int
    ): ResponseEntity<Any> {
        val user = userRepository.get(email)

        return if (user != null) {
            val url = "$urlpanier/${user.email}/remove-article?articleid=$articleid&quantity=$quantity"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PATCH"

            try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    // Handle errors
                    throw Ensufficientquantity("Error: $responseCode")
                }
            } finally {
                // Close the connection
                connection.disconnect()
            }

            // Add any additional logic or response based on success

            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")
        }
    }
    @Operation(summary = "User change state of newletter")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User change state of newletter",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PatchMapping("/paniers/{id}/change-newletter")
    fun changeNewletter(@PathVariable id: String): ResponseEntity<Any> {
        val user = userRepository.get(id)
        return if (userRepository.get(id) != null) {
            val updatedPanier = user!!.copy( newsletterfollower =  !user.newsletterfollower)
            userRepository.update(updatedPanier)
                .fold(
                    { success -> ResponseEntity.ok(success.asUserDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            throw UserNotFoundError("Panier not found")
        }
    }

    @Operation(summary = "User update adresse")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User update adresse",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PatchMapping("/paniers/{id}/update-adresse")
    fun updateAdresse(@PathVariable id: String, @RequestParam Newadresse: String): ResponseEntity<Any> {
        val user = userRepository.get(id)
        return if (userRepository.get(id) != null) {
            val updatedPanier = user!!.copy( adresseDeLivraison = Newadresse)
            userRepository.update(updatedPanier)
                .fold(
                    { success -> ResponseEntity.ok(success.asUserDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            throw UserNotFoundError("Panier not found")
        }
    }


}
