package com.example.toremainserver.dto.market;

import java.util.Map;

public class OffchainSignatureDataResponse {
    
    private Object sellOrder; // NFTSellOrder 타입
    private String offchainSignature;
    private String messageHash;
    private Domain domain;
    private Map<String, Object> types;
    
    // 기본 생성자
    public OffchainSignatureDataResponse() {}
    
    // 생성자
    public OffchainSignatureDataResponse(Object sellOrder, String offchainSignature, 
                                        String messageHash, Domain domain, Map<String, Object> types) {
        this.sellOrder = sellOrder;
        this.offchainSignature = offchainSignature;
        this.messageHash = messageHash;
        this.domain = domain;
        this.types = types;
    }
    
    // Domain 내부 클래스
    public static class Domain {
        private String name;
        private String version;
        private int chainId;
        private String verifyingContract;
        
        // 기본 생성자
        public Domain() {}
        
        // 생성자
        public Domain(String name, String version, int chainId, String verifyingContract) {
            this.name = name;
            this.version = version;
            this.chainId = chainId;
            this.verifyingContract = verifyingContract;
        }
        
        // Getter와 Setter
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        public int getChainId() {
            return chainId;
        }
        
        public void setChainId(int chainId) {
            this.chainId = chainId;
        }
        
        public String getVerifyingContract() {
            return verifyingContract;
        }
        
        public void setVerifyingContract(String verifyingContract) {
            this.verifyingContract = verifyingContract;
        }
    }
    
    // Getter와 Setter
    public Object getSellOrder() {
        return sellOrder;
    }
    
    public void setSellOrder(Object sellOrder) {
        this.sellOrder = sellOrder;
    }
    
    public String getOffchainSignature() {
        return offchainSignature;
    }
    
    public void setOffchainSignature(String offchainSignature) {
        this.offchainSignature = offchainSignature;
    }
    
    public String getMessageHash() {
        return messageHash;
    }
    
    public void setMessageHash(String messageHash) {
        this.messageHash = messageHash;
    }
    
    public Domain getDomain() {
        return domain;
    }
    
    public void setDomain(Domain domain) {
        this.domain = domain;
    }
    
    public Map<String, Object> getTypes() {
        return types;
    }
    
    public void setTypes(Map<String, Object> types) {
        this.types = types;
    }
}
