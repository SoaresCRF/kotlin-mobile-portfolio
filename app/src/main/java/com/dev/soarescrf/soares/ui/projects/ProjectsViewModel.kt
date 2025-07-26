package com.dev.soarescrf.soares.ui.projects

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dev.soarescrf.soares.R
import com.dev.soarescrf.soares.data.network.RetrofitClient
import com.dev.soarescrf.soares.data.service.PortfolioRepositoriesApi
import com.dev.soarescrf.soares.domain.model.Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.coroutineContext
import kotlin.math.pow

/**
 * ViewModel responsável por buscar, armazenar e filtrar
 * a lista de repositórios/projetos do portfólio.
 */
class ProjectsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        //Tag utilizada para logs gerais relacionados ao ViewModel de projetos.
        private const val TAG = "ProjectsViewModel"

        //Tag específica para logs relacionados ao ciclo de vida de jobs.
        private const val TAG_JOB = "JOB"

        //Referência ao job atual de busca de repositórios, permitindo controle e cancelamento.
        private var fetchJob: Job? = null

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

    // Indica se uma operação de carregamento está em andamento.
    // Usado para mostrar ou ocultar indicadores de progresso na UI.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Armazena mensagens de erro que podem ser exibidas na interface do usuário.
    // Pode conter null quando não há erro.
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Controla a visibilidade de um indicador de carregamento específico,
    // usado quando a operação está demorando mais que o esperado (timeout).
    private val _showTimeoutLoading = MutableLiveData<Boolean>()
    val showTimeoutLoading: LiveData<Boolean> get() = _showTimeoutLoading

    private val _toastEvent = MutableLiveData<String?>()
    val toastEvent: LiveData<String?> = _toastEvent

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
     * Inicia o processo de busca e filtragem dos repositórios.
     * Utiliza um job cancelável com tentativas automáticas usando backoff exponencial.
     */
    fun fetchRepositories() {
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            Log.d(TAG_JOB, "Job iniciado")

            prepareLoadingState()

            val success = retryWithExponentialBackoff(
                maxAttempts = 10,
                baseDelay = 2000L,
                maxDelay = 15000L
            ) {
                fetchAndFilterRepositories()
            }

            if (!success) {
                handleFetchFailure()
            } else {
                _errorMessage.postValue(null)
            }

            finalizeLoadingState()
        }.also { job ->
            job.invokeOnCompletion { throwable ->
                Log.d(TAG_JOB, "Job finalizado. Motivo: ${throwable?.message ?: "completo"}")
            }
        }
    }

    /**
     * Executa um bloco suspenso com política de tentativas baseada em backoff exponencial.
     *
     * @param maxAttempts Número máximo de tentativas antes de desistir.
     * @param baseDelay Tempo base (em ms) para cálculo do delay exponencial.
     * @param maxDelay Delay máximo permitido entre tentativas.
     * @param block Bloco suspenso a ser executado que retorna true em caso de sucesso.
     * @return true se o bloco teve sucesso em alguma tentativa, false caso contrário.
     */
    private suspend fun retryWithExponentialBackoff(
        maxAttempts: Int,
        baseDelay: Long,
        maxDelay: Long,
        block: suspend () -> Boolean
    ): Boolean {
        var attempt = 0

        while (attempt < maxAttempts && coroutineContext.isActive) {
            if (block()) return true

            attempt++

            _showTimeoutLoading.postValue(true)
            _toastEvent.postValue("Carregando... Tentativa $attempt de $maxAttempts")

            val expDelay = baseDelay * 2.0.pow(attempt).toLong()
            val delayTime = (0..minOf(expDelay, maxDelay).toInt()).random().toLong()
            delay(delayTime)
        }

        return false
    }

    /**
     * Realiza a chamada para buscar os repositórios e aplica filtros,
     * ignorando repositórios com nomes específicos.
     *
     * @return true se a operação foi bem-sucedida, false caso contrário.
     */
    private suspend fun fetchAndFilterRepositories(): Boolean = runCatching {
        val repositories = portfolioApiService.getRepositories()
            .filterNot { it.name.equals(IGNORED_REPOSITORY_NAME, ignoreCase = true) }

        allRepositories = repositories
        applyFilters()
        true
    }.onFailure { exception ->
        if (exception !is SocketTimeoutException && exception !is IOException) {
            throw exception
        }
    }.getOrDefault(false)

    /**
     * Prepara os estados de UI para o carregamento, limpando mensagens de erro e mostrando loading.
     */
    private fun prepareLoadingState() {
        _isLoading.postValue(true)
        _errorMessage.postValue(null)
        _showTimeoutLoading.postValue(false)
    }

    /**
     * Finaliza o estado de carregamento na UI, ocultando indicadores visuais.
     */
    private fun finalizeLoadingState() {
        _isLoading.postValue(false)
        _showTimeoutLoading.postValue(false)
    }

    /**
     * Trata falhas de busca de dados, atualizando mensagens de erro e exibindo toast.
     */
    private fun handleFetchFailure() {
        Log.e(TAG, "Falha ao carregar repositórios.")
        _errorMessage.postValue(getApplication<Application>().getString(R.string.error_loading_data))
        _toastEvent.postValue("Falha ao carregar os dados. Use o botão Tentar Novamente.")
    }

    /**
     * Limpa o evento de toast atual.
     */
    fun clearToast() {
        _toastEvent.postValue(null)
    }

    /**
     * Cancela o job de busca de repositórios em andamento.
     */
    fun cancelFetchJob() {
        fetchJob?.cancel()
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
