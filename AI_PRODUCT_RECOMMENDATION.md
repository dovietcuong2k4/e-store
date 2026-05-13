# AI Product Recommendation Feature

## 📋 Tổng quan

Tính năng này sử dụng AI (OpenRouter) để gợi ý các sản phẩm liên quan khi user xem chi tiết một sản phẩm. Hệ thống:
- Lấy sản phẩm hiện tại
- Tìm các sản phẩm liên quan (cùng category & brand)
- Gửi danh sách này cho AI
- AI chọn 3-5 sản phẩm tốt nhất và giải thích lý do
- Trả kết quả về frontend

---

## 🏗️ Cấu trúc Code

### 1. **DTOs** (Data Transfer Objects)
- **RecommendedProductDTO.java**: Dữ liệu của sản phẩm được gợi ý
  ```java
  - productId: Long
  - productName: String
  - price: Long
  - thumbnailUrl: String
  - reason: String (giải thích từ AI)
  ```

- **ProductRecommendationResponse.java**: Response chính
  ```java
  - recommendations: List<RecommendedProductDTO>
  - aiEnabled: boolean (có dùng AI hay không)
  - message: String (thông báo)
  ```

### 2. **Service** - ProductRecommendationService.java

**Phương thức chính:**
```java
public ProductRecommendationResponse getRecommendations(Long productId)
```

**Luồng thực thi:**
1. Lấy sản phẩm từ database
2. Tìm sản phẩm liên quan (cùng category + brand)
3. Nếu không có API key → fallback
4. Gửi prompt tới OpenRouter API
5. Parse response JSON
6. Trả về danh sách gợi ý

**Fallback Logic:**
- Nếu OpenRouter API có lỗi → trả về 5 sản phẩm đầu tiên từ danh sách liên quan
- Không phụ thuộc vào AI, luôn có kết quả

### 3. **Repository** - Thêm vào ProductRepository

```java
@Query("SELECT p FROM Product p WHERE "
        + "p.category.id = :categoryId AND p.brand.id = :brandId AND p.id != :currentProductId")
List<Product> findRelatedProducts(
        Long categoryId,
        Long brandId,
        Long currentProductId,
        Pageable pageable);
```

### 4. **Controller** - Endpoint mới trong ProductController

```
GET /api/products/{id}/recommendations
```

**Response Example:**
```json
{
  "recommendations": [
    {
      "productId": 2,
      "productName": "Samsung Galaxy S23",
      "price": 15000000,
      "thumbnailUrl": "https://...",
      "reason": "Có cùng chip Snapdragon 8 Gen 2, màn hình 6.1 inch, pin 3900mAh"
    },
    {
      "productId": 5,
      "productName": "Samsung Galaxy S23 Plus",
      "price": 17000000,
      "thumbnailUrl": "https://...",
      "reason": "Nâng cấp pin lên 4500mAh, RAM lên 8GB, giá hợp lý"
    }
  ],
  "aiEnabled": true,
  "message": "AI đã chọn những sản phẩm tốt nhất cho bạn"
}
```

---

## 🔧 Cấu hình

Application.yml đã có config OpenRouter:

```yaml
openrouter:
  api-key: 'sk-proj-...'
  model: openai/gpt-4o-mini
  responses-url: https://openrouter.ai/api/v1/responses
  timeout-seconds: 30
  max-output-tokens: 700
  max-catalog-products: 40
```

---

## 📝 Prompt mà hệ thống gửi tới AI

```
Tôi đang xem sản phẩm: iPhone 15
Đặc tính: CPU: A17 Pro, RAM: 6GB, Màn hình: 6.1", Giá: 30000000đ

Danh sách các sản phẩm liên quan (cùng category và brand):
1. ID: 2, Tên: iPhone 14, CPU: A16, RAM: 6GB, Màn hình: 6.1", Giá: 25000000đ
2. ID: 3, Tên: iPhone 13, CPU: A15, RAM: 4GB, Màn hình: 6.1", Giá: 20000000đ
...

Dựa trên sản phẩm tôi đang xem, hãy chọn ra 3-5 sản phẩm liên quan tốt nhất từ danh sách trên.
Trả về kết quả dưới dạng JSON array với cấu trúc: [{"productId": <id>, "reason": "lý do ngắn gọn"}]
Trả về CHỈ JSON array, không có text khác.
```

---

## 🚀 Cách sử dụng

### Frontend (React, Angular, Vue, etc.)

