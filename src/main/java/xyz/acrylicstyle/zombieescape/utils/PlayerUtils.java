package xyz.acrylicstyle.zombieescape.utils;

public class PlayerUtils {
	public static MinecraftProfile getByName(String username) {
		return new MinecraftProfile(username);
	}
}
