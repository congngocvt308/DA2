# BÁO CÁO PHÂN TÍCH - THIẾT KẾ HƯỚNG ĐỐI TƯỢNG
# PHƯƠNG PHÁP OOAD/UML

**Hệ thống Báo thức Thông minh với Quiz và QR Code**

**Nền tảng:** Android (Kotlin)  
**Kiến trúc:** MVVM + Room Database + Jetpack Compose  
**Ngày:** Tháng 1/2026

---

## I. KHẢO SÁT VÀ YÊU CẦU (Tóm tắt)

### 1.1. Tổng quan

Hệ thống Báo thức Thông minh là ứng dụng Android giúp người dùng thức dậy hiệu quả bằng cách:
- Kết hợp **báo thức truyền thống** với **quiz/câu hỏi** và **QR code**
- Áp dụng **thuật toán SRS** (Spaced Repetition System) để tối ưu học tập
- Cung cấp **thống kê** và **gamification** để tạo động lực

### 1.2. Mục tiêu hệ thống

| Mục tiêu | Mô tả |
|----------|-------|
| **Chức năng** | Quản lý báo thức, quiz thông minh, QR code, thống kê |
| **Hiệu năng** | Báo thức chính xác (sai số < 1s), hoạt động ngay cả khi máy khóa |
| **Trải nghiệm** | Giao diện thân thiện, thao tác nhanh (<30s tạo báo thức) |
| **Động lực** | Gamification với streak, điểm số, wake-up score |

### 1.3. Phạm vi

**Bao gồm:** Quản lý Alarm/Topic/Question/QR, Xử lý báo thức reo, Quiz với SRS, Thống kê  
**Không bao gồm:** Cloud sync, Chia sẻ cộng đồng, Tích hợp IoT, AI sinh câu hỏi

---

## II. PHÂN TÍCH HƯỚNG ĐỐI TƯỢNG (OOA)

### 2.1. Danh sách Actor

| Mã | Actor | Vai trò | Mô tả |
|----|-------|---------|-------|
| **A1** | **Người dùng** | Chính | Sử dụng ứng dụng để tạo/quản lý báo thức, trả lời quiz, xem thống kê |
| **A2** | **Android AlarmManager** | Hệ thống | Kích hoạt báo thức đúng giờ thông qua broadcast intent |

### 2.2. Danh sách Use Case

**File sơ đồ:** [ooad_usecase.puml](diagrams/ooad_usecase.puml)

#### 2.2.1. Module Quản lý Báo thức

| Mã | Use Case | Actor | Mô tả ngắn |
|----|----------|-------|-----------|
| **UC01** | Tạo báo thức mới | A1 | Tạo báo thức với giờ/phút, nhãn, ngày lặp, nhạc, số câu hỏi, snooze |
| **UC02** | Chỉnh sửa báo thức | A1 | Sửa đổi thông tin báo thức đã tạo |
| **UC03** | Xóa báo thức | A1 | Xóa báo thức khỏi danh sách và hủy lịch hẹn hệ thống |
| **UC04** | Bật/Tắt báo thức | A1 | Toggle switch để bật/tắt báo thức |
| **UC05** | Xem danh sách báo thức | A1 | Hiển thị tất cả báo thức, sắp xếp, xem thời gian đổ chuông tiếp theo |
| **UC06** | Tạo báo thức nhanh | A1 | Tạo báo thức đổ chuông sau X phút (5/10/15/30) |

#### 2.2.2. Module Quản lý Chủ đề & Câu hỏi

| Mã | Use Case | Actor | Mô tả ngắn |
|----|----------|-------|-----------|
| **UC07** | Tạo chủ đề mới | A1 | Tạo chủ đề để nhóm các câu hỏi liên quan |
| **UC08** | Thêm câu hỏi vào chủ đề | A1 | Nhập câu hỏi, đáp án đúng, 3 đáp án sai |
| **UC09** | Chỉnh sửa câu hỏi | A1 | Sửa nội dung câu hỏi hoặc đáp án |
| **UC10** | Xóa câu hỏi | A1 | Xóa câu hỏi khỏi chủ đề |
| **UC11** | Xem chi tiết chủ đề | A1 | Xem danh sách câu hỏi trong chủ đề |
| **UC12** | Tìm kiếm chủ đề | A1 | Tìm chủ đề theo tên |

#### 2.2.3. Module Thực thi Báo thức

| Mã | Use Case | Actor | Mô tả ngắn |
|----|----------|-------|-----------|
| **UC13** | Nhận báo thức reo | A2 | Hệ thống gửi broadcast, ứng dụng nhận và khởi động service |
| **UC14** | Trả lời câu hỏi Quiz | A1 | Hiển thị câu hỏi, đếm giờ 15s, kiểm tra đáp án, cập nhật SRS |
| **UC15** | Quét QR Code để tắt | A1 | Mở camera, quét mã QR/Barcode, xác thực với mã đã chọn |
| **UC16** | Snooze báo thức | A1 | Hẹn reo lại sau X phút |
| **UC17** | Tắt báo thức | A1 | Dừng nhạc, cập nhật lịch sử, lập lại lịch (nếu lặp) |

#### 2.2.4. Module Quản lý QR Code

| Mã | Use Case | Actor | Mô tả ngắn |
|----|----------|-------|-----------|
| **UC18** | Quét và lưu mã QR | A1 | Quét QR/Barcode, đặt tên, lưu vào hệ thống (tối đa 5) |
| **UC19** | Liên kết QR với báo thức | A1 | Chọn tối đa 3 mã QR để dùng cho báo thức cụ thể |
| **UC20** | Xóa mã QR | A1 | Xóa mã QR khỏi hệ thống |

#### 2.2.5. Module Thống kê & Báo cáo

| Mã | Use Case | Actor | Mô tả ngắn |
|----|----------|-------|-----------|
| **UC21** | Xem thống kê tuần | A1 | Biểu đồ đường hiển thị tỷ lệ đúng 7 ngày gần nhất |
| **UC22** | Xem phân phối SRS | A1 | Biểu đồ tròn hiển thị số câu hỏi New/Learning/Mastered |
| **UC23** | Xem điểm Wake-up Score | A1 | Điểm hiệu suất thức dậy (0-100) dựa trên 5 lần gần nhất |
| **UC24** | Theo dõi Streak | A1 | Xem chuỗi ngày liên tiếp hoàn thành báo thức, kỷ lục |

#### 2.2.6. Use Case nội bộ (Internal)

