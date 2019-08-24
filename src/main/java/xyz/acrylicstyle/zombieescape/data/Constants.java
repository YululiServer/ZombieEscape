package xyz.acrylicstyle.zombieescape.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;

public class Constants {
	/**
	 * Represents material(block) durability
	 */
	public final static HashMap<Material, Integer> materialDurability = new HashMap<Material, Integer>();
	public final static String version = "v1.1";
	public final static String instanceIdentifier;
	public final static Set<Material> breakableWall = new HashSet<Material>(); // applies to only players
	public final static String requiredMinecraftVersion = "1.12.2";
	public static int mininumPlayers = 2;
	public final static char warning = '\u26a0';
	public final static char heavy_check_mark = '\u2714';
	public final static char heart_suit = '\u2665';
	public final static char heart = '\u2764';
	public final static char heart_exclamation = '\u2763';
	public final static char peace = '\u270c';

	static {
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
		Random random = new Random();
		char identifier;
		switch(random.nextInt(5)) {
			case 0: identifier = heavy_check_mark;
			case 1: identifier = heart_suit;
			case 2: identifier = heart;
			case 3: identifier = heart_exclamation;
			case 4: identifier = peace;
			default: identifier = warning;
		}
		instanceIdentifier = Integer.toString(random.nextInt(100000)) + identifier;
	}
}
