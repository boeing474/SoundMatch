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
import com.example.soundmatch.model.QuizAnswers
import com.example.soundmatch.model.Subgenre
import com.example.soundmatch.model.UserProfile

class SoundQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoundQuizBinding
    private lateinit var profile: UserProfile

    private val questionGroups: List<RadioGroup> by lazy {
        listOf(binding.rgPlaylist, binding.rgConcert, binding.rgLongTrack)
    }

    private val subgenreChecks by lazy {
        mapOf(
            binding.cbBlues to Subgenre.BLUES_ROCK,
            binding.cbHard to Subgenre.HARD_ROCK,
            binding.cbFunk to Subgenre.FUNK_ALTERNATIVE,
            binding.cbProg to Subgenre.PROG_ROCK
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profile = IntentCompat.getSerializableExtra(intent, EXTRA_PROFILE, UserProfile::class.java)
            ?: run { finish(); return }

        questionGroups.forEach { group ->
            group.setOnCheckedChangeListener { _, _ -> updateProgress() }
        }
        binding.btnSeeResult.setOnClickListener { showResult() }
        updateProgress()
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
            playlist = selectedIndex(binding.rgPlaylist),
            concert = selectedIndex(binding.rgConcert),
            longTrack = selectedIndex(binding.rgLongTrack),
            subgenres = subgenreChecks.filterKeys { it.isChecked }.values.toList()
        )
        val result = SoundMatchEngine.recommend(profile, answers)

        val intent = Intent(this, ResultActivity::class.java)
            .putExtra(ResultActivity.EXTRA_RESULT, result)
        startActivity(intent)
    }

    // A posição da opção marcada (0, 1, 2...) é o peso dela no score.
    private fun selectedIndex(group: RadioGroup): Int =
        group.indexOfChild(group.findViewById<View>(group.checkedRadioButtonId))

    companion object {
        const val EXTRA_PROFILE = "extra_profile"
    }
}
