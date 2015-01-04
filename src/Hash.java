import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	private byte[] value;
	
	public Hash(byte[] hashValue){
		this.value = hashValue;
	}
	
	public static Hash concatenateAndHash(Hash h1, Hash h2){
		byte[] v1 = h1.value, v2 = h2.value, 
			   hashValue = new byte[v1.length + v2.length];
		for (int i = 0; i < v1.length; i++)
			hashValue[i] = v1[i];
		for (int i = 0; i < v2.length ; i++)
			hashValue[v1.length+i] = v2[i];
		
		return getHash(hashValue);
	}

	public static Hash getHash(String text){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashValue = digest.digest(text.getBytes("UTF-8"));
			return new Hash(hashValue);
		}catch(NoSuchAlgorithmException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch(UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	private static Hash getHash(byte[] h){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashValue = digest.digest(h);
			return new Hash(hashValue);
		}catch(NoSuchAlgorithmException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public byte[] getHashValue(){
		return value;
	}

	@Override
	public boolean equals(Object h){
		byte[] value2 = ((Hash) h).getHashValue();
		if (value.length != value2.length)
			return false;

		for (int i = 0; i < value.length; i++) {
			if (value[i] != value2[i])
				return false;
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		return ""+value;
	}
	
}
