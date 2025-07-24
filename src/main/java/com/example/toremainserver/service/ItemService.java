package com.example.toremainserver.service;

import com.example.toremainserver.dto.ItemRequest;
import com.example.toremainserver.entity.Item;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.ItemRepository;
import com.example.toremainserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }
    
    // 아이템 추가
    public Item createItem(ItemRequest itemRequest, String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        Item item = new Item(itemRequest.getName(), itemRequest.getDescription(), itemRequest.getPrice());

        return itemRepository.save(item);
    }
    
    // 사용자별 아이템 조회
    public List<Item> getItemsByUserId(String userId) {
        return itemRepository.findByUserId(userId);
    }
    
    // 아이템 조회
    public Item getItem(String id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        return itemOptional.orElse(null);
    }
    
    // 모든 아이템 조회
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    // 아이템 수정
    public Item updateItem(String id, ItemRequest itemRequest) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if (itemOptional.isPresent()) {
            Item existingItem = itemOptional.get();

            return itemRepository.save(existingItem);
        }
        return null;
    }
    
    // 아이템 삭제
    public boolean deleteItem(String id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 