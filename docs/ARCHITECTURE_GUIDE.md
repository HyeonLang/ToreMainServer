# 🏗️ 아키텍처 개선 가이드

> 현업에서 사용하는 실전 아키텍처 패턴

---

## 📋 목차

1. [도메인 주도 설계 (DDD)](#1-도메인-주도-설계-ddd)
2. [이벤트 기반 아키텍처](#2-이벤트-기반-아키텍처)
3. [API 설계 Best Practices](#3-api-설계-best-practices)
4. [마이크로서비스 전환 가이드](#4-마이크로서비스-전환-가이드)
5. [메시지 큐 도입](#5-메시지-큐-도입)

---

## 1. 도메인 주도 설계 (DDD)

### 1.1 현재 구조의 문제점

```
com.example.toremainserver
├── controller/      # 모든 컨트롤러
├── service/         # 모든 서비스
├── repository/      # 모든 리포지토리
├── entity/          # 모든 엔티티
└── dto/             # 모든 DTO
```

**문제점**:
- 도메인 경계가 불분명
- 비즈니스 로직이 Service에 집중
- 도메인 모델이 빈약함 (Anemic Domain Model)
- 관심사 분리 부족

### 1.2 개선된 구조 (DDD 적용)

```
com.example.toremainserver
├── domain/
│   ├── user/
│   │   ├── domain/
│   │   │   ├── User.java              (도메인 엔티티)
│   │   │   ├── UserService.java       (도메인 서비스)
│   │   │   └── UserRole.java          (값 객체)
│   │   ├── application/
│   │   │   ├── UserApplicationService.java
│   │   │   ├── dto/
│   │   │   │   ├── RegisterUserCommand.java
│   │   │   │   └── UserDto.java
│   │   │   └── port/
│   │   │       └── UserRepository.java (인터페이스)
│   │   ├── infrastructure/
│   │   │   ├── persistence/
│   │   │   │   └── UserJpaRepository.java
│   │   │   └── messaging/
│   │   └── presentation/
│   │       └── UserController.java
│   │
│   ├── nft/
│   │   ├── domain/
│   │   │   ├── NFT.java
│   │   │   ├── NFTMetadata.java
│   │   │   ├── MintingService.java
│   │   │   └── events/
│   │   │       └── NFTMintedEvent.java
│   │   ├── application/
│   │   │   ├── MintNftUseCase.java
│   │   │   ├── BurnNftUseCase.java
│   │   │   └── dto/
│   │   ├── infrastructure/
│   │   │   ├── blockchain/
│   │   │   │   └── BlockchainClient.java
│   │   │   └── persistence/
│   │   └── presentation/
│   │       └── NftController.java
│   │
│   ├── market/
│   │   ├── domain/
│   │   │   ├── SellOrder.java
│   │   │   ├── OrderStatus.java
│   │   │   ├── Price.java (값 객체)
│   │   │   └── MarketService.java
│   │   ├── application/
│   │   ├── infrastructure/
│   │   └── presentation/
│   │
│   └── game/
│       ├── domain/
│       │   ├── npc/
│       │   │   ├── NPC.java
│       │   │   ├── Conversation.java
│       │   │   └── DialogueService.java
│       │   └── item/
│       │       ├── Item.java
│       │       ├── Equipment.java
│       │       └── Consumable.java
│       ├── application/
│       ├── infrastructure/
│       └── presentation/
│
├── common/
│   ├── domain/
│   │   ├── BaseEntity.java
│   │   └── DomainEvent.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   └── infrastructure/
│       ├── config/
│       └── security/
│
└── ToreMainServerApplication.java
```

### 1.3 도메인 엔티티 리팩토링

#### Before (빈약한 도메인 모델)
```java
// ❌ Anemic Domain Model
@Entity
public class NFTSellOrder {
    private Long id;
    private String seller;
    private String price;
    private OrderStatus status;
    
    // Getter/Setter만 있음
}

// 비즈니스 로직이 Service에 집중
@Service
public class MarketService {
    public void cancelOrder(String orderId, String userAddress) {
        NFTSellOrder order = repository.findByOrderId(orderId)
                .orElseThrow(...);
        
        if (!order.getSeller().equals(userAddress)) {
            throw new RuntimeException("권한 없음");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        repository.save(order);
    }
}
```

#### After (풍부한 도메인 모델)
```java
// ✅ Rich Domain Model
@Entity
public class NFTSellOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    private Seller seller;
    
    @Embedded
    private Price price;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Embedded
    private OrderTimestamps timestamps;
    
    protected NFTSellOrder() {} // JPA용
    
    /**
     * 팩토리 메서드 - 도메인 규칙을 강제
     */
    public static NFTSellOrder create(Seller seller, 
                                      NFTContract nftContract,
                                      TokenId tokenId,
                                      Price price,
                                      Signature signature) {
        validateCreation(seller, price, signature);
        
        NFTSellOrder order = new NFTSellOrder();
        order.seller = seller;
        order.price = price;
        order.status = OrderStatus.ACTIVE;
        order.timestamps = OrderTimestamps.now();
        
        // 도메인 이벤트 발행
        order.registerEvent(new OrderCreatedEvent(order));
        
        return order;
    }
    
    /**
     * 도메인 로직 - 주문 취소
     */
    public void cancel(WalletAddress cancelRequester) {
        // 비즈니스 규칙 검증
        if (!this.seller.hasSameAddress(cancelRequester)) {
            throw new InsufficientPermissionException("주문을 취소할 권한이 없습니다");
        }
        
        if (!this.status.isCancellable()) {
            throw new InvalidOrderStateException("취소할 수 없는 상태입니다");
        }
        
        this.status = OrderStatus.CANCELLED;
        this.timestamps.updateModifiedAt();
        
        // 도메인 이벤트 발행
        this.registerEvent(new OrderCancelledEvent(this));
    }
    
    /**
     * 도메인 로직 - 주문 락
     */
    public void lock(WalletAddress buyer) {
        if (!this.status.isLockable()) {
            throw new InvalidOrderStateException("락할 수 없는 상태입니다");
        }
        
        this.status = OrderStatus.LOCKED;
        this.lockedBy = buyer;
        this.timestamps.lockAt();
        
        this.registerEvent(new OrderLockedEvent(this, buyer));
    }
    
    private static void validateCreation(Seller seller, Price price, Signature signature) {
        if (seller == null || !seller.isValid()) {
            throw new InvalidSellerException();
        }
        if (price == null || !price.isPositive()) {
            throw new InvalidPriceException();
        }
        if (!signature.verify(seller.getAddress())) {
            throw new InvalidSignatureException();
        }
    }
}

/**
 * 값 객체 (Value Object) - 불변성 보장
 */
@Embeddable
public class Price {
    private String amount;
    private String currency;
    
    protected Price() {} // JPA용
    
    public Price(String amount, String currency) {
        if (amount == null || new BigInteger(amount).compareTo(BigInteger.ZERO) <= 0) {
            throw new InvalidPriceException("가격은 0보다 커야 합니다");
        }
        if (currency == null || currency.isEmpty()) {
            throw new InvalidCurrencyException();
        }
        
        this.amount = amount;
        this.currency = currency;
    }
    
    public boolean isPositive() {
        return new BigInteger(amount).compareTo(BigInteger.ZERO) > 0;
    }
    
    public boolean isGreaterThan(Price other) {
        return new BigInteger(this.amount).compareTo(new BigInteger(other.amount)) > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Price)) return false;
        Price price = (Price) o;
        return Objects.equals(amount, price.amount) &&
               Objects.equals(currency, price.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}

/**
 * 도메인 서비스 - 여러 엔티티가 관여하는 로직
 */
@Service
public class MarketDomainService {
    
    /**
     * 주문 매칭 로직
     */
    public void matchOrder(NFTSellOrder sellOrder, BuyOrder buyOrder) {
        // 복잡한 매칭 로직
        if (!sellOrder.getPrice().equals(buyOrder.getPrice())) {
            throw new PriceMismatchException();
        }
        
        if (!sellOrder.isMatchable()) {
            throw new InvalidOrderStateException();
        }
        
        sellOrder.lock(buyOrder.getBuyer());
        buyOrder.lock();
    }
}

/**
 * Application Service - Use Case 구현
 */
@Service
@Transactional
public class CancelOrderUseCase {
    
    private final NFTSellOrderRepository orderRepository;
    
    public void execute(CancelOrderCommand command) {
        NFTSellOrder order = orderRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", command.getOrderId()));
        
        // ✅ 도메인 로직 호출
        order.cancel(new WalletAddress(command.getUserAddress()));
        
        orderRepository.save(order);
    }
}
```

### 1.4 Aggregate 패턴

```java
/**
 * Aggregate Root - 일관성 경계
 */
@Entity
public class UserInventory {
    
    @Id
    private Long userId;
    
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryItem> items = new ArrayList<>();
    
    /**
     * Aggregate 내부의 일관성을 보장
     */
    public void addItem(ItemDefinition itemDefinition, int quantity) {
        Optional<InventoryItem> existingItem = findItemByDefinition(itemDefinition.getId());
        
        if (existingItem.isPresent() && itemDefinition.isStackable()) {
            // 스택 가능한 아이템은 수량 증가
            existingItem.get().increaseQuantity(quantity);
        } else {
            // 새로운 아이템 추가
            InventoryItem newItem = InventoryItem.create(this, itemDefinition, quantity);
            this.items.add(newItem);
        }
        
        this.registerEvent(new ItemAddedToInventoryEvent(this.userId, itemDefinition.getId(), quantity));
    }
    
    /**
     * Aggregate 내에서만 접근 가능
     */
    private Optional<InventoryItem> findItemByDefinition(Integer itemDefinitionId) {
        return items.stream()
                .filter(item -> item.hasItemDefinition(itemDefinitionId))
                .findFirst();
    }
}

/**
 * Repository는 Aggregate Root만 조회
 */
public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {
    Optional<UserInventory> findByUserId(Long userId);
}
```

---

## 2. 이벤트 기반 아키텍처

### 2.1 도메인 이벤트 패턴

#### 도메인 이벤트 정의

```java
/**
 * 기본 도메인 이벤트
 */
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    
    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}

/**
 * NFT 민팅 이벤트
 */
public class NFTMintedEvent extends DomainEvent {
    private final Long userId;
    private final String nftId;
    private final Integer itemId;
    private final Long localItemId;
    
    public NFTMintedEvent(Long userId, String nftId, Integer itemId, Long localItemId) {
        super();
        this.userId = userId;
        this.nftId = nftId;
        this.itemId = itemId;
        this.localItemId = localItemId;
    }
    
    // Getters
}

/**
 * 주문 생성 이벤트
 */
public class OrderCreatedEvent extends DomainEvent {
    private final String orderId;
    private final String seller;
    private final String nftId;
    private final String price;
    
    // Constructor, Getters
}
```

#### 이벤트 발행자

```java
/**
 * 도메인 이벤트를 관리하는 기본 엔티티
 */
@MappedSuperclass
public abstract class BaseEntity {
    
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    @DomainEvents
    protected Collection<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
```

#### 이벤트 리스너

```java
/**
 * NFT 이벤트 리스너
 */
@Component
@Transactional
public class NFTEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(NFTEventListener.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AchievementService achievementService;
    
    /**
     * NFT 민팅 완료 시 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNFTMinted(NFTMintedEvent event) {
        log.info("NFT minted event received: nftId={}", event.getNftId());
        
        // 1. 사용자에게 알림 발송
        notificationService.sendNFTMintedNotification(
                event.getUserId(), 
                event.getNftId());
        
        // 2. 업적 확인 및 부여
        achievementService.checkAndAwardAchievement(
                event.getUserId(), 
                AchievementType.FIRST_NFT_MINT);
        
        // 3. 통계 업데이트
        statisticsService.incrementNFTMintCount(event.getUserId());
    }
    
    /**
     * NFT 소각 시 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNFTBurned(NFTBurnedEvent event) {
        log.info("NFT burned event received: nftId={}", event.getNftId());
        
        // 소각 관련 후처리
        notificationService.sendNFTBurnedNotification(event.getUserId(), event.getNftId());
    }
}

/**
 * 마켓 이벤트 리스너
 */
@Component
@Transactional
public class MarketEventListener {
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private MarketAnalyticsService analyticsService;
    
    /**
     * 주문 생성 시 캐시 무효화
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 활성 주문 목록 캐시 무효화
        Cache activeOrdersCache = cacheManager.getCache("activeOrdersCache");
        if (activeOrdersCache != null) {
            activeOrdersCache.clear();
        }
        
        // 마켓 통계 업데이트
        analyticsService.recordNewOrder(event);
    }
    
    /**
     * 주문 완료 시 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        // 판매자에게 알림
        notificationService.sendOrderCompletedNotification(
                event.getSeller(), 
                event.getOrderId());
        
        // 구매자에게 알림
        notificationService.sendPurchaseCompletedNotification(
                event.getBuyer(), 
                event.getNftId());
    }
}
```

### 2.2 Spring Events vs 메시지 큐

| 특성 | Spring Events | 메시지 큐 (RabbitMQ/Kafka) |
|------|---------------|---------------------------|
| **범위** | 단일 애플리케이션 내부 | 여러 마이크로서비스 간 |
| **트랜잭션** | 트랜잭션에 참여 가능 | 별도 트랜잭션 |
| **신뢰성** | 메모리 기반 (재시작 시 소실) | 영구 저장, 재처리 가능 |
| **성능** | 빠름 (로컬) | 상대적으로 느림 (네트워크) |
| **확장성** | 제한적 | 높음 |
| **사용 시점** | 간단한 내부 이벤트 | 중요한 비즈니스 이벤트 |

---

## 3. API 설계 Best Practices

### 3.1 RESTful API 개선

#### Before
```java
// ❌ RESTful 원칙 위반
@PostMapping("/sell-orders/confirm/{orderId}")
public ResponseEntity<?> confirmPurchase(...)

@GetMapping("/api/nfts/user/{address}")
public ResponseEntity<?> getUserNftItems(...)
```

#### After
```java
// ✅ RESTful 원칙 준수
@PutMapping("/api/v1/orders/{orderId}/status")
public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
        @PathVariable String orderId,
        @RequestBody UpdateOrderStatusRequest request) {
    // ...
}

@GetMapping("/api/v1/users/{userId}/nfts")
public ResponseEntity<ApiResponse<PagedResponse<NftDto>>> getUserNfts(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    // ...
}

// 리소스 관계 표현
@GetMapping("/api/v1/users/{userId}/inventory/items")
@GetMapping("/api/v1/users/{userId}/conversations/{npcId}")
```

### 3.2 API 버저닝

```java
/**
 * 방법 1: URL 버저닝 (권장)
 */
@RestController
@RequestMapping("/api/v1/nfts")
public class NftControllerV1 {
    // V1 API
}

@RestController
@RequestMapping("/api/v2/nfts")
public class NftControllerV2 {
    // V2 API (향상된 기능)
}

/**
 * 방법 2: Header 버저닝
 */
@RestController
@RequestMapping("/api/nfts")
public class NftController {
    
    @GetMapping(headers = "API-Version=1")
    public ResponseEntity<?> getUserNftsV1(...) {
        // V1 로직
    }
    
    @GetMapping(headers = "API-Version=2")
    public ResponseEntity<?> getUserNftsV2(...) {
        // V2 로직
    }
}

/**
 * 방법 3: Content Negotiation
 */
@GetMapping(produces = "application/vnd.company.nft-v1+json")
public ResponseEntity<?> getUserNftsV1(...) {
    // V1 로직
}

@GetMapping(produces = "application/vnd.company.nft-v2+json")
public ResponseEntity<?> getUserNftsV2(...) {
    // V2 로직
}
```

### 3.3 표준 응답 형식

```java
/**
 * 표준 성공 응답
 */
@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

/**
 * 페이징 응답
 */
@Getter
@Builder
public class PagedResponse<T> {
    private final List<T> content;
    private final int currentPage;
    private final int totalPages;
    private final long totalElements;
    private final int size;
    private final boolean first;
    private final boolean last;
    private final boolean hasNext;
    private final boolean hasPrevious;
    
    public static <T> PagedResponse<T> of(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}

/**
 * Controller 사용 예시
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderDto>>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NFTSellOrder> orderPage = marketService.getOrders(page, size);
        PagedResponse<OrderDto> pagedResponse = PagedResponse.of(
                orderPage.map(OrderDto::from));
        
        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        NFTSellOrder order = marketService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(OrderDto.from(order), "주문이 생성되었습니다"));
    }
}
```

### 3.4 OpenAPI (Swagger) 문서화

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tore NFT Game API")
                        .version("1.0.0")
                        .description("NFT 게임 백엔드 API 문서")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@toregame.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("개발 서버"),
                        new Server().url("https://api.toregame.com").description("운영 서버")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

/**
 * Controller 어노테이션
 */
@RestController
@RequestMapping("/api/v1/nfts")
@Tag(name = "NFT", description = "NFT 관리 API")
public class NftController {
    
    @Operation(
        summary = "NFT 민팅",
        description = "게임 아이템을 NFT로 민팅합니다",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "민팅 성공",
            content = @Content(schema = @Schema(implementation = NftMintClientResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/mint")
    public ResponseEntity<ApiResponse<NftMintClientResponse>> mintNft(
            @Parameter(description = "NFT 민팅 요청 정보", required = true)
            @Valid @RequestBody NftMintClientRequest request) {
        
        NftMintClientResponse response = nftService.mintNft(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## 4. 마이크로서비스 전환 가이드

### 4.1 서비스 분리 전략

#### 현재 모놀리식 구조
```
ToreMainServer (단일 애플리케이션)
├── User Management
├── NFT Management
├── Market
├── Game (NPC, Items)
└── Blockchain Integration
```

#### 마이크로서비스 구조
```
┌─────────────────┐
│   API Gateway   │ (Spring Cloud Gateway)
└────────┬────────┘
         │
    ┌────┴────────────────────────────┐
    │                                  │
┌───▼──────────┐              ┌──────▼────────┐
│ User Service │              │  NFT Service  │
│              │              │               │
│ - 인증/인가   │              │ - NFT 민팅     │
│ - 사용자 관리 │              │ - NFT 소각     │
│ - JWT 발급   │              │ - 소유권 관리  │
└──────────────┘              └───────────────┘
       │                              │
       │                              │
┌──────▼────────┐              ┌─────▼─────────┐
│Market Service │              │  Game Service │
│               │              │               │
│ - 주문 관리    │              │ - NPC 대화    │
│ - 거래 처리    │              │ - 아이템 관리 │
│ - 검색/필터   │              │ - 인벤토리    │
└───────────────┘              └───────────────┘
       │                              │
       │                              │
       └──────────┬───────────────────┘
                  │
          ┌───────▼────────┐
          │ Message Broker │ (RabbitMQ/Kafka)
          └────────────────┘
                  │
          ┌───────▼────────┐
          │Event Processor │
          └────────────────┘
```

### 4.2 API Gateway 설정

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        # User Service
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/api/v1/users/**, /api/v1/auth/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /$\{segment}
            - name: CircuitBreaker
              args:
                name: userServiceCircuitBreaker
                fallbackUri: forward:/fallback/user
        
        # NFT Service
        - id: nft-service
          uri: lb://NFT-SERVICE
          predicates:
            - Path=/api/v1/nfts/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /$\{segment}
            - name: RateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
        
        # Market Service
        - id: market-service
          uri: lb://MARKET-SERVICE
          predicates:
            - Path=/api/v1/orders/**, /api/v1/market/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /$\{segment}
        
        # Game Service
        - id: game-service
          uri: lb://GAME-SERVICE
          predicates:
            - Path=/api/v1/npcs/**, /api/v1/items/**
          filters:
            - RewritePath=/api/v1/(?<segment>.*), /$\{segment}
```

### 4.3 서비스 간 통신

#### Feign Client 사용
```java
/**
 * User Service Client
 */
@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
public interface UserServiceClient {
    
    @GetMapping("/users/{userId}")
    UserDto getUser(@PathVariable Long userId);
    
    @GetMapping("/users/{userId}/wallet")
    WalletInfo getWalletInfo(@PathVariable Long userId);
}

/**
 * Fallback 구현 (Circuit Breaker)
 */
@Component
public class UserServiceFallback implements UserServiceClient {
    
    @Override
    public UserDto getUser(Long userId) {
        // 캐시에서 조회하거나 기본 값 반환
        return UserDto.createDefault(userId);
    }
    
    @Override
    public WalletInfo getWalletInfo(Long userId) {
        throw new ServiceUnavailableException("User service is temporarily unavailable");
    }
}

/**
 * NFT Service에서 User Service 호출
 */
@Service
public class NftService {
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        // User Service에서 사용자 정보 조회
        UserDto user = userServiceClient.getUser(request.getUserId());
        WalletInfo wallet = userServiceClient.getWalletInfo(request.getUserId());
        
        if (wallet.getAddress() == null) {
            throw new InvalidInputException("지갑 주소가 설정되지 않았습니다");
        }
        
        // NFT 민팅 로직...
    }
}
```

---

## 5. 메시지 큐 도입

### 5.1 RabbitMQ 설정

```java
@Configuration
public class RabbitMQConfig {
    
    public static final String NFT_EXCHANGE = "nft.exchange";
    public static final String NFT_MINTING_QUEUE = "nft.minting.queue";
    public static final String NFT_MINTING_ROUTING_KEY = "nft.minting";
    
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_COMPLETED_QUEUE = "order.completed.queue";
    
    @Bean
    public TopicExchange nftExchange() {
        return new TopicExchange(NFT_EXCHANGE);
    }
    
    @Bean
    public Queue nftMintingQueue() {
        return QueueBuilder.durable(NFT_MINTING_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .build();
    }
    
    @Bean
    public Binding nftMintingBinding() {
        return BindingBuilder
                .bind(nftMintingQueue())
                .to(nftExchange())
                .with(NFT_MINTING_ROUTING_KEY);
    }
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message delivery failed: {}", cause);
            }
        });
        return template;
    }
}
```

### 5.2 메시지 발행

```java
@Service
public class NftEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * NFT 민팅 이벤트 발행
     */
    public void publishNFTMintedEvent(NFTMintedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NFT_EXCHANGE,
                RabbitMQConfig.NFT_MINTING_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setHeader("event-type", "NFT_MINTED");
                    return message;
                }
        );
    }
}

@Service
@Transactional
public class NftService {
    
    @Autowired
    private NftEventPublisher eventPublisher;
    
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        // 민팅 로직...
        
        // 이벤트 발행 (비동기)
        NFTMintedEvent event = new NFTMintedEvent(
                user.getId(),
                nftId,
                itemDefinition.getId(),
                userEquipItem.getLocalItemId()
        );
        eventPublisher.publishNFTMintedEvent(event);
        
        return NftMintClientResponse.success(nftId);
    }
}
```

### 5.3 메시지 구독

```java
@Component
public class NFTEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(NFTEventConsumer.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * NFT 민팅 이벤트 처리
     */
    @RabbitListener(queues = RabbitMQConfig.NFT_MINTING_QUEUE)
    public void handleNFTMintedEvent(NFTMintedEvent event) {
        log.info("Received NFT minted event: nftId={}", event.getNftId());
        
        try {
            // 알림 발송
            notificationService.sendNFTMintedNotification(
                    event.getUserId(),
                    event.getNftId()
            );
            
            // 업적 처리
            achievementService.checkAndAward(event.getUserId(), AchievementType.FIRST_NFT_MINT);
            
        } catch (Exception e) {
            log.error("Failed to process NFT minted event", e);
            throw new AmqpRejectAndDontRequeueException("Processing failed", e);
        }
    }
}
```

---

## 📝 마이그레이션 로드맵

### Phase 1: 기초 개선 (1-2개월)
1. ✅ 예외 처리 표준화
2. ✅ 로깅 체계 구축
3. ✅ 테스트 코드 작성
4. ✅ 캐싱 도입
5. ✅ 페이징 처리

### Phase 2: 아키텍처 개선 (2-3개월)
1. ✅ 도메인 주도 설계 적용
2. ✅ 이벤트 기반 아키텍처 도입
3. ✅ API 표준화 및 버저닝
4. ✅ 비동기 처리 확대

### Phase 3: 확장성 확보 (3-6개월)
1. ✅ 메시지 큐 도입
2. ✅ 서비스 분리 준비
3. ✅ API Gateway 구축
4. ✅ 모니터링 시스템 구축

### Phase 4: 마이크로서비스 전환 (6-12개월)
1. ✅ 서비스별 독립 배포
2. ✅ 데이터베이스 분리
3. ✅ Circuit Breaker 적용
4. ✅ 분산 트레이싱

---

**이전 문서**: [성능 최적화 가이드](./PERFORMANCE_GUIDE.md)  
**메인 문서**: [프로젝트 분석](./PROJECT_ANALYSIS.md)

