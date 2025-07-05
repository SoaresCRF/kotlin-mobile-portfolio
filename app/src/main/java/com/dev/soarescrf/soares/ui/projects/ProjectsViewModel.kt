package com.dev.soarescrf.soares.ui.projects

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.soarescrf.soares.data.network.RetrofitClient
import com.dev.soarescrf.soares.data.service.PortfolioRepositoriesApi
import com.dev.soarescrf.soares.domain.model.Repository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ViewModel responsável por buscar, armazenar e filtrar
 * a lista de repositórios/projetos do portfólio.
 */
class ProjectsViewModel : ViewModel() {

    companion object {
        private const val TAG = "ProjectsViewModel"

        // Modos de ordenação
        private const val SORT_RECENT = 0
        private const val SORT_OLDEST = 1
        private const val SORT_ALPHABETICAL = 2

        // Nome de repositório a ser ignorado na lista
        private const val IGNORED_REPOSITORY_NAME = "SoaresCRF"

        // Formato usado para parsear datas do GitHub (ISO 8601 UTC)
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    // LiveData pública para a lista filtrada de repositórios
    private val _repositoriesLiveData = MutableLiveData<List<Repository>>()
    val repositoriesLiveData: LiveData<List<Repository>> get() = _repositoriesLiveData

    // Serviço Retrofit para acessar a API de repositórios do portfólio
    private val portfolioApiService: PortfolioRepositoriesApi =
        RetrofitClient.instance.create(PortfolioRepositoriesApi::class.java)

    // Lista completa de repositórios carregada da API
    private var allRepositories: List<Repository> = emptyList()

    // Estado dos filtros e ordenação aplicados
    private var searchQuery: String = ""
    private var selectedLanguage: String = ""
    private var sortMode: Int = SORT_RECENT

    /**
     * Busca repositórios da API via Retrofit.
     * - Filtra repositório ignorado
     * - Aplica filtros e atualiza LiveData
     * - Registra logs em caso de sucesso ou falha
     */
    fun fetchRepositories() {
        portfolioApiService.getRepositories().enqueue(object : Callback<List<Repository>> {
            override fun onResponse(
                call: Call<List<Repository>>,
                response: Response<List<Repository>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { repositories ->
                        // Filtra repositório que deve ser ignorado
                        allRepositories = repositories.filterNot {
                            it.name.equals(
                                IGNORED_REPOSITORY_NAME,
                                ignoreCase = true
                            )
                        }
                        applyFilters()
                        Log.d(TAG, "Repositories loaded successfully.")
                    }
                } else {
                    Log.e(TAG, "API Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                Log.e(TAG, "API Failure: ${t.message}", t)
            }
        })
    }

    /**
     * Atualiza o texto de busca e reaplica filtros.
     *
     * @param query texto de busca digitado pelo usuário
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        applyFilters()
    }

    /**
     * Atualiza a linguagem selecionada para filtro e reaplica filtros.
     *
     * @param language linguagem selecionada no filtro ("" significa todas)
     */
    fun updateSelectedLanguage(language: String) {
        selectedLanguage = language
        applyFilters()
    }

    /**
     * Cicla entre os modos de ordenação disponíveis
     * e reaplica filtros para atualizar a lista.
     */
    fun cycleSortMode() {
        sortMode = (sortMode + 1) % 3
        applyFilters()
    }

    /**
     * Retorna o modo de ordenação atual.
     */
    fun getSortMode(): Int = sortMode

    /**
     * Retorna a quantidade total de repositórios carregados,
     * antes da aplicação de filtros.
     */
    fun getTotalRepositoryCount(): Int = allRepositories.size

    /**
     * Retorna o conjunto de linguagens disponíveis
     * na lista completa de repositórios.
     */
    fun getAvailableLanguages(): Set<String> =
        allRepositories.map { it.language }.toSet()

    /**
     * Aplica os filtros de busca, linguagem e ordenação
     * sobre a lista completa de repositórios, e publica
     * o resultado no LiveData para atualização da UI.
     */
    private fun applyFilters() {
        val filteredList = allRepositories
            .asSequence()
            .filter { it.matchesQuery(searchQuery) }
            .filter { it.matchesLanguage(selectedLanguage) }
            .sortedWith(getSortComparator())
            .toList()

        _repositoriesLiveData.postValue(filteredList)
    }

    /**
     * Retorna o comparador adequado para ordenar
     * os repositórios segundo o modo atual.
     */
    private fun getSortComparator(): Comparator<Repository> = when (sortMode) {
        SORT_RECENT -> compareByDescending { it.pushedAt.toDate() }
        SORT_OLDEST -> compareBy { it.pushedAt.toDate() }
        SORT_ALPHABETICAL -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        else -> compareByDescending { it.pushedAt.toDate() }
    }

    /**
     * Verifica se o nome do repositório contém a query de busca,
     * ignorando maiúsculas/minúsculas.
     */
    private fun Repository.matchesQuery(query: String): Boolean =
        name.contains(query, ignoreCase = true)

    /**
     * Verifica se a linguagem do repositório corresponde
     * ao filtro selecionado ou se o filtro está vazio (todas).
     */
    private fun Repository.matchesLanguage(language: String): Boolean =
        language.isEmpty() || language == this.language

    /**
     * Converte a String da data (formato ISO 8601 UTC) para objeto Date.
     * Caso haja erro no parsing, retorna data "zero".
     */
    private fun String?.toDate(): Date =
        try {
            this?.let { DATE_FORMAT.parse(it) } ?: Date(0)
        } catch (e: Exception) {
            Log.w(TAG, "Date parsing error for: $this", e)
            Date(0)
        }
}
