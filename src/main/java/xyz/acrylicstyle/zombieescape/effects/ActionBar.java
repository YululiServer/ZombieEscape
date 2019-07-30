package xyz.acrylicstyle.zombieescape.effects;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class ActionBar {
	ProtocolManager protocolManager = null;

	public ActionBar() {
		protocolManager = ProtocolLibrary.getProtocolManager();
	}

	public void setActionBar(Player player, String text) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.CHAT);
		packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
		packet.getBytes().write(0, (byte) 2);
		try {
			protocolManager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static void setActionBarWithoutException(Player player, String text) {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		PacketContainer packet = manager.createPacket(PacketType.Play.Server.CHAT);
		packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
		packet.getBytes().write(0, (byte) 2);
		try {
			manager.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			// we shouldn't ignore it
		}
	}
}
