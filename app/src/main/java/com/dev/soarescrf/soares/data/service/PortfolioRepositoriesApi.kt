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
     * Realiza uma requisição GET ao endpoint "repositories" para obter a lista de repositórios.
     *
     * Esta função é suspensa e deve ser chamada dentro de uma coroutine.
     *
     * @return uma lista de objetos [Repository] obtida do backend.
     */
    @GET("repositories")
    suspend fun getRepositories(): List<Repository>

    companion object {
        /**
         * URL base da API usada pelo Retrofit para realizar as requisições.
         */
        const val BASE_URL = "https://portfolio-repositories-backend.onrender.com/"
    }
}
