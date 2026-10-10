# BÁO CÁO NGHIÊN CỨU - THIẾT KẾ
# MÔ HÌNH CNN CHỐNG GIẢ MẠO KHUÔN MẶT TRÊN THIẾT BỊ DI ĐỘNG TRONG ĐIỀU KIỆN THIẾU SÁNG

**Hệ thống Báo thức Thông minh — Nhiệm vụ "Thử thách nụ cười"**

**Nền tảng:** Android (Kotlin), CameraX, TensorFlow Lite
**Bài toán:** Face Anti-Spoofing (FAS) / Presentation Attack Detection (PAD) trên ảnh RGB đơn khung hình
**Ngày:** Tháng 9/2026

---

## I. BỐI CẢNH VÀ VẤN ĐỀ

### 1.1. Hiện trạng

Nhiệm vụ "Thử thách nụ cười" yêu cầu người dùng **mở mắt và cười** trước camera trước để tắt báo thức. Phiên bản hiện tại dùng **Google ML Kit Face Detection**: đọc xác suất `smilingProbability`, `leftEyeOpenProbability` và `rightEyeOpenProbability` rồi so với ngưỡng.

### 1.2. Các vấn đề

| # | Vấn đề | Hệ quả |
|---|---|---|
| P1 | ML Kit đôi khi **nhận nhầm vật thể khác** thành mắt và miệng | Tắt được báo thức mà không cần người thật |
| P2 | **Giả mạo bằng ảnh in** (mặt đang cười, mắt mở) | Vượt qua nhiệm vụ mà vẫn nằm trên giường |
| P3 | **Giả mạo bằng màn hình** (ảnh chụp hoặc video phát lại trên điện thoại khác) | Như trên |
| P4 | **Thiếu sáng**: báo thức thường reo khi phòng còn tối | Ảnh nhiễu, tối và mờ làm giảm độ chính xác của cả nhận diện lẫn chống giả mạo |
| P5 | Ràng buộc thiết bị: phải chạy **thời gian thực** trên điện thoại tầm trung (kể cả Android 11) | Không dùng được các mô hình lớn (ViT, ResNet-50…) |

### 1.3. Phạm vi

**Bao gồm:** tấn công 2D (ảnh in, ảnh hoặc video phát lại trên màn hình); camera trước RGB; điều kiện ánh sáng từ bình thường đến thiếu sáng.

**Không bao gồm:** mặt nạ 3D (silicon, giấy bồi), deepfake bơm thẳng vào luồng camera, cảm biến hồng ngoại hoặc độ sâu (phần lớn điện thoại không có).

---

## II. MỤC TIÊU VÀ YÊU CẦU

### 2.1. Yêu cầu chức năng

- **FR1.** Phân biệt khuôn mặt thật với ảnh in hoặc màn hình từ luồng camera trước.
- **FR2.** Hoạt động được khi thiếu sáng. Nếu ảnh quá tối, hệ thống phải chủ động tăng sáng (dùng màn hình làm đèn) thay vì đoán bừa.
- **FR3.** Xác định chính xác trạng thái mắt mở và miệng cười, giảm nhận nhầm (P1).
- **FR4.** Kết hợp với cơ chế thử thách–phản hồi: yêu cầu hành động theo thứ tự ngẫu nhiên.

### 2.2. Yêu cầu phi chức năng (chỉ tiêu thiết kế, **chưa đo thực tế**)

| Tiêu chí | Chỉ tiêu |
|---|---|
| Kích thước mô hình (TFLite int8) | ≤ 1 MB |
| Độ trễ suy luận / khung hình (CPU, máy tầm trung) | ≤ 15 ms |
| Độ trễ toàn luồng (phát hiện mặt + chống giả mạo) | ≤ 40 ms (≥ 25 FPS) |
| Độ chính xác | "vừa phải": ACER ≤ 5% trên OULU-NPU Protocol 1; báo cáo riêng cho tập ảnh tối |
| Chỉ số đánh giá | APCER, BPCER, ACER theo ISO/IEC 30107-3 [R17] |

