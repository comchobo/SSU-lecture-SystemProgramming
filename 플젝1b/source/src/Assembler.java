import java.io.*;
import java.util.*;

public class Assembler {
	InstTable instTable;
	ArrayList<String> lineList;
	ArrayList<SymbolTable> symtabList;
	ArrayList<TokenTable> TokenList;
	ArrayList<LitTable> litList; 	//���ͷ����̺�
	ArrayList<String> codeList;
	ArrayList<ExtTable> ExtList; 	//�ܺ��ּ� ���̺�
	
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		litList = new ArrayList<LitTable>();
		codeList = new ArrayList<String>();
		ExtList = new ArrayList<ExtTable>();
	}

	public static void main(String[] args) {
		
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		
		assembler.pass1();
		
		try{assembler.printSymbolTable("symtab_20142249.txt");}
		catch(IOException e) {
			System.out.println("����� ���� �߻�");
		}
		assembler.pass2();
		try{assembler.printObjectCode("output_20142249.txt");}
		catch(IOException e) {
			System.out.println("����� ���� �߻�");
		}
		
	}

	//������Ʈ �ڵ带 ����Ѵ�.
	private void printObjectCode(String fileName) throws IOException {
		PrintWriter output = new PrintWriter(fileName);
		for(int i=0;i<TokenList.size();i++) {
			output.println(TokenList.get(i).FinalCode);
		}
		output.close();
	}
	
	//�ɺ����̺� ��º�
	private void printSymbolTable(String fileName) throws IOException {

		PrintWriter output = new PrintWriter(fileName);
		for(int i =0;i<symtabList.size();i++) {
			for(int j = 0; j<symtabList.get(i).symbolList.size(); j++) {
				output.println(symtabList.get(i).symbolList.get(j));
				output.println(Integer.toHexString(symtabList.get(i).locationList.get(j)));
			}
		}
		output.close();
	}

	//��ū���̺�, �ɺ����̺�, ���ͷ� ���̺��� �����ϴ� pass1()
	private void pass1() {
		int i = 0;
		int j = 0;
		
		//������ ��ū���� 1:1 �Ҵ��ϴ� �����̴�.
		TokenTable temp= new TokenTable();
		TokenList.add(temp);
		for(;i<lineList.size();i++) {
			try{TokenList.get(j).putToken(lineList.get(i),instTable);}
			
			//���Ͱ� �ٲ� ��� ����ü������ ���ο��Ͽ� ���� ����Ʈ�� �����Ѵ�.
			catch(SectorChange e) {
				Token tempToken = TokenList.get(j).popToken();
				j++; 
				TokenList.add(new TokenTable());
				TokenList.get(j).pushToken(tempToken);
				continue;
			}
		}
		i=0;j=0;
		
		//���ͷ� ���̺��� �����ϴ� ����
		for(i=0; i<TokenList.size();i++) {
			try{
				litList.add(new LitTable());
				TokenList.get(i).linkLit(litList.get(i));
				TokenList.get(i).linkInst(instTable);
				TokenList.get(i).makeLit();}
			catch(SectorChange e) {
				continue;
				}
		}
		
		//������ ���ͷ� ���̺� ���� �ּҸ� �����ϴ� ����
		for(i=0;i<TokenList.size();i++) {
			TokenList.get(i).calLit();
		}

		
		// �ɺ� ���̺��� �����ϴ� ����
		for(i=0;i<TokenList.size();i++) {
			try{
				symtabList.add(new SymbolTable());
				TokenList.get(i).linkSym(symtabList.get(i));
				TokenList.get(i).makeSymtab();}
			catch(SectorChange e) {
				continue;
			}
		}
		j=0;
		
		int t=5;
		// TODO Auto-generated method stub
		
	}
	

	/**
	 * pass2 ������ �����Ѵ�.<br>
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		
		//�ܺ��ּ� ���̺��� ����� ����
		for(int i=0;i<TokenList.size();i++) {
			try{
				ExtList.add(new ExtTable());
				TokenList.get(i).linkExt(ExtList.get(i));
				TokenList.get(i).makeExt();
			}
			catch(SectorChange e) {
				continue;
			}
		}
		
		//��� �����ϴ� ����
		for(int i=0;i<TokenList.size();i++) {
			try{
				TokenList.get(i).makeObjectCode();
			}
			catch(SectorChange e) {
				continue;
			}
		}
		int a;
		
		//��¹��� �����ϴ� ����
		for(int i=0;i<TokenList.size();i++) {
			try{
				TokenList.get(i).buildObjectCode();
			}
			catch(SectorChange e) {
				continue;
			}
		}
	}
	
	//inputFile�� �о� ���κ��� �����Ѵ�.
	private void loadInputFile(String inputFile) {
		try {
		File input = new File(inputFile);
		Scanner temp;
		temp = new Scanner(input);
		while(temp.hasNextLine()) {
			String line = temp.nextLine();
			if(line.startsWith(".")) continue;
			lineList.add(line);
		}
		temp.close();
	}
		catch(FileNotFoundException e){
			System.out.println("file not found");
			e.printStackTrace();
		}
	}
}

