# Aminmart Password Manager - Android (Kotlin)

Native Android password manager app built with Kotlin, Jetpack Compose, and modern Android architecture components.

## Security Features

### Encryption Implementation

This app implements **production-grade security** using Android's recommended cryptographic practices:

#### 1. **AES-256-GCM Encryption**
- **Algorithm**: AES (Advanced Encryption Standard) with 256-bit key
- **Mode**: GCM (Galois/Counter Mode) - authenticated encryption
- **IV Size**: 96 bits (12 bytes) - NIST recommended
- **Tag Size**: 128 bits - for authentication

#### 2. **Android Keystore System**
- Keys are stored in hardware-backed secure enclave (when available)
- Keys never leave the secure hardware
- Protection against root/jailbreak attacks
- Key attestation support (on compatible devices)

#### 3. **PBKDF2 Password Hashing**
- **Algorithm**: PBKDF2WithHmacSHA256
- **Iterations**: 100,000 (OWASP recommended minimum)
- **Salt**: 256-bit random salt per password
- **Output**: 256-bit hash

#### 4. **Security Architecture**
```
Master Password → PBKDF2 → Hash (stored)
                    ↓
Master Password → Android Keystore → AES Key (hardware-backed)
                    ↓
AES-256-GCM → Encrypt/Decrypt Password Data
```

### Security Disclaimer

While this implementation uses industry-standard cryptographic primitives:
- **Always verify** the security of any password manager before storing sensitive data
- **Keep backups** of your passwords in a secure location
- **Use a strong master password** (minimum 12 characters recommended)
- **Enable biometric authentication** for additional security

## Tech Stack

### Core Technologies
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt (Dagger)
- **Navigation**: Jetpack Navigation Compose

### Data & Storage
- **Database**: Room (SQLite abstraction)
- **Encrypted Preferences**: AndroidX Security Crypto
- **Data Serialization**: Kotlinx Serialization

### Security Libraries
```kotlin
// AndroidX Security - EncryptedSharedPreferences & MasterKey
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// Biometric Authentication
implementation("androidx.biometric:biometric:1.1.0")

// Android Keystore (built-in)
java.security.KeyStore
javax.crypto.Cipher
```

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/aminmart/passwordmanager/
│   │   │   ├── data/
│   │   │   │   ├── local/           # Room entities & DAOs
│   │   │   │   ├── repository/      # Data repositories
│   │   │   │   └── security/        # Encryption services
│   │   │   ├── domain/
│   │   │   │   └── model/           # Domain models
│   │   │   ├── di/                  # Hilt dependency injection modules
│   │   │   ├── ui/
│   │   │   │   ├── navigation/      # Navigation graph
│   │   │   │   ├── screens/         # Compose screens & ViewModels
│   │   │   │   ├── components/      # Reusable UI components
│   │   │   │   └── theme/           # Material theme
│   │   │   ├── MainActivity.kt
│   │   │   └── PasswordManagerApplication.kt
│   │   ├── res/                     # Android resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── build.gradle.kts
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35 (target), SDK 26 (minimum)

### Build & Run

1. **Open in Android Studio**
   ```bash
   # Open the android/ folder in Android Studio
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync
   - Wait for dependencies to download

3. **Run the App**
   - Select an emulator or physical device
   - Click Run (Shift+F10)

### Build from Command Line

```bash
cd android

# Debug build
./gradlew assembleDebug

# Release build (signed)
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

## Key Components

### EncryptionService (`data/security/EncryptionService.kt`)

Handles all cryptographic operations using Android Keystore:

```kotlin
class EncryptionService {
    // Generate/retrieve master key from Android Keystore
    fun getOrCreateMasterKey(): SecretKey
    
    // Encrypt data with AES-256-GCM
    fun encrypt(plaintext: ByteArray): EncryptedData
    
    // Decrypt data with AES-256-GCM
    fun decrypt(encryptedData: EncryptedData): ByteArray
}
```

### SecretEncryptionService (`data/security/SecretEncryptionService.kt`)

