package com.example.toremainserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "item_location_types")
public class ItemLocationType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "code_name", nullable = false, unique = true, length = 50)
    private String codeName;
    
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // 기본 생성자
    public ItemLocationType() {}
    
    // 생성자
    public ItemLocationType(String codeName, String displayName, String description) {
        this.codeName = codeName;
        this.displayName = displayName;
        this.description = description;
    }
    
    // Getter와 Setter
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getCodeName() {
        return codeName;
    }
    
    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

