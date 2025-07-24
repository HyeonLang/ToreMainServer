package com.example.toremainserver.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id; // 또는 Long id; 타입에 맞게

    private String username;
    private String password;

    // JPA 기본 생성자
    public User() {
        // JPA 기본 생성자 (필수)
    }

    // username, password로 생성하는 생성자
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ... 기존 코드 ...

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    // ... 기타 getter/setter ...
} 