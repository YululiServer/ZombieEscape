package xyz.acrylicstyle.zombieescape.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.json.simple.parser.ParseException;

import net.md_5.bungee.api.ChatColor;
import xyz.acrylicstyle.zombieescape.providers.ConfigProvider;
import xyz.acrylicstyle.zombieescape.utils.PlayerUtils;

public final class Sponsor {
	public final ConfigProvider config;

	public Sponsor() throws IOException, InvalidConfigurationException {
		config = new ConfigProvider("./plugins/ZombieEscape/config.yml");
	}

	public final class SetSponsor implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /setsponsor <プレイヤー名>");
				return true;
			}
			config.reloadWithoutException();
			UUID uuid = null;
			try {
				uuid = PlayerUtils.getByName(args[0]).toUUID();
			} catch (IllegalArgumentException | IOException | ParseException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "プレイヤーを取得中にエラーが発生しました");
				return true;
			}
			if (uuid == null) {
				sender.sendMessage(ChatColor.RED + "プレイヤーを見つけられませんでした: " + args[0]);
				return true;
			}
			List<String> sponsors = new ArrayList<String>();
			sponsors.addAll(Arrays.asList(config.getList("sponsors", new ArrayList<String>()).toArray(new String[0])));
			if (sponsors.contains(uuid.toString())) {
				sender.sendMessage(ChatColor.RED + "指定したプレイヤーはすでにスポンサーです");
				return true;
			}
			sponsors.add(uuid.toString());
			try {
				config.setThenSave("sponsors", sponsors);
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "設定を保存中にエラーが発生しました");
				return true;
			}
			sender.sendMessage(ChatColor.GREEN + args[0] + " をスポンサーとして登録しました。");
			return true;
		}
	}

	public final class RemoveSponsor implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /removesponsor <プレイヤー名>");
				return true;
			}
			config.reloadWithoutException();
			UUID uuid = null;
			try {
				uuid = PlayerUtils.getByName(args[0]).toUUID();
			} catch (IllegalArgumentException | IOException | ParseException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "プレイヤーを取得中にエラーが発生しました");
				return true;
			}
			if (uuid == null) {
				sender.sendMessage(ChatColor.RED + "プレイヤーを見つけられませんでした: " + args[0]);
				return true;
			}
			List<String> sponsors = new ArrayList<String>();
			sponsors.addAll(Arrays.asList(config.getList("sponsors", new ArrayList<String>()).toArray(new String[0])));
			if (!sponsors.contains(uuid.toString())) {
				sender.sendMessage(ChatColor.RED + "指定したプレイヤーはスポンサーではありません");
				return true;
			}
			sponsors.remove(uuid.toString());
			try {
				config.setThenSave("sponsors", sponsors);
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "設定を保存中にエラーが発生しました");
				return true;
			}
			sender.sendMessage(ChatColor.GREEN + args[0] + " をスポンサーから除外しました。");
			return true;
		}
	}
}
