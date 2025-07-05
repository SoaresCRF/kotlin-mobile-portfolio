package com.dev.soarescrf.soares.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dev.soarescrf.soares.databinding.ActivityIntroBinding

/**
 * Tela de introdução inicial exibida ao abrir o aplicativo.
 *
 * Esta activity é responsável por configurar a aparência inicial da UI
 * e redirecionar o usuário para a [MainActivity] após a interação.
 */
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    /**
     * Inicializa a interface da activity, configura o layout de bordas,
     * oculta a action bar, aplica padding nas bordas do sistema e
     * define a transparência da status bar.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        enableEdgeToEdge()

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajusta o padding com base nas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        makeStatusBarTransparent()
        openMainActivity()
    }

    /**
     * Configura o clique do botão de entrada para iniciar a [MainActivity],
     * finalizando a [IntroActivity] para que não permaneça na back stack.
     */
    private fun openMainActivity() {
        binding.button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Torna a status bar transparente e configura a aparência dela
     * para garantir contraste visual com o conteúdo exibido.
     */
    private fun makeStatusBarTransparent() {
        val window = this.window

        // Permite que o conteúdo seja desenhado atrás da status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Define a status bar como transparente
        window.statusBarColor = Color.TRANSPARENT

        // Obtém o controlador de insets compatível
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Define o comportamento da status bar (ex: tema escuro ou claro)
        insetsController.isAppearanceLightStatusBars = false
    }
}
