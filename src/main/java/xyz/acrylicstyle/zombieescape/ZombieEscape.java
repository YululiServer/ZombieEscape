// *Warning* Bugged string may be found while decompiling this source!
package xyz.acrylicstyle.zombieescape;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
import org.bukkit.potion.PotionEffect;
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
import xyz.acrylicstyle.tomeito_core.utils.Log;
import xyz.acrylicstyle.zombieescape.commands.Sponsor;
import xyz.acrylicstyle.zombieescape.commands.ZombieEscapeConfig;
import xyz.acrylicstyle.zombieescape.commands.ZombieEscapeGameUtil;
import xyz.acrylicstyle.zombieescape.data.Constants;
import xyz.acrylicstyle.zombieescape.effects.ActionBar;

@SuppressWarnings("depression")
public class ZombieEscape extends JavaPlugin implements Listener {
	public final static int mininumPlayers = 2;
	public static ConfigProvider config = null;
	public static ConfigProvider mapConfig = null;
	public static HashMap<UUID, Scoreboard> hashMapScoreboard = new HashMap<UUID, Scoreboard>();
	public static HashMap<UUID, PlayerTeam> hashMapTeam = new HashMap<UUID, PlayerTeam>();
	public static HashMap<UUID, String> hashMapLastScore4 = new HashMap<UUID, String>();
	public static HashMap<UUID, String> hashMapLastScore8 = new HashMap<UUID, String>();
	public static HashMap<String, Integer> hashMapBlockState = new HashMap<String, Integer>();
	/**
	 * Not in use.
	 */
	public static HashMap<UUID, Boolean> hashMapOriginZombie = new HashMap<UUID, Boolean>();
	public static HashMap<UUID, Boolean> lockActionBar = new HashMap<UUID, Boolean>();
	public static HashMap<String, Team> teams = new HashMap<String, Team>();
	public static String mapName = null;
	public static ScoreboardManager manager = null;
	public static ProtocolManager protocol = null;
	public static volatile int zombies = 0;
	public static volatile int players = 0;
	public static int timesLeft = 180;
	public static boolean timerStarted = false;
	public static boolean hasEnoughPlayers = false;
	public static boolean settingsCheck = false;
	public static boolean gameStarted = false;
	public static boolean debug = false;
	public static boolean playersReset = false;
	public static int gameTime = 1800; // 30 minutes
	public static int playedTime = 0;
	public static int checkpoint = 0;
	public static int maxCheckpoints = 0;
	public static int fireworked = 0;
	public static boolean gameEnded = false;
	public static Map<String, Object> locationWall = null;

