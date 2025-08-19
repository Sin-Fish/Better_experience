package com.aeolyn.better_experience.inventory.strategy;

import net.minecraft.item.ItemStack;

/**
 * 按数量排序策略（降序）
 */
public class QuantitySortStrategy implements SortStrategy {
    
    @Override
    public int compare(ItemStack stack1, ItemStack stack2) {
        int count1 = stack1.getCount();
        int count2 = stack2.getCount();
        
        if (count1 != count2) {
            return Integer.compare(count2, count1); // 降序
        }
        
        // 数量相同时按名称排序
        String name1 = stack1.getName().getString();
        String name2 = stack2.getName().getString();
        return name1.compareTo(name2);
    }
    
    @Override
    public String getName() {
        return "按数量降序";
    }
}
