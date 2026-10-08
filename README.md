# OOPProject - E-Commerce Clothing Store

Hệ thống quản lý và bán hàng thời trang trực tuyến được phát triển bằng **Java thuần (Java Core)** kết hợp kiến trúc **DAO (Data Access Object)** và kết nối cơ sở dữ liệu **MySQL / Aiven Cloud** qua JDBC. Project áp dụng chặt chẽ các nguyên lý Lập trình hướng đối tượng (OOP).

---

## 🛠️ Công Nghệ Sử Dụng

* **Ngôn ngữ:** Java (JDK 17+)
* **Quản lý dự án & Thư viện:** Apache Maven
* **Cơ sở dữ liệu:** MySQL / Aiven Cloud Database
* **Kết nối CSDL:** JDBC (Java Database Connectivity)
* **Bảo mật:** BCrypt / SHA-256 (Mã hóa mật khẩu)
* **Kiến trúc mã nguồn:** Layered Architecture (Model - DAO - Service - Controller)

---

## ✨ Tính Năng Hệ Thống

### 1. Hệ Thống (Chung)
* **Đăng nhập / Đăng xuất:** Phân quyền giao diện theo vai trò (`Customer` hoặc `Admin`).
* **Đăng ký tài khoản:** Khách hàng đăng ký với các thông tin: email, nickname, mật khẩu, số điện thoại, giới tính.
  * Validation: Kiểm tra trùng lặp Email/SĐT, kiểm tra định dạng dữ liệu đầu vào.
* **Bảo mật tài khoản:** Băm mật khẩu bằng thuật toán SHA-256/BCrypt trước khi lưu vào CSDL (không lưu mật khẩu thô).
* **Quản lý mật khẩu:** Đổi mật khẩu cá nhân và chức năng reset mật khẩu dành cho Admin.

### 2. Phía Khách Hàng (Customer)

#### 🔍 Xem & Tìm kiếm Sản phẩm
* Xem danh sách sản phẩm gồm ảnh, tên, giá bán và thương hiệu.
* Lọc sản phẩm theo danh mục, thương hiệu, khoảng giá; tìm kiếm theo tên; sắp xếp theo giá hoặc mới nhất.
* Xem chi tiết nhóm sản phẩm: Mô tả, danh sách ảnh, biến thể (size, màu sắc), giá và số lượng tồn kho của từng biến thể.

#### 🛒 Giỏ hàng & Đặt hàng
* **Giỏ hàng trong bộ nhớ (In-memory List):** Thêm sản phẩm, thay đổi số lượng, xóa khỏi giỏ và tính tổng tiền.
* **Đặt hàng (Transaction):**
  * Chọn địa chỉ giao hàng, tự động tính phí vận chuyển.
  * Tạo đơn hàng (`Order`) và chi tiết đơn hàng (`OrderDetail`), tự động trừ số lượng tồn kho.
  * Xử lý giao dịch an toàn bằng **Database Transaction** (`commit`/`rollback`).
  * Chặn đặt hàng vượt quá tồn kho hoặc sản phẩm đã ngừng kinh doanh (`Is_active = false`).
* **Quản lý đơn hàng:** Xem lịch sử mua hàng, chi tiết đơn hàng và cho phép hủy đơn khi ở trạng thái chờ xác nhận (tự động hoàn lại tồn kho).

#### 👤 Tài khoản & Đánh giá
* **Hồ sơ cá nhân:** Xem và cập nhật thông tin cá nhân.
* **Sổ địa chỉ:** Thêm, sửa, xóa danh sách địa chỉ; thiết lập địa chỉ mặc định (ràng buộc mỗi tài khoản chỉ có 1 địa chỉ mặc định).
* **Đánh giá & Bình luận:** Đánh giá sao và nhận xét sản phẩm đã mua (Ràng buộc khóa chính `(Khách hàng, Sản phẩm)` - mỗi khách hàng chỉ đánh giá 1 lần/sản phẩm). Hiển thị điểm đánh giá trung bình.

---

### 3. Phía Quản Trị Viên (Admin)

#### 📂 Quản lý Danh mục & Thương hiệu
* Quản lý CRUD Danh mục sản phẩm và Thương hiệu (có logo).
* Chặn xóa Danh mục/Thương hiệu đang có sản phẩm liên kết (ràng buộc `NO ACTION`).

