package audit_logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

public class Tests {
	
	//-----------------------------//-----------------------------//
	// Utilities
	//-----------------------------//-----------------------------//

	private static long time;
	private static String[] f, fMB = {"merkle1000.txt", "merkle10000.txt", "merkle20000.txt", 
								   "merkle30000.txt", "merkle40000.txt", "merkle50000.txt"};
	private static MerkleTree[] t, tMB;
	
	public static void ping(){
		time = System.currentTimeMillis();
	}
	
	public static long pong(){
		return System.currentTimeMillis() - time;
	}
	
	public static String getFileSize(File f){
		long size = f.length();
		if (size > 1024){
			if (size > 1048576){
				if (size > 1073741824){
					return (int) Math.round(size/1073741824.) + " GB";
				}return (int) Math.round(size/1048576.) + " MB";
			}return (int) Math.round(size/1024.) + " KB";
		}return size + " Bytes";
	}

	public static void init(){
		f = new String[18];
		t = new MerkleTree[18];
		for (int i = 2; i < t.length; i++) {
			f[i] = "merkle" + i + ".txt";
			t[i] = MerkleTree.computeMerkleTree(new File(f[i]));
		}

		tMB = new MerkleTree[fMB.length];
		for (int i = 0; i < tMB.length; i++) {
			tMB[i] = MerkleTree.computeMerkleTree(new File(fMB[i]));
		}
	}
	
	//-----------------------------//-----------------------------//
	// Tests
	//-----------------------------//-----------------------------//

	public static void testComputeTree(){
		System.out.println("\n----------------- testing computeTree(File f) -----------------");
		MerkleTree[] t = new MerkleTree[f.length];
		for (int i = 2; i < t.length - 1; i++) {
			t[i] = testComputeTree(f[i], false, false);
		}
		
		t[9] = testComputeTree(f[9], true, false); // prints result

		MerkleTree[] tMB = new MerkleTree[fMB.length];
		for (int i = 0; i < tMB.length; i++) {
			tMB[i] = testComputeTree(fMB[i], false, false);
		}
	}
	
	public static MerkleTree testComputeTree(String filePath, boolean printTree, boolean printBuildingProcess){ // no print
		Tests.ping();
		System.out.println("Computing tree from file " + filePath + "...");
		File f = new File(filePath);
		MerkleTree root = MerkleTree.computeMerkleTree(f, printBuildingProcess);
		if (printTree)
			MerkleTree.printDFS(root);
		System.out.println("Merkle tree with " + (root.getEndIndex() - root.getBeginIndex() + 1)
						 + " nodes computed from " + getFileSize(f) + " file succesfully in " + Tests.pong() + " ms\n");
		return root;
	}
	
	
	public static void testAddEvent(){
		System.out.println("\n----------------- testing addEvent(MerkleTree root, String s) -----------------");
		testAddEvent(f[3], t[3].getHash(), false);
		testAddEvent(f[8], t[8].getHash(), false);
		testAddEvent(f[9], t[9].getHash(), false);
	}

	public static void testAddEvent(String filePath, Hash rootHash, boolean print){
		System.out.println("Adding events tree from file " + filePath + "...");
		File file = new File(filePath);
		
		MerkleTree root = null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				root = MerkleTree.addEvent(root, line);
			}br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		if (print)
			MerkleTree.printDFS(root);
		if (rootHash.equals(root.getHash()))
			System.out.println("---> Added " + (root.getEndIndex() - root.getBeginIndex() + 1) + 
				" nodes from file of " + getFileSize(file) + " succesfully in " + Tests.pong() + " ms\n");
		else
			System.out.println("---> FAILED! Root hash does not match expected value!\n");
	}

	public static void testVerifyPath(){
		System.out.println("\n----------------- testing verifyPath() -----------------");
		for(int i = 4; i < t.length; i++)
			testVerifyPath(t[i], f[i], i, i);
		testVerifyPath(tMB[1], fMB[1], 10000, 500);
		testVerifyPath(tMB[2], fMB[2], 20000, 500);
		testVerifyPath(tMB[3], fMB[3], 30000, 5000);
		testVerifyPath(tMB[4], fMB[4], 40000, 500);
	}
	
	public static void testVerifyPath(MerkleTree root, String filePath, int nLines, int nTests){
		TreeSet<Integer> indices = new TreeSet<Integer>(); // TreeSet ordered with no duplicates
		for (int i = 0; i < nTests; i++) {
			int value = (int) (Math.random()*nLines) + 1;
			if (!indices.contains(value))
				indices.add(value);
			else i--;
		}
		
		File file = new File(filePath);
		String[] event = new String[nTests];
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			int j = 0;
			for (int i = 1; (line = br.readLine()) != null && j < nTests; i++) {
				if (indices.contains(i))
					event[j++] = line;
			}br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		ping();
		int j = 0, counter = 0;
		for (Integer i : indices) {
			ArrayList<Hash> verificationPath = MerkleTree.genPath(root, i);
			boolean result = MerkleTree.verifyPath(root.getHash(), event[j++], i, verificationPath, root.getEndIndex());
			if (!result){
				System.out.println("FAIL! Verification path result DOES NOT MATCH root for node " + i);
				counter++;
			}
		}

		if (counter == 0)
			System.out.println(nTests + " records verified in " + pong() + " ms\n");
		else System.out.println("FAIL! Verification path result DOES NOT MATCH root for " 
						+ counter + " nodes in a tree of " + root.getEndIndex() + " nodes\n");
	}

	public static void testUpdate(){
		System.out.println("\n----------------- testing update -----------------");
		testUpdate(t[3], t[7], true);
		testUpdate(t[5], t[8], true);
		testUpdate(t[4], t[9], true);
		testUpdate(tMB[1], tMB[2], false);
	}
	
	public static void testUpdate(MerkleTree n1, MerkleTree n2, boolean expectedResult){
		ping();

		System.out.println("---> testing genUpdate...");
		Hash[] updates = MerkleTree.genUpdate(n1, n2);
		System.out.println("\t#updates = " + (updates.length-1));
		
		System.out.println("---> testing verifyUpdate... " + MerkleTree.verifyUpdate(n1, updates));

		System.out.println("---> testing applyUpdate...");
		n1 = MerkleTree.applyUpdate(n1, updates);

		if (expectedResult){
			if (n1.getHash().equals(n2.getHash()))
				System.out.println("Update verified and applied succesfully in " + pong() + " ms\n");
			else System.out.println("FAILED! Valid update rejected\n");
		}else{
			if (n1.getHash().equals(n2.getHash()))
				System.out.println("FAILED! Invalid update accepted");
			else System.out.println("Invalid update rejected in " + pong() + " ms");
		}
	}
	
	public static void testBigFiles(){
		System.out.println("\n----------------- testing big files (> 1 GB)  -----------------");
		String f2GB = "merkle 100000lines 2GB.txt",
			   f1GB = "merkle 50000lines 1GB.txt";
		testComputeTree(f1GB, false, true);
		System.out.println("--------------\n");
		testComputeTree(f2GB, false, true);
	}

	// TODO: apagar
	public static void stupidTest(int n){
		for (int i = 0; i < n; i++) {
			int n2 = (int) Math.pow(2, (int) (Math.log10(n)/Math.log10(2)));
			int completeDepth = (int) Math.ceil(Math.log10(n)/Math.log10(2));
			System.out.println(n + " * " + n2 + " * " + completeDepth);
		}
	}
	
	public static void main(String[] args) {
		init();
		//testComputeTree();
		//testAddEvent();
		testVerifyPath();
		//testUpdate();
		//testBigFiles();
		
		//stupidTest(70);
	}

}

