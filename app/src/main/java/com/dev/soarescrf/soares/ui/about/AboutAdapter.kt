package com.dev.soarescrf.soares.ui.about

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dev.soarescrf.soares.databinding.AdapterItemAboutParagraphBinding
import com.dev.soarescrf.soares.utils.TextUtils

/**
 * Adapter para exibir uma lista de parágrafos sobre mim
 * na tela "Sobre" ([AboutFragment]).
 *
 * @property paragraphResIds Lista de IDs de string com o conteúdo HTML dos parágrafos.
 * @property accessibilityResIds Lista de IDs de string com textos descritivos para acessibilidade.
 */
class AboutAdapter(
    private val paragraphResIds: List<Int>,
    private val accessibilityResIds: List<Int>
) : RecyclerView.Adapter<AboutAdapter.AboutViewHolder>() {

    /**
     * ViewHolder que contém a view de cada parágrafo.
     *
     * @property binding Binding da view do item.
     */
    inner class AboutViewHolder(val binding: AdapterItemAboutParagraphBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Cria e infla a view do item da lista.
     *
     * @param parent O ViewGroup pai.
     * @param viewType Tipo de view (não utilizado aqui).
     * @return Uma nova instância de [AboutViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutViewHolder {
        val binding = AdapterItemAboutParagraphBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AboutViewHolder(binding)
    }

    /**
     * Associa os dados ao item da lista.
     *
     * @param holder ViewHolder atual.
     * @param position Posição do item na lista.
     */
    override fun onBindViewHolder(holder: AboutViewHolder, position: Int) {
        val context = holder.itemView.context

        // Define o texto HTML formatado no TextView
        val htmlText = Html.fromHtml(
            context.getString(paragraphResIds[position]),
            Html.FROM_HTML_MODE_LEGACY
        )
        holder.binding.textParagraph.text = htmlText

        // Define a descrição para acessibilidade
        holder.binding.textParagraph.contentDescription =
            context.getString(accessibilityResIds[position])

        // Justifica o texto visualmente
        TextUtils.justifyText(holder.binding.textParagraph)

        // Adiciona margem inferior ao último item
        val layoutParams = holder.binding.textParagraph.layoutParams as ViewGroup.MarginLayoutParams
        if (position == paragraphResIds.lastIndex) {
            layoutParams.bottomMargin = 12
        }
    }

    /**
     * Retorna o número total de parágrafos.
     *
     * @return Quantidade de itens na lista.
     */
    override fun getItemCount() = paragraphResIds.size
}
