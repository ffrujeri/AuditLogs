import sun.nio.ByteBuffered;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MerkleTree {
	private byte[] hash;
	private MerkleTree left, right;
	private int beginningIndex, endingIndex;
	
	public MerkleTree(String event){
		hash = getHash(event);
	}

	public MerkleTree(MerkleTree left, MerkleTree right){
		this.left = left;
		this.right = right;
		hash = concatenateHash(left.getHash(), right.getHash());
	}

	private byte[] getHash(String text){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] h = digest.digest(text.getBytes("UTF-8"));
			return h;
		}catch(NoSuchAlgorithmException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch(UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private byte[] getHash(byte[] hash){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] h = digest.digest(hash);
			return h;
		}catch(NoSuchAlgorithmException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch(UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getHash(){
		return hash;
	}

	private static byte[] concatenateHash(byte[] h1, byte[] h2){
		byte[] hash = new hash[h1.length+h2.length];
		for (int i = 0; i < h1.length; i++)
			hash[i]=h1[i];
		for (int i = 0; i <h2.length ; i++)
			hash[h1.length+i]=h2[i];
	}
}
