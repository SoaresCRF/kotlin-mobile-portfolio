package com.dev.soarescrf.soares.utils

import android.graphics.Paint
import android.os.Build
import android.text.Html
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.widget.TextView

/**
 * Utilitários para manipulação de texto em TextViews.
 */
object TextUtils {

    /**
     * Aplica sublinhado ao texto do TextView.
     */
    fun underline(textView: TextView) {
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    /**
     * Define um link HTML clicável com texto e URL no TextView.
     */
    fun setHtmlLink(textView: TextView, linkText: String, url: String) {
        val spanned: Spanned =
            Html.fromHtml("<a href=\"$url\">$linkText</a>", Html.FROM_HTML_MODE_LEGACY)
        textView.apply {
            text = spanned
            // Habilita o clique no link
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    /**
     * Define um link com texto colorido e URL clicável no TextView.
     */
    fun setColoredLink(textView: TextView, linkText: String, url: String, color: Int) {
        val spannable = SpannableString(linkText).apply {
            // Aplica o link clicável
            setSpan(URLSpan(url), 0, linkText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Aplica a cor no texto do link
            setSpan(
                ForegroundColorSpan(color),
                0,
                linkText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textView.apply {
            text = spannable
            // Habilita o clique no link
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    /**
     * Justifica o texto do TextView (disponível a partir do Android Oreo).
     */
    fun justifyText(textView: TextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
        }
    }
}
