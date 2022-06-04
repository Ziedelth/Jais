package fr.ziedelth.jais.utils

data class Anime(var name: String, var description: String?, val image: String) {
    init {
        this.name = this.name.trim()
        this.description = this.description?.trim()?.replace("\n", "")?.ifBlank { null }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Anime

        if (name != other.name) return false
        if (description != other.description) return false
        if (image != other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + image.hashCode()
        return result
    }
}