```javascript
// Khi user xem chi tiết sản phẩm
const productId = 1; // id của sản phẩm đang xem

fetch(`/api/products/${productId}/recommendations`)
  .then(res => res.json())
  .then(data => {
    console.log("AI Recommendations:", data.recommendations);
    // Hiển thị danh sách gợi ý
    data.recommendations.forEach(rec => {
      console.log(`${rec.productName} - ${rec.reason}`);
    });
  });
```

### Backend (cURL)

```bash
curl http://localhost:9091/api/products/1/recommendations
```

---

## 🎯 Luồng hoạt động chi tiết

```
User xem sản phẩm ID=1
   ↓
Frontend gọi GET /api/products/1/recommendations
   ↓
ProductController nhận request
   ↓
ProductRecommendationService.getRecommendations(1)
   ↓
ProductRepository.findById(1) → lấy sản phẩm hiện tại
   ↓
ProductRepository.findRelatedProducts(categoryId, brandId, 1) → lấy 10 sản phẩm liên quan
   ↓
Kiểm tra OpenRouter API key
   ↓
   ├─ Nếu không có API key → fallback (trả 5 sản phẩm đầu)
   └─ Nếu có API key:
       ↓
       buildRecommendationPrompt() → tạo prompt
       ↓
       askOpenRouterForRecommendations() → gửi HTTP request tới OpenRouter
       ↓
       OpenRouter trả về JSON ([ {productId, reason}, ... ])
       ↓
       parseRecommendationsFromResponse() → parse JSON & map với sản phẩm
       ↓
   ↓
Trả ProductRecommendationResponse về frontend
   ↓
Frontend render danh sách gợi ý
```

---

## ⚠️ Error Handling

Service có xử lý các trường hợp:

1. **Sản phẩm không tồn tại** → trả message "Sản phẩm không tồn tại"
2. **Không có sản phẩm liên quan** → trả message "Không có sản phẩm liên quan"
3. **OpenRouter API lỗi** → fallback to local list
4. **OpenRouter response format sai** → log error, trả empty list

---

## 📊 Performance

- **Database Query**: O(n) - lấy tối đa 10 sản phẩm
- **API Call**: ~1-3 giây (tùy OpenRouter response time)
- **Memory**: Minimal - chỉ parse JSON response

---

## 🔐 Security Notes

- API key đã được cấu hình trong environment variables
- Không log API key hoặc sensitive data
- Timeout 30s để tránh infinite wait
- Validate request trước khi gọi external API

---

## 📦 Files Tạo/Sửa

### Tạo mới:
```
src/main/java/com/example/eStore/dto/response/RecommendedProductDTO.java
src/main/java/com/example/eStore/dto/response/ProductRecommendationResponse.java
src/main/java/com/example/eStore/service/ProductRecommendationService.java
```

### Sửa đổi:
```
src/main/java/com/example/eStore/repository/ProductRepository.java (thêm method)
src/main/java/com/example/eStore/controller/api/ProductController.java (thêm endpoint + injection)
```

---

## 🧪 Testing

### Unit Test Example:

```java
@SpringBootTest
public class ProductRecommendationServiceTest {
    
    @Autowired
    private ProductRecommendationService service;
    
    @Test
    public void testGetRecommendations() {
        Long productId = 1L;
        ProductRecommendationResponse response = service.getRecommendations(productId);
        
        assertNotNull(response);
        assertNotNull(response.getRecommendations());
        assertTrue(response.getRecommendations().size() <= 5);
    }
    
    @Test
    public void testFallbackWhenProductNotFound() {
        ProductRecommendationResponse response = service.getRecommendations(99999L);
        
        assertEquals("Sản phẩm không tồn tại", response.getMessage());
        assertTrue(response.getRecommendations().isEmpty());
    }
}
```

---

## 💡 Tương lai (Optional Improvements)

1. **Caching**: Cache recommendations trong 1 giờ
2. **ML Model**: Thay thế OpenRouter bằng custom ML model cho tốc độ nhanh hơn
3. **User Preferences**: Dựa vào lịch sử mua hàng để gợi ý
4. **A/B Testing**: Test nhiều strategies, measure click-through rate
5. **Batch Processing**: Xử lý offline nếu có quá nhiều requests

---

## 📞 Support

Nếu có vấn đề:
- Check logs: `docker logs estore-app` hoặc check console
- Verify OpenRouter API key còn hợp lệ
- Kiểm tra connection tới OpenRouter
- Test endpoint bằng Postman