| Tên | Mô tả |
|-----|-------|
| **Lập lịch hẹn hệ thống** | Đặt lịch với Android AlarmManager (include trong UC01, UC02, UC04) |
| **Chọn câu hỏi theo SRS** | Thuật toán chọn câu hỏi ưu tiên (include trong UC14) |
| **Cập nhật tiến độ học tập** | Cập nhật correctStreak, easinessFactor, interval (include trong UC14) |
| **Xác thực mã QR** | Kiểm tra mã quét có khớp không (include trong UC15, UC17) |

### 2.3. Đặc tả Use Case chi tiết

---

#### **UC01: Tạo báo thức mới**

**Mục tiêu:** Người dùng tạo báo thức mới với đầy đủ cấu hình

**Actor chính:** A1 (Người dùng)

**Tiền điều kiện:**
- Ứng dụng đã được cài đặt
- Đã cấp quyền: SCHEDULE_EXACT_ALARM, POST_NOTIFICATIONS

**Kích hoạt:** Người dùng nhấn nút "Thêm báo thức mới"

**Luồng chính:**
1. Hệ thống hiển thị màn hình cấu hình báo thức
2. Hệ thống điền giá trị mặc định (giờ/phút hiện tại, nhạc chuông mặc định, snooze: tắt)
3. Người dùng chỉnh sửa giờ/phút (Time Picker)
4. Người dùng nhập nhãn (Text Field - optional)
5. Người dùng chọn ngày lặp lại (Chip Selector: T2, T3... hoặc không chọn = 1 lần)
6. Người dùng chọn nhạc chuông (Ringtone Picker)
7. Người dùng chọn số câu hỏi (Slider: 0-10, mặc định 3)
8. [Tùy chọn] Người dùng nhấn "Chọn câu hỏi" → UC19 (Chọn Mission)
9. [Tùy chọn] Người dùng nhấn "Chọn QR" → UC19 (Chọn QR Code)
10. [Tùy chọn] Người dùng bật Snooze và chọn thời gian (Slider: 1-60 phút)
11. Hệ thống tính toán và hiển thị "Đổ chuông sau X giờ Y phút"
12. Người dùng nhấn "Lưu"
13. Hệ thống validate dữ liệu (giờ/phút hợp lệ)
14. Hệ thống lưu AlarmEntity vào database
15. Hệ thống lưu các liên kết (alarm_topic_link, alarm_selected_questions, alarm_qr_link)
16. Hệ thống gọi **Lập lịch hẹn hệ thống** (setAlarmClock)
17. Hệ thống hiển thị thông báo "Đã lưu báo thức"
18. Hệ thống quay về danh sách báo thức

**Luồng thay thế:**

**3a. Người dùng không thay đổi giờ/phút:**
- Hệ thống giữ giá trị mặc định (giờ hiện tại)

**8a. Người dùng chọn "Toàn bộ Topic":**
- Hệ thống lưu liên kết vào `alarm_topic_link`
- Tất cả câu hỏi trong Topic sẽ được dùng cho Quiz

**8b. Người dùng chọn "Câu hỏi lẻ":**
- Hệ thống lưu vào `alarm_selected_questions`
- Chỉ các câu hỏi được chọn mới dùng cho Quiz

**9a. Người dùng chưa có QR nào:**
- Hệ thống hiển thị "Chưa có mã QR. Quét ngay?"
- Nếu đồng ý → UC18 (Quét và lưu mã QR)

**12a. Người dùng nhấn "Hủy":**
- Nếu có thay đổi: Hiển thị dialog "Bỏ thay đổi?"
  - Nếu xác nhận: Quay về danh sách
  - Nếu không: Tiếp tục chỉnh sửa
- Nếu không có thay đổi: Quay về ngay

**13a. Validation lỗi:**
- Hiển thị thông báo lỗi (VD: "Giờ không hợp lệ")
- Quay lại bước 3

**16a. Thiếu quyền SCHEDULE_EXACT_ALARM (Android 12+):**
- Hiển thị dialog "Ứng dụng cần quyền đặt báo thức chính xác"
- Mở Settings để người dùng cấp quyền
- Sau khi quay lại, thử lập lịch lại

**Hậu điều kiện:**
- Báo thức mới được lưu vào database với `isEnabled = true`
- Lịch hẹn đã được đặt trong Android AlarmManager
- Người dùng thấy báo thức mới trong danh sách

**Dữ liệu sử dụng:**
- **Input:** hour, minute, label, daysOfWeek, questionCount, selectedTopics, selectedQuestions, selectedQRCodes, ringtoneUri, snoozeEnabled, snoozeDuration
- **Output:** AlarmEntity (alarmId, isEnabled=true), các bản ghi liên kết

**Tần suất sử dụng:** Cao (trung bình 2-3 lần/tuần)

---

#### **UC14: Trả lời câu hỏi Quiz**

**File Activity Diagram:** [ooad_activity_uc14.puml](diagrams/ooad_activity_uc14.puml)

**Mục tiêu:** Người dùng trả lời đủ số câu hỏi đúng để tắt báo thức

**Actor chính:** A1 (Người dùng)

**Tiền điều kiện:**
- Báo thức đang reo (UC13 đã kích hoạt)
- Báo thức có `questionCount > 0`
- Đã có câu hỏi được chọn (Topics hoặc Questions)

**Kích hoạt:** Người dùng nhấn "Tắt" trên màn hình báo thức reo → Hệ thống yêu cầu làm Quiz

**Luồng chính:**
1. Hệ thống đọc cấu hình báo thức (alarmId, questionCount)
2. Hệ thống tạo AlarmHistoryEntity (scheduledTime, firstRingTime, snoozeCount=0)
3. Hệ thống gọi **Chọn câu hỏi theo SRS** (QuestionAlgorithmManager.generateMissionQuestions)
   - Đọc danh sách câu hỏi đã chọn (manual + topics)
   - Đọc QuestionProgressEntity của từng câu
   - Tính điểm ưu tiên (câu chưa học: 500, câu đến hạn: 1000+, câu khác: difficultyScore)
   - Sắp xếp và chọn Top N câu