---

## III. TỔNG QUAN CÁC NGHIÊN CỨU LIÊN QUAN

Tất cả nguồn dưới đây đều đăng ở hội nghị hoặc tạp chí có bình duyệt (IEEE, CVF, ECVA, IJCAI), trừ một nguồn được ghi rõ là arXiv.

### 3.1. Mạng nhẹ cho chống giả mạo

| Nguồn | Đóng góp chính | Áp dụng vào đề tài |
|---|---|---|
| **FeatherNets** — Zhang et al., CVPR Workshops 2019 [R1] | Mạng cực nhẹ (≈0,35M tham số, 83M FLOPs). **Streaming module** thay cho Global Average Pooling để giữ thông tin không gian | Dùng streaming module làm đầu ra phân loại |
| **Auto-FAS** — Yu et al., ICASSP 2020 [R2] | Tìm kiếm kiến trúc tự động (NAS) với không gian tìm kiếm **giới hạn kích thước cho mobile** + giám sát nhị phân từng pixel | Cơ sở cho việc chọn mạng nhỏ và giám sát theo pixel |
| **DeepPixBiS** — George & Marcel, ICB 2019 [R3] | **Giám sát nhị phân từng pixel** + nhị phân toàn ảnh, chỉ cần 1 khung hình, hợp với thiết bị thông minh | Dùng làm hàm mất mát chính |

### 3.2. Bắt dấu vết giả mạo (vân in, moiré màn hình)

| Nguồn | Đóng góp chính | Áp dụng vào đề tài |
|---|---|---|
| **CDCN / CDCN++** — Yu et al., CVPR 2020 [R4] | **Tích chập hiệu trung tâm (CDC)**: gộp cả cường độ lẫn gradient, bắt được vân lưới mà tích chập thường bỏ sót | Nguyên lý nền của khối trích đặc trưng tầng thấp |
| **DC-CDN** — Yu et al., IJCAI 2021 [R5] | **Cross-CDC (C-CDC)**: tách CDC theo hướng ngang–dọc và chéo, chỉ dùng **5/9 số tham số** mà hiệu quả hơn CDC đầy đủ | Dùng C-CDC ở các lớp đầu (bản tiết kiệm của CDC) |
| **Image Distortion Analysis** — Wen, Han, Jain, IEEE TIFS 2015 [R6] | Phân tích méo ảnh do in hoặc phát lại: phản xạ gương, mờ, biến dạng màu | Cơ sở lý giải vì sao cần đặc trưng tầng thấp; dùng làm mốc so sánh |

### 3.3. Thiếu sáng

| Nguồn | Đóng góp chính | Áp dụng vào đề tài |
|---|---|---|
| **TSCNN (Attention-based Two-Stream CNN)** — Chen et al., IEEE TIFS 2020 [R7] | Hai nhánh: **RGB** và **Multi-Scale Retinex (MSR)** — dạng ảnh không phụ thuộc ánh sáng; gộp bằng attention | Thêm kênh bất biến ánh sáng vào đầu vào (xem mục 5.2) |
| **Multi-Scale Retinex** — Jobson, Rahman, Woodell, IEEE TIP 1997 [R8] | Thuật toán MSR gốc | Định nghĩa phép biến đổi dùng trong tiền xử lý |
| **CLAHE** — Zuiderveld, Graphics Gems IV, 1994 [R9] | Cân bằng histogram thích nghi có giới hạn độ tương phản | Phương án thay MSR, rẻ hơn trên mobile |
| **Aurora Guard** — Liu et al., arXiv 2019/2021 [R10] *(chưa bình duyệt)* | Dùng **màn hình làm nguồn sáng** và phân tích ánh sáng phản chiếu trên mặt; đã chạy thực tế cho hàng triệu người dùng | Ý tưởng "đèn màn hình" khi thiếu sáng (mục 4.3) |

### 3.4. Phát hiện mặt và điểm mốc trên mobile

