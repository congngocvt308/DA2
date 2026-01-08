# BÁO CÁO PHÂN TÍCH - THIẾT KẾ HỆ THỐNG
# PHƯƠNG PHÁP CÓ CẤU TRÚC (SA/SD)

**Hệ thống Báo thức Thông minh với Quiz và QR Code**

**Nền tảng:** Android (Kotlin)  
**Kiến trúc:** MVVM + Room Database + Jetpack Compose  
**Ngày:** Tháng 1/2026

---

## I. KHẢO SÁT NGHIỆP VỤ VÀ YÊU CẦU

### 1.1. Tổng quan hệ thống

Hệ thống Báo thức Thông minh là ứng dụng di động Android nhằm giúp người dùng thức dậy hiệu quả bằng cách kết hợp:
- **Báo thức truyền thống** với các tính năng nâng cao (lặp lại, snooze, nhạc chuông tùy chỉnh)
- **Quiz/Câu hỏi bắt buộc** để tắt báo thức, giúp kích thích não bộ tỉnh táo
- **QR Code/Barcode** buộc người dùng phải đến vị trí cụ thể (tủ lạnh, phòng tắm...) để tắt
- **Thuật toán SRS (Spaced Repetition System)** để tối ưu việc ôn tập kiến thức
- **Thống kê và gamification** để tạo động lực duy trì thói quen

### 1.2. Phạm vi hệ thống

**Bao gồm:**
- Quản lý báo thức (CRUD, lập lịch, bật/tắt)
- Quản lý chủ đề và câu hỏi (CRUD, tìm kiếm)
- Quản lý mã QR/Barcode (quét, lưu, liên kết với báo thức)
- Xử lý báo thức reo (phát nhạc, hiển thị giao diện toàn màn hình)
- Thực thi Quiz với thuật toán chọn câu hỏi thông minh (SRS)
- Thống kê hiệu suất (độ chính xác, phân phối SRS, wake-up score, streak)

**Không bao gồm:**
- Đồng bộ đám mây (cloud sync)
- Chia sẻ câu hỏi với người dùng khác
- Tích hợp với thiết bị IoT bên ngoài
- AI sinh câu hỏi tự động

### 1.3. Yêu cầu chức năng (Functional Requirements)

| Mã | Yêu cầu | Mô tả ngắn |
|----|---------|------------|
| FR-01 | Tạo báo thức | Cho phép người dùng tạo báo thức mới với giờ/phút, nhãn, ngày lặp lại, nhạc chuông, số câu hỏi, cấu hình snooze |
| FR-02 | Chỉnh sửa báo thức | Cho phép sửa đổi thông tin báo thức đã tạo |
| FR-03 | Xóa báo thức | Xóa báo thức khỏi danh sách |
| FR-04 | Bật/Tắt báo thức | Toggle trạng thái hoạt động của báo thức |
| FR-05 | Lập lịch hệ thống | Đặt lịch hẹn với Android AlarmManager |
| FR-06 | Tạo báo thức nhanh | Tạo báo thức đổ chuông sau X phút |
| FR-07 | Quản lý chủ đề | CRUD chủ đề câu hỏi |
| FR-08 | Quản lý câu hỏi | CRUD câu hỏi trong chủ đề |
| FR-09 | Tìm kiếm chủ đề | Tìm kiếm theo tên |
| FR-10 | Chọn câu hỏi cho báo thức | Chọn toàn bộ chủ đề hoặc câu hỏi lẻ |
| FR-11 | Quét QR/Barcode | Sử dụng camera để quét mã |
| FR-12 | Lưu mã QR | Lưu tối đa 5 mã vào hệ thống |
| FR-13 | Liên kết QR với báo thức | Mỗi báo thức có thể dùng tối đa 3 mã |
| FR-14 | Kích hoạt báo thức | Nhận broadcast từ AlarmManager và khởi động service |
| FR-15 | Phát nhạc chuông | Phát nhạc liên tục cho đến khi tắt |
| FR-16 | Hiển thị màn hình reo | Full-screen notification hiển thị ngay cả khi khóa màn hình |
| FR-17 | Chọn câu hỏi theo SRS | Thuật toán ưu tiên câu hỏi đến hạn ôn tập |
| FR-18 | Trả lời Quiz | Hiển thị câu hỏi, đếm ngược 15s, kiểm tra đáp án |
| FR-19 | Cập nhật tiến độ SRS | Cập nhật correctStreak, easinessFactor, interval |
| FR-20 | Tính điểm ELO chủ đề | Tăng/giảm điểm ELO dựa trên kết quả trả lời |
| FR-21 | Xác thực QR để tắt | Kiểm tra mã quét có khớp với mã đã chọn không |
| FR-22 | Snooze báo thức | Hẹn reo lại sau X phút |
| FR-23 | Tắt báo thức | Dừng service, cập nhật lịch sử |
| FR-24 | Xem thống kê tuần | Biểu đồ đường độ chính xác 7 ngày |
| FR-25 | Xem phân phối SRS | Biểu đồ tròn trạng thái học tập (New/Learning/Mastered) |
| FR-26 | Tính Wake-up Score | Điểm hiệu suất dựa trên 5 lần tắt báo thức gần nhất |
| FR-27 | Theo dõi Streak | Chuỗi ngày liên tiếp hoàn thành báo thức |
| FR-28 | Ghi lịch sử trả lời | Lưu từng câu trả lời (đúng/sai, thời gian) |
| FR-29 | Ghi lịch sử báo thức | Lưu thông tin reo/tắt/snooze của từng lần báo thức |
| FR-30 | Cập nhật User Stats | Tổng điểm, streak, tổng báo thức đã tắt |

### 1.4. Yêu cầu phi chức năng (Non-Functional Requirements)