4. Hệ thống chuyển đổi sang QuestionData (với 4 đáp án đã shuffle)
5. Hệ thống khởi tạo: `correctCount = 0`, `targetCount = questionCount`
6. **[Loop] Cho đến khi correctCount >= targetCount:**
   - 6.1. Hệ thống hiển thị câu hỏi hiện tại
   - 6.2. Hệ thống bắt đầu đếm ngược 15 giây (timer progress bar)
   - 6.3. Người dùng xem câu hỏi và chọn 1 trong 4 đáp án
   - **[Race condition]:**
     - **6.3a. Người dùng chọn đáp án trước khi hết giờ:**
       - Hệ thống dừng timer
       - Tiếp tục bước 6.4
     - **6.3b. Hết 15 giây:**
       - Hệ thống đánh dấu timeout
       - `isCorrect = false`
       - Tiếp tục bước 6.4
   - 6.4. Hệ thống kiểm tra đáp án
   - 6.5. **NẾU đúng:**
     - Hiển thị "Đúng" (màu xanh)
     - `correctCount++`
     - Gọi **Cập nhật tiến độ học tập** (processAnswer):
       - `correctStreak++`
       - `easinessFactor += 0.1`
       - `interval = interval * easinessFactor` (hoặc 1 nếu lần đầu)
       - `nextReviewDate = NOW() + interval (days)`
     - Cập nhật TopicStats: `userEloScore += 10`
   - 6.6. **NẾU sai:**
     - Hiển thị "Sai" (màu đỏ)
     - Gọi **Cập nhật tiến độ học tập**:
       - `correctStreak = 0`
       - `easinessFactor -= 0.2` (min 1.3)
       - `interval = 1`
     - Cập nhật TopicStats: `userEloScore -= 5` (min 0)
   - 6.7. Hệ thống ghi HistoryEntity (questionId, isCorrect, answeredAt, timeToAnswerMs)
   - 6.8. Hệ thống chờ 1 giây (để người dùng thấy kết quả)
   - 6.9. **NẾU correctCount < targetCount:**
     - Chuyển sang câu hỏi tiếp theo (pool[index++])
     - Quay lại bước 6.1
7. **[End Loop]** Đã đủ số câu đúng
8. Hệ thống cập nhật AlarmHistory (dismissalTime = NOW(), isDismissed = true)
9. Hệ thống cập nhật UserStats (totalPoints += 10, currentStreak, totalAlarmsDismissed++)
10. Hệ thống dừng AlarmService (stop phát nhạc)
11. Hệ thống đóng màn hình Quiz
12. Hệ thống quay về màn hình chính

**Luồng thay thế:**

**1a. questionCount = 0:**
- Hệ thống bỏ qua Quiz, tắt báo thức ngay lập tức
- Tiếp tục bước 8

**3a. Không có câu hỏi nào (danh sách rỗng):**
- Hệ thống hiển thị "Không có câu hỏi. Đặt lại cấu hình."
- Tắt báo thức ngay lập tức
- Tiếp tục bước 8

**6.3c. Người dùng không tương tác và hết 15 giây nhiều lần:**
- `correctCount` không tăng → Quiz kéo dài vô hạn cho đến khi trả lời đúng đủ
- (Đây là design cố ý để buộc người dùng tỉnh táo)

**10a. Service bị kill bởi hệ thống (low memory):**
- Nhạc sẽ tự dừng
- Notification biến mất
- Người dùng vẫn hoàn thành Quiz, dữ liệu vẫn được lưu

**Ngoại lệ:**

**E1. Người dùng force-stop ứng dụng:**
- Service bị kill ngay lập tức
- AlarmHistory không được cập nhật (dismissalTime = null, isDismissed = false)
- Dữ liệu Progress đã lưu trước đó vẫn giữ nguyên

**E2. Máy tắt nguồn giữa chừng:**
- Tương tự E1, dữ liệu một phần bị mất

**Hậu điều kiện:**
- Báo thức đã tắt, nhạc dừng
- AlarmHistory có bản ghi đầy đủ (dismissalTime, isDismissed=true)
- Tiến độ SRS của các câu hỏi đã trả lời được cập nhật
- UserStats được cập nhật (điểm, streak)

**Dữ liệu sử dụng:**
- **Input:** alarmId, questionCount, danh sách QuestionEntity, QuestionProgressEntity
- **Output:** HistoryEntity (N bản ghi), QuestionProgressEntity (N bản ghi cập nhật), AlarmHistoryEntity (1 bản ghi), UserStatsEntity (cập nhật)

**Tần suất sử dụng:** Rất cao (mỗi lần báo thức reo)

---

#### **UC18: Quét và lưu mã QR**

**Mục tiêu:** Người dùng quét QR/Barcode và lưu vào hệ thống

**Actor chính:** A1 (Người dùng)

**Tiền điều kiện:**
- Đã cấp quyền CAMERA
- Số lượng QR đã lưu < 5

**Kích hoạt:** Người dùng nhấn "Quét mã mới" trong QRCodeManagementDialog

**Luồng chính:**
1. Hệ thống mở QRCodeScannerScreen
2. Hệ thống khởi động camera (CameraX)
3. Hệ thống hiển thị camera preview + khung hình quét + hướng dẫn
4. Người dùng đưa mã QR/Barcode vào khung hình
5. Hệ thống phân tích frame bằng ML Kit (BarcodeScanning)
6. ML Kit phát hiện barcode
7. Hệ thống trích xuất:
   - `codeValue`: Giá trị mã
   - `codeType`: "QR" hoặc "BARCODE"
8. Hệ thống dừng camera
9. Hệ thống hiển thị "Đã quét: [codeValue]"
10. Hệ thống yêu cầu người dùng đặt tên (Text Field)
11. Người dùng nhập tên (VD: "Mã tủ lạnh")
12. Người dùng nhấn "Lưu"
13. Hệ thống kiểm tra số lượng QR hiện tại (SELECT COUNT(*) FROM qr_codes)
14. **NẾU < 5:**
    - Hệ thống kiểm tra trùng lặp (SELECT * FROM qr_codes WHERE codeValue = ?)
    - **NẾU chưa tồn tại:**
      - Hệ thống lưu QRCodeEntity (name, codeValue, codeType, createdAt)
      - Hệ thống hiển thị "Đã lưu mã thành công"
      - Hệ thống quay về QRCodeManagementDialog
      - Danh sách QR được cập nhật (có mã mới)
15. Người dùng tick chọn mã vừa lưu
16. Người dùng nhấn "Xong"
17. Hệ thống cập nhật selectedQRCodeIds trong AlarmSettingsViewModel
18. Hệ thống quay về AlarmSettingsScreen
19. AlarmSettingsScreen hiển thị số QR đã chọn

**Luồng thay thế:**

**6a. Không phát hiện được mã (ảnh mờ, góc nghiêng):**
- Hệ thống tiếp tục quét (loop bước 5-6)
- Người dùng điều chỉnh vị trí/góc độ

