# 🍔 DFood - Food Delivery App

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4.svg?style=flat&logo=android)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-green.svg?style=flat)
![Hilt](https://img.shields.io/badge/Dependency%20Injection-Hilt-red.svg?style=flat)

> Đồ án môn học: Xây dựng ứng dụng đặt đồ ăn online toàn diện, bao gồm cả luồng dành cho Khách hàng và Quản lý Nhà hàng .

---

## 📖 Giới thiệu (Introduction)

**DFood** là một ứng dụng di động Android giúp kết nối những người yêu ẩm thực với các nhà hàng địa phương. Ứng dụng cung cấp trải nghiệm mượt mà từ việc tìm kiếm món ăn, đặt hàng, thanh toán trực tuyến đến theo dõi đơn hàng theo thời gian thực. 
Đặc biệt, ứng dụng tích hợp sẵn phân hệ dành cho Chủ nhà hàng để theo dõi doanh thu và quản lý thực đơn trực tiếp trên điện thoại.

Dự án này là phần **Frontend Android**, giao tiếp với Backend thông qua RESTful APIs.

---

## ✨ Tính năng nổi bật (Key Features)

### 👤 Dành cho Khách hàng (User App)
* **Xác thực an toàn:** Đăng nhập/Đăng ký, Quên mật khẩu, Xác thực OTP.
* **Khám phá ẩm thực:** Hiển thị danh mục món ăn, nhà hàng nổi bật, tìm kiếm và lọc theo đánh giá/giá cả.
* **Trải nghiệm mua sắm:** Xem chi tiết món ăn, tùy chọn topping, quản lý giỏ hàng thông minh.
* **Thanh toán & Giao hàng:** Tích hợp thẻ tín dụng, chọn địa chỉ giao hàng trên bản đồ, theo dõi trạng thái đơn hàng (Track Order).
* **Tương tác:** Đánh giá món ăn, nhắn tin trực tiếp với nhà hàng/tài xế.

### 🏪 Dành cho Nhà hàng (Vendor/Admin App)
* **Dashboard:** Thống kê doanh thu, hiển thị đơn hàng đang chạy (Running Orders).
* **Quản lý thực đơn:** Thêm, sửa, xóa món ăn, upload hình ảnh món ăn.
* **Ví điện tử (Wallet):** Theo dõi số dư, yêu cầu rút tiền về tài khoản ngân hàng.
* **Chăm sóc khách hàng:** Quản lý thông báo, trả lời tin nhắn và phản hồi đánh giá của khách.

---

## 🛠 Công nghệ sử dụng (Tech Stack)

Dự án được xây dựng hoàn toàn bằng các công nghệ và thư viện hiện đại nhất của Android:

* **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/)
* **Giao diện (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose).
* **Kiến trúc (Architecture):** Tách biệt logic theo chuẩn **MVVM** (Model - View - ViewModel) kết hợp Clean Architecture (Tầng Data, Domain, UI).
* **Định tuyến (Navigation):** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Gọi API (Network):** [Retrofit](https://square.github.io/retrofit/) & OkHttp3
* **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)

---

## 🚀 Cài đặt & chạy app (Setup)

### Yêu cầu

- Android Studio (Khuyến nghị Ladybug trở lên)
- JDK 17
- Máy Android hoặc Emulator (API 24+)
- Kết nối internet (app gọi Backend trên Azure)

### Các bước

1. **Clone repository**

   ```bash
   git clone <repo-url>
   cd FoodDeliveryFE
   ```

2. **Tạo file `local.properties`** (thư mục gốc project)

   - Copy từ [`local.properties.example`](local.properties.example)
   - Android Studio thường tự tạo `sdk.dir` khi mở project
   - Điền key Facebook / Google nếu dùng đăng nhập social:
     - `FACEBOOK_APP_ID`
     - `FACEBOOK_CLIENT_TOKEN`
     - `GOOGLE_WEB_CLIENT_ID`
   - Copy `app/google-services.json` từ Firebase (xin team lead — **không commit file này**)

