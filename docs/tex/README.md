# Hướng dẫn Build Báo cáo LaTeX

## Yêu cầu

- XeLaTeX hoặc pdfLaTeX (khuyến nghị XeLaTeX để hỗ trợ Unicode tiếng Việt)
- Các package LaTeX: geometry, graphicx, float, booktabs, longtable, array, caption, hyperref, listings, xcolor, enumitem, fontspec (nếu dùng XeLaTeX)

## Cấu trúc thư mục

```
docs/tex/
├── structured_report.tex    # Báo cáo phương pháp có cấu trúc
├── ooad_report.tex         # Báo cáo phương pháp hướng đối tượng
├── images/                 # Thư mục chứa ảnh sơ đồ
│   ├── structured_*.png
│   └── ooad_*.png
└── README.md               # File này
```

## Build PDF

### Cách 1: Sử dụng XeLaTeX (khuyến nghị)

```bash
cd docs/tex
xelatex structured_report.tex
xelatex structured_report.tex  # Chạy 2 lần để tạo mục lục
xelatex ooad_report.tex
xelatex ooad_report.tex
```

### Cách 2: Sử dụng latexmk (tự động chạy nhiều lần)

```bash
cd docs/tex
latexmk -xelatex structured_report.tex
latexmk -xelatex ooad_report.tex
```

### Cách 3: Sử dụng pdfLaTeX (nếu không có XeLaTeX)

**Lưu ý:** Cần sửa file .tex để bỏ phần fontspec và dùng package khác cho tiếng Việt (ví dụ: babel, inputenc).

```bash
cd docs/tex
pdflatex structured_report.tex
pdflatex structured_report.tex
pdflatex ooad_report.tex
pdflatex ooad_report.tex
```

## Kiểm tra lỗi

Nếu có lỗi về đường dẫn ảnh, đảm bảo:
- Tất cả file ảnh PNG đã được render và đặt trong thư mục `images/`
- Đường dẫn trong file .tex là `images/filename.png` (relative path)

Nếu có lỗi về font, đảm bảo:
- Đã cài đặt font Times New Roman, Arial, Menlo trên hệ thống
- Hoặc sửa lại font trong file .tex

## Danh sách file ảnh cần có

### Structured Report:
- `structured_fdd.png`
- `structured_dfd_context.png`
- `structured_dfd_level0.png`
- `structured_dfd_level1_p4.png`
- `structured_dfd_level1_p5.png`
- `structured_erd.png`

### OOAD Report:
- `ooad_usecase.png`
- `ooad_activity_uc01.png`
- `ooad_activity_uc14.png`
- `ooad_domain_class.png`
- `ooad_design_class.png`
- `ooad_sequence_uc01.png`
- `ooad_sequence_uc04.png`
- `ooad_sequence_uc08.png`
- `ooad_sequence_uc13_uc14.png`
- `ooad_sequence_uc18_uc19.png`
- `ooad_sequence_uc21.png`

## Troubleshooting

**Lỗi: "File not found: images/xxx.png"**
- Kiểm tra file ảnh có tồn tại trong thư mục `images/` không
- Kiểm tra tên file có đúng chính tả không (case-sensitive)

**Lỗi: "Font not found"**
- Cài đặt font Times New Roman, Arial, Menlo
- Hoặc comment dòng `\setmainfont`, `\setsansfont`, `\setmonofont` và dùng font mặc định

**Lỗi: "Undefined control sequence"**
- Kiểm tra đã cài đặt đầy đủ các package LaTeX chưa
- Chạy lại với `-interaction=nonstopmode` để xem lỗi chi tiết

## Output

Sau khi build thành công, file PDF sẽ được tạo:
- `structured_report.pdf`
- `ooad_report.pdf`

Các file phụ sinh ra (.aux, .log, .toc, .out) có thể xóa sau khi build xong.