	@Override
	public void onEnable() {
		protocol = ProtocolLibrary.getProtocolManager();
		try {
			config = new ConfigProvider("./plugins/ZombieEscape/config.yml");
			mapName = config.getString("map", "world");
			mapConfig = new ConfigProvider("./plugins/ZombieEscape/maps/" + mapName + ".yml");
			debug = config.getBoolean("debug", false);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
			e1.getCause().printStackTrace();
			Bukkit.getLogger().severe("[ZombieEscape] Failed to load config, disabling plugin.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		if (Bukkit.getWorld(mapConfig.getString("spawnPoints.world", "world")) == null) {
			Log.severe("Failed to load world(probably does not exist), disabling plugin.");
			Log.severe("Tried to load world: " + mapConfig.getString("spawnPoints.world", "world"));
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
			Bukkit.getPluginCommand("setmapname").setExecutor(zec.new SetMapName());
			Bukkit.getPluginCommand("setmap").setExecutor(zec.new SetMap());
			Bukkit.getPluginCommand("suicide").setExecutor(zegu.new Suicide());
			Bukkit.getPluginCommand("setcp").setExecutor(zegu.new SetCheckpoint());
			Bukkit.getPluginCommand("startgame").setExecutor(zegu.new StartGame());
			Bukkit.getPluginCommand("endgame").setExecutor(zegu.new EndGame());
			Bukkit.getPluginCommand("check").setExecutor(zegu.new CheckConfig());
			Bukkit.getPluginCommand("setstatus").setExecutor(zegu.new SetStatus());
			Bukkit.getPluginCommand("zombieescape").setExecutor(new CommandExecutor() {
				@Override
				public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
					if (args.length != 0 && args[0].equalsIgnoreCase("reload")) {
						reload();
						sender.sendMessage(ChatColor.GREEN + "✓ 設定を再読み込みしました。");
						return true;
					}
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
		maxCheckpoints = Math.min(mapConfig.getStringList("spawnPoints.player").size(), mapConfig.getStringList("spawnPoints.zombie").size());
		locationWall = ConfigProvider.getConfigSectionValue(mapConfig.get("locationWall", new HashMap<String, Object>()), true);
		Bukkit.getLogger().info("[ZombieEscape] Enabled Zombie Escape");
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e){
		List<String> cmds = config.getStringList("disabledcommands");
		cmds.forEach(cmd -> {
			if (e.getMessage().startsWith(cmd) || e.getMessage().startsWith("/" + cmd)) {
				e.getPlayer().sendMessage(ChatColor.RED + "このコマンドは無効化されています。");
				e.setCancelled(true);
			}
		});
	}

	public static void reload() {
		ZombieEscape.config.reloadWithoutException();
		mapName = config.getString("map", "world");
		try {
			mapConfig = new ConfigProvider("./plugins/ZombieEscape/maps/" + mapName + ".yml");
		} catch (IOException | InvalidConfigurationException e) {
			Log.error("Couldn't read config: maps/" + mapName + ".yml");
			e.printStackTrace();
		}
		locationWall = ConfigProvider.getConfigSectionValue(mapConfig.get("locationWall", new HashMap<String, Object>()), true);
		maxCheckpoints = Math.min(mapConfig.getStringList("spawnPoints.player").size(), mapConfig.getStringList("spawnPoints.zombie").size());
		debug = config.getBoolean("debug", false);
	}

	/**
	 * Reload config and check if all configs are set correctly.<br>
	 * If all checks passed, settingsCheck will be true. Otherwise it'll set to false.
	 */
	public static void checkConfig() {
		config.reloadWithoutException();
		mapConfig.reloadWithoutException();
		if (mapConfig.getList("spawnPoints.zombie") != null // check if zombie spawn points exists
				&& mapConfig.getList("spawnPoints.zombie").size() != 0 // check if zombie spawn points are *actually* exists(0 isn't exist)
				&& mapConfig.getList("spawnPoints.player") != null // check if player spawn points exists
				&& mapConfig.getList("spawnPoints.player").size() != 0
				&& mapConfig.get("locationWall") != null) settingsCheck = true; // if it's null, ProjectileHitEvent won't work!
		else settingsCheck = false;
	}

	public ZombieEscape getInstance() {
		return this;
	}

	@EventHandler
	public synchronized void onPlayerJoin(final PlayerJoinEvent event) {
		long time = System.currentTimeMillis();
		if (!gameStarted && timesLeft >= 7) players = players + 1;
		World world = Bukkit.getWorld(mapConfig.getString("spawnPoints.world", "world"));
		new BukkitRunnable() {
			public void run() {
				event.getPlayer().teleport(world.getSpawnLocation());
			}
		}.runTask(this);
		hashMapTeam.put(event.getPlayer().getUniqueId(), PlayerTeam.PLAYER);
		lockActionBar.put(event.getPlayer().getUniqueId(), false);
		new BukkitRunnable() {
			public void run() {
				long time = System.currentTimeMillis();
				for (Player player : Bukkit.getOnlinePlayers()) {
					Set<Material> set = new HashSet<Material>();
					set.add(Material.AIR);
					Block block = player.getTargetBlock(set, 4);
					if (block == null) {
						lockActionBar.put(player.getUniqueId(), false);
						continue;
					}
					String location = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
					String wall = (String) locationWall.getOrDefault(location, null);
					if (wall == null) {
						lockActionBar.put(player.getUniqueId(), false);
						continue;
					}
					Integer state = hashMapBlockState.get(wall) != null ? hashMapBlockState.get(wall) : 0;
					int durability = (int) Math.nextUp(Math.min(Constants.materialDurability.getOrDefault(block.getType(), 5)*((double)players/(double)5), 1000));
					ActionBar.setActionBarWithoutException(player, ChatColor.GREEN + "壁の耐久力: " + state + "/" + durability);
					lockActionBar.put(player.getUniqueId(), true);
					if (debug) {
						long end = System.currentTimeMillis()-time;
						Log.debug("Display durability took " + end + "ms");
					}
				}
			}
		}.runTaskTimer(this, 0, 20);
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
					player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 1, false, false));
					if (hashMapTeam.get(player.getUniqueId()) == PlayerTeam.ZOMBIE) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 0, false, false));
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000, 0, false, false));
						player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100000, 100, false, false));
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
		final Scoreboard board = manager.getNewScoreboard();
		event.getPlayer().setScoreboard(board);
		final Objective objective = board.registerNewObjective("scoreboard", "dummy");
		Score score7 = objective.getScore(" ");
		score7.setScore(7);
		Score score5 = objective.getScore("  ");
		score5.setScore(5);
		Score score3 = objective.getScore("   ");
		score3.setScore(3);
		Score score2 = objective.getScore(ChatColor.GREEN + "    マップ: " + ChatColor.translateAlternateColorCodes('&', mapConfig.getString("mapname", "???")));
		score2.setScore(2);
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
				event.getPlayer().setGameMode(GameMode.ADVENTURE);
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
				if (event.getPlayer().isOp()) {
					event.getPlayer().sendMessage(ChatColor.BLUE + "--------------------------------------------------");
					event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "          - OP用コマンド -");
					event.getPlayer().sendMessage(ChatColor.GREEN + "/setmap <マップ> - プレイするマップを設定します。全プレイヤーがキックされます。*ここで指定するマップはマップ名ではありません*");
					event.getPlayer().sendMessage(ChatColor.GREEN + "/setstatus - ゲームに関する設定を変更します。");
					event.getPlayer().sendMessage(ChatColor.GREEN + "/sponsor <プレイヤー> - 指定したプレイヤーをスポンサーとして登録します。");
					event.getPlayer().sendMessage(ChatColor.GREEN + "/removesponsor <プレイヤー> - 指定したプレイヤーをスポンサーから削除します。");
					event.getPlayer().sendMessage(ChatColor.GREEN + "/setcp <数値> - チェックポイントを設定します。");
					event.getPlayer().sendMessage(ChatColor.GREEN + "/startgame - ゲーム開始までのカウントダウンを6秒に設定します。" + ChatColor.AQUA + "/setstatus timesLeft 6" + ChatColor.GREEN + "と同等です。");
					event.getPlayer().sendMessage(ChatColor.GREEN + "/endgame - ゲームが開始している場合、ゲームを終了します。");
					event.getPlayer().sendMessage("");
					event.getPlayer().sendMessage("" + ChatColor.RED + "これらのOP用コマンドの乱用は禁止されています。");
					event.getPlayer().sendMessage("" + ChatColor.RED + "乱用した場合、OP権限の剥奪や、BANが行われます。");
					event.getPlayer().sendMessage(ChatColor.BLUE + "--------------------------------------------------");
				}
				if (gameStarted || timesLeft < 6) {
					event.getPlayer().sendMessage(ChatColor.RED + "ゲームはすでに開始しています！");
					event.getPlayer().setGameMode(GameMode.SPECTATOR);
					event.getPlayer().setPlayerListName(ChatColor.WHITE + event.getPlayer().getName());
					hashMapTeam.put(event.getPlayer().getUniqueId(), PlayerTeam.SPECTATOR);
					return;
				}
			}
		}.runTaskLater(this, 40);
		healthBar.runTaskTimer(this, 0, 20);
		for (final Player player : Bukkit.getOnlinePlayers()) {
			for (final Player other : Bukkit.getOnlinePlayers()) {
				player.hidePlayer(other);
				player.showPlayer(other);
			}
		}
		if (debug) {
			long end = System.currentTimeMillis() - time;
			Log.debug("onPlayerJoin() took " + end + "ms");
		}
		if (timerStarted) return;
		timerStarted = true;
		new BukkitRunnable() {
			@SuppressWarnings("deprecation") // player#sendTitle, i can't find non-deprecated methods in 1.8.8.
			public synchronized void run() {
				long time = System.currentTimeMillis();
				for (final Player player : Bukkit.getOnlinePlayers()) {
					hashMapScoreboard.get(player.getUniqueId()).resetScores(hashMapLastScore4.get(player.getUniqueId()));
				}
				final String zombieMessage = ChatColor.GREEN + "    チーム: " + ChatColor.DARK_GREEN + "ゾンビ";
				final String playerMessage = ChatColor.GREEN + "    チーム: " + ChatColor.AQUA + "プレイヤー";
				final String spectatorMessage = ChatColor.GREEN + "    チーム: " + ChatColor.GRAY + "スペクテイター";
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
						scoreboard.resetScores(spectatorMessage);
						if (hashMapTeam.get(player.getUniqueId()) == PlayerTeam.ZOMBIE) {
							Score score6 = objective3.getScore(zombieMessage);
							score6.setScore(6);
						} else if (hashMapTeam.get(player.getUniqueId()) == PlayerTeam.PLAYER) {
							Score score6 = objective3.getScore(playerMessage);
							score6.setScore(6);
						} else { // spectator
							Score score6 = objective3.getScore(spectatorMessage);
							score6.setScore(6);
						}
						player.setScoreboard(hashMapScoreboard.get(player.getUniqueId()));
						if (timesLeft == 5) {
							if (!playersReset) {
								playersReset = true;
								players = 0;
								zombies = 0;
							}
							board.resetScores(playerMessage);
							if (hashMapTeam.get(event.getPlayer().getUniqueId()) != PlayerTeam.SPECTATOR) {
								if (((int) Math.round((double) Bukkit.getOnlinePlayers().size() / (double) 10) - zombies) >= 0) {
									hashMapOriginZombie.put(player.getUniqueId(), true);
									hashMapTeam.put(player.getUniqueId(), PlayerTeam.ZOMBIE);
									zombies = zombies+1;
									teams.get(hashMapTeam.get(player.getUniqueId()).toString()).setAllowFriendlyFire(false);
									teams.get(hashMapTeam.get(player.getUniqueId()).toString()).addEntry(player.getName());
									Score score6 = objective.getScore(ChatColor.GREEN + "    チーム: " + ChatColor.DARK_GREEN + "ゾンビ");
									score6.setScore(6);
									player.setMaxHealth(150);
									player.setHealth(150);
									player.setHealthScale(40);
									player.getInventory().setHelmet(createLeatherItemStack(Material.LEATHER_HELMET, 0, 100, 0));
									player.getInventory().setChestplate(createLeatherItemStack(Material.LEATHER_CHESTPLATE, 0, 100, 0));
									player.getInventory().setLeggings(createLeatherItemStack(Material.LEATHER_LEGGINGS, 0, 100, 0));
									player.getInventory().setBoots(createLeatherItemStack(Material.LEATHER_BOOTS, 0, 100, 0));
									player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 0, false, false));
									player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000, 0, false, false));
									player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100000, 100, false, false));
									player.setPlayerListName(ChatColor.DARK_GREEN + player.getName());
								} else {
									players = players+1;
									hashMapTeam.put(player.getUniqueId(), PlayerTeam.PLAYER);
									teams.get(hashMapTeam.get(player.getUniqueId()).toString()).setAllowFriendlyFire(false);
									teams.get(hashMapTeam.get(player.getUniqueId()).toString()).addEntry(player.getName());
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
									player.setPlayerListName(ChatColor.AQUA + player.getName());
								}
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
							player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 100, 1);
							if (hashMapTeam.get(player.getUniqueId()) == PlayerTeam.ZOMBIE) {
								player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "GO!", ChatColor.YELLOW + "目標: プレイヤーを全員倒すか先にゴールに到達する");
								player.sendMessage(ChatColor.GRAY + "あと12秒後にワープします...");
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
										String[] spawnLists = Arrays.asList(mapConfig.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])).get(0).split(",");
										Location location = new Location(Bukkit.getWorld(mapConfig.getString("spawnPoints.world")), Double.parseDouble(spawnLists[0]), Double.parseDouble(spawnLists[1]), Double.parseDouble(spawnLists[2]));
										if (!player.teleport(location)) {
											player.sendMessage(ChatColor.RED + "ワープに失敗しました。");
											return;
										}
									}
								}.runTaskLater(getInstance(), 20*12);
							} else if (hashMapTeam.get(player.getUniqueId()) == PlayerTeam.PLAYER) {
								new BukkitRunnable() {
									public void run() {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "shot give " + player.getName() + " ak-47");
										player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + "GO!", ChatColor.YELLOW + "目標: ゾンビから逃げ、ゾンビよりも先にゴールに到達する");
										String[] spawnLists = Arrays.asList(mapConfig.getList("spawnPoints.player", new ArrayList<String>()).toArray(new String[0])).get(0).split(",");
										Location location = new Location(Bukkit.getWorld(mapConfig.getString("spawnPoints.world")), Double.parseDouble(spawnLists[0]), Double.parseDouble(spawnLists[1]), Double.parseDouble(spawnLists[2]));
										if (!player.teleport(location)) {
											player.sendMessage(ChatColor.RED + "ワープに失敗しました。");
											return;
										}
									}
								}.runTaskLater(getInstance(), 10);
							} else {
								player.setGameMode(GameMode.SPECTATOR);
							}
						}
					}
					if (hasEnoughPlayers && timesLeft >= 0 && settingsCheck) timesLeft--;
				} else if (gameStarted) {
					if (debug) Log.debug("Players: " + players + ", Zombies: " + zombies);
					if (playedTime >= gameTime) {
						endGame("ゾンビ");
						this.cancel();
						return;
					}
					for (Player player : Bukkit.getOnlinePlayers()) {
						final Scoreboard scoreboard = hashMapScoreboard.get(player.getUniqueId());
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
						Score score0 = objective3.getScore("     ");
						score0.setScore(0);
						for (int i = 1; i < maxCheckpoints; i++) {
							String okString = ChatColor.RED + "✕ チェックポイント" + i;
							String koString = ChatColor.GREEN + "✓ チェックポイント" + i;
							if (checkpoint >= i) {
								scoreboard.resetScores(koString);
								Score score = objective3.getScore(okString);
								score.setScore(-i);
							} else {
								scoreboard.resetScores(okString);
								Score score = objective3.getScore(koString);
								score.setScore(-i);
							}
						}
						player.setScoreboard(scoreboard);
					}
					playedTime++;
				}
				if (debug) {
					long end = System.currentTimeMillis()-time;
					Log.debug("Scoreboard update tick took " + end + "ms");
				}
			}
		}.runTaskTimer(this, 0, 20);
	}

	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) return;
		if (hashMapTeam.get(event.getEntity().getUniqueId()) == PlayerTeam.SPECTATOR) return;
		event.getEntity().getInventory().clear();
		if (hashMapTeam.get(event.getEntity().getUniqueId()) == PlayerTeam.PLAYER) players--;
		hashMapTeam.remove(event.getEntity().getUniqueId());
		hashMapTeam.put(event.getEntity().getUniqueId(), PlayerTeam.ZOMBIE);
		final Objective objective = hashMapScoreboard.get(event.getEntity().getUniqueId()).getObjective("scoreboard");
		Score score6 = objective.getScore(ChatColor.GREEN + "    チーム: " + ChatColor.DARK_GREEN + "ゾンビ");
		objective.getScoreboard().resetScores(ChatColor.GREEN + "    チーム: " + ChatColor.AQUA + "プレイヤー");
		score6.setScore(6);
		event.getEntity().setPlayerListName(ChatColor.DARK_GREEN + event.getEntity().getName());
		if (players <= 0 && gameStarted) endGame("ゾンビ");
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		String[] spawnLists = Arrays.asList(mapConfig.getList("spawnPoints.zombie", new ArrayList<String>()).toArray(new String[0])).get(checkpoint).split(",");
		Location location = new Location(Bukkit.getWorld(mapConfig.getString("spawnPoints.world")), Double.parseDouble(spawnLists[0]), Double.parseDouble(spawnLists[1]), Double.parseDouble(spawnLists[2]));
		event.setRespawnLocation(location);
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
		event.getPlayer().setPlayerListName(ChatColor.DARK_GREEN + event.getPlayer().getName());
		event.getPlayer().getInventory().setHelmet(createLeatherItemStack(Material.LEATHER_HELMET, 0, 100, 0));
		event.getPlayer().getInventory().setChestplate(createLeatherItemStack(Material.LEATHER_CHESTPLATE, 0, 100, 0));
		event.getPlayer().getInventory().setLeggings(createLeatherItemStack(Material.LEATHER_LEGGINGS, 0, 100, 0));
		event.getPlayer().getInventory().setBoots(createLeatherItemStack(Material.LEATHER_BOOTS, 0, 100, 0));
		event.getPlayer().setMaxHealth(150);
		event.getPlayer().setHealth(150);
		event.getPlayer().setHealthScale(40);
		new BukkitRunnable() {
			public void run() {
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100000, 0, false, false));
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 1, false, false));
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100000, 100, false, false));
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 0, false, false));
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100000, 0, false, false));
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
			endGame("ゾンビ");
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerHurt(EntityDamageByEntityEvent event) {
		long time = System.currentTimeMillis();
		if (event.getEntityType() != EntityType.PLAYER || event.getDamager().getType() != EntityType.PLAYER) return;
		event.setCancelled(true);
		if (!gameStarted || gameEnded) return;
		if (hashMapTeam.get(event.getDamager().getUniqueId()) == PlayerTeam.ZOMBIE && playedTime < 10) return;
		if (hashMapTeam.get(event.getEntity().getUniqueId()) == PlayerTeam.ZOMBIE) return;
		if (hashMapTeam.get(event.getDamager().getUniqueId()) == hashMapTeam.get(event.getEntity().getUniqueId())) return;
		Player player = (Player) event.getEntity();
		player.getInventory().clear();
		if (hashMapTeam.get(player.getUniqueId()) == PlayerTeam.PLAYER) {
			players = players - 1;
			zombies = zombies + 1;
		}
		hashMapTeam.remove(player.getUniqueId());
		hashMapTeam.put(player.getUniqueId(), PlayerTeam.ZOMBIE);
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
		player.setMaxHealth(150);
		player.setHealth(150);
		player.setHealthScale(40);
		new BukkitRunnable() {
			public void run() {
				player.addPotionEffect(PotionEffectType.HUNGER.createEffect(100000, 0));
				player.addPotionEffect(PotionEffectType.SATURATION.createEffect(100000, 1));
				player.addPotionEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(100000, 100));
				player.addPotionEffect(PotionEffectType.SPEED.createEffect(100000, 0));
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
		for (Player player2 : Bukkit.getOnlinePlayers()) {
			player2.playSound(player2.getLocation(), Sound.ENDERDRAGON_GROWL, 80, 1); // avoid loud sound, it's 80%!
		}
		player.sendTitle(ChatColor.DARK_GREEN + "ゾンビチームになった！", "");
		Bukkit.broadcastMessage(ChatColor.DARK_GREEN + player.getName() + "が" + event.getDamager().getName() + "によってゾンビにされた。");
		if (players <= 0 && gameStarted) {
			endGame("ゾンビ");
		}
		if (debug) {
			long end = System.currentTimeMillis()-time;
			Log.debug("onPlayerHurt() took " + end + "ms");
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) return;
		if (!gameStarted || gameEnded) event.setCancelled(true);
		if (event.getCause() == DamageCause.FALL) event.setCancelled(true);
		if (hashMapTeam.get(event.getEntity().getUniqueId()) == PlayerTeam.PLAYER && event.getCause() == DamageCause.PROJECTILE) event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerLeft(PlayerQuitEvent event) {
		if (Bukkit.getOnlinePlayers().size() >= 2) hasEnoughPlayers = true; else {
			hasEnoughPlayers = false;
			timesLeft = 180;
		}
		if (hashMapTeam.get(event.getPlayer().getUniqueId()) == PlayerTeam.ZOMBIE) {
			zombies = zombies - 1;
		} else if(hashMapTeam.get(event.getPlayer().getUniqueId()) == PlayerTeam.PLAYER) {
			players = players - 1;
		} // if else, do nothing.
		if (gameStarted && zombies < 0) {
			zombies = 0;
			throw new IllegalStateException("Zombie count is should be 0 or more.");
		}
		if (gameStarted && players < 0) {
			players = 0;
			throw new IllegalStateException("Player count is should be 0 or more.");
		}
		hashMapTeam.remove(event.getPlayer().getUniqueId());
		if (gameStarted && (zombies == 0 || players == 0)) {
			// String team = zombies == 0 ? "プレイヤー" : "ゾンビ";
			// endGame(team);
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
		long time = System.currentTimeMillis();
		Location Llocation = new Location(
				event.getEntity().getLocation().getWorld(),
				Math.nextUp(event.getEntity().getLocation().getX()),
				Math.nextUp(event.getEntity().getLocation().getY()),
				Math.nextUp(event.getEntity().getLocation().getZ()+0.6));
		Block block = event.getEntity().getWorld().getBlockAt(Llocation);
		if (block == null) return;
		int durability = (int) Math.nextUp(Math.min(Constants.materialDurability.getOrDefault(block.getType(), 5)*((double)players/(double)5), 1000));
		if (block.getType() == Material.DIRT || block.getType() == Material.GRASS || block.getType() == Material.WOOD) {
			String location = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
			String wall = (String) locationWall.getOrDefault(location, null);
			Integer state = hashMapBlockState.get(wall) != null ? hashMapBlockState.get(wall) : 0;
			if (state >= durability) {
				mapConfig.getStringList("wallLocation." + wall).forEach(blocation -> {
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
		if (debug) {
			long end = System.currentTimeMillis()-time;
			Log.debug("onProjectileHit() took " + end + "ms");
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		long time = System.currentTimeMillis();
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
		if (debug) {
			long end = System.currentTimeMillis()-time;
			Log.debug("onBlockBreak() took " + end + "ms");
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
		if (hashMapTeam.get(event.getPlayer().getUniqueId()) == PlayerTeam.ZOMBIE) {
			if (event.getMessage().startsWith("!") || gameEnded || !gameStarted) {
				event.setMessage(event.getMessage().replaceFirst("!", ""));
				event.setFormat(ChatColor.RED + "[All] " + ChatColor.DARK_GREEN + "[Z] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
			} else {
				hashMapTeam.forEach((uuid, team) -> {
					if (team != PlayerTeam.ZOMBIE) return;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getUniqueId().equals(uuid)) {
							player.sendMessage(ChatColor.AQUA + "[チーム] " +  ChatColor.DARK_GREEN + "[Z] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
						}
					}
				});
				event.setCancelled(true);
			}
		} else if (hashMapTeam.get(event.getPlayer().getUniqueId()) == PlayerTeam.PLAYER) {
			if (event.getMessage().startsWith("!") || gameEnded || !gameStarted) {
				event.setMessage(event.getMessage().replaceFirst("!", ""));
				event.setFormat(ChatColor.RED + "[All] " + ChatColor.AQUA + "[P] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
			} else {
				hashMapTeam.forEach((uuid, team) -> {
					if (team != PlayerTeam.PLAYER) return;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getUniqueId().equals(uuid)) {
							player.sendMessage(ChatColor.AQUA + "[チーム] " +  ChatColor.AQUA + "[P] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
						}
					}
				});
				event.setCancelled(true);
			}
		} else { // Spectator
			if (event.getMessage().startsWith("!") || gameEnded || !gameStarted) {
				event.setMessage(event.getMessage().replaceFirst("!", ""));
				event.setFormat(ChatColor.RED + "[All] " + ChatColor.GRAY + "[S] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
			} else {
				hashMapTeam.forEach((uuid, team) -> {
					if (team != PlayerTeam.SPECTATOR) return;
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getUniqueId().equals(uuid)) {
							player.sendMessage(ChatColor.AQUA + "[チーム] " +  ChatColor.GRAY + "[S] " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
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
		Log.info("we're going to find players");
		for(Player p : loc.getWorld().getPlayers()){
			Log.info("team: " + hashMapTeam.get(p.getUniqueId()));
			Log.info("distance: " + loc.distance(p.getLocation()));
			if (!(hashMapTeam.get(p.getUniqueId()) == PlayerTeam.PLAYER)) continue;
			double distanceSqrd = loc.distance(p.getLocation());
			if (distanceSqrd > 7) continue;
			if(distanceSqrd < lastDistance){
				lastDistance = distanceSqrd;
				nearestPlayer = p;
			}
		}
		return nearestPlayer;
	}

	@SuppressWarnings("deprecation")
	public void endGame(String team) {
		gameEnded = true;
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + team + "チームの勝ち！", "");
			new BukkitRunnable() {
				public void run() {
					if (fireworked >= 80) this.cancel();
					player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
					Entity tnt = player.getWorld().spawn(player.getLocation(), TNTPrimed.class);
					((TNTPrimed)tnt).setFuseTicks(40);
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

	@SuppressWarnings("deprecation")
	public static void endGameStatic(String team) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
			player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + team + "チームの勝ち！", "");
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
