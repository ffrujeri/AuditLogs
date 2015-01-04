import java.io.File;

public class Tests {
	
	// 2^h >= n => h >= log2(n) => ceil
	public static int getTreeHeight(int n){
		return (int) Math.ceil(Math.log(n)/Math.log(2.));
	}

	public static void main(String[] args) {
//		System.out.println(Math.ceil(10.01));
//		for(int i = 1; i < 10; i++) System.out.println("i = " + i + " * h(i) = " + getTreeHeight(i));
		
		File f = new File("merkle2.txt");
		MerkleTree n = MerkleTree.computeMerkleTree(f);
		MerkleTree.DFSprint(n, "");
//		MerkleTree.BFSprint(n);

	}
}
