package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.function.Function;

/**
 * 排序比较器工厂
 * 提供默认比较器和自定义比较器创建方法
 */
public class SortComparatorFactory {
    
    /**
     * 创建默认比较器
     * @param sortMode 排序模式
     * @return 比较器
     */
    public static Comparator<ItemStack> createComparator(InventorySortConfig.SortMode sortMode) {
        return switch (sortMode) {
            case NAME -> (a, b) -> {
                if (a.isEmpty() && b.isEmpty()) return 0;
                if (a.isEmpty()) return 1;
                if (b.isEmpty()) return -1;
                String nameA = a.getItem().getName().getString();
                String nameB = b.getItem().getName().getString();
                return nameA.compareToIgnoreCase(nameB);
            };
            case QUANTITY -> (a, b) -> {
                if (a.isEmpty() && b.isEmpty()) return 0;
                if (a.isEmpty()) return 1;
                if (b.isEmpty()) return -1;
                return Integer.compare(b.getCount(), a.getCount()); // 数量降序
            };
            case TYPE -> (a, b) -> {
                if (a.isEmpty() && b.isEmpty()) return 0;
                if (a.isEmpty()) return 1;
                if (b.isEmpty()) return -1;
                String typeA = a.getItem().getClass().getSimpleName();
                String typeB = b.getItem().getClass().getSimpleName();
                return typeA.compareTo(typeB);
            };
        };
    }
    
    /**
     * 创建自定义比较器
     * @param nameExtractor 名称提取器
     * @param ascending 是否升序
     * @return 比较器
     */
    public static Comparator<ItemStack> createCustomComparator(
            Function<ItemStack, String> nameExtractor,
            boolean ascending) {
        return (a, b) -> {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            
            String nameA = nameExtractor.apply(a);
            String nameB = nameExtractor.apply(b);
            return ascending ? nameA.compareTo(nameB) : nameB.compareTo(nameA);
        };
    }
    
    /**
     * 创建数量比较器
     * @param ascending 是否升序
     * @return 比较器
     */
    public static Comparator<ItemStack> createQuantityComparator(boolean ascending) {
        return (a, b) -> {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            
            int countA = a.getCount();
            int countB = b.getCount();
            return ascending ? Integer.compare(countA, countB) : Integer.compare(countB, countA);
        };
    }
    
    /**
     * 创建复合比较器
     * @param comparators 比较器数组
     * @return 复合比较器
     */
    @SafeVarargs
    public static Comparator<ItemStack> createCompositeComparator(Comparator<ItemStack>... comparators) {
        return (a, b) -> {
            for (Comparator<ItemStack> comparator : comparators) {
                int result = comparator.compare(a, b);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        };
    }
}
