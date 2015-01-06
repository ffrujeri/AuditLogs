package audit_logs;

public class GenerateFiles {
	public static void generateFile(int charactersPerLine, long lines, File file){
		try{
		PrintWriter br = new PrintWriter(file);

		for (int i = 0; i < lines; i++) {
		char[] c = new char[charactersPerLine];
		for (int j = 0; j < charactersPerLine; j++) {
		c[j] = (char) (int) (Math.random()*256);
		}

		br.println(String.valueOf(c));
		}

		br.close();
		}catch(IOException e){
		e.printStackTrace();
		}
		}

		public static void main(String[] args) {
		generateFile(20, 50000000l, new File("merkle 50000000lines 1GB.txt"));
		}
}
