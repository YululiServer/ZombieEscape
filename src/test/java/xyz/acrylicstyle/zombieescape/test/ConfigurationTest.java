package xyz.acrylicstyle.zombieescape.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.Assert;
import org.junit.Test;

import xyz.acrylicstyle.zombieescape.providers.ConfigProvider;

public class ConfigurationTest {
	@Test
	public void setThenSave() throws FileNotFoundException, IOException, InvalidConfigurationException {
		ConfigProvider config = new ConfigProvider("./test.yml");
		if (config.file.exists()) config.file.delete();
		config.reload();
		config.setThenSave("test.a.deep.so.deep.tree.of.the.faith", "L :maths:");
		config.reload();
		assertEquals("Checks if it deleted file and created file, then set then save.", "L :maths:", config.getString("test.a.deep.so.deep.tree.of.the.faith"));
	}

	@Test
	public void save() throws FileNotFoundException, IOException, InvalidConfigurationException { // Error/exception on save shouldn't happen, so I made this test.
		ConfigProvider config = new ConfigProvider("./test.yml");
		config.save();
		assertEquals("Checks if ConfigProvider is doing load & save correctly", true, true); // always true, but stop on exception so it's no problem.
	}

	private void assertEquals(String string, boolean b, boolean c) {
		Assert.assertEquals(string, (Object) b, (Object) c);

	}

	private void assertEquals(String string, String string2, String string3) {
		Assert.assertEquals(string, (Object) string2, (Object) string3);
	}
}
