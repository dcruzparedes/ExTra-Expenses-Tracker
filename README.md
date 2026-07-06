<h1 align="center" id="title">ExTra: Expenses Tracker</h1>

<p align="center"><img src="https://socialify.git.ci/dcruzparedes/ExTra-Expenses-Tracker/image?custom_description=An+Android+native+app+for+expense+tracking.&description=1&font=Source+Code+Pro&language=1&name=1&owner=1&pattern=Plus&stargazers=1&theme=Dark"></p>

<p id="description">An Android native app for expense tracking.</p>

<p align="center"><img src="https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&amp;logo=kotlin&amp;logoColor=white" alt="shields"><img src="https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&amp;logo=android-studio&amp;logoColor=white" alt="shields"><img src="https://img.shields.io/badge/Material%20Design-757575.svg?style=for-the-badge&amp;logo=material-design&amp;logoColor=white" alt="shields"><img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&amp;logo=android&amp;logoColor=white" alt="shields"><img src="https://img.shields.io/badge/version-v1.0.0-blue?style=for-the-badge" alt="shields"><img src="https://img.shields.io/badge/Status-Active%20Development-green?style=for-the-badge" alt="shields"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=for-the-badge" alt="shields"></p>

<h2>Project Screenshots:</h2>
<p align="center">
  <img src="https://github.com/user-attachments/assets/995f170a-5fdb-4b59-9308-480c03eb664e" width="300" />
  <img src="https://github.com/user-attachments/assets/a92848df-a6e8-4038-9c12-622bff98f03f" width="300" />
  <img src="https://github.com/user-attachments/assets/4e93aede-2476-4fe5-8c14-14c985d609d0" width="300" />
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/7d80df0d-8a83-48cd-94f0-f02f3b005607" width="300" />
  <img src="https://github.com/user-attachments/assets/804f9314-fe92-4f2e-a155-fd5bfbb4f450" width="300" />
</p>
<h3>Quick Settings Panel Button:</h3>
<p align="center">
  <img src="https://github.com/user-attachments/assets/f8503695-c19b-44f4-9fb5-0d1ac66a99ed" width="300" />
  <img src="https://github.com/user-attachments/assets/54508021-cbb1-4e97-b89f-29a65e852f57" width="300" />
</p>



  
  
<h2>🧐 Features</h2>

Here're some of the project's best features:

*   Add expenses quickly
*   Edit existing expenses
*   Local persistence using Room
*   Quick Settings Tile to add an expense faster (Android Quick Settings integration)
*   Expenses classification using categories

<h2>🛠️ Installation Steps:</h2>

<p>1. Download the <a href="https://github.com/dcruzparedes/ExTra-Expenses-Tracker/releases/latest">latest release</a> and install it on your smartphone.</p>

  
  
<h2>💻 Built with</h2>

Technologies used in the project:

*   Kotlin (100%)
*   Android (Gradle Kotlin DSL)
*   Jetpack Compose + ViewBinding
*   Room (KSP)

<h2>Development</h2>
<h3>Requirements</h3>

- **Android Studio** (recent version recommended)
- **Min SDK:** 29  
- **Target SDK:** 36  
- **Java:** 11 (source/target compatibility)

---

<section>
    <h2>Getting Started (Run locally)</h2>
    <ol>
        <li>Clone the repository:
            <pre><code>git clone https://github.com/dcruzparedes/ExTra-Expenses-Tracker.git</code></pre>
        </li>
        <li>Open the project in <strong>Android Studio</strong></li>
        <li>Let Gradle sync dependencies</li>
        <li>Run the <code>app</code> configuration on an emulator or a physical device (Android 10 / API 29+)</li>
    </ol>
</section>

<hr>

<section>
    <h2>Build Configuration Notes</h2>
    <p>Key dependencies include:</p>
    <ul>
        <li>Jetpack Compose (Material 3 via Compose BOM)</li>
        <li>Room:
            <ul>
                <li><code>androidx.room:room-runtime:2.8.4</code></li>
                <li><code>androidx.room:room-ktx:2.8.4</code></li>
                <li><code>androidx.room:room-compiler:2.8.4</code> (via <strong>KSP</strong>)</li>
            </ul>
        </li>
        <li>Material Components:
            <ul>
                <li><code>com.google.android.material:material:1.12.0</code></li>
            </ul>
        </li>
        <li>RecyclerView + Selection</li>
        <li>AppCompat / CoordinatorLayout</li>
    </ul>
</section>

<hr>

<section>
    <h2>Project Structure (high level)</h2>
    <ul>
        <li><code>app/</code> — Android application module</li>
        <li><code>gradle/libs.versions.toml</code> — version catalog</li>
        <li><code>build.gradle.kts</code>, <code>settings.gradle.kts</code> — Gradle Kotlin DSL configuration</li>
    </ul>
</section>

<hr>

<section>
    <h2>App Branding</h2>
    <p>An app icon asset is present at:</p>
    <ul>
        <li><code>app/src/main/money_agenda_launcher-playstore.png</code></li>
    </ul>
</section>

<hr>

<section>
    <h2>Contributing</h2>
    <p>Contributions are welcome. If you’d like to help:</p>
    <ol>
        <li>Fork the repo</li>
        <li>Create a feature branch</li>
        <li>Open a pull request describing your changes</li>
    </ol>
</section>

<hr>

<section>
    <h2>License</h2>
    <p>This project is licensed under the <strong>MIT License</strong>. See the <a href="LICENSE">LICENSE</a> file for details.</p>
</section>
