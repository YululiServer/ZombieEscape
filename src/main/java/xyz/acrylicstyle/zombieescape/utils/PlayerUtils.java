package xyz.acrylicstyle.zombieescape.utils;

import java.util.UUID;

public class PlayerUtils {
	/**
	 * @param something UUID or username.
	 * @param uuid Is "something" uuid or not
	 */
	public static MinecraftProfile getBySomething(String something, boolean uuid) {
		return new MinecraftProfile(something, uuid);
	}

	public static MinecraftProfile getByName(String username) {
		return new MinecraftProfile(username);
	}

	public static MinecraftProfile getByUUID(UUID uuid) {
		return new MinecraftProfile(uuid);
	}
}
