package xyz.acrylicstyle.zombieescape.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.zombieescape.PlayerTeam;
import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.data.Constants;
import xyz.acrylicstyle.zombieescape.utils.Utils;

public class ZombieEscapeGameUtil {
	public final class SetCheckpoint implements CommandExecutor { // /setcp from command block or something
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /setcp <0, 1, 2, 3, ...>");
				return true;
			}
			Player nearestPlayer = null;
			if (sender instanceof BlockCommandSender) {
				nearestPlayer = Utils.targetP(((BlockCommandSender)sender).getBlock().getLocation());
			} else if (sender instanceof Player) {
				nearestPlayer = Utils.targetP(((Player)sender).getLocation());
			} else {
				sender.sendMessage(ChatColor.RED + "不明なタイプです: " + sender.toString() + ", Name: " + sender.getName());
				return false;
			}
			// op
			if (sender instanceof Player && ((Player)sender).isOp()) {
				if (sender instanceof BlockCommandSender) {
					if (ZombieEscape.zombieCheckpoint >= Integer.parseInt(args[0])) {
						sender.sendMessage(ChatColor.RED + "そのゾンビチェックポイントはすでに通過しています。");
						return false;
					}
				}
				ZombieEscape.zombieCheckpoint = Integer.parseInt(args[0]);
				sender.sendMessage(ChatColor.GREEN + "ゾンビチェックポイントを " + args[0] + " に設定しました。");
				Bukkit.broadcastMessage(ChatColor.GREEN + "ゾンビチェックポイント" + args[0] + "を通過しました。");
				return true;
			}
			// op end
			if (ZombieEscape.hashMapTeam.get(nearestPlayer.getUniqueId()) != PlayerTeam.ZOMBIE) {
				if (sender instanceof BlockCommandSender) {
					if (ZombieEscape.playerCheckpoint >= Integer.parseInt(args[0])) {
						sender.sendMessage(ChatColor.RED + "そのプレイヤーチェックポイントはすでに通過しています。");
						return false;
					}
				}
				ZombieEscape.playerCheckpoint = Integer.parseInt(args[0]);
				sender.sendMessage(ChatColor.GREEN + "プレイヤーチェックポイントを " + args[0] + " に設定しました。");
				Bukkit.broadcastMessage(ChatColor.GREEN + "プレイヤーがチェックポイント" + args[0] + "を通過しました。");
				return true;
			}
			if (sender instanceof BlockCommandSender) {
				if (ZombieEscape.zombieCheckpoint >= Integer.parseInt(args[0])) {
					sender.sendMessage(ChatColor.RED + "そのゾンビチェックポイントはすでに通過しています。");
					return false;
				}
			}
			ZombieEscape.zombieCheckpoint = Integer.parseInt(args[0]);
			sender.sendMessage(ChatColor.GREEN + "ゾンビチェックポイントを " + args[0] + " に設定しました。");
			Bukkit.broadcastMessage(ChatColor.GREEN + "ゾンビがチェックポイント" + args[0] + "を通過しました。");
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
			if (Constants.mininumPlayers > Bukkit.getOnlinePlayers().size()) {
				sender.sendMessage(ChatColor.RED + "プレイヤー数が最低人数に満たないため、開始できません。");
				return true;
			}
			ZombieEscape.timesLeft = 11;
			return true;
		}
	}

	public final class CheckConfig implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			Utils.checkConfig();
			sender.sendMessage(ChatColor.GREEN + "設定を再確認しました。結果は " + ZombieEscape.settingsCheck + " です。");
			return true;
		}
	}

	public final class SetStatus implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "引数が2つ必要です。");
				sender.sendMessage(ChatColor.GRAY + "引数<type> [gameEnded<Boolean>, gameStarted<Boolean>, gameTime<Integer>, playedTime<Integer>, timesLeft<Integer>, debug<Boolean>]");
				return true;
			}
			if (args[0].equalsIgnoreCase("gameEnded")) {
				ZombieEscape.gameEnded = Boolean.getBoolean(args[1]);
			} else if (args[0].equalsIgnoreCase("gameStarted")) {
				ZombieEscape.gameStarted = Boolean.getBoolean(args[1]);
			} else if (args[0].equalsIgnoreCase("gameTime")) {
				ZombieEscape.gameTime = Integer.parseInt(args[1]);
			} else if (args[0].equalsIgnoreCase("playedTime")) {
				ZombieEscape.playedTime = Integer.parseInt(args[1]);
			} else if (args[0].equalsIgnoreCase("timesLeft")) {
				ZombieEscape.timesLeft = Integer.parseInt(args[1]);
			} else if (args[0].equalsIgnoreCase("debug")) {
				ZombieEscape.debug = Boolean.parseBoolean(args[1]);
			} else if (args[0].equalsIgnoreCase("hashMapVote")) {
				ZombieEscape.hashMapVote = null;
				args[1] = "null";
			} else {
				sender.sendMessage(ChatColor.GRAY + "引数<type> [gameEnded<Boolean>, gameStarted<Boolean>, gameTime<Integer>, playedTime<Integer>, timesLeft<Integer>, debug<Boolean>]");
				return true;
			}
			sender.sendMessage(ChatColor.GREEN + args[0] + "を" + args[1] + "に設定しました。");
			return true;
		}
	}

	public final class VoteGui implements CommandExecutor, InventoryHolder, Listener {
		private boolean init = false;
		private Inventory inventory;

		public void initialize() {
			this.inventory = Utils.initializeItems(Bukkit.createInventory(this, 27, "投票"));
			this.init = true;
		}

		@Override
		public Inventory getInventory() {
			return this.inventory;
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!Utils.senderCheck(sender)) return true;
			if (!this.init) this.initialize();
			Player ps = (Player) sender;
			ps.openInventory(inventory);
			return true;
		}

		@EventHandler
		public void onInventoryClick(InventoryClickEvent e) {
			if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
			if (!e.getView().getTopInventory().getTitle().equalsIgnoreCase("投票")) return;
			Bukkit.dispatchCommand(((Player)e.getWhoClicked()), "vote " + e.getCurrentItem().getItemMeta().getLore().get(0));
			e.setCancelled(true);
			e.getWhoClicked().closeInventory();
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent e) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.EMPTY_MAP) {
					e.getPlayer().openInventory(inventory);
					e.setCancelled(true);
					e.getPlayer().getInventory().clear();
					e.getPlayer().getInventory().addItem(Utils.generateVoteItem());
					e.getPlayer().getInventory().addItem(Utils.generateResourcePackItem());
				} else if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.CHEST) {
					Bukkit.dispatchCommand(e.getPlayer(), "resourcepack");
					e.setCancelled(true);
					e.getPlayer().getInventory().clear();
					e.getPlayer().getInventory().addItem(Utils.generateVoteItem());
					e.getPlayer().getInventory().addItem(Utils.generateResourcePackItem());
				}
			}
		}
	}

	public final class Vote implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!Utils.senderCheck(sender)) return true;
			Player ps = (Player) sender;
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使用法: /vote <マップ名>");
				return true;
			}
			if (ZombieEscape.gameStarted) {
				sender.sendMessage(ChatColor.RED + "ゲームはすでに開始されています！");
				return true;
			}
			File maps = new File("./plugins/ZombieEscape/maps/");
			List<String> files = new ArrayList<String>();
			for (File file : maps.listFiles()) files.add(file.getName().replaceAll(".yml", ""));
			if (!files.contains(args[0])) {
				sender.sendMessage(ChatColor.RED + "指定されたマップは存在しません。");
				return true;
			}
			ConfigProvider mapConfig = null;
			try {
				mapConfig = new ConfigProvider("./plugins/ZombieEscape/maps/" + args[0] + ".yml");
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			if (Bukkit.getWorld(mapConfig.getString("spawnPoints.world", "world")) == null) {
				sender.sendMessage(ChatColor.RED + "指定されたマップのワールドはこのサーバーには存在しません。");
				return true;
			}
			ZombieEscape.hashMapVote.put(ps.getUniqueId(), args[0]);
			sender.sendMessage(ChatColor.GREEN + mapConfig.getString("mapname") + " に投票しました。");
			return true;
		}
	}

	public final class DestroyWall implements CommandExecutor {
		private Map<String, Integer> count = new HashMap<String, Integer>();

		@Override
		public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "使用法: /destroywall <壁のID> <壁破壊までの時間(秒)>");
				return true;
			}
			if (sender instanceof BlockCommandSender) {
				((BlockCommandSender)sender).getBlock().setType(Material.AIR);
			}
			int countdown = 0;
			try {
				countdown = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "時間は数値にしてください。");
				return true;
			}
			new BukkitRunnable() {
				public void run() {
					ZombieEscape.mapConfig.getStringList("wallLocation." + args[0]).forEach(blocation -> {
						String[] blocationArray = blocation.split(",");
						Block ablock = Bukkit.getWorld(ZombieEscape.mapConfig.getString("spawnPoints.world", "world")).getBlockAt(Integer.parseInt(blocationArray[0]), Integer.parseInt(blocationArray[1]), Integer.parseInt(blocationArray[2]));
						ablock.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, ablock.getLocation(), 2);
						ablock.setType(Material.AIR);
						ablock.getWorld().playSound(ablock.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5, 1);
					});
					sender.sendMessage(ChatColor.GREEN + "壁を破壊しました。");
				}
			}.runTaskLater(ZombieEscape.getProvidingPlugin(ZombieEscape.class), 20*countdown);
			count.put(args[0], countdown);
			new BukkitRunnable() {
				public void run() {
					ZombieEscape.ongoingEventMap.put(args[0], "あと" + count.get(args[0]) + "秒で壁(" + args[0] + ")破壊");
					if (count.get(args[0]) <= 0) {
						ZombieEscape.ongoingEventMap.remove(args[0]);
						this.cancel();
						return;
					}
					count.put(args[0], count.get(args[0])-1);
				}
			}.runTaskTimer(ZombieEscape.getProvidingPlugin(ZombieEscape.class), 0, 20);
			Utils.doBossBarTick(Bukkit.createBossBar(args[0], BarColor.GREEN, BarStyle.SOLID, BarFlag.DARKEN_SKY), countdown, args[0]);
			return true;
		}
	}

	public final class ResourcePack implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!Utils.senderCheck(sender)) return true;
			if (ZombieEscape.config.getString("resourcepack") != null) {
				sender.sendMessage(ChatColor.GREEN + "リソースパックを送信中です...");
				((Player) sender).setResourcePack(ZombieEscape.config.getString("resourcepack"));
			} else {
				sender.sendMessage(ChatColor.RED + "このサーバーにはリソースパックが設定されていません。");
			}
			return true;
		}
	}

	public final class Ping implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (!Utils.senderCheck(sender)) return true;
			Player player = (Player) sender;
			try {
				Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
				int ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
				String pingmsg = "";
				if (ping <= 100) {
					pingmsg = "" + ChatColor.GREEN + ping;
				} else if (ping <= 300) {
					pingmsg = "" + ChatColor.GOLD + ping;
				} else if (ping <= 500) {
					pingmsg = "" + ChatColor.RED + ping;
				} else {
					pingmsg = "" + ChatColor.DARK_RED + ping;
				}
				sender.sendMessage(ChatColor.GREEN + "Ping: " + pingmsg + "ms");
			} catch (Exception e) {
				Log.error("Error while getting player's ping:");
				e.printStackTrace();
				e.getCause().printStackTrace();
				sender.sendMessage(ChatColor.RED + "Pingの取得中に不明なエラーが発生しました");
			}
			return true;
		}
	}
}
