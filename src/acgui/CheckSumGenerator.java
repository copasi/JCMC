package acgui;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSumGenerator
{
	public static String generate(String fileName) throws NoSuchAlgorithmException, IOException
	{
		FileInputStream input;
		MessageDigest messageDigest;
		int bytesRead;
		byte[] digestBytes;
		StringBuffer sb;
		
		input = new FileInputStream(fileName);
		byte[] dataBytes = new byte[1024];
		
		messageDigest = MessageDigest.getInstance("MD5");
		
		bytesRead = 0;
		
		while ((bytesRead = input.read(dataBytes)) != -1)
		{
			messageDigest.update(dataBytes, 0, bytesRead);
		}
		
		digestBytes = messageDigest.digest();
		
		sb = new StringBuffer("");
		for (int i = 0; i < digestBytes.length; i++)
		{
			sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		
		input.close();
		
		return sb.toString();
	}
}
