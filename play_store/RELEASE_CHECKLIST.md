# Google Play Store Release & Deployment Checklist

## 1. Cryptographic Keystore Generation

Generate a release signing key using JDK `keytool`:

```bash
keytool -genkeypair -v \
  -keystore aura-release.keystore \
  -alias aura_key \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storetype PKCS12
```

Store credentials safely as CI/CD environment variables:
- `KEYSTORE_PATH`: Path to `.keystore` file
- `KEYSTORE_PASSWORD`: Keystore pass
- `KEY_ALIAS`: `aura_key`
- `KEY_PASSWORD`: Key pass

---

## 2. Release Android App Bundle (AAB) Compilation

From the `android/` directory:

```bash
# Clean project
./gradlew clean

# Run unit tests
./gradlew testReleaseUnitTest

# Assemble Release Android App Bundle
./gradlew bundleRelease
```

The output bundle will be located at:
`android/app/build/outputs/bundle/release/app-release.aab`

---

## 3. 16 KB Memory Page Size Compatibility Verification

Google Play requires native shared libraries (`.so`) in Android 15 to be aligned with 16 KB boundaries:

```bash
# Unzip APK/AAB and inspect .so alignment
python -m zipfile -e android/app/build/outputs/bundle/release/app-release.aab ./extracted_aab

# Run check on all native shared libraries
find ./extracted_aab -name "*.so" -exec readelf -l {} + | grep -i "LOAD"
```
Ensure all `LOAD` segment alignments report `0x4000` (16384 bytes). In this project, `ndk.abiFilters` in `build.gradle.kts` and standard AndroidX Media3 1.5+ libraries conform to this requirement.

---

## 4. Google Play Console Pre-Launch Checklist

- [ ] **Application ID:** Confirmed `com.aura.wallpapers`
- [ ] **Target SDK:** Set to `35` (Android 15)
- [ ] **Minimum SDK:** Set to `26` (Android 8.0)
- [ ] **Version Code:** `100` (`versionName = "1.0.0"`)
- [ ] **ProGuard Obfuscation:** Minified and shrunk with `proguard-rules.pro`
- [ ] **Mapping File:** Upload `mapping.txt` from `app/build/outputs/mapping/release/` for de-obfuscation in Crashlytics and Play Console Vitals.
- [ ] **App Content Declarations:**
  - [ ] Privacy Policy URL entered (`https://aura-wallpapers.com/privacy`)
  - [ ] Ads declaration (Select "Yes, my app contains ads" if optional banners enabled, otherwise "No")
  - [ ] App Access: All features available without special login credentials
  - [ ] Content Rating Questionnaire: General / Everyone (PEGI 3, ESRB Everyone)
  - [ ] Target Audience: 13 and older
  - [ ] Data Safety: Answers match `play_store/DATA_SAFETY.md`
  - [ ] Government Apps: "No"
  - [ ] Financial Features: "No"
  - [ ] Health Apps: "No"
- [ ] **Store Assets:**
  - [ ] High-resolution app icon: 512x512 PNG, 32-bit color
  - [ ] Feature Graphic: 1024x500 PNG/JPEG, 24-bit color (no alpha)
  - [ ] Phone screenshots: Minimum 4 screenshots (1080x2400 or 1080x1920)
  - [ ] 7-inch & 10-inch tablet screenshots (optional for tablet readiness)
- [ ] **Internal Testing Track:**
  - [ ] Create internal release track
  - [ ] Upload `app-release.aab`
  - [ ] Verify Pre-launch Report for crashes, ANRs, and layout issues
  - [ ] Promote to Closed Testing / Production
