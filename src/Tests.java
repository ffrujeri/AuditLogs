package audit_logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Tests {
	private static long time;

	public static void ping(){
		time = System.currentTimeMillis();
	}
	
	public static long pong(){
		return System.currentTimeMillis() - time;
	}
	
	public static void testComputeTree(String filePath){
		System.out.println("\n\n----------------- testing computeTree(File f) -----------------");
		System.out.println("computing tree from file " + filePath + "...");
		File f = new File(filePath);
		MerkleTree root = MerkleTree.computeMerkleTree(f);
		MerkleTree.printDFS(root);
	}
	
	public static void testComputeTree2(String filePath){ // no print
	Tests.ping();
		System.out.println("\n\n----------------- testing computeTree(File f) -----------------");
		System.out.println("computing tree from file " + filePath + "...");
		File f = new File(filePath);
		MerkleTree root = MerkleTree.computeMerkleTree(f);
		System.out.println("Merkle tree with " + (root.getEndIndex() - root.getBeginIndex() + 1) + " nodes computed succesfully in " +Tests.pong() + " ms");
	}
	
	public static void testAddEvent(String filePath){
		System.out.println("\n\n----------------- testing addEvent(MerkleTree root, String s) -----------------");
		System.out.println("adding events tree from file " + filePath + "...");
		File file = new File(filePath);

		MerkleTree root = null;
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				root = MerkleTree.addEvent(root, line);
			}
			
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		MerkleTree.printDFS(root);
	}
	
	public static void testVerifyPath(String filePath, String s, int i, boolean expectedResult){
		System.out.println("\n\n----------------- testing verifyPath() -----------------");
		System.out.println("computing tree from file " + filePath + "...");
		File f = new File(filePath);
		
		MerkleTree root = MerkleTree.computeMerkleTree(f);
		
		ArrayList<Hash> verificationPath = MerkleTree.genPath(root, i);
		boolean result = MerkleTree.verifyPath(root.getHash(), s, i, verificationPath);
		System.out.println(" ---> Expected result = " + expectedResult + "\n" +
						   " ---> Obtained result = " + result);
	}
	
	public static void testUpdate(String filePath1, String filePath2){
		System.out.println("\n\n----------------- testing update -----------------");
		System.out.println("using files " + filePath1 + " and " + filePath2);
		File f = new File(filePath1);
		MerkleTree n1 = MerkleTree.computeMerkleTree(f);
		f = new File(filePath2);
		MerkleTree n2 = MerkleTree.computeMerkleTree(f);

		System.out.println("---> testing genUpdate(...):");
		Hash[] updates = MerkleTree.genUpdate(n1, n2);
		System.out.println("\t#updates = " + (updates.length-1) + "\n");
		
		System.out.println("---> testing verifyUpdate(...):");
		System.out.println("\tresult = " + MerkleTree.verifyUpdate(n1, updates) + "\n");

		System.out.println("---> testing applyUpdate(...):");
		n1 = MerkleTree.applyUpdate(n1, updates);
		System.out.println("\tchecking if roots match... " + (n1.getHash().equals(n2.getHash()) ? " ---> YES!" : " ---> NO!"));
	}

	public static void main(String[] args) {
		String[] f = {"", "", "merkle2.txt", "merkle3.txt", "merkle4.txt", 
							 "merkle5.txt", "merkle6.txt", "merkle7.txt", "merkle8.txt"};
		//String f15 = "merkle15.txt", f12MB = "merkle12MB.txt";
		
		testComputeTree(f[4]);
		testComputeTree(f[7]);
		//testComputeTree2(f12MB);
		
		testAddEvent(f[3]);
		//testAddEvent(f15);
		
		testVerifyPath(f[5], "ccc", 3, true);
		testVerifyPath(f[8], "ddd", 4, true);
		testVerifyPath(f[5], "ccc", 4, false);
		testVerifyPath(f[7], "ggg", 7, true);

		testUpdate(f[3], f[7]);
		//testUpdate(f[4], f15);
	}

}
