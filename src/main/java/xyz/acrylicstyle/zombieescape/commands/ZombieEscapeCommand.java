package xyz.acrylicstyle.zombieescape.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
			} else if (args[0].equalsIgnoreCase("debug")) {
				if (!sender.hasPermission("*") || !sender.isOp()) {
					sender.sendMessage(ChatColor.RED + "Sorry but you don't have enough permission.");
					return true;
				}
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Usage:");
					sender.sendMessage(ChatColor.RED + "/zombieescape debug <Class> <Field> [= [Value]] - Get / Set field.");
					sender.sendMessage(ChatColor.RED + "/zombieescape debug <Class> <Method> ( <args...> ) - Invoke method with args.");
					return true;
				}
				try {
					boolean did = false;
					for (int i = 1; i < args.length; i++) {
						if (!did) did = true; else return true;
						Class<?> clazz = Class.forName(args[1]);
						if (Utils.includes(args, "=")) {
							// set field, example: /zombieescape debug xyz.acrylicstyle.zombieescape.ZombieEscape gameStarted = true
							if (args.length != (Utils.indexOf(args, "=")+2)) throw new IllegalArgumentException("Missing 1 argument after =");
							Field field = clazz.getField(args[2]);
							field.setAccessible(true);
							String s = args[Utils.indexOf(args, "=")+1];
							if (Utils.isInt(s)) {
								field.setInt(clazz, Integer.parseInt(s));
							} else if (Utils.isBoolean(s)) {
								field.setBoolean(clazz, Boolean.parseBoolean(s));
							} else if (Utils.isDouble(s)) {
								field.setDouble(clazz, Double.parseDouble(s));
							} else if (Utils.isFloat(s)) {
								field.setFloat(clazz, Float.parseFloat(s));
							} else {
								field.set(clazz, s);
							}
							sender.sendMessage(ChatColor.GREEN + "Field " + args[Utils.indexOf(args, "=")-1] + " has been set to:");
							sender.sendMessage("" + field.get(clazz));
						} else if (Utils.includes(args, "(") && Utils.includes(args, ")")) {
							// invoke method, example: /zombieescape debug xyz.acrylicstyle.zombieescape.utils.Utils isInt ( 123 )
							throw new Exception("Not implemented.");
						} else {
							// get field, example: /zombieescape debug xyz.acrylicstyle.zombieescape.ZombieEscape gameStarted
							Field field = clazz.getField(args[2]);
							field.setAccessible(true);
							sender.sendMessage(ChatColor.GREEN + "Result:");
							sender.sendMessage(ChatColor.GREEN + "" + field.get(clazz));
						}
					}
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "An unknown error occurred: " + e);
				}
			} else {
				if (!Utils.senderCheck(sender)) return true;
				Command target = Bukkit.getPluginCommand(args[0]);
				if (target == null) {
					sender.sendMessage(ChatColor.DARK_GRAY + "不明なコマンドです。");
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