**13a. Đã đủ 5 mã:**
- Hệ thống hiển thị "Bạn chỉ có thể lưu tối đa 5 mã. Vui lòng xóa bớt mã cũ."
- Quay về QRCodeManagementDialog
- Người dùng có thể xóa mã cũ (UC20) rồi quét lại

**14a. Mã đã tồn tại:**
- Hệ thống hiển thị "Mã này đã được lưu với tên \"[existingName]\""
- Người dùng có thể:
  - Nhấn "OK" → Quay về QRCodeManagementDialog
  - Chọn mã cũ đó thay vì quét mới

**12a. Người dùng nhấn "Hủy":**
- Hệ thống bỏ mã vừa quét
- Quay về QRCodeManagementDialog

**Ngoại lệ:**

**E1. Thiếu quyền Camera:**
- Hệ thống hiển thị "Ứng dụng cần quyền Camera để quét mã"
- Mở Settings để người dùng cấp quyền

**E2. Camera không hoạt động (lỗi phần cứng):**
- Hiển thị "Không thể mở camera. Vui lòng kiểm tra thiết bị."

**Hậu điều kiện:**
- Mã QR mới được lưu vào bảng qr_codes
- Mã QR đã được chọn cho báo thức hiện tại (trong memory, chưa lưu DB)

**Dữ liệu sử dụng:**
- **Input:** codeValue (từ ML Kit), codeType, name (từ người dùng)
- **Output:** QRCodeEntity (qrId, name, codeValue, codeType, createdAt)

**Tần suất sử dụng:** Trung bình (1-2 lần khi setup lần đầu)

---

#### **UC04: Bật/Tắt báo thức**

**File Sequence Diagram:** [ooad_sequence_uc04.puml](diagrams/ooad_sequence_uc04.puml)

**Mục tiêu:** Người dùng bật hoặc tắt báo thức nhanh chóng

**Actor chính:** A1 (Người dùng)

**Tiền điều kiện:**
- Đã có ít nhất 1 báo thức trong danh sách

**Kích hoạt:** Người dùng toggle switch trên AlarmCard

**Luồng chính:**
1. Người dùng nhấn switch của báo thức
2. Switch chuyển trạng thái (bật ↔ tắt)
3. Hệ thống gọi AlarmViewModel.toggleAlarm(alarmId, isEnabled)
4. AlarmViewModel đọc AlarmEntity từ database (getAlarmById)
5. AlarmViewModel cập nhật `alarm.isEnabled = isEnabled`
6. AlarmViewModel lưu lại database (updateAlarm)
7. **NẾU isEnabled = true (Bật):**
   - AlarmViewModel gọi AlarmScheduler.schedule(alarm)
   - AlarmScheduler tính thời gian đổ chuông tiếp theo
   - AlarmScheduler gọi AlarmManager.setAlarmClock(time, pendingIntent)
   - Hệ thống hiển thị switch màu xanh + "Báo thức sẽ reo sau X giờ Y phút"
8. **NẾU isEnabled = false (Tắt):**
   - AlarmViewModel gọi AlarmScheduler.cancel(alarm)
   - AlarmScheduler tạo PendingIntent với cùng requestCode (alarmId)
   - AlarmScheduler gọi AlarmManager.cancel(pendingIntent)
   - Hệ thống hiển thị switch màu xám

**Luồng thay thế:**

**7a. Thiếu quyền SCHEDULE_EXACT_ALARM:**
- Hiển thị dialog yêu cầu cấp quyền
- Mở Settings
- Sau khi quay lại, nếu đã cấp quyền → Thử lập lịch lại
- Nếu vẫn không cấp → Switch quay về trạng thái tắt, hiển thị lỗi

**Ngoại lệ:**
- Không có ngoại lệ đặc biệt (operation khá đơn giản)

**Hậu điều kiện:**
- Trạng thái `isEnabled` được cập nhật trong database
- Lịch hẹn hệ thống được đặt (nếu bật) hoặc hủy (nếu tắt)

**Dữ liệu sử dụng:**
- **Input:** alarmId, isEnabled (boolean)
- **Output:** AlarmEntity (cập nhật isEnabled)

**Tần suất sử dụng:** Rất cao (hàng ngày)

---

#### **UC21: Xem thống kê tuần**

**File Sequence Diagram:** [ooad_sequence_uc21.puml](diagrams/ooad_sequence_uc21.puml)

**Mục tiêu:** Người dùng xem biểu đồ độ chính xác trả lời 7 ngày gần nhất

**Actor chính:** A1 (Người dùng)

**Tiền điều kiện:**
- Người dùng đã trả lời ít nhất 1 câu hỏi (có dữ liệu trong bảng history)

**Kích hoạt:** Người dùng nhấn tab "Thống kê" trên Bottom Navigation

**Luồng chính:**
1. Người dùng nhấn tab "Thống kê"
2. MainScreen navigate đến StatsScreen
3. StatsScreen observe StatsViewModel.weeklyAccuracy
4. StatsViewModel gọi StatisticsDao.getWeeklyAccuracy(sevenDaysAgo)
5. StatisticsDao thực thi SQL query:
   ```sql
   SELECT date(answeredAt/1000, 'unixepoch', 'localtime') as day,
          SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correct,
          COUNT(*) as total
   FROM history
   WHERE answeredAt > (NOW() - 7 days)
   GROUP BY day
   ORDER BY day ASC
   ```
6. StatisticsDao trả về Flow<List<DailyStat>>
7. StatsViewModel xử lý dữ liệu:
   - Tạo list 7 ngày (từ 6 ngày trước đến hôm nay)
   - Với mỗi ngày:
     - NẾU có data: `accuracy = correct / total`
     - NẾU không có: `accuracy = 0`
   - Format nhãn: "Nay", "06", "05", "04", "03", "02", "01"
8. StatsViewModel emit Flow với List<Pair<String, Float>>
9. StatsScreen collect Flow
10. StatsScreen vẽ LineChart (biểu đồ đường) với 7 điểm
11. Người dùng xem biểu đồ

**Đồng thời, StatsScreen hiển thị thêm:**
- **SRS Distribution (Pie Chart):**
  - StatsViewModel gọi StatisticsDao.getSrsDistribution()
  - Query: GROUP BY status (New/Learning/Mastered) với COUNT(*)
  - Vẽ biểu đồ tròn 3 phần
- **Wake-up Score:**
  - StatsViewModel gọi calculateWakeUpPerformance()
  - Lấy 5 lần alarm_history gần nhất
  - Tính điểm trung bình: `100 - (snoozeCount*10) - (delayMinutes*0.5)`
  - Hiển thị điểm số (0-100)
