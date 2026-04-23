# Đặc Tả Use Case

Tài liệu này mô tả 14 use case chính của hệ thống bán hàng điện tử, được tổng hợp từ nghiệp vụ trong file `business-analysis.md`.

## 2.2.1. Đăng ký tài khoản

**Mô tả vắn tắt:**
Cho phép khách hàng tạo tài khoản mới để sử dụng các chức năng mua sắm của hệ thống.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng truy cập chức năng đăng ký.
  2. Hệ thống hiển thị form đăng ký.
  3. Người dùng nhập thông tin cá nhân, email và mật khẩu.
  4. Hệ thống kiểm tra dữ liệu và xác thực email chưa tồn tại.
  5. Hệ thống tạo tài khoản mới với vai trò mặc định là khách hàng.
  6. Hệ thống lưu dữ liệu vào bảng users và user_roles.
  7. Hệ thống hiển thị thông báo đăng ký thành công.
- **Luồng rẽ nhánh:**
  1. Nếu email đã tồn tại, hệ thống từ chối tạo tài khoản và thông báo lỗi.
  2. Nếu dữ liệu bắt buộc thiếu hoặc không hợp lệ, hệ thống yêu cầu nhập lại.
  3. Nếu xảy ra lỗi hệ thống, quá trình đăng ký bị dừng và hiển thị thông báo lỗi.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Email đăng ký chưa tồn tại trong hệ thống.

**Hậu điều kiện:** Tài khoản mới được tạo thành công.

**Điểm mở rộng:** Không có.

## 2.2.2. Đăng nhập hệ thống

**Mô tả vắn tắt:**
Cho phép người dùng xác thực thông tin đăng nhập để truy cập hệ thống theo đúng vai trò.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng mở chức năng đăng nhập.
  2. Hệ thống hiển thị form nhập email và mật khẩu.
  3. Người dùng nhập thông tin và nhấn đăng nhập.
  4. Hệ thống kiểm tra thông tin xác thực.
  5. Nếu hợp lệ, hệ thống sinh JWT và thông tin hồ sơ người dùng.
  6. Hệ thống chuyển hướng người dùng theo vai trò tương ứng.
- **Luồng rẽ nhánh:**
  1. Nếu email không tồn tại, hệ thống thông báo đăng nhập thất bại.
  2. Nếu mật khẩu không đúng, hệ thống từ chối truy cập.
  3. Nếu có lỗi mạng hoặc lỗi máy chủ, hệ thống hiển thị thông báo lỗi.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Tài khoản người dùng đã tồn tại trong hệ thống.

**Hậu điều kiện:** Người dùng được xác thực và có phiên đăng nhập hợp lệ.

**Điểm mở rộng:** Không có.

## 2.2.3. Quản lý tài khoản cá nhân

**Mô tả vắn tắt:**
Cho phép người dùng xem và cập nhật thông tin cá nhân của mình trong phạm vi được phép.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng truy cập trang tài khoản cá nhân.
  2. Hệ thống hiển thị thông tin hồ sơ hiện tại.
  3. Người dùng chỉnh sửa các thông tin cần thiết như họ tên, số điện thoại, địa chỉ.
  4. Người dùng nhấn lưu thay đổi.
  5. Hệ thống kiểm tra dữ liệu và cập nhật thông tin vào cơ sở dữ liệu.
  6. Hệ thống thông báo cập nhật thành công.
- **Luồng rẽ nhánh:**
  1. Nếu dữ liệu nhập không hợp lệ, hệ thống yêu cầu nhập lại.
  2. Nếu người dùng chưa đăng nhập, hệ thống chuyển đến màn hình đăng nhập.
  3. Nếu lưu dữ liệu thất bại, hệ thống hiển thị thông báo lỗi.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Người dùng đã đăng nhập hệ thống.

**Hậu điều kiện:** Thông tin cá nhân được cập nhật hoặc giữ nguyên nếu người dùng không lưu thay đổi.

**Điểm mở rộng:** Không có.

## 2.2.4. Xem và tìm kiếm sản phẩm

**Mô tả vắn tắt:**
Cho phép khách hàng và khách vãng lai tra cứu danh mục sản phẩm, xem chi tiết và thông tin liên quan trước khi mua hàng.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng mở trang danh sách sản phẩm.
  2. Hệ thống hiển thị danh sách sản phẩm theo phân trang.
  3. Người dùng nhập từ khóa hoặc chọn bộ lọc tìm kiếm.
  4. Hệ thống lọc và hiển thị kết quả phù hợp.
  5. Người dùng chọn một sản phẩm để xem chi tiết.
  6. Hệ thống hiển thị thông tin sản phẩm, hình ảnh, thương hiệu, danh mục và đánh giá.
