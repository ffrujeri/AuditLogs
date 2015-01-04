/* Suppose a user has the root hash for a Merkle tree of size n, but now the
 * tree has grown to size m > n. Design an algorithm that computes the minimal
 * array of hashes required to communicate the new Merkle tree to the user.
 * Implement three methods:
 *  � genUpdate computes this minimal update as an array of hashes,
 *  � verifyUpdate checks that the update is valid for a given current Merkle tree
 *  � applyUpdate that creates modifies the current Merkle tree to apply the
 *  update (creating a tree of size m)
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
		hash = Hash.concatenateAndHash(left.getHash(), right.getHash());
		beginIndex = left.getBeginIndex();
		endIndex = right.getEndIndex();
	}

	/* Takes an input text file and computes its Merkle tree (treating each line 
	 * as a new event). Works with large (>1GB) text files.
	 */
	// TODO: test!
	public static MerkleTree computeMerkleTree(File file){
		MerkleTree root = null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			line = br.readLine();
			
			if (line == null){
				br.close();
				return null;
			}
			
			int n = 1;
			root = new MerkleTree(line, n, n); // TODO: unecessary, can be null; test!
			while ((line = br.readLine()) != null) {
				root = addEvent(root, n, line);
				n++;
			}
			
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		return root;
	}
		
	
	/*  Method that takes a Merkle tree of size n and adds a string s
	 *  to it as record en+1.
	 */
	// TODO: test!
	public static MerkleTree addEvent(MerkleTree root, int n, String s){
		if (root == null){
			return new MerkleTree(s, 1, 1);
		}else if(n == 1){
			return new MerkleTree(root, new MerkleTree(s, 2, 2));
		}
			
		MerkleTree newNode = new MerkleTree(s, n+1, n+1),
				   node = root, parent = null;
		int h = 0;
		while(node.right != null){
//			node.endIndex = n+1;
			parent = node;
			node = node.right;
			h++;
		}
		
		if(node.left != null && node.right == null){
			node.right = newNode;
		}else{
			if (h == getTreeHeight(n)){ // tree is full
				root = new MerkleTree(root, newNode);
			}else{
				parent.right = new MerkleTree(node, newNode);
			}
		}

		return root;
	}
	
	// TODO: Hash[]?
	// TODO: inverse order?
	// TODO: verify
	public static ArrayList<Hash> genPath(MerkleTree root, int i){
		ArrayList<Hash> verificationPath = new ArrayList<Hash>();
		MerkleTree node = root;
		while(node.beginIndex != node.endIndex){
			if(node.left.endIndex <= i){
				verificationPath.add(node.right.getHash());
				node = root.left;
			}else{
				verificationPath.add(node.left.getHash());
				node = root.right;
			}
		}
		
		return verificationPath;
	}
	
	// TODO: ArrayList<Hash>?
	// TODO: verify
	public static Hash[] genPath(MerkleTree root, int i, int n){
		int size = getTreeHeight(n);
		Hash[] verificationPath = new Hash[size];
		MerkleTree node = root;
		
		for(int j = size-1; node.beginIndex != node.endIndex; j--){
			if(node.left.endIndex >= i){
				verificationPath[j] = node.right.getHash();
				node = root.left;
			}else{
				verificationPath[j] = node.left.getHash();
				node = root.right;
			}
		}
		
		return verificationPath;
	}

	private static int getTreeHeight(int n){
		return (int) Math.ceil(Math.log(n)/Math.log(2.));
	}
	
	public static boolean verifyPath(Hash rootHash, String s, 
			int i, Hash[] verificationPath){
		Hash current = Hash.getHash(s);
		for (int j = 0; j < verificationPath.length; j++) {
			// TODO decide if concatenate from left or right? Use i!
			current = Hash.concatenateAndHash(current, verificationPath[j]);
		}
		
		return current.equals(rootHash);
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

	// TODO: erase
	public static void BFSprint(MerkleTree root){
		System.out.println("BFS print");
		Queue<MerkleTree> q = new LinkedList<MerkleTree>();
		q.add(root);
		while(!q.isEmpty()){
			MerkleTree n = (MerkleTree) q.poll();
			System.out.println(n.beginIndex + " * " + n.endIndex);
			if (n.left != null)
				q.add(n.left);
			if (n.right != null)
				q.add(n.right);
		}
		System.out.println("BFS end of print");
	}

	// TODO: erase
	public static void DFSprint(MerkleTree n, String spaces){
		if (n != null){
			System.out.println(spaces + n.beginIndex + " * " + n.endIndex);
			DFSprint(n.left, spaces + "   ");
			DFSprint(n.right, spaces + "   ");
		}
	}
}
