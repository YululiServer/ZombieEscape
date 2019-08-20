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
import xyz.acrylicstyle.tomeito_core.providers.LanguageProvider;
import xyz.acrylicstyle.tomeito_core.utils.Lang;
import xyz.acrylicstyle.zombieescape.PlayerTeam;
import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.data.Constants;
import xyz.acrylicstyle.zombieescape.utils.Utils;

public class ZombieEscapeGameUtil {
	public final LanguageProvider lang;

	public ZombieEscapeGameUtil() {
		lang = ZombieEscape.lang;
	}

	public final class SetCheckpoint implements CommandExecutor { // /setcp from command block or something
		private Map<String, Integer> count = new HashMap<String, Integer>();

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setcp <0, 1, 2, 3, ...>");
				return true;
			}
			Player nearestPlayer = null;
			if (sender instanceof BlockCommandSender) {
				nearestPlayer = Utils.targetP(((BlockCommandSender)sender).getBlock().getLocation());
			} else if (sender instanceof Player) {
				nearestPlayer = Utils.targetP(((Player)sender).getLocation());
			} else { // it shouldn't happen
				sender.sendMessage(ChatColor.RED + "Unknown type: " + sender.toString() + ", Name: " + sender.getName());
				return false;
			}
			// op
			if (sender instanceof Player && ((Player)sender).isOp()) {
				if (sender instanceof BlockCommandSender) {
					if (ZombieEscape.zombieCheckpoint >= Integer.parseInt(args[0])) {
						return false;
					}
				}
				ZombieEscape.zombieCheckpoint = Integer.parseInt(args[0]);
				Bukkit.broadcastMessage(Lang.format(lang.get("passedCPZombie"), args[0]));
				return true;
			}
			// op end
			if (ZombieEscape.hashMapTeam.get(nearestPlayer.getUniqueId()) != PlayerTeam.ZOMBIE) {
				if (sender instanceof BlockCommandSender) {
					if (ZombieEscape.playerCheckpoint >= Integer.parseInt(args[0])) {
						return false;
					}
				}
				ZombieEscape.playerCheckpoint = Integer.parseInt(args[0]);
				Bukkit.broadcastMessage(Lang.format(lang.get("passedCPPlayer"), args[0]));
				if (!ZombieEscape.mapConfig.getBoolean("worldborder.enable", false)) return true;
				int delay = ZombieEscape.mapConfig.getInt("worldborder.cp" + args[0] + ".delay", 0);
				int seconds = ZombieEscape.mapConfig.getInt("worldborder.cp" + args[0] + ".seconds", 0);
				if (delay > 0) {
					count.put("stormcoming" + args[0], delay);
					new BukkitRunnable() {
						public void run() {
							ZombieEscape.ongoingEventMap.put("stormcoming" + args[0],  Lang.format(lang.get("stormComing"), count.get("stormcoming" + args[0]).toString()));
							if (count.get("stormcoming" + args[0]) <= 0) {
								ZombieEscape.ongoingEventMap.remove("stormcoming" + args[0]);
								this.cancel();
								return;
							}
							count.put("stormcoming" + args[0], count.get("stormcoming" + args[0])-1);
						}
					}.runTaskTimer(ZombieEscape.getProvidingPlugin(ZombieEscape.class), 0, 20);
					Utils.doBossBarTick(Bukkit.createBossBar(args[0], BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.DARKEN_SKY), delay, "stormcoming" + args[0], true);
				}
				new BukkitRunnable() {
					public void run() {
						count.put("stormmoving" + args[0], seconds);
						Bukkit.getWorld(ZombieEscape.mapConfig.getString("spawnPoints.world", "world")).getWorldBorder().setSize(ZombieEscape.mapConfig.getInt("worldborder.cp" + args[0] + ".blocks"), ZombieEscape.mapConfig.getInt("worldborder.cp" + args[0] + ".seconds"));
						new BukkitRunnable() {
							public void run() {
								ZombieEscape.ongoingEventMap.put("stormmoving" + args[0],  Lang.format(lang.get("stormMoving"), count.get("stormmoving" + args[0]).toString()));
								if (count.get("stormmoving" + args[0]) <= 0) {
									ZombieEscape.ongoingEventMap.remove("stormmoving" + args[0]);
									this.cancel();
									return;
								}
								count.put("stormmoving" + args[0], count.get("stormmoving" + args[0])-1);
							}
						}.runTaskTimer(ZombieEscape.getProvidingPlugin(ZombieEscape.class), 0, 20);
						Utils.doBossBarTick(Bukkit.createBossBar(args[0], BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.DARKEN_SKY), seconds, "stormmoving" + args[0]);
					}
				}.runTaskLater(ZombieEscape.getProvidingPlugin(ZombieEscape.class), delay <= 0 ? 1 : delay*20);
				return true;
			}
			if (sender instanceof BlockCommandSender) {
				if (ZombieEscape.zombieCheckpoint >= Integer.parseInt(args[0])) {
					return false;
				}
			}
			ZombieEscape.zombieCheckpoint = Integer.parseInt(args[0]);
			Bukkit.broadcastMessage(Lang.format(lang.get("passedCPZombie"), args[0]));
			return true;
		}
	}

	public final class StartGame implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (ZombieEscape.gameStarted) {
				sender.sendMessage(lang.get("alreadyStarted"));
				return true;
			}
			if (Constants.mininumPlayers > Bukkit.getOnlinePlayers().size()) {
				sender.sendMessage(lang.get("cantStart"));
				return true;
			}
			ZombieEscape.timesLeft = 11;
			return true;
		}
	}

	public final class VoteGui implements CommandExecutor, InventoryHolder, Listener {
		private boolean init = false;
		private Inventory inventory;

		public void initialize() {
			this.inventory = Utils.initializeItems(Bukkit.createInventory(this, 27, lang.get("mapVote")));
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
			if (!e.getView().getTopInventory().getTitle().equalsIgnoreCase(lang.get("mapVote"))) return;
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
				sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /vote <Map>");
				return true;
			}
			if (ZombieEscape.gameStarted) {
				sender.sendMessage(lang.get("alreadyStarted"));
				return true;
			}
			File maps = new File("./plugins/ZombieEscape/maps/");
			List<String> files = new ArrayList<String>();
			for (File file : maps.listFiles()) files.add(file.getName().replaceAll(".yml", ""));
			if (!files.contains(args[0])) {
				sender.sendMessage(lang.get("nomap"));
				return true;
			}
			ConfigProvider mapConfig = null;
			try {
				mapConfig = new ConfigProvider("./plugins/ZombieEscape/maps/" + args[0] + ".yml");
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			if (Bukkit.getWorld(mapConfig.getString("spawnPoints.world", "world")) == null) {
				sender.sendMessage(lang.get("nonExistWorld"));
				return true;
			}
			if (!(mapConfig.getList("spawnPoints.zombie") != null // check if zombie spawn points exists
					&& mapConfig.getList("spawnPoints.zombie").size() != 0 // check if zombie spawn points are *actually* exists(0 isn't exist)
					&& mapConfig.getList("spawnPoints.player") != null // check if player spawn points exists
					&& mapConfig.getList("spawnPoints.player").size() != 0)) {
				sender.sendMessage(ChatColor.RED + "指定したマップにはスポーン地点が設定されていません。");
				return true;
			}
			ZombieEscape.hashMapVote.put(ps.getUniqueId(), args[0]);
			sender.sendMessage(Lang.format(lang.get("votedTo"), mapConfig.getString("mapname")));
			return true;
		}
	}

	public final class DestroyWall implements CommandExecutor {
		private Map<String, Integer> count = new HashMap<String, Integer>();

		@Override
		public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /destroywall <ID of wall> <Time until broke a wall (seconds)>");
				return true;
			}
			if (sender instanceof BlockCommandSender) {
				((BlockCommandSender)sender).getBlock().setType(Material.AIR);
			}
			int countdown = 0;
			try {
				countdown = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				sender.sendMessage(lang.get("timeMustNumber"));
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
					sender.sendMessage(lang.get("brokenWall"));
				}
			}.runTaskLater(ZombieEscape.getProvidingPlugin(ZombieEscape.class), 20*countdown);
			count.put(args[0], countdown);
			new BukkitRunnable() {
				public void run() {
					if (ZombieEscape.config.getString("language", "en_US").equalsIgnoreCase("en_US")) {
						ZombieEscape.ongoingEventMap.put(args[0],  Lang.format(lang.get("brokeWallIn"), args[0], count.get(args[0]).toString()));
					} else {
						ZombieEscape.ongoingEventMap.put(args[0],  Lang.format(lang.get("brokeWallIn"), count.get(args[0]).toString(), args[0]));
					}
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
				sender.sendMessage(lang.get("sendingResourcePack"));
				((Player) sender).setResourcePack(ZombieEscape.config.getString("resourcepack"));
			} else {
				sender.sendMessage(lang.get("resourcePackIsntSet"));
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
			} catch (Exception e) { // it shouldn't happen
				Log.error("Error while getting player's ping:");
				e.printStackTrace();
				e.getCause().printStackTrace();
				sender.sendMessage(ChatColor.RED + "An unknown error occurred while getting your ping");
			}
			return true;
		}
	}
}