| Mã | Yêu cầu | Chi tiết |
|----|---------|----------|
| NFR-01 | Hiệu năng | Báo thức phải đổ chuông chính xác (sai số < 1 giây) |
| NFR-02 | Độ tin cậy | Báo thức vẫn hoạt động khi máy khóa màn hình hoặc tắt màn hình |
| NFR-03 | Khả năng sử dụng | Giao diện thân thiện, thao tác tạo báo thức < 30 giây |
| NFR-04 | Bảo mật | Dữ liệu lưu local, không chia sẻ ra ngoài |
| NFR-05 | Tương thích | Hỗ trợ Android 8.0 (API 26) trở lên |
| NFR-06 | Khả năng mở rộng | Dễ dàng thêm loại câu hỏi mới (hình ảnh, audio) |
| NFR-07 | Tài nguyên | Tiêu thụ < 50MB RAM khi chạy nền |

### 1.5. Tác nhân (Actors)

| Mã | Tác nhân | Vai trò |
|----|----------|---------|
| E1 | Người dùng | Sử dụng ứng dụng, tạo/quản lý báo thức, trả lời quiz, xem thống kê |
| E2 | Android AlarmManager | Hệ thống Android, kích hoạt báo thức đúng giờ |

### 1.6. Giả định và ràng buộc

**Giả định:**
1. Người dùng luôn cấp quyền Camera, Notification, và Exact Alarm cho ứng dụng
2. Thiết bị có kết nối mạng khi cài đặt (để tải thư viện ML Kit cho QR scanner)
3. Người dùng tự chịu trách nhiệm tạo câu hỏi phù hợp (không có kiểm duyệt nội dung)

**Ràng buộc:**
1. Tối đa 5 mã QR/Barcode trong hệ thống
2. Mỗi báo thức chỉ dùng tối đa 3 mã QR
3. Thời gian đếm ngược mỗi câu hỏi: 15 giây (cố định)
4. Snooze duration: 1-60 phút

---

## II. PHÂN TÍCH CÓ CẤU TRÚC

### 2.1. Sơ đồ Phân rã Chức năng (FDD)

**File sơ đồ:** [structured_fdd.mmd](diagrams/structured_fdd.mmd)

Hệ thống được phân rã thành 5 module chính:

1. **Quản lý Báo thức** (6 chức năng con)
   - Tạo mới, chỉnh sửa, xóa, bật/tắt, xem danh sách, tạo nhanh

2. **Quản lý Chủ đề & Câu hỏi** (8 chức năng con)
   - Quản lý chủ đề: tạo, sửa, xóa, tìm kiếm
   - Quản lý câu hỏi: thêm, sửa, xóa, xem chi tiết

3. **Thực thi Báo thức** (6 chức năng chính, trong đó Quiz có 4 chức năng con)
   - Kích hoạt, hiển thị giao diện reo, thực hiện Quiz, quét QR, snooze, tắt
   - Quiz: chọn câu hỏi theo SRS, hiển thị & đếm giờ, kiểm tra đáp án, cập nhật tiến độ

4. **Quản lý QR Code** (5 chức năng con)
   - Quét, lưu, xóa, liên kết với báo thức, xác thực

5. **Thống kê & Báo cáo** (5 chức năng con)
   - Độ chính xác tuần, phân phối SRS, Wake-up Score, Streak, lịch sử

### 2.2. Sơ đồ Luồng Dữ liệu Ngữ cảnh (DFD Context)

**File sơ đồ:** [structured_dfd_context.mmd](diagrams/structured_dfd_context.mmd)

**Mô tả:**
- Hệ thống nằm ở trung tâm, tương tác với 2 tác nhân ngoài:
  - **E1 (Người dùng):** Gửi thông tin báo thức, chủ đề, câu hỏi, mã QR, lệnh điều khiển, đáp án quiz → Nhận danh sách, thông báo, câu hỏi, kết quả, báo cáo
  - **E2 (Android AlarmManager):** Nhận lịch hẹn từ hệ thống → Gửi sự kiện đổ chuông

### 2.3. Sơ đồ Luồng Dữ liệu Mức 0 (DFD Level 0)

**File sơ đồ:** [structured_dfd_level0.mmd](diagrams/structured_dfd_level0.mmd)

**7 Tiến trình chính:**

| Mã | Tiến trình | Mô tả |
|----|------------|-------|
| P1 | Quản lý Báo thức | Nhận thông tin từ người dùng, lưu/sửa/xóa vào DS1, gửi yêu cầu lập lịch đến P3 |
| P2 | Quản lý Chủ đề & Câu hỏi | Nhận chủ đề/câu hỏi từ người dùng, lưu vào DS2 |
| P3 | Lập lịch Báo thức | Đọc cấu hình từ DS1, đặt lịch hẹn với E2 (AlarmManager) |
| P4 | Xử lý Báo thức Reo | Nhận sự kiện từ E2, đọc DS1, khởi động P5 (Quiz) hoặc P6 (QR), ghi lịch sử vào DS4 |
| P5 | Thực thi Quiz | Đọc câu hỏi từ DS2, đọc tiến độ từ DS4, hiển thị câu hỏi, nhận đáp án, cập nhật tiến độ SRS vào DS4 và DS5 |
| P6 | Quản lý QR Code | Lưu/xóa mã QR vào DS3, liên kết với báo thức (DS1), xác thực mã khi tắt |
| P7 | Tạo báo cáo Thống kê | Đọc DS4 (history) và DS5 (stats), tạo báo cáo gửi người dùng |

**5 Kho dữ liệu:**

| Mã | Kho dữ liệu | Nội dung |
|----|-------------|----------|
| DS1 | Alarms | Thông tin báo thức (giờ, phút, nhãn, daysOfWeek, isEnabled, questionCount, ringtone, snooze...) + liên kết với Topics/Questions/QR |
| DS2 | Topics & Questions | Chủ đề (topicName), Câu hỏi (prompt, options, correctAnswer, ownerTopicId) |
| DS3 | QR Codes | Mã QR/Barcode (name, codeValue, codeType) + liên kết với Alarms |
| DS4 | Progress & History | QuestionProgress (correctStreak, easinessFactor, interval, difficultyScore, nextReviewDate), History (isCorrect, answeredAt, timeToAnswerMs), AlarmHistory (snoozeCount, scheduledTime, dismissalTime) |
| DS5 | User Stats | UserStats (totalPoints, currentStreak, bestStreak, totalAlarmsDismissed), TopicStats (userEloScore) |

