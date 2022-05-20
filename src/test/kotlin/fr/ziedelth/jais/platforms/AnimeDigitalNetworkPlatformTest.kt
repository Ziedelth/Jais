package fr.ziedelth.jais.platforms

import kotlin.test.Test
import kotlin.test.assertEquals

internal class AnimeDigitalNetworkPlatformTest {
    @Test
    fun toEpisode() {
        val platform = AnimeDigitalNetworkPlatform()
        val episode = platform.toEpisode(
            """
            {
              "id": 18965,
              "title": "I'm Quitting Heroing - Épisode 7",
              "name": "Un bon guerrier ne fait pas forcément un bon supérieur",
              "number": "Épisode 7",
              "shortNumber": "7",
              "season": "1",
              "reference": "heroing_tv_0007",
              "type": "EPS",
              "order": 7,
              "image": "https://image.animedigitalnetwork.fr/license/heroing/tv/web/eps7_320x180.jpg",
              "image2x": "https://image.animedigitalnetwork.fr/license/heroing/tv/web/eps7_640x360.jpg",
              "summary": "Tout juste sorti de ses conversations avec Mernes, Leo doit maintenant s’occuper du cas d’Edvard, qui semble prendre très à cœur la défaite de ses hommes contre une chimère de niveau supérieur. Pendant ce temps, les ouvriers de Lily exhument quelques vestiges de l’ancienne civilisation.",
              "releaseDate": "2022-05-17T14:30:00Z",
              "duration": 1420,
              "url": "https://animedigitalnetwork.fr/video/im-quitting-heroing-isekai/18965-episode-7-un-bon-guerrier-ne-fait-pas-forcement-un-bon-superieur",
              "urlPath": "/video/im-quitting-heroing-isekai/18965-episode-7-un-bon-guerrier-ne-fait-pas-forcement-un-bon-superieur",
              "embeddedUrl": "https://animedigitalnetwork.fr/embedded/im-quitting-heroing-isekai/18965",
              "languages": [
                "vostf"
              ],
              "qualities": [
                "fhd",
                "hd",
                "sd",
                "mobile"
              ],
              "rating": 4.9,
              "ratingsCount": 30,
              "commentsCount": 3,
              "available": true,
              "download": false,
              "free": false,
              "freeWithAds": false,
              "show": {
                "id": 860,
                "title": "I'm Quitting Heroing",
                "type": "EPS",
                "originalTitle": "Yuusha, Yamemasu",
                "shortTitle": "I'm Quitting Heroing",
                "reference": "heroing_tv",
                "age": "10+",
                "languages": [
                  "vostf"
                ],
                "summary": "Leo Demonheart est le héros qui a sauvé le royaume d'une invasion menée par la Reine-Démon Echidna et ses Quatre généraux. Mais le voilà de retour dans le château ennemi, en tant que simple candidat aux annonces de recrutement. Mais une reine vaincue peut-elle décemment embaucher l'homme qui a triomphé d'elle ?",
                "image": "https://image.animedigitalnetwork.fr/license/heroing/tv/web/affiche_175x250.jpg",
                "image2x": "https://image.animedigitalnetwork.fr/license/heroing/tv/web/affiche_350x500.jpg",
                "imageHorizontal": "https://image.animedigitalnetwork.fr/license/heroing/tv/web/license_320x180.jpg",
                "imageHorizontal2x": "https://image.animedigitalnetwork.fr/license/heroing/tv/web/license_640x360.jpg",
                "url": "https://animedigitalnetwork.fr/video/im-quitting-heroing-isekai",
                "urlPath": "/video/im-quitting-heroing-isekai",
                "episodeCount": 7,
                "genres": [
                  "Animation japonaise",
                  "Action",
                  "Aventure",
                  "Comédie",
                  "Ecchi",
                  "Heroic Fantasy",
                  "Seinen"
                ],
                "copyright": "©2022 Quantum, Hana Amano/KADOKAWA/I'm Quitting Heroing Committee",
                "rating": 4.8,
                "ratingsCount": 369,
                "commentsCount": 23,
                "qualities": [
                  "fhd",
                  "hd",
                  "sd"
                ],
                "simulcast": true,
                "free": true,
                "available": true,
                "download": false,
                "basedOn": null,
                "tagline": null,
                "firstReleaseYear": "2022",
                "productionStudio": "EMT Squared",
                "countryOfOrigin": "Japon",
                "productionTeam": [
                  {
                    "role": "Réalisateur",
                    "name": "Yuu Nobuta"
                  },
                  {
                    "role": "Réalisateur",
                    "name": "Hisashi Ishii"
                  },
                  {
                    "role": "Scénariste",
                    "name": "Shigeru Morakoshi"
                  },
                  {
                    "role": "Character designer original",
                    "name": "Hana Amano"
                  },
                  {
                    "role": "Compositeur",
                    "name": "Kôhei Munemoto"
                  }
                ],
                "nextVideoReleaseDate": "2022-05-24T14:30:00Z"
              }
            }
        """.trimIndent()
        )

        assertEquals("Yuusha, Yamemasu", episode.anime.name)
        assertEquals(
            "Leo Demonheart est le héros qui a sauvé le royaume d'une invasion menée par la Reine-Démon Echidna et ses Quatre généraux. Mais le voilà de retour dans le château ennemi, en tant que simple candidat aux annonces de recrutement. Mais une reine vaincue peut-elle décemment embaucher l'homme qui a triomphé d'elle ?",
            episode.anime.description
        )
        assertEquals(
            "https://image.animedigitalnetwork.fr/license/heroing/tv/web/affiche_350x500.jpg",
            episode.anime.image
        )

        assertEquals("2022-05-17T14:30:00Z", episode.releaseDate)
        assertEquals(1, episode.season)
        assertEquals(7, episode.number)
        assertEquals("ANIM-18965", episode.episodeId)
        assertEquals("Un bon guerrier ne fait pas forcément un bon supérieur", episode.title)
        assertEquals(
            "https://animedigitalnetwork.fr/video/im-quitting-heroing-isekai/18965-episode-7-un-bon-guerrier-ne-fait-pas-forcement-un-bon-superieur",
            episode.url
        )
        assertEquals("https://image.animedigitalnetwork.fr/license/heroing/tv/web/eps7_640x360.jpg", episode.image)
        assertEquals(1420, episode.duration)
    }
}