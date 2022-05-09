/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils.animes

/* This class is used to represent the different genres that an anime can have */
enum class Genre(val fr: String, val identifiers: Array<String> = arrayOf()) {
    UNKNOWN("Inconnu"),
    ACTION("Action", identifiers = arrayOf("Action")),
    ADVENTURE("Aventure", identifiers = arrayOf("Aventure", "adventure")),
    COMEDY("Comédie", identifiers = arrayOf("Comédie", "comedy")),
    DEMONS("Démons", identifiers = arrayOf("Démons", "Demons")),
    DETECTIVE("Détective", identifiers = arrayOf("Détective", "Detective")),
    DRAMA("Drame", identifiers = arrayOf("Drame", "Drama")),
    ECCHI("Ecchi", identifiers = arrayOf("Ecchi", "Fan service")),
    FANTASY("Fantaisie", identifiers = arrayOf("Fantaisie", "Fantastique", "fantasy")),
    HEROIC_FANTASY("Héroique fantaisie", identifiers = arrayOf("Heroic fantasy")),
    GAME("Jeu", identifiers = arrayOf("Jeu", "Game")),
    GASTRONOMY("Gastronomie", identifiers = arrayOf("Gastronomie", "cooking")),
    HAREM("Harem", identifiers = arrayOf("Harem")),
    HISTORICAL("Historique", identifiers = arrayOf("Historique")),
    HORROR("Horreur", identifiers = arrayOf("Horreur", "horror")),
    HUMOR("Humour", identifiers = arrayOf("Humour", "Humor")),
    ISEKAI("Isekai", identifiers = arrayOf("Isekai")),
    IDOLS("Idoles", identifiers = arrayOf("Idoles", "idols")),
    JOSEI("Josei", identifiers = arrayOf("Josei", "Jôsei")),
    MAGICAL_GIRL("Magical girl", identifiers = arrayOf("Magical Girl", "Magical Girls")),
    MAGIC("Magie", identifiers = arrayOf("Magie")),
    MARTIAL_ARTS("Arts martiaux", identifiers = arrayOf("Arts martiaux")),
    MECHA("Mecha", identifiers = arrayOf("Mecha")),
    MYSTERY("Mystère", identifiers = arrayOf("Mystère", "mystery")),
    MUSIC("Musique", identifiers = arrayOf("Musique", "Musical", "music")),
    NOSTALGIA("Nostalgie", identifiers = arrayOf("Nostalgie")),
    POLICE("Police", identifiers = arrayOf("Police", "Policier")),
    POST_APOCALYPTIC("Post Apocalyptique", identifiers = arrayOf("Post Apo", "Post Apocalyptique")),
    PIRATES("Pirates", identifiers = arrayOf("Pirates", "Pirate")),
    PSYCHOLOGY("Psychologie", identifiers = arrayOf("Psychologie")),
    ROMANCE("Romance", identifiers = arrayOf("Romance", "Romantique")),
    SCHOOL(
        "École",
        identifiers = arrayOf("École", "School", "Scolaire", "Vie Scolaire", "Animation japonaise sur l'école")
    ),
    SCI_FI("Science-fiction", identifiers = arrayOf("Science-fiction", "Science-fi", "science fiction", "sci-fi")),
    SEINEN("Seinen", identifiers = arrayOf("Seinen")),
    SHOJO("Shôjo", identifiers = arrayOf("Shôjo", "shojo")),
    SHONEN("Shônen", identifiers = arrayOf("Shônen", "Shonen", "shounen")),
    SPACE("Espace", identifiers = arrayOf("Espace", "Space")),
    SPORT("Sport", identifiers = arrayOf("Sport", "Sports")),
    STEAMPUNK("Steampunk", identifiers = arrayOf("Steampunk")),
    SUPERNATURAL("Surnaturel", identifiers = arrayOf("Surnaturel", "Supernaturel", "supernatural")),
    THRILLER("Thriller", identifiers = arrayOf("Thriller")),
    TRAGEDY("Tragédie", identifiers = arrayOf("Tragédie", "tragedy")),
    SLICE_OF_LIFE("Tranche de vie", identifiers = arrayOf("Tranche de vie", "Tranches de vie", "slice of life")),
    VIOLENCE("Violence", identifiers = arrayOf("Violence")),
    WAR("Guerre", identifiers = arrayOf("War")),
    WEBCOMIC("Webcomic", identifiers = arrayOf("Webcomic", "Webtoon")),
    YAOI("Yaoi", identifiers = arrayOf("Yaoi")),
    YURI("Yuri", identifiers = arrayOf("Yuri")),
    ;

    companion object {
        /**
         * Return the Genre enum value whose identifiers contain the given string
         *
         * @param string The string to match against.
         */
        fun getGenre(string: String): Genre =
            values().firstOrNull { v -> v.identifiers.map { it.lowercase() }.contains(string.lowercase()) } ?: UNKNOWN

        /**
         * If the array is not null, map each element to a Genre object, filter out nulls, and return an array of distinct
         * Genre objects
         *
         * @param array The array of strings that you want to convert to Genre objects.
         */
        fun getGenres(array: Iterable<String>?): Array<Genre> =
            array?.mapNotNull { s ->
                values().firstOrNull {
                    it.identifiers.map(String::lowercase).contains(s.lowercase())
                }
            }
                ?.distinct()?.toTypedArray() ?: emptyArray()
    }
}