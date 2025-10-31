package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NftMintClientRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    private Long userId;
    
    @NotNull(message = "프로필 ID는 필수입니다")
    @Positive(message = "프로필 ID는 양수여야 합니다")
    private Long profileId;
    
    @NotNull(message = "장비 아이템 ID는 필수입니다")
    @Positive(message = "장비 아이템 ID는 양수여야 합니다")
    private Long equipItemId;  // 단일 PK (UserEquipItem의 id)
    
    // 기본 생성자
    public NftMintClientRequest() {}
    
    // 생성자
    public NftMintClientRequest(Long userId, Long profileId, Long equipItemId) {
        this.userId = userId;
        this.profileId = profileId;
        this.equipItemId = equipItemId;
    }
    
    // Getter와 Setter
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
    
    public Long getEquipItemId() {
        return equipItemId;
    }
    
    public void setEquipItemId(Long equipItemId) {
        this.equipItemId = equipItemId;
    }
}
