package xyz.acrylicstyle.zombieescape.commands;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.acrylicstyle.zombieescape.ZombieEscape;

public class ZombieEscapeGameUtil {
	public final class Suicide implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command must be run from in-game.");
				return true;
			}
			((Player) sender).setHealth(0.0);
			sender.sendMessage(ChatColor.DARK_RED + "残酷な世界よ、さようなら。");
			return true;
		}
	}

	public final class SetCheckpoint implements CommandExecutor { // /setcp from command block or something
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /setcp <0, 1, 2, 3, ...>");
				return true;
			}
			ZombieEscape.checkpoint = Integer.parseInt(args[0]);
			sender.sendMessage(ChatColor.GREEN + "チェックポイントを " + args[0] + " に設定しました。");
			return true;
		}
	}

	public final class StartGame implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (ZombieEscape.gameStarted) {
				sender.sendMessage(ChatColor.RED + "ゲームはすでに開始されています！");
				return true;
			}
			if (ZombieEscape.mininumPlayers > Bukkit.getOnlinePlayers().size()) {
				sender.sendMessage(ChatColor.RED + "プレイヤー数が最低人数に満たないため、開始できません。");
				return true;
			}
			ZombieEscape.timesLeft = 6;
			return true;
		}
	}

	public final class EndGame implements CommandExecutor {
		@SuppressWarnings("deprecation")
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!ZombieEscape.gameStarted) {
				sender.sendMessage(ChatColor.RED + "ゲームはまだ開始されていません！");
				return true;
			}
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
				player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "プレイヤーチームの勝ち！", "");
			}
			Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + "プレイヤーチームの勝ち！");
			Bukkit.broadcastMessage(ChatColor.GRAY + "このサーバーはあと15秒でシャットダウンします。");
			TimerTask task = new TimerTask() {
				public void run() {
					Bukkit.broadcastMessage(ChatColor.GRAY + "サーバーをシャットダウン中...");
					Bukkit.shutdown();
				}
			};
			Timer timer = new Timer();
			timer.schedule(task, 1000*15);
			return true;
		}
	}

	public final class CheckConfig implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			ZombieEscape.checkConfig();
			sender.sendMessage(ChatColor.GREEN + "設定を再確認しました。結果は " + ZombieEscape.settingsCheck + " です。");
			return true;
		}
	}
}
