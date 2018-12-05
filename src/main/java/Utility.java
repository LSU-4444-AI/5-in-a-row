import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Utility {
	
	/** Extract wins from data.
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	static void wins(String filePath, String newFilePath) throws IOException{
		FileReader reader=new FileReader(filePath);
		BufferedReader br = new BufferedReader(reader);
        FileWriter writer = new FileWriter(newFilePath);
        BufferedWriter bw = new BufferedWriter(writer);
        String line = null;
        br.readLine();
        String nbr=null;
		while((line = br.readLine()) != null) {
			switch(line.charAt(0)){
			case 'G': 
				nbr=cleanUp(line);
				break;
			case 'W': 
	            bw.write(nbr);
	            bw.newLine();
			}

        }  
		
        bw.close();
        br.close();
	}
	
	/** Extract wins from data.
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	static void ties(String filePath, String newFilePath) throws IOException{
		FileReader reader=new FileReader(filePath);
		BufferedReader br = new BufferedReader(reader);
        FileWriter writer = new FileWriter(newFilePath);
        BufferedWriter bw = new BufferedWriter(writer);
        String line = null;
        br.readLine();
        String nbr=null;
		while((line = br.readLine()) != null) {
			switch(line.charAt(0)){
			case 'G': 
				nbr=cleanUp(line);
				break;
			case 'T': 
	            bw.write(nbr);
	            bw.newLine();
			}

        }  
		
        bw.close();
        br.close();
	}
	
	/** Removes everything except numbers from textfile
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	static void cleanUp(String filePath, String newFilePath) throws IOException{
		FileReader reader=new FileReader(filePath);
		BufferedReader br = new BufferedReader(reader);
        FileWriter writer = new FileWriter(newFilePath);
        BufferedWriter bw = new BufferedWriter(writer);
        String line = null;
        
		while((line = br.readLine()) != null) {
            bw.write(cleanUp(line));
            bw.newLine();
        }  
		
        bw.close();
        br.close();
	}
	
	private static String cleanUp(String s){
		StringBuilder sb= new StringBuilder();
		for(int i=0;i<s.length();i++){
			char c=s.charAt(i);
	        if(c > 47 && c < 58){
	            sb.append(c);
	        }
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args){
		try {
			wins("output_zero_500_000.txt", "Zero_500_000_wins.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
