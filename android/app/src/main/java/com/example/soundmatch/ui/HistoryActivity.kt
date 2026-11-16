package com.example.soundmatch.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.soundmatch.R
import com.example.soundmatch.adapter.HistoryAdapter
import com.example.soundmatch.databinding.ActivityHistoryBinding
import com.example.soundmatch.databinding.DialogEditNoteBinding
import com.example.soundmatch.model.NoteUpdateRequest
import com.example.soundmatch.model.RecommendationResponse
import com.example.soundmatch.network.RetrofitClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private val adapter = HistoryAdapter(
        onEditNote = { item -> showEditNoteDialog(item) },
        onDelete = { item -> confirmDelete(item) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerHistory.adapter = adapter
    }

    // Recarrega toda vez que a tela aparece.
    override fun onStart() {
        super.onStart()
        loadHistory()
    }

    // GET /recommendations
    private fun loadHistory() {
        lifecycleScope.launch {
            binding.progressHistory.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
            try {
                val items = RetrofitClient.api.listRecommendations()
                adapter.submitList(items)
                binding.tvHistoryCount.text =
                    resources.getQuantityString(R.plurals.history_count, items.size, items.size)
                if (items.isEmpty()) showMessage(R.string.history_empty)
            } catch (e: IOException) {
                showLoadError()
            } catch (e: HttpException) {
                showLoadError()
            } finally {
                binding.progressHistory.visibility = View.GONE
            }
        }
    }

    private fun showEditNoteDialog(item: RecommendationResponse) {
        val dialogBinding = DialogEditNoteBinding.inflate(layoutInflater)
        dialogBinding.etDialogNote.setText(item.personalNote)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_edit_title)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_save) { _, _ ->
                val newNote = dialogBinding.etDialogNote.text?.toString()?.trim().orEmpty()
                updateNote(item.id, newNote)
            }
            .show()
    }

    // PUT /recommendations/{id}
    private fun updateNote(id: Int, note: String) {
        lifecycleScope.launch {
            try {
                RetrofitClient.api.updateNote(id, NoteUpdateRequest(note))
                Toast.makeText(this@HistoryActivity, R.string.toast_updated, Toast.LENGTH_SHORT).show()
                loadHistory()
            } catch (e: IOException) {
                showConnectionError()
            } catch (e: HttpException) {
                showConnectionError()
            }
        }
    }

    private fun confirmDelete(item: RecommendationResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, item.user.name))
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.btn_delete) { _, _ -> deleteRecommendation(item.id) }
            .show()
    }

    // DELETE /recommendations/{id}
    private fun deleteRecommendation(id: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.deleteRecommendation(id)
                if (response.isSuccessful) {
                    Toast.makeText(this@HistoryActivity, R.string.toast_deleted, Toast.LENGTH_SHORT).show()
                    loadHistory()
                } else {
                    showConnectionError()
                }
            } catch (e: IOException) {
                showConnectionError()
            }
        }
    }

    private fun showMessage(@StringRes messageRes: Int) {
        binding.tvEmpty.setText(messageRes)
        binding.tvEmpty.visibility = View.VISIBLE
    }

    private fun showLoadError() {
        adapter.submitList(emptyList())
        binding.tvHistoryCount.text = ""
        showMessage(R.string.history_load_error)
    }

    private fun showConnectionError() {
        Toast.makeText(this, R.string.toast_connection_error, Toast.LENGTH_LONG).show()
    }
}
