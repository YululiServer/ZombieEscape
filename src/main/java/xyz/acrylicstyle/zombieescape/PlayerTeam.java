package xyz.acrylicstyle.zombieescape;

import java.util.Locale;

public enum PlayerTeam {
	/**
	 * Represents ZOMBIE, returns "zombie" if called {@link #toString()}
	 */
	ZOMBIE,
	/**
	 * Represents PLAYER, returns "player" if called {@link #toString()}
	 */
	PLAYER,
	/**
	 * Represents SPECTATOR mode, returns "spectator" if called {@link #toString()}
	 */
	SPECTATOR;

	@Override
	public String toString() {
		return super.toString().toLowerCase(Locale.ROOT);
	}
}
