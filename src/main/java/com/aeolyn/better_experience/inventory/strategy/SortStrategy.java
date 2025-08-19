package com.aeolyn.better_experience.inventory.strategy;

import net.minecraft.item.ItemStack;

/**
 * 排序策略接口
 * 用于实现不同的排序比较逻辑
 */
public interface SortStrategy {
    
    /**
     * 比较两个物品
     * @param stack1 物品1
     * @param stack2 物品2
     * @return 负数表示stack1应该排在stack2前面，正数表示stack2应该排在stack1前面，0表示相等
     */
    int compare(ItemStack stack1, ItemStack stack2);
    
    /**
     * 获取策略名称
     * @return 策略名称
     */
    String getName();
}
