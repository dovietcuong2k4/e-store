# Reverse-Engineered Business Logic and System Behavior

## Scope
- Backend: `C:\cuong\e-store`
- Frontend: `C:\cuong\Estore-fe`
- Objective: Reverse-engineer business logic and system behavior from implemented source code.

## 1. Actors
1. Guest (unauthenticated user)
2. Customer (`ROLE_CUSTOMER`)
3. Staff (`ROLE_STAFF`)
4. Shipper (`ROLE_SHIPPER`)
5. Admin (`ROLE_ADMIN`)
6. External Mail Service (contact notification)
7. External Image Service (Cloudinary for upload/delete)

## 2. Use Cases by Actor

### Guest
1. UC-01 Register account
2. UC-02 Login
3. UC-03 Browse products and view product details
4. UC-04 Submit contact/support request

### Customer
1. UC-05 Manage shopping cart
2. UC-06 View personal vouchers and preview discount
3. UC-07 Create order
4. UC-08 View and cancel own orders
5. UC-09 Review purchased products

### Staff
1. UC-10 Process internal order lifecycle
2. UC-11 Assign shipper and handle re-delivery for failed shipment
3. UC-13 Manage product catalog (products/brands; category with restricted permission)
4. UC-14 Manage voucher templates and assign vouchers to users

### Shipper
1. UC-12 Process assigned deliveries

### Admin
1. UC-10 Process internal order lifecycle
2. UC-11 Assign shipper and handle re-delivery for failed shipment
3. UC-13 Full catalog management
4. UC-14 Manage voucher templates and assignment
5. UC-15 Manage users and roles

---

## 3. Detailed Use Cases

## UC-01
### 1. Use Case Name
Register account

### 2. Actor
Guest

### 3. Description
Create a new customer account to use shopping features.

### 4. Preconditions
- Email does not already exist.
- `ROLE_CUSTOMER` exists in role master data.

### 5. Main Flow (Business Flow)
1. Guest enters registration info.
2. System checks email uniqueness.
3. System creates user with default Customer role.
4. Guest receives registration success message.

### 6. System Flow
1. API call: `POST /api/auth/register`
2. Backend processing:
- Validate duplicate email.
- Hash password using BCrypt.
- Assign role `ROLE_CUSTOMER`.
3. DB interaction:
- Insert into `users`.
- Insert into `user_roles`.

### 7. Alternative Flows
- Email already exists.
- Missing required data at UI level.

### 8. Postconditions
- New user account is created.
- User is not automatically logged in.

### 9. Data Involved
- Input: `fullName`, `email`, `password`, `phone`, `address`
- Output: success/error message
- Tables/entities: `users`, `roles`, `user_roles`

## UC-02
### 1. Use Case Name
Login

### 2. Actor
Guest

### 3. Description
Authenticate user and establish role-based session.

### 4. Preconditions
- Account exists.
- Password is valid.

### 5. Main Flow (Business Flow)
1. User submits email and password.
2. System validates credentials.
3. System issues JWT and user profile.
4. Frontend redirects by role: Admin/Staff/Shipper/Customer.

### 6. System Flow
1. API call: `POST /api/auth/login`
2. Backend processing:
- Find user by email.
- Compare password hash.
- Generate JWT with `email` + `userId` claim.
3. DB interaction:
- Read from `users`, `roles`, `user_roles`.

### 7. Alternative Flows
- Email not found.
- Invalid password.
- Network/server error.

### 8. Postconditions
- JWT token stored on frontend.
- Authenticated session active.

### 9. Data Involved
- Input: `email`, `password`
- Output: `token`, user profile, roles
- Tables/entities: `users`, `roles`, `user_roles`

## UC-03
### 1. Use Case Name
Browse products and view details

### 2. Actor
Guest, Customer

### 3. Description
Explore catalog and product information before purchase.

### 4. Preconditions
- Product catalog exists.

### 5. Main Flow (Business Flow)
1. User opens product listing.
2. User searches/filters/sorts products.
3. User opens product detail page.
4. User reads specs, images, reviews, and related products.

