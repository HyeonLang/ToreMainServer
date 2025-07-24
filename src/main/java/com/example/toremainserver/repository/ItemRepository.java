package com.example.toremainserver.repository;

import com.example.toremainserver.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByUserId(String userId);
} 