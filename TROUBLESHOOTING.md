# 🔧 Troubleshooting Guide - AI Product Recommendation

## Common Issues & Solutions

---

## 1️⃣ Build/Compilation Errors

### ❌ Error: "Symbol 'ProductRecommendationService' not found"
```
Error: Cannot find symbol: class ProductRecommendationService
```

**Nguyên nhân**: Service chưa được compile hoặc import sai

**Giải pháp**:
```bash
# Clean và rebuild
mvn clean compile

# Hoặc IDE: Ctrl+Shift+F9 (Invalidate caches)
```

### ❌ Error: "Method 'findRelatedProducts' not found in ProductRepository"
```
Error: Method not found in interface ProductRepository
```

**Nguyên nhân**: Repository update không được picked up

**Giải pháp**:
```bash
# Reload Maven dependencies
# IDE: Right-click project → Maven → Reload Projects
# Hoặc command:
mvn reload
```

### ❌ Error: Missing @Autowired dependency

**Giải pháp**: Kiểm tra ProductController có inject đúng:
```java
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductRecommendationService productRecommendationService;
    // ...
}
```

---

## 2️⃣ Runtime Errors

### ❌ NullPointerException khi call /api/products/1/recommendations

```
java.lang.NullPointerException at ProductRecommendationService.getRecommendations()
```

**Nguyên nhân**: Có field bị null

**Giải pháp**:
1. Check logs để xem dòng nào
2. Verify product tồn tại:
   ```bash
   curl http://localhost:9091/api/products/detail/1
   ```
3. Kiểm tra category/brand của product:
   ```sql
   SELECT id, name, category_id, brand_id FROM products WHERE id = 1;
   ```

### ❌ ClassCastException

```
java.lang.ClassCastException: Cannot cast X to Y
```

**Nguyên nhân**: JSON parse error

**Giải pháp**:
```java
// Kiểm tra JSON response format:
try {
    JsonNode json = objectMapper.readTree(response);
    // Log full response để debug
    log.info("OpenRouter response: {}", response);
} catch (Exception e) {
    log.error("Parse error: ", e);
}
```

---

## 3️⃣ API Response Issues

### ❌ Response: `{"message": "Sản phẩm không tồn tại", "recommendations": []}`

**Nguyên nhân**: Product ID không tồn tại

**Kiểm tra**:
```bash
# Test với product ID hợp lệ
curl http://localhost:9091/api/products/detail/1  # Check exist

# Nếu không tìm thấy, xem danh sách:
curl http://localhost:9091/api/products?size=100
```

**Giải pháp**: Insert dữ liệu test hoặc dùng product ID hợp lệ

### ❌ Response: `{"message": "Không có sản phẩm liên quan", "recommendations": []}`

**Nguyên nhân**: Không có sản phẩm khác cùng category + brand

**Kiểm tra**:
```sql
-- Tìm category và brand của product 1
SELECT c.id, c.name, b.id, b.name 
FROM products p
LEFT JOIN categories c ON p.category_id = c.id
LEFT JOIN brands b ON p.brand_id = b.id
WHERE p.id = 1;

-- Sau đó check có bao nhiêu sản phẩm khác:
SELECT COUNT(*) FROM products 
WHERE category_id = ? AND brand_id = ? AND id != 1;
```

**Giải pháp**: 
- Thêm más products với cùng category/brand
- Hoặc modify query để search rộng hơn (chỉ category, hoặc chỉ brand)

---

## 4️⃣ OpenRouter API Issues

### ❌ Response: `{"aiEnabled": false, "message": "..."}`

**Nguyên nhân**: OpenRouter API call failed (fallback mode)

**Kiểm tra logs**:
```bash
# Xem error message
grep -i "openrouter\|recommendation" app.log

# Hoặc set log level
# application.yml:
# logging:
#   level:
#     com.example.eStore.service: DEBUG
```

### ❌ Error: `401 Unauthorized` từ OpenRouter

```
Error: 401 - Invalid API key
```

**Nguyên nhân**: API key không hợp lệ

**Giải pháp**:
1. Verify API key trong `application.yml`
   ```yaml
   openrouter:
     api-key: 'sk-proj-XXXX'  # Check format
   ```

2. Regenerate API key từ OpenRouter dashboard
   - Đăng nhập: https://openrouter.ai
   - API Keys → Create new secret key
   - Copy vào config

3. Test API key trực tiếp:
   ```bash
   curl -H "Authorization: Bearer sk-proj-XXXX" \
     https://openrouter.ai/api/v1/chat/completions
   ```

### ❌ Error: `429 - Rate limit exceeded`

```
Error: 429 - You have exceeded your rate limit
```

**Nguyên nhân**: Quá nhiều requests

**Giải pháp**:
1. Kiểm tra OpenRouter billing: https://openrouter.ai/account/billing/overview
2. Wait 1 minute trước call lại
3. Implement retry logic:
   ```java
   @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
   private List<RecommendedProductDTO> askOpenRouterForRecommendations(...) {
       // ...
   }
   ```

### ❌ Error: `Timeout` từ OpenRouter

```
java.net.SocketTimeoutException: Connection timeout
```

**Nguyên nhân**: OpenRouter chậm hoặc network issue

**Giải pháp**:
1. Tăng timeout:
   ```yaml
   openrouter:
     timeout-seconds: 60  # Từ 30 lên 60
   ```

2. Check network connection:
   ```bash
   ping openrouter.ai
   curl -v https://openrouter.ai/api/v1/chat/completions
   ```