### 2.4. Sơ đồ Luồng Dữ liệu Mức 1 (DFD Level 1)

#### 2.4.1. Phân rá P4 - Xử lý Báo thức Reo

**File sơ đồ:** [structured_dfd_level1_p4.mmd](diagrams/structured_dfd_level1_p4.mmd)

**7 tiến trình con:**

- **P4.1 - Nhận sự kiện đổ chuông:** Nhận broadcast từ E2, đọc alarm từ DS1
- **P4.2 - Khởi chạy Alarm Service:** Phát nhạc chuông, tạo notification
- **P4.3 - Hiển thị màn hình reo:** Hiển thị giao diện full-screen, tạo AlarmHistory
- **P4.4 - Xử lý Snooze:** Cập nhật snooze count vào DS4, đặt lịch reo lại với E2
- **P4.5 - Kiểm tra điều kiện tắt:** Đọc cấu hình QR từ DS3, yêu cầu quét QR (nếu có), khởi động P5 (Quiz), kiểm tra hoàn thành
- **P4.6 - Lập lại lịch lặp lại:** Đọc daysOfWeek từ DS1, đặt lịch tiếp theo với E2 (nếu là báo thức lặp) hoặc tắt (nếu 1 lần)
- **P4.7 - Ghi lịch sử báo thức:** Ghi AlarmHistory vào DS4 (scheduledTime, firstRingTime, dismissalTime, snoozeCount, isDismissed)

#### 2.4.2. Phân rã P5 - Thực thi Quiz

**File sơ đồ:** [structured_dfd_level1_p5.mmd](diagrams/structured_dfd_level1_p5.mmd)

**5 tiến trình con:**

- **P5.1 - Chọn câu hỏi theo SRS:** Đọc câu hỏi đã chọn từ DS2, đọc tiến độ từ DS4, tính điểm ưu tiên (câu chưa học: 500, câu đến hạn: 1000+, câu khác: dùng difficultyScore), sắp xếp và chọn Top N, lưu vào DS_TEMP (bộ nhớ tạm)
- **P5.2 - Hiển thị câu hỏi & Đếm giờ:** Lấy câu hỏi từ DS_TEMP, hiển thị cho E1, bắt đầu timer 15s
- **P5.3 - Kiểm tra đáp án:** Nhận đáp án từ E1, so sánh với đáp án đúng, hiển thị kết quả, gửi thông tin (isCorrect, timeSpent) cho P5.4
- **P5.4 - Cập nhật tiến độ SRS:** Đọc tiến độ cũ từ DS4, tính toán SRS mới (nếu đúng: tăng streak/easiness/interval, nếu sai: reset streak=0, giảm easiness, interval=1), ghi lại DS4, ghi History vào DS4
- **P5.5 - Tính điểm ELO Topic:** Nhận Question ID từ P5.4, đọc Topic Stats từ DS5, cập nhật điểm ELO (+10 nếu đúng, -5 nếu sai)

**Kho dữ liệu tạm:** DS_TEMP chứa danh sách câu hỏi đã chọn và sắp xếp cho phiên Quiz hiện tại

#### 2.4.3. Phân rã P1 - Quản lý Báo thức (TBD)

Do số lượng giới hạn, chỉ phân rã 2 tiến trình quan trọng nhất (P4 và P5). Tiến trình P1 tương đối đơn giản:
- P1.1: Nhận thông tin báo thức
- P1.2: Validate dữ liệu
- P1.3: Lưu/Cập nhật vào DS1
- P1.4: Gửi yêu cầu lập lịch cho P3

### 2.5. Data Dictionary (Từ điển Dữ liệu)

#### 2.5.1. Luồng dữ liệu (Data Flows)

| Tên luồng | Nguồn | Đích | Mô tả | Thành phần |
|-----------|-------|------|-------|-----------|
| Thông tin báo thức | E1 | P1 | Dữ liệu tạo/sửa báo thức | hour + minute + label + daysOfWeek + questionCount + ringtoneUri + snoozeEnabled + snoozeDuration |
| Lịch hẹn | P3 | E2 | Yêu cầu đặt báo thức hệ thống | alarmId + triggerTime (Unix timestamp) + PendingIntent |
| Sự kiện đổ chuông | E2 | P4 | Thông báo báo thức reo | alarmId + label + ringtoneUri |
| Câu hỏi quiz | P5 | E1 | Hiển thị câu hỏi | questionId + prompt + List<options> + correctAnswer + timerProgress |
| Đáp án người dùng | E1 | P5 | Câu trả lời | answerId + timeSpent (ms) |
| Mã QR | E1 | P6 | Mã vừa quét | codeValue + codeType (QR/BARCODE) + name |
| Báo cáo thống kê | P7 | E1 | Dữ liệu biểu đồ | weeklyAccuracy + srsDistribution + wakeUpScore + userStats |

#### 2.5.2. Kho dữ liệu (Data Stores)

##### DS1: Alarms

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| alarmId | INT | PK, Auto-increment | Mã định danh báo thức |
| hour | INT | 0-23 | Giờ (24h format) |
| minute | INT | 0-59 | Phút |
| label | VARCHAR(100) | Nullable | Nhãn báo thức |
| daysOfWeek | SET | {"T2","T3","T4","T5","T6","T7","CN"} | Ngày lặp lại. Rỗng = 1 lần |
| questionCount | INT | 0-100 | Số câu hỏi bắt buộc |
| isEnabled | BOOLEAN | Default TRUE | Trạng thái bật/tắt |
| ringtoneUri | VARCHAR(255) | Nullable | Đường dẫn file nhạc |
| snoozeDuration | INT | 1-60 | Thời gian snooze (phút) |
| snoozeEnabled | BOOLEAN | Default FALSE | Cho phép snooze |

**Bảng liên kết:**
- `alarm_topic_link` (alarmId, topicId): Báo thức chọn toàn bộ Topic
- `alarm_selected_questions` (alarmId, questionId): Báo thức chọn câu hỏi lẻ
- `alarm_qr_link` (alarmId, qrId): Báo thức sử dụng QR Code

