# Descripix 📸✨

![Descripix Banner](C:\Users\joeva\.gemini\antigravity\brain\6db457ac-6dd6-414f-b220-866da34ad150\descripix_banner_1777741033306.png)

**Descripix** is a premium Android application that leverages Artificial Intelligence to breathe life into your photos. By automatically generating descriptive, creative, and context-aware captions, Descripix helps you tell the story behind every pixel.

---

## 🚀 Features

- **🤖 AI-Powered Captioning**: Instantly generate high-quality captions for any image using state-of-the-art AI models.
- **🔍 Metadata Extraction**: Deep-dive into your photos with automated EXIF and metadata extraction to enhance caption accuracy.
- **📂 Caption Management**: Save, edit, and organize your favorite captions in a sleek, searchable library.
- **🔐 Secure Authentication**: Seamless onboarding with Google Login and secure token-based sessions.
- **🎨 Modern UI/UX**: Built with Jetpack Compose and Material 3 for a fluid, responsive, and "glassmorphic" experience.
- **📱 Profile Customization**: Tailor your experience with a personalized user profile.

---

## 🛠️ Tech Stack

Descripix is built with a focus on performance, scalability, and clean code principles.

| Layer | Technologies |
| :--- | :--- |
| **Core** | Kotlin, Coroutines, Flow |
| **UI** | Jetpack Compose, Material 3, Lottie, Coil |
| **Architecture** | MVVM, Clean Architecture, Repository Pattern |
| **Dependency Injection** | Dagger Hilt |
| **Networking** | Retrofit, OkHttp, GSON |
| **Persistence** | Room Database, DataStore, SQLCipher (Encrypted) |
| **Metadata** | Metadata Extractor (EXIF) |
| **Auth** | Firebase Auth, Google Play Services |

---

## 📸 Screenshots

![Descripix Mockup] <img width="1994" height="1779" alt="Home" src="https://github.com/user-attachments/assets/37f6ab10-0af1-46c6-8841-d9539793dc6a" />

![Descripix Mockup] <img width="1994" height="1779" alt="Home" src="https://github.com/user-attachments/assets/37f6ab10-0af1-46c6-8841-d9539793dc6a" />

![Descripix Mockup] <img width="1994" height="1779" alt="Home" src="https://github.com/user-attachments/assets/37f6ab10-0af1-46c6-8841-d9539793dc6a" />


---

## 📥 Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Joevandyta/Descripix.git
   ```
2. **Open in Android Studio**:
   Import the project and wait for Gradle sync to complete.
3. **Configure API Keys**:
   - Add your `BASE_URL` and `GUEST_USER_TOKEN` to `local.properties`.
   - Place your `google-services.json` in the `app/` directory.
4. **Build and Run**:
   Select your device and hit **Run**.

---

## 🏛️ Project Structure

The project follows the **Clean Architecture** pattern to ensure modularity and testability:

- `data/`: Implementation of repositories and data sources (Local & Remote).
- `domain/`: Business logic, use cases, and repository interfaces.
- `ui/`: UI components, Composables, ViewModels, and navigation logic.
- `di/`: Dependency injection modules using Hilt.
- `utils/`: Helper classes for image processing, connectivity, and more.

---

## 🤝 Contributing

Contributions are welcome! If you have suggestions for new features or improvements, please feel free to open an issue or submit a pull request.

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/Joevandyta">Jovan</a>
</p>

