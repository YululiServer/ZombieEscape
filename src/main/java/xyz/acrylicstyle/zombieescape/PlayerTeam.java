package xyz.acrylicstyle.zombieescape;

public enum PlayerTeam {
	/**
	 * Represents ZOMBIE, returns "zombie" if called {@link #toString()}
	 */
	ZOMBIE,
	/**
	 * Represents PLAYER, returns "player" if called {@link #toString()}
	 */
	PLAYER;

	@Override
	public String toString() {
		switch (this) {
			case ZOMBIE: return "zombie";
			case PLAYER: return "player";
			default: return null;
		}
	}
}
