package com.example.toremainserver.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "item_definitions")
public class ItemDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;  // itemId PK
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ItemType type;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_stats", columnDefinition = "JSON")
    private Map<String, Object> baseStats;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_stackable")
    private Boolean isStackable = true;
    
    @Column(name = "max_stack")
    private Integer maxStack = 99;
    
    @Column(name = "image_url", length = 500, nullable = true)
    private String imageUrl;
    
    @Column(name = "ipfs_image_url", length = 500, nullable = true)
    private String ipfsImageUrl;
    
    // 아이템 타입 enum
    public enum ItemType {
        CONSUMABLE("consumable"),
        EQUIPMENT("equipment"),
        ETC("etc");
        
        private final String value;
        
        ItemType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // 기본 생성자
    public ItemDefinition() {}
    
    // 생성자
    public ItemDefinition(String name, ItemType type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }
    
    public ItemDefinition(String name, ItemType type, Map<String, Object> baseStats, String description) {
        this.name = name;
        this.type = type;
        this.baseStats = baseStats;
        this.description = description;
    }
    
    public ItemDefinition(String name, ItemType type, Map<String, Object> baseStats, String description, String imageUrl, String ipfsImageUrl) {
        this.name = name;
        this.type = type;
        this.baseStats = baseStats;
        this.description = description;
        this.imageUrl = imageUrl;
        this.ipfsImageUrl = ipfsImageUrl;
    }
    
    // Getter와 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public ItemType getType() {
        return type;
    }
    
    public void setType(ItemType type) {
        this.type = type;
    }
    
    public Map<String, Object> getBaseStats() {
        return baseStats;
    }
    
    public void setBaseStats(Map<String, Object> baseStats) {
        this.baseStats = baseStats;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsStackable() {
        return isStackable;
    }
    
    public void setIsStackable(Boolean isStackable) {
        this.isStackable = isStackable;
    }
    
    public Integer getMaxStack() {
        return maxStack;
    }
    
    public void setMaxStack(Integer maxStack) {
        this.maxStack = maxStack;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getIpfsImageUrl() {
        return ipfsImageUrl;
    }
    
    public void setIpfsImageUrl(String ipfsImageUrl) {
        this.ipfsImageUrl = ipfsImageUrl;
    }
    
    /**
     * 사용 가능한 이미지 URL을 반환 (우선순위: imageUrl > ipfsImageUrl)
     * @return 사용 가능한 이미지 URL, 둘 다 null이면 null
     */
    public String getAvailableImageUrl() {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            return imageUrl;
        }
        if (ipfsImageUrl != null && !ipfsImageUrl.trim().isEmpty()) {
            return ipfsImageUrl;
        }
        return null;
    }
    
    /**
     * 이미지가 있는지 확인
     * @return 이미지 URL이 하나라도 있으면 true
     */
    public boolean hasImage() {
        return (imageUrl != null && !imageUrl.trim().isEmpty()) || 
               (ipfsImageUrl != null && !ipfsImageUrl.trim().isEmpty());
    }
} 