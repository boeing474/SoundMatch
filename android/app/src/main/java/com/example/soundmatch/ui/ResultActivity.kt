package com.example.soundmatch.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivityResultBinding
import com.example.soundmatch.model.SoundResult

/**
 * Mostra o perfil sonoro e a música recomendada, permite salvar na API
 * e abre o YouTube / compartilhamento (Intents implícitas).
 */
class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var result: SoundResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        result = IntentCompat.getSerializableExtra(intent, EXTRA_RESULT, SoundResult::class.java)
            ?: run { finish(); return }

        showResult()
        binding.btnYoutube.setOnClickListener { openYoutube() }
        binding.btnShare.setOnClickListener { shareProfile() }
    }

    private fun showResult() {
        val archetypeColor = ContextCompat.getColor(this, result.archetype.colorRes)

        with(binding) {
            // O card inteiro assume a cor do perfil sonoro.
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

    private fun openYoutube() {
        // REQUISITO: Intent Implícita (ACTION_VIEW): o Android escolhe o app do YouTube ou o navegador.
        val url = YOUTUBE_SEARCH_URL + Uri.encode(result.song.displayName)
        startSafely(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun shareProfile() {
        val text = getString(
            R.string.share_text,
            result.profile.name,
            result.archetype.title,
            result.score,
            result.song.displayName
        )
        // REQUISITO: Intent Implícita (ACTION_SEND): o usuário escolhe WhatsApp, e-mail etc.
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startSafely(Intent.createChooser(sendIntent, getString(R.string.share_chooser)))
    }

    /** Evita que o app feche se o aparelho não tiver nenhum app capaz de abrir a Intent. */
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
    }
}
