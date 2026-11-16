package com.example.soundmatch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ItemRecommendationBinding
import com.example.soundmatch.model.Genre
import com.example.soundmatch.model.RecommendationResponse
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Os cliques de avaliar e excluir só são repassados: quem chama a API é a HistoryActivity.
class HistoryAdapter(
    private val onToggleRating: (RecommendationResponse) -> Unit,
    private val onDelete: (RecommendationResponse) -> Unit
) : ListAdapter<RecommendationResponse, HistoryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemRecommendationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val colorRes = Genre.fromTitle(item.archetype)?.colorRes ?: R.color.neon_cyan
        val color = ContextCompat.getColor(context, colorRes)

        with(holder.binding) {
            viewStripe.setBackgroundColor(color)
            tvUserName.text = item.user.name
            tvDate.text = formatApiDate(item.createdAt)
            tvSong.text = context.getString(R.string.item_song, item.recommendedSong, item.songArtist)
            tvGenre.text = item.archetype
            tvGenre.setTextColor(color)
            tvMatch.text = context.getString(R.string.item_match, item.score)
            tvMatch.setTextColor(color)
            tvRating.setText(if (item.liked) R.string.item_liked else R.string.item_disliked)
            tvRating.setTextColor(ContextCompat.getColor(context, if (item.liked) R.color.neon_cyan else R.color.neon_magenta))
            btnToggleRating.setText(if (item.liked) R.string.btn_mark_disliked else R.string.btn_mark_liked)

            btnToggleRating.setOnClickListener { onToggleRating(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    // Compara a lista antiga com a nova para o RecyclerView atualizar só os cards que mudaram.
    object DiffCallback : DiffUtil.ItemCallback<RecommendationResponse>() {
        override fun areItemsTheSame(old: RecommendationResponse, new: RecommendationResponse) = old.id == new.id
        override fun areContentsTheSame(old: RecommendationResponse, new: RecommendationResponse) = old == new
    }

    private companion object {
        // A API grava em UTC ("2026-11-15T22:42:10.123456"); aqui vira o horário local.
        val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val DISPLAY_DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

        fun formatApiDate(apiDate: String): String = try {
            API_DATE_FORMAT.parse(apiDate.take(19))?.let { DISPLAY_DATE_FORMAT.format(it) } ?: apiDate
        } catch (e: ParseException) {
            apiDate
        }
    }
}
