package xyz.acrylicstyle.zombieescape.utils;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class MinecraftProfile {
    public String username = null;
    public String uuid = null;

    public MinecraftProfile(String something, boolean uuid) {
        if (uuid) this.uuid = something;
    }

    public MinecraftProfile(UUID uuid) {
        this.uuid = uuid.toString();
    }

    public MinecraftProfile(String username) {
        this.username = username;
    }

    public MinecraftProfile setUUID(UUID uuid) {
        this.uuid = uuid.toString();
        return this;
    }

    public MinecraftProfile setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public MinecraftProfile setUsername(String username) {
        this.username = username;
        return this;
    }

    public UUID toUUID() throws IllegalArgumentException, IOException, ParseException {
        try {
            if (this.username == null) throw new IllegalArgumentException("Username must be set before call this method.");
            String url = "https://api.mojang.com/users/profiles/minecraft/" + this.username;
            @SuppressWarnings("deprecation")
            String nameJson = IOUtils.toString(new URL(url).openStream());
            JSONObject nameValue = (JSONObject) JSONValue.parseWithException(nameJson);
            return UUID.fromString(nameValue.get("id").toString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String toUsername() throws IllegalArgumentException, IOException, ParseException {
        try {
            if (this.uuid == null) throw new IllegalArgumentException("UUID must be set before call this method.");
            UUID.fromString(this.uuid); // Try parse
            String url = "https://api.mojang.com/user/profiles/" + this.uuid.replaceAll("-", "") + "/names";
            @SuppressWarnings("deprecation")
            String nameJson = IOUtils.toString(new URL(url).openStream());
            JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
            String playerSlot = nameValue.get(nameValue.size()-1).toString();
            JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
            return nameObject.get("name").toString();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
