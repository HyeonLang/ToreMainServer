# ⚡ 성능 최적화 가이드

> 현업에서 사용하는 실전 성능 최적화 방법

---

## 📋 목차

1. [데이터베이스 최적화](#1-데이터베이스-최적화)
2. [캐싱 전략](#2-캐싱-전략)
3. [페이징 처리](#3-페이징-처리)
4. [비동기 처리](#4-비동기-처리)
5. [연결 풀 최적화](#5-연결-풀-최적화)
6. [모니터링 및 성능 측정](#6-모니터링-및-성능-측정)

---

## 1. 데이터베이스 최적화

### 1.1 N+1 쿼리 문제 해결

#### ⚠️ 현재 문제점

```java
// ❌ N+1 쿼리 발생
public List<Map<String, Object>> getUserNftItems(String walletAddress) {
    List<UserEquipItem> nftItems = userEquipItemRepository.findNftItemsByWalletAddress(walletAddress);
    List<Map<String, Object>> metadataList = new ArrayList<>();
    
    for (UserEquipItem userEquipItem : nftItems) {
        // 각 아이템마다 DB 쿼리 발생! (N+1 문제)
        ItemDefinition itemDefinition = itemDefinitionRepository.findById(userEquipItem.getItemId())
                .orElseThrow(() -> new RuntimeException("ItemDefinition not found"));
        
        Map<String, Object> metadata = createItemData(itemDefinition, userEquipItem);
        metadataList.add(metadata);
    }
    
    return metadataList;
}
```

#### ✅ 해결 방법 1: @EntityGraph 사용

```java
// Repository
public interface UserEquipItemRepository extends JpaRepository<UserEquipItem, Long> {
    
    @EntityGraph(attributePaths = {"itemDefinition"})
    @Query("SELECT u FROM UserEquipItem u WHERE u.nftId IS NOT NULL " +
           "AND EXISTS (SELECT 1 FROM User user WHERE user.walletAddress = :walletAddress " +
           "AND user.id = u.userId)")
    List<UserEquipItem> findNftItemsByWalletAddressWithItemDefinition(@Param("walletAddress") String walletAddress);
}

// Entity 수정
@Entity
@Table(name = "user_equip_items")
public class UserEquipItem {
    // ... 기존 필드
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemDefinition itemDefinition;
    
    // ... getters/setters
}

// Service
public List<Map<String, Object>> getUserNftItems(String walletAddress) {
    // ✅ 단일 쿼리로 모든 데이터 조회 (JOIN 사용)
    List<UserEquipItem> nftItems = userEquipItemRepository
            .findNftItemsByWalletAddressWithItemDefinition(walletAddress);
    
    return nftItems.stream()
            .map(item -> createItemData(item.getItemDefinition(), item))
            .collect(Collectors.toList());
}
```

#### ✅ 해결 방법 2: Fetch Join 사용

```java
@Repository
public interface UserEquipItemRepository extends JpaRepository<UserEquipItem, Long> {
    
    @Query("SELECT u FROM UserEquipItem u " +
           "JOIN FETCH u.itemDefinition " +
           "WHERE u.nftId IS NOT NULL " +
           "AND EXISTS (SELECT 1 FROM User user WHERE user.walletAddress = :walletAddress " +
           "AND user.id = u.userId)")
    List<UserEquipItem> findNftItemsByWalletAddressWithFetchJoin(@Param("walletAddress") String walletAddress);
}
```

#### ✅ 해결 방법 3: IN 절 사용

```java
public List<Map<String, Object>> getUserNftItems(String walletAddress) {
    List<UserEquipItem> nftItems = userEquipItemRepository
            .findNftItemsByWalletAddress(walletAddress);
    
    // ✅ 모든 itemId를 한 번에 조회
    List<Integer> itemIds = nftItems.stream()
            .map(UserEquipItem::getItemId)
            .distinct()
            .collect(Collectors.toList());
    
    Map<Integer, ItemDefinition> itemDefinitionMap = itemDefinitionRepository
            .findAllById(itemIds)
            .stream()
            .collect(Collectors.toMap(ItemDefinition::getId, item -> item));
    
    return nftItems.stream()
            .map(item -> {
                ItemDefinition itemDefinition = itemDefinitionMap.get(item.getItemId());
                return createItemData(itemDefinition, item);
            })
            .collect(Collectors.toList());
}
```

### 1.2 인덱스 최적화

#### 복합 인덱스 추가

```java
@Entity
@Table(name = "conversations",
       indexes = {
           @Index(name = "idx_user_npc", columnList = "user_id, npc_id", unique = true),
           @Index(name = "idx_last_updated", columnList = "last_updated")
       })
public class Conversation {
    // ...
}

@Entity
@Table(name = "user_equip_items",
       indexes = {
           @Index(name = "idx_user_item_local", columnList = "user_id, item_id, local_item_id", unique = true),
           @Index(name = "idx_nft_id", columnList = "nft_id"),
           @Index(name = "idx_user_id", columnList = "user_id")
       })
public class UserEquipItem {
    // ...
}

@Entity
@Table(name = "nft_sell_orders",
       indexes = {
           @Index(name = "idx_status_created", columnList = "status, created_at"),
           @Index(name = "idx_seller_status", columnList = "seller, status"),
           @Index(name = "idx_nft_contract_token", columnList = "nft_contract, token_id"),
           @Index(name = "idx_currency_price", columnList = "currency, price")
       })
public class NFTSellOrder {
    // ...
}
```

#### 인덱스 사용 확인

```sql
-- 쿼리 실행 계획 확인
EXPLAIN SELECT * FROM conversations WHERE user_id = 1 AND npc_id = 1;

-- 인덱스 사용 통계 확인
SHOW INDEX FROM conversations;

-- Slow Query 로그 활성화
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1; -- 1초 이상 걸리는 쿼리 로깅
```

### 1.3 쿼리 최적화

#### ✅ 필요한 컬럼만 조회

```java
// ❌ 모든 컬럼 조회
@Query("SELECT u FROM User u WHERE u.username = :username")
Optional<User> findByUsername(@Param("username") String username);

// ✅ 필요한 컬럼만 조회 (DTO Projection)
@Query("SELECT new com.example.toremainserver.dto.UserBasicInfo(u.id, u.username, u.playername) " +
       "FROM User u WHERE u.username = :username")
Optional<UserBasicInfo> findBasicInfoByUsername(@Param("username") String username);

// DTO
@Getter
@AllArgsConstructor
public class UserBasicInfo {
    private Long id;
    private String username;
    private String playername;
}
```

#### ✅ COUNT 쿼리 최적화

```java
// ❌ 전체 데이터 로드 후 카운트
public long getActiveOrdersCount() {
    return sellOrderRepository.findActiveOrders().size();
}

// ✅ COUNT 쿼리 사용
@Query("SELECT COUNT(o) FROM NFTSellOrder o WHERE o.status = 'ACTIVE'")
long countActiveOrders();
```

#### ✅ Batch Size 설정

```properties
# application.properties
spring.jpa.properties.hibernate.default_batch_fetch_size=100
```

```java
@Entity
public class ItemDefinition {
    @OneToMany(mappedBy = "itemDefinition")
    @BatchSize(size = 100)
    private List<UserEquipItem> userEquipItems;
}
```

---

## 2. 캐싱 전략

### 2.1 Spring Cache 설정

#### 의존성 추가
```gradle
implementation 'org.springframework.boot:spring-boot-starter-cache'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'com.github.ben-manes.caffeine:caffeine' // 로컬 캐시
```

#### Redis 설정

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // NPC 정보: 1시간 캐싱
        cacheConfigurations.put("npcCache", 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 아이템 정의: 30분 캐싱
        cacheConfigurations.put("itemDefinitionCache", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 활성 주문: 1분 캐싱 (자주 변경됨)
        cacheConfigurations.put("activeOrdersCache", 
                defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // 사용자 정보: 10분 캐싱
        cacheConfigurations.put("userCache", 
                defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
    
    /**
     * 로컬 캐시 (Caffeine) - Redis 실패 시 Fallback
     */
    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
```

### 2.2 캐시 적용

#### Service 계층

```java
@Service
@CacheConfig(cacheNames = "npcCache")
public class NpcService {
    
    /**
     * NPC 정보 조회 (캐싱)
     * Key: npc::1, npc::2 형식
     */
    @Cacheable(key = "#npcId")
    public Npc getNpc(Long npcId) {
        return npcRepository.findByNpcId(npcId)
                .orElseThrow(() -> new ResourceNotFoundException("NPC", npcId));
    }
    
    /**
     * NPC 정보 업데이트 (캐시 무효화)
     */
    @CachePut(key = "#npc.npcId")
    public Npc updateNpc(Npc npc) {
        return npcRepository.save(npc);
    }
    
    /**
     * NPC 삭제 (캐시 제거)
     */
    @CacheEvict(key = "#npcId")
    public void deleteNpc(Long npcId) {
        npcRepository.deleteById(npcId);
    }
    
    /**
     * 모든 NPC 캐시 초기화
     */
    @CacheEvict(allEntries = true)
    public void clearNpcCache() {
        // 캐시 전체 삭제
    }
}

@Service
@CacheConfig(cacheNames = "itemDefinitionCache")
public class ItemService {
    
    @Cacheable(key = "#itemId")
    public ItemDefinition getItemDefinition(Integer itemId) {
        return itemDefinitionRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemDefinition", itemId.longValue()));
    }
    
    /**
     * 여러 아이템 조회 (개별 캐싱)
     */
    @Cacheable(key = "#itemId")
    public ItemDefinition findItemById(Integer itemId) {
        return itemDefinitionRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemDefinition", itemId.longValue()));
    }
    
    public List<ItemDefinition> getItemDefinitions(List<Integer> itemIds) {
        return itemIds.stream()
                .map(this::findItemById) // 각 아이템은 캐시에서 조회
                .collect(Collectors.toList());
    }
}

@Service
@CacheConfig(cacheNames = "activeOrdersCache")
public class MarketService {
    
    /**
     * 활성 주문 목록 캐싱
     * 1분마다 갱신됨
     */
    @Cacheable(key = "'activeOrders'")
    public List<NFTSellOrder> getActiveSellOrders() {
        return sellOrderRepository.findActiveOrders();
    }
    
    /**
     * 주문 생성 시 캐시 무효화
     */
    @CacheEvict(key = "'activeOrders'")
    public NFTSellOrder createSellOrder(CreateSellOrderRequest request) {
        // ... 주문 생성 로직
        return sellOrderRepository.save(sellOrder);
    }
}
```

### 2.3 캐시 키 전략

```java
@Service
public class UserService {
    
    /**
     * SpEL을 사용한 복합 키
     */
    @Cacheable(cacheNames = "userCache", 
               key = "#username + ':' + #includeDetails")
    public UserDto getUser(String username, boolean includeDetails) {
        // ...
    }
    
    /**
     * 커스텀 키 생성기
     */
    @Cacheable(cacheNames = "nftCache", keyGenerator = "customKeyGenerator")
    public List<NFTMetadata> getUserNfts(Long userId, String currency, String sortBy) {
        // ...
    }
}

@Component
public class CustomKeyGenerator implements KeyGenerator {
    
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return target.getClass().getSimpleName() + "_" +
               method.getName() + "_" +
               Arrays.stream(params)
                       .map(Object::toString)
                       .collect(Collectors.joining("_"));
    }
}
```

### 2.4 캐시 모니터링

```java
@RestController
@RequestMapping("/api/admin/cache")
public class CacheMonitoringController {
    
    @Autowired
    private CacheManager cacheManager;
    
    /**
     * 모든 캐시 통계 조회
     */
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("name", cacheName);
                // Redis 캐시인 경우
                if (cache.getNativeCache() instanceof org.springframework.data.redis.cache.RedisCache) {
                    // Redis 통계 수집
                }
                stats.put(cacheName, cacheStats);
            }
        });
        
        return stats;
    }
    
    /**
     * 특정 캐시 초기화
     */
    @DeleteMapping("/{cacheName}")
    public void clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
    
    /**
     * 모든 캐시 초기화
     */
    @DeleteMapping("/all")
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}
```

---

## 3. 페이징 처리

### 3.1 Pageable 적용

#### Repository

```java
public interface NFTSellOrderRepository extends JpaRepository<NFTSellOrder, Long> {
    
    /**
     * 페이징 지원 조회
     */
    Page<NFTSellOrder> findByStatus(NFTSellOrder.OrderStatus status, Pageable pageable);
    
    /**
     * 정렬 지원 조회
     */
    List<NFTSellOrder> findByStatus(NFTSellOrder.OrderStatus status, Sort sort);
    
    /**
     * 커스텀 쿼리 + 페이징
     */
    @Query("SELECT o FROM NFTSellOrder o WHERE o.status = :status AND o.price BETWEEN :minPrice AND :maxPrice")
    Page<NFTSellOrder> findByStatusAndPriceRange(
            @Param("status") NFTSellOrder.OrderStatus status,
            @Param("minPrice") String minPrice,
            @Param("maxPrice") String maxPrice,
            Pageable pageable);
}
```

#### Service

```java
@Service
public class MarketService {
    
    /**
     * 페이징된 활성 주문 조회
     */
    public Page<NFTSellOrder> getActiveSellOrders(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        return sellOrderRepository.findByStatus(NFTSellOrder.OrderStatus.ACTIVE, pageable);
    }
    
    /**
     * 가격 범위 + 페이징
     */
    public Page<NFTSellOrder> searchOrders(String minPrice, String maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return sellOrderRepository.findByStatusAndPriceRange(
                NFTSellOrder.OrderStatus.ACTIVE, 
                minPrice, 
                maxPrice, 
                pageable);
    }
}
```

#### Controller

```java
@RestController
@RequestMapping("/api")
public class MarketController {
    
    /**
     * 페이징 지원 API
     * 
     * GET /api/sell-orders?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/sell-orders")
    public ResponseEntity<ApiResponse<PagedResponse<NFTSellOrder>>> getActiveSellOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Page<NFTSellOrder> orderPage = marketService.getActiveSellOrders(page, size, sortBy, direction);
        
        PagedResponse<NFTSellOrder> pagedResponse = PagedResponse.<NFTSellOrder>builder()
                .content(orderPage.getContent())
                .currentPage(orderPage.getNumber())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .size(orderPage.getSize())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }
}

/**
 * 페이징 응답 DTO
 */
@Getter
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
}
```

### 3.2 Cursor-based Pagination (무한 스크롤)

```java
public interface NFTSellOrderRepository extends JpaRepository<NFTSellOrder, Long> {
    
    /**
     * Cursor 기반 페이징 (성능 우수)
     */
    @Query("SELECT o FROM NFTSellOrder o WHERE o.status = 'ACTIVE' " +
           "AND o.id < :cursor ORDER BY o.id DESC")
    List<NFTSellOrder> findNextPage(@Param("cursor") Long cursor, Pageable pageable);
}

@Service
public class MarketService {
    
    public CursorPageResponse<NFTSellOrder> getActiveOrdersCursor(Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1); // 1개 더 조회하여 hasNext 판단
        
        List<NFTSellOrder> orders;
        if (cursor == null) {
            // 첫 페이지
            orders = sellOrderRepository.findByStatus(NFTSellOrder.OrderStatus.ACTIVE, 
                    PageRequest.of(0, size + 1, Sort.by("id").descending())).getContent();
        } else {
            orders = sellOrderRepository.findNextPage(cursor, pageable);
        }
        
        boolean hasNext = orders.size() > size;
        if (hasNext) {
            orders = orders.subList(0, size);
        }
        
        Long nextCursor = hasNext && !orders.isEmpty() ? 
                orders.get(orders.size() - 1).getId() : null;
        
        return new CursorPageResponse<>(orders, nextCursor, hasNext);
    }
}

@Getter
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> content;
    private Long nextCursor;
    private boolean hasNext;
}
```

---

## 4. 비동기 처리

### 4.1 @Async 설정

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async method {} threw exception: {}", method.getName(), ex.getMessage(), ex);
        };
    }
}
```

### 4.2 비동기 작업 예시

```java
@Service
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    /**
     * 이메일 발송 (비동기)
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {}", to);
        try {
            // 이메일 발송 로직 (시간이 오래 걸림)
            Thread.sleep(3000);
            log.info("Email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 푸시 알림 발송 (비동기)
     */
    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendPushNotification(Long userId, String message) {
        log.info("Sending push notification to user: {}", userId);
        try {
            // 푸시 알림 발송 로직
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
            return CompletableFuture.completedFuture(false);
        }
    }
}

@Service
public class NftService {
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        // 동기 처리: NFT 민팅
        // ... 민팅 로직
        
        // 비동기 처리: 알림 발송 (메인 로직에 영향 없음)
        notificationService.sendEmail(user.getEmail(), 
                "NFT Minted", 
                "Your NFT has been minted successfully!");
        
        notificationService.sendPushNotification(user.getId(), 
                "NFT minted successfully!");
        
        return NftMintClientResponse.success(nftId);
    }
}
```

### 4.3 블록체인 호출 비동기 처리

```java
@Service
public class BlockchainAsyncService {
    
    /**
     * 비동기 NFT 민팅
     */
    @Async("taskExecutor")
    public CompletableFuture<String> mintNftAsync(Long userId, Integer itemId, Long localItemId) {
        try {
            // 블록체인 호출 (시간이 오래 걸림)
            ContractNftResponse response = blockchainClient.mintNft(...);
            
            if (response.isSuccess()) {
                // DB 업데이트
                updateNftId(localItemId, response.getNftId());
                return CompletableFuture.completedFuture(response.getNftId());
            } else {
                throw new BlockchainException(response.getErrorMessage());
            }
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}

@RestController
@RequestMapping("/api")
public class NftController {
    
    /**
     * 비동기 NFT 민팅 요청
     * 즉시 응답 반환, 실제 처리는 백그라운드에서 수행
     */
    @PostMapping("/nft/mint/async")
    public ResponseEntity<ApiResponse<String>> mintNftAsync(@Valid @RequestBody NftMintClientRequest request) {
        
        // 작업 ID 생성
        String taskId = UUID.randomUUID().toString();
        
        // 비동기 처리 시작
        CompletableFuture<String> future = blockchainAsyncService.mintNftAsync(
                request.getUserId(), 
                request.getItemId(), 
                request.getLocalItemId());
        
        // 완료 시 콜백
        future.thenAccept(nftId -> {
            log.info("NFT minting completed - taskId: {}, nftId: {}", taskId, nftId);
            // 사용자에게 알림 발송
            notificationService.sendPushNotification(request.getUserId(), 
                    "NFT minting completed! NFT ID: " + nftId);
        }).exceptionally(ex -> {
            log.error("NFT minting failed - taskId: {}", taskId, ex);
            notificationService.sendPushNotification(request.getUserId(), 
                    "NFT minting failed: " + ex.getMessage());
            return null;
        });
        
        // 즉시 응답 반환
        return ResponseEntity.accepted().body(
                ApiResponse.success("NFT minting request accepted. Task ID: " + taskId));
    }
    
    /**
     * 작업 상태 조회
     */
    @GetMapping("/nft/mint/status/{taskId}")
    public ResponseEntity<ApiResponse<TaskStatus>> getMintingStatus(@PathVariable String taskId) {
        // Redis 등에서 작업 상태 조회
        TaskStatus status = taskStatusService.getStatus(taskId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
```

---

## 5. 연결 풀 최적화

### 5.1 HikariCP 설정

```properties
# application.properties

# 연결 풀 크기 (권장: CPU 코어 수 * 2 + 효율적인 디스크 스핀들 수)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10

# 연결 타임아웃 (30초)
spring.datasource.hikari.connection-timeout=30000

# 유휴 연결 타임아웃 (10분)
spring.datasource.hikari.idle-timeout=600000

# 연결 최대 수명 (30분)
spring.datasource.hikari.max-lifetime=1800000

# 연결 테스트 쿼리
spring.datasource.hikari.connection-test-query=SELECT 1

# 누수 감지 (개발 환경에서만)
spring.datasource.hikari.leak-detection-threshold=60000
```

### 5.2 RestTemplate 연결 풀 설정

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
                new HttpComponentsClientHttpRequestFactory();
        
        // 연결 풀 설정
        PoolingHttpClientConnectionManager connectionManager = 
                new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200); // 최대 연결 수
        connectionManager.setDefaultMaxPerRoute(20); // 라우트당 최대 연결 수
        
        // 타임아웃 설정
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000) // 연결 요청 타임아웃
                .setConnectTimeout(5000) // 연결 타임아웃
                .setSocketTimeout(10000) // 소켓 읽기 타임아웃
                .build();
        
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();
        
        factory.setHttpClient(httpClient);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // 에러 핸들러 설정
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        
        return restTemplate;
    }
}
```

---

## 6. 모니터링 및 성능 측정

### 6.1 Spring Boot Actuator

**의존성 추가**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-registry-prometheus'
```

