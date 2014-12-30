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
		// TODO: compute hash;
	}

	public MerkleTree(MerkleTree left, MerkleTree right){
		this.left = left;
		this.right = right;
		hash = getHash(left.getHash() + right.getHash());
		// TODO: compute hash (concatenates hashes and hash result)
	}
	
	private byte[] getHash(String text){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] h = digest.digest(text.getBytes("UTF-8"));
			return h.toString(); // TODO: correct
		}catch(NoSuchAlgorithmException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch(UnsupportedEncodingException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getHash(){
		return hash;
	}
	
	// TODO: tests
	public static void main(String[] args) {
		byte[] b = {3, 5, 1, 0};
		System.out.println(b.toString());
	}
}
