package xyz.acrylicstyle.zombieescape;

import java.util.Locale;

public enum PlayerTeam {
	ZOMBIE,
	PLAYER,
	SPECTATOR;

	@Override
	public final String toString() {
		return this.name;
	}

	private PlayerTeam() {
		this.name = this.toString().toLowerCase(Locale.ROOT);
	}

	private final String name;
}
