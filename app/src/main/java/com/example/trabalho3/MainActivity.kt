package com.example.trabalho3

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ajustarBarrasDoSistema(findViewById(R.id.raiz_menu))
    }

    fun abrirBuscaPersonagem(botao: View) {
        val intencao = Intent(this, PersonagemActivity::class.java)
        startActivity(intencao)
    }

    fun abrirBuscaCriatura(botao: View) {
        val intencao = Intent(this, CriaturaActivity::class.java)
        startActivity(intencao)
    }
}
