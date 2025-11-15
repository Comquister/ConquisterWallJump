package dev.conquister.walljump.command;

import java.util.ArrayList;
import java.util.List;
import dev.conquister.walljump.config.WallJumpConfiguration;
import dev.conquister.walljump.player.WPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import dev.conquister.walljump.WallJump;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WallJumpCommand implements CommandExecutor, TabExecutor {

    private final WallJumpConfiguration config;
    public WallJumpCommand() {
        config = WallJump.getInstance().getWallJumpConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        try {
            // Check if the command is walljump
            if (cmd.getName().equalsIgnoreCase("walljump")) {
                if (args.length > 0) {
                    // Check if the sender wants to reload the config
                    if (args[0].equalsIgnoreCase("reload")) {
                        return handleReloadCommand(sender);
                    }
                    if (args[0].equalsIgnoreCase("toggle")) {
                        // Check if the sender wants to toggle the wall jump
                        return handleToggleCommand((Player) sender, args[1]);
                    }
                    if (args[0].equalsIgnoreCase("help")) {
                        // Check if the command is walljump and the argument is help
                        return handleHelpCommand(sender);
                    }
                    if (args[0].equalsIgnoreCase("info")) {
                        // Check if the command is walljump and the argument is info
                        return handleWallJumpInfo(sender);
                    }
                }
                return handleHelpCommand(sender);
            }
            // If the command is not recognized
            return false;
        } catch (Exception e) {
            WallJump.warning("An error occurred while executing the command.");
            return false;
        }
    }

    private boolean handleHelpCommand(@NotNull CommandSender sender) {
        Component helpHeader = Component.text("[===] ")
                .color(NamedTextColor.GOLD)
                .append(Component.text("WallJump Plugin Help", NamedTextColor.WHITE))
                .append(Component.text(" [===]", NamedTextColor.GOLD));
        sender.sendMessage(helpHeader);

        // Interactive help entries
        sender.sendMessage(
                Component.text("/walljump help", NamedTextColor.YELLOW)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/walljump help"))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                Component.text("Show this help message", NamedTextColor.GRAY)))
        );
        sender.sendMessage(
                Component.text("/walljump info", NamedTextColor.YELLOW)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/walljump info"))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                Component.text("Show plugin info", NamedTextColor.GRAY)))
        );
        sender.sendMessage(
                Component.text("/walljump reload", NamedTextColor.YELLOW)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/walljump reload"))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                Component.text("Reload the plugin config", NamedTextColor.GRAY)))
        );
        sender.sendMessage(
                Component.text("/walljump toggle ")
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text("[on]", NamedTextColor.GREEN)
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/walljump toggle on"))
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                        Component.text("Enable wall jump", NamedTextColor.GRAY))))
                        .append(Component.space())
                        .append(Component.text("[off]", NamedTextColor.RED)
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/walljump toggle off"))
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                        Component.text("Disable wall jump", NamedTextColor.GRAY))))
        );
        return true;
    }

    private boolean handleReloadCommand(@NotNull CommandSender sender) {
        config.reload();
        Component message = Component.text("Config reloaded!").color(NamedTextColor.YELLOW);
        sender.sendMessage(message);
        return true;
    }

    private boolean handleToggleCommand(@NotNull Player player, String arg) {
        WPlayer wPlayer = WallJump.getInstance().getPlayerManager().getWPlayer(player);
        if (arg == null) {
            Component message = Component.text("You must specify a state!", NamedTextColor.RED)
                    .append(Component.newline())
                    .append(Component.text("Click: ", NamedTextColor.YELLOW))
                    .append(Component.text("[on]", NamedTextColor.GREEN)
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/walljump toggle on"))
                            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                    Component.text("Enable wall jump", NamedTextColor.GRAY))))
                    .append(Component.space())
                    .append(Component.text("[off]", NamedTextColor.RED)
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/walljump toggle off"))
                            .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                    Component.text("Disable wall jump", NamedTextColor.GRAY))));
            player.sendMessage(message);
            return false;
        } else if (arg.equalsIgnoreCase("on")) {
            wPlayer.enabled = true;
            Component message = Component.text("Wall jump enabled!").color(NamedTextColor.YELLOW);
            player.sendMessage(message);
            return true;
        } else if (arg.equalsIgnoreCase("off")) {
            wPlayer.enabled = false;
            Component message = Component.text("Wall jump disabled!").color(NamedTextColor.YELLOW);
            player.sendMessage(message);
            return true;
        } else {
            // Unknown argument for toggle command
            Component message = Component.text("Unknown command!").color(NamedTextColor.RED).append(Component.newline())
                    .append(Component.text("Usage: /walljump toggle [on|off]").color(NamedTextColor.YELLOW));
            player.sendMessage(message);
            return false;
        }
    }
    @SuppressWarnings("deprecation")
    private boolean handleWallJumpInfo(@NotNull CommandSender sender) {
        Component message = Component.text("WallJump version " + WallJump.getInstance().getDescription().getVersion() + " by Monster_What").color(NamedTextColor.YELLOW);
        sender.sendMessage(message);
        return true;
    }


    // The tab completer
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        try {
            if(args.length > 0) {
                List<String> arguments = new ArrayList<>();
                // Check if the sender has the permission to reload the config
                if(sender.hasPermission("walljump.reload"))
                    arguments.add("reload");
                if(config.getBoolean("toggleCommand")) {
                    arguments.add("on");
                    arguments.add("off");
                }
                // Return the matching arguments
                return getMatchingArgument(args[0], arguments);
            }
            return null;
        }catch (Exception e) {
            WallJump.warning("An error occurred while tab completing the command.");
            return null;
        }
    }

    private List<String> getMatchingArgument(String arg, List<String> elements) {
        try {
            List<String> list = new ArrayList<>();
            for(String s : elements) {
                if(s.toLowerCase().contains(arg.toLowerCase()))
                    list.add(s);
            }
            return list;
        }catch (Exception e) {
            WallJump.warning("An error occurred while getting the matching arguments.");
            return null;
        }
    }
}