- **Luồng rẽ nhánh:**
  1. Nếu không tìm thấy sản phẩm phù hợp, hệ thống hiển thị danh sách rỗng hoặc thông báo không có dữ liệu.
  2. Nếu sản phẩm không tồn tại, hệ thống thông báo lỗi không tìm thấy.
  3. Nếu chưa có đánh giá, hệ thống vẫn hiển thị chi tiết sản phẩm mà không có phần review.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Có dữ liệu sản phẩm trong hệ thống.

**Hậu điều kiện:** Người dùng nắm được thông tin cần thiết để quyết định mua hàng.

**Điểm mở rộng:** Không có.

## 2.2.5. Quản lý giỏ hàng

**Mô tả vắn tắt:**
Cho phép khách hàng thêm, cập nhật, xóa sản phẩm trong giỏ hàng trước khi thanh toán.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng chọn sản phẩm và nhấn thêm vào giỏ hàng.
  2. Hệ thống kiểm tra sản phẩm có tồn tại và còn bán hay không.
  3. Nếu sản phẩm đã có trong giỏ, hệ thống tăng số lượng.
  4. Người dùng thay đổi số lượng hoặc xóa sản phẩm khỏi giỏ.
  5. Hệ thống tính lại tổng tiền giỏ hàng.
  6. Hệ thống lưu trạng thái giỏ hàng và hiển thị kết quả cập nhật.
- **Luồng rẽ nhánh:**
  1. Nếu sản phẩm không tồn tại, hệ thống báo lỗi.
  2. Nếu sản phẩm không thuộc về người dùng hiện tại ở giỏ server-side, hệ thống từ chối thao tác.
  3. Nếu người dùng chưa đăng nhập, hệ thống có thể sử dụng giỏ tạm trên trình duyệt.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Sản phẩm tồn tại và người dùng có quyền thao tác với giỏ hàng.

**Hậu điều kiện:** Giỏ hàng phản ánh đúng thao tác mới nhất của người dùng.

**Điểm mở rộng:** Không có.

## 2.2.6. Quản lý đơn hàng

**Mô tả vắn tắt:**
Cho phép khách hàng tạo đơn hàng từ giỏ hàng và theo dõi, hủy đơn khi được phép.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng nhập thông tin người nhận và xác nhận đặt hàng.
  2. Hệ thống kiểm tra giỏ hàng, tồn kho và voucher nếu có.
  3. Hệ thống tạo đơn hàng mới với trạng thái khởi tạo.
  4. Hệ thống tạo chi tiết đơn hàng, trừ tồn kho và xóa giỏ hàng.
  5. Người dùng xem lịch sử đơn hàng.
  6. Nếu hợp lệ, người dùng có thể yêu cầu hủy đơn.
- **Luồng rẽ nhánh:**
  1. Nếu giỏ hàng trống, hệ thống không cho phép đặt hàng.
  2. Nếu tồn kho không đủ, hệ thống từ chối tạo đơn.
  3. Nếu voucher không hợp lệ, hệ thống loại bỏ hoặc báo lỗi theo quy định.
  4. Nếu đơn ở trạng thái không cho phép hủy, hệ thống từ chối yêu cầu.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Người dùng đã đăng nhập và giỏ hàng có sản phẩm hợp lệ.

**Hậu điều kiện:** Đơn hàng được tạo mới hoặc cập nhật trạng thái theo nghiệp vụ.

**Điểm mở rộng:** Không có.

## 2.2.7. Quản lý vận chuyển

**Mô tả vắn tắt:**
Cho phép nhân viên giao hàng xử lý các đơn được phân công, cập nhật trạng thái giao hàng và kết quả vận chuyển.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Shipper xem danh sách đơn được giao.
  2. Shipper bắt đầu vận chuyển đơn hàng.
  3. Hệ thống cập nhật trạng thái sang đang giao hàng.
  4. Shipper xác nhận giao thành công hoặc giao thất bại.
  5. Hệ thống cập nhật trạng thái cuối cùng và lưu lịch sử xử lý.
- **Luồng rẽ nhánh:**
  1. Nếu đơn không được phân công cho shipper hiện tại, hệ thống từ chối thao tác.
  2. Nếu chuyển trạng thái không hợp lệ, hệ thống báo lỗi.
  3. Nếu đơn giao thất bại, hệ thống ghi nhận trạng thái thất bại để xử lý lại.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Người dùng có vai trò Shipper và đơn hàng đã được phân công đúng người.

**Hậu điều kiện:** Trạng thái vận chuyển của đơn hàng được cập nhật và lưu vết.

**Điểm mở rộng:** Không có.

## 2.2.8. Quản lý sản phẩm

