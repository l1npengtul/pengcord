package net.pengtul.pengcord.util

import org.json.JSONObject

class TranslationProvider {
    private val translationJsonMap: HashMap<String, String> = HashMap()

    init {
        val resourceFileString = TranslationProvider::class.java.getResourceAsStream("/1.18.json")!!.bufferedReader().use { it.readText() }
        val jsonMap = JSONObject(resourceFileString).toMap() as HashMap<String, HashMap<String, String>>
        jsonMap.forEach { outer ->
            outer.value.forEach { inner ->
                translationJsonMap[outer.key+"."+inner.key] = inner.value
            }
        }
    }

    fun getFormatString(key: String): String? {
        return translationJsonMap[key]
    }
}