- **User Stats:**
  - StatsViewModel observe StatisticsDao.getUserStats()
  - Hiển thị: Tổng điểm, Streak hiện tại, Streak tốt nhất, Tổng báo thức đã tắt

**Luồng thay thế:**

**5a. Không có dữ liệu history:**
- Query trả về list rỗng
- StatsViewModel trả về 7 ngày với accuracy = 0
- Biểu đồ hiển thị đường thẳng ở 0%
- Hiển thị "Chưa có dữ liệu. Hoàn thành Quiz đầu tiên để xem thống kê!"

**Hậu điều kiện:**
- Người dùng thấy biểu đồ và các chỉ số thống kê

**Dữ liệu sử dụng:**
- **Input:** sevenDaysAgo (timestamp)
- **Output:** List<DailyStat> (day, correct, total), List<SrsStat> (status, count), wakeUpScore, userStats

**Tần suất sử dụng:** Trung bình (2-3 lần/tuần để theo dõi tiến độ)

---

### 2.4. Activity Diagram (Biểu đồ Hoạt động)

Đã tạo Activity Diagram cho 2 use case phức tạp nhất:

1. **UC01 - Tạo báo thức mới:** [ooad_activity_uc01.puml](diagrams/ooad_activity_uc01.puml)
   - Bao gồm: Nhập thông tin, chọn Mission, chọn QR, validate, lưu DB, đặt lịch hẹn

2. **UC14 - Trả lời câu hỏi Quiz:** [ooad_activity_uc14.puml](diagrams/ooad_activity_uc14.puml)
   - Bao gồm: Chọn câu hỏi SRS, loop quiz, kiểm tra đáp án, cập nhật tiến độ, tính ELO

### 2.5. Domain Class Diagram (Biểu đồ Lớp Miền)

**File sơ đồ:** [ooad_domain_class.puml](diagrams/ooad_domain_class.puml)

**Các lớp khái niệm chính:**

| Lớp | Thuộc tính chính | Quan hệ |
|-----|------------------|---------|
| **Báo thức (Alarm)** | mã, giờ, phút, nhãn, ngày lặp lại, trạng thái, số câu hỏi, nhạc chuông, snooze | Chọn 0..* Topic, Chọn 0..* Question, Sử dụng 0..3 QRCode, Tạo 0..* AlarmHistory |
| **Chủ đề (Topic)** | mã chủ đề, tên chủ đề | Chứa 0..* Question, Có 0..1 TopicStats |
| **Câu hỏi (Question)** | mã, nội dung, đáp án, đáp án đúng | Có 0..1 Progress, Tạo 0..* History |
| **Mã QR (QRCode)** | mã, tên, giá trị, loại (QR/Barcode), thời gian tạo | Được dùng bởi 0..* Alarm |
| **Tiến độ học tập (Progress)** | correctStreak, nextReviewDate, easinessFactor, interval, difficultyScore | Thuộc về 1 Question |
| **Lịch sử trả lời (History)** | thời gian, kết quả, thời gian suy nghĩ | Liên kết 1 Question, Thuộc 0..1 AlarmHistory |
| **Lịch sử báo thức (AlarmHistory)** | thời gian hẹn, thời gian reo, thời gian tắt, số lần snooze | Liên kết 1 Alarm, Chứa 0..* History |
| **Thống kê người dùng (UserStats)** | tổng điểm, streak hiện tại, streak tốt nhất, tổng báo thức đã tắt | Thuộc 1 User |
| **Thống kê chủ đề (TopicStats)** | điểm ELO của user | Thuộc 1 Topic |
| **Người dùng (User)** | tên | Có 1 UserStats |

**Multiplicity (Bội số):**
- Alarm (1) - Topic (0..*): Báo thức có thể chọn nhiều Topic
- Alarm (1) - Question (0..*): Báo thức có thể chọn nhiều câu hỏi lẻ
- Alarm (1) - QRCode (0..3): Báo thức dùng tối đa 3 mã QR
- Topic (1) - Question (0..*): Topic chứa nhiều câu hỏi
- Question (1) - Progress (0..1): Mỗi câu hỏi có tối đa 1 bản ghi tiến độ
- Question (1) - History (0..*): Mỗi câu hỏi có nhiều lần trả lời

**Lưu ý:**
- Đây là mô hình khái niệm, chưa có method/operation
- Giới hạn 5 mã QR trong hệ thống, 3 mã/báo thức được kiểm tra trong code (không phải constraint DB)

---

## III. THIẾT KẾ HƯỚNG ĐỐI TƯỢNG (OOD)

### 3.1. Design Class Diagram (Biểu đồ Lớp Thiết kế)

**File sơ đồ:** [ooad_design_class.puml](diagrams/ooad_design_class.puml)

Mô hình thiết kế chi tiết với **MVVM Architecture + Clean Architecture**:

#### 3.1.1. UI Layer (Presentation)

**Screens (Composables):**

- **AlarmScreen**
  - Thuộc tính: `viewModel: AlarmViewModel`
  - Phương thức: `displayAlarmList()`, `onToggleAlarm(id, enabled)`, `onDeleteAlarm(id)`

- **AlarmSettingsScreen**
  - Thuộc tính: `viewModel: AlarmSettingsViewModel`
  - Phương thức: `displaySettings()`, `onSaveAlarm()`, `onSelectMission()`, `onSelectQR()`

- **QuizScreen**
  - Thuộc tính: `viewModel: QuizViewModel`
  - Phương thức: `displayQuestion()`, `onAnswerSelected(answerId)`, `showTimer()`

- **TopicScreen**
  - Thuộc tính: `viewModel: TopicViewModel`
  - Phương thức: `displayTopicList()`, `onAddTopic()`, `onSearchTopic(query)`

- **StatsScreen**
  - Thuộc tính: `viewModel: StatsViewModel`
  - Phương thức: `displayWeeklyChart()`, `displaySrsDistribution()`, `displayWakeUpScore()`

#### 3.1.2. ViewModel Layer

**ViewModels (Android ViewModel):**

- **AlarmViewModel**
  - Thuộc tính:
    - `alarmDao: AppDao`
    - `alarmScheduler: AlarmScheduler`
    - `alarms: StateFlow<List<AlarmData>>`
    - `timeUntilNextAlarms: StateFlow<String>`
  - Phương thức:
    - `toggleAlarm(id, enabled)`
    - `deleteAlarm(id)`
    - `findNextAlarmTime()`

