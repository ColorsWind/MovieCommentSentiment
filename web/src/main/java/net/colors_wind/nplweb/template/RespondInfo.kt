package net.colors_wind.nplweb.template

import net.colors_wind.nplweb.data.SentenceData

data class RespondInfo(val items: List<String> = emptyList(), val sentenceData: List<SentenceData>? = null)