| Nguồn | Đóng góp chính | Áp dụng vào đề tài |
|---|---|---|
| **BlazeFace** — Bazarevsky et al., CVPR Workshops 2019 [R11] | Phát hiện mặt dưới 1 ms trên GPU di động | Bước phát hiện và cắt vùng mặt |
| **Face Mesh** — Kartynnik et al., CVPR Workshops 2019 [R12] | Lưới 3D 468 điểm, chạy thời gian thực trên mobile | Tính tỉ lệ mở mắt (EAR) và mở miệng (MAR) để giải quyết P1 |
| **MobileNetV2** — Sandler et al., CVPR 2018 [R13]; **MobileNetV3** — Howard et al., ICCV 2019 [R14] | Khối inverted residual, depthwise separable, SE, h-swish | Khối xây dựng thân mạng |

### 3.5. Dữ liệu và tổng quan

| Nguồn | Nội dung | Vai trò |
|---|---|---|
| **CelebA-Spoof** — Zhang et al., ECCV 2020 [R15] | 625.537 ảnh, 10.177 người, có **nhãn điều kiện chiếu sáng** (gồm cả tối) | Dữ liệu huấn luyện chính; tách tập tối để đánh giá |
| **OULU-NPU** — Boulkenafet et al., IEEE FG 2017 [R16] | 5.940 video quay bằng **camera trước điện thoại**, 3 phiên với ánh sáng và nền khác nhau | Bộ đánh giá chuẩn cho chống giả mạo trên mobile |
| **Survey** — Yu et al., IEEE TPAMI 2023 [R18] | Tổng quan chống giả mạo khuôn mặt bằng học sâu | Định hướng chung, bối cảnh nghiên cứu |

---

## IV. KIẾN TRÚC TỔNG THỂ HỆ THỐNG

### 4.1. Sơ đồ luồng xử lý

```
 ┌───────────────────────────────────────────────────────────────────────────┐
 │                 CAMERA TRƯỚC (CameraX ImageAnalysis, 640x480)             │
 └──────────────────────────────────┬────────────────────────────────────────┘
                                    │ khung hình YUV
                                    ▼
 ┌───────────────────────────────────────────────────────────────────────────┐
 │ [1] ĐÁNH GIÁ ÁNH SÁNG — độ sáng trung bình Y của vùng mặt                 │
 │     Y < ngưỡng tối   →  bật "ĐÈN MÀN HÌNH" (nền trắng, độ sáng tối đa) [R10] │
 └──────────────────────────────────┬────────────────────────────────────────┘
                                    ▼
 ┌───────────────────────────────────────────────────────────────────────────┐
 │ [2] PHÁT HIỆN MẶT + ĐIỂM MỐC — BlazeFace [R11] → Face Mesh 468 điểm [R12] │
 │     • Không có đúng 1 mặt → bỏ khung hình                                 │
 │     • Căn chỉnh theo 2 mắt, cắt vùng mặt (mở rộng 1,2×) → 128x128           │
 └───────────────┬───────────────────────────────────────┬───────────────────┘
                 │ vùng mặt 128x128                      │ 468 điểm mốc
                 ▼                                       ▼
 ┌───────────────────────────────┐       ┌───────────────────────────────────┐
 │ [3] TIỀN XỬ LÝ ÁNH SÁNG        │       │ [5] KIỂM TRA HÀNH ĐỘNG (active)   │
 │  RGB + kênh L bất biến ánh     │       │  • EAR (mở mắt), MAR (cười)       │
 │  sáng (CLAHE [R9] / MSR [R8])  │       │  • Thử thách ngẫu nhiên:          │
 │  → tensor 4x128x128            │       │    "chớp mắt" → "cười" → "mở mắt" │
 └───────────────┬───────────────┘       └─────────────────┬─────────────────┘
                 ▼                                         │
 ┌───────────────────────────────┐                         │
 │ [4] LG-FASNet (mục V)          │                         │
 │  → điểm "thật" s_t ∈ [0,1]     │                         │
 │  → bản đồ pixel 16x16          │                         │
 └───────────────┬───────────────┘                         │
                 ▼                                         │
 ┌───────────────────────────────┐                         │
 │ [6] GỘP THEO THỜI GIAN          │                         │
 │  trung bình trượt N=10 khung   │                         │
 └───────────────┬───────────────┘                         │
                 ▼                                         ▼
 ┌───────────────────────────────────────────────────────────────────────────┐
 │ [7] QUYẾT ĐỊNH:  THẬT  ⇔  S ≥ τ_live  VÀ  hoàn thành chuỗi thử thách       │
 │                  GIẢ   ⇔  S < τ_spoof (liên tục K khung) → cảnh báo, làm lại│
 └───────────────────────────────────────────────────────────────────────────┘
```

