package com.dev.soarescrf.soares.ui.education

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.soarescrf.soares.R
import com.dev.soarescrf.soares.databinding.FragmentEducationBinding

/**
 * [Fragment] responsável por exibir informações sobre cursos e tecnologias estudadas.
 * Utiliza dois [RecyclerView]s para listar os conteúdos educacionais.
 */
class EducationFragment : Fragment() {

    /** Binding para acesso à view do fragment. */
    private var _binding: FragmentEducationBinding? = null

    /** Acesso não-nulo ao binding. */
    private val binding get() = _binding!!

    /**
     * Infla o layout do fragment e inicializa o binding.
     *
     * @param inflater O objeto responsável por inflar o layout.
     * @param container O container pai da view, se houver.
     * @param savedInstanceState Estado previamente salvo do fragment, se existir.
     * @return A raiz da view inflada.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEducationBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Chamado após a criação da view. Configura os adapters e listas de conteúdo
     * para os cursos e tecnologias.
     *
     * @param view A view raiz do fragment.
     * @param savedInstanceState Estado previamente salvo, se houver.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paragraphCourseList = listOf(
            R.string.course_1,
            R.string.course_2,
            R.string.course_3,
            R.string.course_4,
            R.string.course_5,
            R.string.course_6,
            R.string.course_7,
            R.string.course_8,
            R.string.course_9,
            R.string.course_10
        )

        val paragraphTechList = listOf(
            R.string.tech_1,
            R.string.tech_2,
            R.string.tech_3,
            R.string.tech_4,
            R.string.tech_5
        )

        val coursesRecycler = view.findViewById<RecyclerView>(R.id.recyclerCourses)
        val techRecycler = view.findViewById<RecyclerView>(R.id.recyclerTechnologies)

        coursesRecycler.layoutManager = LinearLayoutManager(requireContext())
        techRecycler.layoutManager = LinearLayoutManager(requireContext())

        coursesRecycler.adapter = EducationAdapter(paragraphCourseList)
        techRecycler.adapter = EducationAdapter(paragraphTechList)
    }

    /**
     * Libera o binding quando a view é destruída para evitar memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
