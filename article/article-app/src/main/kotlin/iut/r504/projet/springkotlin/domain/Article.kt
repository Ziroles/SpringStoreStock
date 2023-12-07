package bzh.zomzog.prez.springkotlin.domain

import java.time.LocalDate

data class Article(val id: Int, val name: String, val price: Float, val quantity: Int, val lastUpdate : LocalDate)