### 4.2. Vai trò từng khối và nguồn tham chiếu

| Khối | Chức năng | Giải quyết | Nguồn |
|---|---|---|---|
| [1] Đánh giá ánh sáng + đèn màn hình | Tăng sáng vật lý khi phòng tối | P4 | Ý tưởng từ Aurora Guard [R10] |
| [2] BlazeFace + Face Mesh | Cắt mặt chuẩn; điểm mốc mắt và môi chính xác | P1 | [R11], [R12] |
| [3] Tiền xử lý CLAHE / MSR | Thêm kênh ít phụ thuộc ánh sáng | P4 | [R7], [R8], [R9] |
| [4] LG-FASNet | Chống giả mạo thụ động (ảnh in, màn hình) | P2, P3 | [R1]–[R5], [R13], [R14] |
| [5] Kiểm tra hành động | Buộc có chuyển động sinh học theo thứ tự ngẫu nhiên | P1, P2 | EAR/MAR tính trên [R12] |
| [6] Gộp thời gian | Giảm dao động giữa các khung | P4 | Kỹ thuật chuẩn (thiết kế của đề tài) |
| [7] Quyết định | Kết hợp chống giả mạo thụ động và kiểm tra hành động | P1–P3 | Thiết kế của đề tài |

### 4.3. Xử lý thiếu sáng theo 3 lớp

1. **Vật lý (khối 1):** khi độ sáng thấp, chuyển màn hình sang nền trắng với độ sáng tối đa để chiếu sáng mặt. Ý tưởng từ Aurora Guard [R10], nhưng ở đây **chỉ dùng để chiếu sáng**, không phân tích phản xạ như bài gốc.
2. **Tiền xử lý (khối 3):** thêm kênh độ sáng đã chuẩn hóa bằng CLAHE [R9] (mặc định) hoặc MSR [R8], dựa theo TSCNN [R7] dùng ảnh MSR vì nó không phụ thuộc ánh sáng.
3. **Học (mục VI):** tăng cường dữ liệu bằng mô phỏng thiếu sáng, và huấn luyện hoặc đánh giá trên tập ảnh tối của CelebA-Spoof [R15].

---

## V. KIẾN TRÚC MẠNG ĐỀ XUẤT: LG-FASNet

*(LG-FASNet = Lightweight Gradient-aware Face Anti-Spoofing Network — tên đặt riêng cho đề tài)*

### 5.1. Nguyên tắc thiết kế

| Nguyên tắc | Lý do | Nguồn |
|---|---|---|
| Đặc trưng gradient ở **tầng thấp** | Vân in và moiré màn hình là chi tiết tần số cao, nằm ở tầng thấp | CDC [R4], C-CDC [R5], IDA [R6] |
| **C-CDC chỉ ở 2 lớp đầu** | CDC đầy đủ khá nặng; C-CDC dùng 5/9 tham số | [R5] |
| Thân mạng **inverted residual + SE** | Tối ưu cho CPU di động | [R13], [R14] |
| **Không dùng GAP** ở đầu ra | GAP làm mất thông tin vị trí; dùng streaming module | [R1] |
| **Giám sát theo pixel** | Ổn định hơn khi chỉ có nhãn thật/giả; không tăng chi phí khi suy luận | [R2], [R3] |
| **Gộp sớm** RGB + kênh bất biến ánh sáng | Bản rút gọn của TSCNN [R7]: dùng 1 nhánh thay vì 2 để giảm một nửa chi phí | [R7] *(điều chỉnh)* |