**Mô tả vắn tắt:**
Cho phép quản trị viên và nhân viên được phân quyền tạo, cập nhật, xóa sản phẩm, đồng thời quản lý hình ảnh sản phẩm.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng có quyền truy cập màn hình quản lý sản phẩm.
  2. Hệ thống hiển thị danh sách sản phẩm và form thêm/sửa.
  3. Người dùng nhập thông tin sản phẩm, danh mục, thương hiệu và hình ảnh.
  4. Hệ thống kiểm tra dữ liệu và lưu thay đổi.
  5. Hệ thống tải ảnh lên dịch vụ lưu trữ ảnh nếu có.
  6. Hệ thống cập nhật dữ liệu sản phẩm và thông báo kết quả.
- **Luồng rẽ nhánh:**
  1. Nếu danh mục hoặc thương hiệu không tồn tại, hệ thống báo lỗi.
  2. Nếu xóa sản phẩm có ràng buộc dữ liệu, hệ thống từ chối thao tác.
  3. Nếu tải ảnh thất bại, hệ thống thông báo lỗi xử lý hình ảnh.

**Các yêu cầu đặc biệt:** Quản lý ảnh sản phẩm phải đồng bộ với dữ liệu lưu trữ ảnh ngoài hệ thống.

**Tiền điều kiện:** Người dùng có quyền quản lý sản phẩm.

**Hậu điều kiện:** Dữ liệu sản phẩm và hình ảnh được cập nhật phù hợp.

**Điểm mở rộng:** Không có.

## 2.2.9. Quản lý danh mục

**Mô tả vắn tắt:**
Cho phép quản trị viên quản lý danh mục sản phẩm nhằm phục vụ việc phân loại và tìm kiếm sản phẩm dễ dàng hơn.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng truy cập chức năng quản lý danh mục.
  2. Hệ thống hiển thị danh sách danh mục hiện có.
  3. Người dùng chọn thêm mới hoặc chỉnh sửa danh mục.
  4. Người dùng nhập thông tin và nhấn lưu.
  5. Hệ thống kiểm tra dữ liệu và cập nhật bảng categories.
  6. Hệ thống hiển thị thông báo thao tác thành công.
- **Luồng rẽ nhánh:**
  1. Nếu danh mục đang được sử dụng bởi sản phẩm, hệ thống không cho phép xóa.
  2. Nếu dữ liệu không hợp lệ, hệ thống yêu cầu nhập lại.
  3. Nếu xảy ra lỗi hệ thống, hệ thống dừng xử lý và hiển thị thông báo lỗi.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Người dùng đã đăng nhập và có quyền quản lý danh mục.

**Hậu điều kiện:** Danh mục được thêm mới, cập nhật hoặc xóa theo quy định.

**Điểm mở rộng:** Không có.

## 2.2.10. Quản lý thương hiệu

**Mô tả vắn tắt:**
Cho phép người dùng có quyền quản lý thông tin thương hiệu phục vụ việc phân loại sản phẩm.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng mở chức năng quản lý thương hiệu.
  2. Hệ thống hiển thị danh sách thương hiệu.
  3. Người dùng chọn tạo mới hoặc cập nhật thương hiệu.
  4. Người dùng nhập tên và các thông tin liên quan.
  5. Hệ thống kiểm tra dữ liệu và lưu vào bảng brands.
  6. Hệ thống thông báo kết quả thao tác.
- **Luồng rẽ nhánh:**
  1. Nếu thương hiệu đang được sử dụng bởi sản phẩm, hệ thống có thể từ chối xóa.
  2. Nếu dữ liệu nhập không hợp lệ, hệ thống yêu cầu sửa lại.
  3. Nếu thương hiệu không tồn tại khi cập nhật, hệ thống báo lỗi.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Người dùng được phân quyền quản lý thương hiệu.

**Hậu điều kiện:** Dữ liệu thương hiệu được lưu hoặc cập nhật thành công.

**Điểm mở rộng:** Không có.

## 2.2.11. Quản lý người dùng và phân quyền

**Mô tả vắn tắt:**
Cho phép quản trị viên tạo, cập nhật, xóa người dùng và gán vai trò cho từng tài khoản.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Admin truy cập chức năng quản lý người dùng.
  2. Hệ thống hiển thị danh sách tài khoản.
  3. Admin tạo mới hoặc chỉnh sửa thông tin người dùng.
  4. Admin lựa chọn vai trò phù hợp cho tài khoản.
  5. Hệ thống kiểm tra email và vai trò hợp lệ.
  6. Hệ thống lưu thay đổi vào bảng users, roles và user_roles.
- **Luồng rẽ nhánh:**
  1. Nếu email đã tồn tại, hệ thống từ chối tạo mới.
  2. Nếu vai trò không hợp lệ hoặc không tồn tại, hệ thống báo lỗi.
  3. Nếu không tìm thấy người dùng, hệ thống không cho phép cập nhật hoặc xóa.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Người thực hiện có quyền Admin.

