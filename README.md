# OOPProject - Hệ Thống Quản Lý Và Bán Quần Áo (KIKI)

Ứng dụng desktop quản lý và bán hàng thời trang được phát triển bằng ngôn ngữ **Java (Java 25)** với giao diện **Java Swing**, tổ chức theo kiến trúc phân tầng **Model – DAO – Service – Controller** và kết nối cơ sở dữ liệu **MySQL trên Aiven Cloud** thông qua JDBC. Dự án có quy mô khoảng 11.000 dòng mã Java, áp dụng đầy đủ các nguyên lý Lập trình hướng đối tượng (OOP).

---

## 🛠️ Công Nghệ Và Môi Trường

- **Ngôn ngữ:** Java 25
- **Giao diện người dùng:** Java Swing (xử lý tác vụ nền với `SwingWorker`)
- **Quản lý dự án & Thư viện:** Apache Maven
- **Cơ sở dữ liệu:** MySQL trên Aiven Cloud (kết nối bắt buộc SSL - `sslMode=REQUIRED`)
- **Truy cập dữ liệu:** JDBC, driver `mysql-connector-j 8.3.0`
- **Bảo mật mật khẩu:** Băm SHA-256
- **Cấu hình:** File `config.properties` (nằm trong `.gitignore`, không đưa lên kho mã nguồn)
- **Kiến trúc mã nguồn:** Layered Architecture (`Model` – `DAO` – `Service` – `Controller`)

---

## 🏛️ Kiến Trúc & Thiết Kế Hướng Đối Tượng

### 1. Mô hình phân tầng
```text
Giao diện Swing (Controller)  →  Service  →    DAO   →  DBContext (JDBC)  →  MySQL Cloud
          ↑                       ↑             ↑
     Async/SwingWorker        Logic nghiệp vụ  Câu lệnh SQL
                              + phân quyền     (PreparedStatement)
```
Mỗi tầng chỉ gọi tầng ngay bên dưới nó: tầng giao diện không viết SQL, tầng DAO không chứa luật nghiệp vụ.

### 2. Áp dụng 4 nguyên lý OOP
- **Đóng gói (Encapsulation):** Toàn bộ thuộc tính của `Model` đều là `private`, truy cập qua getter/setter. `AccountService` giữ phiên đăng nhập ở `private currentAccount` và trả về bản sao phòng vệ qua `getCurrentAccount()` (`Account` cài đặt `Cloneable` và ghi đè `clone()`). Các hằng trạng thái đơn hàng (`PENDING`, `SHIPPING`, `COMPLETED`, `CANCELLED`, `UNPAID`, `PAID`) khai báo `private static final` trong `OrderService`.
- **Kế thừa (Inheritance):** Lớp trừu tượng `BaseEntity` định nghĩa `id` cùng `equals()`/`hashCode()` cho các thực thể (`Product`, `GroupProduct`, `Order`, `OrderDetail`, `Brand`, `Category`, `AttributeValue`). `Customer` và `Admin` kế thừa lớp cha `User`. Phía giao diện kế thừa `JPanel`/`JFrame` cùng các lớp nền tự xây dựng như `SectionPanel`, `ImagePanel`.
- **Đa hình (Polymorphism):** Ghi đè `equals()`, `hashCode()`, `toString()` (`User.toString()` sử dụng `getClass().getSimpleName()`). Sử dụng rộng rãi Functional Interface và Lambda (`Callable<T>`, `Consumer<T>`, `Runnable`, `Supplier<JComponent>`, `ActionListener`).
- **Trừu tượng (Abstraction):** `BaseEntity` trừu tượng hóa phần chung của thực thể; tầng `Service` và `DAO` che giấu chi tiết truy vấn; lớp `SQL` trong package `EXCEPTION` ánh xạ các mã lỗi MySQL (`1062`, `1451/1452`, `1213`, `2006/2013`...) sang thông báo tiếng Việt thân thiện; `Async` và `HomeProductLoader` đóng gói cơ chế chạy đa luồng.

### 3. Xử lý đa luồng với `SwingWorker`
- Lớp tiện ích `Controller.Common.Async` (`Async.run`, `Async.action`) đưa toàn bộ tác vụ truy vấn CSDL cloud và đọc/giải mã ảnh (sử dụng `setSourceSubsampling` giới hạn cạnh ~1200 px) sang luồng nền `doInBackground()`, sau đó cập nhật giao diện an toàn trên **Event Dispatch Thread (EDT)** trong `done()`.
- Tích hợp cơ chế đếm `ticket` trên `ShopPage` để chống kết quả tìm kiếm cũ ghi đè kết quả mới, tải trì hoãn (lazy loading) từng trang quản trị trên `AdminFrame` và lưu đệm trang chủ (`homePage`).

