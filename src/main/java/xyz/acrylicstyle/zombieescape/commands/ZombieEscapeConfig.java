package xyz.acrylicstyle.zombieescape.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_core.providers.LanguageProvider;
import xyz.acrylicstyle.tomeito_core.utils.Lang;
import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.utils.Utils;

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
				List<String> spawns = new ArrayList<>(Arrays.asList(config.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])));
				spawns.add(Integer.parseInt(args[1]), ps.getLocation().getX() + "," + ps.getLocation().getY() + "," + ps.getLocation().getZ());
				try {
					config.setThenSave("spawnPoints.zombie", spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(lang.get("errorSavingConfig"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("player")) {
				if (args.length <= 1) {
					sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
					return true;
				}
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				List<String> spawns = new ArrayList<>(Arrays.asList(config.getList("spawnPoints.player", new ArrayList<String>()).toArray(new String[0])));
				spawns.add(Integer.parseInt(args[1]), ps.getLocation().getX() + "," + ps.getLocation().getY() + "," + ps.getLocation().getZ());
				try {
					config.setThenSave("spawnPoints.player", spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(lang.get("errorSavingConfig"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("world")) {
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				try {
					config.setThenSave("spawnPoints.world", ps.getWorld().getName());
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(lang.get("errorSavingConfig"));
					return true;
				}
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
			try {
				ConfigProvider.setThenSave("mapname", args[0], new File("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml"));
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorSavingConfig"));
				return true;
			}
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
			try {
				ConfigProvider.setThenSave("map", args[0], new File("./plugins/ZombieEscape/config.yml"));
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.kickPlayer(Lang.format(lang.get("mapChanged"), sender.getName()));
				}
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorSavingConfig"));
				return true;
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
			try {
				String location = x + "," + y + "," + z;
				ConfigProvider config = new ConfigProvider("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				List<String> walls = config.getStringList("wallLocation." + args[0]);
				walls.add(location);
				config.set("wallLocation." + args[0], walls);
				Map<String, Object> locationWall = ConfigProvider.getConfigSectionValue(config.get("locationWall", new HashMap<String, Object>()), true);
				locationWall.put(location, args[0]);
				config.setThenSave("locationWall", locationWall);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorSavingConfig"));
				return true;
			}
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
			try {
				ConfigProvider config = new ConfigProvider("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				config.setThenSave("wallLocation." + args[0], null);
				// Location, Wallname
				Map<String, Object> locationWall = ConfigProvider.getConfigSectionValue(config.get("locationWall", new HashMap<String, Object>()), true);
				locationWall.forEach((location, wall) -> {
					if (wall.toString().equals(args[0])) locationWall.remove(location);
				});
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorSavingConfig"));
				return true;
			}
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
				List<String> spawns = new ArrayList<>(Arrays.asList(config.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])));
				spawns.remove(Integer.parseInt(args[1]));
				try {
					config.setThenSave("spawnPoints.zombie", spawns.size() == 0 ? null : spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(lang.get("errorSavingConfig"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("player")) {
				if (args.length <= 1) {
					sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
					return true;
				}
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				List<String> spawns = new ArrayList<>(Arrays.asList(config.getList("spawnPoints.player", new ArrayList<String>()).toArray(new String[0])));
				spawns.remove(Integer.parseInt(args[1]));
				try {
					config.setThenSave("spawnPoints.player", spawns.size() == 0 ? null : spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(lang.get("errorSavingConfig"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("world")) {
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				try {
					config.setThenSave("spawnPoints.world", null);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(lang.get("errorSavingConfig"));
					return true;
				}
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
