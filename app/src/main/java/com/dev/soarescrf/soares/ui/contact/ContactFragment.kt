package com.dev.soarescrf.soares.ui.contact

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.dev.soarescrf.soares.R
import com.dev.soarescrf.soares.data.preferences.IntroActivityPreferences
import com.dev.soarescrf.soares.databinding.CustomDialogIntroConfigBinding
import com.dev.soarescrf.soares.databinding.FragmentContactBinding
import com.dev.soarescrf.soares.utils.TextUtils

/**
 * Fragment responsável pela tela de contato.
 *
 * Nesta tela, o usuário pode:
 * - Enviar e-mails diretamente para o desenvolvedor.
 * - Acessar redes sociais como GitHub, Instagram e LinkedIn.
 * - Reativar a exibição da tela de introdução (IntroActivity).
 */
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    /**
     * Infla o layout do fragment com ViewBinding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Executado após a view ser criada.
     *
     * Inicializa os eventos de clique nos elementos de contato e
     * configura os links de redes sociais.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentContactBinding.bind(view)

        setupEmailClick()
        setupSocialLinks()

        binding.buttonResetIntro.setOnClickListener {
            showIntroConfigDialog()
        }
    }

    /**
     * Libera os recursos de ViewBinding para evitar memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Configura o clique no campo de e-mail.
     *
     * - Aplica sublinhado no texto.
     * - Ao clicar, abre o app de e-mail com os campos preenchidos.
     */
    private fun setupEmailClick() = with(binding.textEmail) {
        setOnClickListener { sendEmail() }
        TextUtils.underline(this)
    }

    /**
     * Configura os links clicáveis para redes sociais.
     *
     * Define textos com links para GitHub, Instagram e LinkedIn,
     * além de sublinhar a opção de Play Store.
     */
    private fun setupSocialLinks() = with(binding) {
        TextUtils.setHtmlLink(
            textGitHub,
            "GitHub",
            "https://github.com/SoaresCRF"
        )

        TextUtils.setHtmlLink(
            textInstagram,
            "Instagram",
            "https://www.instagram.com/soarescrf_/"
        )

        TextUtils.setHtmlLink(
            textLinkedIn,
            "LinkedIn",
            "https://www.linkedin.com/in/matheus-soares-0569b8251/"
        )

        TextUtils.underline(textPlayStore)
    }

    /**
     * Abre um intent de envio de e-mail.
     *
     * Caso não exista um app de e-mail instalado, exibe um Toast informando o erro.
     */
    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf("matheussoarescrf10@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Vim pelo aplicativo!")
            putExtra(Intent.EXTRA_TEXT, "...")
        }

        try {
            startActivity(emailIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Nenhum aplicativo de e-mail encontrado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Exibe um diálogo para configurar a exibição da tela de introdução.
     *
     * Permite ativar ou desativar a tela de introdução [com.dev.soarescrf.soares.ui.activity.IntroActivity]
     * ao iniciar o aplicativo.
     */
    private fun showIntroConfigDialog() {
        val introActivityPreferences = IntroActivityPreferences(requireContext())

        val binding = CustomDialogIntroConfigBinding.inflate(layoutInflater)
        binding.checkShowIntro.isChecked = introActivityPreferences.showIntroOnStartup

        val dialog = AlertDialog.Builder(requireContext(), R.style.TransparentDialog)
            .setView(binding.root)
            .create()

        binding.imageCheck.setOnClickListener {
            introActivityPreferences.showIntroOnStartup = binding.checkShowIntro.isChecked

            Toast.makeText(
                requireContext(),
                if (introActivityPreferences.showIntroOnStartup)
                    "Tela inicial ativada."
                else
                    "Tela inicial desativada.",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }

        dialog.show()
    }
}
