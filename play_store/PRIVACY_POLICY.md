# Privacy Policy for Aura Wallpapers

**Effective Date:** November 5, 2026  
**Last Updated:** November 5, 2026  
**Application ID:** `com.aura.wallpapers`

## 1. Introduction
Aura Wallpapers ("we", "our", or "the App") is committed to protecting your privacy. This Privacy Policy explains our practices regarding the minimal collection and handling of data when you use the Aura Wallpapers mobile application.

## 2. Information We Do NOT Collect
We believe in least-privilege design. Aura Wallpapers does NOT collect:
- Names, email addresses, phone numbers, or passwords.
- Real-time GPS or network location.
- Contacts, call logs, SMS, or private messaging history.
- Device cameras, microphones, or sensor recordings.
- User photos, personal video galleries, or private storage files.

## 3. Information We Collect Automatically
To guarantee smooth content delivery, popular rankings, and application stability, we collect limited, non-identifying technical data:
- **Anonymous Metrics:** When a wallpaper is downloaded or applied, an anonymous counter is incremented on our cloud server. This is tied only to the asset ID to compute trending charts.
- **Diagnostic Information:** In the event of an unhandled crash or ANR (Application Not Responding), standard anonymous stack traces are logged via Google Play Console to identify device model compatibility issues.

## 4. Android Device Permissions
We request only permissions strictly required to perform wallpaper operations:
- `android.permission.INTERNET`: Required to communicate with our content delivery network and fetch wallpaper images.
- `android.permission.ACCESS_NETWORK_STATE`: Used to detect network connectivity and respect user preferences (such as Wi-Fi only downloads).
- `android.permission.SET_WALLPAPER` & `SET_WALLPAPER_HINTS`: Required by Android system APIs to crop and apply static wallpapers to your home and lock screen.
- `android.permission.POST_NOTIFICATIONS` (Android 13+): Optional permission requested only if you wish to receive progress updates for large background downloads.

## 5. Third-Party Services
Our backend utilizes:
- **Supabase (PostgreSQL & Storage):** Hosts metadata and asset manifests over secure HTTPS.
- **Cloudflare CDN:** Delivers cached WebP images and MP4 video segments with DDoS protection and edge caching.

Neither provider receives personally identifiable customer data from the mobile client.

## 6. Children's Privacy (COPPA & GDPR-K Compliance)
Aura Wallpapers does not knowingly collect any data from children under the age of 13. All content is family-friendly, neutral, and suitable for general audiences.

## 7. Changes to This Privacy Policy
We may update our Privacy Policy periodically. Any updates will be reflected with a revised "Effective Date" at the top of this policy and accessible directly inside the application's Settings menu.

## 8. Contact Information
If you have any questions or concerns regarding our privacy practices, please contact:
**Email:** `privacy@aura-wallpapers.com`  
**Web:** `https://aura-wallpapers.com/privacy`
