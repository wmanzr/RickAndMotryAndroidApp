package com.example.rickandmortyapplication.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.rickandmortyapplication.model.Character
import com.example.rickandmortyapplication.data.DataStoreManager
import com.example.rickandmortyapplication.R
import com.example.rickandmortyapplication.data.RickAndMortyRepository
import com.example.rickandmortyapplication.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("Non-zero value was expected")
    private lateinit var sharedPrefs: SharedPreferences
    private val autoLoadKey = booleanPreferencesKey("auto_load_pages")
    private val displayModeKey = stringPreferencesKey("display_mode")
    private val defaultBackupName = "backup_16.txt"
    private val internalCopyName = "backup_copy.txt"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch { loadSettings() }

        binding.editEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveEmail()
            }
        }

        binding.editBackupName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveBackupName()
            }
        }

        binding.buttonCreateBackup.setOnClickListener { lifecycleScope.launch { createBackup() } }
        binding.buttonDeleteBackup.setOnClickListener { lifecycleScope.launch { deleteBackup() } }
        binding.buttonRestoreBackup.setOnClickListener { lifecycleScope.launch { restoreBackup() } }

        binding.switchAutoLoad.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { saveAutoLoadInstant(isChecked) }
        }
        binding.radioGroupDisplayMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioList -> "list"
                R.id.radioGrid -> "grid"
                else -> "list"
            }
            lifecycleScope.launch { saveDisplayMode(mode) }
        }
    }

    private suspend fun loadSettings() {
        val dataStore = DataStoreManager.getDataStore(requireContext())
        val prefs = dataStore.data.first()

        val email = sharedPrefs.getString("email", "") ?: ""
        val fileName = sharedPrefs.getString("backup_file_name", defaultBackupName) ?: defaultBackupName

        val autoLoad = prefs[autoLoadKey] ?: false
        val displayMode = prefs[displayModeKey] ?: "list"

        withContext(Dispatchers.Main) {
            binding.editEmail.setText(email)
            binding.editBackupName.setText(fileName)
            binding.switchAutoLoad.isChecked = autoLoad

            when (displayMode) {
                "list" -> binding.radioList.isChecked = true
                "grid" -> binding.radioGrid.isChecked = true
            }

            updateBackupInfo()
        }
    }

    private fun saveEmail() {
        val email = binding.editEmail.text.toString()
        sharedPrefs.edit().apply {
            putString("email", email)
            apply()
        }
    }

    private fun saveBackupName() {
        val backupName = binding.editBackupName.text.toString().ifBlank { defaultBackupName }
        sharedPrefs.edit().apply {
            putString("backup_file_name", backupName)
            apply()
        }
        updateBackupInfo()
    }

    private suspend fun saveAutoLoadInstant(enabled: Boolean) {
        val dataStore = DataStoreManager.getDataStore(requireContext())
        dataStore.edit { settings ->
            settings[autoLoadKey] = enabled
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), "Автозагрузка: ${if (enabled) "ВКЛ" else "ВЫКЛ"}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun saveDisplayMode(mode: String) {
        val dataStore = DataStoreManager.getDataStore(requireContext())
        dataStore.edit { settings ->
            settings[displayModeKey] = mode
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), "Режим: ${if (mode == "grid") "сетка" else "список"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDownloadsFile(name: String): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, name)
    }

    private suspend fun createBackupCharactersToFile(): Boolean {
        val repository = RickAndMortyRepository()
        val allCharacters = mutableListOf<Character>()

        var page = 1
        while (true) {
            val response = repository.getCharacters(page)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    allCharacters.addAll(body.results)
                    if (body.info.next == null) break
                    page++
                } else break
            } else break
        }

        if (allCharacters.isEmpty()) return false

        val fileName = sharedPrefs.getString("backup_file_name", defaultBackupName) ?: defaultBackupName
        val file = getDownloadsFile(fileName)

        val json = buildString {
            append("[\n")
            allCharacters.forEachIndexed { index, c ->
                append("  {\n")
                append("    \"id\": ${c.id},\n")
                append("    \"name\": \"${c.name}\",\n")
                append("    \"status\": \"${c.status}\",\n")
                append("    \"species\": \"${c.species}\",\n")
                append("    \"image\": \"${c.image}\"\n")
                append("  }")
                if (index != allCharacters.lastIndex) append(",")
                append("\n")
            }
            append("]")
        }

        file.writeText(json)
        return true
    }

    private suspend fun createBackup() {
        withContext(Dispatchers.Main) {
            binding.buttonCreateBackup.isEnabled = false
        }
        val ok = createBackupCharactersToFile()
        withContext(Dispatchers.Main) {
            if (ok) {
                Toast.makeText(requireContext(), "Бэкап создан в Documents", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Нет данных для бэкапа", Toast.LENGTH_SHORT).show()
            }
            updateBackupInfo()
            binding.buttonCreateBackup.isEnabled = true
        }
    }

    private suspend fun deleteBackup() {
        withContext(Dispatchers.Main) { binding.buttonDeleteBackup.isEnabled = false }

        val fileName = sharedPrefs.getString("backup_file_name", defaultBackupName) ?: defaultBackupName
        val file = getDownloadsFile(fileName)
        if (!file.exists()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Внешний файл отсутствует", Toast.LENGTH_SHORT).show()
                updateBackupInfo()
                binding.buttonDeleteBackup.isEnabled = true
            }
            return
        }

        try {
            val internalCopy = File(requireContext().filesDir, internalCopyName)
            file.copyTo(internalCopy, overwrite = true)
            file.delete()
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Файл удалён, копия сохранена во внутреннем хранилище", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Ошибка при удалении: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                updateBackupInfo()
                binding.buttonDeleteBackup.isEnabled = true
            }
        }
    }

    private fun deleteInternalBackupCopy() {
        val internalCopy = File(requireContext().filesDir, internalCopyName)
        if (internalCopy.exists()) {
            if (internalCopy.delete()) {
                Toast.makeText(requireContext(), "Внутренняя копия удалена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Не удалось удалить внутреннюю копию", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Внутренняя копия отсутствует", Toast.LENGTH_SHORT).show()
        }

        updateBackupInfo()
    }

    private suspend fun restoreBackup() {
        withContext(Dispatchers.Main) { binding.buttonRestoreBackup.isEnabled = false }

        val internalCopy = File(requireContext().filesDir, internalCopyName)
        if (!internalCopy.exists()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Внутренняя копия отсутствует", Toast.LENGTH_SHORT).show()
                updateBackupInfo()
                binding.buttonRestoreBackup.isEnabled = true
            }
            return
        }

        try {
            val fileName = sharedPrefs.getString("backup_file_name", defaultBackupName) ?: defaultBackupName
            val restored = getDownloadsFile(fileName)
            internalCopy.copyTo(restored, overwrite = true)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Файл восстановлен в Documents", Toast.LENGTH_SHORT).show()
                deleteInternalBackupCopy()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Ошибка при восстановлении: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                updateBackupInfo()
                binding.buttonRestoreBackup.isEnabled = true
            }
        }
    }

    private fun updateBackupInfo() {
        val fileName = sharedPrefs.getString("backup_file_name", defaultBackupName) ?: defaultBackupName
        val externalFile = getDownloadsFile(fileName)
        val internalCopy = File(requireContext().filesDir, internalCopyName)
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        val infoParts = mutableListOf<String>()

        if (externalFile.exists()) {
            infoParts += "📄 Внешний файл: ${externalFile.name}"
            infoParts += "Размер: ${externalFile.length()} байт"
            infoParts += "Создан: ${sdf.format(Date(externalFile.lastModified()))}"
        }

        if (internalCopy.exists()) {
            infoParts += "💾 Внутренняя копия: ${internalCopy.name}"
            infoParts += "Размер: ${internalCopy.length()} байт"
        }

        binding.textBackupInfo.text = if (infoParts.isEmpty()) {
            "Резервный файл отсутствует"
        } else {
            infoParts.joinToString("\n\n")
        }

        binding.buttonCreateBackup.visibility = if (externalFile.exists()) View.GONE else View.VISIBLE
        binding.buttonDeleteBackup.visibility = if (externalFile.exists()) View.VISIBLE else View.GONE
        binding.buttonRestoreBackup.visibility = if (internalCopy.exists()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}