### 6. System Flow
1. API calls:
- `GET /api/products`
- `GET /api/products/detail/{id}`
- `GET /api/reviews/{productId}`
2. Backend processing:
- Paginated listing by keyword.
- Product detail retrieval.
- Review summary with average rating.
3. DB interaction:
- `products`, `product_images`, `categories`, `brands`, `product_reviews`

### 7. Alternative Flows
- Product not found.
- No review data available.

### 8. Postconditions
- User gets sufficient information to decide purchase.

### 9. Data Involved
- Input: `keyword`, `page`, `size`, `productId`
- Output: product list, product detail, review summary
- Tables/entities: `products`, `product_images`, `categories`, `brands`, `product_reviews`

## UC-04
### 1. Use Case Name
Submit contact/support request

### 2. Actor
Guest, Customer

### 3. Description
Send support inquiry to service team.

### 4. Preconditions
- Contact form is valid (`name`, `email`, `subject`, `message`).

### 5. Main Flow (Business Flow)
1. User fills contact form.
2. System stores contact record.
3. System asynchronously sends support email.
4. User sees success confirmation.

### 6. System Flow
1. API call: `POST /api/contact`
2. Backend processing:
- Validate request.
- Save contact with status `NEW`.
- Trigger async mail sending.
3. DB interaction:
- Insert into `contacts`.

### 7. Alternative Flows
- Validation fails.
- Async mail fails (does not rollback saved contact).

### 8. Postconditions
- Contact ticket is persisted.

### 9. Data Involved
- Input: `name`, `email`, `phone`, `subject`, `message`
- Output: success/error message
- Tables/entities: `contacts`

## UC-05
### 1. Use Case Name
Manage shopping cart

### 2. Actor
Customer (server cart), Guest (local cart on frontend)

### 3. Description
Add/update/remove items before checkout.

### 4. Preconditions
- Product exists when adding.
- Login required for server-side cart APIs.

### 5. Main Flow (Business Flow)
1. User adds product to cart.
2. System increments quantity if product already exists in cart.
3. User updates quantity or removes line item.
4. System recalculates cart total.
5. Guest cart is merged into account cart on login.

### 6. System Flow
1. API calls:
- `POST /api/cart/add`
- `GET /api/cart`
- `PUT /api/cart/item/{id}`
- `DELETE /api/cart/item/{id}`
- `DELETE /api/cart/clear`
2. Backend processing:
- Auto-create cart if absent.
- Enforce cart item ownership.
- Recalculate total after each mutation.
3. DB interaction:
- `carts`, `cart_items`, `products`, `users`

### 7. Alternative Flows
- Product not found.
- Cart item does not belong to current user.
- Frontend falls back to local storage on API failure.

### 8. Postconditions
- Cart state matches latest user action.

### 9. Data Involved
- Input: `productId`, `quantity`, `itemId`
- Output: operation status + cart snapshot
- Tables/entities: `carts`, `cart_items`, `products`

## UC-06
### 1. Use Case Name
View personal vouchers and preview discount

### 2. Actor
Customer

### 3. Description
Check voucher eligibility and discount value before placing order.

### 4. Preconditions
- User is logged in.
- Voucher is assigned to user.

### 5. Main Flow (Business Flow)
1. Customer opens checkout.
2. System loads customer voucher list.
3. Customer selects voucher.
4. System returns eligibility and computed discount preview.

### 6. System Flow
1. API calls:
- `GET /api/vouchers/my`
- `POST /api/vouchers/preview`
2. Backend processing:
- Mark `AVAILABLE -> EXPIRED` on fetch if end date passed.
- Preview computes discount only; does not mark `USED`.
3. DB interaction:
- `user_vouchers`, `vouchers`

### 7. Alternative Flows
- Voucher not owned by user.
- Voucher expired/not active/not started/minimum order not met.
- Preview returns `eligible=false` with reason code.

### 8. Postconditions
- Customer has accurate pre-checkout discount information.

### 9. Data Involved
- Input: `userVoucherId`, `orderTotal`
- Output: `eligible`, `discountAmount`, `reasonCode`
- Tables/entities: `user_vouchers`, `vouchers`

## UC-07
### 1. Use Case Name
Create order

### 2. Actor
Customer

