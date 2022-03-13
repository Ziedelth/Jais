/*
 * Copyright (c) 2022. Ziedelth
 */

package fr.ziedelth.jais

import fr.ziedelth.jais.utils.JBrowser
import fr.ziedelth.jais.utils.plugins.PluginUtils.onDigits
import fr.ziedelth.jais.utils.plugins.PluginUtils.onlyLettersAndDigits
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>) {
    val calendar = GregorianCalendar.getInstance()
    val f = SimpleDateFormat("dd/MM/yyyy").format(calendar.time)

    val year = calendar.get(Calendar.YEAR)
    val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)

    val base = "https://www.nautiljon.com"
    val elements = JBrowser.get("$base/planning/manga/?y=$year&m=$month")?.getElementsByTag("tr")

    elements?.removeFirst()
    elements?.removeFirst()
    elements?.removeLast()

    /*
## Sorties mangas du 28/02/2022
##### _Rapport généré automatiquement par Jaïs_

---

Image|Manga|Éditeur|Volume|Prix|Lien
:---:|:---:|:---:|:---:|:---:|:---:
![Image manga](https://www.nautiljon.com/imagesmin/manga_volumes/00/08/allargando_134080.jpg?1646069803)|Allargando|Hot Manga|Vol. 1 Nouveauté|9.95 €|[Acheter](https://www.nautiljon.com/site/acheter.php?ean=9782368773970&books)
![Image manga](https://www.nautiljon.com/static/images/public_averti1_100.jpg)|Blue Desire|Hot Manga|Vol. 1 Nouveauté|9.95 €|[Acheter](https://www.nautiljon.com/site/acheter.php?ean=9782368773710&books)
![Image manga](https://www.nautiljon.com/static/images/public_averti1_100.jpg)|D-Medal|Hot Manga|Vol. 1 Nouveauté|9.95 €|[Acheter](https://www.nautiljon.com/site/acheter.php?ean=9782368773901&books)
![Image manga](https://www.nautiljon.com/imagesmin/manga_volumes/00/06/escale_a_yokohama_738160.jpg?1637781355)|Escale à Yokohama|Meian|Vol. 7|6.95 €|[Acheter](https://www.nautiljon.com/site/acheter.php?ean=9782382750476&books)
![Image manga](https://www.nautiljon.com/imagesmin/manga_volumes/00/06/escale_a_yokohama_738160.jpg?1637781355)|Escale à Yokohama|Meian|Vol. 8|6.95 €|[Acheter](https://www.nautiljon.com/site/acheter.php?ean=9782382750483&books)
![Image manga](https://www.nautiljon.com/imagesmin/manga_volumes/11/41/from_the_children_s_country_1958714.jpg?1642675001)|From the Children's Country |Meian|Vol. 1 Nouveauté|6.95 €|[Acheter](https://www.nautiljon.com/site/acheter.php?ean=9782382753965&books)
     */

    println("""## Sorties mangas du $f
##### _Rapport généré automatiquement par Jaïs_

---

Image|Manga|Éditeur|Volume|Prix|Lien
:---:|:---:|:---:|:---:|:---:|:---:""")

     elements?.filter { it.getElementsByTag("td").firstOrNull()?.text()?.equals(f, true) == true }?.forEach {
//    elements?.forEach {
         val tags = it.getElementsByTag("td")
         val image = "$base${tags[1]?.getElementsByTag("img")?.firstOrNull()?.attr("src")}"
         val split = tags[2]?.text()?.split("Vol. ")
         val title = split?.get(0)
         val vol = try { "Vol. ${split?.get(1)?.split(" ")?.get(0)}" } catch (exception: Exception) { "Vol. 1" }
         val price = tags[3]?.text()
         val editor = tags[4]?.text()
         val link = "$base${tags[5]?.getElementsByTag("a")?.firstOrNull()?.attr("href")}"
         println("![Image manga]($image)|$title|$editor|$vol|$price|[Acheter]($link)")
    }
}