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

    private fun showResult() {
        val genreColor = ContextCompat.getColor(this, result.genre.colorRes)

        with(binding) {
            cardCover.strokeColor = genreColor
            cardResult.strokeColor = genreColor
            layoutGenreHeader.setBackgroundColor(genreColor)
            tvMatch.setTextColor(genreColor)

            tvGenre.text = result.genre.title
            tvMatch.text = result.match.toString()
            tvSongTitle.text = result.song.title
            tvSongArtist.text = result.song.artist
            tvHighlight.text = result.genre.highlight
            tvReason.text = result.reason
            tvLikedGenres.text = result.likedGenresLabel.ifEmpty { getString(R.string.result_no_styles) }
        }
    }

    // Busca o álbum no iTunes e baixa a capa. Se falhar (sem internet, por exemplo),
    // a tela continua com o vinil, porque a capa é só um complemento.
    private fun loadAlbumCover() {
        lifecycleScope.launch {
            try {
                val results = RetrofitClient.itunes
                    .search("${result.song.title} ${result.song.artist}", limit = COVER_SEARCH_LIMIT)
                    .results
                val track = TrackPicker.bestMatch(results, result.song.artist) ?: return@launch

                track.collectionName?.let {
                    binding.tvAlbum.text = getString(R.string.result_album, it)
                    binding.tvAlbum.visibility = View.VISIBLE
                }

                val artwork = track.artworkUrl100 ?: return@launch
                ImageLoader.load(ImageLoader.bigCover(artwork))?.let { binding.imgAlbumCover.setImageBitmap(it) }
            } catch (e: IOException) {
                // sem conexão: mantém o vinil
            } catch (e: HttpException) {
                // o iTunes respondeu com erro: mantém o vinil
            }
        }
    }

    // POST /recommendations: Curti e Não curti salvam a música, cada um com a sua avaliação.
    private fun saveRecommendation(liked: Boolean) {
        lifecycleScope.launch {
            setSaving(true)
            try {
                RetrofitClient.api.createRecommendation(RecommendationRequest.from(result, liked))
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
        val text = getString(
            R.string.share_text,
            result.profile.name,
            result.genre.title,
            result.match,
            result.song.displayName
        )
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