3. **Backend API**

   - Mặc định app kết nối BE Azure (không cần chạy BE local):
     - `https://food-deliver-be-cnbggtg6e5a4gbf4.eastasia-01.azurewebsites.net/api/`
   - Muốn test BE trên máy: thêm vào `local.properties`:

     ```properties
     API_BASE_URL=http://10.0.2.2:4000/api/
     SOCKET_URL=http://10.0.2.2:4000
     ```

4. **Mở project & Sync Gradle**

   - **File → Open** → chọn thư mục `FoodDeliveryFE`
   - **File → Sync Project with Gradle Files**

5. **Chạy app**

   - Cắm USB (bật USB debugging) hoặc dùng Emulator
   - Run ▶ (build variant **debug**)
   - **Không cần** bật Backend local nếu dùng server Azure

---

## 📦 Phát hành APK (CI/CD)

App **DFood** phát hành dạng file APK cho team / demo — không qua Google Play. Release được tự động hóa qua **GitHub Actions**.

### Quy trình release tự động

1. Commit theo [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, …)
2. Mở PR → CI chạy unit test + build debug APK
3. Merge vào `main` → **release-please** tạo PR bump version + `CHANGELOG.md`
4. Merge PR release → tạo git tag `vX.Y.Z` → workflow **Release** build signed APK
5. Tải `DFood-vX.Y.Z.apk` tại [GitHub Releases](../../releases)

### Release thủ công (khẩn cấp)

GitHub → **Actions** → **Release** → **Run workflow** → nhập version (vd. `1.0.1`).

Hoặc push tag:

```bash
git tag v1.0.1
git push origin v1.0.1
```

### Cấu hình GitHub Secrets (một lần)

Tạo Environment `production` (Settings → Environments) và thêm secrets:

| Secret | Mô tả |
|--------|--------|
| `KEYSTORE_BASE64` | Keystore `.jks` encode base64: `base64 -w0 release.jks` |
| `KEYSTORE_PASSWORD` | Mật khẩu keystore |
| `KEY_ALIAS` | Alias key |
| `KEY_PASSWORD` | Mật khẩu key |
| `GOOGLE_SERVICES_JSON` | Toàn bộ nội dung `app/google-services.json` |
| `FACEBOOK_APP_ID` | Facebook App ID |
| `FACEBOOK_CLIENT_TOKEN` | Facebook Client Token |
| `GOOGLE_WEB_CLIENT_ID` | Google Web Client ID |

### Version

File `version.properties` (root project) là nguồn version cho local build:

```properties
VERSION_MAJOR=1
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=1
```

CI release dùng `versionName` từ git tag và `versionCode` từ GitHub run number (luôn tăng).

### Build local (fallback)

**Signed release APK:**

1. Điền signing trong `local.properties` (xem `local.properties.example`)
2. `./gradlew assembleRelease`
3. Output: `app/build/outputs/apk/release/app-release.apk`

**Debug APK (demo nhanh):**

- `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`

### Cài APK trên máy Android

1. Bật **Cài ứng dụng không rõ nguồn** (Install unknown apps) cho app tải file
2. Mở file APK → Cài đặt
3. Cần internet để dùng app

### Lưu ý bảo mật

- **Không commit:** `local.properties`, `google-services.json`, file keystore (`.jks`)
- **Luôn backup keystore** — mất keystore thì không update được app đã cài
- File `app/google-services.json.ci` chỉ dùng cho CI, không dùng cho production

### Workflows

| Workflow | Trigger | Mục đích |
|----------|---------|----------|
| `ci.yml` | PR / push `main`, `develop` | Unit test + debug APK |
| `release-please.yml` | Push `main` | Tự động bump version PR |
| `release.yml` | Tag `v*` / manual | Signed APK → GitHub Releases |
| `codeql.yml` | PR / weekly | Security scan |

---

## 📁 Cấu trúc thư mục (Folder Structure)

Project được chia theo Feature-based (gói theo tính năng) để dễ dàng làm việc nhóm và bảo trì:

