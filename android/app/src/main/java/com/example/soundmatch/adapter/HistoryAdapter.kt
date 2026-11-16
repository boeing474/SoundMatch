package com.example.soundmatch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmatch.R
import com.example.soundmatch.databinding.ItemRecommendationBinding
import com.example.soundmatch.model.Archetype
import com.example.soundmatch.model.RecommendationResponse
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Liga a lista de recomendações (vinda da API) aos cards do RecyclerView.
 * Os cliques de "Editar Nota" e "Excluir" são repassados para a HistoryActivity.
 */
class HistoryAdapter(
    private val onEditNote: (RecommendationResponse) -> Unit,
    private val onDelete: (RecommendationResponse) -> Unit
) : ListAdapter<RecommendationResponse, HistoryAdapter.ViewHolder>(DiffCallback) {

    /** Guarda as Views de um card para serem reaproveitadas na rolagem. */
    class ViewHolder(val binding: ItemRecommendationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val colorRes = Archetype.fromTitle(item.archetype)?.colorRes ?: R.color.neon_cyan
        val color = ContextCompat.getColor(context, colorRes)

        with(holder.binding) {
            viewStripe.setBackgroundColor(color)
            tvUserName.text = item.user.name
            tvDate.text = formatApiDate(item.createdAt)
            tvSong.text = context.getString(R.string.item_song, item.recommendedSong, item.songArtist)
            tvArchetype.text = item.archetype
            tvArchetype.setTextColor(color)
            tvScore.text = context.getString(R.string.item_score, item.score)
            tvScore.setTextColor(color)
            tvNote.text = item.personalNote.ifBlank { context.getString(R.string.item_note_empty) }

            btnEditNote.setOnClickListener { onEditNote(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    /** Diz ao RecyclerView o que mudou entre a lista antiga e a nova (anima só o necessário). */
    private object DiffCallback : DiffUtil.ItemCallback<RecommendationResponse>() {
        override fun areItemsTheSame(old: RecommendationResponse, new: RecommendationResponse) = old.id == new.id
        override fun areContentsTheSame(old: RecommendationResponse, new: RecommendationResponse) = old == new
    }

    private companion object {
        // A API grava a data em UTC ("2026-11-15T22:42:10.123456"); mostramos no fuso do aparelho.
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
