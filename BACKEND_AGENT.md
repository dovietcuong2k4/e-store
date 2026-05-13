# Backend Agent Guide

Scan date: 2026-05-06
Project path: `C:\cuong\e-store`

This file is a compact scan of the backend project for coding agents. Use it before making changes so the agent understands the structure, runtime, conventions, and integration points.

## Stack

- Java 17
- Spring Boot 4.0.3
- Maven wrapper: `mvnw`, `mvnw.cmd`
- Spring Web MVC, Spring Data JPA, Spring Security, Validation, Mail
- MySQL 8 local development database
- JWT auth with `io.jsonwebtoken`
- Lombok
- Cloudinary SDK
- OpenRouter integration for product recommendation, semantic search, and review summaries
- VNPAY payment integration

## Run And Verify

```powershell
cd C:\cuong\e-store
docker compose up -d mysql
.\mvnw.cmd spring-boot:run
```

Backend runs on `http://localhost:9091`.

Common verification commands:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -Dtest=ProductAiSearchServiceTest test
.\mvnw.cmd -Dtest=ProductRecommendationServiceTest test
```

## Configuration

Main config file: `src/main/resources/application.yml`

Important config groups:

- `spring.datasource`: local MySQL connection to database `mydb`
- `spring.jpa.hibernate.ddl-auto`: currently `update`
- `server.port`: `9091`
- `openrouter`: API key/model/Responses URL/Embeddings URL/timeouts/catalog limits
- `vnpay`: sandbox payment config and return/IPN URLs
- `spring.mail`: Gmail SMTP config

Do not copy secrets into new docs, tests, commits, or logs. Prefer environment variables for secrets such as `AI_API_KEY`, mail password, and VNPAY secret.

## Project Structure

```text
e-store/
  pom.xml
  docker-compose.yml
  postman-collection.json
  AI_PRODUCT_RECOMMENDATION.md
  FRONTEND_INTEGRATION.md
  QUICK_START.md
  TROUBLESHOOTING.md
  docs/
    business-analysis.md
    use-case-specification.md
  sql/
    eStore.sql
    data.sql
    add_product_embedding.sql
    V__add_product_reviews.sql
    V__add_voucher_system.sql
    migrations/
      2026-04-17-order-status-refactor.sql
  src/
    main/
      java/com/example/eStore/
        EStoreApplication.java
        config/
          CloudinaryConfig.java
          CorsConfig.java
          OpenRouterProperties.java
          VnpayProperties.java
        controller/
          AdminController.java
          AdminVoucherController.java
          ApiExceptionHandler.java
          AuthController.java
          ShipperController.java
          StaffController.java
          TestController.java
          VoucherController.java
          api/
            BrandController.java
            CartController.java
            CategoryController.java
            ChatbotController.java
            ContactController.java
            OrderController.java
            PaymentController.java
            ProductController.java
            ProfileController.java
            ReviewController.java
            UploadController.java
        dto/
          BaseResultDTO.java
          AuthResponse.java
          LoginRequest.java
          RegisterRequest.java
          constants/
          request/
          response/
        entity/
          Brand.java
          Cart.java
          CartItem.java
          Category.java
          Contact.java
          Order.java
          OrderHistory.java
          OrderItem.java
          Product.java
          ProductImage.java
          ProductReview.java
          ReviewAiSummary.java
          Role.java
          User.java
          UserVoucher.java
          Voucher.java
          VoucherUsageHistory.java
        exception/
          AppException.java
          ContactMailException.java
        profile/
          ProfileField.java
          ProfileFieldType.java
          ProfileMetadataService.java
        repository/
          *Repository.java
        security/
          AuthenticatedUser.java
          CustomUserDetailsService.java
          JwtAuthFilter.java
          JwtService.java
          SecurityConfig.java
          SecurityUtils.java
        service/
          AuthService.java
          CartService.java
          ChatbotService.java
          FileUploadService.java
          MailService.java
          OpenRouterEmbeddingService.java
          OrderService.java
          ProductAiSearchService.java
          ProductRecommendationService.java
          ProductService.java
          ProfileService.java
          ReviewAiSummaryService.java
          ReviewService.java
          UserManagementService.java
          VnpayPaymentService.java
          VoucherService.java
      resources/
        application.yml
        schema_ai_summary.sql
    test/
      java/com/example/eStore/
        EStoreApplicationTests.java
        service/
          ProductAiSearchServiceTest.java
          ProductRecommendationServiceTest.java
