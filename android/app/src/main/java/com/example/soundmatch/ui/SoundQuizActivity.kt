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
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivitySoundQuizBinding
import com.example.soundmatch.logic.SoundMatchEngine
import com.example.soundmatch.model.Energy
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.UserProfile

class SoundQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoundQuizBinding
    private lateinit var profile: UserProfile

    private val questionGroups: List<RadioGroup> by lazy {
        listOf(binding.rgFirstPick, binding.rgEnergy)
    }

    private val styleChecks by lazy {
        mapOf(
            binding.cbPop to Genre.POP,
            binding.cbRock to Genre.ROCK,
            binding.cbHiphop to Genre.HIPHOP,
            binding.cbElectronic to Genre.ELECTRONIC,
            binding.cbBrazilian to Genre.BRAZILIAN
        )
    }

    // Sem músicas do artista (não encontrado ou sem internet), a 1ª pergunta usa a reserva.
    private val usesArtistSongs: Boolean get() = profile.artistSongs.isNotEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profile = IntentCompat.getSerializableExtra(intent, EXTRA_PROFILE, UserProfile::class.java)
            ?: run { finish(); return }

        showFirstQuestion()
        showEnergyQuestion()
        questionGroups.forEach { group ->
            group.setOnCheckedChangeListener { _, _ -> updateProgress() }
        }
        binding.btnSeeResult.setOnClickListener { showResult() }
        updateProgress()
    }

    // "Qual dessas de BTS você colocaria pra tocar numa festa com amigos?"
    private fun showFirstQuestion() {
        val context = profile.situation.context
        if (usesArtistSongs) {
            binding.tvQuestion1.text = getString(R.string.q1_title_artist, profile.favoriteArtist, context)
            profile.artistSongs.forEach { addOption(binding.rgFirstPick, it.title) }
        } else {
            binding.tvQuestion1.text = getString(R.string.q1_title, context)
            FALLBACK_OPTIONS.forEach { addOption(binding.rgFirstPick, getString(it)) }
        }
    }

    // Cada energia aparece com o nome em negrito e a explicação menor embaixo.
    private fun showEnergyQuestion() {
        val muted = ContextCompat.getColor(this, R.color.text_secondary)
        Energy.entries.forEach { energy ->
            val text = buildSpannedString {
                bold { append(energy.label) }
                append("\n")
                inSpans(RelativeSizeSpan(0.85f), ForegroundColorSpan(muted)) { append(energy.hint) }
            }
            addOption(binding.rgEnergy, text)
        }
    }

    private fun addOption(group: RadioGroup, text: CharSequence) {
        val option = layoutInflater.inflate(R.layout.item_quiz_option, group, false) as RadioButton
        option.id = View.generateViewId()
        option.text = text
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
        val firstIndex = selectedIndex(binding.rgFirstPick)
        val artistSong = profile.artistSongs.getOrNull(firstIndex)
        val answers = QuizAnswers(
            firstPick = if (usesArtistSongs) artistSong?.genre else Genre.entries[firstIndex],
            firstPickSong = artistSong?.title,
            energy = Energy.entries[selectedIndex(binding.rgEnergy)],
            likedGenres = styleChecks.filterKeys { it.isChecked }.values.toList(),
            surprise = binding.switchSurprise.isChecked
        )
        val result = SoundMatchEngine.recommend(profile, answers)

        // Pediu surpresa, mas não marcou nenhum estilo além do de sempre: não dá para surpreender.
        if (answers.surprise && !result.surprise) {
            val message = getString(R.string.toast_surprise_needs_style, result.genre.title)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_RESULT, result)
        startActivity(intent)
    }

    // A posição da opção marcada. Na energia, é a posição no enum Energy;
    // na pergunta reserva, a posição no enum Genre.
    private fun selectedIndex(group: RadioGroup): Int =
        group.indexOfChild(group.findViewById<View>(group.checkedRadioButtonId))

    companion object {
        const val EXTRA_PROFILE = "extra_profile"

        // A opção N é do gênero N do enum Genre.
        private val FALLBACK_OPTIONS = listOf(
            R.string.q1_opt_0, R.string.q1_opt_1, R.string.q1_opt_2, R.string.q1_opt_3, R.string.q1_opt_4
        )
    }
}
