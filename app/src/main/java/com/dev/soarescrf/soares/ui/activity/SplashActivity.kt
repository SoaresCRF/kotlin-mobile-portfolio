package com.dev.soarescrf.soares.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev.soarescrf.soares.data.preferences.IntroActivityPreferences

/**
 * Tela de splash exibida ao iniciar o aplicativo.
 *
 * Esta activity tem como responsabilidade verificar se a tela de introdução
 * deve ser exibida com base nas preferências armazenadas. Ela redireciona
 * o usuário para a [IntroActivity] ou [MainActivity], conforme necessário.
 *
 * @constructor Cria uma [SplashActivity] e inicia o fluxo condicional.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var introActivityPreferences: IntroActivityPreferences

    /**
     * Inicializa a activity e redireciona o usuário de acordo com a
     * preferência `showIntroOnStartup`. A tela de splash não é exibida
     * visualmente por muito tempo — seu propósito é apenas de controle de fluxo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        introActivityPreferences = IntroActivityPreferences(this)

        // Define qual activity será aberta com base na preferência do usuário
        val intent = if (introActivityPreferences.showIntroOnStartup) {
            Intent(this, IntroActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }.apply {
            // Evita que a SplashActivity fique na pilha de atividades
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}
