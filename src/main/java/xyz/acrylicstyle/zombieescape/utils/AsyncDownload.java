package xyz.acrylicstyle.zombieescape.utils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncDownload extends Thread {
	public boolean result = false;
	private String file = null;
	private String url = null;

	public AsyncDownload(String afile, String aurl) {
		file = afile;
		url = aurl;
	}

	public synchronized void run() {
		this.download();
	}

	public synchronized boolean download() {
		try {
			URL url2 = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
			conn.setAllowUserInteraction(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod("GET");
			conn.connect();
			DataInputStream dataInStream = new DataInputStream(conn.getInputStream());
			DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("plugins/" + file + ".jar")));
			byte[] b = new byte[4096];
			int readByte = 0;
			while(-1 != (readByte = dataInStream.read(b))) {
				dataOutStream.write(b, 0, readByte);
			}
			dataInStream.close(); dataOutStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			this.result = false;
			return false;
		}
		this.result = true;
		return true;
	}
}
