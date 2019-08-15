package xyz.acrylicstyle.zombieescape.test;

import org.junit.Test;

import xyz.acrylicstyle.zombieescape.exception.BooleanFormatException;
import xyz.acrylicstyle.zombieescape.utils.Utils;

public class UtilTest {
	private final static String not = "Not anything";
	private final static String itsInt = "12345";
	private final static String itsDouble = "12345.67";
	private final static String itsFloat = "123.4567890";
	private final static String sTrue = "tRuE";
	private final static String sFalse = "fAlSe";

	private void throwNFE() {
		throw new NumberFormatException("It isn't correct format");
	}

	@Test
	public void isInt() {
		if (!Utils.isInt(itsInt)) throwNFE();
	}

	@Test(expected=NumberFormatException.class)
	public void isntIntItsDouble() {
		if (!Utils.isInt(itsDouble)) throwNFE();
	}

	@Test(expected=NumberFormatException.class)
	public void isntInt() {
		if (!Utils.isInt(not)) throwNFE();
	}

	@Test
	public void isDouble() {
		if (!Utils.isDouble(itsDouble)) throwNFE();
	}

	@Test
	public void isDoubleButInt() {
		if (!Utils.isDouble(itsInt)) throwNFE();
	}

	@Test(expected=NumberFormatException.class)
	public void isntDouble() {
		if (!Utils.isDouble(not)) throwNFE();
	}

	@Test
	public void parseBoolean() throws BooleanFormatException {
		Utils.parseBoolean(sTrue);
		Utils.parseBoolean(sFalse);
	}

	@Test(expected=BooleanFormatException.class)
	public void parseBooleanButNot() throws BooleanFormatException {
		Utils.parseBoolean(not);
	}

	@Test
	public void parseFloat() {
		if (!Utils.isDouble(itsFloat)) throwNFE();
	}

	@Test(expected=NumberFormatException.class)
	public void parseFloatButNot() {
		if (!Utils.isDouble(not)) throwNFE();
	}
}