---

## ✨ Tính Năng Hệ Thống

### 1. Chức năng chung
- **Đăng ký tài khoản:** Nhập email, nickname, mật khẩu, số điện thoại, giới tính. Kiểm tra định dạng email, SĐT (10 chữ số, bắt đầu bằng `0`) và chính sách mật khẩu mạnh (tối thiểu 12 ký tự, gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt).
- **Đăng nhập / Đăng xuất:** So khớp mật khẩu băm SHA-256, từ chối tài khoản bị khóa, phân quyền giao diện theo vai trò (`Customer` hoặc `Admin`). Khi đăng xuất sẽ tự động xóa phiên tài khoản và giỏ hàng.
- **Quản lý mật khẩu & Hồ sơ:** Đổi mật khẩu cá nhân (yêu cầu khớp mật khẩu cũ), reset mật khẩu do Admin thực hiện; xem và cập nhật thông tin cá nhân (nickname, giới tính, địa chỉ giao hàng...).

### 2. Phía Khách Hàng (Customer)
- **Xem & Tìm kiếm sản phẩm:** Trang chủ (`LobbyFrame`) hiển thị sản phẩm mới; trang cửa hàng (`ShopPage`) hỗ trợ lọc theo danh mục, thương hiệu, khoảng giá, từ khóa, sắp xếp theo giá hoặc mới nhất và phân trang (`LIMIT ? OFFSET ?`).
- **Chi tiết sản phẩm (`ProductDetailPage`):** Hiển thị mô tả, hình ảnh, các biến thể (size, màu sắc), đơn giá, số lượng tồn kho, thuộc tính và đánh giá.
- **Giỏ hàng (`CartService` / `CartPage`):** Lưu trữ trong bộ nhớ (`List<OrderDetail>` tĩnh); hỗ trợ thêm, thay đổi số lượng, xóa và tính tổng tiền; tự động chặn sản phẩm ngừng bán hoặc vượt quá tồn kho.
- **Đặt hàng (`OrderService.placeOrder`):** Chọn địa chỉ giao hàng, tính phí vận chuyển, đọc lại đơn giá trực tiếp từ CSDL, tạo `Order` (`PENDING`, `UNPAID`) và `OrderDetail`, khóa dòng (`SELECT ... FOR UPDATE`) và trừ tồn kho (`Stock_quantity >= ?`) trong cùng một **Database Transaction** (`commit`/`rollback`).
- **Quản lý đơn hàng (`OrdersPage`):** Xem lịch sử mua hàng, chi tiết đơn và hủy đơn khi còn ở trạng thái `PENDING` và `UNPAID` (tự động hoàn lại tồn kho trong transaction).
- **Sổ địa chỉ (`AddressPage`):** Thêm, sửa, xóa và thiết lập địa chỉ giao hàng mặc định.
- **Đánh giá sản phẩm:** Chỉ cho phép đánh giá biến thể đã mua thuộc đơn hàng đã hoàn thành (`COMPLETED`); giới hạn mỗi khách hàng một đánh giá cho mỗi sản phẩm.

### 3. Phía Quản Trị Viên (Admin)
Cửa sổ quản trị (`AdminFrame`) gồm 8 phân hệ chức năng ở menu bên trái (mọi thao tác thay đổi dữ liệu đều kiểm tra quyền ở tầng `Service` và ném `PermissionException` nếu không hợp lệ):
1. **Sản phẩm:** CRUD nhóm sản phẩm, biến thể (size, màu, giá, tồn kho, bật/tắt bán), hình ảnh, thuộc tính; cảnh báo sản phẩm sắp hết hàng.
2. **Đơn hàng:** Lọc theo trạng thái đơn, trạng thái thanh toán, khoảng thời gian; cập nhật tiến độ theo đúng luồng `PENDING` $\rightarrow$ `SHIPPING` $\rightarrow$ `COMPLETED`; cập nhật thanh toán và xuất hóa đơn (`exportInvoice`).
3. **Danh mục:** CRUD danh mục, chặn xóa khi vẫn còn sản phẩm liên kết.
4. **Thương hiệu:** CRUD thương hiệu, chặn xóa khi vẫn còn sản phẩm liên kết.
5. **Thuộc tính:** Quản lý các giá trị thuộc tính dùng chung cho sản phẩm.
6. **Tài khoản:** Xem danh sách tài khoản, khóa/mở khóa tài khoản, khởi tạo tài khoản Admin mới.
7. **Nhật ký (`Adjust`):** Tự động ghi vết lịch sử thêm/sửa/xóa của Admin (`AdjustService.log`), hỗ trợ lọc theo Admin và ngày thực hiện.
8. **Thống kê:** Thống kê doanh thu theo ngày, tháng, năm; top sản phẩm bán chạy; phân bổ đơn hàng theo trạng thái bằng biểu đồ cột tự vẽ (`BarChart`).

