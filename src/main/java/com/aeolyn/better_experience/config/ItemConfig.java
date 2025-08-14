package com.aeolyn.better_experience.config;

import com.google.gson.annotations.SerializedName;

public class ItemConfig {
    @SerializedName("item_id")
    private String itemId;
    
    @SerializedName("enabled")
    private boolean enabled;
    
    @SerializedName("render_as_block")
    private boolean renderAsBlock;
    
    @SerializedName("render_as_entity")
    private boolean renderAsEntity;
    
    @SerializedName("block_id")
    private String blockId;
    
    @SerializedName("entity_type")
    private String entityType;
    
    @SerializedName("first_person")
    private RenderSettings firstPerson;
    
    @SerializedName("third_person")
    private RenderSettings thirdPerson;
    
    public static class RenderSettings {
        @SerializedName("scale")
        private float scale = 1.0f;
        
        @SerializedName("rotation_x")
        private float rotationX = 0.0f;
        
        @SerializedName("rotation_y")
        private float rotationY = 0.0f;
        
        @SerializedName("rotation_z")
        private float rotationZ = 0.0f;
        
        @SerializedName("translate_x")
        private float translateX = 0.0f;
        
        @SerializedName("translate_y")
        private float translateY = 0.0f;
        
        @SerializedName("translate_z")
        private float translateZ = 0.0f;
        
        // Getters
        public float getScale() { return scale; }
        public float getRotationX() { return rotationX; }
        public float getRotationY() { return rotationY; }
        public float getRotationZ() { return rotationZ; }
        public float getTranslateX() { return translateX; }
        public float getTranslateY() { return translateY; }
        public float getTranslateZ() { return translateZ; }
    }
    
    // Getters
    public String getItemId() { return itemId; }
    public boolean isEnabled() { return enabled; }
    public boolean isRenderAsBlock() { return renderAsBlock; }
    public boolean isRenderAsEntity() { return renderAsEntity; }
    public String getBlockId() { return blockId; }
    public String getEntityType() { return entityType; }
    public RenderSettings getFirstPerson() { return firstPerson; }
    public RenderSettings getThirdPerson() { return thirdPerson; }
}
