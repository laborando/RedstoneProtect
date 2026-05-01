package org.cel20.redstoneProtect;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.util.EnumSet;
import java.util.Set;

public class ProtectRedstone implements Listener {

    private static final Set<Material> REDSTONE_BLOCKS = EnumSet.of(

            //Normal
            Material.REDSTONE_WIRE,
            Material.REPEATER,
            Material.COMPARATOR,
            Material.REDSTONE_TORCH,
            Material.LEVER,

            //Buttons
            Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON,
            Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON,
            Material.MANGROVE_BUTTON,
            Material.CHERRY_BUTTON,
            Material.PALE_OAK_BUTTON,
            Material.BAMBOO_BUTTON,
            Material.CRIMSON_BUTTON,
            Material.WARPED_BUTTON,
            Material.STONE_BUTTON,
            Material.POLISHED_BLACKSTONE_BUTTON

    );

    @EventHandler
    public void onFlow(BlockFromToEvent e) {

        if (RedstoneProtect.pause.get())
            return;

        Block to = e.getToBlock();

        if (REDSTONE_BLOCKS.contains(to.getType())) {
            RedstoneProtect.getInstance().getLogger().info("Blocked Redstone Destroyed at: " + to.getLocation());
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlace(PlayerBucketEmptyEvent e) {

        if (RedstoneProtect.pause.get())
            return;

        if (REDSTONE_BLOCKS.contains(e.getBlock().getType())) {

            e.getPlayer().sendMessage("Blocked Fluid Placement as there was redstone at the target location.");
            e.getPlayer().sendMessage("To pause redstone protection, execute /redpro");

            e.setCancelled(true);
        }

    }
}
