package fr.ziedelth.jais.exceptions

import java.lang.reflect.MalformedParametersException

class MalformedEpisodeException(override val message: String) : MalformedParametersException(message)