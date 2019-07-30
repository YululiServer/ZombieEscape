package xyz.acrylicstyle.zombieescape.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.providers.ConfigProvider;

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
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/config.yml");
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
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/config.yml");
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
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/config.yml");
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
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/config.yml");
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
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/config.yml");
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
				ConfigProvider config = ConfigProvider.initWithoutException("./plugins/ZombieEscape/config.yml");
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
