package com.dev.soarescrf.soares.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utilitário para verificar o status da conexão de rede.
 */
object NetworkUtils {

    /**
     * Verifica se a rede está disponível e com acesso à internet.
     *
     * @param context Contexto necessário para acessar os serviços do sistema.
     * @return true se houver conexão com internet, false caso contrário.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        // Obtém o serviço de conectividade do sistema
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        // Pega a rede atualmente ativa; se nenhuma, retorna false
        val network = connectivityManager?.activeNetwork ?: return false

        // Obtém as capacidades da rede ativa; se não disponíveis, retorna false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Verifica se a rede tem capacidade de acesso à internet
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
