package xyz.acrylicstyle.zombieescape.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.acrylicstyle.zombieescape.utils.Utils;

public class ZombieEscapeCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length != 0) {
			List<String> cmdArgsList = new ArrayList<String>();
			cmdArgsList.addAll(Arrays.asList(args));
			cmdArgsList.remove(0);
			final String[] cmdArgs = cmdArgsList.toArray(new String[0]);
			if (args[0].equalsIgnoreCase("reload")) {
				Utils.reload();
				sender.sendMessage(ChatColor.GREEN + "✓ 設定を再読み込みしました。");
			} else if (args[0].equalsIgnoreCase("vote")) {
				if (!Utils.senderCheck(sender)) return true;
				Bukkit.getPluginCommand("vote").execute(sender, label, cmdArgs);
			} else {
				String args2 = "";
				for (String arg : args) args2 += " " + arg;
				((Player) sender).performCommand("bukkit:help ZombieEscape" + args2);
			}
		} else {
			String args2 = "";
			for (String arg : args) args2 += " " + arg;
			((Player) sender).performCommand("bukkit:help ZombieEscape" + args2);
			return true;
		}
		return true;
	}
}