- **AlarmSettingsViewModel**
  - Thuộc tính:
    - `alarmDao: AppDao`
    - `uiState: StateFlow<AlarmSettingData>`
  - Phương thức:
    - `loadAlarm()`: Đọc alarm từ DB
    - `saveAlarm()`: Lưu alarm + liên kết Topic/Question/QR
    - `updateMission(count, questions, topics)`
    - `updateSelectedQRCodes(qrIds)`

- **QuizViewModel**
  - Thuộc tính:
    - `dao: AppDao`
    - `algorithmManager: QuestionAlgorithmManager`
    - `uiState: StateFlow<QuizUiStateData>`
    - `timerJob: Job?`
  - Phương thức:
    - `setAlarmId(id)`: Khởi tạo quiz
    - `loadQuestionsForQuiz()`: Gọi thuật toán chọn câu hỏi
    - `onOptionSelected(answerId)`: Xử lý đáp án
    - `startTimer()`: Đếm ngược 15s

- **TopicViewModel**
  - Thuộc tính:
    - `topicDao: AppDao`
    - `filteredTopics: StateFlow<List<TopicData>>`
    - `searchQuery: MutableStateFlow<String>`
  - Phương thức:
    - `addNewTopic(name)`
    - `onSearchQueryChange(query)`: Filter danh sách

- **StatsViewModel**
  - Thuộc tính:
    - `statsDao: StatisticsDao`
    - `weeklyAccuracy: Flow<List<DailyStat>>`
    - `srsDistribution: Flow<List<SrsStat>>`
    - `wakeUpScore: StateFlow<Float>`
  - Phương thức:
    - `calculateWakeUpPerformance()`
    - `updatePerformanceAfterAlarm()`

- **QRCodeViewModel**
  - Thuộc tính:
    - `dao: AppDao`
    - `allQRCodes: StateFlow<List<QRCodeEntity>>`
    - `uiState: StateFlow<QRCodeUiState>`
  - Phương thức:
    - `saveQRCode(name, codeValue, codeType)`
    - `deleteQRCode(qrCode)`
    - `validateQRForAlarm(alarmId, code): Boolean`

#### 3.1.3. Logic Layer (Business Logic)

**Classes:**

- **AlarmScheduler**
  - Thuộc tính:
    - `context: Context`
    - `alarmManager: AlarmManager`
  - Phương thức:
    - `schedule(alarm: AlarmEntity)`: Đặt lịch với AlarmManager
    - `cancel(alarm: AlarmEntity)`: Hủy lịch
    - `createPendingIntent(alarm): PendingIntent`

- **AlarmReceiver** (BroadcastReceiver)
  - Phương thức:
    - `onReceive(context, intent)`: Nhận broadcast từ AlarmManager, khởi động AlarmService

- **AlarmService** (Foreground Service)
  - Thuộc tính:
    - `mediaPlayer: MediaPlayer`
    - `serviceScope: CoroutineScope`
  - Phương thức:
    - `onStartCommand()`: Khởi động service
    - `playAlarmSound(uri, volume)`: Phát nhạc
    - `createNotification(): Notification`: Tạo notification với fullScreenIntent

- **QuestionAlgorithmManager**
  - Thuộc tính:
    - `dao: AppDao`
  - Phương thức:
    - `generateMissionQuestions(alarmId, count): List<MissionQuestion>`: Chọn câu hỏi theo SRS
    - `processAnswer(questionId, isCorrect, timeMs)`: Cập nhật SRS và ELO
    - `calculateSRSPriority(progress): Double`: Tính điểm ưu tiên
    - `updateEloScore(topicId, isCorrect)`

#### 3.1.4. Data Layer (Repository/DAO)

**Interfaces:**

- **AppDao** (Room DAO)
  - Phương thức CRUD:
    - `getAllAlarms(): Flow<List<AlarmEntity>>`
    - `getAlarmById(id): AlarmEntity?`
    - `insertAlarm(alarm): Long`
    - `updateAlarm(alarm)`
    - `deleteAlarm(alarm)`
  - Phương thức Topic/Question:
    - `getAllTopics(): Flow<List<TopicEntity>>`
    - `getQuestionsByTopic(topicId): Flow<List<QuestionEntity>>`
    - `insertQuestion(question): Long`
  - Phương thức QR:
    - `getQRCodesForAlarm(alarmId): Flow<List<QRCodeEntity>>`
    - `isQRCodeValidForAlarm(alarmId, code): Boolean`
  - Phương thức SRS:
    - `getProgressForQuestions(questionIds): List<QuestionProgressEntity>`
    - `updateQuestionProgress(progress)`

- **StatisticsDao** (Room DAO)
  - Phương thức:
    - `getWeeklyAccuracy(sevenDaysAgo): Flow<List<DailyStat>>`
    - `getSrsDistribution(): Flow<List<SrsStat>>`
    - `getRecentAlarmHistory(): List<AlarmHistoryEntity>`
    - `getUserStats(): Flow<UserStatsEntity?>`

**Classes:**

- **AppDatabase** (Room Database)
  - Phương thức:
    - `{static} getDatabase(context): AppDatabase` (Singleton)
    - `appDao(): AppDao`
    - `statisticsDao(): StatisticsDao`

**Entities (Data Classes):**

- **AlarmEntity:** `alarmId, hour, minute, label, daysOfWeek, questionCount, isEnabled, ringtoneUri, snoozeDuration, snoozeEnabled`
- **TopicEntity:** `topicId, topicName`
- **QuestionEntity:** `questionId, ownerTopicId, prompt, options, correctAnswer`
- **QRCodeEntity:** `qrId, name, codeValue, codeType, createdAt`
- **QuestionProgressEntity:** `questionId, correctStreak, lastReviewedDate, nextReviewDate, difficultyScore, easinessFactor, interval`
- **HistoryEntity:** `historyId, questionId, alarmHistoryId, isCorrect, answeredAt, timeToAnswerMs`
- **AlarmHistoryEntity:** `historyId, alarmId, snoozeCount, scheduledTime, firstRingTime, dismissalTime, isDismissed`
- **UserStatsEntity:** `userId, totalPoints, currentStreak, bestStreak, totalAlarmsDismissed, lastActiveDate`
- **TopicStatsEntity:** `topicId, userEloScore`

#### 3.1.5. Quan hệ giữa các lớp

**UI → ViewModel:**
- AlarmScreen → AlarmViewModel (aggregation)
- QuizScreen → QuizViewModel (aggregation)

**ViewModel → Logic:**
- AlarmViewModel → AlarmScheduler (dependency)
- QuizViewModel → QuestionAlgorithmManager (dependency)

