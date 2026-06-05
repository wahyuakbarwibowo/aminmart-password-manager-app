# Aminmart Password Manager

Native Android password manager app built with Kotlin, Jetpack Compose, and modern Android architecture components.

## Security Features

### Production-Grade Encryption

This app implements **industry-standard security** using Android's recommended cryptographic practices:

#### 1. **AES-256-GCM Encryption**
- **Algorithm**: AES (Advanced Encryption Standard) with 256-bit key
- **Mode**: GCM (Galois/Counter Mode) - authenticated encryption
- **IV Size**: 96 bits (12 bytes) - NIST recommended
- **Tag Size**: 128 bits - for authentication

#### 2. **Android Keystore System**
- Keys stored in hardware-backed secure enclave (when available)
- Keys never leave the secure hardware
- Protection against root attacks
- Key attestation support (on compatible devices)

#### 3. **PBKDF2 Password Hashing**
- **Algorithm**: PBKDF2WithHmacSHA256
- **Iterations**: 100,000 (OWASP recommended minimum)
- **Salt**: 256-bit random salt per password
- **Output**: 256-bit hash

### Security Architecture

```
Master Password → PBKDF2 → Hash (stored for verification)
                    ↓
Master Password → Android Keystore → AES Key (hardware-backed)
                    ↓
AES-256-GCM → Encrypt/Decrypt Password Data
```

## Tech Stack

- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Hilt (Dagger)
- **Database**: SQLite (hand-written `SQLiteOpenHelper`)
- **Navigation**: Jetpack Navigation Compose
- **Security**: AndroidX Security Crypto, Android Keystore

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35 (target), SDK 26 (minimum)

### Build & Run

1. **Open in Android Studio**
   ```bash
   # Open the project folder in Android Studio
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync
   - Wait for dependencies to download

3. **Run the App**
   - Select an emulator or physical device
   - Click Run (Shift+F10)

### Build from Command Line

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app-password-manager/
├── app/
│   ├── src/main/
│   │   ├── java/com/aminmart/passwordmanager/
│   │   │   ├── data/
│   │   │   │   ├── local/           # SQLiteOpenHelper, entities
│   │   │   │   ├── repository/      # Data repositories
│   │   │   │   └── security/        # Encryption services
│   │   │   ├── domain/
│   │   │   │   └── model/           # Domain models
│   │   │   ├── di/                  # Hilt modules
│   │   │   ├── ui/
│   │   │   │   ├── navigation/      # Navigation
│   │   │   │   ├── screens/         # Compose screens & ViewModels
│   │   │   │   ├── components/      # Reusable components
│   │   │   │   └── theme/           # Material theme
│   │   │   ├── MainActivity.kt
│   │   │   └── PasswordManagerApplication.kt
│   │   ├── res/                     # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

## Features

- 🔐 **Master Password** - Secure vault with PBKDF2 verification
- 🧬 **Biometric Unlock** - Fingerprint/face authentication
- 🔑 **Password Generator** - Strong random passwords with strength indicator
- 📱 **Categories** - Organize passwords by type
- 🔍 **Search** - Quick search through passwords
- 📤 **Encrypted Backup** - Export/import encrypted backups
- 📋 **Copy to Clipboard** - Easy password copying
- 🎨 **Material 3 UI** - Modern, beautiful interface

## Security Best Practices

1. ✅ Hardware-backed Keystore
2. ✅ Authenticated Encryption (AES-GCM)
3. ✅ Unique Nonce per encryption
4. ✅ Strong KDF (PBKDF2 100k iterations)
5. ✅ Constant-time password comparison
6. ✅ No cloud backup (excluded)
7. ✅ Biometric authentication option

## Database Schema

### Passwords Table
```sql
CREATE TABLE passwords (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    username TEXT,
    password_encrypted TEXT,
    website TEXT,
    notes_encrypted TEXT,
    category TEXT NOT NULL,
    icon TEXT,
    ciphertext TEXT,      -- Encrypted password + notes payload
    nonce TEXT,           -- GCM nonce
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
)
```

### Settings Table
```sql
CREATE TABLE settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key TEXT NOT NULL UNIQUE,
    value TEXT
)
```

## License

Educational purposes only. Use at your own risk.

## Resources

- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Android Keystore](https://developer.android.com/training/articles/keystore)
- [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