##### DS2: Topics & Questions

**Bảng Topics:**

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| topicId | INT | PK, Auto-increment | Mã chủ đề |
| topicName | VARCHAR(100) | NOT NULL, Unique | Tên chủ đề |

**Bảng Questions:**

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| questionId | INT | PK, Auto-increment | Mã câu hỏi |
| ownerTopicId | INT | FK → topics(topicId) | Chủ đề sở hữu |
| prompt | TEXT | NOT NULL | Nội dung câu hỏi |
| options | JSON | List<String> (3 đáp án sai) | Các đáp án sai |
| correctAnswer | VARCHAR(255) | NOT NULL | Đáp án đúng |

##### DS3: QR Codes

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| qrId | INT | PK, Auto-increment | Mã QR |
| name | VARCHAR(50) | NOT NULL | Tên do người dùng đặt |
| codeValue | VARCHAR(255) | NOT NULL, Unique | Giá trị mã QR/Barcode |
| codeType | ENUM | {"QR", "BARCODE"} | Loại mã |
| createdAt | BIGINT | Unix timestamp | Thời gian tạo |

**Ràng buộc nghiệp vụ:**
- Tối đa 5 mã trong hệ thống
- Mỗi báo thức dùng tối đa 3 mã (kiểm tra trong code)

##### DS4: Progress & History

**Bảng QuestionProgress:**

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| questionId | INT | PK, FK → questions | Mã câu hỏi |
| correctStreak | INT | Default 0 | Số lần đúng liên tiếp |
| lastReviewedDate | DATE | Nullable | Ngày ôn cuối |
| nextReviewDate | DATE | Nullable | Ngày ôn tiếp theo |
| difficultyScore | DOUBLE | Default 1000.0 | Điểm độ khó (dùng cho thuật toán) |
| easinessFactor | DOUBLE | Default 2.5, Min 1.3 | Hệ số dễ dàng (SM-2 algorithm) |
| interval | INT | Default 0 | Khoảng cách ngày cho lần ôn tập tới |

**Bảng History:**

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| historyId | INT | PK, Auto-increment | Mã lịch sử |
| questionId | INT | FK → questions | Câu hỏi nào |
| alarmHistoryId | INT | FK → alarm_history, Nullable | Liên kết với lần reo báo thức (null nếu luyện tập tự do) |
| isCorrect | BOOLEAN | NOT NULL | Đúng hay sai |
| answeredAt | DATETIME | NOT NULL | Thời gian trả lời |
| timeToAnswerMs | INT | NOT NULL | Thời gian suy nghĩ (ms) |

**Bảng AlarmHistory:**

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| historyId | INT | PK, Auto-increment | Mã lịch sử báo thức |
| alarmId | INT | FK → alarms | Báo thức nào |
| snoozeCount | INT | Default 0 | Số lần snooze |
| scheduledTime | DATETIME | NOT NULL | Thời gian hẹn ban đầu |
| firstRingTime | DATETIME | NOT NULL | Thời gian reo thực tế |
| dismissalTime | DATETIME | Nullable | Thời gian tắt (null nếu chưa tắt) |
| isDismissed | BOOLEAN | Default FALSE | Đã tắt chưa |

##### DS5: User Stats

**Bảng UserStats:**

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| userId | INT | PK | Mã người dùng (luôn = 1) |
| totalPoints | INT | Default 0 | Tổng điểm tích lũy |
| currentStreak | INT | Default 0 | Chuỗi ngày liên tiếp hiện tại |
| bestStreak | INT | Default 0 | Kỷ lục chuỗi ngày |
| totalAlarmsDismissed | INT | Default 0 | Tổng số báo thức đã tắt |
| lastActiveDate | BIGINT | Unix timestamp | Ngày hoạt động cuối |

**Bảng TopicStats:**

| Thuộc tính | Kiểu | Ràng buộc | Mô tả |
|------------|------|-----------|-------|
| topicId | INT | PK, FK → topics | Mã chủ đề |
| userEloScore | DOUBLE | Default 1000.0 | Điểm ELO của người dùng với chủ đề này |

### 2.6. Mini-spec (Process Specification)

#### Mini-spec P5.1: Chọn câu hỏi theo SRS

**Mục tiêu:** Chọn N câu hỏi phù hợp nhất cho Quiz dựa trên thuật toán Spaced Repetition System

**Input:**
- `alarmId`: Mã báo thức
- `countNeeded`: Số câu hỏi cần chọn
- DS2: Danh sách câu hỏi của các Topics/Questions đã chọn
- DS4: Tiến độ học tập (QuestionProgress) của từng câu

**Output:**
- DS_TEMP: Danh sách câu hỏi đã sắp xếp theo thứ tự ưu tiên

**Xử lý:**
```
1. Đọc alarm_selected_questions WHERE alarmId = alarmId
2. Đọc alarm_topic_link WHERE alarmId = alarmId
3. Lấy tất cả câu hỏi từ:
   - Câu hỏi lẻ (manual selection)
   - Câu hỏi từ các Topic đã chọn full
4. Loại bỏ trùng lặp (dựa trên questionId)
5. NẾU danh sách rỗng:
   - Trả về danh sách rỗng (không có câu hỏi)
6. Đọc question_progress cho tất cả questionId
7. Với mỗi câu hỏi, tính điểm ưu tiên:
   - NẾU chưa có progress (câu mới):
     - priority = 500
   - NẾU có progress:
     - NẾU nextReviewDate <= now (đã đến hạn ôn):
       - priority = 1000 + (now - nextReviewDate)  // Càng quá hạn càng ưu tiên
     - NGƯỢC LẠI:
       - priority = difficultyScore  // Câu khó hơn ưu tiên hơn
8. Sắp xếp danh sách câu hỏi theo priority GIẢM DẦN
9. Thêm yếu tố ngẫu nhiên nhỏ để tránh lặp lại hoàn toàn
10. Lấy TOP countNeeded câu hỏi
11. Lưu vào DS_TEMP
```

