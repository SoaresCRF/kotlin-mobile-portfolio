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
 * Fragmento que exibe uma lista de projetos/repositórios.
 * Permite filtrar por linguagem, buscar por texto, e ordenar resultados.
 */
class ProjectsFragment : Fragment() {
    // Binding da view para acesso aos componentes UI
    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    // ViewModel responsável pela lógica e dados dos projetos
    private val viewModel: ProjectsViewModel by viewModels()

    // Adapter para RecyclerView de projetos
    private lateinit var projectAdapter: ProjectsAdapter

    // Adapter para Spinner de seleção de linguagem
    private lateinit var languageAdapter: ArrayAdapter<String>

    // Flag para controlar comportamento no primeiro carregamento
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Chamado após a criação da view, realiza as configurações iniciais da UI e lógica:
     * - Inicializa o binding da view
     * - Configura o RecyclerView para exibir a lista de projetos
     * - Configura o spinner para seleção de linguagem
     * - Configura o campo de busca para filtrar projetos
     * - Configura o botão de ordenação dos projetos
     * - Observa os dados do ViewModel para atualizar a UI em tempo real
     * - Dispara a busca de repositórios caso a rede esteja disponível
     * - Configura o botão "Tentar novamente" para refazer a busca em caso de erro
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProjectsBinding.bind(view)

        setupRecyclerView()
        setupLanguageSpinner()
        setupSearchInput()
        setupSortButton()
        observeViewModel()

        fetchRepositoriesIfNetworkAvailable()

        // Define o comportamento do botão "Tentar novamente", reiniciando a busca por repositórios.
        binding.buttonRetry.setOnClickListener {
            viewModel.fetchRepositories()
        }
    }

    /**
     * No retorno ao fragmento:
     * - Caso não seja o primeiro carregamento, tenta buscar os repositórios novamente
     */
    override fun onResume() {
        super.onResume()

        if (!isFirstLoad) fetchRepositoriesIfNetworkAvailable()
        isFirstLoad = false
    }

    /**
     * Chamado quando a view está sendo destruída.
     * - Cancela qualquer job ativo relacionado à busca de repositórios no ViewModel para evitar operações desnecessárias.
     * - Limpa a referência do binding para prevenir memory leaks, garantindo que a view possa ser coletada pelo garbage collector.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Cancela o job ativo do ViewModel
        viewModel.cancelFetchJob()

        // Libera o binding
        _binding = null
    }

    /**
     * Configura o RecyclerView responsável por exibir a lista de projetos.
     *
     * Define o layout linear, aplica otimizações de desempenho e adiciona
     * um separador entre os itens. Também inicializa o adapter do RecyclerView.
     */
    private fun setupRecyclerView() = with(binding.recyclerProjects) {
        layoutManager = LinearLayoutManager(requireContext())
        setHasFixedSize(true)
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        projectAdapter = ProjectsAdapter()
        adapter = projectAdapter
    }

    /**
     * Configura Spinner para seleção de linguagem:
     * - Inicializa com opção "Todas"
     * - Atualiza filtro de linguagem no ViewModel quando selecionada
     */
    private fun setupLanguageSpinner() {
        languageAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf(getString(R.string.all_languages)) // String "Todas" no strings.xml
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerLanguage.apply {
            adapter = languageAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selected = languageAdapter.getItem(position).orEmpty()
                    // Atualiza filtro no ViewModel; "" significa "todas"
                    viewModel.updateSelectedLanguage(if (selected == getString(R.string.all_languages)) "" else selected)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
    }

    /**
     * Configura campo de busca por texto:
     * - Escuta mudanças no texto e atualiza consulta no ViewModel
     */
    private fun setupSearchInput() {
        binding.editSearch.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text.toString())
        }
    }

    /**
     * Configura botão de ordenação:
     * - Ao clicar, cicla entre modos de ordenação (recente, antigo, alfabético)
     * - Atualiza texto do botão para refletir o modo atual
     */
    private fun setupSortButton() {
        binding.buttonSort.setOnClickListener {
            viewModel.cycleSortMode()
            updateSortButtonText()
        }
    }

    /**
     * Verifica conexão de rede antes de buscar os repositórios:
     * - Se não houver internet, exibe mensagem Toast
     * - Se houver, solicita dados ao ViewModel
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
     * Observa todos os estados do ViewModel relevantes para a UI.
     */
    private fun observeViewModel() {
        observeRepositories()
        observeLoadingState()
        observeErrorMessages()
        observeTimeoutLoading()
        observeToastMessages()
    }

    /**
     * Observa a lista de repositórios e atualiza o adapter e elementos da UI.
     */
    private fun observeRepositories() {
        viewModel.repositoriesLiveData.observe(viewLifecycleOwner) { repositories ->
            projectAdapter.submitList(repositories)
            updateRepositoryCount(repositories.size)
            updateSortButtonText()
            updateLanguageOptions(viewModel.getAvailableLanguages())
            binding.layoutError.visibility = View.GONE
        }
    }

    /**
     * Observa o estado de carregamento e oculta a área de erro durante o loading.
     */
    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.layoutError.visibility = View.GONE
        }
    }

    /**
     * Observa mensagens de erro e exibe ou oculta o layout de erro conforme necessário.
     */
    private fun observeErrorMessages() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                binding.textError.text = error
                binding.layoutError.visibility = View.VISIBLE
            } else {
                binding.layoutError.visibility = View.GONE
            }
        }
    }

    /**
     * Observa se o indicador de loading por timeout deve ser exibido.
     */
    private fun observeTimeoutLoading() {
        viewModel.showTimeoutLoading.observe(viewLifecycleOwner) { show ->
            binding.lottieLoadingProjects.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    /**
     * Observa eventos de toast e exibe a mensagem, garantindo acessibilidade.
     */
    private fun observeToastMessages() {
        viewModel.toastEvent.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                binding.root.announceForAccessibilityCompat(it)
                viewModel.clearToast()
            }
        }
    }

    /**
     * Anuncia um texto para acessibilidade de forma compatível com versões antigas do Android.
     *
     * Essa extensão é útil para garantir que usuários com leitores de tela (como TalkBack)
     * recebam feedback auditivo ou tátil ao interagir com a interface.
     *
     * @receiver View onde o evento será anunciado.
     * @param text Texto que será anunciado pelo serviço de acessibilidade.
     */
    fun View.announceForAccessibilityCompat(text: String) {
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

    /**
     * Atualiza texto do botão de ordenação conforme o modo atual do ViewModel
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
     * Atualiza contador de repositórios exibidos e total disponível
     *
     * @param filteredCount quantidade de repositórios filtrados exibidos
     */
    private fun updateRepositoryCount(filteredCount: Int) {
        val totalCount = viewModel.getTotalRepositoryCount()

        // Texto com emoji para exibição visual
        val visualText = getString(
            R.string.repository_count_format,
            filteredCount,
            totalCount
        )

        // Texto sem emoji para leitores de tela
        val accessibleText = getString(
            R.string.accessibility_repository_count_format,
            filteredCount,
            totalCount
        )

        // Aplica no TextView
        binding.textRepositoryCount.text = visualText
        binding.textRepositoryCount.contentDescription = accessibleText
    }

    /**
     * Atualiza as opções do spinner de linguagens:
     * - Inclui sempre a opção "Todas"
     * - Ordena alfabeticamente as linguagens disponíveis
     */
    private fun updateLanguageOptions(languages: Set<String>) {
        val items = listOf(getString(R.string.all_languages)) + languages.sorted()
        languageAdapter.clear()
        languageAdapter.addAll(items)
        languageAdapter.notifyDataSetChanged()
    }
}