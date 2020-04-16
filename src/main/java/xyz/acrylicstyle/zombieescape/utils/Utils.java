package xyz.acrylicstyle.zombieescape.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.WorldBorder;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_api.utils.Lang;
import xyz.acrylicstyle.tomeito_api.utils.Log;
import xyz.acrylicstyle.zombieescape.PlayerTeam;
import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.data.Constants;

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
        ZombieEscape.settingsCheck = ZombieEscape.mapConfig.getList("spawnPoints.zombie") != null // check if zombie spawn points exists
                && ZombieEscape.mapConfig.getList("spawnPoints.zombie").size() != 0 // check if zombie spawn points are *actually* exists(0 isn't exist)
                && ZombieEscape.mapConfig.getList("spawnPoints.player") != null // check if player spawn points exists
                && ZombieEscape.mapConfig.getList("spawnPoints.player").size() != 0
                && ZombieEscape.mapConfig.get("locationWall") != null; // if it's null, ProjectileHitEvent won't work!
    }

    /**
     * Check a plugin if exists.
     *
     * This method is shorthand of <pre><code>return Bukkit.getPluginManager().getPlugin(plugin) != null;</code></pre>.
     *
     * @param plugin Target plugin name
     * @return True if exist, otherwize returns false.
     */
    public static boolean checkPlugin(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
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
        ZombieEscape.defmapString = "    " + Lang.format(ZombieEscape.lang.get("defaultMap"), ChatColor.translateAlternateColorCodes('&', ZombieEscape.mapConfig.getString("mapname", "???")));
        ZombieEscape.language = new Lang("ZombieEscape");
        try {
            ZombieEscape.language.addLanguage("ja_JP");
            ZombieEscape.language.addLanguage("en_US");
        } catch (IOException | InvalidConfigurationException ignored) {} // ignore
        ZombieEscape.lang = ZombieEscape.language.get(ZombieEscape.config.getString("language", "en_US"));
    }

    public static void endGameStatic(String team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 100, 1);
            player.sendTitle(Lang.format(ZombieEscape.lang.get("gameEnd"), team), "", 0, 60, 20);
        }
        Bukkit.broadcastMessage(Lang.format(ZombieEscape.lang.get("gameEnd"), team));
        for (Player player : Bukkit.getOnlinePlayers()) {
            TimerTask task2 = new TimerTask() {
                public synchronized void run() {
                    if (ZombieEscape.fireworked >= 80) this.cancel();
                    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 100, 1);
                    TNTPrimed tnt = player.getWorld().spawn(player.getLocation(), TNTPrimed.class);
                    tnt.setFuseTicks(40);
                    ZombieEscape.fireworked++;
                }
            };
            Timer timer2 = new Timer();
            timer2.schedule(task2, 5);
        }
    }

    public static List<Player> targetAFindPlayersWithRange(Location loc, double range) {
        if (ZombieEscape.players <= 0) return null;
        List<Player> players = new ArrayList<>();
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
        event.setMessage(event.getMessage().replaceAll("<3", "" + ChatColor.RED + Constants.heart + ChatColor.RESET));
        event.setMessage(event.getMessage().replaceAll(":peace:", "" + ChatColor.GREEN + Constants.peace + ChatColor.RESET));
        if (event.getMessage().startsWith("!") || ZombieEscape.gameEnded || !ZombieEscape.gameStarted || alwaysAll) {
            if (event.getMessage().startsWith("!")) event.setMessage(event.getMessage().replaceFirst("!", ""));
            event.setFormat(ChatColor.RED + "[All] " + teamname + " " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
        } else {
            ZombieEscape.hashMapTeam.forEach((uuid, team) -> {
                if (team != pteam) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getUniqueId().equals(uuid)) {
                        player.sendMessage(ChatColor.AQUA + "[" + ZombieEscape.lang.get("team") + "] " + teamname + " " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
                    }
                }
            });
            event.setCancelled(true);
        }
    }

    private static Map<String, Double> progress = new HashMap<>();

    public static void doBossBarTick(BossBar bossbar, double countdownInSecond, String eventId) {
        Utils.doBossBarTick(bossbar, countdownInSecond, eventId, false);
    }

    public static void doBossBarTick(BossBar bossbar, double countdownInSecond, String eventId, boolean reverse) {
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
                    if (reverse) {
                        bossbar.setProgress((max-progress.get(eventId))/max);
                    } else {
                        bossbar.setProgress(progress.get(eventId)/max); // double / double => double
                    }
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
        assert keys != null;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i].getName().replaceAll(".yml", "");
            ItemStack item = new ItemStack(Material.DIAMOND);
            List<String> lore = new ArrayList<>();
            ConfigProvider map = null;
            try {
                map = new ConfigProvider("./plugins/ZombieEscape/maps/" + key + ".yml");
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            ItemMeta meta = item.getItemMeta();
            assert map != null;
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
        meta.setDisplayName(ChatColor.GREEN + ZombieEscape.lang.get("mapVote"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack generateResourcePackItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + ZombieEscape.lang.get("resourcepack"));
        item.setItemMeta(meta);
        return item;
    }

    public static void damageIfOutsideOfBorder() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ZombieEscape.hashMapTeam.get(player.getUniqueId()) == PlayerTeam.PLAYER) {
                if (isOutsideOfBorder(player)) player.damage(5.0);
            }
        }
    }

    public static boolean isOutsideOfBorder(Player player) {
        Location location = player.getLocation();
        WorldBorder border = player.getWorld().getWorldBorder();
        double x = location.getX() - border.getCenter().getX();
        double z = location.getZ() - border.getCenter().getZ();
        double size = border.getSize()/2;
        return ((x > size || (-x) > size) || (z > size || (-z) > size));
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    public static void teleport(Player player) {
        String team = ZombieEscape.hashMapTeam.get(player.getUniqueId()).toString();
        String[] spawnLists = Arrays.asList(ZombieEscape.mapConfig.getList("spawnPoints." + team, new ArrayList<String>()).toArray(new String[0])).get(0).split(",");
        Location location = new Location(Bukkit.getWorld(ZombieEscape.mapConfig.getString("spawnPoints.world")), Double.parseDouble(spawnLists[0]), Double.parseDouble(spawnLists[1]), Double.parseDouble(spawnLists[2]));
        if (!player.teleport(location)) player.sendMessage(ZombieEscape.lang.get("failedWarp"));
    }

    public static void teleportAllPlayers() {
        int time = 0; // 20 ticks = 1 second, 2 ticks = 0.1 second
        for (Player player : Bukkit.getOnlinePlayers()) {
            new BukkitRunnable() {
                public void run() {
                    Utils.teleport(player);
                }
            }.runTaskLater(ZombieEscape.getProvidingPlugin(ZombieEscape.class), time);
            time = time + 2;
        }
    }
}
