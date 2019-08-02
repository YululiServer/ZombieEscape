// *Warning* Bugged string may be found while decompiling this source!
package xyz.acrylicstyle.zombieescape;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.zombieescape.commands.Sponsor;
import xyz.acrylicstyle.zombieescape.commands.ZombieEscapeConfig;
import xyz.acrylicstyle.zombieescape.commands.ZombieEscapeGameUtil;
import xyz.acrylicstyle.zombieescape.data.Constants;
import xyz.acrylicstyle.zombieescape.effects.ActionBar;

public class ZombieEscape extends JavaPlugin implements Listener {
	public final static int mininumPlayers = 2;
	public static ConfigProvider config = null;
	public static HashMap<UUID, Scoreboard> hashMapScoreboard = new HashMap<UUID, Scoreboard>();
	public static HashMap<UUID, String> hashMapTeam = new HashMap<UUID, String>();
	public static HashMap<UUID, String> hashMapLastScore4 = new HashMap<UUID, String>();
	public static HashMap<UUID, String> hashMapLastScore8 = new HashMap<UUID, String>();
	public static HashMap<String, Integer> hashMapBlockState = new HashMap<String, Integer>();
	/**
	 * Not in use.
	 */
	public static HashMap<UUID, Boolean> hashMapOriginZombie = new HashMap<UUID, Boolean>();
	public static HashMap<UUID, Boolean> lockActionBar = new HashMap<UUID, Boolean>();
	public static ScoreboardManager manager = null;
	public static ProtocolManager protocol = null;
	public static int zombies = 0;
	public static int players = 0;
	public static int timesLeft = 180;
	public static boolean timerStarted = false;
	public static boolean hasEnoughPlayers = false;
	public static boolean settingsCheck = false;
	public static HashMap<String, Team> teams = new HashMap<String, Team>();
	public static boolean gameStarted = false;
	public static int gameTime = 1800; // 30 minutes
	public static int playedTime = 0;
	public static int checkpoint = 0;
	public static int fireworked = 0;
	public static boolean gameEnded = false;
	public static Map<String, Object> locationWall = null;

