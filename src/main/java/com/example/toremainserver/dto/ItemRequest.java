package com.example.toremainserver.dto;

public class ItemRequest {
    private String name;
    private String description;
    private double price;

    // 기본 생성자
    public ItemRequest() {}

    // 생성자
    public ItemRequest(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Getter와 Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
} 