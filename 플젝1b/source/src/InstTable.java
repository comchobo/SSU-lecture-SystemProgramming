import java.util.HashMap;
import java.io.*;
import java.util.*;

//��ɾ� ������ ���� Ŭ�����̴�.
public class InstTable {
	HashMap<String, Instruction> instMap;
	
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	//���� �� ���� ���� ��ɾ� ������ �̾� �����Ѵ�.
	public void openFile(String fileName) {
		try{
			File input = new File(fileName);
			Scanner temp;
			temp = new Scanner(input);
			
			while(temp.hasNextLine()) {
				String line = temp.nextLine();
				
				Instruction temp_inst = new Instruction(line);
				instMap.put(temp_inst.name,temp_inst);
			}
			temp.close();
		}
		catch(FileNotFoundException e){
			System.out.println("file not found");
			e.printStackTrace();
		}
	}


}

class Instruction {
	String name;
	int opcode;
	int format;
	int operandN;

	//������ ���ÿ� �Ľ��Ѵ�.
	public Instruction(String line) {
		parsing(line);
	}
	
	//��Ű�� �����ڷ� �����Ѵ�.
	public void parsing(String line) {
		Scanner temp_line = new Scanner(line).useDelimiter("\t");
		name = temp_line.next();
		opcode = Integer.parseInt(temp_line.next(),16);
		format = temp_line.nextInt();
		operandN = temp_line.nextInt();
	}

}
