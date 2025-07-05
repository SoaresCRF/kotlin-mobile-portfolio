package com.dev.soarescrf.soares.ui.projects

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.soarescrf.soares.R
import com.dev.soarescrf.soares.databinding.AdapterItemProjectsBinding
import com.dev.soarescrf.soares.domain.model.Repository
import com.dev.soarescrf.soares.utils.DateUtils.formatUpdatedAt
import com.dev.soarescrf.soares.utils.TextUtils

/**
 * Adapter responsável por exibir uma lista de repositórios (projetos) em um RecyclerView.
 *
 * Utiliza ListAdapter com DiffUtil para otimizar atualizações de itens.
 */
class ProjectsAdapter :
    ListAdapter<Repository, ProjectsAdapter.ProjectViewHolder>(RepositoryDiffCallback()) {

    /**
     * Cria um novo ViewHolder para representar um item da lista.
     *
     * @param parent O ViewGroup no qual a nova View será adicionada após ser vinculada a uma posição.
     * @param viewType O tipo de visualização do novo item.
     * @return Um novo ProjectViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = AdapterItemProjectsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProjectViewHolder(binding)
    }

    /**
     * Faz o bind dos dados de um repositório à ViewHolder na posição especificada.
     *
     * @param holder O ViewHolder que deve ser atualizado.
     * @param position A posição do item na lista.
     */
    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder responsável por vincular os dados de um repositório aos componentes visuais.
     *
     * @property binding Referência ao layout do item.
     */
    inner class ProjectViewHolder(
        private val binding: AdapterItemProjectsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Realiza o bind de um objeto [Repository] aos elementos de UI.
         *
         * @param repository O repositório a ser exibido.
         */
        fun bind(repository: Repository) = with(binding) {
            setupRepositoryName(repository)
            setupRepositoryDescription(repository)
            setupRepositoryLanguage(repository)
            setupRepositoryLastUpdated(repository)
        }

        /**
         * Configura o nome do repositório como um link clicável com cor baseada na linguagem.
         */
        private fun AdapterItemProjectsBinding.setupRepositoryName(repository: Repository) {
            val color = repository.getLanguageColor().toColorOrDefault()
            TextUtils.setColoredLink(textName, repository.name, repository.htmlUrl, color)
        }

        /**
         * Define a descrição do repositório e aplica justificação ao texto.
         */
        private fun AdapterItemProjectsBinding.setupRepositoryDescription(repository: Repository) {
            textDescription.text = repository.getDescription()
            TextUtils.justifyText(textDescription)
        }

        /**
         * Exibe a linguagem de programação do repositório e aplica a cor correspondente.
         */
        private fun AdapterItemProjectsBinding.setupRepositoryLanguage(repository: Repository) {
            val language = repository.language
            val color = repository.getLanguageColor().toColorOrDefault()

            textLanguage.text = language
            applyTextColors(if (language.isEmpty()) Color.DKGRAY else color)
        }

        /**
         * Mostra a data do último push do repositório, formatada.
         */
        private fun AdapterItemProjectsBinding.setupRepositoryLastUpdated(repository: Repository) {
            val formattedDate = formatUpdatedAt(repository.pushedAt)
            val contextText = root.context.getString(R.string.last_updated_at)
            textUpdatedAt.text = String.format(contextText, formattedDate)
        }

        /**
         * Aplica a cor especificada aos textos relacionados ao repositório.
         */
        private fun AdapterItemProjectsBinding.applyTextColors(color: Int) {
            textLanguage.setTextColor(color)
            textDescription.setTextColor(color)
            textUpdatedAt.setTextColor(color)
        }
    }

    /**
     * Implementação de [DiffUtil.ItemCallback] para calcular diferenças entre listas de repositórios.
     */
    private class RepositoryDiffCallback : DiffUtil.ItemCallback<Repository>() {

        /**
         * Verifica se dois itens representam o mesmo repositório com base no ID.
         */
        override fun areItemsTheSame(oldItem: Repository, newItem: Repository): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Verifica se o conteúdo de dois repositórios é exatamente igual.
         */
        override fun areContentsTheSame(oldItem: Repository, newItem: Repository): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Converte uma string de cor em um inteiro de cor. Retorna cinza escuro caso inválido.
 *
 * @return A cor convertida ou [Color.DKGRAY] como fallback.
 */
private fun String?.toColorOrDefault(): Int {
    return try {
        this?.toColorInt() ?: Color.DKGRAY
    } catch (_: IllegalArgumentException) {
        Color.DKGRAY
    }
}
