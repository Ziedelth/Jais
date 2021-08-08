package fr.ziedelth.jais.utils.clients

data class JClient(val episodes: MutableList<JEpisode> = mutableListOf()) {
    fun hasEpisode(id: String): Boolean = this.episodes.any { it.id == id }
    private fun hasEpisode(jEpisode: JEpisode): Boolean = this.hasEpisode(jEpisode.id)
    fun getEpisodeById(id: String): JEpisode = this.episodes.find { it.id == id } ?: JEpisode(id)

    fun addEpisode(jEpisode: JEpisode) {
        if (!this.hasEpisode(jEpisode)) this.episodes.add(jEpisode)
    }
}
