package xyz.acrylicstyle.zombieescape.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.utils.Utils;

public class ZombieEscapeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 0) {
			List<String> cmdArgsList = new ArrayList<>(Arrays.asList(args));
			cmdArgsList.remove(0);
			final String[] cmdArgs = cmdArgsList.toArray(new String[0]);
			if (args[0].equalsIgnoreCase("reload")) {
				Utils.reload();
				sender.sendMessage(ChatColor.GREEN + "✓ 設定を再読み込みしました。");
			} else {
				if (!Utils.senderCheck(sender)) return true;
				Command target = Bukkit.getPluginCommand(args[0]);
				if (target == null) {
					sender.sendMessage(ZombieEscape.lang.get("unknownCommand"));
					return true;
				}
				target.execute(sender, label, cmdArgs);
			}
		} else {
			Bukkit.dispatchCommand(sender, "bukkit:help ZombieEscape");
			return true;
		}
		return true;
	}
}
