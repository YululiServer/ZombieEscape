package xyz.acrylicstyle.zombieescape.config;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class Config {
	/**
	* Returns a {@link Map} representative of the passed Object that represents
	* a section of a YAML file. This method neglects the implementation of the
	* section (whether it be {@link ConfigurationSection} or just a
	* {@link Map}), and returns the appropriate value.
	*
	* @since 0.1.0
	* @version 0.1.0
	*
	* @param o The object to interpret
	* @param deep If an object is a {@link ConfigurationSection}, {@code true} to do a deep search
	* @return A {@link Map} representing the section
	*/
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getConfigSectionValue(Object o, boolean deep) {
	    if (o == null) {
	        return null;
	    }
	    Map<String, Object> map;
	    if (o instanceof ConfigurationSection) {
	        map = ((ConfigurationSection) o).getValues(deep);
	    } else if (o instanceof Map) {
	        map = (Map<String, Object>) o;
	    } else {
	        return null;
	    }
	    return map;
	}
}