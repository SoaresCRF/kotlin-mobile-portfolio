package com.dev.soarescrf.soares.utils

/**
 * Utilitário para mapear linguagens de programação a cores específicas.
 */
object ColorUtils {

    /**
     * Objeto responsável por armazenar o mapeamento de linguagens para cores hexadecimais.
     */
    object LanguageColorMapper {
        // Mapa que associa o nome da linguagem à sua cor representativa em formato hexadecimal
        private val languageColors = mapOf(
            "Java" to "#b07219",
            "Python" to "#3572A5",
            "JavaScript" to "#f1e05a",
            "TypeScript" to "#3178c6",
            "HTML" to "#e34c26",
            "CSS" to "#563d7c",
            "C" to "#555555",
            "C++" to "#f34b7d",
            "C#" to "#178600",
            "PHP" to "#4F5D95",
            "Swift" to "#ffac45",
            "Kotlin" to "#A97BFF",
            "Go" to "#00ADD8",
            "Ruby" to "#701516",
            "Shell" to "#89e051"
        )

        /**
         * Retorna a cor correspondente à linguagem fornecida.
         * Se a linguagem não estiver no mapa, retorna uma cor padrão.
         *
         * @param language Nome da linguagem de programação.
         * @return Código hexadecimal da cor.
         */
        fun getColor(language: String): String {
            return languageColors[language] ?: "#546E7A" // Cor padrão para linguagens desconhecidas
        }
    }
}
