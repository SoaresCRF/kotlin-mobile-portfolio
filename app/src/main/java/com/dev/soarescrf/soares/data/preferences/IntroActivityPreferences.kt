package com.dev.soarescrf.soares.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Classe responsável por gerenciar as preferências relacionadas à exibição da introdução (onboarding).
 *
 * Utiliza [SharedPreferences] para persistir a informação sobre se a introdução deve ser exibida
 * ao iniciar o aplicativo.
 *
 * @param context Contexto necessário para acessar as preferências compartilhadas.
 */
class IntroActivityPreferences(context: Context) {

    companion object {
        /** Nome do arquivo de preferências. */
        private const val PREFS_NAME = "app_prefs"

        /** Chave para identificar a configuração de exibição da introdução. */
        private const val KEY_SHOW_INTRO = "show_intro_on_startup"
    }

    /** Instância de [SharedPreferences] usada para persistência local. */
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Define se a introdução deve ser exibida na inicialização do app.
     *
     * - `true`: a introdução será exibida.
     * - `false`: a introdução será ignorada.
     */
    var showIntroOnStartup: Boolean
        get() = prefs.getBoolean(KEY_SHOW_INTRO, true)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_INTRO, value) }
}
