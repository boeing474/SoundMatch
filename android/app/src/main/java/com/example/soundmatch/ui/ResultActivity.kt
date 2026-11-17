package com.example.soundmatch.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivityResultBinding
import com.example.soundmatch.logic.TrackPicker
import com.example.soundmatch.model.RecommendationRequest
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.network.ImageLoader
import com.example.soundmatch.network.RetrofitClient
import com.example.soundmatch.network.networkOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var result: SoundResult
    private var isSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        result = IntentCompat.getSerializableExtra(intent, EXTRA_RESULT, SoundResult::class.java)
            ?: run { finish(); return }

        showResult()
        loadAlbumCover()
        binding.btnLike.setOnClickListener { saveRecommendation(liked = true) }
        binding.btnDislike.setOnClickListener { saveRecommendation(liked = false) }
        binding.btnYoutube.setOnClickListener { openYoutube() }
        binding.btnSpotify.setOnClickListener { openSpotify() }
        binding.btnShare.setOnClickListener { shareProfile() }
    }

    // O título e a linha de detalhe do card. Também vão para a API e para o texto de compartilhar.
    // Descoberta: "Parecido com BTS" e "Dançante · Hit do momento".
    // Quiz reserva: o estilo vencedor e os outros estilos marcados.
    private val cardTitle: String
        get() = if (result.fromSimilarArtists) {
            getString(R.string.result_similar_to, result.profile.favoriteArtist)
        } else {
            result.genre?.title.orEmpty()
        }

    private val cardDetail: String
        get() = if (result.fromSimilarArtists) {
            listOfNotNull(result.song.energy.label, result.popularity?.label).joinToString(" · ")
        } else {
            result.likedGenresLabel
        }

    private fun showResult() {
        // Sem estilo conhecido (o iTunes não soube dizer), o card fica na cor principal do app.
        val genreColor = ContextCompat.getColor(this, result.genre?.colorRes ?: R.color.neon_magenta)

        with(binding) {
            cardCover.strokeColor = genreColor
            cardResult.strokeColor = genreColor
            layoutGenreHeader.setBackgroundColor(genreColor)
            tvMatch.setTextColor(genreColor)

            tvMatch.text = result.match.toString()
            tvSongTitle.text = result.song.title
            tvSongArtist.text = result.song.artist
            tvReason.text = result.reason

            if (result.fromSimilarArtists) {
                // "PARECIDO COM / Wesley Safadão", o % do clima e o que a pessoa pediu.
                tvHeaderLabel.setText(R.string.result_header_similar)
                tvGenre.text = result.profile.favoriteArtist
                tvMatchHint.setText(R.string.result_match_hint_mood)
                tvHighlight.text = cardDetail
                tvLikedGenres.visibility = View.GONE
            } else {
                tvGenre.text = cardTitle
                tvHighlight.text = result.genre?.highlight
                tvLikedGenres.text = cardDetail.ifEmpty { getString(R.string.result_no_styles) }
            }

            // Quem se arriscou no quiz reserva saiu do estilo mais votado: o % não faz sentido, entra uma frase.
            if (result.surprise) {
                layoutMatch.visibility = View.GONE
                tvSurpriseTitle.visibility = View.VISIBLE
                tvSurpriseTitle.setTextColor(genreColor)
                tvMatchHint.setText(R.string.result_surprise_hint)
            }
        }
    }

    // A música do Deezer já vem com a capa. A do catálogo fixo é procurada no iTunes.
    // Se falhar (sem internet, por exemplo), a tela continua com o vinil: a capa é só um complemento.
    private fun loadAlbumCover() {
        lifecycleScope.launch {
            val song = result.song
            val (album, coverUrl) = if (song.coverUrl != null) {
                song.album to song.coverUrl
            } else {
                val track = networkOrNull {
                    RetrofitClient.itunes.search("${song.title} ${song.artist}", limit = COVER_SEARCH_LIMIT).results
                }?.let { TrackPicker.bestMatch(it, song.artist) }
                track?.collectionName to track?.artworkUrl100?.let { ImageLoader.bigCover(it) }
            }

            album?.let {
                binding.tvAlbum.text = getString(R.string.result_album, it)
                binding.tvAlbum.visibility = View.VISIBLE
            }
            coverUrl?.let { url ->
                networkOrNull { ImageLoader.load(url) }?.let { binding.imgAlbumCover.setImageBitmap(it) }
            }
        }
    }

    // POST /recommendations: Curti e Não curti salvam a música, cada um com a sua avaliação.
    private fun saveRecommendation(liked: Boolean) {
        lifecycleScope.launch {
            setSaving(true)
            try {
                RetrofitClient.api.createRecommendation(RecommendationRequest.from(result, liked, cardTitle, cardDetail))
                isSaved = true   // trava os botões para não salvar a mesma música duas vezes
                binding.tvSaved.setText(if (liked) R.string.result_saved_liked else R.string.result_saved_disliked)
                binding.tvSaved.visibility = View.VISIBLE
            } catch (e: IOException) {
                showConnectionError()
            } catch (e: HttpException) {
                showConnectionError()
            } finally {
                setSaving(false)
            }
        }
    }

    private fun setSaving(saving: Boolean) {
        binding.progressSave.visibility = if (saving) View.VISIBLE else View.GONE
        binding.btnLike.isEnabled = !saving && !isSaved
        binding.btnDislike.isEnabled = !saving && !isSaved
    }

    private fun showConnectionError() {
        Toast.makeText(this, R.string.toast_connection_error, Toast.LENGTH_LONG).show()
    }

    // Intent implícita: o Android decide se abre no app do YouTube ou no navegador.
    private fun openYoutube() {
        val url = YOUTUBE_SEARCH_URL + Uri.encode(result.song.displayName)
        startSafely(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    // Tenta abrir a busca direto no app do Spotify; se ele não estiver instalado, abre no navegador.
    private fun openSpotify() {
        val query = Uri.encode(result.song.displayName)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SPOTIFY_APP_URI + query)))
        } catch (e: ActivityNotFoundException) {
            startSafely(Intent(Intent.ACTION_VIEW, Uri.parse(SPOTIFY_WEB_URL + query)))
        }
    }

    // Intent implícita: o usuário escolhe por onde compartilhar (WhatsApp, e-mail...).
    private fun shareProfile() {
        val name = result.profile.name
        val song = result.song.displayName
        val text = when {
            result.fromSimilarArtists ->
                getString(R.string.share_text_similar, name, result.profile.favoriteArtist, song, result.match)
            result.surprise -> getString(R.string.share_text_surprise, name, cardTitle, song)
            else -> getString(R.string.share_text, name, cardTitle, result.match, song)
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startSafely(Intent.createChooser(sendIntent, getString(R.string.share_chooser)))
    }

    // Se nenhum app do celular souber abrir a Intent, mostra um aviso em vez de fechar o app.
    private fun startSafely(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_RESULT = "extra_result"
        private const val COVER_SEARCH_LIMIT = 10
        private const val YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query="
        private const val SPOTIFY_APP_URI = "spotify:search:"
        private const val SPOTIFY_WEB_URL = "https://open.spotify.com/search/"
    }
}
