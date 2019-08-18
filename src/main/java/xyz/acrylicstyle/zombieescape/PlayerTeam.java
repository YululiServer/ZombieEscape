package xyz.acrylicstyle.zombieescape;

import java.util.Locale;

public enum PlayerTeam {
	ZOMBIE,
	PLAYER,
	SPECTATOR;

	@Override
	public final String toString() {
		return super.toString().toLowerCase(Locale.ROOT);
	}
}
