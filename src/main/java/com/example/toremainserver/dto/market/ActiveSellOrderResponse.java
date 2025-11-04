package com.example.toremainserver.dto.market;

import com.example.toremainserver.entity.NFTSellOrder;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.entity.ItemDefinition;

public class ActiveSellOrderResponse {
    
    private NFTSellOrder sellOrder;
    private UserEquipItem equipItem;
    private ItemDefinition itemDefinition;
    
    public ActiveSellOrderResponse() {}
    
    public ActiveSellOrderResponse(NFTSellOrder sellOrder, UserEquipItem equipItem, ItemDefinition itemDefinition) {
        this.sellOrder = sellOrder;
        this.equipItem = equipItem;
        this.itemDefinition = itemDefinition;
    }
    
    public NFTSellOrder getSellOrder() {
        return sellOrder;
    }
    
    public void setSellOrder(NFTSellOrder sellOrder) {
        this.sellOrder = sellOrder;
    }
    
    public UserEquipItem getEquipItem() {
        return equipItem;
    }
    
    public void setEquipItem(UserEquipItem equipItem) {
        this.equipItem = equipItem;
    }
    
    public ItemDefinition getItemDefinition() {
        return itemDefinition;
    }
    
    public void setItemDefinition(ItemDefinition itemDefinition) {
        this.itemDefinition = itemDefinition;
    }
}

