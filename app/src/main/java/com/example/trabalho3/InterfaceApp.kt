package com.example.trabalho3

import android.view.View
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun ajustarBarrasDoSistema(raiz: View) {
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { tela, margens ->
        val barras = margens.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
        )

        tela.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        margens
    }
}

fun validarNome(campoNome: EditText): String? {
    val nome = campoNome.text.toString().trim()

    campoNome.error = when {
        nome.isEmpty() -> campoNome.context.getString(R.string.erro_nome_vazio)
        nome.length < 3 -> campoNome.context.getString(R.string.erro_nome_curto)
        else -> null
    }

    return if (campoNome.error == null) nome else null
}