**Hậu điều kiện:** Thông tin người dùng và phân quyền được cập nhật theo yêu cầu.

**Điểm mở rộng:** Không có.

## 2.2.12. Quản lý voucher và khuyến mãi

**Mô tả vắn tắt:**
Cho phép tạo voucher khuyến mãi, kiểm tra điều kiện sử dụng và phân bổ voucher cho người dùng.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng có quyền truy cập chức năng quản lý voucher.
  2. Hệ thống hiển thị danh sách voucher hiện có.
  3. Người dùng tạo voucher mới với mã, loại giảm, giá trị giảm và thời gian hiệu lực.
  4. Người dùng chọn tài khoản để cấp voucher.
  5. Hệ thống kiểm tra mã voucher có duy nhất hay không.
  6. Hệ thống lưu voucher và bản ghi phân bổ voucher cho người dùng.
- **Luồng rẽ nhánh:**
  1. Nếu mã voucher bị trùng, hệ thống từ chối tạo mới.
  2. Nếu voucher không tồn tại khi phân bổ, hệ thống báo lỗi.
  3. Nếu người dùng mục tiêu không tồn tại, hệ thống không tạo bản ghi cấp voucher.

**Các yêu cầu đặc biệt:** Không có.

**Tiền điều kiện:** Người dùng có quyền quản lý voucher và mã voucher phải là duy nhất.

**Hậu điều kiện:** Voucher hoặc bản ghi cấp voucher được tạo thành công.

**Điểm mở rộng:** Không có.

## 2.2.13. Quản lý đánh giá sản phẩm

**Mô tả vắn tắt:**
Cho phép khách hàng đánh giá sản phẩm đã mua và đã được giao thành công.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng mở trang chi tiết sản phẩm.
  2. Hệ thống kiểm tra điều kiện đánh giá.
  3. Nếu đủ điều kiện, người dùng gửi số sao và nội dung nhận xét.
  4. Hệ thống kiểm tra tính hợp lệ của dữ liệu và quyền sở hữu.
  5. Hệ thống lưu, cập nhật hoặc xóa đánh giá theo yêu cầu.
  6. Hệ thống hiển thị kết quả và cập nhật tổng hợp đánh giá.
- **Luồng rẽ nhánh:**
  1. Nếu sản phẩm chưa được giao thành công, hệ thống không cho phép đánh giá.
  2. Nếu người dùng đã đánh giá trước đó, hệ thống yêu cầu cập nhật thay vì tạo mới.
  3. Nếu số sao không hợp lệ, hệ thống báo lỗi nhập liệu.

**Các yêu cầu đặc biệt:** Mỗi người dùng chỉ được có một đánh giá cho một sản phẩm.

**Tiền điều kiện:** Người dùng đã đăng nhập và có lịch sử mua hàng hợp lệ.

**Hậu điều kiện:** Đánh giá được lưu hoặc cập nhật theo hành động của người dùng.

**Điểm mở rộng:** Không có.

## 2.2.14. Quản lý liên hệ và hỗ trợ

**Mô tả vắn tắt:**
Cho phép khách hàng gửi yêu cầu liên hệ hoặc hỗ trợ đến hệ thống chăm sóc khách hàng.

**Luồng sự kiện:**
- **Luồng cơ bản:**
  1. Người dùng mở form liên hệ/hỗ trợ.
  2. Hệ thống hiển thị các trường cần nhập như họ tên, email, chủ đề và nội dung.
  3. Người dùng gửi thông tin liên hệ.
  4. Hệ thống kiểm tra dữ liệu và lưu yêu cầu vào cơ sở dữ liệu.
  5. Hệ thống gửi email thông báo hỗ trợ theo cơ chế bất đồng bộ.
  6. Hệ thống hiển thị thông báo gửi thành công.
- **Luồng rẽ nhánh:**
  1. Nếu dữ liệu không hợp lệ, hệ thống yêu cầu nhập lại.
  2. Nếu gửi email thất bại, bản ghi liên hệ vẫn được giữ nguyên.
  3. Nếu hệ thống lỗi, thao tác bị dừng và hiển thị thông báo tương ứng.

**Các yêu cầu đặc biệt:** Việc gửi email hỗ trợ được thực hiện bất đồng bộ và không làm rollback dữ liệu đã lưu.

**Tiền điều kiện:** Người dùng nhập đúng thông tin bắt buộc của form liên hệ.

**Hậu điều kiện:** Yêu cầu liên hệ được lưu và chuyển đến bộ phận hỗ trợ.

**Điểm mở rộng:** Không có.
