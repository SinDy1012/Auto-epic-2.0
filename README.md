# Epic Seven Auto Adventure - bản thử nghiệm

Đây là **mã nguồn Android**, chưa phải APK và **chưa có bộ nhận diện map tự động**. App có giao diện tiếng Việt, cấp quyền overlay, bật Trợ năng và nút nổi Bắt đầu/Dừng. Không tự chạm vào game cho đến khi có thuật toán nhận diện đủ tin cậy.

## Tạo APK trên GitHub
1. Giải nén toàn bộ nội dung vào thư mục gốc repository GitHub.
2. Mở tab Actions > Build Android APK > Run workflow (hoặc push lên nhánh main).
3. Khi build thành công, tải artifact `EpicSevenAutoAdventure-debug-apk`.
4. Giải nén artifact để lấy `app-debug.apk`, cài trên Android.
5. Mở app, cấp quyền hiển thị trên ứng dụng khác và bật Trợ năng.

**Giới hạn:** Phiên bản này chỉ kiểm tra luồng cài đặt/quyền/nút nổi. Chưa tự vượt map, chưa tự nhận diện kết thúc trận, chưa tự di chuyển hay chuyển màn. Muốn hoàn thiện cần thêm bộ phân loại trạng thái ảnh và kiểm thử thực tế trên thiết bị.