**ViewModel → Data:**
- AlarmViewModel → AppDao (dependency)
- StatsViewModel → StatisticsDao (dependency)

**Logic → Data:**
- QuestionAlgorithmManager → AppDao (dependency)
- AlarmReceiver → AppDao (dependency)

**Logic ↔ Logic:**
- AlarmReceiver → AlarmScheduler (dependency)
- AlarmReceiver → AlarmService (dependency)

**Database:**
- AppDatabase ..> AppDao (provides)
- AppDatabase ..> StatisticsDao (provides)

### 3.2. Sequence Diagram (Biểu đồ Tuần tự)

Đã tạo Sequence Diagram cho 6 use case quan trọng:

1. **UC01 - Tạo báo thức mới:** [ooad_sequence_uc01.puml](diagrams/ooad_sequence_uc01.puml)
   - Actors: User → AlarmScreen → AlarmSettingsScreen → AlarmSettingsViewModel → AppDao → AlarmScheduler → AlarmManager

2. **UC04 - Bật/Tắt báo thức:** [ooad_sequence_uc04.puml](diagrams/ooad_sequence_uc04.puml)
   - Actors: User → AlarmScreen → AlarmViewModel → AppDao → AlarmScheduler → AlarmManager

3. **UC08 - Thêm câu hỏi vào chủ đề:** [ooad_sequence_uc08.puml](diagrams/ooad_sequence_uc08.puml)
   - Actors: User → TopicScreen → TopicDetailScreen → TopicDetailViewModel → AppDao

4. **UC13+UC14 - Báo thức reo và Quiz:** [ooad_sequence_uc13_uc14.puml](diagrams/ooad_sequence_uc13_uc14.puml)
   - Actors: AlarmManager → AlarmReceiver → AlarmService → AlarmRingingActivity → QuizScreen → QuizViewModel → QuestionAlgorithmManager → AppDao
   - Luồng phức tạp nhất: Kích hoạt báo thức → Phát nhạc → Quiz → Cập nhật SRS

5. **UC18+UC19 - Quét và liên kết QR Code:** [ooad_sequence_uc18_uc19.puml](diagrams/ooad_sequence_uc18_uc19.puml)
   - Actors: User → AlarmSettingsScreen → QRCodeManagementDialog → QRCodeViewModel → QRCodeScannerScreen → CameraPreview → ML Kit → AppDao

6. **UC21 - Xem thống kê tuần:** [ooad_sequence_uc21.puml](diagrams/ooad_sequence_uc21.puml)
   - Actors: User → MainScreen → StatsScreen → StatsViewModel → StatisticsDao

**Đặc điểm chung:**
- Sử dụng Flow/StateFlow để reactive data (không cần polling)
- ViewModel quản lý lifecycle, tự động hủy job khi destroy
- Coroutine cho async operations (suspend functions)

### 3.3. Mapping Class ↔ Database (Ánh xạ lớp - bảng)

| Class (Entity) | Table | Mapping Type | Notes |
|----------------|-------|--------------|-------|
| **AlarmEntity** | `alarms` | 1:1 | Direct mapping, daysOfWeek lưu dạng Set<String> (Room TypeConverter) |
| **TopicEntity** | `topics` | 1:1 | Direct mapping |
| **QuestionEntity** | `questions` | 1:1 | options lưu dạng List<String> (TypeConverter JSON) |
| **QRCodeEntity** | `qr_codes` | 1:1 | Direct mapping |
| **QuestionProgressEntity** | `question_progress` | 1:1 | Date fields dùng TypeConverter (Date ↔ Long) |
| **TopicStatsEntity** | `topic_stats` | 1:1 | Direct mapping |
| **HistoryEntity** | `history` | 1:1 | Date field dùng TypeConverter |
| **AlarmHistoryEntity** | `alarm_history` | 1:1 | Date fields dùng TypeConverter |
| **UserStatsEntity** | `UserStats` | 1:1 | Direct mapping |
| **AlarmTopicLink** | `alarm_topic_link` | 1:1 | Junction table (many-to-many) |
| **AlarmSelectedQuestionEntity** | `alarm_selected_questions` | 1:1 | Link table |
| **AlarmQRLinkEntity** | `alarm_qr_link` | 1:1 | Junction table |

**TypeConverters:**

Room cần TypeConverter cho các kiểu dữ liệu phức tạp:

```kotlin
@TypeConverters(Converters::class)
class Converters {
    // Set<String> ↔ String (daysOfWeek: "T2,T3,T4")
    @TypeConverter
    fun fromStringSet(value: Set<String>): String {
        return value.joinToString(",")
    }
    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        return if (value.isEmpty()) emptySet() 
               else value.split(",").toSet()
    }
    
    // List<String> ↔ String JSON (options)
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
    
    // Date ↔ Long (timestamp)
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
```

**ViewModels không map trực tiếp vào DB:**
- ViewModels chỉ giữ UI state (ephemeral)
- Dữ liệu thật được lưu trong Entities
- VD: `AlarmSettingsViewModel.uiState` chứa `AlarmSettingData` (data class UI), khi save thì convert sang `AlarmEntity`

---

## IV. KẾT LUẬN VÀ ĐÁNH GIÁ

### 4.1. Tổng kết

Hệ thống Báo thức Thông minh đã được phân tích và thiết kế đầy đủ theo phương pháp OOAD/UML:

**Thành quả:**
- ✅ Use Case Diagram: 24 use case chính, 2 actors
- ✅ Đặc tả Use Case chi tiết: 6 use case quan trọng nhất (UC01, UC04, UC08, UC14, UC18, UC21)
- ✅ Activity Diagram: 2 luồng phức tạp (UC01, UC14)
- ✅ Domain Class Diagram: 10 lớp khái niệm + quan hệ
- ✅ Design Class Diagram: MVVM Architecture với 20+ classes
- ✅ Sequence Diagram: 6 luồng tương tác chi tiết
- ✅ Class-DB Mapping: 12 entities ↔ 12 tables

**Kiến trúc MVVM:**
- **Tách biệt rõ ràng:** UI (Compose) → ViewModel → Logic → Data
- **Reactive:** Sử dụng Flow/StateFlow để data tự động cập nhật
- **Testable:** ViewModel và Logic layer có thể test độc lập
- **Lifecycle-aware:** ViewModel tự động hủy job khi destroy

### 4.2. Điểm mạnh của thiết kế

1. **Tính module hóa cao:**
   - Mỗi feature có ViewModel riêng
   - Logic nghiệp vụ tách biệt (AlarmScheduler, QuestionAlgorithmManager)
   - Dễ dàng thêm tính năng mới (VD: PomodoroAlarm) mà không ảnh hưởng code cũ