---

## 📁 Cấu Trúc Dự Án

```text
OOPProject/
├── oop/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/oop/
│   │   │   │   ├── Config/              # Đọc cấu hình, kết nối CSDL (DBLoader, DBContext)
│   │   │   │   ├── Controller/
│   │   │   │   │   ├── Auth/            # Đăng nhập, đăng ký (AuthFrame, LoginPanel, RegisterPanel)
│   │   │   │   │   ├── Main/            # Giao diện khách hàng (LobbyFrame, ShopPage, CartPage, OrdersPage...)
│   │   │   │   │   ├── Admin/           # Giao diện quản trị (AdminFrame, ProductAdminPanel, OrderAdminPanel...)
│   │   │   │   │   └── Common/          # Thành phần dùng chung (Async, DataTable, BarChart, ImageUtil...)
│   │   │   │   ├── DAO/                 # Truy vấn SQL (AccountDAO, ProductDAO, OrderDAO, StatisticDAO...)
│   │   │   │   ├── DTO/                 # Đối tượng truyền dữ liệu thống kê (DailyRevenue, RevenueDTO)
│   │   │   │   ├── EXCEPTION/           # Ngoại lệ tùy biến (PermissionException, SQL)
│   │   │   │   ├── Model/               # Các thực thể (BaseEntity, User, Customer, Admin, Product, Order...)
│   │   │   │   ├── Service/             # Logic nghiệp vụ, phân quyền, transaction (OrderService, CartService...)
│   │   │   │   └── Util/                # Tiện ích (Extension - băm mật khẩu SHA-256)
│   │   │   └── resources/
│   │   │       └── config.properties    # File cấu hình kết nối CSDL (không đưa lên Git)
│   │   └── test/
│   └── pom.xml                          # Cấu hình Maven dependencies
├── .gitignore
└── README.md
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án

### 1. Yêu cầu hệ thống
- **Java Development Kit (JDK):** Java 25
- **Maven:** 3.6+
- **Database:** MySQL trên Aiven Cloud (hoặc MySQL Server hỗ trợ kết nối SSL)

### 2. Các bước khởi chạy

1. **Clone Repository:**
   ```bash
   git clone [https://github.com/evilogre1112/OOPProject.git](https://github.com/evilogre1112/OOPProject.git)
   cd OOPProject
   ```

2. **Cấu hình Cơ sở dữ liệu:**
   Tạo file `config.properties` trong thư mục `oop/src/main/resources/` (được `DBLoader` nạp qua `getResourceAsStream` và kết nối qua `DBContext` với tham số `sslMode=REQUIRED`):
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

## 🛡️ An Toàn Dữ Liệu & Bảo Mật

- **Chống SQL Injection:** 100% câu lệnh SQL sử dụng `PreparedStatement` với tham số `?`.
- **Quản lý tài nguyên kết nối:** Áp dụng `try-with-resources` để tự động đóng `Connection`, `PreparedStatement` và `ResultSet`.
- **Tính toàn vẹn dữ liệu & Xử lý đồng thời:** Sử dụng `setAutoCommit(false)`, `commit()`, `rollback()` (kèm `addSuppressed` để giữ nguyên lỗi gốc) kết hợp khóa dòng `SELECT ... FOR UPDATE` giúp ngăn chặn tình trạng bán vượt tồn kho khi nhiều phiên thao tác đồng thời.
- **Bảo vệ thông tin nhạy cảm:** Mật khẩu băm SHA-256 trước khi lưu trữ; đường truyền tới CSDL cloud bắt buộc mã hóa SSL; thông tin cấu hình tách rời khỏi hệ thống quản lý phiên bản.