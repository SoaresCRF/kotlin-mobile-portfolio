package com.dev.soarescrf.soares.data.network

import com.dev.soarescrf.soares.data.service.PortfolioRepositoriesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton responsável por criar e fornecer uma instância do Retrofit configurada.
 *
 * Utiliza a URL base definida na API do portfólio e o conversor Gson para serializar/deserializar JSON.
 */
object RetrofitClient {

    /**
     * Instância preguiçosamente inicializada do Retrofit.
     * Configurada com a URL base e o conversor Gson.
     */
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(PortfolioRepositoriesApi.BASE_URL)  // Define a URL base da API
            .addConverterFactory(GsonConverterFactory.create()) // Converte JSON para objetos Kotlin e vice-versa
            .build()
    }
}
