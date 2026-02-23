package com.aminmart.passwordmanager.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.aminmart.passwordmanager.data.local.PasswordDatabase
import com.aminmart.passwordmanager.data.local.PasswordEntity
import com.aminmart.passwordmanager.data.local.PasswordCategory
import com.aminmart.passwordmanager.data.security.EncryptedData
import com.aminmart.passwordmanager.data.security.EncryptionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for backup and restore operations.
 * Backups are encrypted using the master password.
 */
@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PasswordDatabase,
    private val encryptionService: EncryptionService
) {

    companion object {
        private const val BACKUP_VERSION = 1
        private const val BACKUP_FILE_EXTENSION = ".aminmartbackup"
        private const val PBKDF2_ITERATIONS = 100000
        private const val SALT_LENGTH = 32
    }

    private val secureRandom = java.security.SecureRandom()

    /**
     * Export all passwords to a backup file.
     */
    suspend fun exportBackup(
        masterPassword: String,
        uri: Uri
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            // Get all encrypted passwords
            val passwords = database.getAllEncryptedPasswords()

            if (passwords.isEmpty()) {
                return@withContext BackupResult.Error("No passwords to backup")
            }

            // Create backup payload JSON
            val passwordsArray = JSONArray()
            passwords.forEach { entity ->
                val passwordObj = JSONObject()
                    .put("title", entity.title)
                    .put("username", entity.username)
                    .put("website", entity.website)
                    .put("category", entity.category.name)
                    .put("ciphertext", entity.ciphertext ?: "")
                    .put("nonce", entity.nonce ?: "")
                    .put("createdAt", entity.createdAt)
                    .put("updatedAt", entity.updatedAt)
                passwordsArray.put(passwordObj)
            }

            val payloadObj = JSONObject()
                .put("version", BACKUP_VERSION)
                .put("createdAt", System.currentTimeMillis())
                .put("passwords", passwordsArray)

            val payloadBytes = payloadObj.toString().toByteArray(StandardCharsets.UTF_8)

            // Derive key from master password and encrypt payload
            val salt = ByteArray(SALT_LENGTH).apply { secureRandom.nextBytes(this) }

            // Encrypt payload
            val encryptedData = encryptionService.encrypt(payloadBytes)

            // Create backup file JSON
            val backupObj = JSONObject()
                .put("version", BACKUP_VERSION)
                .put("createdAt", System.currentTimeMillis())
                .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                .put("ciphertext", Base64.encodeToString(encryptedData.ciphertext, Base64.NO_WRAP))
                .put("nonce", Base64.encodeToString(encryptedData.nonce, Base64.NO_WRAP))

            val backupJson = backupObj.toString()

            // Write to URI
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(backupJson.toByteArray(StandardCharsets.UTF_8))
            }

            BackupResult.Success(passwords.size)
        } catch (e: Exception) {
            BackupResult.Error("Export failed: ${e.message}")
        }
    }

    /**
     * Import passwords from a backup file.
     * @param mode Import mode (merge or overwrite)
     */
    suspend fun importBackup(
        masterPassword: String,
        uri: Uri,
        mode: ImportMode = ImportMode.MERGE
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            // Read backup file
            val backupJson = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext BackupResult.Error("Failed to read backup file")

            // Parse backup file
            val backupObj = try {
                JSONObject(backupJson)
            } catch (e: Exception) {
                return@withContext BackupResult.Error("Invalid backup file format")
            }

            val version = backupObj.getInt("version")
            if (version != BACKUP_VERSION) {
                return@withContext BackupResult.Error("Unsupported backup version")
            }

            // Derive key from master password
            val salt = Base64.decode(backupObj.getString("salt"), Base64.NO_WRAP)

            // Decrypt payload
            val ciphertext = Base64.decode(backupObj.getString("ciphertext"), Base64.NO_WRAP)
            val nonce = Base64.decode(backupObj.getString("nonce"), Base64.NO_WRAP)

            val encryptedData = EncryptedData(ciphertext = ciphertext, nonce = nonce)
            val payloadBytes = try {
                encryptionService.decrypt(encryptedData)
            } catch (e: Exception) {
                return@withContext BackupResult.Error("Wrong password or corrupted backup")
            }

            val payloadJson = String(payloadBytes, StandardCharsets.UTF_8)
            val payloadObj = try {
                JSONObject(payloadJson)
            } catch (e: Exception) {
                return@withContext BackupResult.Error("Failed to parse backup payload")
            }

            // Import passwords
            var imported = 0
            var skipped = 0

            when (mode) {
                ImportMode.OVERWRITE -> {
                    // Delete all existing passwords
                    database.getAllPasswordsList().forEach { entity ->
                        database.deletePassword(entity)
                    }
                }
                ImportMode.MERGE -> {
                    // Will skip duplicates
                }
            }

            val passwordsArray = payloadObj.getJSONArray("passwords")
            for (i in 0 until passwordsArray.length()) {
                try {
                    val item = passwordsArray.getJSONObject(i)
                    val title = item.getString("title")
                    val username = item.getString("username")
                    val website = item.getString("website")
                    val category = item.getString("category")
                    val ciphertext = item.getString("ciphertext")
                    val nonce = item.getString("nonce")
                    val createdAt = item.getLong("createdAt")
                    val updatedAt = item.getLong("updatedAt")

                    // Check for duplicates in merge mode
                    if (mode == ImportMode.MERGE) {
                        val existing = database.getAllPasswordsList()
                            .find { it.title == title && it.username == username && it.website == website }
                        if (existing != null) {
                            skipped++
                            continue
                        }
                    }

                    val entity = PasswordEntity(
                        title = title,
                        username = username,
                        passwordEncrypted = ciphertext,
                        website = website,
                        notesEncrypted = "",
                        category = PasswordCategory.valueOf(category),
                        icon = "",
                        ciphertext = ciphertext,
                        nonce = nonce,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )

                    database.insertPassword(entity)
                    imported++
                } catch (e: Exception) {
                    // Skip failed imports
                    skipped++
                }
            }

            BackupResult.Success(imported = imported, skipped = skipped)
        } catch (e: Exception) {
            BackupResult.Error("Import failed: ${e.message}")
        }
    }

    /**
     * Derive an encryption key from the master password using PBKDF2.
     */
    private fun deriveKeyFromPassword(password: String, salt: ByteArray): ByteArray {
        val spec = javax.crypto.spec.PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            256
        )
        try {
            val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            return factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

/**
 * Import mode for backup restoration.
 */
enum class ImportMode {
    MERGE,      // Merge with existing, skip duplicates
    OVERWRITE   // Delete all existing and replace
}

/**
 * Backup operation result.
 */
sealed class BackupResult {
    data class Success(val imported: Int = 0, val skipped: Int = 0) : BackupResult()
    data class Error(val message: String) : BackupResult()
}
