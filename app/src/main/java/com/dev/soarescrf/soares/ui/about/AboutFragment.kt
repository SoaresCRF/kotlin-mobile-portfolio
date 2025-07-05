package com.dev.soarescrf.soares.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.soarescrf.soares.R
import com.dev.soarescrf.soares.databinding.FragmentAboutBinding

/**
 * Fragmento responsável por exibir informações sobre mim.
 *
 * Exibe uma lista de parágrafos informativos utilizando um [RecyclerView],
 * com suporte à acessibilidade por meio de strings alternativas.
 */
class AboutFragment : Fragment() {

    /** Referência interna para o binding da view. */
    private var _binding: FragmentAboutBinding? = null

    /** Binding não nulo da view, acessível somente entre onCreateView e onDestroyView. */
    private val binding get() = _binding!!

    /**
     * Infla o layout do fragmento e inicializa o binding.
     *
     * @param inflater O LayoutInflater usado para inflar a view.
     * @param container O ViewGroup pai que conterá a view (ou null).
     * @param savedInstanceState Estado previamente salvo, se houver.
     * @return A raiz da view inflada.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configura o conteúdo da tela após a view ser criada.
     *
     * Inicializa a lista de parágrafos e suas respectivas versões acessíveis,
     * e configura o [RecyclerView] com um [LinearLayoutManager] e o [AboutAdapter].
     *
     * @param view A view raiz criada para o fragmento.
     * @param savedInstanceState Estado previamente salvo, se houver.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paragraphList = listOf(
            R.string.about_paragraph_1,
            R.string.about_paragraph_2,
            R.string.about_paragraph_3,
            R.string.about_paragraph_4,
            R.string.about_paragraph_5
        )

        val accessibilityList = listOf(
            R.string.accessibility_about_paragraph_1,
            R.string.accessibility_about_paragraph_2,
            R.string.accessibility_about_paragraph_3,
            R.string.accessibility_about_paragraph_4,
            R.string.accessibility_about_paragraph_5
        )

        binding.recyclerAbout.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = AboutAdapter(paragraphList, accessibilityList)
        }
    }

    /**
     * Libera a referência do binding para evitar memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
