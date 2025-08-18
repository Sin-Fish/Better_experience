package com.aeolyn.better_experience.client.command;

import com.aeolyn.better_experience.inventory.core.InventorySortController;
import com.aeolyn.better_experience.inventory.config.InventorySortConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

/**
 * 背包整理测试命令
 */
public class InventorySortCommand {
    
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("sortinventory")
            .executes(InventorySortCommand::executeSort)
        );
        
        dispatcher.register(ClientCommandManager.literal("testsort")
            .executes(InventorySortCommand::executeTestSort)
        );
    }
    
    private static int executeSort(CommandContext<FabricClientCommandSource> context) {
        try {
            InventorySortController controller = InventorySortController.getInstance();
            controller.sortInventory(InventorySortConfig.SortMode.NAME);
            
            context.getSource().sendFeedback(Text.literal("§a背包整理完成！"));
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c背包整理失败: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int executeTestSort(CommandContext<FabricClientCommandSource> context) {
        try {
            InventorySortController controller = InventorySortController.getInstance();
            controller.sortInventory(InventorySortConfig.SortMode.NAME);
            
            context.getSource().sendFeedback(Text.literal("§a测试排序完成！"));
            context.getSource().sendFeedback(Text.literal("§e现在可以尝试按R键进行背包整理"));
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c测试排序失败: " + e.getMessage()));
            return 0;
        }
    }
}
