package iut.r504.projet.springkotlin.domain

import iut.r504.projet.springkotlin.domain.ArticleQuantite

data class Panier(val userEmail: String, val items: MutableList<ArticleQuantite>)


