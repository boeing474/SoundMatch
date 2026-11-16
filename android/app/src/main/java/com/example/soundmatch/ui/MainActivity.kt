package com.example.soundmatch.ui

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val animators = mutableListOf<Animator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
    }

    // Animações só rodam com a tela visível, para não gastar bateria.
    override fun onStart() {
        super.onStart()
        startAnimations()
    }

    override fun onStop() {
        stopAnimations()
        super.onStop()
    }

    private fun setupNavigation() {
        binding.btnDiscover.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun startAnimations() {
        val spin = ObjectAnimator.ofFloat(binding.imgVinyl, View.ROTATION, 0f, 360f).apply {
            duration = SPIN_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }

        // Brilho do título alternando entre magenta e ciano, como um letreiro neon.
        val magenta = ContextCompat.getColor(this, R.color.neon_magenta)
        val cyan = ContextCompat.getColor(this, R.color.neon_cyan)
        val glowRadius = GLOW_RADIUS_DP * resources.displayMetrics.density
        val pulse = ValueAnimator.ofObject(ArgbEvaluator(), magenta, cyan).apply {
            duration = PULSE_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animator ->
                binding.tvTitle.setShadowLayer(glowRadius, 0f, 0f, animator.animatedValue as Int)
            }
        }

        animators += listOf(spin, pulse)
        animators.forEach { it.start() }
    }

    private fun stopAnimations() {
        animators.forEach { it.cancel() }
        animators.clear()
    }

    private companion object {
        const val SPIN_DURATION_MS = 3_000L
        const val PULSE_DURATION_MS = 1_600L
        const val GLOW_RADIUS_DP = 10f
    }
}