### 3. Description
Place order from cart, optionally applying voucher.

### 4. Preconditions
- Logged in.
- Cart is not empty.
- Stock is sufficient.
- Receiver information is provided.

### 5. Main Flow (Business Flow)
1. Customer enters receiver details.
2. Customer optionally selects voucher.
3. Customer confirms checkout.
4. System creates order with status `CREATED`.
5. System deducts stock, creates order items, clears cart.
6. If voucher used, system marks voucher `USED`.

### 6. System Flow
1. API call: `POST /api/orders`
2. Backend processing:
- Load current cart.
- Lock product rows (pessimistic lock).
- Validate stock.
- Deduct stock + increment sold quantity.
- Create `order_items`.
- Validate and compute voucher discount.
- Finalize voucher usage after order persisted.
- Insert initial `order_history` record.
3. DB interaction:
- `orders`, `order_items`, `products`, `carts`, `cart_items`, `user_vouchers`, `voucher_usage_history`, `order_history`

### 7. Alternative Flows
- Not authenticated.
- Cart missing/empty.
- Out-of-stock at checkout time.
- Voucher invalid.

### 8. Postconditions
- New order created.
- Cart emptied.
- Stock and voucher state updated.

### 9. Data Involved
- Input: `receiverName`, `receiverPhone`, `receiverAddress`, `note`, `userVoucherId`
- Output: created `orderId` or error
- Tables/entities: `orders`, `order_items`, `products`, `carts`, `cart_items`, `user_vouchers`, `voucher_usage_history`, `order_history`

## UC-08
### 1. Use Case Name
View and cancel own orders

### 2. Actor
Customer

### 3. Description
Track order history and cancel order when allowed.

### 4. Preconditions
- Logged in.
- Order belongs to current customer.

### 5. Main Flow (Business Flow)
1. Customer opens order history page.
2. System loads customer orders sorted by date.
3. Customer requests cancellation.
4. System validates role + state transition.
5. If valid, status becomes `CANCELLED`.

### 6. System Flow
1. API calls:
- `GET /api/orders`
- `PUT /api/orders/{id}/cancel`
2. Backend processing:
- Fetch by current `userId`.
- Validate allowed transition for `ROLE_CUSTOMER`.
- Record transition in `order_history`.
3. DB interaction:
- `orders`, `order_items`, `order_history`

### 7. Alternative Flows
- Attempt to cancel order not owned by customer.
- Attempt to cancel from invalid status.
- Voucher remains `USED` even after cancellation; cancellation audit is logged.

### 8. Postconditions
- Order remains unchanged or moved to `CANCELLED`.

### 9. Data Involved
- Input: `orderId`
- Output: order list and cancellation result message
- Tables/entities: `orders`, `order_items`, `order_history`, `voucher_usage_history`

## UC-09
### 1. Use Case Name
Review purchased product

### 2. Actor
Customer

### 3. Description
Create/update/delete product reviews only after successful delivery.

### 4. Preconditions
- Logged in.
- Product was delivered in at least one customer order.
- One review per user per product.

### 5. Main Flow (Business Flow)
1. Customer opens product detail.
2. System checks review eligibility.
3. If eligible, customer submits rating/comment.
4. Customer can update or delete own review.

### 6. System Flow
1. API calls:
- `GET /api/reviews/{productId}/eligibility`
- `POST /api/reviews/{productId}`
- `PUT /api/reviews/{productId}`
- `DELETE /api/reviews/{productId}`
2. Backend processing:
- Validate product existence.
- Validate delivered purchase history.
- Enforce unique review per user/product.
3. DB interaction:
- `product_reviews`, `orders`, `order_items`, `products`, `users`

### 7. Alternative Flows
- Product not purchased/delivered yet.
- Already reviewed.
- Invalid rating (must be 1..5).
- Review owner mismatch.

### 8. Postconditions
- Review state is updated according to user action.

### 9. Data Involved
- Input: `productId`, `rating`, `comment`
- Output: review detail/summary, eligibility result
- Tables/entities: `product_reviews`, `orders`, `order_items`, `products`

## UC-10
### 1. Use Case Name
Process internal order lifecycle

