package com.aeolyn.better_experience.inventory.strategy;

import net.minecraft.item.ItemStack;

/**
 * 按名称排序策略（升序）
 */
public class NameSortStrategy implements SortStrategy {
    
    @Override
    public int compare(ItemStack stack1, ItemStack stack2) {
        String name1 = stack1.getName().getString();
        String name2 = stack2.getName().getString();
        int nameCompare = name1.compareTo(name2);
        
        // 如果名称相同，按数量降序排序（数量多的在前）
        if (nameCompare == 0) {
            int count1 = stack1.getCount();
            int count2 = stack2.getCount();
            return Integer.compare(count2, count1); // 降序
        }
        return nameCompare;
    }
    
    @Override
    public String getName() {
        return "按名称升序";
    }
}
