package com.example.soundmatch.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
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
        listOf(binding.rgSongPick, binding.rgArtistPick, binding.rgAlbumPick)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profile = IntentCompat.getSerializableExtra(intent, EXTRA_PROFILE, UserProfile::class.java)
            ?: run { finish(); return }

        showPersonalizedQuestions()
        questionGroups.forEach { group ->
            group.setOnCheckedChangeListener { _, _ -> updateProgress() }
        }
        binding.btnSeeResult.setOnClickListener { showResult() }
        updateProgress()
    }

    // As perguntas usam o que a pessoa contou no perfil.
    private fun showPersonalizedQuestions() {
        binding.tvQuestion1.text = getString(R.string.q1_title, profile.situation.context)
        binding.tvQuestion2.text = getString(R.string.q2_title, profile.favoriteArtist)
        binding.tvQuestion3.text = getString(R.string.q3_title)
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
        // Provisório até a etapa 4 (tela nova do quiz): energia fixa e sem surpresa.
        val answers = QuizAnswers(
            firstPick = selectedGenre(binding.rgSongPick),
            firstPickSong = null,
            energy = Energy.DANCE,
            likedGenres = styleChecks.filterKeys { it.isChecked }.values.toList(),
            surprise = false
        )
        val result = SoundMatchEngine.recommend(profile, answers)

        val intent = Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_RESULT, result)
        startActivity(intent)
    }

    // A posição da opção marcada (0 a 4) é a posição do gênero no enum Genre.
    private fun selectedGenre(group: RadioGroup): Genre {
        val index = group.indexOfChild(group.findViewById<View>(group.checkedRadioButtonId))
        return Genre.entries[index]
    }

    companion object {
        const val EXTRA_PROFILE = "extra_profile"
    }
}
