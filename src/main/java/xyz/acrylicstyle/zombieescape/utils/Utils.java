package xyz.acrylicstyle.zombieescape.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_core.utils.Log;
import xyz.acrylicstyle.zombieescape.PlayerTeam;
import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.exception.BooleanFormatException;

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
	 * Check a plugin if exists.
	 *
	 * This method is shorthand of <pre><code>return Bukkit.getPluginManager().getPlugin(plugin) != null;</code></pre>.
	 *
	 * @see #downloadPlugin(String, String)
	 * @param plugin Target plugin name
	 * @param url A url for download plugin if not exist
	 * @return True if exist, otherwize returns false.
	 */
	public static boolean checkPlugin(String plugin) {
		return Bukkit.getPluginManager().getPlugin(plugin) != null;
	}

	/**
	 * Check a plugin if exists, if does not exist, it'll download fresh plugin from provided url.
	 * <br>
	 * Similar as {@link #checkPlugin(String, String)} but it returns false if exist.
	 *
	 * @see #checkPlugin(String, String)
	 * @param plugin Target plugin name
	 * @param url A url for download plugin if not exist
	 * @return False if exist, true if it was does not exist and attempted to download plugin.
	 */
	public static boolean downloadPlugin(String plugin, String url) {
		if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
			Bukkit.getLogger().warning("[ZombieEscape] Does not exist " + plugin + ", downloading from " + url);
			AsyncDownload downloader = new AsyncDownload(plugin, url);
			if (!downloader.download()) {
				throw new IllegalArgumentException("Something is wrong with argument so we couldn't download file from specified URL.");
			}
			return true;
		} else return false;
	}

	/**
	 * Reloads all config.
	 */
	public static void reload() {
		ZombieEscape.config.reloadWithoutException();
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
		for(Player p : loc.getWorld().getPlayers()){
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
		for(Player p : loc.getWorld().getPlayers()){
			if (!(ZombieEscape.hashMapTeam.get(p.getUniqueId()) == PlayerTeam.PLAYER)) continue;
			double distanceSqrd = loc.distance(p.getLocation());
			if (distanceSqrd > 10) continue;
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
		chat(event, pteam, teamname, false);
	}

	public static void chat(AsyncPlayerChatEvent event, PlayerTeam pteam, String teamname, boolean alwaysAll) {
		event.setMessage(event.getMessage().replaceAll("<3", ChatColor.RED + "❤" + ChatColor.RESET));
		event.setMessage(event.getMessage().replaceAll(":peace:", ChatColor.GREEN + "✌" + ChatColor.RESET));
		if (event.getMessage().startsWith("!") || ZombieEscape.gameEnded || !ZombieEscape.gameStarted || alwaysAll) {
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
					progress.remove(eventId);
					bossbar.removeAll();
					this.cancel(); // probably already ended bossbar
				}
			}
		}.runTaskTimer(ZombieEscape.getProvidingPlugin(ZombieEscape.class), 0, 1);
	}

	public static Inventory initializeItems(Inventory inv) {
		File maps = new File("./plugins/ZombieEscape/maps/");
		File[] keys = maps.listFiles();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i].getName().replaceAll(".yml", "");
			ItemStack item = new ItemStack(Material.DIAMOND);
			List<String> lore = new ArrayList<String>();
			ConfigProvider map = null;
			try {
				map = new ConfigProvider("./plugins/ZombieEscape/maps/" + key + ".yml");
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + map.getString("mapname", "???"));
			lore.add(key);
			meta.setLore(lore);
			item.setItemMeta(meta);
			inv.setItem(i, item);
		}
		return inv;
	}

	public static ItemStack generateVoteItem() {
		ItemStack item = new ItemStack(Material.EMPTY_MAP);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "マップ投票");
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack generateResourcePackItem() {
		ItemStack item = new ItemStack(Material.CHEST);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "リソースパック");
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Check if array contains needle but not case-sensitive.
	 * @param array Haystack
	 * @param needle Needle
	 * @return true if found, otherwise false
	 */
	public static boolean includes(String[] array, String needle) {
		boolean found = false;
		for (String element : array) {
			if (element.equalsIgnoreCase(needle)) found = true;
		}
		return found;
	}

	/**
	 * Check if array contains needle and that element contains all of multiple needle but not case-sensitive.
	 * @param array Haystack
	 * @param needle Needle
	 * @return true if found, otherwise false
	 */
	public static boolean contains(String[] array, String... needle) {
		boolean found = false;
		for (String element : array) {
			found = true;
			for (String e : needle) {
				if (found) found = element.contains(e);
			}
		}
		return found;
	}

	/**
	 * Returns array element index that contains needle but not case-sensitive.<br>
	 * If there are multiple needles in haystack, returns first one.
	 * @param array Haystack
	 * @param needle Needle
	 * @return a number except -1 if found, returns -1 if not found
	 */
	public static int indexOf(String[] array, String needle) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equalsIgnoreCase(needle)) return i;
		}
		return -1;
	}

	public static boolean isInt(String arg) {
		try {
			Integer.parseInt(arg);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isDouble(String arg) {
		try {
			Double.parseDouble(arg);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean parseBoolean(String bool) throws BooleanFormatException {
		if (bool.equalsIgnoreCase("true")) return true;
		else if (bool.equalsIgnoreCase("false")) return false;
		else throw new BooleanFormatException("Provided string is not boolean.");
	}

	public static boolean isBoolean(String arg) {
		try {
			Utils.parseBoolean(arg);
			return true;
		} catch (BooleanFormatException e) {
			return false;
		}
	}

	public static boolean isFloat(String arg) {
		try {
			Float.parseFloat(arg);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static Object invokeMethodWithType(Class<?> clazz, String methodName, String arg1) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> class1 = null;
		String s = arg1;
		if (Utils.isInt(s)) {
			class1 = Integer.class;
		} else if (Utils.isBoolean(s)) {
			class1 = Boolean.class;
		} else if (Utils.isDouble(s)) {
			class1 = Double.class;
		} else if (Utils.isFloat(s)) {
			class1 = Float.class;
		} else {
			class1 = String.class;
		}
		Method method = clazz.getMethod(methodName, class1);
		if (Utils.isInt(s)) {
			return method.invoke(clazz, Integer.parseInt(s));
		} else if (Utils.isBoolean(s)) {
			return method.invoke(clazz, Boolean.parseBoolean(s));
		} else if (Utils.isDouble(s)) {
			return method.invoke(clazz, Double.parseDouble(s));
		} else if (Utils.isFloat(s)) {
			return method.invoke(clazz, Float.parseFloat(s));
		} else {
			return method.invoke(clazz, s);
		}
	}

	public static Object invokeMethodWithType(Class<?> clazz, String methodName, String arg1, String arg2) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> class1 = null;
		Class<?> class2 = null;
		String s = arg1;
		String t = arg2;
		if (Utils.isInt(s)) {
			class1 = Integer.class;
		} else if (Utils.isBoolean(s)) {
			class1 = Boolean.class;
		} else if (Utils.isDouble(s)) {
			class1 = Double.class;
		} else if (Utils.isFloat(s)) {
			class1 = Float.class;
		} else {
			class1 = String.class;
		}
		if (Utils.isInt(t)) {
			class2 = Integer.class;
		} else if (Utils.isBoolean(t)) {
			class2 = Boolean.class;
		} else if (Utils.isDouble(t)) {
			class2 = Double.class;
		} else if (Utils.isFloat(t)) {
			class2 = Float.class;
		} else {
			class2 = String.class;
		}
		Method method = clazz.getMethod(methodName, class1, class2);
		if (Utils.isInt(s)) {
			if (Utils.isInt(t)) {
				return method.invoke(clazz, Integer.parseInt(s), Integer.parseInt(t));
			} else if (Utils.isBoolean(t)) {
				return method.invoke(clazz, Integer.parseInt(s), Boolean.parseBoolean(t));
			} else if (Utils.isDouble(t)) {
				return method.invoke(clazz, Integer.parseInt(s), Double.parseDouble(t));
			} else if (Utils.isFloat(t)) {
				return method.invoke(clazz, Integer.parseInt(s), Float.parseFloat(t));
			} else {
				return method.invoke(clazz, Integer.parseInt(s), t);
			}
		} else if (Utils.isBoolean(s)) {
			if (Utils.isInt(t)) {
				return method.invoke(clazz, Boolean.parseBoolean(s), Integer.parseInt(t));
			} else if (Utils.isBoolean(t)) {
				return method.invoke(clazz, Boolean.parseBoolean(s), Boolean.parseBoolean(t));
			} else if (Utils.isDouble(t)) {
				return method.invoke(clazz, Boolean.parseBoolean(s), Double.parseDouble(t));
			} else if (Utils.isFloat(t)) {
				return method.invoke(clazz, Boolean.parseBoolean(s), Float.parseFloat(t));
			} else {
				return method.invoke(clazz, Boolean.parseBoolean(s), t);
			}
		} else if (Utils.isDouble(s)) {
			if (Utils.isInt(t)) {
				return method.invoke(clazz, Double.parseDouble(s), Integer.parseInt(t));
			} else if (Utils.isBoolean(t)) {
				return method.invoke(clazz, Double.parseDouble(s), Boolean.parseBoolean(t));
			} else if (Utils.isDouble(t)) {
				return method.invoke(clazz, Double.parseDouble(s), Double.parseDouble(t));
			} else if (Utils.isFloat(t)) {
				return method.invoke(clazz, Double.parseDouble(s), Float.parseFloat(t));
			} else {
				return method.invoke(clazz, Double.parseDouble(s), t);
			}
		} else if (Utils.isFloat(s)) {
			if (Utils.isInt(t)) {
				return method.invoke(clazz, Float.parseFloat(s), Integer.parseInt(t));
			} else if (Utils.isBoolean(t)) {
				return method.invoke(clazz, Float.parseFloat(s), Boolean.parseBoolean(t));
			} else if (Utils.isDouble(t)) {
				return method.invoke(clazz, Float.parseFloat(s), Double.parseDouble(t));
			} else if (Utils.isFloat(t)) {
				return method.invoke(clazz, Float.parseFloat(s), Float.parseFloat(t));
			} else {
				return method.invoke(clazz, Float.parseFloat(s), t);
			}
		} else {
			if (Utils.isInt(t)) {
				return method.invoke(clazz, s, Integer.parseInt(t));
			} else if (Utils.isBoolean(t)) {
				return method.invoke(clazz, s, Boolean.parseBoolean(t));
			} else if (Utils.isDouble(t)) {
				return method.invoke(clazz, s, Double.parseDouble(t));
			} else if (Utils.isFloat(t)) {
				return method.invoke(clazz, s, Float.parseFloat(t));
			} else {
				return method.invoke(clazz, s, t);
			}
		}
	}
}
