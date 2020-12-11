package br.com.p2p.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;

import br.com.p2p.models.*;

public class FileUtil {
	public static byte[] ReadFileChunk(String file, long offset, int len) throws IOException {
		try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
			byte[] buffer = new byte[len];
			f.seek(offset);
			f.read(buffer);
			return buffer;
		}
	}
	
	public static void AllocateFile(String file, long len) throws IOException {
		try(RandomAccessFile f = new RandomAccessFile(file, "rw")){
			f.setLength(len);
		}
	}
	
	public static void WriteFileChunk(String file, byte[] data, long offset) throws IOException {
		try (RandomAccessFile f = new RandomAccessFile(file, "rw")) {
			f.seek(offset);
			f.write(data);
		}
	}
	
	public static ArrayList<FileData> GetFilesData(String path) {
		ArrayList<FileData> results = new ArrayList<FileData>();
		File[] files = new File(path).listFiles();
		if(files != null) {
			for (File file : files) {
				if (file.isFile()) {
					FileData f = new FileData();
					f.Name = file.getName();
					f.Size = file.length();
					f.Hash = FileUtil.GetSha256OfFile(file.getPath());
					results.add(f);
				}
			}
		}

		return results;
	}
	
	public static String GetMd5OfFile(String filePath) {
		String returnVal = "";
		try {
			InputStream input = new FileInputStream(filePath);
			byte[] buffer = new byte[1024];
			MessageDigest md5Hash = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while (numRead != -1) {
				numRead = input.read(buffer);
				if (numRead > 0)
					md5Hash.update(buffer, 0, numRead);
			}
			input.close();

			byte[] md5Bytes = md5Hash.digest();
			for (int i = 0; i < md5Bytes.length; i++)
				returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return returnVal.toUpperCase();
	}
	
	public static String GetSha256OfFile(String filePath) {
		String returnVal = "";
		try {
			InputStream input = new FileInputStream(filePath);
			byte[] buffer = new byte[1024];
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			int numRead = 0;
			while (numRead != -1) {
				numRead = input.read(buffer);
				if (numRead > 0)
					digest.update(buffer, 0, numRead);
			}
			input.close();

			byte[] md5Bytes = digest.digest();
			for (int i = 0; i < md5Bytes.length; i++)
				returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return returnVal.toUpperCase();
	}

	public static String ReadAllTextFile(String path) throws IOException {
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		return new String(data, "UTF-8");
	}
}