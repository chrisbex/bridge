package com.twojafirma.askier.ui.data

// Nowa, bardziej szczegółowa definicja rozdania
data class Deal(
    val id: Int,
    val level: Int,
    val suit: Suit,
    val declarer: Player,
    val double: Double,
    val tricks: Int,
    val vulnerability: Vulnerability, // <-- NOWE POLE
    val score: Int
)

// Typy wyliczeniowe
enum class Suit(val symbol: String) { CLUBS("♣"), DIAMONDS("♦"), HEARTS("♥"), SPADES("♠"), NO_TRUMP("BA") } // ZMIANA Z "NT" NA "BA"
enum class Player { N, S, E, W }
enum class Double(val symbol: String) { NONE(""), DBL("x"), RDBL("xx") }
enum class Vulnerability(val description: String) { // <-- NOWY TYP
    NONE("Nikt po partii"),
    NS("NS po partii"),
    EW("EW po partii"),
    BOTH("Wszyscy po partii")
}