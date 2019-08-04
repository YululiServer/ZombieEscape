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
import xyz.acrylicstyle.zombieescape.ZombieEscape;

public class ZombieEscapeConfig {
	public final class SetSpawn implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command must be run at in-game.");
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
				return true;
			}
			Player ps = (Player) sender;
			if (args[0].equalsIgnoreCase("zombie")) {
				if (args.length <= 1) {
					sender.sendMessage(ChatColor.RED + "使用法: /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
					return true;
				}
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				List<String> spawns = new ArrayList<String>();
				spawns.addAll(Arrays.asList(config.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])));
				spawns.add(Integer.parseInt(args[1]), ps.getLocation().getX() + "," + ps.getLocation().getY() + "," + ps.getLocation().getZ());
				try {
					config.setThenSave("spawnPoints.zombie", spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("player")) {
				if (args.length <= 1) {
					sender.sendMessage(ChatColor.RED + "使用法: /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
					return true;
				}
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				List<String> spawns = new ArrayList<String>();
				spawns.addAll(Arrays.asList(config.getList("spawnPoints.player", new ArrayList<String>()).toArray(new String[0])));
				spawns.add(Integer.parseInt(args[1]), ps.getLocation().getX() + "," + ps.getLocation().getY() + "," + ps.getLocation().getZ());
				try {
					config.setThenSave("spawnPoints.player", spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("world")) {
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				try {
					config.setThenSave("spawnPoints.world", ps.getWorld().getName());
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "使用法: /setspawn <zombie/player> <0, 1, 2, 3, ...> or /setspawn <world>");
				return true;
			}
			ZombieEscape.checkConfig();
			sender.sendMessage(ChatColor.GREEN + "設定を保存しました。");
			return true;
		}
	}

	public final class SetMapName implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /setmapname <マップ名>");
				return true;
			}
			try {
				ConfigProvider.setThenSave("mapname", args[0], new File("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml"));
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
				return true;
			}
			ZombieEscape.checkConfig();
			sender.sendMessage(ChatColor.GREEN + "設定を保存しました。");
			return true;
		}
	}

	public final class SetMap implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (ZombieEscape.gameStarted || ZombieEscape.timesLeft < 6) {
				sender.sendMessage(ChatColor.RED + "ゲームがすでに開始しているので設定できません！");
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /setmap <マップ名>");
				return true;
			}
			try {
				ConfigProvider.setThenSave("map", args[0], new File("./plugins/ZombieEscape/config.yml"));
				for (Player player : Bukkit.getOnlinePlayers()) {
					player.kickPlayer(ChatColor.RED + sender.getName() + ChatColor.AQUA + "によってマップが変更されました。もう一度参加しなおしてください！");
				}
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
				return true;
			}
			ZombieEscape.checkConfig();
			ZombieEscape.reload();
			sender.sendMessage(ChatColor.GREEN + "設定を保存しました。");
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
				sender.sendMessage(ChatColor.RED + "使用法: /addwall <壁の名前>");
				return true;
			}
			Set<Material> set = new HashSet<Material>();
			set.add(Material.AIR);
			Block block = ((Player)sender).getTargetBlock(set, 4);
			if (block == null) {
				sender.sendMessage(ChatColor.RED + "ブロックを視野に入れてください(4ブロック以内)。");
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
				sender.sendMessage(ChatColor.RED + "コマンドの実行中に不明なエラーが発生しました");
				return true;
			}
			ZombieEscape.checkConfig(); // well we dont need to do this smh
			sender.sendMessage(ChatColor.GREEN + "設定を保存しました。");
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
				sender.sendMessage(ChatColor.RED + "使用法: /deletewall <壁の名前>");
				return true;
			}
			try {
				ConfigProvider config = new ConfigProvider("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				config.setThenSave("wallLocation." + args[0], null);
				// Location, Wallname
				Map<String, Object> locationWall = ConfigProvider.getConfigSectionValue(config.get("locationWall", new HashMap<String, Object>()), true);
				locationWall.forEach((location, wall) -> {
					if (wall.toString() == args[0]) locationWall.remove(location);
				});
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "コマンドの実行中に不明なエラーが発生しました");
				return true;
			}
			ZombieEscape.checkConfig(); // well we dont need to do this
			sender.sendMessage(ChatColor.GREEN + "設定を保存しました。");
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
				sender.sendMessage(ChatColor.RED + "使用法: /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
				return true;
			}
			if (args[0].equalsIgnoreCase("zombie")) {
				if (args.length <= 1) {
					sender.sendMessage(ChatColor.RED + "使用法: /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
					return true;
				}
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				List<String> spawns = new ArrayList<String>();
				spawns.addAll(Arrays.asList(config.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])));
				spawns.remove(Integer.parseInt(args[1]));
				try {
					config.setThenSave("spawnPoints.zombie", spawns.size() == 0 ? null : spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("player")) {
				if (args.length <= 1) {
					sender.sendMessage(ChatColor.RED + "使用法: /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
					return true;
				}
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				List<String> spawns = new ArrayList<String>();
				spawns.addAll(Arrays.asList(config.getList("spawnPoints.player", new ArrayList<String>()).toArray(new String[0])));
				spawns.remove(Integer.parseInt(args[1]));
				try {
					config.setThenSave("spawnPoints.player", spawns.size() == 0 ? null : spawns);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("world")) {
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
				try {
					config.setThenSave("spawnPoints.world", null);
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage(ChatColor.RED + "設定の保存中にエラーが発生しました。");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "使用法: /removespawn <zombie/player> <0, 1, 2, 3, ...> or /removespawn <world>");
				return true;
			}
			ZombieEscape.checkConfig();
			sender.sendMessage(ChatColor.GREEN + "設定を保存しました。");
			return true;
		}
	}
}
