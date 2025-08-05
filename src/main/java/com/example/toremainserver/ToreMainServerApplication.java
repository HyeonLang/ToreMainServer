package com.example.toremainserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import com.example.toremainserver.repository.UserRepository;
import com.example.toremainserver.repository.NpcRepository;
import com.example.toremainserver.repository.ItemDefinitionRepository;
import com.example.toremainserver.repository.UserConsumableItemRepository;
import com.example.toremainserver.repository.UserEquipItemRepository;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.entity.Npc;
import com.example.toremainserver.entity.ItemDefinition;
import com.example.toremainserver.entity.UserConsumableItem;
import com.example.toremainserver.entity.UserEquipItem;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
public class ToreMainServerApplication {

    public static void main(String[] args) {
        System.out.println("Hello");
        SpringApplication.run(ToreMainServerApplication.class, args);
    }

    // RestTemplate 빈 등록
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner testDbConnection(
            UserRepository userRepository,
            NpcRepository npcRepository,
            ItemDefinitionRepository itemDefinitionRepository,
            UserConsumableItemRepository userConsumableItemRepository,
            UserEquipItemRepository userEquipItemRepository) {
        return args -> {
            System.out.println("=== 데이터베이스 연결 테스트 시작 ===");
            
            // 1. User 테스트
            System.out.println("\n--- User 테스트 ---");
            Optional<User> adminUser = userRepository.findByUsername("admin");
            if (adminUser.isPresent()) {
                User user = adminUser.get();
                System.out.println("Admin User ID: " + user.getId());
                System.out.println("Admin Username: " + user.getUsername());
                System.out.println("Admin Password: " + user.getPassword());
            } else {
                System.out.println("Admin user not found.");
            }
            
            // 2. NPC 테스트
            System.out.println("\n--- NPC 테스트 ---");
            try {
                // NPC 생성 테스트
                Map<String, Object> npcInfo = new HashMap<>();
                npcInfo.put("level", 10);
                npcInfo.put("location", "마을 광장");
                npcInfo.put("dialogue", "안녕하세요, 모험가님!");
                
                Npc testNpc = new Npc("상인", npcInfo);
                Npc savedNpc = npcRepository.save(testNpc);
                System.out.println("NPC 생성 성공 - ID: " + savedNpc.getNpcId() + ", 이름: " + savedNpc.getName());
                System.out.println("NPC 정보: " + savedNpc.getNpcInfo());
                
                // NPC 조회 테스트
                Optional<Npc> foundNpc = npcRepository.findById(savedNpc.getNpcId());
                if (foundNpc.isPresent()) {
                    System.out.println("NPC 조회 성공: " + foundNpc.get().getName());
                }
            } catch (Exception e) {
                System.out.println("NPC 테스트 실패: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 3. ItemDefinition 테스트
            System.out.println("\n--- ItemDefinition 테스트 ---");
            try {
                // 아이템 정의 생성 테스트
                Map<String, Object> baseStats = new HashMap<>();
                baseStats.put("attack", 15);
                baseStats.put("defense", 5);
                baseStats.put("durability", 100);
                
                ItemDefinition testItem = new ItemDefinition(
                    "강화된 검",
                    ItemDefinition.ItemType.EQUIPMENT,
                    baseStats,
                    "강화된 강력한 검입니다."
                );
                ItemDefinition savedItem = itemDefinitionRepository.save(testItem);
                System.out.println("아이템 정의 생성 성공 - ID: " + savedItem.getId() + ", 이름: " + savedItem.getName());
                System.out.println("아이템 타입: " + savedItem.getType());
                System.out.println("기본 스탯: " + savedItem.getBaseStats());
                
                // 아이템 정의 조회 테스트
                Optional<ItemDefinition> foundItem = itemDefinitionRepository.findById(savedItem.getId());
                if (foundItem.isPresent()) {
                    System.out.println("아이템 정의 조회 성공: " + foundItem.get().getName());
                }
            } catch (Exception e) {
                System.out.println("ItemDefinition 테스트 실패: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 4. UserConsumableItem 테스트
            System.out.println("\n--- UserConsumableItem 테스트 ---");
            try {
                // 소비 아이템 생성 테스트
                UserConsumableItem testConsumable = new UserConsumableItem(1L, 101, 5, 1001L);
                UserConsumableItem savedConsumable = userConsumableItemRepository.save(testConsumable);
                System.out.println("소비 아이템 생성 성공 - UserID: " + savedConsumable.getUserId() + 
                                 ", ItemID: " + savedConsumable.getItemId() + 
                                 ", 수량: " + savedConsumable.getQuantity() +
                                 ", LocalItemID: " + savedConsumable.getLocalItemId());
                
                // 소비 아이템 조회 테스트
                Optional<UserConsumableItem> foundConsumable = userConsumableItemRepository
                    .findByUserIdAndItemId(1L, 101);
                if (foundConsumable.isPresent()) {
                    System.out.println("소비 아이템 조회 성공: " + foundConsumable.get().getQuantity() + "개");
                }
            } catch (Exception e) {
                System.out.println("UserConsumableItem 테스트 실패: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 5. UserEquipItem 테스트
            System.out.println("\n--- UserEquipItem 테스트 ---");
            try {
                // 장비 아이템 생성 테스트
                Map<String, Object> enhancementData = new HashMap<>();
                enhancementData.put("enhancement_level", 3);
                enhancementData.put("additional_attack", 10);
                enhancementData.put("additional_defense", 5);
                
                UserEquipItem testEquip = new UserEquipItem(1L, 201, enhancementData, "NFT_001", 2001L);
                UserEquipItem savedEquip = userEquipItemRepository.save(testEquip);
                System.out.println("장비 아이템 생성 성공 - ID: " + savedEquip.getId() + 
                                 ", UserID: " + savedEquip.getUserId() + 
                                 ", ItemID: " + savedEquip.getItemId() +
                                 ", LocalItemID: " + savedEquip.getLocalItemId());
                System.out.println("강화 데이터: " + savedEquip.getEnhancementData());
                System.out.println("NFT ID: " + savedEquip.getNftId());
                
                // 장비 아이템 조회 테스트
                Optional<UserEquipItem> foundEquip = userEquipItemRepository.findById(savedEquip.getId());
                if (foundEquip.isPresent()) {
                    System.out.println("장비 아이템 조회 성공: " + foundEquip.get().getNftId());
                }
            } catch (Exception e) {
                System.out.println("UserEquipItem 테스트 실패: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("\n=== 데이터베이스 연결 테스트 완료 ===");
        };
    }
}
