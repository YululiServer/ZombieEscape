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
	OYACHU,
	SAND;

	@Override
	public String toString() {
		return super.toString().toLowerCase(Locale.ROOT);
	}
}
