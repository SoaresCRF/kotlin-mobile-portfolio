package com.dev.soarescrf.soares.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utilitário para manipulação e formatação de datas.
 */
object DateUtils {

    private const val TAG = "DateFormatError"

    // Formato ISO padrão usado para interpretar a data recebida, ex: 2023-04-01T14:30:00Z
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)

    // Formato desejado para exibição, ex: 01/04/2023
    private val desiredFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

    /**
     * Converte uma string de data no formato ISO para o formato "dd/MM/yyyy".
     * Se ocorrer erro na conversão, retorna a string original e loga o erro.
     *
     * @param updatedAt String da data no formato ISO.
     * @return Data formatada ou a string original em caso de erro.
     */
    fun formatUpdatedAt(updatedAt: String): String = try {
        isoFormat.parse(updatedAt)?.let { date ->
            desiredFormat.format(date)
        } ?: run {
            Log.e(TAG, "Parsed date is null for input: $updatedAt")
            updatedAt
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse date: $updatedAt", e)
        updatedAt
    }
}
