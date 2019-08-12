package xyz.acrylicstyle.zombieescape;

import java.util.Locale;

public enum PlayerTeam {
	ZOMBIE("ゾンビ"),
	PLAYER("プレイヤー"),
	SPECTATOR("スペクテイター");

	@Override
	public final String toString() {
		return this.name;
	}

	private PlayerTeam(String stringName) {
		this.name = stringName;
	}

	private PlayerTeam() {
		this.name = this.toString().toLowerCase(Locale.ROOT);
	}

	private final String name;
}
