package com.example.toremainserver;

import com.example.toremainserver.dto.nft.NftMintClientRequest;
import com.example.toremainserver.dto.nft.NftBurnClientRequest;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.UserEquipItemRepository;
import com.example.toremainserver.repository.UserRepository;
import com.example.toremainserver.service.NftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class NftTestRunner implements CommandLineRunner {
    
    @Autowired
    private UserEquipItemRepository userEquipItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NftService nftService;
    
    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== NFT 테스트 프로그램 ===");
        System.out.println("1: 모든 아이템 NFT화 (민팅)");
        System.out.println("2: 모든 NFT 버닝");
        System.out.println("0: 종료");
        System.out.println("=========================");
        
        while (true) {
            System.out.print("선택하세요 (0-2): ");
            int choice = scanner.nextInt();
            
            switch (choice) {
                case 1:
                    mintAllItems();
                    break;
                case 2:
                    burnAllNfts();
                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    scanner.close();
                    return;
                default:
                    System.out.println("잘못된 선택입니다. 0-2 중에서 선택하세요.");
            }
        }
    }
    
    private void mintAllItems() {
        System.out.println("\n=== NFT 민팅 시작 ===");
        
        // NFT화되지 않은 모든 아이템 조회
        List<UserEquipItem> nonNftItems = userEquipItemRepository.findAll()
            .stream()
            .filter(item -> item.getNftId() == null)
            .toList();
        
        if (nonNftItems.isEmpty()) {
            System.out.println("NFT화할 아이템이 없습니다.");
            return;
        }
        
        System.out.println("총 " + nonNftItems.size() + "개의 아이템을 NFT화합니다...");
        
        int successCount = 0;
        int failCount = 0;
        
        for (UserEquipItem item : nonNftItems) {
            try {
                // 사용자 정보 조회
                Optional<User> userOpt = userRepository.findById(item.getUserId());
                if (userOpt.isEmpty()) {
                    System.out.println("사용자 ID " + item.getUserId() + "를 찾을 수 없습니다.");
                    failCount++;
                    continue;
                }
                
                User user = userOpt.get();
                System.out.println("아이템 ID " + item.getId() + " (사용자: " + user.getUsername() + ") NFT화 중...");
                
                // NFT 민팅 요청 생성
                NftMintClientRequest mintRequest = new NftMintClientRequest(
                    user.getId(),
                    item.getItemId(),
                    item.getId()
                );
                
                // NFT 민팅 실행
                var mintResponse = nftService.mintNft(mintRequest);
                
                if (mintResponse.isSuccess()) {
                    System.out.println("✓ 성공: NFT ID " + mintResponse.getNftId());
                    successCount++;
                } else {
                    System.out.println("✗ 실패: " + mintResponse.getErrorMessage());
                    failCount++;
                }
                
            } catch (Exception e) {
                System.out.println("✗ 오류: " + e.getMessage());
                failCount++;
            }
        }
        
        System.out.println("\n=== 민팅 완료 ===");
        System.out.println("성공: " + successCount + "개");
        System.out.println("실패: " + failCount + "개");
        System.out.println("================\n");
    }
    
    private void burnAllNfts() {
        System.out.println("\n=== NFT 버닝 시작 ===");
        
        // NFT화된 모든 아이템 조회
        List<UserEquipItem> nftItems = userEquipItemRepository.findAll()
            .stream()
            .filter(item -> item.getNftId() != null)
            .toList();
        
        if (nftItems.isEmpty()) {
            System.out.println("버닝할 NFT가 없습니다.");
            return;
        }
        
        System.out.println("총 " + nftItems.size() + "개의 NFT를 버닝합니다...");
        
        int successCount = 0;
        int failCount = 0;
        
        for (UserEquipItem item : nftItems) {
            try {
                // 사용자 정보 조회
                Optional<User> userOpt = userRepository.findById(item.getUserId());
                if (userOpt.isEmpty()) {
                    System.out.println("사용자 ID " + item.getUserId() + "를 찾을 수 없습니다.");
                    failCount++;
                    continue;
                }
                
                User user = userOpt.get();
                System.out.println("NFT ID " + item.getNftId() + " (사용자: " + user.getUsername() + ") 버닝 중...");
                
                // NFT 버닝 요청 생성
                NftBurnClientRequest burnRequest = new NftBurnClientRequest(
                    user.getId(),
                    item.getItemId(),
                    item.getId(),
                    item.getNftId()
                );
                
                // NFT 버닝 실행
                var burnResponse = nftService.burnNft(burnRequest);
                
                if (burnResponse.isSuccess()) {
                    System.out.println("✓ 성공: NFT ID " + item.getNftId() + " 버닝 완료");
                    successCount++;
                } else {
                    System.out.println("✗ 실패: " + burnResponse.getErrorMessage());
                    failCount++;
                }
                
            } catch (Exception e) {
                System.out.println("✗ 오류: " + e.getMessage());
                failCount++;
            }
        }
        
        System.out.println("\n=== 버닝 완료 ===");
        System.out.println("성공: " + successCount + "개");
        System.out.println("실패: " + failCount + "개");
        System.out.println("================\n");
    }
}
