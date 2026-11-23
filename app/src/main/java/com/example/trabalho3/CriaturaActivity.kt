package com.example.trabalho3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CriaturaActivity : AppCompatActivity() {

    private lateinit var campoNome: EditText
    private lateinit var botaoBuscar: Button
    private lateinit var barraProgresso: ProgressBar
    private lateinit var textoResultado: TextView
    private lateinit var imagemCriatura: ImageView
    private lateinit var avisoImagem: TextView

    override fun onCreate(estadoSalvo: Bundle?) {
        super.onCreate(estadoSalvo)
        enableEdgeToEdge()
        setContentView(R.layout.activity_criatura)
        ajustarBarrasDoSistema(findViewById(R.id.raiz_criatura))

        campoNome = findViewById(R.id.campo_nome_criatura)
        botaoBuscar = findViewById(R.id.botao_buscar_criatura) // ajuste o ID conforme seu XML
        barraProgresso = findViewById(R.id.progresso_criatura)
        textoResultado = findViewById(R.id.resultado_criatura)
        imagemCriatura = findViewById(R.id.imagem_criatura)
        avisoImagem = findViewById(R.id.aviso_imagem)

        botaoBuscar.setOnClickListener { buscarCriatura() }
    }

    fun buscarCriatura(view: View? = null) {
        limparEstadoAnterior()

        val nome = validarNome(campoNome) ?: return

        lifecycleScope.launch {
            definirCarregando(true)
            try {
                val criatura = encontrarCriatura(nome)
                if (criatura == null) {
                    textoResultado.setText(R.string.criatura_nao_encontrada)
                } else {
                    textoResultado.text = criatura.nome
                    exibirImagem(criatura.enderecoImagem, criatura.nome)
                }
            } catch (erro: IOException) {
                textoResultado.setText(R.string.erro_conexao)
            } catch (erro: HttpException) {
                if (erro.code() == 404) {
                    textoResultado.setText(R.string.criatura_nao_encontrada)
                } else {
                    textoResultado.setText(R.string.erro_servidor)
                }
            } catch (erro: JsonParseException) {
                textoResultado.setText(R.string.erro_resposta)
            } finally {
                definirCarregando(false)
            }
        }
    }

    private fun limparEstadoAnterior() {
        textoResultado.text = ""
        avisoImagem.visibility = View.GONE
        imagemCriatura.visibility = View.GONE
        imagemCriatura.setImageDrawable(null)
    }

    private fun definirCarregando(carregando: Boolean) {
        botaoBuscar.isEnabled = !carregando
        barraProgresso.visibility = if (carregando) View.VISIBLE else View.GONE
    }

    private suspend fun exibirImagem(endereco: String?, nomeCriatura: String?) {
        val imagem = baixarImagem(endereco)
        if (imagem == null) {
            imagemCriatura.setImageResource(R.drawable.imagem_indisponivel)
            imagemCriatura.contentDescription = null
            avisoImagem.visibility = View.VISIBLE
        } else {
            imagemCriatura.setImageBitmap(imagem)
            imagemCriatura.contentDescription = nomeCriatura
        }
        imagemCriatura.visibility = View.VISIBLE
    }

    private suspend fun encontrarCriatura(nome: String): Criatura? {
        var pagina = 1
        var totalPaginas = 1

        do {
            val resposta = apiHogwarts.buscarCriaturas(nome, pagina)
            val criatura = resposta.dados.orEmpty().firstOrNull {
                it.nome?.trim().equals(nome.trim(), ignoreCase = true)
            }
            if (criatura != null) return criatura

            pagina++
            totalPaginas = resposta.paginacao?.totalPaginas ?: 1
        } while (pagina <= totalPaginas)

        return null
    }

    private suspend fun baixarImagem(endereco: String?): Bitmap? {
        if (endereco.isNullOrBlank()) return null

        return withContext(Dispatchers.IO) {
            var conexao: HttpURLConnection? = null
            try {
                conexao = (URL(endereco).openConnection() as? HttpURLConnection)?.apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    instanceFollowRedirects = true
                }

                if (conexao?.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }

                conexao.inputStream.use { entrada ->
                    BitmapFactory.decodeStream(entrada)
                }
            } catch (erro: Exception) {
                null
            } finally {
                conexao?.disconnect()
            }
        }
    }
}