```text
com.fooddelivery
│
├── di                          # Hilt Dependency Injection Modules
│   ├── AppModule.kt            
│   └── NetworkModule.kt       
│
├── data                        # Tầng Data (Xử lý dữ liệu)
│   ├── local            
│   │   ├── room                # Cấu hình Room Database 
│   │   └── datastore           # Lưu Token, state in app
│   ├── remote              
│   │   ├── api                 # Các interface Retrofit 
│   │   └── dto              
│   └── repository             
│       ├── AuthRepositoryImpl.kt
│       ├── FoodRepositoryImpl.kt
│       └── VendorRepositoryImpl.kt
│
├── domain                      # Tầng Domain 
│   ├── model                   # Các Model chuẩn dùng trong app (User, Food, Order)
│   └── repository              # Các interface định nghĩa hàm gọi dữ liệu
│       ├── AuthRepository.kt
│       ├── FoodRepository.kt
│       └── VendorRepository.kt
│
├── util                        # Tiện ích
│   ├── Constants.kt            # Hằng số 
│   ├── Extensions.kt           # Các hàm mở rộng (Modifier, String...)
│   └── Resource.kt             # Class Wrapper quản lý API state
│
└── ui                          # Tầng UI (Giao diện & State)
    ├── theme                   # Cấu hình Material Theme
    │   ├── Color.kt
    │   ├── Shape.kt
    │   ├── Theme.kt
    │   └── Type.kt
    │
    ├── navigations             # Cấu hình định tuyến (Navigation)
    │   ├── NavGraph.kt         # Nơi chứa NavHost và liên kết các route
    │   └── Screen.kt           # Sealed class
    │
    ├── components              # Các UI Component dùng chung
    │   ├── CustomButton.kt 
    │   ├── CustomTextField.kt
    │   └── FoodItemCard.kt
    │
    └── screens                
        │
        ├── onboarding          # Introduction (for first login)
        │   └── OnboardingScreen.kt
        │
        ├── auth                # authentication
        │   ├── login
        │   │   ├── LoginScreen.kt
        │   │   └── LoginViewModel.kt      
        │   ├── register
        │   │   ├── RegisterScreen.kt
        │   │   └── RegisterViewModel.kt
        │   ├── forgot_password
        │   │   ├── ForgotPasswordScreen.kt
        │   │   └── ForgotPasswordViewModel.kt
        │   └── verification
        │       ├── VerificationScreen.kt
        │       └── VerificationViewModel.kt
        │
        ├── home                # Trang chủ khách hàng
        │   ├── HomeScreen.kt
        │   ├── HomeViewModel.kt
        │   ├── search
        │   │   ├── SearchScreen.kt
        │   │   └── SearchViewModel.kt
        │   └── location
        │       ├── LocationScreen.kt
        │       └── LocationViewModel.kt
        │
        ├── food                # food details
        │   ├── FoodDetailScreen.kt
        │   └── FoodDetailViewModel.kt
        │
        ├── cart               
        │   ├── CartScreen.kt
        │   └── CartViewModel.kt
        │
        ├── checkout            # payment
        │   ├── PaymentScreen.kt
        │   ├── AddCardScreen.kt
        │   └── CheckoutViewModel.kt
        │
        ├── order               
        │   ├── MyOrdersScreen.kt
        │   ├── TrackOrderScreen.kt
        │   └── OrderViewModel.kt
        │
        ├── profile             
        │   ├── ProfileScreen.kt
        │   ├── EditProfileScreen.kt
        │   ├── address
        │   │   ├── MyAddressScreen.kt
        │   │   └── AddressViewModel.kt
        │   └── ProfileViewModel.kt
        │
        └── vendor              # Restaurant management
            ├── dashboard
            │   ├── DashboardScreen.kt
            │   └── DashboardViewModel.kt
            ├── food_management
            │   ├── MyFoodListScreen.kt
            │   ├── AddFoodScreen.kt
            │   └── FoodManagementViewModel.kt
            ├── wallet
            │   ├── WalletScreen.kt
            │   ├── WithdrawScreen.kt
            │   └── WalletViewModel.kt
            ├── reviews
            │   ├── ReviewsScreen.kt
            │   └── ReviewsViewModel.kt
            └── communications
                ├── notifications
                │   ├── NotificationsScreen.kt
                │   └── NotificationsViewModel.kt
                └── messages
                    ├── MessageListScreen.kt
                    └── MessageViewModel.kt