	@Override
	public void onEnable() {
		protocol = ProtocolLibrary.getProtocolManager();
		try {
			config = new ConfigProvider("./plugins/ZombieEscape/config.yml");
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
			Bukkit.getLogger().severe("[ZombieEscape] Failed to load config, disabling plugin.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		manager = Bukkit.getScoreboardManager();
		Sponsor sponsor = null;
		ZombieEscapeConfig zec = null;
		ZombieEscapeGameUtil zegu = null;
		try {
			sponsor = new Sponsor();
			zec = new ZombieEscapeConfig();
			zegu = new ZombieEscapeGameUtil();
		} catch (Exception e) {
			e.printStackTrace();
			e.getCause().printStackTrace();
		}
		if (sponsor != null && zec != null && zegu != null) {
			Bukkit.getPluginCommand("setsponsor").setExecutor(sponsor.new SetSponsor());
			Bukkit.getPluginCommand("removesponsor").setExecutor(sponsor.new RemoveSponsor());
			Bukkit.getPluginCommand("setspawn").setExecutor(zec.new SetSpawn());
			Bukkit.getPluginCommand("removespawn").setExecutor(zec.new RemoveSpawn());
			Bukkit.getPluginCommand("addwall").setExecutor(zec.new AddWall());
			Bukkit.getPluginCommand("deletewall").setExecutor(zec.new DeleteWall());
			Bukkit.getPluginCommand("suicide").setExecutor(zegu.new Suicide());
			Bukkit.getPluginCommand("setcp").setExecutor(zegu.new SetCheckpoint());
			Bukkit.getPluginCommand("startgame").setExecutor(zegu.new StartGame());
			Bukkit.getPluginCommand("endgame").setExecutor(zegu.new EndGame());
			Bukkit.getPluginCommand("check").setExecutor(zegu.new CheckConfig());
			Bukkit.getPluginCommand("setstatus").setExecutor(zegu.new SetStatus());
			Bukkit.getPluginCommand("zombieescape").setExecutor(new CommandExecutor() {
				@Override
				public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "This command must be run from in-game.");
						return true;
					}
					((Player) sender).performCommand("bukkit:help ZombieEscape");
					return true;
				}
			});
		} else {
			Bukkit.getLogger().severe("[ZombieEscape] Unable to register commands! Commands are disabled.");
		}
		teams.put("zombie", manager.getNewScoreboard().registerNewTeam("zombie"));
		teams.put("player", manager.getNewScoreboard().registerNewTeam("player"));
		Bukkit.getPluginManager().registerEvents(this, this);
		checkConfig();
		locationWall = ConfigProvider.getConfigSectionValue(config.get("locationWall", new HashMap<String, Object>()), true);
		Bukkit.getLogger().info("[ZombieEscape] Enabled Zombie Escape");
	}

	/**
	 * Reload config and check if all configs are set correctly.<br>
	 * If all checks passed, settingsCheck will be true. Otherwise it'll set to false.
	 */
	public static void checkConfig() {
		config.reloadWithoutException();
		if (config.getList("spawnPoints.zombie") != null // check if zombie spawn points exists
				&& config.getList("spawnPoints.zombie").size() != 0 // check if zombie spawn points are *actually* exists(0 isn't exist)
				&& config.getList("spawnPoints.player") != null // check if player spawn points exists
				&& config.getList("spawnPoints.player").size() != 0
				&& config.getString("spawnPoints.world") != null // check if spawn world is set
				&& Bukkit.getWorld(config.getString("spawnPoints.world")) != null
				&& config.get("locationWall") != null) settingsCheck = true; // if it's null, ProjectileHitEvent won't work!
		else settingsCheck = false;
	}

	public ZombieEscape getInstance() {
		return this;
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
		hashMapTeam.put(event.getPlayer().getUniqueId(), "player");
		lockActionBar.put(event.getPlayer().getUniqueId(), false);
		new BukkitRunnable() {
			public void run() {
				ZombieEscape.config.reloadWithoutException();
				locationWall = ConfigProvider.getConfigSectionValue(config.get("locationWall", new HashMap<String, Object>()), true);
			}
		}.runTaskTimer(this, 6000, 6000);
		/*
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					Set<Material> set = new HashSet<Material>();
					set.add(Material.AIR);
					Block block = player.getTargetBlock(set, 4);
					if (block == null) {
						lockActionBar.put(player.getUniqueId(), false);
						continue;
					}
					String location = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
					Map<String, Object> locationWall = Config.getConfigSectionValue(config2.get("locationWall", new HashMap<String, Object>()), true);
					String wall = (String) locationWall.getOrDefault(location, null);
					if (wall == null) {
						lockActionBar.put(player.getUniqueId(), false);
						continue;
					}
					Integer state = hashMapBlockState.get(wall) != null ? hashMapBlockState.get(wall) : 0;
					ActionBar.setActionBarWithoutException(player, ChatColor.GREEN + "壁の耐久力: " + state + "/" + Constants.materialDurability.get(block.getType()));
					lockActionBar.put(player.getUniqueId(), true);
				}
			}
		}.runTaskTimer(this, 0, 20);
		*/
		BukkitRunnable healthBar = new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (lockActionBar.getOrDefault(player.getUniqueId(), false)) continue;
					int maxHealth = (int) player.getMaxHealth();
					int health = (int) player.getHealth();
					ActionBar.setActionBarWithoutException(player, "" + ChatColor.RED + health + "/" + maxHealth + "❤");
				}
			}
		};
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.isDead()) player.spigot().respawn();
					player.addPotionEffect(PotionEffectType.SATURATION.createEffect(100000, 1));
					if (hashMapTeam.get(player.getUniqueId()) == "zombie") {
						player.addPotionEffect(PotionEffectType.SPEED.createEffect(100000, 1));
						player.addPotionEffect(PotionEffectType.JUMP.createEffect(100000, 0));
						player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(100000, 100));
						player.getInventory().setHelmet(createLeatherItemStack(Material.LEATHER_HELMET, 0, 100, 0));
						player.getInventory().setChestplate(createLeatherItemStack(Material.LEATHER_CHESTPLATE, 0, 100, 0));
						player.getInventory().setLeggings(createLeatherItemStack(Material.LEATHER_LEGGINGS, 0, 100, 0));
						player.getInventory().setBoots(createLeatherItemStack(Material.LEATHER_BOOTS, 0, 100, 0));
					} else {
						player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
						player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
						player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
						player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
					}
				}
			}
		}.runTaskTimer(this, 200, 200);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + event.getPlayer().getName() + " clear");
		hashMapOriginZombie.put(event.getPlayer().getUniqueId(), false);
		if (!settingsCheck) {
			event.getPlayer().sendMessage(ChatColor.RED + "設定がまだ完了してない/エラーが発生したため、ゲームを開始できません。");
			event.getPlayer().sendMessage(ChatColor.RED + "/setspawn、/removespawn、/addwall、/deletewallで設定してください。");
		}
		hashMapLastScore4.put(event.getPlayer().getUniqueId(), "");
		hashMapLastScore8.put(event.getPlayer().getUniqueId(), "");
		event.getPlayer().getWorld().setGameRuleValue("doMobLoot", "false");
		event.getPlayer().getWorld().setGameRuleValue("doDaylightCycle", "false");
		event.getPlayer().getWorld().setGameRuleValue("keepInventory", "true");
		event.getPlayer().getWorld().setGameRuleValue("doFireTick", "false");
		event.getPlayer().getWorld().setTime(0);
		event.getPlayer().getInventory().clear();
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + event.getPlayer().getName() + " minecraft:saturation 100000 1");
		final Scoreboard board = manager.getNewScoreboard();
		event.getPlayer().setScoreboard(board);
		final Objective objective = board.registerNewObjective("scoreboard", "dummy");
		Score score7 = objective.getScore(" ");
		score7.setScore(7);
		Score score5 = objective.getScore("  ");
		score5.setScore(5);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(""+ChatColor.GREEN + ChatColor.BOLD + "Zombie Escape");
		if (Bukkit.getOnlinePlayers().size() >= 2) hasEnoughPlayers = true; else {
			hasEnoughPlayers = false;
			timesLeft = 180;
		}
		hashMapScoreboard.put(event.getPlayer().getUniqueId(), board);
		event.getPlayer().setMaxHealth(100);
		event.getPlayer().setHealth(100);
		event.getPlayer().setHealthScale(20);
		new BukkitRunnable() {
			public void run() {
				event.getPlayer().sendMessage(ChatColor.BLUE + "--------------------------------------------------");
				event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "          - Zombie Escape -");
				event.getPlayer().sendMessage("");
				event.getPlayer().sendMessage(ChatColor.YELLOW + "プレイヤー: ゾンビから逃げ、ゾンビよりも先にゴールに到達する");
				event.getPlayer().sendMessage(ChatColor.YELLOW + "ゾンビ: プレイヤーを全員倒すか先にゴールに到達する");
				event.getPlayer().sendMessage("");
				event.getPlayer().sendMessage(ChatColor.YELLOW + "チームはゲーム開始5秒前に決定されます。");
				event.getPlayer().sendMessage(ChatColor.YELLOW + "チャットはデフォルトで" + ChatColor.AQUA + "[チーム]" + ChatColor.YELLOW + "チャットです。");
				event.getPlayer().sendMessage(ChatColor.YELLOW + "'!'をチャットの先頭につけると" + ChatColor.RED + "[All]" + ChatColor.YELLOW + "(全体)チャットになります。");
				event.getPlayer().sendMessage(ChatColor.BLUE + "--------------------------------------------------");
				if (gameStarted) {
					event.getPlayer().sendMessage(ChatColor.RED + "ゲームはすでに開始しています！");
					event.getPlayer().setGameMode(GameMode.SPECTATOR);
					event.getPlayer().setPlayerListName(ChatColor.WHITE + event.getPlayer().getName());
					return;
				}
			}
		}.runTaskLater(this, 40);
		healthBar.runTaskTimer(this, 0, 20);
		if (timerStarted) return;
		timerStarted = true;
		new BukkitRunnable() {
			@SuppressWarnings("deprecation") // player#sendTitle, i can't find non-deprecated methods in 1.8.8.
			public void run() {
				for (final Player player : Bukkit.getOnlinePlayers()) {
					hashMapScoreboard.get(player.getUniqueId()).resetScores(hashMapLastScore4.get(player.getUniqueId()));
				}
				final String zombieMessage = ChatColor.GREEN + "    チーム: " + ChatColor.DARK_GREEN + "ゾンビ";
				final String playerMessage = ChatColor.GREEN + "    チーム: " + ChatColor.AQUA + "プレイヤー";
				if (!gameStarted) {
					for (final Player player : Bukkit.getOnlinePlayers()) {
						Scoreboard scoreboard = hashMapScoreboard.get(player.getUniqueId());
						Objective objective3 = scoreboard.getObjective(DisplaySlot.SIDEBAR);
						String lastScore8 = hashMapLastScore8.get(player.getUniqueId());
						scoreboard.resetScores(lastScore8);
						lastScore8 = ChatColor.GREEN + "    プレイヤー: " + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers();
						Score score8 = objective3.getScore(lastScore8);
						score8.setScore(8);
						String leftSecond = Integer.toString(timesLeft % 60);
						if (leftSecond.length() == 1) leftSecond = "0" + leftSecond;
						String lastScore4 = hashMapLastScore4.get(player.getUniqueId());
						scoreboard.resetScores(lastScore4);
						if (hasEnoughPlayers && settingsCheck)
							lastScore4 = ChatColor.GREEN + "    あと" + Math.round(Math.nextDown(timesLeft/60)) + ":" + leftSecond + "で開始";
						else
							lastScore4 = ChatColor.WHITE + "    待機中...";
						hashMapLastScore8.put(player.getUniqueId(), lastScore8);
						hashMapLastScore4.put(player.getUniqueId(), lastScore4);
						Score score4 = objective3.getScore(lastScore4);
						score4.setScore(4);
						scoreboard.resetScores(zombieMessage);
						scoreboard.resetScores(playerMessage);
						if (hashMapTeam.get(player.getUniqueId()) == "zombie") {
							Score score6 = objective3.getScore(zombieMessage);
							score6.setScore(6);
						} else if (hashMapTeam.get(player.getUniqueId()) == "player") {
							Score score6 = objective3.getScore(playerMessage);
							score6.setScore(6);
						}
						player.setScoreboard(hashMapScoreboard.get(player.getUniqueId()));
						if (timesLeft == 5) {
							board.resetScores(playerMessage);
							if ((((int) Math.round(Bukkit.getOnlinePlayers().size() / 10) - zombies) >= 0) == true) {
								hashMapOriginZombie.put(player.getUniqueId(), true);
								hashMapTeam.put(player.getUniqueId(), "zombie");
								zombies = zombies+1;
								teams.get(hashMapTeam.get(player.getUniqueId())).setAllowFriendlyFire(false);
								teams.get(hashMapTeam.get(player.getUniqueId())).addEntry(player.getName());
								Score score6 = objective.getScore(ChatColor.GREEN + "    チーム: " + ChatColor.DARK_GREEN + "ゾンビ");
								score6.setScore(6);
								player.setMaxHealth(200);
								player.setHealth(200);
								player.setHealthScale(40);
								player.getInventory().setHelmet(createLeatherItemStack(Material.LEATHER_HELMET, 0, 100, 0));
								player.getInventory().setChestplate(createLeatherItemStack(Material.LEATHER_CHESTPLATE, 0, 100, 0));
								player.getInventory().setLeggings(createLeatherItemStack(Material.LEATHER_LEGGINGS, 0, 100, 0));
								player.getInventory().setBoots(createLeatherItemStack(Material.LEATHER_BOOTS, 0, 100, 0));
								player.addPotionEffect(PotionEffectType.SPEED.createEffect(100000, 1));
								player.addPotionEffect(PotionEffectType.JUMP.createEffect(100000, 0));
								player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(100000, 100));
								player.setPlayerListName(ChatColor.DARK_GREEN + player.getName());
							} else if ((((int) Math.round(Bukkit.getOnlinePlayers().size() / 10) - zombies) >= 0) == false) {
								players = players+1;
								hashMapTeam.put(player.getUniqueId(), "player");
								teams.get(hashMapTeam.get(player.getUniqueId())).setAllowFriendlyFire(false);
								teams.get(hashMapTeam.get(player.getUniqueId())).addEntry(player.getName());
								player.setMaxHealth(1);
								player.setHealth(1);
								player.setHealthScale(1);
								player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
								player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
								player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
								player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
								player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
								Score score6 = objective.getScore(ChatColor.GREEN + "    チーム: " + ChatColor.AQUA + "プレイヤー");
								score6.setScore(6);
								player.setMaxHealth(1);
								player.setHealth(1);
								player.setPlayerListName(ChatColor.AQUA + player.getName());
							}
							player.playSound(player.getLocation(), Sound.NOTE_STICKS, 100, 1);
							player.sendTitle(ChatColor.GREEN + "5", ChatColor.YELLOW + "チーム: " + hashMapTeam.get(player.getUniqueId()));
						} else if (timesLeft == 4) {
							player.playSound(player.getLocation(), Sound.NOTE_STICKS, 100, 1);
							player.sendTitle(ChatColor.AQUA + "4", ChatColor.YELLOW + "チーム: " + hashMapTeam.get(player.getUniqueId()));
						} else if (timesLeft == 3) {
							player.playSound(player.getLocation(), Sound.NOTE_STICKS, 100, 1);
							player.sendTitle(ChatColor.BLUE + "3", ChatColor.YELLOW + "チーム: " + hashMapTeam.get(player.getUniqueId()));
						} else if (timesLeft == 2) {
							player.playSound(player.getLocation(), Sound.NOTE_STICKS, 100, 1);
							player.sendTitle(ChatColor.YELLOW + "2", ChatColor.YELLOW + "チーム: " + hashMapTeam.get(player.getUniqueId()));
						} else if (timesLeft == 1) {
							player.playSound(player.getLocation(), Sound.NOTE_STICKS, 100, 1);
							player.sendTitle(ChatColor.RED + "1", ChatColor.YELLOW + "チーム: " + hashMapTeam.get(player.getUniqueId()));
						} else if (timesLeft == 0) {
							player.setGameMode(GameMode.ADVENTURE);
							gameStarted = true;
							player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 100F, 1F);
							if (hashMapTeam.get(player.getUniqueId()) == "zombie") {
								player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "GO!", ChatColor.YELLOW + "目標: プレイヤーを全員倒すか先にゴールに到達する");
								player.sendMessage(ChatColor.GRAY + "あと10秒後にワープします...");
								new BukkitRunnable() {
									@Override
									public void run() {
										player.setGameMode(GameMode.ADVENTURE);
										player.addPotionEffect(PotionEffectType.SPEED.createEffect(100000, 0));
										ItemStack item = new ItemStack(Material.IRON_SWORD);
										ItemMeta meta = item.getItemMeta();
										meta.setDisplayName("" + ChatColor.RESET + ChatColor.WHITE + "ナイフ");
										item.setItemMeta(meta);
										item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
										item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 100);
										player.getInventory().setItem(0, item);
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + player.getName() + " minecraft:stone_axe 1 0 {CanDestroy:[\"minecraft:grass\",\"minecraft:planks\",\"minecraft:dirt\"],HideFlags:1,Unbreakable:1,display:{Name:\"錆びついた斧\"},ench:[{id:32,lvl:10}]}");
										String[] spawnLists = Arrays.asList(config.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])).get(0).split(",");
										Location location = new Location(Bukkit.getWorld(config.getString("spawnPoints.world")), Double.parseDouble(spawnLists[0]), Double.parseDouble(spawnLists[1]), Double.parseDouble(spawnLists[2]));
										if (!player.teleport(location)) {
											player.sendMessage(ChatColor.RED + "ワープに失敗しました。");
											return;
										}
									}
								}.runTaskLater(getInstance(), 20*10);
							} else if (hashMapTeam.get(player.getUniqueId()) == "player") {
								new BukkitRunnable() {
									public void run() {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shot give " + player.getName() + " ak-47");
										player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "GO!", ChatColor.YELLOW + "目標: ゾンビから逃げ、ゾンビよりも先にゴールに到達する");
										String[] spawnLists = Arrays.asList(config.getList("spawnPoints.player", new ArrayList<String>()).toArray(new String[0])).get(0).split(",");
										Location location = new Location(Bukkit.getWorld(config.getString("spawnPoints.world")), Double.parseDouble(spawnLists[0]), Double.parseDouble(spawnLists[1]), Double.parseDouble(spawnLists[2]));
										if (!player.teleport(location)) {
											player.sendMessage(ChatColor.RED + "ワープに失敗しました。");
											return;
										}
									}
								}.runTaskLater(getInstance(), 10);
							}
						}
					}
					if (hasEnoughPlayers && timesLeft >= 0 && settingsCheck) timesLeft--;
				} else if (gameStarted) {
					if (playedTime >= gameTime) {
						gameEnded = true;
						for (Player player : Bukkit.getOnlinePlayers()) {
							player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！", "");
							new BukkitRunnable() {
								public void run() {
									if (fireworked >= 20) this.cancel();
									player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
									fireworked++;
								}
							}.runTaskTimer(getInstance(), 0, 5);
						}
						Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！");
						Bukkit.broadcastMessage(ChatColor.GRAY + "このサーバーはあと15秒でシャットダウンします。");
						TimerTask task = new TimerTask() {
							public void run() {
								Bukkit.broadcastMessage(ChatColor.GRAY + "サーバーをシャットダウン中...");
								Bukkit.shutdown();
							}
						};
						Timer timer = new Timer();
						timer.schedule(task, 1000*15);
						this.cancel();
						return;
					}
					for (Player player : Bukkit.getOnlinePlayers()) {
						Scoreboard scoreboard = hashMapScoreboard.get(player.getUniqueId());
						Objective objective3 = scoreboard.getObjective(DisplaySlot.SIDEBAR);
						String leftSecondPlayed = Integer.toString(playedTime % 60);
						if (leftSecondPlayed.length() == 1) leftSecondPlayed = "0" + leftSecondPlayed;
						String leftSecond = Integer.toString(gameTime % 60);
						if (leftSecond.length() == 1) leftSecond = "0" + leftSecond;
						String lastScore4 = hashMapLastScore4.get(event.getPlayer().getUniqueId());
						scoreboard.resetScores(lastScore4);
						lastScore4 = ChatColor.GREEN + "    " + Math.round(Math.nextDown(playedTime/60)) + ":" + leftSecondPlayed + " / " + Math.round(Math.nextDown(gameTime/60)) + ":" + leftSecond;
						hashMapLastScore4.put(player.getUniqueId(), lastScore4);
						Score score4 = objective3.getScore(lastScore4);
						score4.setScore(4);
						player.setScoreboard(scoreboard);
					}
					playedTime++;
				}
			}
		}.runTaskTimer(this, 0, 20);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) return;
		event.getEntity().getInventory().clear();
		if (hashMapTeam.get(event.getEntity().getUniqueId()) == "player") players--;
		hashMapTeam.remove(event.getEntity().getUniqueId());
		hashMapTeam.put(event.getEntity().getUniqueId(), "zombie");
		final Objective objective = hashMapScoreboard.get(event.getEntity().getUniqueId()).getObjective("scoreboard");
		Score score6 = objective.getScore(ChatColor.GREEN + "    チーム: " + ChatColor.DARK_GREEN + "ゾンビ");
		objective.getScoreboard().resetScores(ChatColor.GREEN + "    チーム: " + ChatColor.AQUA + "プレイヤー");
		score6.setScore(6);
		event.getEntity().setPlayerListName(ChatColor.DARK_GREEN + event.getEntity().getName());
		new BukkitRunnable() {
			public void run() {
				event.getEntity().spigot().respawn();
			}
		}.runTaskLater(this, 1000);
		if (players == 0 && gameStarted) {
			gameEnded = true;
			for (final Player player : Bukkit.getOnlinePlayers()) {
				player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！", "");
				new BukkitRunnable() {
					public void run() {
						if (fireworked >= 20) this.cancel();
						player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
						fireworked++;
					}
				}.runTaskTimer(this, 0, 5);
			}
			Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！");
			Bukkit.broadcastMessage(ChatColor.GRAY + "このサーバーはあと15秒でシャットダウンします。");
			TimerTask task = new TimerTask() {
				public void run() {
					Bukkit.broadcastMessage(ChatColor.GRAY + "サーバーをシャットダウン中...");
					Bukkit.shutdown();
				}
			};
			Timer timer = new Timer();
			timer.schedule(task, 1000*15);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		String[] spawnLists = Arrays.asList(config.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])).get(checkpoint).split(",");
		Location location = new Location(Bukkit.getWorld(config.getString("spawnPoints.world")), Double.parseDouble(spawnLists[0]), Double.parseDouble(spawnLists[1]), Double.parseDouble(spawnLists[2]));
		event.setRespawnLocation(location);
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
		event.getPlayer().setPlayerListName(ChatColor.DARK_GREEN + event.getPlayer().getName());
		event.getPlayer().getInventory().setHelmet(createLeatherItemStack(Material.LEATHER_HELMET, 0, 100, 0));
		event.getPlayer().getInventory().setChestplate(createLeatherItemStack(Material.LEATHER_CHESTPLATE, 0, 100, 0));
		event.getPlayer().getInventory().setLeggings(createLeatherItemStack(Material.LEATHER_LEGGINGS, 0, 100, 0));
		event.getPlayer().getInventory().setBoots(createLeatherItemStack(Material.LEATHER_BOOTS, 0, 100, 0));
		event.getPlayer().setMaxHealth(200);
		event.getPlayer().setHealth(200);
		event.getPlayer().setHealthScale(40);
		new BukkitRunnable() {
			public void run() {
				event.getPlayer().addPotionEffect(PotionEffectType.HUNGER.createEffect(100000, 0));
				event.getPlayer().addPotionEffect(PotionEffectType.SATURATION.createEffect(100000, 1));
				event.getPlayer().addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(100000, 100));
				event.getPlayer().addPotionEffect(PotionEffectType.SPEED.createEffect(100000, 1));
				event.getPlayer().addPotionEffect(PotionEffectType.JUMP.createEffect(100000, 0));
				ItemStack item = new ItemStack(Material.IRON_SWORD);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("" + ChatColor.RESET + ChatColor.WHITE + "ナイフ");
				item.setItemMeta(meta);
				item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
				item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 100);
				event.getPlayer().getInventory().setItem(0, item);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + event.getPlayer().getName() + " minecraft:stone_axe 1 0 {CanDestroy:[\"minecraft:grass\",\"minecraft:planks\",\"minecraft:dirt\"],HideFlags:1,Unbreakable:1,display:{Name:\"錆びついた斧\"},ench:[{id:32,lvl:10}]}");
			}
		}.runTaskLater(this, 40);
		if (players == 0 && gameStarted) {
			gameEnded = true;
			for (final Player player : Bukkit.getOnlinePlayers()) {
				player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！", "");
				new BukkitRunnable() {
					public void run() {
						if (fireworked >= 20) this.cancel();
						player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
						fireworked++;
					}
				}.runTaskTimer(this, 0, 5);
			}
			Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！");
			Bukkit.broadcastMessage(ChatColor.GRAY + "このサーバーはあと15秒でシャットダウンします。");
			TimerTask task = new TimerTask() {
				public void run() {
					Bukkit.broadcastMessage(ChatColor.GRAY + "サーバーをシャットダウン中...");
					Bukkit.shutdown();
				}
			};
			Timer timer = new Timer();
			timer.schedule(task, 1000*15);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerHurt(EntityDamageByEntityEvent event) {
		if (event.getEntityType() != EntityType.PLAYER || event.getDamager().getType() != EntityType.PLAYER) return;
		event.setCancelled(true);
		if (!gameStarted || gameEnded) return;
		if (hashMapTeam.get(event.getDamager().getUniqueId()) == "zombie" && playedTime < 10) return;
		if (hashMapTeam.get(event.getEntity().getUniqueId()) == "zombie") return;
		if (hashMapTeam.get(event.getDamager().getUniqueId()) == hashMapTeam.get(event.getEntity().getUniqueId())) return;
		Player player = (Player) event.getEntity();
		player.getInventory().clear();
		if (hashMapTeam.get(player.getUniqueId()) == "player") players--;
		hashMapTeam.remove(player.getUniqueId());
		hashMapTeam.put(player.getUniqueId(), "zombie");
		final Objective objective = hashMapScoreboard.get(player.getUniqueId()).getObjective("scoreboard");
		Score score6 = objective.getScore(ChatColor.GREEN + "    チーム: " + ChatColor.DARK_GREEN + "ゾンビ");
		objective.getScoreboard().resetScores(ChatColor.GREEN + "    チーム: " + ChatColor.AQUA + "プレイヤー");
		score6.setScore(6);
		player.setGameMode(GameMode.ADVENTURE);
		player.setPlayerListName(ChatColor.DARK_GREEN + player.getName());
		player.getInventory().setHelmet(createLeatherItemStack(Material.LEATHER_HELMET, 0, 100, 0));
		player.getInventory().setChestplate(createLeatherItemStack(Material.LEATHER_CHESTPLATE, 0, 100, 0));
		player.getInventory().setLeggings(createLeatherItemStack(Material.LEATHER_LEGGINGS, 0, 100, 0));
		player.getInventory().setBoots(createLeatherItemStack(Material.LEATHER_BOOTS, 0, 100, 0));
		player.setMaxHealth(200);
		player.setHealth(200);
		player.setHealthScale(40);
		new BukkitRunnable() {
			public void run() {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + player.getName() + " minecraft:saturation 100000 1");
				player.addPotionEffect(PotionEffectType.HUNGER.createEffect(100000, 0));
				player.addPotionEffect(PotionEffectType.SATURATION.createEffect(100000, 1));
				player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(100000, 100));
				player.addPotionEffect(PotionEffectType.SPEED.createEffect(100000, 1));
				player.addPotionEffect(PotionEffectType.JUMP.createEffect(100000, 0));
				ItemStack item = new ItemStack(Material.IRON_SWORD);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName("" + ChatColor.RESET + ChatColor.WHITE + "ナイフ");
				item.setItemMeta(meta);
				item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
				item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 100);
				player.getInventory().setItem(0, item);
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + event.getEntity().getName() + " minecraft:stone_axe 1 0 {CanDestroy:[\"minecraft:grass\",\"minecraft:planks\",\"minecraft:dirt\"],HideFlags:1,Unbreakable:1,display:{Name:\"錆びついた斧\"},ench:[{id:32,lvl:10}]}");
			}
		}.runTaskLater(this, 40);
		player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 100, 1);
		player.sendTitle(ChatColor.DARK_GREEN + "ゾンビチームになった！", "");
		Bukkit.broadcastMessage(ChatColor.DARK_GREEN + player.getName() + "が" + event.getDamager().getName() + "によってゾンビにされた。");
		if (players == 0 && gameStarted) {
			gameEnded = true;
			for (final Player player2 : Bukkit.getOnlinePlayers()) {
				player2.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！", "");
				new BukkitRunnable() {
					public void run() {
						if (fireworked >= 20) this.cancel();
						player2.playSound(player2.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
						fireworked++;
					}
				}.runTaskTimer(this, 0, 5);
			}
			Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！");
			Bukkit.broadcastMessage(ChatColor.GRAY + "このサーバーはあと15秒でシャットダウンします。");
			TimerTask task = new TimerTask() {
				public void run() {
					Bukkit.broadcastMessage(ChatColor.GRAY + "サーバーをシャットダウン中...");
					Bukkit.shutdown();
				}
			};
			Timer timer = new Timer();
			timer.schedule(task, 1000*15);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) return;
		if (!gameStarted || gameEnded) event.setCancelled(true);
		if (event.getCause() == DamageCause.FALL) event.setCancelled(true);
		if (hashMapTeam.get(event.getEntity().getUniqueId()) == "player" && event.getCause() == DamageCause.PROJECTILE) event.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerLeft(PlayerQuitEvent event) {
		if (Bukkit.getOnlinePlayers().size() >= 2) hasEnoughPlayers = true; else {
			hasEnoughPlayers = false;
			timesLeft = 180;
		}
		if (hashMapTeam.get(event.getPlayer().getUniqueId()) == "zombie") zombies--; else players--;
		if (gameStarted && zombies < 0) throw new IllegalStateException("Zombie count is should be 0 or more.");
		if (gameStarted && players < 0) throw new IllegalStateException("Player count is should be 0 or more.");
		hashMapTeam.remove(event.getPlayer().getUniqueId());
		if (gameStarted && (zombies == 0 || players == 0)) {
			String team = zombies == 0 ? "プレイヤー" : "ゾンビ";
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "ゾンビチームの勝ち！", "");
				new BukkitRunnable() {
					public void run() {
						if (fireworked >= 20) this.cancel();
						player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
						fireworked++;
					}
				}.runTaskTimer(this, 0, 5);
			}
			Bukkit.broadcastMessage("" + ChatColor.GREEN + ChatColor.BOLD + team + "チームの勝ち！");
			Bukkit.broadcastMessage(ChatColor.GRAY + "このサーバーはあと15秒でシャットダウンします。");
			TimerTask task = new TimerTask() {
				public void run() {
					Bukkit.broadcastMessage(ChatColor.GRAY + "サーバーをシャットダウン中...");
					Bukkit.shutdown();
				}
			};
			Timer timer = new Timer();
			timer.schedule(task, 1000*15);
		}
	}

	public ItemStack createLeatherItemStack(Material material, int red, int green, int blue) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
		lam.setColor(Color.fromRGB(red, green, blue));
		item.setItemMeta(lam);
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
		return item;
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Location Llocation = new Location(
				event.getEntity().getLocation().getWorld(),
				Math.nextUp(event.getEntity().getLocation().getX()),
				Math.nextUp(event.getEntity().getLocation().getY()),
				Math.nextUp(event.getEntity().getLocation().getZ()+0.6));
		Block block = event.getEntity().getWorld().getBlockAt(Llocation);
		if (block == null) return;
		int durability = Constants.materialDurability.getOrDefault(block.getType(), 5);
		if (block.getType() == Material.DIRT || block.getType() == Material.GRASS || block.getType() == Material.WOOD) {
			String location = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
			String wall = (String) locationWall.getOrDefault(location, null);
			Integer state = hashMapBlockState.get(wall) != null ? hashMapBlockState.get(wall) : 0;
			if (state >= durability) {
				config.getStringList("wallLocation." + wall).forEach(blocation -> {
					String[] blocationArray = blocation.split(",");
					Block ablock = event.getEntity().getWorld().getBlockAt(Integer.parseInt(blocationArray[0]), Integer.parseInt(blocationArray[1]), Integer.parseInt(blocationArray[2]));
					ablock.setType(Material.AIR);
				});
				block.setType(Material.AIR);
				hashMapBlockState.remove(wall);
				PacketContainer packet1 = protocol.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
				packet1.getBlockPositionModifier().write(0, new BlockPosition(block.getX(), block.getY()-1, block.getZ()));
				packet1.getIntegers().write(0, new Random().nextInt(2000));
				packet1.getIntegers().write(1, 0); // remove animation
				for (Player player : Bukkit.getOnlinePlayers()) {
					try {
						protocol.sendServerPacket(player, packet1);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				return;
			}
			hashMapBlockState.put(wall, state+1);
			PacketContainer packet1 = protocol.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
			packet1.getBlockPositionModifier().write(0, new BlockPosition(block.getX(), block.getY(), block.getZ()));
			packet1.getIntegers().write(0, new Random().nextInt(2000));
			packet1.getIntegers().write(1, (state+1)*3);
			for (Player player : Bukkit.getOnlinePlayers()) {
				try {
					protocol.sendServerPacket(player, packet1);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					e.getCause().printStackTrace();
				}
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (gameStarted) {
			if (hashMapTeam.get(event.getPlayer().getUniqueId()) == "zombie") {

			}
		}
		Block block = event.getBlock();
		if (block == null) return;
		String location = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
		String wall = (String) locationWall.getOrDefault(location, null);
		hashMapBlockState.remove(wall);
		PacketContainer packet1 = protocol.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
		packet1.getBlockPositionModifier().write(0, new BlockPosition(block.getX(), block.getY(), block.getZ()));
		packet1.getIntegers().write(0, new Random().nextInt(2000));
		packet1.getIntegers().write(1, 0); // remove animation
		for (Player player : Bukkit.getOnlinePlayers()) {
			try {
				protocol.sendServerPacket(player, packet1);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		config.reloadWithoutException();
		if (gameEnded) {
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "ゲームはすでに終了しています！約30秒後に参加しなおしてください。");
			return;
		}
		if (Bukkit.getOnlinePlayers().size() < Bukkit.getMaxPlayers()) {
			event.allow();
			return;
		}
		try {
			List<String> sponsors = Arrays.asList(config.getList("sponsors", new ArrayList<String>()).toArray(new String[0]));
			if (sponsors.contains(event.getPlayer().getUniqueId().toString()) == true) {
				event.allow();
			} else if (sponsors.contains(event.getPlayer().getUniqueId().toString()) == false) {
				event.disallow(Result.KICK_OTHER, ChatColor.RED + "満員のサーバーに参加するにはスポンサーが必要です！");
			}
		} catch (Exception e) {
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "設定の読み込みに失敗しました。あとでやり直してください。");
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.getMessage().equalsIgnoreCase("gg") || event.getMessage().equalsIgnoreCase("good game")) {
			event.setMessage(ChatColor.GOLD + event.getMessage());
		}
		if (hashMapTeam.get(event.getPlayer().getUniqueId()) == "zombie") {
			if (event.getMessage().startsWith("!") || gameEnded || !gameStarted) {
				event.setMessage(event.getMessage().replaceFirst("!", ""));
				event.setFormat(ChatColor.RED + "[All] " + ChatColor.DARK_GREEN + "[Z] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
			} else {
				hashMapTeam.forEach((uuid, team) -> {
					if (team != "zombie") return;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getUniqueId().equals(uuid)) {
							player.sendMessage(ChatColor.AQUA + "[チーム] " +  ChatColor.DARK_GREEN + "[Z] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
						}
					}
				});
				event.setCancelled(true);
			}
		} else {
			if (event.getMessage().startsWith("!") || gameEnded || !gameStarted) {
				event.setMessage(event.getMessage().replaceFirst("!", ""));
				event.setFormat(ChatColor.RED + "[All] " + ChatColor.AQUA + "[P] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
			} else {
				hashMapTeam.forEach((uuid, team) -> {
					if (team != "player") return;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getUniqueId().equals(uuid)) {
							player.sendMessage(ChatColor.AQUA + "[チーム] " +  ChatColor.AQUA + "[P] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
						}
					}
				});
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	/**
	 * Cancel the click event when player has clicked armor slot to prevents wear off their armors.
	 */
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlotType() == SlotType.ARMOR) event.setCancelled(true);
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e){
		World world = e.getWorld();
		if (world.hasStorm()) {
			world.setWeatherDuration(1);
		}
	}

	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		ItemStack knifeitem = new ItemStack(Material.IRON_SWORD);
		ItemMeta knifemeta = knifeitem.getItemMeta();
		knifemeta.setDisplayName("" + ChatColor.RESET + ChatColor.WHITE + "ナイフ");
		knifeitem.setItemMeta(knifemeta);
		knifeitem.addUnsafeEnchantment(Enchantment.DURABILITY, 100); // Always sharp!
		knifeitem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 100); // *stabs someone*
		if (event.getItemDrop().getItemStack().isSimilar(knifeitem)) event.setCancelled(true); // Please don't drop knife, its dangerous
		if (event.getItemDrop().getItemStack().isSimilar(new ItemStack(Material.DIAMOND_HELMET))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().isSimilar(new ItemStack(Material.DIAMOND_CHESTPLATE))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().isSimilar(new ItemStack(Material.DIAMOND_LEGGINGS))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().isSimilar(new ItemStack(Material.DIAMOND_BOOTS))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().isSimilar(createLeatherItemStack(Material.LEATHER_HELMET, 0, 100, 0))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().isSimilar(createLeatherItemStack(Material.LEATHER_CHESTPLATE, 0, 100, 0))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().isSimilar(createLeatherItemStack(Material.LEATHER_LEGGINGS, 0, 100, 0))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().isSimilar(createLeatherItemStack(Material.LEATHER_BOOTS, 0, 100, 0))) event.setCancelled(true); // armor
		if (event.getItemDrop().getItemStack().getType() == Material.IRON_BARDING) event.setCancelled(true); // Please don't drop gun
		if (event.getItemDrop().getItemStack().getType() == Material.STONE_AXE) event.setCancelled(true); // Please don't drop axe
	}

	public static Player targetP(Location loc){
		Player nearestPlayer = null;
		double lastDistance = Double.MAX_VALUE;
		for(Player p : loc.getWorld().getPlayers()){
			double distanceSqrd = loc.distanceSquared(p.getLocation());
			if(distanceSqrd < lastDistance){
				lastDistance = distanceSqrd;
				nearestPlayer = p;
			}
		}
		return nearestPlayer;
	}

	public static Player targetPFindPlayers(Location loc){
		if (players <= 0) return null;
		Player nearestPlayer = null;
		double lastDistance = Double.MAX_VALUE;
		for(Player p : loc.getWorld().getPlayers()){
			if (!hashMapTeam.get(p.getUniqueId()).equalsIgnoreCase("player")) continue;
			double distanceSqrd = loc.distanceSquared(p.getLocation());
			if (distanceSqrd > 10) continue;
			if(distanceSqrd < lastDistance){
				lastDistance = distanceSqrd;
				nearestPlayer = p;
			}
		}
		return nearestPlayer;
	}
}
