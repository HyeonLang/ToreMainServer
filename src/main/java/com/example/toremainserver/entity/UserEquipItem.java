package com.example.toremainserver.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "user_equip_items",
              indexes = {
                  @Index(name = "idx_profile_id", columnList = "profile_id"),
                  @Index(name = "idx_profile_item_def", columnList = "profile_id, item_def_id"),
                  @Index(name = "idx_nft_id", columnList = "nft_id"),
                  @Index(name = "idx_user_id", columnList = "user_id")
              })
public class UserEquipItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // 단일 PK (equipItemId로 사용)
    
    @Column(name = "profile_id", nullable = true)
    private Long profileId;  // UserGameProfile.id 참조 (nullable - 프로필이 없을 수 있음)
    
    @Column(name = "user_id", nullable = true)
    private Long userId;  // User.id 참조 (nullable - NFT 소유권 추적용, 외래키: users.id)
    
    @Column(name = "item_def_id", nullable = false)
    private Long itemDefId;  // ItemDefinition.id 참조
    
    @Column(name = "location_id", nullable = false)
    private Integer locationId;  // ItemLocationType.id 참조
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enhancement_data", columnDefinition = "JSON")
    private Map<String, Object> enhancementData;
    
    @Column(name = "nft_id", unique = true)
    private String nftId;
    
    // 기본 생성자
    public UserEquipItem() {}
    
    // 생성자
    public UserEquipItem(Long profileId, Long itemDefId) {
        this.profileId = profileId;
        this.itemDefId = itemDefId;
        this.locationId = 1; // 기본값 1
    }
    
    public UserEquipItem(Long profileId, Long itemDefId, Map<String, Object> enhancementData) {
        this.profileId = profileId;
        this.itemDefId = itemDefId;
        this.enhancementData = enhancementData;
        this.locationId = 1; // 기본값 1
    }
    
    public UserEquipItem(Long profileId, Long userId, Long itemDefId, Map<String, Object> enhancementData) {
        this.profileId = profileId;
        this.userId = userId;
        this.itemDefId = itemDefId;
        this.enhancementData = enhancementData;
        this.locationId = 1; // 기본값 1
    }
    
    public UserEquipItem(Long profileId, Long itemDefId, Map<String, Object> enhancementData, String nftId) {
        this.profileId = profileId;
        this.itemDefId = itemDefId;
        this.enhancementData = enhancementData;
        this.nftId = nftId;
        this.locationId = 1; // 기본값 1
    }

    public UserEquipItem(Long profileId, Long itemDefId, Map<String, Object> enhancementData, String nftId, Integer locationId) {
        this.profileId = profileId;
        this.itemDefId = itemDefId;
        this.enhancementData = enhancementData;
        this.nftId = nftId;
        this.locationId = locationId;
    }
    
    // Getter와 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
    
    public Long getItemDefId() {
        return itemDefId;
    }
    
    public void setItemDefId(Long itemDefId) {
        this.itemDefId = itemDefId;
    }
    
    public Integer getLocationId() {
        return locationId;
    }
    
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }
    
    public Map<String, Object> getEnhancementData() {
        return enhancementData;
    }
    
    public void setEnhancementData(Map<String, Object> enhancementData) {
        this.enhancementData = enhancementData;
    }
    
    public String getNftId() {
        return nftId;
    }
    
    public void setNftId(String nftId) {
        this.nftId = nftId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
} 