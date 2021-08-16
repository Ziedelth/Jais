/*
 * Copyright (c) 2021. Ziedelth
 */

package fr.ziedelth.jais.utils

import fr.ziedelth.jais.utils.animes.Country

data class Channel(
    var anime: Boolean = true,
    var news: Boolean = true,
    var countries: MutableList<Country> = mutableListOf()
) {
    fun addCountry(country: Country) {
        if (!this.countries.contains(country)) this.countries.add(country)
    }

    fun addAllCountries(countries: Collection<Country>) {
        countries.forEach { this.addCountry(it) }
    }

    fun removeCountry(country: Country) {
        if (this.countries.contains(country)) this.countries.remove(country)
    }
}