#### 📦 Quản lý Sản phẩm
* CRUD Nhóm sản phẩm (tên, mô tả, danh mục, thương hiệu).
* CRUD Biến thể sản phẩm (size, màu sắc, giá, tồn kho, bật/tắt trạng thái bán).
* Quản lý thuộc tính sản phẩm (`Attribute_value`) và danh sách hình ảnh sản phẩm.
* Cảnh báo sản phẩm sắp hết hàng (tồn kho dưới ngưỡng cảnh báo).

#### 🚚 Quản lý Đơn hàng
* Danh sách đơn hàng với bộ lọc nâng cao (trạng thái đơn, trạng thái thanh toán, khoảng thời gian).
* Cập nhật tiến độ đơn hàng: `Chờ xác nhận` $\rightarrow$ `Đang giao` $\rightarrow$ `Hoàn thành` / `Hủy`.
* Cập nhật trạng thái thanh toán và xuất/in hóa đơn đơn hàng.

#### 👥 Quản lý Người dùng & Nhật ký
* Xem danh sách tài khoản, khóa/mở khóa tài khoản khách hàng (`Status`).
* Khởi tạo tài khoản Admin mới.
* **Nhật ký thao tác (`Adjust`):** Ghi vết tự động lịch sử thêm, sửa, xóa sản phẩm/danh mục của Admin. Xem và lọc nhật ký theo Admin và ngày thực hiện.

#### 📊 Thống kê & Báo cáo
* Báo cáo doanh thu theo ngày, tháng, năm.
* Thống kê Top sản phẩm bán chạy và biểu đồ/bảng phân bổ đơn hàng theo trạng thái.

---

## 📁 Cấu Trúc Dự Án

```
OOPProject/
├── oop/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/oop/
│   │   │   │   ├── Config/          # Quản lý đọc cấu hình (ConfigLoader, DBContext)
│   │   │   │   ├── Controller/      # Điều hướng luồng ứng dụng
│   │   │   │   ├── DAO/             # Xử lý truy vấn SQL (Data Access Object)
│   │   │   │   ├── DTO/             # Data Transfer Objects
│   │   │   │   ├── Model/           # Các lớp Entity (User, Product, Order, v.v.)
│   │   │   │   ├── Service/         # Xử lý Logic nghiệp vụ (Business Logic)
│   │   │   │   └── Util/            # Thư viện tiện ích (Password Hash, Validation)
│   │   │   └── resources/
│   │   │       └── config.properties.example # Template file cấu hình CSDL
│   │   └── test/
│   └── pom.xml                      # Cấu hình Maven dependencies
├── .gitignore
└── README.md
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án

### 1. Yêu cầu hệ thống
* **Java Development Kit (JDK):** 17 trở lên
* **Maven:** 3.6+
* **Database:** MySQL 8.0+ hoặc tài khoản Aiven Cloud MySQL

### 2. Các bước khởi chạy

1. **Clone Repository:**
   ```bash
   git clone https://github.com/evilogre1112/OOPProject.git
   cd OOPProject
   ```

2. **Cấu hình Cơ sở dữ liệu:**
   * Tạo file `config.properties` trong thư mục `oop/src/main/resources/` dựa trên file template:
   ```properties
   db.host=YOUR_DATABASE_HOST
   db.port=YOUR_DATABASE_PORT
   db.name=defaultdb
   db.user=YOUR_DATABASE_USER
   db.password=YOUR_DATABASE_PASSWORD
   ```

3. **Biên dịch dự án bằng Maven:**
   ```bash
   cd oop
   mvn clean compile
   ```

4. **Chạy ứng dụng:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.oop.Main"
   ```

---

## 🛡️ Nguyên Tắc Lập Trình & Bảo Mật

* **Chống SQL Injection:** Sử dụng 100% `PreparedStatement` truyền tham số dạng `?` cho mọi truy vấn database.
* **Quản lý bộ nhớ:** Áp dụng cú pháp `try-with-resources` tự động đóng `Connection`, `PreparedStatement` và `ResultSet`.
* **Tính toàn vẹn dữ liệu (ACID):** Áp dụng `setAutoCommit(false)`, `commit()` và `rollback()` đối với tác vụ đặt hàng liên quan tới nhiều bảng dữ liệu.