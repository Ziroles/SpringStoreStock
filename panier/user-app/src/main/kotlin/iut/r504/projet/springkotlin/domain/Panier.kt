package iut.r504.projet.springkotlin.domain

import iut.r504.projet.springkotlin.domain.ArticleQuantite

data class Panier(val id: Long, val userEmail: String, val items: List<ArticleQuantite>)


