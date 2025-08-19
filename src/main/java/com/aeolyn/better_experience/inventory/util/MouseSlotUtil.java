package com.aeolyn.better_experience.inventory.util;

import com.aeolyn.better_experience.common.util.LogUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

/**
 * 鼠标槽位检测工具类
 * 提供统一的鼠标位置槽位检测功能
 * 供背包排序和智能转移功能共同使用
 */
public class MouseSlotUtil {
    
    /**
     * 获取指定位置的槽位
     * 使用反射调用 HandledScreen 的 getSlotAt 方法
     * 
     * @param handledScreen 容器界面
     * @param mouseX 鼠标X坐标（已缩放）
     * @param mouseY 鼠标Y坐标（已缩放）
     * @return 鼠标下的槽位，如果未找到则返回null
     */
    public static Slot getSlotAtPosition(HandledScreen<?> handledScreen, double mouseX, double mouseY) {
        try {
            // 尝试通过反射获取槽位
            Class<?> currentClass = handledScreen.getClass();
            while (currentClass != null) {
                String[] possibleMethodNames = {"getSlotAt", "method_5452", "method_2385", "method_1542", "method_64240", "method_2383", "method_64241", "method_2381", "method_2378"};
                for (String methodName : possibleMethodNames) {
                    try {
                        java.lang.reflect.Method getSlotAtMethod = currentClass.getDeclaredMethod(methodName, double.class, double.class);
                        getSlotAtMethod.setAccessible(true);
                        Slot slot = (Slot) getSlotAtMethod.invoke(handledScreen, mouseX, mouseY);
                        if (slot != null) {
                            LogUtil.info("MouseSlotUtil", "成功调用 " + methodName + "，找到槽位: " + slot.id);
                            return slot;
                        }
                    } catch (Exception e) {
                        // 继续尝试下一个方法
                    }
                }
                if (currentClass != null) {
                    currentClass = currentClass.getSuperclass();
                }
            }
        } catch (Exception e) {
            LogUtil.warn("MouseSlotUtil", "获取槽位失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取当前鼠标下的槽位
     * 自动获取鼠标位置并调用 getSlotAtPosition
     * 
     * @param handledScreen 容器界面
     * @return 鼠标下的槽位，如果未找到则返回null
     */
    public static Slot getSlotAtMouse(HandledScreen<?> handledScreen) {
        try {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            if (client == null) {
                return null;
            }
            
            // 获取缩放后的鼠标位置
            double mouseX = client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth();
            double mouseY = client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
            
            return getSlotAtPosition(handledScreen, mouseX, mouseY);
        } catch (Exception e) {
            LogUtil.warn("MouseSlotUtil", "获取鼠标槽位失败", e);
            return null;
        }
    }
}