### 5.2. Chi tiết các lớp

Đầu vào: tensor **4 × 128 × 128** = RGB (3 kênh) + L_norm (1 kênh, CLAHE hoặc MSR).

| # | Lớp | Toán tử | Kênh ra | Stride | Kích thước ra | Nguồn |
|---|---|---|---|---|---|---|
| 0 | Input | — | 4 | — | 128×128 | [R7] (RGB+MSR) |
| 1 | Stem | **C-CDC 3×3** (hướng ngang–dọc) + BN + h-swish | 16 | 2 | 64×64 | [R5], [R14] |
| 2 | Low-level | **C-CDC 3×3** (hướng chéo) + BN + h-swish | 24 | 2 | 32×32 | [R5] |
| 3 | Stage-2a | Inverted residual (t=4) + SE | 40 | 2 | 16×16 | [R13], [R14] |
| 4 | Stage-2b | Inverted residual (t=4) + SE | 40 | 1 | 16×16 | [R13], [R14] |
| 5a | **Nhánh pixel** (chỉ khi huấn luyện) | Conv 1×1 → Sigmoid | 1 | 1 | **16×16** | [R3] |
| 6 | Stage-3a | Inverted residual (t=4) + SE | 80 | 2 | 8×8 | [R13], [R14] |
| 7 | Stage-3b,c | 2 × Inverted residual (t=4) + SE | 80 | 1 | 8×8 | [R13], [R14] |
| 8 | **Streaming module** | Depthwise conv 8×8 (không padding) | 80 | — | 1×1 | [R1] |
| 9 | Head | Dropout 0,2 → FC 80→1 → Sigmoid | 1 | — | điểm `s` | — |

**Ghi chú về C-CDC [R5]:** mỗi lớp tính `y = conv(x) − θ · (tổng trọng số trên vùng chữ thập) · x_center`, với θ = 0,7 theo [R4]. Lớp 1 dùng vùng ngang–dọc, lớp 2 dùng vùng chéo, giống cách tách của DC-CDN.

### 5.3. Ước tính độ phức tạp (tính tay, **cần đo lại** sau khi hiện thực)

| Thành phần | Tham số (≈) | MACs (≈) |
|---|---|---|
| Lớp 1–2 (C-CDC) | 4 K | 6 M |
| Stage-2 (lớp 3–4) | 22 K | 5,5 M |
| Stage-3 (lớp 6–7) + SE | 135 K | 9 M |
| Streaming + head | 5 K | < 0,1 M |
| **Tổng** | **≈ 0,17 M** | **≈ 21 M** |

So với FeatherNet-B [R1] (≈0,35M tham số, 83M FLOPs), mạng đề xuất nhỏ hơn khoảng 2 lần về tham số và khoảng 4 lần về tính toán. Đổi lại, mô hình có thể kém chính xác hơn; đây là đánh đổi có chủ đích để đạt yêu cầu "độ chính xác vừa phải". Sau lượng tử hóa int8, kích thước dự kiến ≈ 0,2–0,3 MB.

### 5.4. Hàm mất mát

Theo DeepPixBiS [R3], kết hợp giám sát toàn ảnh và theo pixel:

```
L = λ · L_pix + (1 − λ) · L_bin ,   λ = 0,5

L_bin = BCE( s , y )                          y ∈ {0: giả, 1: thật}
L_pix = (1/256) Σ_{i,j} BCE( M_ij , y )       M: bản đồ 16×16 ở lớp 5a
```

Khi suy luận, nhánh pixel (lớp 5a) bị **bỏ đi**. Tùy chọn: dùng `s' = ½(s + mean(M))` như DeepPixBiS nếu muốn tăng độ ổn định, đổi lại tốn thêm một phép tính rất nhỏ.

