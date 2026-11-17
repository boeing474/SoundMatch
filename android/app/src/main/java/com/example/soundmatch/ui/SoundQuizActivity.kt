package com.example.soundmatch.ui

import android.content.Intent
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivitySoundQuizBinding
import com.example.soundmatch.logic.SoundMatchEngine
import com.example.soundmatch.model.DeezerArtist
import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.Popularity
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.UserProfile
import com.example.soundmatch.network.ImageLoader
import com.example.soundmatch.network.networkOrNull
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class SoundQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoundQuizBinding
    private lateinit var profile: UserProfile

    // Com artistas parecidos (vindos do Deezer), o quiz é o da descoberta.
    // Sem eles fica o quiz reserva: clima + outros estilos, e a música sai do catálogo fixo.
    private val hasRelatedArtists: Boolean get() = profile.relatedArtists.isNotEmpty()

    // As perguntas de escolha única: todas precisam de resposta para liberar o botão.
    private val questionGroups: List<RadioGroup> by lazy {
        if (hasRelatedArtists) listOf(binding.rgEnergy, binding.rgPopularity) else listOf(binding.rgEnergy)
    }

    // Cada chip da pergunta dos parecidos e o artista que ele representa.
    private val artistChips = mutableMapOf<Chip, DeezerArtist>()

    private val styleChecks by lazy {
        mapOf(
            binding.cbPop to Genre.POP,
            binding.cbRock to Genre.ROCK,
            binding.cbHiphop to Genre.HIPHOP,
            binding.cbElectronic to Genre.ELECTRONIC,
            binding.cbBrazilian to Genre.BRAZILIAN
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profile = IntentCompat.getSerializableExtra(intent, EXTRA_PROFILE, UserProfile::class.java)
            ?: run { finish(); return }

        showQuizMode()
        if (hasRelatedArtists) showKnownQuestion()
        Energy.entries.forEach { addOption(binding.rgEnergy, it.label, it.hint) }
        Popularity.entries.forEach { addOption(binding.rgPopularity, it.label, it.hint) }

        questionGroups.forEach { group ->
            group.setOnCheckedChangeListener { _, _ -> updateProgress() }
        }
        binding.btnSeeResult.setOnClickListener { showResult() }
        updateProgress()
    }

    // Mostra só os cards do modo em que o quiz está e ajusta os textos que mudam.
    private fun showQuizMode() {
        binding.cardKnown.isVisible = hasRelatedArtists
        binding.cardPopularity.isVisible = hasRelatedArtists
        binding.cardStyles.isVisible = !hasRelatedArtists
        binding.tvRiskHint.setText(if (hasRelatedArtists) R.string.risk_hint else R.string.risk_hint_reserve)
        binding.tvQuizHint.setText(
            if (hasRelatedArtists) R.string.quiz_hint_discovery else R.string.quiz_hint_reserve
        )
        binding.progressQuiz.max = questionGroups.size
    }

    // "Desses parecidos com BTS, quais você já conhece?": um chip com foto para cada artista.
    private fun showKnownQuestion() {
        binding.tvKnownTitle.text = getString(R.string.known_title, profile.favoriteArtist)
        profile.relatedArtists.forEach { artist ->
            val chip = layoutInflater.inflate(R.layout.item_artist_chip, binding.chipsKnown, false) as Chip
            chip.text = artist.name
            chip.setOnCheckedChangeListener { _, _ -> updateKnownCount() }
            binding.chipsKnown.addView(chip)
            artistChips[chip] = artist
            artist.picture?.let { loadPhoto(chip, it) }
        }
        updateKnownCount()
    }

    // A foto vem da internet e é recortada em círculo. Se o download falhar, o chip fica com o vinil.
    private fun loadPhoto(chip: Chip, url: String) {
        lifecycleScope.launch {
            val photo = networkOrNull { ImageLoader.load(url) } ?: return@launch
            chip.chipIcon = RoundedBitmapDrawableFactory.create(resources, photo).apply { isCircular = true }
        }
    }

    // "2 conhecidos · 8 na disputa", logo abaixo dos chips.
    private fun updateKnownCount() {
        val known = artistChips.keys.count { it.isChecked }
        val left = artistChips.size - known
        binding.tvKnownCount.text = when {
            known == 0 -> getString(R.string.known_none)
            left == 0 -> getString(R.string.known_all)
            else -> resources.getQuantityString(R.plurals.known_count, known, known, left)
        }
    }

    // Cada opção aparece com o nome em negrito e a explicação menor embaixo.
    private fun addOption(group: RadioGroup, label: String, hint: String) {
        val muted = ContextCompat.getColor(this, R.color.text_secondary)
        val option = layoutInflater.inflate(R.layout.item_quiz_option, group, false) as RadioButton
        option.id = View.generateViewId()
        option.text = buildSpannedString {
            bold { append(label) }
            append("\n")
            inSpans(RelativeSizeSpan(0.85f), ForegroundColorSpan(muted)) { append(hint) }
        }
        group.addView(option)
    }

    private fun updateProgress() {
        val answered = questionGroups.count { it.checkedRadioButtonId != View.NO_ID }
        val allAnswered = answered == questionGroups.size

        binding.progressQuiz.progress = answered
        binding.tvProgress.text = getString(R.string.quiz_progress, answered, questionGroups.size)
        binding.btnSeeResult.isEnabled = allAnswered
        binding.tvQuizHint.visibility = if (allAnswered) View.GONE else View.VISIBLE
    }

    private fun showResult() {
        val answers = QuizAnswers(
            firstPick = null,
            firstPickSong = null,
            energy = Energy.entries[selectedIndex(binding.rgEnergy)],
            likedGenres = styleChecks.filterKeys { it.isChecked }.values.toList(),
            surprise = binding.switchRisk.isChecked,
            knownArtists = artistChips.filterKeys { it.isChecked }.values.toList(),
            popularity = Popularity.entries.getOrNull(selectedIndex(binding.rgPopularity))
        )
        val result = SoundMatchEngine.recommend(profile, answers)

        // No quiz reserva, quem quer se arriscar precisa marcar um estilo além do de sempre.
        if (!hasRelatedArtists && answers.surprise && !result.surprise) {
            val message = getString(R.string.toast_surprise_needs_style, result.genre.title)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_RESULT, result)
        startActivity(intent)
    }

    // A posição da opção marcada, que é a mesma posição no enum (Energy ou Popularity).
    // Sem opção marcada devolve -1.
    private fun selectedIndex(group: RadioGroup): Int =
        group.indexOfChild(group.findViewById<View>(group.checkedRadioButtonId))

    companion object {
        const val EXTRA_PROFILE = "extra_profile"
    }
}