Encrypts password and notes fields:

```kotlin
class SecretEncryptionService {
    // Encrypt password + notes payload
    fun encryptSecrets(password: String, notes: String): EncryptedSecrets
    
    // Decrypt to retrieve original values
    fun decryptSecrets(ciphertext: String, nonce: String): SecretsPayload
}
```

### PasswordHashingService (`data/security/PasswordHashingService.kt`)

Secure password hashing for master password verification:

```kotlin
class PasswordHashingService {
    // Hash with random salt using PBKDF2
    fun hashPassword(password: String): PasswordHash
    
    // Verify password against stored hash (constant-time)
    fun verifyPassword(password: String, salt: String, hash: String): Boolean
}
```

### BiometricAuthService (`data/security/BiometricAuthService.kt`)

Biometric authentication using AndroidX Biometric:

```kotlin
class BiometricAuthService {
    // Check biometric availability
    fun isBiometricAvailable(context: Context): BiometricAvailability
    
    // Show biometric prompt
    fun authenticate(activity: FragmentActivity, ...)
}
```

## Database Schema

### Passwords Table
```sql
CREATE TABLE passwords (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    username TEXT NOT NULL,
    website TEXT NOT NULL,
    category TEXT NOT NULL,
    ciphertext TEXT,      -- Encrypted password + notes
    nonce TEXT,           -- GCM nonce
    createdAt INTEGER,
    updatedAt INTEGER
)
```

### Settings Table
```sql
CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updatedAt INTEGER
)
```

## Backup Format

Encrypted backup files use JSON format:

```json
{
  "version": 1,
  "createdAt": 1234567890,
  "salt": "base64-encoded-salt",
  "ciphertext": "base64-encoded-encrypted-data",
  "nonce": "base64-encoded-nonce"
}
```

The ciphertext contains the encrypted backup payload with all password entries.

## Security Best Practices Implemented

1. ✅ **Hardware-backed Keystore**: Keys stored in secure enclave
2. ✅ **Authenticated Encryption**: AES-GCM provides integrity + confidentiality
3. ✅ **Unique Nonce**: Random IV for each encryption operation
4. ✅ **Strong KDF**: PBKDF2 with 100k iterations
5. ✅ **Constant-time Comparison**: Prevents timing attacks
6. ✅ **No Cloud Backup**: App data excluded from cloud backups
7. ✅ **Biometric Auth**: Optional biometric unlock
8. ✅ **Memory Safety**: Keys cleared from memory when not needed

## Comparison with React Native Version

| Feature | React Native (Original) | Kotlin Android (New) |
|---------|------------------------|---------------------|
| Encryption | XOR cipher (educational) | AES-256-GCM (production) |
| Key Storage | In-memory | Android Keystore (hardware) |
| Password Hashing | SHA-256 | PBKDF2 (100k iterations) |
| Biometric | expo-local-authentication | androidx.biometric |
| Database | expo-sqlite | Room |
| UI | React Native + Paper | Jetpack Compose + M3 |

## Testing

```kotlin
// Example: Encryption test
@Test
fun encryptDecrypt_roundTrip() {
    val plaintext = "test password".toByteArray()
    val encrypted = encryptionService.encrypt(plaintext)
    val decrypted = encryptionService.decrypt(encrypted)
    
    assertArrayEquals(plaintext, decrypted)
}

// Example: Password hashing test
@Test
fun verifyPassword_correctPassword() {
    val passwordHash = hashingService.hashPassword("test123")
    val isValid = hashingService.verifyPassword(
        "test123",
        passwordHash.salt,
        passwordHash.hash
    )
    
    assertTrue(isValid)
}
```

## License

Educational purposes only. Use at your own risk.

## Contributing

This is a learning project. Contributions welcome for educational improvements.

## Resources

- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Android Keystore](https://developer.android.com/training/articles/keystore)
- [NIST AES-GCM Guidelines](https://csrc.nist.gov/publications/detail/sp/800-38d/final)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
