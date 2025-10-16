package com.dev.soarescrf.soares.ui.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.soarescrf.soares.R
import com.dev.soarescrf.soares.databinding.FragmentProjectsBinding
import com.dev.soarescrf.soares.utils.NetworkUtils

/**
 * Fragmento responsável por exibir e gerenciar a lista de projetos (repositórios).
 * Contém recursos de paginação, filtro por linguagem, busca textual e ordenação.
 */
class ProjectsFragment : Fragment() {

    /** Página atual da listagem paginada */
    private var currentPage = 1

    /** Quantidade de itens por página */
    private val itemsPerPage = 10

    /** ViewBinding para acessar as views do layout XML */
    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    /** ViewModel responsável por gerenciar dados e lógica da UI */
    private val viewModel: ProjectsViewModel by viewModels()

    /** Adapter responsável por exibir os projetos na RecyclerView */
    private lateinit var projectAdapter: ProjectsAdapter

    /** Adapter responsável por preencher o spinner de linguagens */
    private lateinit var languageAdapter: ArrayAdapter<String>

    /** Flag que indica se é o primeiro carregamento da tela */
    private var isFirstLoad = true

    /**
     * Infla o layout do fragmento.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura a interface e inicializa os componentes ao criar a view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupLanguageSpinner()
        setupSearchInput()
        setupSortButton()
        setupPaginationButtons()
        observeViewModel()

        // Busca os repositórios apenas se houver conexão com a internet
        fetchRepositoriesIfNetworkAvailable()

        // Botão de tentativa novamente em caso de erro
        binding.buttonRetry.setOnClickListener {
            viewModel.fetchRepositories()
        }
    }

    /**
     * Recarrega os repositórios ao retornar ao fragmento, caso não seja o primeiro carregamento.
     */
    override fun onResume() {
        super.onResume()
        if (!isFirstLoad) fetchRepositoriesIfNetworkAvailable()
        isFirstLoad = false
    }

    /**
     * Cancela tarefas em andamento e limpa o binding ao destruir a view.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.cancelFetchJob()
        _binding = null
    }

    /**
     * Configura o RecyclerView e associa o adapter de projetos.
     */
    private fun setupRecyclerView() = with(binding.recyclerProjects) {
        layoutManager = LinearLayoutManager(requireContext())
        setHasFixedSize(true) // melhora desempenho com tamanho fixo
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        projectAdapter = ProjectsAdapter()
        adapter = projectAdapter
    }

