package com.dev.soarescrf.soares.data.service

import com.dev.soarescrf.soares.domain.model.Repository
import retrofit2.Call
import retrofit2.http.GET

/**
 * Interface da API Retrofit para buscar repositórios do portfólio.
 *
 * Define as chamadas HTTP para obter a lista de repositórios do backend.
 */
interface PortfolioRepositoriesApi {

    /**
     * Chamada GET para o endpoint "repositories".
     *
     * @return um Call contendo a lista de objetos Repository obtidos do backend.
     */
    @GET("repositories")
    fun getRepositories(): Call<List<Repository>>

    companion object {
        /**
         * URL base da API usada pelo Retrofit para realizar as requisições.
         */
        const val BASE_URL = "https://portfolio-repositories-backend.onrender.com/"
    }
}