3. Implement async calling:
   ```java
   @Async
   public void getRecommendationsAsync(Long productId) {
       // Call OpenRouter non-blocking
   }
   ```

---

## 5️⃣ Data Issues

### ❌ Recommendations list trống nhưng có related products

**Nguyên nhân**: JSON parse error hoặc AI trả format sai

**Debug**:
```java
// Thêm vào ProductRecommendationService:
log.debug("AI Response: {}", responseBody);  // Log full response

// Check format
if (!responseBody.contains("[")) {
    log.error("Invalid format - không có JSON array");
}
```

### ❌ Product thumbnail URL bị null

**Nguyên nhân**: Sản phẩm không có images

**Giải pháp**:
```java
// ProductRecommendationService - update:
private String getThumbnailUrl(Product product) {
    String url = product.getThumbnailUrl();
    return url != null ? url : "https://placeholder.com/200";
}
```

### ❌ Recommendation reasons bị lạ/không liên quan

**Nguyên nhân**: OpenRouter prompt cần cải thiện

**Giải pháp**: Update prompt trong `buildRecommendationPrompt()`:
```java
private String buildRecommendationPrompt(...) {
    // Thêm chi tiết hơn về product attributes
    // Format lại prompt để AI hiểu rõ hơn
    // Thử avec different temperature settings
}
```

---

## 6️⃣ Performance Issues

### ❌ Response time quá lâu (>5 giây)

**Nguyên nhân**: OpenRouter API chậm + database query slow

**Giải pháp**:
1. **Database optimization**:
   ```sql
   -- Add index
   CREATE INDEX idx_product_category_brand 
   ON products(category_id, brand_id);
   ```

2. **Implement caching**:
   ```java
   @Cacheable(value = "productRecommendations", key = "#productId")
   public ProductRecommendationResponse getRecommendations(Long productId) {
       // ...
   }
   ```

3. **Async OpenRouter calling**:
   ```java
   @Async
   public void fetchRecommendationsAsync(Long productId) {
       // Don't wait for response
   }
   ```

### ❌ Memory leak?

**Giải pháp**:
```java
// Không keep large objects trong memory
// Đảm bảo close HttpClient:
httpClient.close();  // or try-with-resources
```

---

## 7️⃣ Database Issues

### ❌ Database connection error

```
Error: com.mysql.cj.jdbc.exceptions.CommunicationsException
```

**Giải pháp**:
1. Verify MySQL running:
   ```bash
   mysql -u root -p -e "SELECT 1"
   ```

2. Check config:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/mydb
       username: root
       password: 123456
   ```

3. Create database nếu chưa có:
   ```bash
   mysql -u root -p -e "CREATE DATABASE mydb;"
   ```

### ❌ Table `products` không tồn tại

```
Error: Table 'mydb.products' doesn't exist
```

**Giải pháp**:
```bash
# Run migrations
mysql -u root -p mydb < src/main/resources/db/data.sql

# Hoặc set auto create:
# application.yml:
# spring:
#   jpa:
#     hibernate:
#       ddl-auto: create  # hoặc update
```

---

## 8️⃣ Frontend Issues

### ❌ CORS error khi gọi từ frontend

```
Access to XMLHttpRequest has been blocked by CORS policy
```

**Giải pháp**: 
Kiểm tra CorsConfig.java hoặc thêm annotation:
```java
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    // ...
}
```

### ❌ Frontend không hiển thị recommendations

1. Check network tab → see response
2. Verify JSON structure match với code
3. Log response trong console:
   ```javascript
   fetch(url).then(r => r.json()).then(d => console.log(d));
   ```

---

## 9️⃣ Testing Issues

### ❌ Unit test fail

**Giải pháp**:
```bash
# Run test với debug info
mvn -X test -Dtest=ProductRecommendationServiceTest

# Hoặc dùng IDE debugger
# Run → Debug Test
```

### ❌ Test không tìm database

**Giải pháp**: Thêm annotation:
```java
@SpringBootTest
@ActiveProfiles("test")
public class ProductRecommendationServiceTest {
    // ...
}

// application-test.yml:
# spring:
#   datasource:
#     url: jdbc:h2:mem:testdb
#   jpa:
#     hibernate:
#       ddl-auto: create-drop
```

---

## 🔟 Logging & Debugging

### Enable debug logging:

```yaml
# application.yml
logging:
  level:
    com.example.eStore.service.ProductRecommendationService: DEBUG
    org.springframework.web: DEBUG
    org.hibernate: DEBUG
```

### View logs:

```bash
# Tail logs
tail -f logs/estore.log

# Filter by keyword
grep "ProductRecommendation" logs/estore.log
```

### Add debug breakpoint:

```java
// Trong IDE:
// 1. Click left margin → set breakpoint
// 2. Run → Debug
// 3. Step through code
```

---

## 📞 When to Escalate

- ❌ OpenRouter API consistently down
- ❌ Database connection lost
- ❌ Server crash/OutOfMemory
- ❌ Critical security issue

**Contact**: bb10102004@gmail.com

---

## ✅ Quick Validation Checklist

- [ ] Service compiles without errors
- [ ] Repository method works
- [ ] Controller endpoint accessible
- [ ] Sample product exists in DB
- [ ] OpenRouter API key valid
- [ ] Response format correct
- [ ] Frontend can parse JSON
- [ ] No memory leaks
- [ ] Performance acceptable
- [ ] Error handling works

---

**Last Updated**: 2026-05-06
