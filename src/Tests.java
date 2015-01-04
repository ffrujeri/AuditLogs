package audit_logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Tests {
	/* Deprecated: Test of tree height */
	public static int getTreeHeight(int n){
		return (int) Math.ceil(Math.log(n)/Math.log(2.));
	}

	/* Test of addEvent to append events with txt files */
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
			
			int n = 0;
			while ((line = br.readLine()) != null) {
				root = MerkleTree.addEvent(root, n, line);
				n++;
			}
			
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		return root;
	}

	public static void main(String[] args) {
//		System.out.println(Math.ceil(10.01));
//		for(int i = 1; i < 10; i++) System.out.println("i = " + i + " * h(i) = " + getTreeHeight(i));

		for (int i = 2; i <= 8; i++) {
			String fileName = "merkle" + i + ".txt";
			System.out.println("\n\n[merkle tree " + i + "]");
			File f = new File(fileName);
			MerkleTree n = MerkleTree.computeMerkleTree(f);
//			MerkleTree.DFSprint(n, "");
			
			n = computeMerkleTree(f);
			MerkleTree.DFSprint(n, "");
		}

		String fileName = "merkle15.txt";
		System.out.println("\n\n[merkle tree 15]");
		File f = new File(fileName);
		MerkleTree n = MerkleTree.computeMerkleTree(f);
		MerkleTree.DFSprint(n, "");
		n = computeMerkleTree(f);
		MerkleTree.DFSprint(n, "");
		
		ArrayList<Hash> verPath = MerkleTree.genPath(n, 4);
		MerkleTree.pathPrint(verPath);
		
/*		fileName = "merkle12MB.txt";
		System.out.println("\n\n[merkle tree 15]");
		f = new File(fileName);
		n = MerkleTree.computeMerkleTree(f);
		MerkleTree.DFSprint(n, "");
		n = computeMerkleTree(f);
		MerkleTree.DFSprint(n, "");
	*/
	
	}
}
