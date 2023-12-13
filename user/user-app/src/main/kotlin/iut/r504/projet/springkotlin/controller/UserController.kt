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
import iut.r504.projet.springkotlin.errors.ArticleNotFoundError
import iut.r504.projet.springkotlin.errors.Ensufficientquantity
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController
import java.io.OutputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate


@RestController
@Validated
class UserController(val userRepository: UserRepository) {
    var urlpanier = URL("http://localhost:8082/panierapi/paniers")
    private val logger: Logger = LoggerFactory.getLogger(UserController::class.java)


    @Operation(summary = "List users")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List users",
            content = [Content(mediaType = "application/json",
                array = ArraySchema(
                    schema = Schema(implementation = UserDTO::class))
            )])])
    @GetMapping("/users")
    fun list(@RequestParam(required = false) @Min(15) age: Int?) {
        logger.info("Request to get all user")
        return userRepository.list(age)
            .map { it.asUserDTO() }
            .let {
                ResponseEntity.ok(it)
            }}

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
        logger.info("Request to find one user")
        val user = userRepository.get(email)
        return if (user != null) {
            logger.info("User found")
            ResponseEntity.ok(user.asUserDTO())
        } else {
            throw UserNotFoundError(email)
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
        logger.info("Request to validate the panier of $email user")
        val user = userRepository.get(email)

        try{
        return if (user != null) {
            val url = "$urlpanier/validate/${user.email}"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {

                    throw Ensufficientquantity(email)
                }else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                    throw UserNotFoundError(email)
                }else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                    return ResponseEntity.badRequest().body("Error: $responseCode")
                }
            } finally {

                connection.disconnect()
            }

            val updatedUser = user.copy(lastpurchase = LocalDate.now())
            userRepository.update(updatedUser)
                .fold(
                    { success ->
                        logger.info("Panier validated")
                        ResponseEntity.ok(success.asUserDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            throw UserNotFoundError(email)
        }}catch (e: ConnectException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connection refused: ${e.message}")
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
        logger.info("Request to add article to $email panier")
        val user = userRepository.get(email)
        try{
        return if (user != null) {


            val url = "$urlpanier/${user.email}/add-article?articleId=${articleid.toLong()}&quantite=$quantity"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            try {
                val responseCode = connection.responseCode
                println(responseCode)
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND ) {

                    throw ArticleNotFoundError(email)
                }else{
                    if(responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE){
                        throw Ensufficientquantity(email)
                    }else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                        return ResponseEntity.badRequest().body("Error: $responseCode")
                    }
                }
            } finally {
                connection.disconnect()
            }
            logger.info("Article added to $email panier")
            ResponseEntity.ok("Article add to user panier")
        } else {
            throw UserNotFoundError(email)
        }} catch (e: ConnectException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connection Panier refused: ${e.message}")
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
        logger.info("Request to remove one article from $email panier")
        try{

            return if (user != null) {
                val url = "$urlpanier/${user.email}/remove-article?articleId=$articleid&quantite=$quantity"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"

                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {

                        throw Ensufficientquantity("Error: $responseCode")
                    }else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                        throw ArticleNotFoundError(email)

                    } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                        return ResponseEntity.badRequest().body("Error: $responseCode")
                    }
                } finally {

                    connection.disconnect()
                }


                logger.info("Article deleted from user panier")
                ResponseEntity.ok("Article deleted from user panier")
            } else {
                throw UserNotFoundError(email)
            }
        } catch (e: ConnectException) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Connection refused: ${e.message}")
        }



    }
    @Operation(summary = "User change state of newletter")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User change state of newletter",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PatchMapping("/paniers/{email}/change-newletter")
    fun changeNewletter(@PathVariable email: String): ResponseEntity<Any> {
        logger.info("Request to change newletter status of $email user")
        val user = userRepository.get(email)
        return if (userRepository.get(email) != null) {
            val updatedPanier = user!!.copy( newsletterfollower =  !user.newsletterfollower)
            userRepository.update(updatedPanier)
                .fold(
                    { success ->
                        logger.info("Newletter status of $email user changed")
                        ResponseEntity.ok(success.asUserDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            throw UserNotFoundError(email)
        }
    }

    @Operation(summary = "User update adresse")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "User update adresse",
            content = [Content(mediaType = "application/json",
                schema = Schema(implementation = UserDTO::class))]),
        ApiResponse(responseCode = "404", description = "Panier not found")
    ])
    @PatchMapping("/paniers/{email}/update-adresse")
    fun updateAdresse(@PathVariable email: String, @RequestParam Newadresse: String): ResponseEntity<Any> {
        logger.info("Request to change deliveryadress of $email user")
        val user = userRepository.get(email)
        return if (userRepository.get(email) != null) {
            val updatedPanier = user!!.copy( adresseDeLivraison = Newadresse)
            userRepository.update(updatedPanier)
                .fold(
                    { success ->
                        logger.info("Adress change for $email user")
                        ResponseEntity.ok(success.asUserDTO()) },
                    { failure -> ResponseEntity.badRequest().body(failure.message) }
                )
        } else {
            throw UserNotFoundError(email)
        }
    }


}
