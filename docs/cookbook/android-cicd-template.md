# Recipe: Android CI/CD Template — Copy sang project mới

Checklist 1 trang để setup pipeline giống DFood trên **project Android/Kotlin mới**.

---

## Files cần copy

```
.github/
  workflows/ci.yml
  workflows/release.yml
  workflows/release-please.yml
  workflows/codeql.yml              # optional
  release-please-config.json
  .release-please-manifest.json
  dependabot.yml                    # optional
version.properties
app/google-services.json.ci         # nếu dùng Firebase plugin
local.properties.example
CHANGELOG.md
```

---

## Sửa sau khi copy

| File | Đổi gì |
|------|--------|
| `release.yml` | `DFood-v` → `YourApp-v` (tên APK) |
| `ci.yml`, `release.yml` | `android-35` → khớp `compileSdk` |
| `google-services.json.ci` | `package_name` = `applicationId` |
| `.release-please-manifest.json` | `"1.0.0"` version ban đầu |
| `version.properties` | VERSION_* khởi tạo |

---

## Gradle bắt buộc (`app/build.gradle.kts`)

- [ ] Đọc `version.properties`
- [ ] Hỗ trợ `-PVERSION_NAME` và `-PVERSION_CODE`
- [ ] `signingConfigs.release` đọc `KEYSTORE_PATH` (env) hoặc `RELEASE_STORE_FILE` (local)
- [ ] `release { isMinifyEnabled = true }` (khuyến nghị)

Copy pattern từ DFood `app/build.gradle.kts` dòng 18–33, 88–119.

---

## GitHub Secrets (Environment: `production`)

| Secret | Bắt buộc |
|--------|----------|
| `KEYSTORE_BASE64` | ✅ |
| `KEYSTORE_PASSWORD` | ✅ |
| `KEY_ALIAS` | ✅ |
| `KEY_PASSWORD` | ✅ |
| `GOOGLE_SERVICES_JSON` | Nếu Firebase |
| `FACEBOOK_APP_ID` | Nếu Facebook login |
| `FACEBOOK_CLIENT_TOKEN` | Nếu Facebook login |
| `GOOGLE_WEB_CLIENT_ID` | Nếu Google login |

Encode keystore:
```bash
base64 -w0 release.jks    # Linux
```

---

## Keystore (một lần)

```bash
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias YOUR_ALIAS
```

SHA-1 cho Firebase/Facebook:
```bash
keytool -list -v -keystore release.jks -alias YOUR_ALIAS
```

---

## Verify checklist

- [ ] PR → CI pass + debug APK artifact
- [ ] Merge `main` → release-please PR xuất hiện
- [ ] Merge release PR → tag `vX.Y.Z` + Release workflow
- [ ] APK tải được từ GitHub Releases
- [ ] Cài đè bản cũ thành công
- [ ] Social login trên release build (nếu có)

---

## Quy trình release (nhắc nhanh)

1. Commit `feat:` / `fix:` (Conventional Commits)
2. PR → CI green → merge `main`
3. Merge PR release-please
4. Tải APK từ Releases

**Hotfix:** Actions → Release → Run workflow → version `1.0.x`

---

## Chi tiết đầy đủ

[../learning/cicd-release.md](../learning/cicd-release.md)