    /**
     * Configura o Spinner de linguagens disponíveis para filtro.
     */
    private fun setupLanguageSpinner() {
        languageAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf(getString(R.string.all_languages))
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerLanguage.apply {
            adapter = languageAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    // Obtém o idioma selecionado
                    val selected = languageAdapter.getItem(position).orEmpty()
                    val filter = if (selected == getString(R.string.all_languages)) "" else selected

                    // Atualiza filtro no ViewModel
                    viewModel.updateSelectedLanguage(filter)

                    // Reinicia paginação ao alterar filtro
                    currentPage = 1
                    updatePagination()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
    }

    /**
     * Configura o campo de busca textual por nome do projeto.
     */
    private fun setupSearchInput() {
        binding.editSearch.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text.toString()) // atualiza query no ViewModel
            currentPage = 1
            updatePagination()
        }
    }

    /**
     * Configura o botão de ordenação de repositórios (recente, antigo, alfabético, etc.).
     */
    private fun setupSortButton() {
        binding.buttonSort.setOnClickListener {
            viewModel.cycleSortMode() // alterna modo de ordenação
            updateSortButtonText()
            currentPage = 1
            updatePagination()
        }
    }

    /**
     * Configura os botões de paginação (anterior e próximo).
     */
    private fun setupPaginationButtons() = with(binding) {
        buttonPreviousPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage-- // volta uma página
                updatePagination()
            }
        }
        buttonNextPage.setOnClickListener {
            val totalPages = calculateTotalPages()
            if (currentPage < totalPages) {
                currentPage++ // avança uma página
                updatePagination()
            }
        }
    }

    /**
     * Busca os repositórios se houver conexão com a internet, caso contrário exibe mensagem de erro.
     */
    private fun fetchRepositoriesIfNetworkAvailable() {
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            viewModel.fetchRepositories()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_internet_connection),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Observa os LiveData do ViewModel para reagir às mudanças de estado da interface.
     */
    private fun observeViewModel() {
        viewModel.repositoriesLiveData.observe(viewLifecycleOwner) { _ ->
            updatePagination()
            updateSortButtonText()
            updateLanguageOptions(viewModel.getAvailableLanguages())
            binding.layoutError.visibility = View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.layoutError.visibility = View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.layoutError.visibility = if (!error.isNullOrBlank()) {
                binding.textError.text = error
                View.VISIBLE
            } else View.GONE
        }

        viewModel.showTimeoutLoading.observe(viewLifecycleOwner) { show ->
            binding.lottieLoadingProjects.visibility = if (show) View.VISIBLE else View.GONE
        }

        viewModel.toastEvent.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                binding.root.announceForAccessibilityCompat(it)
                viewModel.clearToast()
            }
        }
    }

    /**
     * Atualiza a lista exibida conforme a página atual e filtros aplicados.
     */
    private fun updatePagination() {
        val filteredRepositories = viewModel.getFilteredRepositories()
        val totalPages = calculateTotalPages()

        // Garante que a página atual nunca exceda o total disponível
        if (currentPage > totalPages) currentPage = totalPages

        // Calcula índice de início e fim da sublista
        val start = (currentPage - 1) * itemsPerPage
        val end = (start + itemsPerPage).coerceAtMost(filteredRepositories.size)
        val currentList = filteredRepositories.subList(start, end)

        // Atualiza lista no adapter e rola para o topo
        projectAdapter.submitList(currentList) {
            // Garante scroll para o topo após atualizar a lista
            (binding.recyclerProjects.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(0, 0)
        }

        // Atualiza indicador visual da página
        binding.textPageIndicator.text = getString(
            R.string.page_indicator_format,
            currentPage,
            totalPages
        )

        // Atualiza estado dos botões de navegação
        updatePaginationButtons(totalPages)

        // Atualiza contador de itens exibidos
        updateRepositoryCount(
            startIndex = start + 1,
            endIndex = end,
            totalCount = filteredRepositories.size
        )
    }

    /**
     * Habilita ou desabilita os botões de navegação de página conforme o estado atual.
     */
    private fun updatePaginationButtons(totalPages: Int) {
        binding.buttonPreviousPage.isEnabled = currentPage > 1
        binding.buttonNextPage.isEnabled = currentPage < totalPages
    }

    /**
     * Calcula o número total de páginas baseado na quantidade de itens filtrados.
     * @return número total de páginas
     */
    private fun calculateTotalPages(): Int {
        val filteredRepositories = viewModel.getFilteredRepositories()
        return if (filteredRepositories.isEmpty()) 1
        else (filteredRepositories.size + itemsPerPage - 1) / itemsPerPage
    }

    /**
     * Atualiza o texto exibido no botão de ordenação conforme o modo atual.
     */
    private fun updateSortButtonText() {
        val sortText = when (viewModel.getSortMode()) {
            0 -> getString(R.string.sort_recent)
            1 -> getString(R.string.sort_oldest)
            2 -> getString(R.string.sort_alphabetical)
            else -> getString(R.string.sort_default)
        }
        binding.buttonSort.text = sortText
    }

    /**
     * Atualiza o contador de repositórios exibido na interface e sua descrição para acessibilidade.
     */
    private fun updateRepositoryCount(startIndex: Int, endIndex: Int, totalCount: Int) {
        val visualText = resources.getQuantityString(
            R.plurals.repository_count_format,
            totalCount, // controla o plural automaticamente
            startIndex,
            endIndex,
            totalCount
        )

        val accessibleText = resources.getQuantityString(
            R.plurals.accessibility_repository_count_format,
            totalCount, // controla o plural automaticamente
            startIndex,
            endIndex,
            totalCount
        )

        binding.textRepositoryCount.text = visualText
        binding.textRepositoryCount.contentDescription = accessibleText
    }

    /**
     * Atualiza a lista de linguagens disponíveis no Spinner de filtro.
     */
    private fun updateLanguageOptions(languages: Set<String>) {
        val items = listOf(getString(R.string.all_languages)) + languages.sorted()
        languageAdapter.clear()
        languageAdapter.addAll(items)
        languageAdapter.notifyDataSetChanged()
    }

    /**
     * Extensão para emitir mensagens de acessibilidade, garantindo compatibilidade com leitores de tela.
     */
    private fun View.announceForAccessibilityCompat(text: String) {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

        if (accessibilityManager?.isEnabled == true) {
            @Suppress("DEPRECATION")
            val event = AccessibilityEvent.obtain().apply {
                eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED
                className = javaClass.name
                packageName = context.packageName
                this.text.add(text)
            }
            accessibilityManager.sendAccessibilityEvent(event)
        }
    }
}
