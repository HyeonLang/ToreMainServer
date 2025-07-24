package com.example.toremainserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "items")
public class Item {
    @Id
    private String id;
    private String userId;
    private String name;
    private String description;
    private double price;
    private int quantity;

    public Item() {}

    public Item(String id, String userId, String name, int quantity) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
    }

    // name, description, price로 생성하는 생성자 추가
    public Item(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
} 