---

## VI. HUẤN LUYỆN

### 6.1. Dữ liệu

| Tập | Dùng cho | Ghi chú |
|---|---|---|
| CelebA-Spoof [R15] | Huấn luyện chính | Dùng nhãn *illumination* để tách tập **Dark** |
| OULU-NPU [R16] | Đánh giá chuẩn (Protocol 1–4) | Quay bằng điện thoại, sát với ứng dụng |
| Dữ liệu tự thu (từ chính app) | Tinh chỉnh + kiểm thử thực tế | Người thật / ảnh in / màn hình, **phòng tối và bật đèn màn hình** |

⚠️ **Giấy phép:** CelebA-Spoof và OULU-NPU chỉ cho phép dùng cho **nghiên cứu phi thương mại** (OULU-NPU phải ký thỏa thuận). Muốn phát hành thương mại phải dùng dữ liệu tự thu.

### 6.2. Tăng cường dữ liệu mô phỏng thiếu sáng

| Phép biến đổi | Tham số | Mục đích |
|---|---|---|
| Gamma tối | γ ∈ [1,5 ; 3,0] | Mô phỏng phơi sáng thiếu |
| Giảm độ sáng tuyến tính | × [0,2 ; 0,6] | Như trên |
| Nhiễu Poisson–Gaussian | σ ∈ [0,01 ; 0,05] | Mô phỏng nhiễu cảm biến khi ISO cao |
| Mờ chuyển động | kernel 3–7 px | Mô phỏng tay run lúc mới ngủ dậy |
| Nén JPEG | chất lượng 40–90 | Mô phỏng nén của camera |
| Ám màu | ±15% mỗi kênh | Đèn vàng, đèn màn hình |

**Lưu ý:** kênh L_norm được tính **sau** khi tăng cường, giống quy trình trên thiết bị.

### 6.3. Cấu hình huấn luyện đề xuất

- Optimizer AdamW, lr = 1e-3, cosine decay, 60 epoch, batch 128.
- Cân bằng số mẫu thật/giả trong mỗi batch.
- Huấn luyện có lượng tử hóa (Quantization-Aware Training) ở 10 epoch cuối để xuất int8 ít mất độ chính xác.

---

## VII. TRIỂN KHAI TRÊN ANDROID

| Hạng mục | Lựa chọn |
|---|---|
| Runtime | TensorFlow Lite, int8, XNNPACK (CPU); GPU delegate tùy chọn |
| Phát hiện mặt và điểm mốc | MediaPipe Face Mesh [R12] (có sẵn BlazeFace [R11] bên trong), thay cho ML Kit hiện tại |
| Luồng camera | CameraX `ImageAnalysis`, `STRATEGY_KEEP_ONLY_LATEST` |
| Tiền xử lý CLAHE | OpenCV Android, hoặc tự viết trên kênh Y (128×128, chi phí không đáng kể) |
| Tích hợp | Thay phần kiểm tra trong `FaceMissionAnalyzer` / `FaceMissionViewModel` bằng luồng [1]→[7] ở mục IV |

**Tham số quyết định ban đầu** (hiệu chỉnh trên dữ liệu tự thu):

| Tham số | Giá trị khởi điểm |
|---|---|
| Ngưỡng tối bật đèn màn hình (độ sáng Y trung bình của vùng mặt) | 60 / 255 |
| N (cửa sổ gộp thời gian) | 10 khung |
| τ_live / τ_spoof | 0,7 / 0,3 |
| K (số khung giả liên tiếp để cảnh báo) | 5 |
| EAR mở mắt / MAR cười | hiệu chỉnh theo từng người ở lần cấu hình đầu |

---

## VIII. KẾ HOẠCH ĐÁNH GIÁ

