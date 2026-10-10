# Smart Alarm: Giải Pháp Thức Dậy Thông Minh & Tỉnh Táo ☀️

Ứng dụng báo thức Android giúp bạn không chỉ thức dậy đúng giờ mà còn khởi đầu ngày mới tỉnh táo và vui vẻ. Thay vì bấm "Dừng" một cách vô thức, bạn phải vượt qua một **thử thách** (trí tuệ, cảm xúc hoặc vận động) thì chuông mới tắt.

---

## 🚀 Các cách tắt báo thức

### 1. 🧠 Giải trắc nghiệm (Brain Quiz)
- Đọc câu hỏi và chọn đáp án đúng buộc não bộ hoạt động, giúp thoát khỏi trạng thái lờ đờ sau khi ngủ dậy.
- Thuật toán **SRS (Spaced Repetition System)** ưu tiên những câu bạn hay sai.
- Có giới hạn thời gian để kích thích sự tập trung.

### 2. 😊 Thử thách nụ cười (Smile Challenge)
- Dùng **ML Kit Face Detection** qua camera trước: bạn phải mở mắt và giữ nụ cười trong khoảng thời gian cài đặt.
- **Bảo tàng nụ cười:** tự động lưu khoảnh khắc cười tươi nhất vào bộ sưu tập; chạm vào ảnh để xem lớn, đặt tên hoặc chia sẻ.

### 3. 📱 Quét mã QR / Barcode
- Đăng ký trước một mã (ví dụ dán ở phòng tắm); khi chuông reo phải rời giường đi quét đúng mã mới tắt được.

---

## 🛠 Tính năng khác

| Tính năng | Mô tả |
|---|---|
| **Chủ đề & câu hỏi** | Tự tạo bộ câu hỏi, hỗ trợ công thức toán (KaTeX) |
| **Sinh câu hỏi bằng AI** | Tải tài liệu (PDF…) lên, máy chủ AI sinh câu hỏi trắc nghiệm theo chủ đề |
| **Thống kê** | Chuỗi ngày, tổng điểm, *Chỉ số tỉnh táo* (Wake-up Score), phân phối trí nhớ SRS, biểu đồ **Hiệu suất 7 ngày** (tỉ lệ đúng, mục tiêu 80%) |
| **Báo thức nhanh** | Đặt chuông sau N phút chỉ với một chạm |
| **Tuỳ chỉnh** | Âm thanh, âm lượng, lặp theo ngày, báo lại (snooze), ảnh nền riêng cho từng báo thức |

---

## 🎨 Giao diện

Giao diện theo phong cách **"Mental Health App"** của bộ quy tắc [UI UX Pro Max](https://uupm.cc/): *Neumorphism + Accessible & Ethical*, tông màu dịu để thức dậy nhẹ nhàng.

- **Màu:** đơn sắc đen / xám trung tính, nhấn xanh lá wellness `#059669`; nền sáng `#FAFAFA`.
- **Bề mặt nổi mềm (soft UI):** thẻ và nút trắng với hai lớp bóng mềm, lún nhẹ khi nhấn (150 ms, không nảy).
- **Chữ:** Lora (tiêu đề) + Raleway (nội dung), hỗ trợ đầy đủ tiếng Việt; số giờ luôn thẳng hàng.
- **Chuyển động tiết chế:** nền "thở" chậm theo nhịp 8 giây; tự đứng yên khi bật *Giảm chuyển động* trong cài đặt hệ thống.
- **Trợ năng:** chữ đạt độ tương phản ≥ 4.5:1, vùng chạm ≥ 48dp, trạng thái không chỉ thể hiện bằng màu, có mô tả cho TalkBack.

Token màu nằm trong `object Calm` (`theme/Theme.kt`); các component dùng chung nằm trong `ui/theme/components/` (`GlassCard`, `GlassButton`, `GlassAlertDialog`, `CalmSlider`…).

---

## 💻 Công nghệ

| Hạng mục | Thư viện |
|---|---|
| Ngôn ngữ / UI | Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose |
| Kiến trúc | MVVM, Kotlin Coroutines + Flow |
| Lưu trữ | Room 2.6 (KSP) |
| Camera & AI | CameraX 1.4, ML Kit Face Detection, ML Kit Barcode Scanning |
| Mạng | Retrofit 2 + OkHttp (gọi máy chủ AI sinh câu hỏi) |
| Ảnh | Coil |
| Công thức toán | KaTeX (trong `assets/katex`, hiển thị qua WebView) |

---

## 📂 Cấu trúc thư mục

```
app/src/main/java/com/example/myapplication/      (package com.cong.smartalarm)
├─ alarm_logic/        AlarmScheduler, AlarmReceiver, AlarmService, AlarmDismissHelper
├─ data/               Room: AppDatabase, Entities, DAO, model dữ liệu, AiApiService
├─ logic/              QuestionAlgorithmManager (thuật toán SRS)
├─ utils/              SoundPlayer, RingtoneUtils
└─ ui/theme/
   ├─ theme/           Theme.kt — bảng màu Calm, font Lora/Raleway
   ├─ components/      Component dùng chung (bề mặt nổi mềm, CalmSlider, …)
   ├─ navigation/      MainScreen (thanh điều hướng dưới), Screen
   ├─ alarm/           Danh sách, cài đặt, màn hình reo, quiz, nụ cười
   ├─ facedetection/   Nhiệm vụ khuôn mặt, Bảo tàng nụ cười
   ├─ qrcode/          Quản lý / quét mã QR
   ├─ topic/           Chủ đề, câu hỏi, sinh câu hỏi bằng AI
   ├─ mission/         Chọn nhiệm vụ cho báo thức
   └─ stats/           Thống kê
docs/                  Báo cáo OOAD, báo cáo chống giả mạo khuôn mặt, sơ đồ PlantUML
```

---

## ▶️ Chạy dự án

**Yêu cầu:** Android Studio (bản mới), JDK 17 (dùng JBR đi kèm Android Studio), thiết bị Android 8.0+ (minSdk 26, targetSdk 36).

1. Clone và mở thư mục dự án bằng Android Studio, chờ Gradle sync.
2. Chạy cấu hình `app` trên máy thật (nên dùng máy thật để thử camera và báo thức).
3. Dòng lệnh:
   ```bash
   ./gradlew :app:assembleDebug      # build APK debug
   ./gradlew :app:installDebug       # cài lên máy đang kết nối
   ```

**Máy chủ AI (tuỳ chọn):** tính năng sinh câu hỏi gọi tới máy chủ trong mạng nội bộ, địa chỉ đang đặt cứng `http://192.168.1.219:3000/` trong `TopicViewModel.kt` và `TopicDetailViewModel.kt`. Đổi thành IP máy chạy server của bạn; các tính năng khác hoạt động không cần mạng.

### Quyền ứng dụng

| Quyền | Dùng cho |
|---|---|
| `SCHEDULE_EXACT_ALARM`, `USE_FULL_SCREEN_INTENT`, `WAKE_LOCK` | Báo thức đúng giờ, hiện toàn màn hình khi khoá máy |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `VIBRATE` | Phát chuông và rung |
| `POST_NOTIFICATIONS` | Thông báo báo thức |
| `CAMERA` | Thử thách nụ cười, quét mã QR |
| `RECORD_AUDIO` | Thu âm lời nhắc cho thử thách nụ cười |