**Kiểm tra hợp lệ:**
- `countNeeded` phải > 0 và <= 100
- Nếu số câu hỏi có sẵn < countNeeded, chỉ trả về số câu có sẵn

**Ngoại lệ:**
- Nếu không có câu hỏi nào → Trả về danh sách rỗng, P5 sẽ tắt báo thức ngay lập tức

---

#### Mini-spec P5.4: Cập nhật tiến độ SRS

**Mục tiêu:** Cập nhật trạng thái học tập của câu hỏi sau khi người dùng trả lời

**Input:**
- `questionId`: Mã câu hỏi
- `isCorrect`: Đúng hay sai
- `timeSpentMs`: Thời gian trả lời (ms)
- `alarmHistoryId`: Mã lịch sử báo thức (nullable)

**Output:**
- DS4 (question_progress): Cập nhật tiến độ mới
- DS4 (history): Ghi lại lịch sử trả lời

**Xử lý:**
```
1. Ghi lại History:
   INSERT INTO history (questionId, alarmHistoryId, isCorrect, answeredAt, timeToAnswerMs)
   VALUES (questionId, alarmHistoryId, isCorrect, NOW(), timeSpentMs)

2. Đọc question_progress WHERE questionId = questionId
   NẾU không tồn tại:
   - Tạo mới progress với giá trị mặc định:
     correctStreak = 0, easinessFactor = 2.5, interval = 0, difficultyScore = 1000

3. NẾU isCorrect = TRUE (đúng):
   - correctStreak = correctStreak + 1
   - easinessFactor = MIN(easinessFactor + 0.1, 3.0)  // Tăng, tối đa 3.0
   - NẾU interval = 0:
     - interval = 1  // Lần đầu đúng, ôn lại sau 1 ngày
   - NGƯỢC LẠI:
     - interval = ROUND(interval * easinessFactor)  // Giãn cách tăng theo hệ số
   - difficultyScore = difficultyScore - 50  // Câu dễ hơn

4. NẾU isCorrect = FALSE (sai):
   - correctStreak = 0  // Reset streak
   - easinessFactor = MAX(easinessFactor - 0.2, 1.3)  // Giảm, tối thiểu 1.3
   - interval = 1  // Phải ôn lại sớm
   - difficultyScore = difficultyScore + 100  // Câu khó hơn

5. Tính thời gian ôn tập tiếp theo:
   - nextReviewDate = NOW() + interval ngày
   - lastReviewedDate = NOW()

6. Cập nhật vào database:
   UPDATE question_progress
   SET correctStreak = ..., easinessFactor = ..., interval = ...,
       difficultyScore = ..., nextReviewDate = ..., lastReviewedDate = ...
   WHERE questionId = questionId
```

**Kiểm tra hợp lệ:**
- `questionId` phải tồn tại trong bảng questions (trừ câu mặc định có ID âm)
- `timeSpentMs` phải >= 0

**Ngoại lệ:**
- Nếu questionId < 0 (câu hỏi mặc định), bỏ qua cập nhật SRS

---

#### Mini-spec P4.5: Kiểm tra điều kiện tắt

**Mục tiêu:** Xác định người dùng có đủ điều kiện tắt báo thức chưa (đã quét QR và hoàn thành Quiz)

**Input:**
- `alarmId`: Mã báo thức
- DS1: Cấu hình báo thức (questionCount)
- DS3: Danh sách QR đã liên kết (qua alarm_qr_link)
- Kết quả từ P5 (Quiz) hoặc P6 (QR Scanner)

**Output:**
- Lệnh tắt báo thức (gửi cho P4.2 để dừng service)
- Hoặc yêu cầu tiếp tục Quiz/QR

**Xử lý:**
```
1. Đọc alarm WHERE alarmId = alarmId
2. Đọc QRCodeCount:
   SELECT COUNT(*) FROM alarm_qr_link WHERE alarmId = alarmId

3. NẾU QRCodeCount > 0:
   - Kiểm tra biến qrValidated (do P6 set)
   - NẾU qrValidated = FALSE:
     - RETURN "Yêu cầu quét QR"  // Chuyển đến QRCodeScannerScreen
   - NẾU qrValidated = TRUE:
     - Tiếp tục bước 4

4. NẾU alarm.questionCount > 0:
   - Kiểm tra biến quizCompleted (do P5 set)
   - NẾU quizCompleted = FALSE:
     - RETURN "Yêu cầu làm Quiz"  // Chuyển đến QuizScreen
   - NẾU quizCompleted = TRUE:
     - Tiếp tục bước 5

5. Tất cả điều kiện đã đủ:
   - Cập nhật AlarmHistory: dismissalTime = NOW(), isDismissed = TRUE
   - Gửi lệnh stopService() cho P4.2
   - Gửi lệnh cho P4.6 (Lập lại lịch lặp lại)
   - RETURN "Đã tắt báo thức"
```

**Kiểm tra hợp lệ:**
- `alarmId` phải tồn tại

**Ngoại lệ:**
- Nếu người dùng force-stop app, service sẽ bị kill (hệ thống Android tự xử lý)

---

#### Mini-spec P3: Lập lịch Báo thức

**Mục tiêu:** Đặt lịch hẹn với Android AlarmManager để hệ thống kích hoạt báo thức đúng giờ

**Input:**
- `alarmEntity`: Thông tin báo thức (alarmId, hour, minute, daysOfWeek)

**Output:**
- Lịch hẹn đã đặt trong hệ thống Android