| Thí nghiệm | Mục tiêu | Chỉ số |
|---|---|---|
| E1. So sánh các mô hình | LG-FASNet vs. FeatherNet [R1], DeepPixBiS [R3], MobileNetV3-Small [R14] | ACER, số tham số, MACs, độ trễ trên máy thật |
| E2. Loại bỏ từng thành phần (ablation) | Đóng góp của C-CDC, kênh L_norm, nhánh pixel, streaming module | ACER trên OULU-NPU P1 |
| E3. Thiếu sáng | Có/không có kênh L_norm, tăng cường dữ liệu và đèn màn hình | ACER trên CelebA-Spoof-Dark + dữ liệu tự thu trong phòng tối |
| E4. Trên thiết bị | Máy Android 11 tầm trung, máy Android 14 | ms/khung, mức RAM, % pin mỗi lần báo thức |
| E5. Toàn hệ thống | Tỉ lệ vượt qua bằng ảnh in / màn hình khi có kiểm tra hành động | APCER toàn hệ thống |

Chỉ số theo ISO/IEC 30107-3 [R17]:
- **APCER** — tỉ lệ tấn công bị nhận nhầm là thật
- **BPCER** — tỉ lệ người thật bị từ chối
- **ACER** = (APCER + BPCER) / 2

---

## IX. HẠN CHẾ VÀ RỦI RO

1. **Tổng quát hóa sang miền mới:** mô hình huấn luyện trên dữ liệu công khai thường giảm độ chính xác khi gặp camera hoặc môi trường khác [R18]. Cần tinh chỉnh bằng dữ liệu tự thu.
2. **Mạng rất nhỏ** (≈0,17M tham số) có thể không đạt ACER ≤ 5%. Phương án dự phòng: tăng độ rộng lên 1,5× (≈0,4M tham số, vẫn < 1 MB).
3. **Màn hình chất lượng cao** (OLED, ảnh độ phân giải cao) có ít vân moiré, khó phát hiện thụ động. Vì vậy mới cần thêm kiểm tra hành động (khối 5).
4. **Đèn màn hình** có thể làm người dùng chói mắt lúc mới dậy. Nên bật tăng dần trong 0,5 giây.
5. **Không chống được** mặt nạ 3D và tấn công bơm luồng camera (ngoài phạm vi, mục 1.3).
6. Các số liệu ở mục 2.2 và 5.3 là **chỉ tiêu hoặc ước tính thiết kế**, chưa phải kết quả đo.

---

## X. TÀI LIỆU THAM KHẢO

[R1] P. Zhang, F. Zou, Z. Wu, N. Dai, S. Mark, M. Fu, J. Zhao, K. Li, "FeatherNets: Convolutional Neural Networks as Light as Feather for Face Anti-spoofing," *Proc. IEEE/CVF CVPR Workshops*, 2019. https://openaccess.thecvf.com/content_CVPRW_2019/papers/CFS/Zhang_FeatherNets_Convolutional_Neural_Networks_as_Light_as_Feather_for_Face_CVPRW_2019_paper.pdf

[R2] Z. Yu, Y. Qin, X. Xu, C. Zhao, Z. Wang, Z. Lei, G. Zhao, "Auto-Fas: Searching Lightweight Networks for Face Anti-Spoofing," *Proc. IEEE ICASSP*, pp. 996–1000, 2020. https://ieeexplore.ieee.org/document/9053587/

[R3] A. George, S. Marcel, "Deep Pixel-wise Binary Supervision for Face Presentation Attack Detection," *Proc. International Conference on Biometrics (ICB)*, 2019. https://publications.idiap.ch/attachments/papers/2019/George_ICB2019.pdf

[R4] Z. Yu, C. Zhao, Z. Wang, Y. Qin, Z. Su, X. Li, F. Zhou, G. Zhao, "Searching Central Difference Convolutional Networks for Face Anti-Spoofing," *Proc. IEEE/CVF CVPR*, 2020. https://openaccess.thecvf.com/content_CVPR_2020/html/Yu_Searching_Central_Difference_Convolutional_Networks_for_Face_Anti-Spoofing_CVPR_2020_paper.html

