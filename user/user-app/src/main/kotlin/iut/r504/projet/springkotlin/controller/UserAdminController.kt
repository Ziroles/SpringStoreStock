package iut.r504.projet.springkotlin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import iut.r504.projet.springkotlin.controller.dto.UserDTO
import iut.r504.projet.springkotlin.controller.dto.asUserDTO
import iut.r504.projet.springkotlin.errors.UserNotFoundError
import iut.r504.projet.springkotlin.repository.UserRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.OutputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL

@RestController
@RequestMapping("/admin")
class UserAdminController (val userRepository: UserRepository)
{
    @Operation(summary = "Create user with cart")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "User created",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = UserDTO::class)
            )]),
        ApiResponse(responseCode = "409", description = "User already exists",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = String::class))])])
    @PostMapping("/users")
    fun create(@RequestBody @Valid user: UserDTO): ResponseEntity<Any> {
        var urlpanier = URL("http://localhost:8082/panierapi/admin/paniers")
        try{

        return userRepository.create(user.asUser()).fold(
            { success ->


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
        }catch(e: ConnectException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connection refused: ${e.message}")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request")
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
            throw UserNotFoundError(email)
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
            throw UserNotFoundError(email)
        } else {

            try {
                val deletePanierUrl = "http://localhost:8082/panierapi/admin/paniers/$email"
                val connection = URL(deletePanierUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {

                    ResponseEntity.noContent()
                } else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw UserNotFoundError(email)
                }else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                    return ResponseEntity.badRequest().body("Error: $responseCode")
                }

                connection.disconnect()
                ResponseEntity.ok("User deleted")
            } catch (e: ConnectException) {

                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Panier connection refused: ${e.message}")
            } catch (e: Exception) {

                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request")
            }
        }
    }


}