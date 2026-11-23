package com.example.trabalho3

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParseException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PersonagemActivity : AppCompatActivity() {

    private lateinit var campoNome: EditText
    private lateinit var barraProgresso: ProgressBar
    private lateinit var textoResultado: TextView

    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personagem)

        ajustarBarrasDoSistema(findViewById(R.id.raiz_personagem))

        campoNome = findViewById(R.id.campo_nome_personagem)
        barraProgresso = findViewById(R.id.progresso_personagem)
        textoResultado = findViewById(R.id.resultado_personagem)
    }

    fun buscarPersonagem(botaoBuscar: View) {
        textoResultado.text = ""

        val nome = validarNome(campoNome) ?: return

        lifecycleScope.launch {
            botaoBuscar.isEnabled = false
            barraProgresso.visibility = View.VISIBLE

            try {
                val personagem = encontrarPersonagem(nome)

                if (personagem == null) {
                    textoResultado.setText(R.string.personagem_nao_encontrado)
                } else {
                    val ausente = getString(R.string.nao_informado)

                    val nomeCompleto = personagem.nome?.ifBlank { ausente } ?: ausente
                    val especie = personagem.especie?.ifBlank { ausente } ?: ausente
                    val casa = personagem.casa?.ifBlank { ausente } ?: ausente

                    textoResultado.text = getString(
                        R.string.detalhes_personagem,
                        nomeCompleto,
                        especie,
                        casa
                    )
                }

            } catch (erro: IOException) {
                textoResultado.setText(R.string.erro_conexao)

            } catch (erro: HttpException) {
                textoResultado.setText(R.string.erro_servidor)

            } catch (erro: JsonParseException) {
                textoResultado.setText(R.string.erro_resposta)
            } finally {
                barraProgresso.visibility = View.GONE
                botaoBuscar.isEnabled = true
            }
        }
    }

    private suspend fun encontrarPersonagem(nome: String): Personagem? {
        val termo = nome.replace(Regex("\\s+"), " ")

        if (' ' in termo) {
            val slug = termo.lowercase().replace(' ', '-')
            val personagem = try {
                apiHogwarts.buscarPersonagemPorSlug(slug).personagem
            } catch (erro: HttpException) {
                if (erro.code() != 404) throw erro
                null
            }
            if (personagem != null) return personagem
        }

        var pagina = 1
        var totalPaginas: Int
        var primeiroResultado: Personagem? = null
        var primeiroSobrenome: Personagem? = null
        var primeiroPrefixo: Personagem? = null

        do {
            val resposta = apiHogwarts.buscarPersonagens(termo, pagina)
            val personagens = resposta.dados.orEmpty()
            val exato = personagens.firstOrNull {
                it.nome?.trim().equals(termo, ignoreCase = true)
            }
            if (exato != null) return exato

            if (primeiroResultado == null) primeiroResultado = personagens.firstOrNull()
            if (primeiroSobrenome == null) {
                primeiroSobrenome = personagens.firstOrNull {
                    it.nome?.trim()?.endsWith(" $termo", ignoreCase = true) == true
                }
            }
            if (primeiroPrefixo == null) {
                primeiroPrefixo = personagens.firstOrNull {
                    it.nome?.trim()?.startsWith(termo, ignoreCase = true) == true
                }
            }

            totalPaginas = resposta.paginacao?.totalPaginas ?: 1
            pagina++
        } while (pagina <= totalPaginas)

        return primeiroSobrenome ?: primeiroPrefixo ?: primeiroResultado
    }
}
