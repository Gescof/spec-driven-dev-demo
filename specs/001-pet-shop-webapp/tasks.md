# Tasks: Pet Shop Web App

**Input**: Design documents from `specs/001-pet-shop-webapp/`
**Prerequisites**: plan.md ✅, spec.md ✅, data-model.md ✅, contracts/openapi.yaml ✅, research.md ✅, quickstart.md ✅

**Tests**: Backend tests included per Constitution Principle III (Test-First is NON-NEGOTIABLE). Tests are written BEFORE implementation in each user story phase. Frontend uses browser-based manual testing only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths included in all descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Maven project initialization, Spring Boot bootstrap, PostgreSQL seed data, and frontend page shells.

- [ ] T001 Create backend Maven project in backend/pom.xml with Java 21, spring-boot-parent 3.x, starters: web, data-jpa, security, validation; runtime: postgresql; test: spring-boot-starter-test
- [ ] T002 Create Spring Boot entry point in backend/src/main/java/com/petshop/PetShopApplication.java with @SpringBootApplication annotation
- [ ] T003 Create backend/src/main/resources/application.properties (datasource url/username/password for petshop db, jpa.hibernate.ddl-auto=create-drop, server.port=8080, petshop.cors.allowed-origins=http://localhost:5500,http://127.0.0.1:5500) and application.properties.example as a committed template
- [ ] T004 Create backend/src/main/resources/db/seed.sql with INSERT statements for 4 categories (Dogs, Cats, Birds, Fish) and at least 8 products with name, description, price, category FK, imageUrl (external URLs), available=true
- [ ] T005 [P] Create all 8 frontend HTML page shells in frontend/ (index.html, catalog.html, product.html, login.html, register.html, account.html, cart.html, checkout.html) each with DOCTYPE html, shared nav bar containing links to catalog, cart, login/register, and a main content area with placeholder headings; reference api.js and auth.js in each page
- [ ] T006 [P] Create frontend/css/style.css with CSS custom properties (brand colors, spacing scale), mobile-first responsive base (flexbox/grid), shared nav bar styles, button component styles, form field styles, and WCAG 2.1 AA minimum 4.5:1 contrast ratios on all text
- [ ] T007 [P] Create frontend/js/api.js with BASE_URL = 'http://localhost:8080/api/v1' and async apiFetch(method, path, body) using fetch(BASE_URL + path) with credentials: 'include', Content-Type: application/json, and throwing a structured error on non-2xx responses
- [ ] T008 [P] Create frontend/js/auth.js with async checkSession() calling GET /auth/me and storing result in window.currentUser, requireLogin(returnUrl) that redirects to login.html if not authenticated, and updateNavForAuthState() toggling nav links between (Login, Register) and (Account, Logout)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Spring Security session-based authentication config, CORS, and error handling infrastructure. Must complete before any user story.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T009 Implement Spring Security configuration in backend/src/main/java/com/petshop/config/SecurityConfig.java: permit GET /api/v1/categories/**, /api/v1/products/**, /api/v1/auth/**; require authentication for /api/v1/cart/**, /api/v1/orders/**, /api/v1/users/**; configure sessionManagement ALWAYS; define @Bean BCryptPasswordEncoder; disable CSRF; configure 401 JSON response (not redirect) on unauthorized access
- [ ] T010 [P] Implement CORS configuration in backend/src/main/java/com/petshop/config/CorsConfig.java: read allowed origins from @Value("${petshop.cors.allowed-origins}"), allow all HTTP methods, allowCredentials=true, allowedHeaders=*, maxAge=3600
- [ ] T011 [P] Create error DTO classes: backend/src/main/java/com/petshop/dto/ErrorResponse.java (int status, String error, String message, LocalDateTime timestamp) and backend/src/main/java/com/petshop/dto/ValidationErrorResponse.java (same fields plus Map<String,String> fieldErrors)
- [ ] T012 [P] Create custom exception classes in backend/src/main/java/com/petshop/exception/: NotFoundException.java, DuplicateEmailException.java, and OutOfStockException.java — all extend RuntimeException with a message constructor
- [ ] T013 Implement @ControllerAdvice global exception handler in backend/src/main/java/com/petshop/GlobalExceptionHandler.java: handle MethodArgumentNotValidException → 400 ValidationErrorResponse (map field errors from binding result); NotFoundException → 404 ErrorResponse; DuplicateEmailException → 409 ErrorResponse; AccessDeniedException → 403 ErrorResponse; generic Exception → 500 ErrorResponse

**Checkpoint**: Spring Security, CORS, and error handling infrastructure ready — user story implementation can now begin

---

## Phase 3: User Story 1 - Browse Products (Priority: P1) 🎯 MVP

**Goal**: Visitors can browse the home page (featured products), filter the catalog by category, search by keyword, and view full product detail pages — without an account.

**Independent Test**: Start backend (`./mvnw spring-boot:run`), run seed.sql. Open frontend/index.html via Live Server at http://127.0.0.1:5500. Verify featured products display with name, image, and price. Navigate to catalog.html, click "Dogs" filter — verify only dog products shown. Type a keyword in search — verify matching products shown. Click any product — verify product.html loads with full description, price, availability, and "Add to Cart" button.

### Tests for User Story 1 (Constitution: Test-First, NON-NEGOTIABLE) ⚠️

> **NOTE: Write these tests FIRST, verify they FAIL before writing any implementation code**

- [ ] T014 [P] [US1] Write @WebMvcTest in backend/src/test/java/com/petshop/controller/CategoryControllerTest.java: test GET /api/v1/categories returns 200 with JSON array of categories; mock CategoryService.findAll()
- [ ] T015 [P] [US1] Write @WebMvcTest in backend/src/test/java/com/petshop/controller/ProductControllerTest.java: test GET /api/v1/products (no params) → 200 ProductPage; GET /api/v1/products?categoryId=1 → 200 filtered page; GET /api/v1/products?search=cat → 200 page; GET /api/v1/products/{id} → 200 ProductDetail; GET /api/v1/products/999 → 404; GET /api/v1/products/featured → 200 array; mock ProductService
- [ ] T016 [P] [US1] Write Mockito unit tests in backend/src/test/java/com/petshop/service/ProductServiceTest.java: test listProducts with no filter returns full page; with categoryId returns filtered page; with search returns filtered page; getById found returns ProductDetailResponse; getById not-found throws NotFoundException; getFeatured returns List of max 8 items

### Implementation for User Story 1

- [ ] T017 [P] [US1] Create Category JPA entity in backend/src/main/java/com/petshop/model/Category.java: @Entity, Long id @GeneratedValue, @NotBlank @Size(max=100) String name, String description; @Table(uniqueConstraints=@UniqueConstraint(columnNames="name"))
- [ ] T018 [P] [US1] Create Product JPA entity in backend/src/main/java/com/petshop/model/Product.java: @Entity, Long id @GeneratedValue, @NotBlank @Size(max=200) String name, @NotBlank String description, @Positive @Digits(integer=10,fraction=2) BigDecimal price, @ManyToOne(fetch=LAZY) @NotNull Category category, @Size(max=500) String imageUrl, boolean available=true, @CreationTimestamp LocalDateTime createdAt
- [ ] T019 [P] [US1] Create backend/src/main/java/com/petshop/repository/CategoryRepository.java extending JpaRepository<Category, Long> with no additional methods needed for v1
- [ ] T020 [P] [US1] Create backend/src/main/java/com/petshop/repository/ProductRepository.java extending JpaRepository<Product, Long>: add @Query for combined optional-filter search (categoryId, keyword on name/description, available flag) with Pageable support; add List<Product> findTop8ByAvailableTrueOrderByCreatedAtDesc()
- [ ] T021 [P] [US1] Create category/product DTOs in backend/src/main/java/com/petshop/dto/: CategoryResponse.java (Long id, String name, String description), ProductSummaryResponse.java (Long id, String name, BigDecimal price, String imageUrl, boolean available, CategoryResponse category), ProductDetailResponse.java (extends ProductSummaryResponse adding String description), ProductPageResponse.java (List<ProductSummaryResponse> content, long totalElements, int totalPages, int page, int size)
- [ ] T022 [US1] Implement backend/src/main/java/com/petshop/service/CategoryService.java: @Service with CategoryRepository; findAll() returns List<CategoryResponse> via manual mapping from CategoryRepository.findAll()
- [ ] T023 [US1] Implement backend/src/main/java/com/petshop/service/ProductService.java: @Service with ProductRepository; listProducts(Long categoryId, String search, Boolean available, Pageable) builds ProductPageResponse; getById(Long id) throws NotFoundException if absent; getFeatured() returns List<ProductSummaryResponse> (top 8 available, newest first); all methods use manual DTO mapping
- [ ] T024 [US1] Implement backend/src/main/java/com/petshop/controller/CategoryController.java: @RestController @RequestMapping("/api/v1/categories"); @GetMapping returns ResponseEntity<List<CategoryResponse>> from CategoryService.findAll()
- [ ] T025 [US1] Implement backend/src/main/java/com/petshop/controller/ProductController.java: @RestController @RequestMapping("/api/v1/products"); GET "/" with optional @RequestParam categoryId/search/available and Pageable params; GET "/featured"; GET "/{id}"; all delegate to ProductService
- [ ] T026 [US1] Create frontend/js/index.js: on DOMContentLoaded call apiFetch('GET', '/products/featured'), render each product as a card in #featured-products (img with imageUrl or placeholder, name, price formatted as USD, anchor to product.html?id=X); call checkSession() and updateNavForAuthState()
- [ ] T027 [P] [US1] Create frontend/js/catalog.js: on load fetch GET /categories to build category filter buttons in #category-filters; fetch GET /products to render product cards in #product-grid; on category button click add active style and re-fetch with categoryId param; on #search-input change re-fetch with search param after 300ms debounce; show #no-results message when content array is empty
- [ ] T028 [P] [US1] Create frontend/js/product.js: read id from new URLSearchParams(location.search).get('id'); fetch GET /products/{id}; populate page with name, full description, price, image, availability badge (In Stock / Out of Stock); "Add to Cart" button stub — if not logged in redirect to login.html?returnUrl=product.html?id={id}; full add-to-cart wiring is in T064
- [ ] T029 [US1] Add US1 CSS components to frontend/css/style.css: product card (image, name, price, hover shadow), responsive product grid (1 col mobile / 2 col tablet / 4 col desktop using CSS grid auto-fill), hero/banner section for index.html, category filter button strip with active state, catalog search input, product detail two-column layout (image left + info right on desktop; stacked on mobile), in-stock/out-of-stock availability badge

**Checkpoint**: User Story 1 fully functional — visitors can browse and discover products without an account

---

## Phase 4: User Story 2 - Register & Manage Account (Priority: P2)

**Goal**: Visitors can register with email/password, log in, view their account page (name, email, order history — empty initially), update their profile, and log out.

**Independent Test**: Navigate to register.html, fill valid email/name/password (≥8 chars), submit. Verify auto-login and redirect to account.html showing profile details and "No orders yet". Click Logout — redirected to home as visitor. Log in at login.html, enter credentials — redirected to account.html. Update name via profile form — verify change persists on reload. Attempt registration with same email — verify "Email already taken" error displayed.

### Tests for User Story 2 (Constitution: Test-First, NON-NEGOTIABLE) ⚠️

> **NOTE: Write these tests FIRST, verify they FAIL before writing any implementation code**

- [ ] T030 [P] [US2] Write @WebMvcTest in backend/src/test/java/com/petshop/controller/AuthControllerTest.java: POST /auth/register 201 success returns UserProfile; POST /auth/register duplicate email → 409; POST /auth/register invalid email format → 400 ValidationErrorResponse with fieldErrors; POST /auth/login valid credentials → 200 UserProfile; POST /auth/login wrong password → 401; POST /auth/logout → 204; GET /auth/me authenticated → 200 UserProfile; GET /auth/me unauthenticated → 401
- [ ] T031 [P] [US2] Write @WebMvcTest in backend/src/test/java/com/petshop/controller/UserControllerTest.java: GET /users/me with mock authenticated principal → 200 UserProfile; GET /users/me unauthenticated → 401; PUT /users/me with valid name → 200 updated UserProfile; PUT /users/me with blank name → 400 ValidationErrorResponse
- [ ] T032 [P] [US2] Write Mockito unit tests in backend/src/test/java/com/petshop/service/AuthServiceTest.java: register with new email creates User, hashes password, returns UserProfileResponse; register with duplicate email throws DuplicateEmailException; getCurrentUser extracts and returns UserProfileResponse from Authentication principal

### Implementation for User Story 2

- [ ] T033 [P] [US2] Create User JPA entity in backend/src/main/java/com/petshop/model/User.java: @Entity, Long id @GeneratedValue, @Email @NotBlank @Size(max=255) String email (UNIQUE), @NotBlank @Size(max=200) String name, @NotBlank String passwordHash, @CreationTimestamp LocalDateTime registeredAt; @Table(uniqueConstraints=@UniqueConstraint(columnNames="email"))
- [ ] T034 [P] [US2] Create backend/src/main/java/com/petshop/repository/UserRepository.java extending JpaRepository<User, Long>: Optional<User> findByEmail(String email); boolean existsByEmail(String email)
- [ ] T035 [P] [US2] Create auth/user DTOs in backend/src/main/java/com/petshop/dto/: RegisterRequest.java (@NotBlank @Email String email, @NotBlank @Size(max=200) String name, @NotBlank @Size(min=8) String password), LoginRequest.java (String email, String password), UserProfileResponse.java (Long id, String email, String name, LocalDateTime registeredAt — no passwordHash field), UpdateProfileRequest.java (nullable String name, nullable String currentPassword, nullable @Size(min=8) String newPassword)
- [ ] T036 [US2] Implement backend/src/main/java/com/petshop/service/PetShopUserDetailsService.java implementing Spring Security UserDetailsService: loadUserByUsername(email) calls UserRepository.findByEmail(), throws UsernameNotFoundException if absent, returns User wrapped as org.springframework.security.core.userdetails.User with passwordHash as password and authority ROLE_USER
- [ ] T037 [US2] Implement backend/src/main/java/com/petshop/service/AuthService.java: @Service with UserRepository, BCryptPasswordEncoder; register(@Valid RegisterRequest) throws DuplicateEmailException if existsByEmail; encodes password; saves new User; returns UserProfileResponse; getCurrentUser(Authentication) extracts email from principal and returns UserProfileResponse via UserRepository.findByEmail()
- [ ] T038 [US2] Implement backend/src/main/java/com/petshop/service/UserService.java: @Service; getProfile(Long userId) returns UserProfileResponse; updateProfile(Long userId, UpdateProfileRequest) updates name if provided; if newPassword provided, verifies currentPassword matches stored BCrypt hash then re-encodes and saves new password; returns updated UserProfileResponse
- [ ] T039 [US2] Implement backend/src/main/java/com/petshop/controller/AuthController.java: @RestController @RequestMapping("/api/v1/auth"); POST /register validates body, calls AuthService.register(), programmatically authenticates via AuthenticationManager and populates SecurityContextHolder, returns 201 UserProfileResponse; POST /login calls AuthenticationManager.authenticate() with UsernamePasswordAuthenticationToken from LoginRequest JSON body, returns 200 UserProfileResponse; POST /logout invalidates HttpSession, returns 204; GET /me returns getCurrentUser from authenticated principal
- [ ] T040 [US2] Implement backend/src/main/java/com/petshop/controller/UserController.java: @RestController @RequestMapping("/api/v1/users"); GET /me returns UserService.getProfile(userId); PUT /me with @Valid @RequestBody UpdateProfileRequest returns UserService.updateProfile(userId, request); extract userId from Authentication principal
- [ ] T041 [US2] Update backend/src/main/java/com/petshop/config/SecurityConfig.java to wire PetShopUserDetailsService: inject PetShopUserDetailsService; configure DaoAuthenticationProvider (setUserDetailsService, setPasswordEncoder); expose AuthenticationManager @Bean for injection into AuthController
- [ ] T042 [US2] Create frontend/js/register.js: on #register-form submit call apiFetch('POST', '/auth/register', {email, name, password}); on 400 display field-level errors below each input using fieldErrors map from ValidationErrorResponse; on 409 display "Email already taken" message; on 201 success redirect to account.html
- [ ] T043 [P] [US2] Create frontend/js/login.js: on page load call checkSession() and redirect already-authenticated users to account.html; on #login-form submit call apiFetch('POST', '/auth/login', {email, password}); on 401 display "Invalid email or password" message; on 200 success redirect to URLSearchParams returnUrl or account.html
- [ ] T044 [P] [US2] Create frontend/js/account.js: call requireLogin() at top; fetch GET /users/me and display name, email, registeredAt in #profile-section; render #profile-update-form pre-filled with current name; on update form submit call apiFetch('PUT', '/users/me', request body) and update displayed values; fetch GET /orders and render list in #order-history (orderNumber, formatted date, totalAmount as USD, itemCount); show "No orders yet — start shopping!" with catalog link when empty; Logout button calls apiFetch('POST', '/auth/logout') then redirects to index.html
- [ ] T045 [US2] Add US2 CSS components to frontend/css/style.css: auth form card (centered, max-width 400px, card shadow), inline field error message (red, 0.85rem, below input), success/error notification banner, account page two-column layout (profile left, orders right on desktop; stacked on mobile), order history list item styles, empty state style for "No orders yet"

**Checkpoint**: User Stories 1 and 2 independently functional — visitors can browse AND register/manage accounts

---

## Phase 5: User Story 3 - Shop & Place an Order (Priority: P3)

**Goal**: Logged-in users can add products to a persistent cart, adjust quantities, remove items, place an order with a simulated payment form, receive a confirmation with a unique order number, and see the order in account history.

**Independent Test**: Log in. From product.html for two different products, click "Add to Cart" on each. Navigate to cart.html — verify both items appear with quantity inputs, unit prices, line totals, and grand total. Change one quantity to 3 — verify line and grand totals update. Remove the other item — verify cart recalculates. Click "Proceed to Checkout" — verify order summary shown on checkout.html. Fill simulated payment form, click "Confirm Order" — verify order confirmation section shows ORD-{uuid} order number. Navigate to account.html — verify the new order appears in history with correct items and total.

### Tests for User Story 3 (Constitution: Test-First, NON-NEGOTIABLE) ⚠️

> **NOTE: Write these tests FIRST, verify they FAIL before writing any implementation code**

- [ ] T046 [P] [US3] Write @WebMvcTest in backend/src/test/java/com/petshop/controller/CartControllerTest.java: GET /cart authenticated → 200 CartResponse; GET /cart unauthenticated → 401; POST /cart/items with valid productId → 200 CartResponse; POST /cart/items product unavailable → 400; POST /cart/items unknown product → 404; PUT /cart/items/{productId} → 200; DELETE /cart/items/{productId} → 200; all authenticated endpoints return 401 when no session
- [ ] T047 [P] [US3] Write @WebMvcTest in backend/src/test/java/com/petshop/controller/OrderControllerTest.java: GET /orders authenticated → 200 List<OrderSummary>; GET /orders unauthenticated → 401; POST /orders with items in cart → 201 OrderDetail with non-null orderNumber matching ORD-; POST /orders empty cart → 400; POST /orders out-of-stock item → 400; GET /orders/{id} owned → 200 OrderDetail; GET /orders/{id} not owner → 403; GET /orders/{id} not found → 404
- [ ] T048 [P] [US3] Write Mockito unit tests in backend/src/test/java/com/petshop/service/CartServiceTest.java and backend/src/test/java/com/petshop/service/OrderServiceTest.java: addToCart creates new CartItem when product not in cart; addToCart increments quantity when product already in cart; updateQuantity changes CartItem quantity; removeItem deletes CartItem; placeOrder snapshots productName and unitPrice from Product entity, generates orderNumber starting with "ORD-", clears cart items, throws exception when cart empty, throws OutOfStockException when any item is unavailable

### Implementation for User Story 3

- [ ] T049 [P] [US3] Create Cart JPA entity in backend/src/main/java/com/petshop/model/Cart.java: @Entity, Long id @GeneratedValue, @OneToOne(fetch=LAZY) @JoinColumn(unique=true) User user, @CreationTimestamp LocalDateTime createdAt, @UpdateTimestamp LocalDateTime updatedAt, @OneToMany(mappedBy="cart", cascade=ALL, orphanRemoval=true) List<CartItem> items
- [ ] T050 [P] [US3] Create CartItem JPA entity in backend/src/main/java/com/petshop/model/CartItem.java: @Entity, Long id @GeneratedValue, @ManyToOne(fetch=LAZY) Cart cart, @ManyToOne(fetch=LAZY) Product product, @Min(1) int quantity; @Table(uniqueConstraints=@UniqueConstraint(columnNames={"cart_id","product_id"}))
- [ ] T051 [P] [US3] Create Order JPA entity in backend/src/main/java/com/petshop/model/Order.java: @Entity @Table(name="orders"), Long id @GeneratedValue, @Column(unique=true) String orderNumber, @ManyToOne(fetch=LAZY) User user, @Enumerated(EnumType.STRING) OrderStatus status default CONFIRMED, @CreationTimestamp LocalDateTime placedAt, @Digits(integer=10,fraction=2) BigDecimal totalAmount, @OneToMany(mappedBy="order", cascade=ALL, orphanRemoval=true) List<OrderItem> items
- [ ] T052 [P] [US3] Create OrderItem JPA entity in backend/src/main/java/com/petshop/model/OrderItem.java: @Entity, Long id @GeneratedValue, @ManyToOne(fetch=LAZY) Order order, @ManyToOne(fetch=LAZY) Product product, @NotBlank String productName (price-snapshot), @Positive @Digits(integer=10,fraction=2) BigDecimal unitPrice (price-snapshot), @Min(1) int quantity
- [ ] T053 [P] [US3] Create backend/src/main/java/com/petshop/model/OrderStatus.java: public enum OrderStatus { CONFIRMED }
- [ ] T054 [P] [US3] Create backend/src/main/java/com/petshop/repository/CartRepository.java (Optional<Cart> findByUserId(Long userId)) and backend/src/main/java/com/petshop/repository/CartItemRepository.java (Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId))
- [ ] T055 [P] [US3] Create backend/src/main/java/com/petshop/repository/OrderRepository.java: List<Order> findByUserIdOrderByPlacedAtDesc(Long userId); Optional<Order> findByIdAndUserId(Long id, Long userId)
- [ ] T056 [P] [US3] Create cart/order DTOs in backend/src/main/java/com/petshop/dto/: CartItemResponse.java (Long productId, String productName, String productImageUrl, BigDecimal unitPrice, int quantity, BigDecimal lineTotal, boolean available), CartResponse.java (List<CartItemResponse> items, BigDecimal grandTotal, int itemCount), AddToCartRequest.java (@Positive Long productId, @Min(1) int quantity), UpdateCartItemRequest.java (@Min(1) int quantity), OrderSummaryResponse.java (Long id, String orderNumber, String status, LocalDateTime placedAt, BigDecimal totalAmount, int itemCount), OrderLineItemResponse.java (Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal), OrderDetailResponse.java (all OrderSummaryResponse fields plus List<OrderLineItemResponse> items), PlaceOrderRequest.java (String cardholderName, @Pattern(regexp="\\d{4}") String cardNumberLast4)
- [ ] T057 [US3] Implement backend/src/main/java/com/petshop/service/CartService.java: @Service with CartRepository, CartItemRepository, ProductRepository; getOrCreateCart(Long userId) finds or creates Cart; getCart(Long userId) returns CartResponse with computed lineTotal (unitPrice × quantity per item) and grandTotal (sum of all lineTotals) and itemCount; addToCart(Long userId, AddToCartRequest) validates product exists and available=true, finds existing CartItem and increments or creates new one, saves, returns updated CartResponse; updateQuantity(Long userId, Long productId, int qty) finds CartItem and updates quantity; removeItem(Long userId, Long productId) deletes CartItem from cart
- [ ] T058 [US3] Implement backend/src/main/java/com/petshop/service/OrderService.java: @Service with CartService, OrderRepository, ProductRepository; placeOrder(Long userId, PlaceOrderRequest) gets cart, throws 400 error if empty, throws OutOfStockException listing unavailable items, creates Order with orderNumber="ORD-"+UUID.randomUUID().toString(), creates OrderItem for each CartItem snapshotting product.getName() and product.getPrice(), computes totalAmount, saves Order, clears CartItems, returns OrderDetailResponse; getUserOrders(Long userId) returns List<OrderSummaryResponse> newest first; getOrderDetail(Long userId, Long orderId) throws 403 if order belongs to different user, throws NotFoundException if absent, returns OrderDetailResponse
- [ ] T059 [US3] Implement backend/src/main/java/com/petshop/controller/CartController.java: @RestController @RequestMapping("/api/v1/cart"); GET "/" returns CartService.getCart(userId); POST "/items" with @Valid @RequestBody AddToCartRequest returns 200 CartResponse; PUT "/items/{productId}" with @Valid @RequestBody UpdateCartItemRequest returns 200; DELETE "/items/{productId}" returns 200; extract userId from Authentication principal in all methods
- [ ] T060 [US3] Implement backend/src/main/java/com/petshop/controller/OrderController.java: @RestController @RequestMapping("/api/v1/orders"); GET "/" returns 200 List<OrderSummaryResponse>; POST "/" with @RequestBody PlaceOrderRequest returns 201 OrderDetailResponse; GET "/{id}" returns 200 OrderDetailResponse; extract userId from Authentication principal in all methods
- [ ] T061 [US3] Create frontend/js/cart.js: call requireLogin(); fetch GET /cart on page load and render rows in #cart-table (60px product image thumbnail, name, unit price, quantity <input type=number min=1>, line total, Remove button); wire quantity input change event to PUT /cart/items/{productId} with {quantity}, re-render full cart on response; wire Remove button to DELETE /cart/items/{productId}, re-render full cart on response; show #cart-empty with catalog link when items array is empty; display grand total in #grand-total; add "Out of Stock" warning badge on rows where available=false; "Proceed to Checkout" link to checkout.html; disable checkout link when any item is out of stock
- [ ] T062 [US3] Create frontend/js/checkout.js: call requireLogin(); fetch GET /cart and render order summary in #order-summary (item list with name, qty, line total, grand total); if cart empty redirect to cart.html; render simulated payment form (#checkout-form) with cardholderName text input, fake card number input (client-side max 16 digits, last 4 digits extracted), expiry (MM/YY), CVV — client validation only, no real processing; on form submit extract cardholderName and last 4 digits, call apiFetch('POST', '/orders', {cardholderName, cardNumberLast4}); on 201 hide form, show #order-confirmation with orderNumber and "View Order History" link to account.html; on 400 show error message with link back to cart.html
- [ ] T063 [US3] Update frontend/js/account.js to render order history: for each order from GET /orders render a row in #order-history showing orderNumber, placedAt (formatted as locale date string), totalAmount (USD format), and itemCount; each order row is expandable or links to order detail (fetch GET /orders/{id} on click to show line items inline); show "No orders yet — start shopping!" with catalog.html link when response array is empty
- [ ] T064 [P] [US3] Update frontend/js/product.js "Add to Cart" button: call apiFetch('POST', '/cart/items', {productId: id, quantity: 1}) on button click; on success show a brief "Added to cart!" toast notification and fetch GET /cart to update #nav-cart-count badge in nav; on 401 redirect to login.html?returnUrl=product.html?id={id}; on 400 (product unavailable) display "This product is currently out of stock" error message
- [ ] T065 [US3] Add US3 CSS components to frontend/css/style.css: cart table layout (thumbnail column, name column, price column, quantity input column, line-total column, remove button), cart empty state, out-of-stock row highlight and badge, checkout form card with payment field groups and simulated-payment disclaimer text, order confirmation section (order number large font, success color background), checkout order summary sidebar

**Checkpoint**: All three user stories independently functional — complete e-commerce loop implemented

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Responsive design completion, edge cases, security hardening, and quickstart validation.

- [ ] T066 [P] Complete responsive CSS for all 8 pages in frontend/css/style.css: audit at ≤480px mobile, 768px tablet, and 1280px desktop breakpoints; ensure no horizontal scroll, body font ≥16px, tap targets ≥44px; verify WCAG 2.1 AA contrast ratios (≥4.5:1 normal text, ≥3:1 large text) for all text/background combinations
- [ ] T067 [P] Add out-of-stock edge case handling: confirm backend/src/main/java/com/petshop/service/OrderService.java returns 400 with a message listing which product names are out of stock; confirm frontend/js/cart.js disables "Proceed to Checkout" button and shows per-item out-of-stock badge when any cart item has available=false
- [ ] T068 [P] Add empty state messaging across frontend: frontend/js/catalog.js shows "No products found. Browse all categories." with clear-filters link when product grid is empty; frontend/js/account.js shows "No orders yet — start shopping!" with catalog link; frontend/js/cart.js shows "Your cart is empty." with catalog link
- [ ] T069 [P] Validate quickstart.md steps end-to-end: confirm DB setup SQL in quickstart.md creates petshop database and user matching application.properties credentials; confirm `./mvnw test` in backend/ runs all tests green with no failures; confirm seed.sql inserts without constraint errors; confirm frontend/index.html loads featured products at http://127.0.0.1:5500 against backend on port 8080
- [ ] T070 Run security hardening review across backend/: confirm UserProfileResponse never includes passwordHash; confirm JSESSIONID cookie is HttpOnly (Spring Security default session); confirm all @WebMvcTest tests verify 401 on unauthenticated access to protected endpoints; confirm all JPA @Query methods use named parameters (no string concatenation); confirm OutOfStockException returns 400 and ForbiddenException returns 403
- [ ] T071 [P] Add cart item count badge to nav bar in frontend/js/auth.js: after checkSession() succeeds fetch GET /cart and set #nav-cart-count text to cart.itemCount (hidden with display:none when count is 0 or user not logged in); export an updateCartBadge() helper and call it from frontend/js/cart.js and frontend/js/product.js after every successful cart mutation
- [ ] T072 [P] Performance spot-check: manually verify GET /api/v1/products?search= responds in under 2 seconds with seed data loaded (SC-004); verify all 8 frontend HTML pages load and become interactive in under 3 seconds over Live Server (SC-007); document any bottlenecks found in a "Performance Notes" section appended to specs/001-pet-shop-webapp/plan.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately; T005, T006, T007, T008 run in parallel with T001-T004
- **Foundational (Phase 2)**: Depends on Phase 1 completion — **BLOCKS all user stories**
- **User Story 1 (Phase 3)**: Depends on Phase 2; tests (T014-T016) and entity/DTO creation (T017-T021) run in parallel before services and controllers
- **User Story 2 (Phase 4)**: Depends on Phase 2; can start in parallel with US1 if staffed
- **User Story 3 (Phase 5)**: Depends on Phase 2; requires US1 (Product/Category entities and API) and US2 (User entity and session auth) for full integration
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **US1 (P1)**: Depends only on Foundational — no dependency on US2 or US3
- **US2 (P2)**: Depends only on Foundational — authentication can be tested without product browsing
- **US3 (P3)**: Depends on both US1 (Product entity, ProductRepository for CartItem) and US2 (User entity, UserRepository, session auth for cart/order ownership)

### Within Each User Story

1. Tests MUST be written and confirmed **failing** before any implementation code is written
2. Entities and repositories in parallel (different files, no conflicts)
3. DTOs in parallel with entities
4. Services after entities + repositories are present
5. Controllers after services
6. Frontend scripts after backend controllers are running
7. CSS additions after frontend scripts within each phase

### Parallel Opportunities

- **Phase 1**: T005, T006, T007, T008 in parallel (all different frontend setup files)
- **Phase 2**: T010, T011, T012 in parallel after T009 (different config/dto/exception files)
- **Phase 3**: T014, T015, T016 in parallel; then T017, T018, T019, T020, T021 in parallel; then T027, T028 in parallel after T026
- **Phase 4**: T030, T031, T032, T033, T034, T035 in parallel; then T043, T044 in parallel
- **Phase 5**: T046, T047, T048, T049, T050, T051, T052, T053, T054, T055, T056 in parallel; T064 in parallel with T061, T062, T063

---

## Parallel Example: User Story 1

```bash
# Step 1 — Write tests in parallel (all different files):
Task T014: backend/src/test/java/com/petshop/controller/CategoryControllerTest.java
Task T015: backend/src/test/java/com/petshop/controller/ProductControllerTest.java
Task T016: backend/src/test/java/com/petshop/service/ProductServiceTest.java

# Step 2 — Create entities/repositories/DTOs in parallel:
Task T017: backend/src/main/java/com/petshop/model/Category.java
Task T018: backend/src/main/java/com/petshop/model/Product.java
Task T019: backend/src/main/java/com/petshop/repository/CategoryRepository.java
Task T020: backend/src/main/java/com/petshop/repository/ProductRepository.java
Task T021: backend/src/main/java/com/petshop/dto/ (CategoryResponse, ProductSummaryResponse, ProductDetailResponse, ProductPageResponse)

# Step 3 — Services sequentially after entities:
Task T022: CategoryService.java
Task T023: ProductService.java

# Step 4 — Controllers after services:
Task T024: CategoryController.java
Task T025: ProductController.java

# Step 5 — Frontend JS in parallel:
Task T026: frontend/js/index.js
Task T027: frontend/js/catalog.js
Task T028: frontend/js/product.js

# Step 6 — CSS additions last:
Task T029: frontend/css/style.css (US1 product card, grid, hero, filter, detail layout)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T008)
2. Complete Phase 2: Foundational (T009-T013) — **CRITICAL: blocks all stories**
3. Complete Phase 3: User Story 1 (T014-T029)
4. **STOP and VALIDATE**: Visit site via Live Server, browse catalog, filter by category, search, view product detail page
5. Demo or share — this is a working read-only product showcase

### Incremental Delivery

1. Setup (Phase 1) + Foundational (Phase 2) → Foundation ready
2. User Story 1 (Phase 3) → **MVP: Read-only product catalog** ✅ Demo
3. User Story 2 (Phase 4) → **+Auth: Registration and account management** ✅ Demo
4. User Story 3 (Phase 5) → **+Shopping: Full cart and order placement** ✅ Full e-commerce loop
5. Polish (Phase 6) → Production-ready quality

### Parallel Team Strategy

With 2+ developers after Foundational phase (Phase 2) is complete:
- **Developer A**: User Story 1 (Browse Products — backend + frontend, T014-T029)
- **Developer B**: User Story 2 (Auth & Accounts — backend + frontend, T030-T045)
- **Both developers** converge on User Story 3 after US1 and US2 are complete (T046-T065)

---

## Notes

- **[P]** tasks can run concurrently — they touch different files with no shared state
- **[Story]** label maps each task to its user story for traceability and independent delivery
- Each user story phase produces an independently testable increment; validate the checkpoint before moving on
- Constitution Principle III mandates Test-First: all backend tests MUST be written and confirmed **failing** before any implementation code is written (Red-Green-Refactor)
- Commit after each task or logical group using `/speckit-git-commit`
- The seed.sql in T004 is required for US1 manual testing; re-run it after each backend restart because ddl-auto=create-drop drops the schema on shutdown
- Cart and Order entities (T049-T055) are US3 only — do not create them during US1 or US2 phases
- UserProfileResponse must never include the passwordHash field — this is enforced by the DTO design in T035, not by runtime filtering