[R5] Z. Yu, Y. Qin, H. Zhao, X. Li, G. Zhao, "Dual-Cross Central Difference Network for Face Anti-Spoofing," *Proc. IJCAI*, 2021. https://www.ijcai.org/proceedings/2021/177

[R6] D. Wen, H. Han, A. K. Jain, "Face Spoof Detection With Image Distortion Analysis," *IEEE Trans. Information Forensics and Security*, vol. 10, no. 4, pp. 746–761, 2015. DOI: 10.1109/TIFS.2015.2400395

[R7] H. Chen, G. Hu, Z. Lei, Y. Chen, N. M. Robertson, S. Z. Li, "Attention-Based Two-Stream Convolutional Networks for Face Spoofing Detection," *IEEE Trans. Information Forensics and Security*, vol. 15, pp. 578–593, 2020. DOI: 10.1109/TIFS.2019.2922241

[R8] D. J. Jobson, Z. Rahman, G. A. Woodell, "A Multiscale Retinex for Bridging the Gap Between Color Images and the Human Observation of Scenes," *IEEE Trans. Image Processing*, vol. 6, no. 7, pp. 965–976, 1997.

[R9] K. Zuiderveld, "Contrast Limited Adaptive Histogram Equalization," in *Graphics Gems IV*, Academic Press, pp. 474–485, 1994.

[R10] Y. Liu, Y. Tai, J. Li, S. Ding, C. Wang, F. Huang, D. Li, W. Qi, R. Ji, "Aurora Guard: Real-Time Face Anti-Spoofing via Light Reflection," arXiv:1902.10311, 2019; bản mở rộng "Aurora Guard: Reliable Face Anti-Spoofing via Mobile Lighting System," arXiv:2102.00713, 2021. *(Bản thảo arXiv, chưa xác nhận đã qua bình duyệt.)*

[R11] V. Bazarevsky, Y. Kartynnik, A. Vakunov, K. Raveendran, M. Grundmann, "BlazeFace: Sub-millisecond Neural Face Detection on Mobile GPUs," *CVPR Workshop on Computer Vision for AR/VR*, 2019. arXiv:1907.05047

[R12] Y. Kartynnik, A. Ablavatski, I. Grishchenko, M. Grundmann, "Real-time Facial Surface Geometry from Monocular Video on Mobile GPUs," *CVPR Workshop on Computer Vision for AR/VR*, 2019. arXiv:1907.06724

[R13] M. Sandler, A. Howard, M. Zhu, A. Zhmoginov, L.-C. Chen, "MobileNetV2: Inverted Residuals and Linear Bottlenecks," *Proc. IEEE/CVF CVPR*, 2018.

[R14] A. Howard, M. Sandler, G. Chu, et al., "Searching for MobileNetV3," *Proc. IEEE/CVF ICCV*, 2019.

[R15] Y. Zhang, Z. Yin, Y. Li, G. Yin, J. Yan, J. Shao, Z. Liu, "CelebA-Spoof: Large-Scale Face Anti-Spoofing Dataset with Rich Annotations," *Proc. ECCV*, 2020. DOI: 10.1007/978-3-030-58610-2_5

[R16] Z. Boulkenafet, J. Komulainen, L. Li, X. Feng, A. Hadid, "OULU-NPU: A Mobile Face Presentation Attack Database with Real-World Variations," *Proc. IEEE FG*, pp. 612–618, 2017. DOI: 10.1109/FG.2017.77

[R17] ISO/IEC 30107-3:2017, *Information technology — Biometric presentation attack detection — Part 3: Testing and reporting*.

[R18] Z. Yu, Y. Qin, X. Li, C. Zhao, Z. Lei, G. Zhao, "Deep Learning for Face Anti-Spoofing: A Survey," *IEEE Trans. Pattern Analysis and Machine Intelligence*, vol. 45, no. 5, 2023. DOI: 10.1109/TPAMI.2022.3215850