### 2. Actor
Staff, Admin

### 3. Description
Move orders through pre-delivery operational statuses.

### 4. Preconditions
- Actor has valid role.
- Current status allows requested transition.

### 5. Main Flow (Business Flow)
1. Operator opens order management dashboard.
2. Move `CREATED -> PROCESSING`.
3. Move `PROCESSING -> READY_FOR_SHIPPING`.
4. Cancel order when policy allows.

### 6. System Flow
1. Staff APIs:
- `GET /api/staff/orders`
- `PUT /api/staff/orders/{id}/process`
- `PUT /api/staff/orders/{id}/ready`
- `PUT /api/staff/orders/{id}/cancel`
2. Admin APIs:
- `GET /api/admin/orders`
- `PUT /api/admin/orders/{id}/process`
- `PUT /api/admin/orders/{id}/ready`
- `PUT /api/admin/orders/{id}/cancel`
3. Backend processing:
- Validate role and transition matrix.
- Update status and timestamps if applicable.
- Insert `order_history`.
4. DB interaction:
- `orders`, `order_history`

### 7. Alternative Flows
- Invalid transition path.
- Unauthorized actor for requested operation.

### 8. Postconditions
- Order transitions to new valid state with audit trail.

### 9. Data Involved
- Input: `orderId`, target action/status
- Output: transition result message
- Tables/entities: `orders`, `order_history`

## UC-11
### 1. Use Case Name
Assign shipper and handle failed delivery retry

### 2. Actor
Staff, Admin

### 3. Description
Assign delivery owner and prepare failed deliveries for re-dispatch.

### 4. Preconditions
- Order status is `READY_FOR_SHIPPING` or `DELIVERY_FAILED`.
- Selected user has `ROLE_SHIPPER`.

### 5. Main Flow (Business Flow)
1. Operator selects order for assignment.
2. Operator selects shipper.
3. System assigns shipper to order.
4. If order was `DELIVERY_FAILED`, status is reset to `READY_FOR_SHIPPING`.

### 6. System Flow
1. API calls:
- `PUT /api/staff/orders/{id}/assign-shipper`
- `PUT /api/admin/orders/{id}/assign-shipper`
2. Backend processing:
- Validate operator role (staff/admin).
- Validate order status allowed for assignment.
- Validate target user role is shipper.
3. DB interaction:
- `orders`, `users`, `user_roles`

### 7. Alternative Flows
- Target user is not a shipper.
- Assignment attempted in invalid order status.
- Shipper not found.

### 8. Postconditions
- Order has assigned shipper and proper status for shipping phase.

### 9. Data Involved
- Input: `orderId`, `shipperId`
- Output: assignment result
- Tables/entities: `orders`, `users`, `user_roles`

## UC-12
### 1. Use Case Name
Process assigned delivery

### 2. Actor
Shipper

### 3. Description
Handle pickup/start delivery and complete/fail delivery outcome.

### 4. Preconditions
- Logged in as `ROLE_SHIPPER`.
- Order is assigned to current shipper.

### 5. Main Flow (Business Flow)
1. Shipper views assigned orders.
2. Start shipping (`READY_FOR_SHIPPING -> SHIPPING`).
3. Finalize result (`SHIPPING -> DELIVERED` or `DELIVERY_FAILED`).

### 6. System Flow
1. API calls:
- `GET /api/shipper/orders`
- `PUT /api/shipper/orders/{id}/start`
- `PUT /api/shipper/orders/{id}/deliver`
- `PUT /api/shipper/orders/{id}/fail`
2. Backend processing:
- Validate shipper ownership.
- Validate transition by state machine.
- Set `shippingDate`/`receivedDate` where applicable.
- Insert history record.
3. DB interaction:
- `orders`, `order_history`

### 7. Alternative Flows
- Order not assigned to current shipper.
- Invalid status transition attempt.

### 8. Postconditions
- Delivery result is persisted with audit trail.

### 9. Data Involved
- Input: `orderId`, action (`start`/`deliver`/`fail`)
- Output: status update result
- Tables/entities: `orders`, `order_history`

## UC-13
### 1. Use Case Name
Manage product catalog (products/categories/brands/images)

