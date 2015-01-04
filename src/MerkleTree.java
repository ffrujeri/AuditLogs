
package audit_logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class MerkleTree {
	private Hash hash;
	private MerkleTree left, right;
	private int beginIndex, endIndex;
	
	public MerkleTree(String event, int beginIndex, int endIndex){
		hash = Hash.getHash(event);
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}

	public MerkleTree(MerkleTree left, MerkleTree right){
		this.left = left;
		this.right = right;
		hash = Hash.concatenateAndHash(left.hash, right.hash);
		beginIndex = left.beginIndex;
		endIndex = right.endIndex;
	}

	public int getBeginIndex(){
		return beginIndex;
	}

	public int getEndIndex(){
		return endIndex;
	}

	public Hash getHash(){
		return hash;
	}



	//-----------------------------//-----------------------------//
	// Compute merkle tree from txt
	//-----------------------------//-----------------------------//
	
	/* Takes an input text file and computes its Merkle tree (treating each line 
	 * as a new event). Works with large (>1GB) text files.
	 */
	public static MerkleTree computeMerkleTree(File file){
		Queue<MerkleTree> currentLevel = new LinkedList<MerkleTree>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			for(long i = 1; (line = br.readLine()) != null; i++){
				currentLevel.add(new MerkleTree(line, (int) i, (int) i));
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		for(Queue<MerkleTree> levelUp = new LinkedList<MerkleTree>(); levelUp.size() != 1; currentLevel = levelUp){
			levelUp = new LinkedList<MerkleTree>();
			while(currentLevel.size() > 0){
				MerkleTree left = currentLevel.poll(), right = currentLevel.poll();
				if (right != null)
					levelUp.add(new MerkleTree(left, right));
				else levelUp.add(left);
			}
		}
		
		return currentLevel.poll();
	}



	//-----------------------------//-----------------------------//
	// Verify membership
	//-----------------------------//-----------------------------//
	
	public static ArrayList<Hash> genPath(MerkleTree root, int i){
		ArrayList<Hash> verificationPath = new ArrayList<Hash>();
		MerkleTree node = root;
		while(node.beginIndex != node.endIndex){
			if(node.left.endIndex >= i){
				verificationPath.add(node.right.getHash());
				node = node.left;
			}else{
				verificationPath.add(node.left.getHash());
				node = node.right;
			}
		
		Collections.reverse(verificationPath);
		}
		
		return verificationPath;
	}

	public static boolean verifyPath(Hash rootHash, String s, int i, Hash[] verificationPath){
		Hash current = Hash.getHash(s);
		for (int j = 0; j < verificationPath.length; j++) {
			// TODO decide if concatenate from left or right? Use i!
			current = Hash.concatenateAndHash(current, verificationPath[j]);
		}
		
		return current.equals(rootHash);
	}



	//-----------------------------//-----------------------------//
	// Appending events
	//-----------------------------//-----------------------------//

	/*  Method that takes a Merkle tree of size n and adds a string s
	 *  to it as record e(n+1).
	 */
	public static MerkleTree addEvent(MerkleTree root, int n, String s) {
		if (n == 0)
			return new MerkleTree(s, 1, 1);
		if (n == 1)
			return new MerkleTree(root, new MerkleTree(s, 2, 2));
		if ((n & (n - 1)) == 0) // if tree is full, create new right branch from root
			return new MerkleTree(root, new MerkleTree(s, n + 1, n + 1));
		
		MerkleTree newNode = new MerkleTree(s, n + 1, n + 1), node = root, parent = null;
		int q = n;
		while ((q & (q - 1)) != 0) {
			node.endIndex = n + 1;
			parent = node;
			node = node.right;
			q = node.endIndex - node.beginIndex + 1;
		}

		if (node.left != null && node.right == null) { // add as right node
			node.endIndex = n + 1;
			node.right = newNode;
		} else { // create new branch
			parent.right = new MerkleTree(node, newNode);
		}
		return root;
	}

	public static MerkleTree addMultipleEvents(MerkleTree root, int n, String[] events){
		return null;
	}

	private Hash[] genUpdate(){
		return null;
	}
	
	private boolean verifyUpdate(){
		return false;
	}
	
	private MerkleTree applyUpdate(MerkleTree root, Hash[] updates){
		return null;
	}



	//-----------------------------//-----------------------------//
	// Useful methods to test
	//-----------------------------//-----------------------------//

	public static void DFSprint(MerkleTree n, String spaces){
		if (n != null){
			System.out.println(spaces + n.beginIndex + " * " + n.endIndex);
			DFSprint(n.left, spaces + "   ");
			DFSprint(n.right, spaces + "   ");
		}
	}
	
	public static void pathPrint(ArrayList<Hash> a){
		System.out.print("[");
		for (Hash hash : a) {
			System.out.print(" " + hash + ", ");
		}
		System.out.println("]");
	}
	
}
