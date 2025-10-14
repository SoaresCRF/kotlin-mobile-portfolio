package com.dev.soarescrf.soares.domain.model

import com.dev.soarescrf.soares.utils.ColorUtils
import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados que representa um repositório GitHub.
 *
 * @property id identificador único do repositório
 * @property name nome do repositório
 * @property description descrição do repositório (pode ser nula)
 * @property language linguagem principal usada no repositório
 * @property htmlUrl URL web para o repositório (mapeado do JSON como "html_url")
 * @property createdAt data de criação do repositório (formato String ISO)
 * @property pushedAt data do último push no repositório (formato String ISO)
 * @property updatedAt data da última atualização no repositório (formato String ISO)
 * @property zipUrl link para download do arquivo ZIP contendo a versão mais recente do repositório (gerado a partir do último push)
 */
data class Repository(
    val id: Long,
    val name: String,
    private val description: String?,
    val language: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("pushed_at") val pushedAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("zip_url") val zipUrl: String
) {

    /**
     * Retorna a descrição do repositório ou "N/A" caso
     * a descrição seja nula ou vazia.
     */
    fun getDescription(): String {
        return if (description.isNullOrEmpty()) "N/A" else description
    }

    /**
     * Retorna a cor associada à linguagem do repositório,
     * usando um mapeamento definido em ColorUtils.LanguageColorMapper.
     */
    fun getLanguageColor(): String {
        return ColorUtils.LanguageColorMapper.getColor(language)
    }
}