### 2. Actor
Admin, Staff (partial), External Image Service

### 3. Description
Maintain sellable product data and media assets.

### 4. Preconditions
- Actor has required permission by operation.

### 5. Main Flow (Business Flow)
1. Operator creates/updates/deletes product.
2. Operator assigns category and brand.
3. Operator uploads product images and marks thumbnail.
4. Operator manages categories and brands.

### 6. System Flow
1. Product APIs:
- `GET /api/products`
- `POST /api/products/create`
- `PUT /api/products/update/{id}`
- `DELETE /api/products/delete/{id}`
2. Brand APIs:
- `GET /api/brands`
- `POST /api/brands`
- `PUT /api/brands/{id}`
- `DELETE /api/brands/{id}`
3. Category APIs:
- `GET /api/categories`
- `POST /api/categories/create`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`
4. Upload API:
- `POST /api/upload`
5. Backend processing:
- Validate category and brand references.
- Replace image set on update.
- Delete obsolete Cloudinary files after DB update/delete.
6. DB interaction:
- `products`, `product_images`, `categories`, `brands`

### 7. Alternative Flows
- Category/brand not found.
- Referential constraint violations when deleting category/brand in use.
- Frontend category create path mismatch vs backend create endpoint.
- Staff has category screen but backend write access for category is admin-only.

### 8. Postconditions
- Catalog data and image metadata are synchronized.

### 9. Data Involved
- Input: product specs, stock, categoryId, brandId, image list/form-data file
- Output: saved product/brand/category data
- Tables/entities: `products`, `product_images`, `categories`, `brands`

## UC-14
### 1. Use Case Name
Manage voucher templates and assign vouchers

### 2. Actor
Admin, Staff

### 3. Description
Create discount programs and distribute vouchers to users.

### 4. Preconditions
- Actor is admin/staff.
- Voucher code is unique.

### 5. Main Flow (Business Flow)
1. Operator creates voucher template (code/type/value/conditions/time range).
2. Operator selects target user.
3. System creates user voucher instance with `AVAILABLE` status.

### 6. System Flow
1. API calls:
- `GET /api/admin/vouchers`
- `POST /api/admin/vouchers`
- `POST /api/admin/vouchers/assign`
2. Backend processing:
- Validate code uniqueness.
- Persist template as active.
- Assign to user by creating user voucher row.
3. DB interaction:
- `vouchers`, `user_vouchers`, `users`

### 7. Alternative Flows
- Duplicate voucher code.
- Voucher template not found.
- Target user not found.
- Frontend has update/delete template actions but backend currently exposes list/create/assign only.

### 8. Postconditions
- Voucher template and/or assignment records are persisted.

### 9. Data Involved
- Input: `code`, `discountType`, `discountValue`, `minOrderValue`, `startDate`, `endDate`, `userId`
- Output: template/assignment success or failure
- Tables/entities: `vouchers`, `user_vouchers`, `users`

## UC-15
### 1. Use Case Name
Manage users and roles

### 2. Actor
Admin

### 3. Description
Maintain system users and role assignments.

### 4. Preconditions
- Actor is admin.
- Assigned roles exist.

### 5. Main Flow (Business Flow)
1. Admin views user list.
2. Admin creates user with role set.
3. Admin updates user profile and roles.
4. Admin deletes user if no longer needed.

### 6. System Flow
1. API calls:
- `GET /api/admin/users`
- `GET /api/admin/users/{id}`
- `POST /api/admin/users`
- `PUT /api/admin/users/{id}`
- `DELETE /api/admin/users/{id}`
2. Backend processing:
- Validate unique email.
- Resolve role names to role entities.
- Hash password on create/update when provided.
3. DB interaction:
- `users`, `roles`, `user_roles`

### 7. Alternative Flows
- Duplicate email.
- Role not found.
- User not found for update/delete.

### 8. Postconditions
- User/role data updated according to admin request.

### 9. Data Involved
- Input: `fullName`, `email`, `password`, `phone`, `address`, `roles`
- Output: user detail/result message
- Tables/entities: `users`, `roles`, `user_roles`

---

## 4. Core System Flows

### 4.1 RBAC and Security Flow
1. User logs in and receives JWT.
2. Frontend sends `Authorization: Bearer <token>` for protected APIs.
3. Backend security filter validates token and resolves user context.
4. Access is granted/denied by endpoint role policy.

### 4.2 Order State Machine
- `CREATED -> PROCESSING` (Admin/Staff)
- `CREATED -> CANCELLED` (Admin/Staff/Customer)
- `PROCESSING -> READY_FOR_SHIPPING` (Admin/Staff)
- `PROCESSING -> CANCELLED` (Admin/Staff)
- `READY_FOR_SHIPPING -> SHIPPING` (Shipper)
- `SHIPPING -> DELIVERED` (Shipper)
- `SHIPPING -> DELIVERY_FAILED` (Shipper)
- `DELIVERY_FAILED -> READY_FOR_SHIPPING` (Admin/Staff)

### 4.3 Voucher Lifecycle
- `AVAILABLE -> USED` when order is finalized.
- `AVAILABLE -> EXPIRED` when voucher is past end date.
- On order cancel after voucher usage, voucher remains `USED`; cancellation is logged.

---

## 5. Exception and Edge Cases
1. Checkout attempts without authenticated user cannot complete successfully in backend business context.
2. Product stock is locked pessimistically during order creation to reduce overselling race condition.
3. Review permission requires delivered purchase, not just order placement.
4. Shipper can only operate orders assigned to that exact shipper account.
5. Contact email async failure does not rollback saved contact record.
6. Frontend/backend behavior mismatches to monitor:
- Category create path mismatch in frontend vs backend endpoint.
- Voucher update/delete actions exist on UI but backend endpoints are not exposed for those actions.
- Staff has category UI while backend restricts category write operations to admin.
7. Validation/business errors are returned with structured error codes for UI handling.

---

## 6. Data Flow Summary by Major Process

### 6.1 Checkout and Order Creation
1. Input from UI:
- Receiver fields + optional note
- Optional `userVoucherId`
2. Backend transforms:
- Reads cart items
- Validates stock and voucher
- Computes totals and discount
- Persists order + lines + history
3. Output to UI:
- `orderId` and success/failure message

### 6.2 Delivery Processing
1. Input from UI:
- `orderId` + delivery action (`start`/`deliver`/`fail`)
2. Backend transforms:
- Validates transition + actor ownership
- Updates status and timestamps
- Inserts order history
3. Output to UI:
- Transition result message + refreshed order list

### 6.3 Voucher Assignment and Usage
1. Input from UI:
- Voucher template data
- Assignment target `userId`
- Checkout preview request
2. Backend transforms:
- Validates template/ownership/date/status/min-order
- Computes discount
- Marks usage on finalize
- Logs voucher usage actions
3. Output to UI:
- Voucher list, eligibility, discount amount, assignment/create result

---

## 7. Main API Groups (for traceability)
- Auth: `/api/auth/*`
- Product: `/api/products/*`
- Category: `/api/categories/*`
- Brand: `/api/brands/*`
- Cart: `/api/cart/*`
- Customer Orders: `/api/orders/*`
- Staff Orders: `/api/staff/orders/*`
- Admin Orders: `/api/admin/orders/*`
- Shipper Orders: `/api/shipper/orders/*`
- Reviews: `/api/reviews/*`
- Customer Vouchers: `/api/vouchers/*`
- Admin/Staff Vouchers: `/api/admin/vouchers/*`
- Admin Users: `/api/admin/users/*`
- Contact: `/api/contact`
- Upload: `/api/upload`

---

## 8. Primary Database Objects
- `users`, `roles`, `user_roles`
- `products`, `product_images`, `categories`, `brands`
- `carts`, `cart_items`
- `orders`, `order_items`, `order_history`
- `vouchers`, `user_vouchers`, `voucher_usage_history`
- `product_reviews`
- `contacts`

---

## 9. Documentation Notes
- This document focuses on business behavior and process flow extracted from implementation.
- It intentionally avoids source-code explanation and code snippets.
- Where implementation gaps/mismatches exist between frontend and backend, they are listed as operational edge cases.
