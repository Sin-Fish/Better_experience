package com.aeolyn.better_experience.inventory.core;

import com.aeolyn.better_experience.common.util.LogUtil;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import net.minecraft.item.ItemStack;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

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
                String nameA = a.getName().getString(); 
                String nameB = b.getName().getString(); 
                Collator collator = Collator.getInstance(Locale.CHINESE);
                if(collator.compare(nameA, nameB) == 0){
                    return Integer.compare(b.getCount(), a.getCount());
                }
                return collator.compare(nameA, nameB);
            };
            case QUANTITY -> (a, b) -> {
                if (a.isEmpty() && b.isEmpty()) return 0;
                if (a.isEmpty()) return 1;
                if (b.isEmpty()) return -1;
                if(Integer.compare(b.getCount(), a.getCount())==0){
                String nameA = a.getName().getString(); 
                String nameB = b.getName().getString();
                Collator collator = Collator.getInstance(Locale.CHINESE);
                    return collator.compare(nameA, nameB);
                }

                return Integer.compare(b.getCount(), a.getCount());
            };
            case TYPE -> (a, b) -> {
                if (a.isEmpty() && b.isEmpty()) return 0;
                if (a.isEmpty()) return 1;
                if (b.isEmpty()) return -1;
                String typeA = a.getItem().getClass().getSimpleName();
                String typeB = b.getItem().getClass().getSimpleName();
                if (typeA.compareTo(typeB)==0) {
                String nameA = a.getName().getString();   
                String nameB = b.getName().getString();
                Collator collator = Collator.getInstance(Locale.CHINESE);
                if (collator.compare(nameA, nameB)==0){
                    return Integer.compare(b.getCount(), a.getCount());
                }
                    return collator.compare(nameA, nameB);
                }
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
            Collator collator = Collator.getInstance(Locale.CHINESE);
            return ascending ? collator.compare(nameA, nameB) : collator.compare(nameB, nameA);
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