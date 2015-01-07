
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

	public static boolean verifyPath(Hash rootHash, String s, int i, ArrayList<Hash> verificationPath){
		Hash current = Hash.getHash(s);
		int concatenationOrder = i-1;
		int completeDepth = (int) Math.ceil(Math.log10(i)/Math.log10(2));

		if (verificationPath.size() < completeDepth){
			int pow = 1 << (verificationPath.size()-1);
			concatenationOrder = pow + concatenationOrder%pow;
		}
					
		for (Hash hash : verificationPath) {
			if((concatenationOrder & 1) == 0){
				current = Hash.concatenateAndHash(current, hash);
			}else{
				current = Hash.concatenateAndHash(hash, current);
			}
			concatenationOrder = concatenationOrder >> 1;
		}
		return current.equals(rootHash);
	}



//-----------------------------//-----------------------------//
	// Appending events
	//-----------------------------//-----------------------------//

	/*  Method that takes a Merkle tree of size n and adds a string s
	 *  to it as record e(n+1).
	 */
	public static MerkleTree addEvent(MerkleTree root, String s) {		
		return addEvent(root, Hash.getHash(s));
	}
	
	private static MerkleTree addEvent(MerkleTree root, Hash h) {
		if (root == null)
			return new MerkleTree(h, 1, 1);
		int n = root.endIndex;
		if (n == 1)
			return new MerkleTree(root, new MerkleTree(h, 2, 2));
		if ((n & (n - 1)) == 0) // if tree is full, create new right branch from root
			return new MerkleTree(root, new MerkleTree(h, n + 1, n + 1));
		
		MerkleTree newNode = new MerkleTree(h, n + 1, n + 1), node = root, parent = null;
		Stack<MerkleTree> traverseNodes = new Stack<MerkleTree>();
		int q = n;
		while ((q & (q - 1)) != 0) {
			traverseNodes.push(node);
			parent = node;
			node = node.right;
			q = node.endIndex - node.beginIndex + 1;
		}

		if (node.left != null && node.right == null) { // add as right node
			node.right = newNode;
		} else { // create new branch
			parent.right = new MerkleTree(node, newNode);
		}
		
		while(!traverseNodes.empty()){
			node = traverseNodes.pop();
			node.endIndex = node.right.endIndex;
			node.hash = Hash.concatenateAndHash(node.left.hash, node.right.hash);
		}
		
		return root;
	}

	private MerkleTree(Hash hash, int beginIndex, int endIndex){
		this.hash = hash;
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}

	public static Hash[] genUpdate(MerkleTree oldTree, MerkleTree newTree){
		int n = oldTree.endIndex, m = newTree.endIndex;
		Hash[] hashes = new Hash[m-n+1];
		hashes[0] = newTree.hash;

		Stack<MerkleTree> s = new Stack<>();
		s.add(newTree);
		for(int i = 1; !s.isEmpty(); ){
			MerkleTree node = s.pop();
			if (node.beginIndex == node.endIndex){
				if(node.beginIndex > n){
					hashes[i] = node.hash;
					i++;
				}
			}else{
				if (node.left.endIndex > n){
					s.push(node.right);
					s.push(node.left);
				}else if (node.right.endIndex > n)
					s.push(node.right);
			}
		}
		
		return hashes;
	}
	
	public static boolean verifyUpdate(MerkleTree oldTree, Hash[] updates){
		int n = oldTree.endIndex,
			n2 = (int) Math.pow(2., Math.floor(Math.log(n)/Math.log(2))); // biggest power of 2 <= n
		
		// get pertinent leaves hashes from oldTree and update vector
		Hash completeSubtreeHash = null;
		Queue<Hash> currentLevel = new LinkedList<>();
		Queue<MerkleTree> q = new LinkedList<>();
		q.add(oldTree);
		while(!q.isEmpty()){
			MerkleTree node = q.poll();
			if (node != null && node.beginIndex == 1 && node.endIndex == n2){
				completeSubtreeHash = node.hash;
			}if (node.beginIndex == node.endIndex){
				if(node.beginIndex > n2){
					currentLevel.add(node.hash);
				}
			}else{
				if (node.left.endIndex >= n2){
					q.add(node.left);
					q.add(node.right);
				}else if (node.right.endIndex >= n2){
					q.add(node.right);
				}
			}
		}
		
		for (int i = 1; i < updates.length; i++)
			currentLevel.add(updates[i]);

		// compute hashes by level
		int counter = 1;
		for(Queue<Hash> levelUp = new LinkedList<>(); currentLevel.size() != 1; currentLevel = levelUp){
			levelUp = new LinkedList<>();
			while(currentLevel.size() > 0){
				Hash h1, h2;
				if (counter == n2){
					h1 = completeSubtreeHash;
					h2 = currentLevel.poll();
					counter += 1;
				}else{
					h1 = currentLevel.poll();
					h2 = currentLevel.poll();
				}

				if (h2 != null)
					levelUp.add(Hash.concatenateAndHash(h1, h2));
				else levelUp.add(h1);
			}
			
			counter = counter << 1;
		}
		
		if (counter == n2){
			Hash h1 = completeSubtreeHash,
				 h2 = currentLevel.poll();
			currentLevel.add(Hash.concatenateAndHash(h1, h2));
		}
		
		// verify root hash of new tree equals obtained result
		return updates[0].equals(currentLevel.poll());
	}
	
	public static MerkleTree applyUpdate(MerkleTree oldTree, Hash[] updates){
		for (int i = 1; i < updates.length; i++) {
			oldTree = addEvent(oldTree, updates[i]);
		}
		
		return oldTree;
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
