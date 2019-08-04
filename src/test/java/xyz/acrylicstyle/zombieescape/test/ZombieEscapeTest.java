package xyz.acrylicstyle.zombieescape.test;

import org.bukkit.Material;
import org.junit.Assert;
import org.junit.Test;

import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.data.Constants;

public class ZombieEscapeTest {
	@Test(expected=NullPointerException.class)
	public void endGame() {
		if (ZombieEscape.gameEnded) throw new IllegalStateException("Game is already ended");
		ZombieEscape.endGameStatic("Zombie");
	}

	@Test
	public void durabilityTest() {
		int players = 5;
		Assert.assertEquals("Is wood wall durability is 100 if 5 players", 100, (int) Math.nextUp(Math.min(Constants.materialDurability.getOrDefault(Material.WOOD, 5)*((double)players/(double)5), 1000)));
	}

	@Test
	public void durabilityTest1000() {
		int players = 1;
		Assert.assertEquals("Is wood wall durability is 1000 if 1 player", 1000, (int) Math.nextUp(Math.min(Constants.materialDurability.getOrDefault(Material.BEDROCK, 5)*((double)players/(double)5), 1000)));
	}
}