**Xử lý:**
```
1. Tạo Calendar instance
2. SET hour = alarmEntity.hour, minute = alarmEntity.minute, second = 0, millisecond = 0
3. NẾU calendar.timeInMillis <= currentTimeMillis:
   - calendar.add(Calendar.DAY_OF_YEAR, 1)  // Đặt sang ngày mai
4. NẾU daysOfWeek.isEmpty():
   - Sử dụng thời gian vừa tính ở bước 3 (1 lần)
5. NGƯỢC LẠI (có lặp lại):
   - Tìm ngày lặp gần nhất trong tương lai:
     FOR i FROM 0 TO 7:
       - candidate = calendar + i ngày
       - dayCode = getDayCode(candidate.dayOfWeek)  // "T2", "T3"...
       - NẾU dayCode IN daysOfWeek VÀ candidate > NOW():
         - triggerTime = candidate
         - BREAK
6. Tạo PendingIntent:
   - Intent target = AlarmReceiver
   - requestCode = alarmId  // Quan trọng: mỗi alarm có code riêng
   - extras: ALARM_ID, ALARM_LABEL, RINGTONE_URI
   - flags: FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
7. Gọi AlarmManager.setAlarmClock(triggerTime, pendingIntent)
   - Dùng setAlarmClock để đảm bảo đánh thức máy ngay cả khi Doze mode
```

**Kiểm tra hợp lệ:**
- Kiểm tra quyền `SCHEDULE_EXACT_ALARM` (Android 12+)
- Nếu không có quyền, hiển thị dialog yêu cầu người dùng cấp

**Ngoại lệ:**
- Nếu thiếu quyền, không đặt được lịch → Báo lỗi cho người dùng

---

#### Mini-spec P7: Tạo báo cáo Thống kê

**Mục tiêu:** Tính toán và tạo các báo cáo thống kê hiệu suất học tập và thức dậy

**Input:**
- DS4: history, alarm_history
- DS5: user_stats, topic_stats

**Output:**
- `weeklyAccuracy`: List<Pair<String, Float>> (7 ngày, mỗi ngày có % đúng)
- `srsDistribution`: List<Pair<String, Int>> (New/Learning/Mastered, số lượng)
- `wakeUpScore`: Float (0-100)

**Xử lý:**

**1. Tính Weekly Accuracy:**
```sql
SELECT 
  date(answeredAt/1000, 'unixepoch', 'localtime') as day,
  SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correct,
  COUNT(*) as total
FROM history
WHERE answeredAt > (NOW() - 7 days)
GROUP BY day
ORDER BY day ASC
```
- Với mỗi ngày trong 7 ngày gần nhất:
  - NẾU có dữ liệu: accuracy = correct / total
  - NẾU không có: accuracy = 0
- Nhãn: "Nay", "06", "05", "04", "03", "02", "01"

**2. Tính SRS Distribution:**
```sql
SELECT 
  CASE 
    WHEN correctStreak = 0 THEN 'New'
    WHEN correctStreak BETWEEN 1 AND 4 THEN 'Learning'
    ELSE 'Mastered'
  END as status,
  COUNT(*) as count
FROM question_progress
GROUP BY status
```
- Trả về 3 giá trị: countNew, countLearning, countMastered

**3. Tính Wake-up Score:**
```
1. Lấy 5 lần alarm_history gần nhất (ORDER BY firstRingTime DESC LIMIT 5)
2. Với mỗi lần:
   - delayMinutes = (dismissalTime - firstRingTime) / 60000
   - score = 100 - (snoozeCount * 10) - (delayMinutes * 0.5)
3. Tính trung bình 5 điểm
4. Giới hạn [0, 100]
```

**Kiểm tra hợp lệ:**
- Nếu không có dữ liệu history → Trả về danh sách rỗng/0
- Nếu không có alarm_history → wakeUpScore = 0

---

### 2.7. Sơ đồ ERD (Entity Relationship Diagram)

**File sơ đồ:** [structured_erd.mmd](diagrams/structured_erd.mmd)

**Các thực thể và quan hệ:**

1. **ALARMS** (1) ↔ (N) **ALARM_TOPIC_LINK** ↔ (N) **TOPICS** (1)
   - Quan hệ N-N: Báo thức có thể chọn nhiều Topic, mỗi Topic được dùng bởi nhiều Alarm

2. **ALARMS** (1) ↔ (N) **ALARM_SELECTED_QUESTIONS**
   - Quan hệ 1-N: Báo thức có thể chọn nhiều câu hỏi lẻ

3. **ALARMS** (1) ↔ (N) **ALARM_QR_LINK** ↔ (N) **QR_CODES** (1)
   - Quan hệ N-N: Báo thức có thể dùng nhiều QR, mỗi QR được dùng bởi nhiều Alarm

4. **ALARMS** (1) ↔ (N) **ALARM_HISTORY**
   - Quan hệ 1-N: Mỗi lần báo thức reo tạo 1 AlarmHistory

5. **TOPICS** (1) ↔ (N) **QUESTIONS**
   - Quan hệ 1-N: Mỗi Topic chứa nhiều câu hỏi

6. **TOPICS** (1) ↔ (1) **TOPIC_STATS**
   - Quan hệ 1-1: Mỗi Topic có 1 bản ghi thống kê

7. **QUESTIONS** (1) ↔ (1) **QUESTION_PROGRESS**
   - Quan hệ 1-1: Mỗi câu hỏi có 1 bản ghi tiến độ

8. **QUESTIONS** (1) ↔ (N) **HISTORY**
   - Quan hệ 1-N: Mỗi câu hỏi có nhiều lần trả lời

9. **ALARM_HISTORY** (1) ↔ (N) **HISTORY**
   - Quan hệ 1-N: Mỗi lần báo thức reo chứa nhiều câu trả lời

10. **USER** (1) ↔ (1) **USER_STATS**
    - Quan hệ 1-1: Mỗi user có 1 bản ghi thống kê

**Lưu ý:** 
- Câu hỏi mặc định (ID âm) không có FK constraint với bảng questions
- Cascade delete được áp dụng: xóa Alarm → xóa các link, xóa Topic → xóa Questions

---

## III. THIẾT KẾ

### 3.1. Thiết kế tổng thể

**Kiến trúc:** MVVM (Model-View-ViewModel) kết hợp Clean Architecture

**Phân tầng:**