2. **Thuật toán SRS tinh vi:**
   - `QuestionAlgorithmManager` đóng gói toàn bộ logic SRS
   - Dễ dàng thay đổi công thức (VD: từ SM-2 sang Anki algorithm) mà không ảnh hưởng UI

3. **Sử dụng Room Database hiệu quả:**
   - Flow giúp UI tự động cập nhật khi data thay đổi
   - Foreign Key cascade delete đảm bảo tính toàn vẹn dữ liệu
   - Query phức tạp (JOIN, GROUP BY) cho thống kê

4. **Xử lý báo thức chính xác:**
   - `AlarmManager.setAlarmClock()` đảm bảo đánh thức máy
   - Foreground Service với priority HIGH không bị kill
   - PendingIntent với requestCode = alarmId đảm bảo mỗi alarm độc lập

5. **UX tốt:**
   - Giao diện Material 3 hiện đại
   - Animation mượt mà (Compose)
   - Feedback ngay lập tức (đáp án đúng/sai màu xanh/đỏ)
   - Gamification tạo động lực (streak, điểm số)

### 4.3. Hạn chế và cải tiến

**Hạn chế:**

1. **Single-user only:**
   - UserStats có `userId = 1` cố định
   - Không hỗ trợ multi-profile (VD: gia đình dùng chung)

2. **Không có backup/restore:**
   - Nếu xóa ứng dụng → mất toàn bộ dữ liệu
   - Cần implement Export/Import (JSON file)

3. **SRS algorithm đơn giản:**
   - Chưa tính forgetting curve (đường cong quên)
   - Chưa tính độ khó câu hỏi (dựa vào % đúng trung bình)

4. **QR Code cố định:**
   - Nếu mã QR thay đổi (VD: in lại), phải quét lại
   - Có thể cải tiến bằng cách dùng NFC tag

5. **Thiếu accessibility:**
   - Chưa hỗ trợ TalkBack (người khiếm thị)
   - Chưa có chế độ contrast cao

**Cải tiến đề xuất:**

| Mức độ | Cải tiến | Mô tả |
|--------|----------|-------|
| **Cao** | Đồng bộ đám mây | Firebase Firestore để sync data giữa các thiết bị |
| **Cao** | Backup/Restore | Export data sang JSON/CSV, import khi cần |
| **Trung bình** | Multi-user | Hỗ trợ nhiều profile (gia đình, bạn cùng phòng) |
| **Trung bình** | Câu hỏi dạng hình ảnh | Thêm `imageUri` vào QuestionEntity |
| **Trung bình** | Text-to-Speech | Đọc câu hỏi bằng giọng nói (cho người sáng mê man) |
| **Thấp** | Widget | Hiển thị báo thức tiếp theo trên home screen |
| **Thấp** | Dark mode auto | Tự động chuyển theme theo giờ |

### 4.4. So sánh Structured vs OO Approach

| Tiêu chí | Phương pháp Có cấu trúc (SA/SD) | Phương pháp Hướng đối tượng (OOAD) |
|----------|----------------------------------|-------------------------------------|
| **Tập trung** | Luồng dữ liệu, tiến trình | Đối tượng, trách nhiệm, tương tác |
| **Sơ đồ chính** | DFD, ERD | Use Case, Class, Sequence |
| **Phù hợp** | Hệ thống data-intensive | Hệ thống phức tạp, tương tác nhiều |
| **Ưu điểm** | Dễ hiểu luồng xử lý, rõ ràng | Dễ mở rộng, maintainable, reusable |
| **Nhược điểm** | Khó maintain khi lớn | Cần hiểu OOP tốt |

**Kết luận:** Với hệ thống Báo thức Thông minh, **OOAD phù hợp hơn** vì:
- Nhiều đối tượng tương tác phức tạp (Alarm, Quiz, QR, Stats)
- Cần mở rộng liên tục (thêm loại câu hỏi, loại báo thức...)
- Android sử dụng OOP (Kotlin) → ánh xạ trực tiếp từ design sang code

Tuy nhiên, **DFD vẫn hữu ích** để hiểu luồng dữ liệu tổng thể (đặc biệt P5 - Thực thi Quiz).

### 4.5. Kết luận cuối cùng

Hệ thống Báo thức Thông minh đã được phân tích và thiết kế một cách toàn diện theo cả hai phương pháp:
- **Phương pháp Có cấu trúc (SA/SD):** Giúp hiểu rõ luồng dữ liệu, tiến trình xử lý, cấu trúc database
- **Phương pháp Hướng đối tượng (OOAD):** Thiết kế kiến trúc chi tiết, class diagram, interaction flows

Thiết kế này:
✅ **Đáp ứng đầy đủ yêu cầu chức năng** (30 FR)  
✅ **Đảm bảo yêu cầu phi chức năng** (hiệu năng, tin cậy, UX)  
✅ **Có khả năng mở rộng** (dễ thêm feature mới)  
✅ **Maintainable** (code rõ ràng, module hóa)  
✅ **Testable** (ViewModel, Logic tách biệt)

**Triển khai thực tế:** Code base hiện tại đã implement đầy đủ theo thiết kế này (MVVM + Room + Compose). Các sơ đồ UML phản ánh đúng kiến trúc thực tế của ứng dụng.

---

**PHỤ LỤC:**

**Sơ đồ UML:**
- [Use Case Diagram](diagrams/ooad_usecase.puml)
- [Activity Diagram - UC01](diagrams/ooad_activity_uc01.puml)
- [Activity Diagram - UC14](diagrams/ooad_activity_uc14.puml)
- [Domain Class Diagram](diagrams/ooad_domain_class.puml)
- [Design Class Diagram](diagrams/ooad_design_class.puml)
- [Sequence Diagram - UC01](diagrams/ooad_sequence_uc01.puml)
- [Sequence Diagram - UC04](diagrams/ooad_sequence_uc04.puml)
- [Sequence Diagram - UC08](diagrams/ooad_sequence_uc08.puml)
- [Sequence Diagram - UC13+UC14](diagrams/ooad_sequence_uc13_uc14.puml)
- [Sequence Diagram - UC18+UC19](diagrams/ooad_sequence_uc18_uc19.puml)
- [Sequence Diagram - UC21](diagrams/ooad_sequence_uc21.puml)

---

**KẾT THÚC BÁO CÁO PHÂN TÍCH - THIẾT KẾ HƯỚNG ĐỐI TƯỢNG**


