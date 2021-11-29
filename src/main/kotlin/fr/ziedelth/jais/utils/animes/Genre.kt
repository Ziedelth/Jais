/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

enum class Genre(val string: Array<String> = arrayOf()) {
    UNKNOWN,
    ACTION(arrayOf("Action")),
    ADVENTURE(arrayOf("Aventure", "adventure")),
    COMEDY(arrayOf("Comédie", "comedy")),
    DEMONS(arrayOf("Démons", "Demons")),
    DETECTIVE(arrayOf("Détective", "Detective")),
    DRAMA(arrayOf("Drame", "Drama")),
    ECCHI(arrayOf("Ecchi", "Fan service")),
    FANTASY(arrayOf("Fantaisie", "Fantastique", "fantasy")),
    HEROIC_FANTASY(arrayOf("Heroic fantasy")),
    GAME(arrayOf("Jeu", "Game")),
    GASTRONOMY(arrayOf("Gastronomie", "cooking")),
    HAREM(arrayOf("Harem")),
    HISTORICAL(arrayOf("Historique")),
    HORROR(arrayOf("Horreur", "horror")),
    HUMOR(arrayOf("Humour", "Humor")),
    ISEKAI(arrayOf("Isekai")),
    IDOLS(arrayOf("Idoles", "idols")),
    JOSEI(arrayOf("Josei", "Jôsei")),
    MAGICAL_GIRL(arrayOf("Magical Girl", "Magical Girls")),
    MAGIC(arrayOf("Magie")),
    MARTIAL_ARTS(arrayOf("Arts martiaux")),
    MECHA(arrayOf("Mecha")),
    MYSTERY(arrayOf("Mystère", "mystery")),
    MUSIC(arrayOf("Musique", "Musical", "music")),
    NOSTALGIA(arrayOf("Nostalgie")),
    POLICE(arrayOf("Police", "Policier")),
    POST_APOCALYPTIC(arrayOf("Post Apo", "Post Apocalyptique")),
    PIRATES(arrayOf("Pirates", "Pirate")),
    PSYCHOLOGY(arrayOf("Psychologie")),
    ROMANCE(arrayOf("Romance", "Romantique")),
    SCHOOL(arrayOf("École", "School", "Scolaire", "Vie Scolaire")),
    SCI_FI(arrayOf("Science-fiction", "Science-fi", "science fiction", "sci-fi")),
    SEINEN(arrayOf("Seinen")),
    SHOJO(arrayOf("Shôjo", "shojo")),
    SHONEN(arrayOf("Shônen", "Shonen", "shounen")),
    SPACE(arrayOf("Espace", "Space")),
    SPORT(arrayOf("Sport", "Sports")),
    STEAMPUNK(arrayOf("Steampunk")),
    SUPERNATURAL(arrayOf("Surnaturel", "Supernaturel", "supernatural")),
    THRILLER(arrayOf("Thriller")),
    TRAGEDY(arrayOf("Tragédie", "tragedy")),
    SLICE_OF_LIFE(arrayOf("Tranche de vie", "Tranches de vie", "slice of life")),
    VIOLENCE(arrayOf("Violence")),
    WAR(arrayOf("War")),
    WEBCOMIC(arrayOf("Webcomic")),
    YAOI(arrayOf("Yaoi")),
    YURI(arrayOf("Yuri")),
    ;

    companion object {
        fun getGenre(string: String): Genre =
            values().firstOrNull { v -> v.string.map { it.lowercase() }.contains(string.lowercase()) } ?: UNKNOWN

        fun getGenres(array: Iterable<String>?): Array<Genre> =
            array?.mapNotNull { s -> values().firstOrNull { it.string.map(String::lowercase).contains(s.lowercase()) } }
                ?.distinct()?.toTypedArray() ?: emptyArray()
    }
}