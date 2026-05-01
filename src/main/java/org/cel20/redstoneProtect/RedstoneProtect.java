package org.cel20.redstoneProtect;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.cel20.redstoneProtect.update.CUpdater;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RedstoneProtect extends JavaPlugin {

    public static final AtomicBoolean pause = new AtomicBoolean(false);
    static RedstoneProtect instance;

    static public CUpdater cUpdater;
    Metrics metrics;

    @Override
    public void onEnable() {

        instance = this;

        getServer().getPluginManager().registerEvents(new ProtectRedstone(),  this);

        //Cmd
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(
                    Commands.literal("redprot")

                            .then(Commands.literal("pause")

                                    .executes(ctx -> {

                                        RedstoneProtect.pause.set(!RedstoneProtect.pause.get());

                                        if(!ctx.getSource().getSender().isOp()){
                                            ctx.getSource().getSender().sendMessage("You dont have permission to run this command!");
                                            return 1;
                                        }

                                        if(RedstoneProtect.pause.get()) {
                                            Bukkit.getServer().broadcast(Component.text("Redstone Protection has been paused."));
                                            ctx.getSource().getSender().sendMessage("Reenable with /redprot.");
                                            RedstoneProtect.getInstance().getLogger().info("Reenable with /redprot.");
                                        }else{
                                            Bukkit.getServer().broadcast(Component.text("Redstone Protection has been unpaused."));
                                        }

                                        return 1;
                                    })

                            )


                            .then(Commands.literal("help")

                                    .executes(ctx -> {

                                        ctx.getSource().getSender().sendMessage("RedstoneProtect by Cel20");
                                        ctx.getSource().getSender().sendMessage("");
                                        ctx.getSource().getSender().sendMessage("Stop water accidents destroying your redstone builds.");
                                        ctx.getSource().getSender().sendMessage("");
                                        ctx.getSource().getSender().sendMessage("Commands: /redprot < help | pause | update >");

                                        return 1;
                                    })

                            )

                            .then(Commands.literal("update")
                                    .executes(ctx -> {

                                        if(!ctx.getSource().getSender().isOp()){
                                            ctx.getSource().getSender().sendMessage("You dont have permission to run this command!");
                                            return 1;
                                        }

                                        boolean isFolia = false;

                                        try {
                                            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                                            isFolia = true;
                                        } catch (ClassNotFoundException ignored) {}

                                        if(!RedstoneProtect.cUpdater.shouldUpdate){
                                            ctx.getSource().getSender().sendMessage("The Plugin seems to be up-to-date already!");
                                            return 1;
                                        }

                                        if(isFolia) {
                                            ctx.getSource().getSender().sendMessage("This seems to be a folia server. Please update manually!");
                                            return 1;
                                        }


                                        RedstoneProtect.cUpdater.executeUpdate(RedstoneProtect.getRawInstance());

                                        return 1;
                                    })
                            )

                            .executes(ctx -> {

                                ctx.getSource().getSender().sendMessage("No subcommand!");

                                return 1;
                            })

                            .build()
            );
        });

        try {
            cUpdater = new CUpdater("1.0.0", "redstoneprotect");
        } catch (Exception e) {
            cUpdater = new CUpdater(true);
            e.printStackTrace();
        }

        //Metrics
        int pluginId = 31038;
        metrics = new Metrics(this, pluginId);

        Bukkit.getLogger().info("Redstone Protect enabled!");
    }

    @Override
    public void onDisable() {
        metrics.shutdown();
    }

    public static Plugin getInstance(){
        return instance;
    }

    public static RedstoneProtect getRawInstance(){
        return instance;
    }

    public File getFileNonProt() {
        return super.getFile();
    }

}
