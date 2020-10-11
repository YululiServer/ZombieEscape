package xyz.acrylicstyle.zombieescape.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_api.providers.LanguageProvider;
import xyz.acrylicstyle.tomeito_api.utils.Lang;
import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZombieEscapeConfig {
    public final LanguageProvider lang;

    public ZombieEscapeConfig() {
        lang = ZombieEscape.lang;
    }

    public final class SetSpawn implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command must be run at in-game.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
                return true;
            }
            Player ps = (Player) sender;
            if (args[0].equalsIgnoreCase("zombie")) {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
                    return true;
                }
                ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
                List<String> spawns = config.getStringList("spawnPoints.zombie");
                spawns.add(Integer.parseInt(args[1]), ps.getLocation().getX() + "," + ps.getLocation().getY() + "," + ps.getLocation().getZ());
                config.setThenSave("spawnPoints.zombie", spawns);
            } else if (args[0].equalsIgnoreCase("player")) {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
                    return true;
                }
                ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
                List<String> spawns = config.getStringList("spawnPoints.player");
                spawns.add(Integer.parseInt(args[1]), ps.getLocation().getX() + "," + ps.getLocation().getY() + "," + ps.getLocation().getZ());
                config.setThenSave("spawnPoints.player", spawns);
            } else if (args[0].equalsIgnoreCase("world")) {
                ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
                config.setThenSave("spawnPoints.world", ps.getWorld().getName());
            } else {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
                return true;
            }
            Utils.checkConfig();
            sender.sendMessage(lang.get("savedConfig"));
            return true;
        }
    }

    public final class SetMapName implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setmapname <Map name>");
                return true;
            }
            ConfigProvider.setThenSave("mapname", args[0], new File("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml"));
            Utils.checkConfig();
            sender.sendMessage(lang.get("savedConfig"));
            return true;
        }
    }

    public final class SetMap implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (ZombieEscape.gameStarted || ZombieEscape.timesLeft < 6) {
                sender.sendMessage(lang.get("alreadyStarted"));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setmap <Map>");
                return true;
            }
            ConfigProvider.setThenSave("map", args[0], new File("./plugins/ZombieEscape/config.yml"));
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(Lang.format(lang.get("mapChanged"), sender.getName()));
            }
            Utils.checkConfig();
            Utils.reload();
            sender.sendMessage(lang.get("savedConfig"));
            return true;
        }
    }

    public final class AddWall implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command must be run at in-game.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /addwall <Wall>");
                return true;
            }
            Set<Material> set = new HashSet<>();
            set.add(Material.AIR);
            Block block = ((Player)sender).getTargetBlock(set, 4);
            if (block == null) {
                sender.sendMessage(lang.get("seeTheBlock"));
                return true;
            }
            int x = block.getLocation().getBlockX();
            int y = block.getLocation().getBlockY();
            int z = block.getLocation().getBlockZ();
            String location = x + "," + y + "," + z;
            ConfigProvider config = new ConfigProvider("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
            List<String> walls = config.getStringList("wallLocation." + args[0]);
            walls.add(location);
            config.set("wallLocation." + args[0], walls);
            Map<String, Object> locationWall = ConfigProvider.getConfigSectionValue(config.get("locationWall", new HashMap<String, Object>()), true);
            locationWall.put(location, args[0]);
            config.setThenSave("locationWall", locationWall);
            Utils.checkConfig(); // well we dont need to do this smh
            sender.sendMessage(lang.get("savedConfig"));
            return true;
        }
    }

    public final class DeleteWall implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, final String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command must be run at in-game.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /deletewall <Wall>");
                return true;
            }
            ConfigProvider config = new ConfigProvider("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
            config.setThenSave("wallLocation." + args[0], null);
            // Location, Wallname
            Map<String, Object> locationWall = ConfigProvider.getConfigSectionValue(config.get("locationWall", new HashMap<String, Object>()), true);
            locationWall.forEach((location, wall) -> {
                if (wall.toString().equals(args[0])) locationWall.remove(location);
            });
            Utils.checkConfig(); // well we dont need to do this
            sender.sendMessage(lang.get("savedConfig"));
            return true;
        }
    }

    public final class RemoveSpawn implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command must be run at in-game.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
                return true;
            }
            if (args[0].equalsIgnoreCase("zombie")) {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
                    return true;
                }
                ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
                List<String> spawns = config.getStringList("spawnPoints.zombie");
                spawns.remove(Integer.parseInt(args[1]));
                config.setThenSave("spawnPoints.zombie", spawns.size() == 0 ? null : spawns);
            } else if (args[0].equalsIgnoreCase("player")) {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
                    return true;
                }
                ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
                List<String> spawns = config.getStringList("spawnPoints.player");
                spawns.remove(Integer.parseInt(args[1]));
                config.setThenSave("spawnPoints.player", spawns.size() == 0 ? null : spawns);
            } else if (args[0].equalsIgnoreCase("world")) {
                ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
                config.setThenSave("spawnPoints.world", null);
            } else {
                sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
                return true;
            }
            Utils.checkConfig();
            sender.sendMessage(lang.get("savedConfig"));
            return true;
        }
    }
}