```
┌─────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)                │
│  - Screens: AlarmScreen, QuizScreen...     │
│  - Components: AlarmCard, QuestionCard...  │
└───────────────┬─────────────────────────────┘
                │ observes StateFlow
┌───────────────▼─────────────────────────────┐
│  ViewModel Layer                            │
│  - AlarmViewModel, QuizViewModel...         │
│  - Quản lý UI state, xử lý user interaction│
└───────────────┬─────────────────────────────┘
                │ calls
┌───────────────▼─────────────────────────────┐
│  Logic Layer                                │
│  - AlarmScheduler: Đặt lịch hệ thống       │
│  - QuestionAlgorithmManager: SRS algorithm │
│  - AlarmReceiver, AlarmService              │
└───────────────┬─────────────────────────────┘
                │ reads/writes
┌───────────────▼─────────────────────────────┐
│  Data Layer (Room Database)                 │
│  - AppDao, StatisticsDao                    │
│  - Entities: AlarmEntity, QuestionEntity... │
│  - AppDatabase (Singleton)                  │
└─────────────────────────────────────────────┘
```

**Luồng dữ liệu:**
- **UI → ViewModel:** User actions (clicks, inputs)
- **ViewModel → Logic:** Business logic calls
- **Logic → Data:** CRUD operations
- **Data → ViewModel:** Flow/StateFlow emissions
- **ViewModel → UI:** UI state updates

**Android Components sử dụng:**
- **Activity:** MainActivity, AlarmRingingActivity
- **BroadcastReceiver:** AlarmReceiver (nhận sự kiện từ AlarmManager)
- **Foreground Service:** AlarmService (phát nhạc, hiển thị notification)
- **Navigation:** Jetpack Navigation Compose
- **Database:** Room (SQLite wrapper)

### 3.2. Thiết kế cơ sở dữ liệu

**Database:** Room SQLite, tên: `app_database`, version: 2

**Danh sách bảng:** (Đã mô tả chi tiết trong Data Dictionary 2.5.2)

1. **alarms** (PK: alarmId)
2. **topics** (PK: topicId)
3. **questions** (PK: questionId, FK: ownerTopicId)
4. **qr_codes** (PK: qrId)
5. **alarm_topic_link** (PK: alarmId + topicId, FK: alarmId, topicId)
6. **alarm_selected_questions** (PK: selectionId, FK: alarmId)
7. **alarm_qr_link** (PK: alarmId + qrId, FK: alarmId, qrId)
8. **question_progress** (PK: questionId, FK: questionId)
9. **topic_stats** (PK: topicId, FK: topicId)
10. **history** (PK: historyId, FK: questionId, alarmHistoryId)
11. **alarm_history** (PK: historyId, FK: alarmId)
12. **UserStats** (PK: userId)

**Indexes:**
- `alarm_topic_link`: INDEX(alarmId), INDEX(topicId)
- `alarm_qr_link`: INDEX(alarmId), INDEX(qrId)
- `questions`: INDEX(ownerTopicId)
- `history`: INDEX(questionId), INDEX(alarmHistoryId)
- `alarm_history`: INDEX(alarmId)

**Foreign Key Constraints:**
- ON DELETE CASCADE: Xóa parent tự động xóa child (VD: xóa Alarm → xóa alarm_topic_link)

**Migration:**
- Version 1 → 2: Thêm bảng qr_codes và alarm_qr_link

### 3.3. Thiết kế thuật toán SRS (Spaced Repetition System)

**Cơ sở lý thuyết:** Dựa trên thuật toán SM-2 (SuperMemo 2)

**Các tham số:**
- `correctStreak`: Số lần đúng liên tiếp (>= 0)
- `easinessFactor` (EF): Hệ số dễ dàng (1.3 - 3.0, mặc định 2.5)
- `interval`: Khoảng cách ngày cho lần ôn tập tới (0 = chưa học)
- `difficultyScore`: Điểm độ khó (dùng cho sắp xếp ưu tiên, mặc định 1000)
- `nextReviewDate`: Thời gian ôn tập tiếp theo

**Công thức cập nhật:**

**Khi trả lời đúng:**
```
correctStreak = correctStreak + 1
EF = MIN(EF + 0.1, 3.0)

IF interval == 0:
    interval = 1  // Lần đầu đúng, ôn lại sau 1 ngày
ELSE:
    interval = ROUND(interval * EF)  // Giãn cách tăng theo EF

difficultyScore = difficultyScore - 50
nextReviewDate = NOW() + interval (days)
```

**Khi trả lời sai:**
```
correctStreak = 0
EF = MAX(EF - 0.2, 1.3)
interval = 1  // Phải ôn lại sớm
difficultyScore = difficultyScore + 100
nextReviewDate = NOW() + 1 (day)
```

**Điểm ưu tiên khi chọn câu hỏi:**
```
IF progress == NULL (câu chưa học):
    priority = 500
ELSE IF nextReviewDate <= NOW (đã đến hạn ôn):
    priority = 1000 + (NOW - nextReviewDate)  // Càng quá hạn càng ưu tiên
ELSE:
    priority = difficultyScore  // Câu khó hơn ưu tiên hơn
```

**Sắp xếp:** DESC priority (giảm dần) + thêm yếu tố random nhỏ để tránh lặp lại 100%

### 3.4. Thiết kế giao diện (UI/UX)

**Theme:** Material Design 3, Dark mode mặc định

**Màn hình chính:**
1. **AlarmScreen:** Danh sách báo thức, FAB "Thêm mới", switch bật/tắt
2. **TopicScreen:** Danh sách chủ đề, search bar, card hiển thị số câu hỏi
3. **StatsScreen:** Biểu đồ line chart (7 ngày), pie chart (SRS), card điểm số

**Màn hình phụ:**
4. **AlarmSettingsScreen:** Time picker, text field (label), chip selector (days), slider (snooze), button chọn Mission/QR
5. **QuizScreen:** Progress bar (số câu đúng/tổng), timer progress (circular), câu hỏi + 4 đáp án (card)
6. **AlarmRingingScreen:** Hiển thị nhãn, thời gian, nút "Tắt", nút "Snooze" (nếu enabled)
7. **TopicDetailScreen:** Tên chủ đề, danh sách câu hỏi, FAB "Thêm câu hỏi"
8. **QRCodeScannerScreen:** Camera preview, khung hình quét, hướng dẫn

