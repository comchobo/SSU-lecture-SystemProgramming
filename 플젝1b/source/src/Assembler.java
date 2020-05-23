import java.io.*;
import java.util.*;

public class Assembler {
	InstTable instTable;
	ArrayList<String> lineList;
	ArrayList<SymbolTable> symtabList;
	ArrayList<TokenTable> TokenList;
	ArrayList<LitTable> litList; 	//리터럴테이블
	ArrayList<String> codeList;
	ArrayList<ExtTable> ExtList; 	//외부주소 테이블
	
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
			System.out.println("출력중 오류 발생");
		}
		assembler.pass2();
		try{assembler.printObjectCode("output_20142249.txt");}
		catch(IOException e) {
			System.out.println("출력중 오류 발생");
		}
		
	}

	//오브젝트 코드를 출력한다.
	private void printObjectCode(String fileName) throws IOException {
		PrintWriter output = new PrintWriter(fileName);
		for(int i=0;i<TokenList.size();i++) {
			output.println(TokenList.get(i).FinalCode);
		}
		output.close();
	}
	
	//심볼테이블 출력부
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

	//토큰테이블, 심볼테이블, 리터럴 테이블을 생성하는 pass1()
	private void pass1() {
		int i = 0;
		int j = 0;
		
		//라인을 토큰으로 1:1 할당하는 과정이다.
		TokenTable temp= new TokenTable();
		TokenList.add(temp);
		for(;i<lineList.size();i++) {
			try{TokenList.get(j).putToken(lineList.get(i),instTable);}
			
			//섹터가 바뀔 경우 섹터체인지를 쓰로우하여 다음 리스트에 저장한다.
			catch(SectorChange e) {
				Token tempToken = TokenList.get(j).popToken();
				j++; 
				TokenList.add(new TokenTable());
				TokenList.get(j).pushToken(tempToken);
				continue;
			}
		}
		i=0;j=0;
		
		//리터럴 테이블을 생성하는 과정
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
		
		//생성된 리터럴 테이블에 따라 주소를 수정하는 과정
		for(i=0;i<TokenList.size();i++) {
			TokenList.get(i).calLit();
		}

		
		// 심볼 테이블을 생성하는 과정
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
	 * pass2 과정을 수행한다.<br>
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		
		//외부주소 테이블을 만드는 과정
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
		
		//기계어를 생성하는 과정
		for(int i=0;i<TokenList.size();i++) {
			try{
				TokenList.get(i).makeObjectCode();
			}
			catch(SectorChange e) {
				continue;
			}
		}
		int a;
		
		//출력물을 생성하는 과정
		for(int i=0;i<TokenList.size();i++) {
			try{
				TokenList.get(i).buildObjectCode();
			}
			catch(SectorChange e) {
				continue;
			}
		}
	}
	
	//inputFile을 읽어 라인별로 저장한다.
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