**설정**
```properties
# Actuator 설정
management.endpoints.web.exposure.include=health,metrics,prometheus,info
management.endpoint.health.show-details=always
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.system=true
```

### 6.2 커스텀 메트릭

```java
@Service
public class NftService {
    
    private final Counter nftMintCounter;
    private final Timer nftMintTimer;
    
    public NftService(MeterRegistry meterRegistry) {
        this.nftMintCounter = Counter.builder("nft.mint.count")
                .description("Total number of NFT mints")
                .tag("status", "success")
                .register(meterRegistry);
        
        this.nftMintTimer = Timer.builder("nft.mint.duration")
                .description("NFT minting duration")
                .register(meterRegistry);
    }
    
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        return nftMintTimer.record(() -> {
            try {
                // 민팅 로직
                NftMintClientResponse response = doMint(request);
                nftMintCounter.increment();
                return response;
            } catch (Exception e) {
                Counter.builder("nft.mint.count")
                        .tag("status", "failed")
                        .register(meterRegistry)
                        .increment();
                throw e;
            }
        });
    }
}
```

### 6.3 쿼리 성능 로깅

```properties
# 느린 쿼리 로깅
spring.jpa.properties.hibernate.session.events.log.LOG_QUERIES_SLOWER_THAN_MS=100

# 쿼리 통계
spring.jpa.properties.hibernate.generate_statistics=true
```

---

**다음 문서**: [아키텍처 개선 가이드](./ARCHITECTURE_GUIDE.md)

