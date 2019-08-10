package xyz.acrylicstyle.zombieescape.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_core.utils.Log;
import xyz.acrylicstyle.zombieescape.PlayerTeam;
import xyz.acrylicstyle.zombieescape.ZombieEscape;

public final class Utils {
	private Utils() {}

	/**
	 * @param sender CommandSender
	 * @return false if they're not player, true if they're player
	 */
	public static boolean senderCheck(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command must be run from in-game.");
		}
		return sender instanceof Player;
	}

	/**
	 * @param <T>
	 * @param sender CommandSender
	 * @return false if they're not player, true if they're player
	 */
	public static <T extends CommandSender> boolean senderCheck(CommandSender sender, Class<T> trueIf) {
		if (!(trueIf.isInstance(sender))) {
			sender.sendMessage(ChatColor.RED + "This command must be run from in-game.");
			return false;
		} else return true;
	}

	/**
	 * Reload config and check if all configs are set correctly.<br>
	 * If all checks passed, settingsCheck will be true. Otherwise it'll set to false.
	 */
	public static void checkConfig() {
		ZombieEscape.config.reloadWithoutException();
		ZombieEscape.mapConfig.reloadWithoutException();
		if (ZombieEscape.mapConfig.getList("spawnPoints.zombie") != null // check if zombie spawn points exists
				&& ZombieEscape.mapConfig.getList("spawnPoints.zombie").size() != 0 // check if zombie spawn points are *actually* exists(0 isn't exist)
				&& ZombieEscape.mapConfig.getList("spawnPoints.player") != null // check if player spawn points exists
				&& ZombieEscape.mapConfig.getList("spawnPoints.player").size() != 0
				&& ZombieEscape.mapConfig.get("locationWall") != null) ZombieEscape.settingsCheck = true; // if it's null, ProjectileHitEvent won't work!
		else ZombieEscape.settingsCheck = false;
	}

	/**
	 * Reloads all config.
	 */
	public static void reload() {
		ZombieEscape.config.reloadWithoutException();
		ZombieEscape.mapName = ZombieEscape.config.getString("map", "world");
		try {
			ZombieEscape.mapConfig = new ConfigProvider("./plugins/ZombieEscape/maps/" + ZombieEscape.mapName + ".yml");
		} catch (IOException | InvalidConfigurationException e) {
			Log.error("Couldn't read config: maps/" + ZombieEscape.mapName + ".yml");
			e.printStackTrace();
		}
		ZombieEscape.locationWall = ConfigProvider.getConfigSectionValue(ZombieEscape.mapConfig.get("locationWall", new HashMap<String, Object>()), true);
		ZombieEscape.maxCheckpoints = Math.min(ZombieEscape.mapConfig.getStringList("spawnPoints.player").size(), ZombieEscape.mapConfig.getStringList("spawnPoints.zombie").size());
		ZombieEscape.debug = ZombieEscape.config.getBoolean("debug", false);
	}

	public static void endGameStatic(String team) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 100, 1);
			player.sendTitle("" + ChatColor.GREEN + ChatColor.BOLD + team + "チームの勝ち！", "", 0, 60, 20);
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
		for (Player player : Bukkit.getOnlinePlayers()) {
			TimerTask task2 = new TimerTask() {
				public synchronized void run() {
					if (ZombieEscape.fireworked >= 80) this.cancel();
					player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 100, 1);
					Entity tnt = player.getWorld().spawn(player.getLocation(), TNTPrimed.class);
					((TNTPrimed)tnt).setFuseTicks(40);
					ZombieEscape.fireworked++;
				}
			};
			Timer timer2 = new Timer();
			timer2.schedule(task2, 5);
		}
	}

	public static List<Player> targetAFindPlayersWithRange(Location loc, double range) {
		if (ZombieEscape.players <= 0) return null;
		List<Player> players = new ArrayList<Player>();
		Log.info("we're going to find players");
		for(Player p : loc.getWorld().getPlayers()){
			Log.info("team: " + ZombieEscape.hashMapTeam.get(p.getUniqueId()));
			Log.info("distance: " + loc.distance(p.getLocation()));
			if (ZombieEscape.hashMapTeam.get(p.getUniqueId()) != PlayerTeam.PLAYER) continue;
			double distanceSqrd = loc.distance(p.getLocation());
			if (distanceSqrd > range) continue;
			players.add(p);
		}
		return players;
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
		if (ZombieEscape.players <= 0) return null;
		Player nearestPlayer = null;
		double lastDistance = Double.MAX_VALUE;
		Log.info("we're going to find players");
		for(Player p : loc.getWorld().getPlayers()){
			Log.info("team: " + ZombieEscape.hashMapTeam.get(p.getUniqueId()));
			Log.info("distance: " + loc.distance(p.getLocation()));
			if (!(ZombieEscape.hashMapTeam.get(p.getUniqueId()) == PlayerTeam.PLAYER)) continue;
			double distanceSqrd = loc.distance(p.getLocation());
			if (distanceSqrd > 7) continue;
			if(distanceSqrd < lastDistance){
				lastDistance = distanceSqrd;
				nearestPlayer = p;
			}
		}
		return nearestPlayer;
	}

	public static ItemStack createLeatherItemStack(Material material, int red, int green, int blue) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
		lam.setColor(Color.fromRGB(red, green, blue));
		item.setItemMeta(lam);
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
		return item;
	}

	public static void chat(AsyncPlayerChatEvent event, PlayerTeam pteam, String teamname) {
		event.setMessage(event.getMessage().replaceAll("<3", ChatColor.RED + "❤" + ChatColor.RESET));
		event.setMessage(event.getMessage().replaceAll(":peace:", ChatColor.GREEN + "✌" + ChatColor.RESET));
		if (event.getMessage().startsWith("!") || ZombieEscape.gameEnded || !ZombieEscape.gameStarted) {
			if (event.getMessage().startsWith("!")) event.setMessage(event.getMessage().replaceFirst("!", ""));
			event.setFormat(ChatColor.RED + "[All] " + teamname + " " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
		} else {
			ZombieEscape.hashMapTeam.forEach((uuid, team) -> {
				if (team != pteam) return;
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.getUniqueId().equals(uuid)) {
						player.sendMessage(ChatColor.AQUA + "[チーム] " + teamname + " " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
					}
				}
			});
			event.setCancelled(true);
		}
	}

	private static Map<String, Double> progress = new HashMap<String, Double>();

	public static void doBossBarTick(BossBar bossbar, double countdownInSecond, String eventId) {
		final double max = 20 * countdownInSecond;
		progress.put(eventId, max);
		for (Player player : Bukkit.getOnlinePlayers()) bossbar.addPlayer(player);
		new BukkitRunnable() {
			public void run() {
				try {
					if (progress.get(eventId) <= 0) {
						progress.remove(eventId);
						bossbar.removeAll();
						this.cancel();
						return;
					}
					bossbar.setTitle(ChatColor.AQUA + ZombieEscape.ongoingEventMap.get(eventId));
					bossbar.setProgress(progress.get(eventId)/max); // double / double => double
					progress.put(eventId, progress.get(eventId)-1);
				} catch(Exception e) {
					e.printStackTrace();
					e.getCause().printStackTrace();
					progress.remove(eventId);
					bossbar.removeAll();
					this.cancel(); // probably already ended bossbar
				}
			}
		}.runTaskTimer(ZombieEscape.getProvidingPlugin(ZombieEscape.class), 0, 1);
	}
}
