package SP18_simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class SicLoader {
	ResourceManager rMgr;
	ArrayList<String> lineList;
	SectTable sectTable;
	SymbolTable symbolTable;
	ModTable modTable;

	public SicLoader(ResourceManager resourceManager) {
		lineList = new ArrayList<String>();
		sectTable = new SectTable();
		modTable = new ModTable();
		symbolTable = new SymbolTable();
		sectTable.init();
		modTable.init();
		symbolTable.init();
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode) throws FileNotFoundException {
		//우선 라인을 쭉 입력받는다.
		
		Scanner temp;
		temp = new Scanner(objectCode);
		while(temp.hasNextLine()) {
			String line = temp.nextLine();
			lineList.add(line);			
			}
		temp.close();
		
		//섹터테이블, 심볼테이블, mod테이블을 생성한다. 이후 전달.
		makeTable();
		rMgr.loadtab(symbolTable);
		rMgr.loadtab(modTable);
		rMgr.loadtab(sectTable);
		
		//코드를 메모리에 로드한다.
		loadCode();
		
	}
	
	public void makeTable() {
		//우선 섹터테이블 먼저 생성한다.
			for(int i =0;i<lineList.size();i++) {
				if(lineList.get(i).startsWith("H")) {
					String temp[] = lineList.get(i).split("\t");
					temp[0] = temp[0].replaceFirst("H","");
					int Size = Integer.parseInt(temp[1].substring(6,12),16);
					int tempAdr = 0;
					
					//다음 섹터의 주소는 이전 섹터정보의 크기부터 시작한다.
					if(sectTable.SectNameList.size()!=0) {
						tempAdr = sectTable.SizeList.get(sectTable.SizeList.size()-1)
								+sectTable.startAdrList.get(sectTable.startAdrList.size()-1);
					}
					sectTable.putSect(tempAdr,Size,temp[0]);
				}
			}
		
			//D/R 외부주소 정보를 통해 심볼을 생성한다.
			for(int i =0;i<3;i++) {
				//D의 경우 주소를 그대로 심볼테이블에 저장
				if(lineList.get(i).startsWith("D")) {
					String temp[] = lineList.get(i).split(" ");
					temp[0]=temp[0].replaceFirst("D","");
					for(int j=0;j<temp.length;j++) {
						String temp1 = temp[j].substring(0, temp[j].length()-6);
						String temp2 = temp[j].substring(temp[j].length()-6, temp[j].length());
						try{
							symbolTable.putSymbol(temp1, Integer.parseInt(temp2,16));
						}
						catch (Exception e) {}					
					}
				}
				//R의 경우 섹터심볼을 서치하여 해당 주소를 저장
				else if (lineList.get(i).startsWith("R")) {
					String temp[] = lineList.get(i).split(" ");
					temp[0]=temp[0].replaceFirst("R","");
					for(int j=0;j<temp.length;j++) {							
						try {
							symbolTable.putSymbol(temp[j], sectTable.search(temp[j]));
						}
						catch (Exception e) {}
					}
				}
			}
			
			//Mod테이블을 생성한다
			int SectAdr=0;
			int j=1;
			for(int i =1;i<lineList.size();i++) {	
				if(lineList.get(i).startsWith("M")) {
					String[] temp;
					boolean isminus,Is05;
					if(lineList.get(i).contains("+")) {
						temp = lineList.get(i).split("[+]");
						isminus = false;
					}
					else {
						temp = lineList.get(i).split("[-]");
						isminus = true;
					}
					temp[0]=temp[0].replaceFirst("M","");
					int Adr = Integer.parseInt(temp[0].substring(0,6),16);

					if(temp[0].charAt(7) == '5') {
						Is05 = true;
					}
					else {
						Is05 = false;
					}
									
					modTable.putMod(SectAdr+Adr,symbolTable.search(temp[1]),Is05,isminus);
				}
				//다음 섹터로 넘어가면 자동으로 주소를 계산한다. 첫 섹터는 제외
				else if(lineList.get(i).startsWith("H")) {
					SectAdr = sectTable.startAdrList.get(j);
					j++;
				}
			}
	}
	
	//코드를 로드하는 메소드
	public void loadCode() {
		int SectAdr = 0;
		int j = 1;
		for(int i =1;i<lineList.size();i++) {
			if(lineList.get(i).startsWith("T")) {
				int start = Integer.parseInt(lineList.get(i).substring(1,7),16);
				int len = Integer.parseInt(lineList.get(i).substring(7,9),16)*2;
				String data = lineList.get(i).substring(9,lineList.get(i).length());
				rMgr.setMemory(start,SectAdr, data,len);
			}
			else if(lineList.get(i).startsWith("H")) {
				SectAdr = sectTable.startAdrList.get(j);
				j++;
			}
		}
	}
}
