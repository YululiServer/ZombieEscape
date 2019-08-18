package xyz.acrylicstyle.zombieescape.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.json.simple.parser.ParseException;

import net.md_5.bungee.api.ChatColor;
import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_core.providers.LanguageProvider;
import xyz.acrylicstyle.tomeito_core.utils.Lang;
import xyz.acrylicstyle.zombieescape.ZombieEscape;
import xyz.acrylicstyle.zombieescape.utils.PlayerUtils;

public final class Sponsor {
	public final ConfigProvider config;
	public final LanguageProvider lang;

	public Sponsor() throws IOException, InvalidConfigurationException {
		config = new ConfigProvider("./plugins/ZombieEscape/config.yml");
		lang = ZombieEscape.lang;
	}

	public final class SetSponsor implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /setsponsor <" + lang.get("player") + ">");
				return true;
			}
			config.reloadWithoutException();
			UUID uuid = null;
			try {
				uuid = PlayerUtils.getByName(args[0]).toUUID();
			} catch (IllegalArgumentException | IOException | ParseException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorFetchingPlayer"));
				return true;
			}
			if (uuid == null) {
				sender.sendMessage(ChatColor.RED + lang.get("couldntFindPlayer") + ": " + args[0]);
				return true;
			}
			List<String> sponsors = new ArrayList<String>();
			sponsors.addAll(Arrays.asList(config.getList("sponsors", new ArrayList<String>()).toArray(new String[0])));
			if (sponsors.contains(uuid.toString())) {
				sender.sendMessage(lang.get("alreadySponsor"));
				return true;
			}
			sponsors.add(uuid.toString());
			try {
				config.setThenSave("sponsors", sponsors);
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorSavingConfig"));
				return true;
			}
			sender.sendMessage(Lang.format(lang.get("addedSponsor"), args[0]));
			return true;
		}
	}

	public final class RemoveSponsor implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + lang.get("usage") + ": /removesponsor <" + lang.get("player") + ">");
				return true;
			}
			config.reloadWithoutException();
			UUID uuid = null;
			try {
				uuid = PlayerUtils.getByName(args[0]).toUUID();
			} catch (IllegalArgumentException | IOException | ParseException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorFetchingPlayer"));
				return true;
			}
			if (uuid == null) {
				sender.sendMessage(ChatColor.RED + lang.get("couldntFindPlayer") + ": " + args[0]);
				return true;
			}
			List<String> sponsors = new ArrayList<String>();
			sponsors.addAll(Arrays.asList(config.getList("sponsors", new ArrayList<String>()).toArray(new String[0])));
			if (!sponsors.contains(uuid.toString())) {
				sender.sendMessage(lang.get("notSponsor"));
				return true;
			}
			sponsors.remove(uuid.toString());
			try {
				config.setThenSave("sponsors", sponsors);
			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(lang.get("errorSavingConfig"));
				return true;
			}
			sender.sendMessage(Lang.format(lang.get("removedSponsor"), args[0]));
			return true;
		}
	}
}
