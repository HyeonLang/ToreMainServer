package com.example.toremainserver.dto.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public class UpdateLocationRequest {
    
    @NotNull(message = "locationId는 필수입니다")
    @Min(value = 1, message = "locationId는 1 이상이어야 합니다")
    @Max(value = 3, message = "locationId는 3 이하여야 합니다")
    private Integer locationId;
    
    // profileId는 선택사항 (제공되면 현재 값과 다를 때만 업데이트, null 불가)
    @Positive(message = "profileId는 양수여야 합니다")
    private Long profileId;
    
    public UpdateLocationRequest() {}
    
    public UpdateLocationRequest(Integer locationId) {
        this.locationId = locationId;
    }
    
    public UpdateLocationRequest(Integer locationId, Long profileId) {
        this.locationId = locationId;
        this.profileId = profileId;
    }
    
    public Integer getLocationId() {
        return locationId;
    }
    
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }
    
    public Long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}

