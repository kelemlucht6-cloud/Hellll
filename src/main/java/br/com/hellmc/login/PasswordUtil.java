package br.com.hellmc.login;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
public final class PasswordUtil {
 private static final SecureRandom R=new SecureRandom();
 private PasswordUtil(){}
 public static String salt(){byte[] b=new byte[16];R.nextBytes(b);return Base64.getEncoder().encodeToString(b);}
 public static String hash(String p,String s)throws Exception{PBEKeySpec x=new PBEKeySpec(p.toCharArray(),Base64.getDecoder().decode(s),210000,256);try{return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(x).getEncoded());}finally{x.clearPassword();}}
 public static boolean verify(String p,String s,String expected)throws Exception{return MessageDigest.isEqual(Base64.getDecoder().decode(hash(p,s)),Base64.getDecoder().decode(expected));}
}
