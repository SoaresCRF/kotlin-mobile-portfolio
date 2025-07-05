package com.dev.soarescrf.soares.ui.education

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.dev.soarescrf.soares.databinding.AdapterItemEducationParagraphBinding
import com.dev.soarescrf.soares.utils.TextUtils

/**
 * Adapter responsável por exibir uma lista de parágrafos de texto sobre formação acadêmica e tecnologias
 * em um [RecyclerView], utilizando recursos de string (HTML) como conteúdo.
 *
 * @param paragraphResIds Lista de IDs de recursos de string que representam os parágrafos.
 */
class EducationAdapter(
    private val paragraphResIds: List<Int>
) : RecyclerView.Adapter<EducationAdapter.EducationViewHolder>() {

    /**
     * ViewHolder que representa um item da lista de parágrafos.
     *
     * @property binding Binding da view associada ao item.
     */
    inner class EducationViewHolder(
        val binding: AdapterItemEducationParagraphBinding
    ) : RecyclerView.ViewHolder(binding.root)

    /**
     * Cria e infla a view do ViewHolder para um item da lista.
     *
     * @param parent O ViewGroup pai no qual a nova view será adicionada.
     * @param viewType O tipo da view (não utilizado neste caso).
     * @return Uma nova instância de [EducationViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EducationViewHolder {
        val binding = AdapterItemEducationParagraphBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EducationViewHolder(binding)
    }

    /**
     * Faz o binding do conteúdo (texto HTML) para o item na posição especificada.
     *
     * @param holder ViewHolder que deve ser atualizado.
     * @param position Posição do item na lista.
     */
    override fun onBindViewHolder(holder: EducationViewHolder, position: Int) {
        val context = holder.itemView.context
        @StringRes val resId = paragraphResIds[position]

        val htmlText = Html.fromHtml(context.getString(resId), Html.FROM_HTML_MODE_LEGACY)
        holder.binding.textParagraph.text = htmlText

        TextUtils.justifyText(holder.binding.textParagraph)
    }

    /**
     * Retorna o número total de itens no adapter.
     *
     * @return Quantidade de parágrafos.
     */
    override fun getItemCount(): Int = paragraphResIds.size
}
