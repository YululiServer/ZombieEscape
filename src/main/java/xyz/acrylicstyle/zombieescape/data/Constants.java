package xyz.acrylicstyle.zombieescape.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

import xyz.acrylicstyle.zombieescape.ZombieEscape;

public class Constants {
	/**
	 * Represents material(block) durability
	 */
	public final static HashMap<Material, Integer> materialDurability = new HashMap<Material, Integer>();
	public final static Set<Material> breakableWall = new HashSet<Material>(); // applies to only players
	public final static String requiredMinecraftVersion = "1.12.2";
	public final static int mininumPlayers;
	public final static char heart = '\u2764';
	public final static char peace = '\u270c';

	static {
		mininumPlayers = ZombieEscape.config.getInt("mininumPlayers", 2);
		materialDurability.put(Material.DIRT, 120);
		materialDurability.put(Material.GRASS, 120);
		materialDurability.put(Material.WOOD, 150);
		materialDurability.put(Material.LAPIS_BLOCK, 300);
		materialDurability.put(Material.COBBLESTONE, 50000000);
		materialDurability.put(Material.BEDROCK, 50000000);
		materialDurability.put(Material.COMMAND, 50000000);
		breakableWall.add(Material.WOOD);
		breakableWall.add(Material.DIRT);
		breakableWall.add(Material.GRASS);
		breakableWall.add(Material.LAPIS_BLOCK);
	}
}
