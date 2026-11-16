package com.example.soundmatch.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.example.soundmatch.model.RecommendationRequest
import com.example.soundmatch.model.SoundResult
import com.example.soundmatch.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.URL

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
        binding.btnSave.setOnClickListener { saveRecommendation() }
        binding.btnYoutube.setOnClickListener { openYoutube() }
        binding.btnSpotify.setOnClickListener { openSpotify() }
        binding.btnShare.setOnClickListener { shareProfile() }
    }

    private fun showResult() {
        val archetypeColor = ContextCompat.getColor(this, result.archetype.colorRes)

        with(binding) {
            cardResult.strokeColor = archetypeColor
            layoutArchetypeHeader.setBackgroundColor(archetypeColor)
            tvScore.setTextColor(archetypeColor)

            tvArchetype.text = result.archetype.title
            tvScore.text = result.score.toString()
            tvSongTitle.text = result.song.title
            tvSongArtist.text = result.song.artist
            tvHighlight.text = result.archetype.highlight
            tvReason.text = result.reason
            tvSubgenres.text = result.subgenresLabel.ifEmpty { getString(R.string.result_no_subgenres) }
        }
    }

    // Busca o álbum no iTunes e baixa a capa. Se falhar (sem internet, por exemplo),
    // a tela continua com o vinil, porque a capa é só um complemento.
    private fun loadAlbumCover() {
        lifecycleScope.launch {
            try {
                val track = RetrofitClient.itunes
                    .searchSong("${result.song.title} ${result.song.artist}")
                    .results.firstOrNull() ?: return@launch

                track.collectionName?.let {
                    binding.tvAlbum.text = getString(R.string.result_album, it)
                    binding.tvAlbum.visibility = View.VISIBLE
                }

                // A API devolve a capa em 100x100; trocando no link pedimos uma maior.
                val coverUrl = track.artworkUrl100?.replace("100x100", "600x600") ?: return@launch
                downloadImage(coverUrl)?.let { binding.imgAlbumCover.setImageBitmap(it) }
            } catch (e: IOException) {
                // sem conexão: mantém o vinil
            } catch (e: HttpException) {
                // o iTunes respondeu com erro: mantém o vinil
            }
        }
    }

    // Download em Dispatchers.IO, porque o Android não deixa usar a rede na thread da tela.
    private suspend fun downloadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
        URL(url).openStream().use { BitmapFactory.decodeStream(it) }
    }

    // POST /recommendations na nossa API
    private fun saveRecommendation() {
        val note = binding.etNote.text?.toString()?.trim().orEmpty()
        val request = RecommendationRequest.from(result, note)

        lifecycleScope.launch {
            setSaving(true)
            try {
                RetrofitClient.api.createRecommendation(request)
                isSaved = true   // trava o botão para não salvar a mesma música duas vezes
                binding.etNote.isEnabled = false
                Toast.makeText(this@ResultActivity, R.string.toast_saved, Toast.LENGTH_SHORT).show()
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
        binding.btnSave.isEnabled = !saving && !isSaved
        binding.btnSave.text = when {
            saving -> ""
            isSaved -> getString(R.string.btn_saved)
            else -> getString(R.string.btn_save)
        }
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
            result.archetype.title,
            result.score,
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
        private const val YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query="
        private const val SPOTIFY_APP_URI = "spotify:search:"
        private const val SPOTIFY_WEB_URL = "https://open.spotify.com/search/"
    }
}