**Navigation:**
- Bottom Navigation Bar: 3 tabs (Alarm, Topic, Stats)
- Stack navigation: Setting → Mission Dialog, Setting → QR Dialog, Topic → Topic Detail

### 3.5. Thiết kế bảo mật và hiệu năng

**Bảo mật:**
- Dữ liệu lưu local, không gửi ra ngoài
- Room database không mã hóa (do tính chất ứng dụng cá nhân, không có dữ liệu nhạy cảm)
- QR code chỉ lưu giá trị, không lưu hình ảnh (tiết kiệm dung lượng)

**Hiệu năng:**
- Sử dụng Flow để reactive data (không cần query lại liên tục)
- AlarmManager.setAlarmClock() đảm bảo báo thức chính xác ngay cả khi Doze mode
- Foreground Service với notification priority HIGH để không bị kill
- Lazy loading cho danh sách câu hỏi (chỉ load khi cần)

**Quyền (Permissions):**
- `SCHEDULE_EXACT_ALARM` (Android 12+): Đặt báo thức chính xác
- `POST_NOTIFICATIONS` (Android 13+): Hiển thị notification
- `CAMERA`: Quét QR code
- `USE_FULL_SCREEN_INTENT`: Hiển thị màn hình báo thức toàn màn hình

---

## IV. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 4.1. Tổng kết

Hệ thống Báo thức Thông minh đã được phân tích và thiết kế chi tiết theo phương pháp có cấu trúc (SA/SD):

**Thành quả:**
- ✅ Sơ đồ FDD: 5 module chính, 30+ chức năng con
- ✅ DFD Context, Level 0: 7 tiến trình chính, 5 kho dữ liệu
- ✅ DFD Level 1: Phân rã chi tiết P4 (Xử lý Báo thức Reo) và P5 (Thực thi Quiz)
- ✅ Data Dictionary: Mô tả chi tiết 7 luồng dữ liệu, 12 bảng dữ liệu
- ✅ Mini-spec: 5 tiến trình quan trọng nhất (P5.1, P5.4, P4.5, P3, P7)
- ✅ ERD: 12 entity, 10 quan hệ

**Điểm mạnh:**
- Kiến trúc rõ ràng, phân tầng tốt
- Thuật toán SRS thông minh, tối ưu học tập
- Tích hợp QR code để tăng hiệu quả thức dậy
- Gamification (streak, điểm số) tạo động lực

### 4.2. Hạn chế

1. **Giả định về quyền hệ thống:** Code hiện tại giả định người dùng luôn cấp quyền. Trên thực tế, cần xử lý trường hợp từ chối quyền tốt hơn.

2. **Thuật toán SRS đơn giản:** Chỉ dựa trên correctStreak và easinessFactor. Có thể cải tiến bằng cách tích hợp:
   - Thời gian suy nghĩ (nhanh = nhớ tốt)
   - Lịch sử trả lời dài hạn (forget curve)

3. **Không có đồng bộ đám mây:** Nếu người dùng đổi máy, mất toàn bộ dữ liệu.

4. **QR Code cố định:** Nếu di chuyển vật có gắn QR (VD: di chuyển tủ lạnh), cần quét lại.

5. **Chưa có chế độ "Emergency dismiss":** Trong trường hợp khẩn cấp (VD: cần đi gấp), người dùng không có cách tắt nhanh.

### 4.3. Hướng phát triển

**Giai đoạn 1 (Đã hoàn thành):**
- ✅ CRUD báo thức, chủ đề, câu hỏi
- ✅ Quiz với thuật toán SRS
- ✅ QR Code integration
- ✅ Thống kê cơ bản

**Giai đoạn 2 (3-6 tháng):**
- 🔲 Hỗ trợ câu hỏi dạng hình ảnh (ImageQuestion)
- 🔲 Tích hợp Text-to-Speech để đọc câu hỏi
- 🔲 Chế độ "Morning briefing" (đọc tin tức, thời tiết sau khi tắt báo thức)
- 🔲 Widget trên màn hình chính
- 🔲 Cải thiện UI/UX (animation, haptic feedback)

**Giai đoạn 3 (6-12 tháng):**
- 🔲 Đồng bộ đám mây (Firebase/Supabase)
- 🔲 Chia sẻ bộ câu hỏi với cộng đồng
- 🔲 AI sinh câu hỏi tự động (dựa trên chủ đề)
- 🔲 Tích hợp smart home (IoT): bật đèn, pha cà phê khi báo thức reo
- 🔲 Phân tích giấc ngủ (kết hợp với wearable device)

**Giai đoạn 4 (Dài hạn):**
- 🔲 Multi-user support (dùng cho gia đình)
- 🔲 Gamification nâng cao (leaderboard, achievement)
- 🔲 Chatbot hỗ trợ tạo câu hỏi
- 🔲 Cross-platform (iOS, Web)

### 4.4. Tài liệu tham khảo

1. **Thuật toán SRS:**
   - Wozniak, P. (1990). "Algorithm SM-2"
   - Anki Documentation: https://docs.ankiweb.net/

2. **Android Development:**
   - Android Developers Guide: AlarmManager Best Practices
   - Jetpack Compose Documentation

3. **Phân tích hệ thống:**
   - Yourdon, E., & Constantine, L. (1979). "Structured Design"
   - DeMarco, T. (1978). "Structured Analysis and System Specification"

---

**PHỤ LỤC:**

- [FDD Diagram](diagrams/structured_fdd.mmd)
- [DFD Context](diagrams/structured_dfd_context.mmd)
- [DFD Level 0](diagrams/structured_dfd_level0.mmd)
- [DFD Level 1 - P4](diagrams/structured_dfd_level1_p4.mmd)
- [DFD Level 1 - P5](diagrams/structured_dfd_level1_p5.mmd)
- [ERD Diagram](diagrams/structured_erd.mmd)

---

**KẾT THÚC BÁO CÁO PHÂN TÍCH CÓ CẤU TRÚC**