```

## Layering Rules

- Controllers should stay thin. Put business rules in `service/`.
- Use request DTOs from `dto/request/` and response DTOs from `dto/response/`.
- Do not expose JPA entities directly from public API responses unless an existing controller already does so.
- Repositories should contain persistence queries only. Complex orchestration belongs in services.
- Keep transaction boundaries in services with `@Transactional`.
- Use `ApiResponseFactory` and `BaseResultDTO<T>` for wrapped API responses when the surrounding endpoint already follows that pattern.
- Global exception handling is in `controller/ApiExceptionHandler.java`.

## Response Shape

Generic backend wrapper:

```java
BaseResultDTO<T> {
    boolean success;
    String message;
    T data;
    String errorCode;
    Integer count;
}
```

Frontend usually unwraps `res.data`. If changing an endpoint response, update matching frontend models/services in `C:\cuong\Estore-fe`.

## Security And Roles

Security is configured in `security/SecurityConfig.java`.

Public or partly public endpoints include:

- `POST /api/auth/**`
- `POST /api/contact`
- `POST /api/orders`
- `POST /api/chatbot/**`
- `GET /api/payments/vnpay/**`
- `GET /api/products/**`
- `GET /api/reviews/**`
- `GET /api/categories/**`
- `GET /api/brands/**`

Protected role groups:

- Product write APIs: `ADMIN`, `STAFF`
- Category write APIs: `ADMIN`
- Brand write APIs: `ADMIN`, `STAFF` for create/update and `ADMIN` for delete
- Admin APIs: `ADMIN`
- Admin voucher APIs: `ADMIN`, `STAFF`
- Staff APIs: `STAFF`, `ADMIN`
- Shipper APIs: `SHIPPER`
- Customer APIs: `CUSTOMER`

Frontend role strings are `ROLE_ADMIN`, `ROLE_CUSTOMER`, `ROLE_SHIPPER`, `ROLE_STAFF`; Spring `hasRole("ADMIN")` expects granted authorities with `ROLE_` prefix.

## Main API Areas

- Auth: `AuthController`, `AuthService`
- Products: `controller/api/ProductController`, `ProductService`, `ProductRepository`
- AI product search: `ProductAiSearchService`, `OpenRouterEmbeddingService`, `ProductAiSearchRequest`
- Recommendations: `ProductRecommendationService`, `ProductRecommendationResponse`, `RecommendedProductDTO`
- Review summary: `ReviewAiSummaryService`, `ReviewAiSummary`, `ReviewAiSummaryResponse`
- Categories/brands: `CategoryController`, `BrandController`
- Cart/order: `CartController`, `OrderController`, `CartService`, `OrderService`
- Admin/staff/shipper workflows: `AdminController`, `StaffController`, `ShipperController`
- Payments: `PaymentController`, `VnpayPaymentService`
- Vouchers: `VoucherController`, `AdminVoucherController`, `VoucherService`
- Profile metadata/update: `ProfileController`, `ProfileService`, `profile/`
- Uploads: `UploadController`, `FileUploadService`, `CloudinaryConfig`
- Chatbot/contact/mail: `ChatbotController`, `ContactController`, `ChatbotService`, `MailService`

## Product And AI Notes

`Product` includes regular catalog fields plus `embeddingJson` mapped to `product_embedding` as `LONGTEXT`.

When changing product fields, update all relevant places:

- `entity/Product.java`
- `dto/request/ProductRequest.java`
- `dto/response/ProductResponse.java`
- `service/ProductService.java`
- repository queries in `ProductRepository.java`
- SQL migration or setup script under `sql/`
- frontend model/service in `Estore-fe/src/app/core/models/product.model.ts` and `product-api.service.ts`

AI search endpoint:

- `GET /api/products/ai-search`
- query model: `ProductAiSearchRequest`
- response: `BaseResultDTO<Page<ProductResponse>>`
- repository candidates: `findSemanticSearchCandidates`, `searchByKeywordAndFilters`
- frontend fallback behavior exists in `ProductApiService.searchProducts`

Recommendation endpoint:

- `GET /api/products/{id}/recommendations`
- response is currently `ProductRecommendationResponse` directly, not wrapped in `BaseResultDTO`

Review summary endpoint:

- `GET /api/products/{id}/review-summary`
- response is `BaseResultDTO<ReviewAiSummaryResponse>`

## Database Notes

Local MySQL service is defined in `docker-compose.yml`.

SQL setup and migrations live under `sql/`. Important scripts:

- `sql/eStore.sql`: schema/setup
- `sql/data.sql`: seed data
- `sql/add_product_embedding.sql`: product embedding column
- `sql/V__add_product_reviews.sql`: product reviews
- `sql/V__add_voucher_system.sql`: voucher system
- `sql/migrations/2026-04-17-order-status-refactor.sql`: order status refactor

There is no Flyway dependency in `pom.xml` at scan time, so SQL files are project scripts, not automatically guaranteed migrations unless the runtime is wired elsewhere.

## Agent Coding Checklist

- Run `git status --short` first; this project may have active uncommitted work.
- Do not touch `target/` or generated build output.
- Keep changes scoped to the feature or bug.
- Preserve existing API response shapes unless the frontend is updated in the same task.
- Add or update focused tests when changing service logic, security, payment, order workflow, or AI behavior.
- For cross-stack changes, update the frontend guide and frontend code models/services together.
- Never revert unrelated user changes.
