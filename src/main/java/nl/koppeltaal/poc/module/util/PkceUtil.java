package nl.koppeltaal.poc.module.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 *
 */
public class PkceUtil {

	public static String generateCodeVerifier() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String generateCodeChallenge(String codeVerifier){
		try {
			byte[] bytes = codeVerifier.getBytes("US-ASCII");
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(bytes, 0, bytes.length);
			//TODO: Check if the padding should be removed or not
			return Base64.getUrlEncoder().encodeToString(digest.digest());
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
