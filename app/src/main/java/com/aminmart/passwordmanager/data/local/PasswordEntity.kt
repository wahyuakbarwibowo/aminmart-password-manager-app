package com.aminmart.passwordmanager.data.local

/**
 * Password entry entity stored in the database.
 * Sensitive fields are stored encrypted.
 */
data class PasswordEntity(
    val id: Long = 0,
    val title: String,
    val username: String = "",
    val passwordEncrypted: String = "",
    val website: String = "",
    val notesEncrypted: String = "",
    val category: PasswordCategory,
    val icon: String = "",
    val ciphertext: String? = null,
    val nonce: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Categories for organizing passwords.
 */
enum class PasswordCategory {
    SOCIAL,
    EMAIL,
    SHOPPING,
    FINANCE,
    ENTERTAINMENT,
    WORK,
    OTHER
}
