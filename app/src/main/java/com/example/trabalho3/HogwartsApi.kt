package com.example.trabalho3

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class Personagem(
    @SerializedName("name") val nome: String?,
    @SerializedName("species") val especie: String?,
    @SerializedName("house") val casa: String?
)

data class Criatura(
    @SerializedName("name") val nome: String?,
    @SerializedName("image") val enderecoImagem: String?
)

data class Paginacao(
    @SerializedName("total_pages") val totalPaginas: Int
)

data class RespostaBusca<T>(
    @SerializedName("data") val dados: List<T>?,
    @SerializedName("meta") val paginacao: Paginacao?
)

data class RespostaPersonagem(
    @SerializedName("data") val personagem: Personagem?
)

interface HogwartsApi {
    @GET("characters")
    suspend fun buscarPersonagens(
        @Query("search") nome: String,
        @Query("page") pagina: Int = 1,
        @Query("page_size") tamanhoPagina: Int = 100
    ): RespostaBusca<Personagem>

    @GET("characters/{slug}")
    suspend fun buscarPersonagemPorSlug(
        @Path("slug") slug: String
    ): RespostaPersonagem

    @GET("creatures")
    suspend fun buscarCriaturas(
        @Query("search") nome: String,
        @Query("page") pagina: Int,
        @Query("page_size") tamanhoPagina: Int = 100
    ): RespostaBusca<Criatura>
}

val apiHogwarts: HogwartsApi = Retrofit.Builder()
    .baseUrl("https://hogwarts-api.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(HogwartsApi::class.java)
