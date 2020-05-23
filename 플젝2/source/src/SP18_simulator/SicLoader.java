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
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
	 */
	public void load(File objectCode) throws FileNotFoundException {
		//�켱 ������ �� �Է¹޴´�.
		
		Scanner temp;
		temp = new Scanner(objectCode);
		while(temp.hasNextLine()) {
			String line = temp.nextLine();
			lineList.add(line);			
			}
		temp.close();
		
		//�������̺�, �ɺ����̺�, mod���̺��� �����Ѵ�. ���� ����.
		makeTable();
		rMgr.loadtab(symbolTable);
		rMgr.loadtab(modTable);
		rMgr.loadtab(sectTable);
		
		//�ڵ带 �޸𸮿� �ε��Ѵ�.
		loadCode();
		
	}
	
	public void makeTable() {
		//�켱 �������̺� ���� �����Ѵ�.
			for(int i =0;i<lineList.size();i++) {
				if(lineList.get(i).startsWith("H")) {
					String temp[] = lineList.get(i).split("\t");
					temp[0] = temp[0].replaceFirst("H","");
					int Size = Integer.parseInt(temp[1].substring(6,12),16);
					int tempAdr = 0;
					
					//���� ������ �ּҴ� ���� ���������� ũ����� �����Ѵ�.
					if(sectTable.SectNameList.size()!=0) {
						tempAdr = sectTable.SizeList.get(sectTable.SizeList.size()-1)
								+sectTable.startAdrList.get(sectTable.startAdrList.size()-1);
					}
					sectTable.putSect(tempAdr,Size,temp[0]);
				}
			}
		
			//D/R �ܺ��ּ� ������ ���� �ɺ��� �����Ѵ�.
			for(int i =0;i<3;i++) {
				//D�� ��� �ּҸ� �״�� �ɺ����̺� ����
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
				//R�� ��� ���ͽɺ��� ��ġ�Ͽ� �ش� �ּҸ� ����
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
			
			//Mod���̺��� �����Ѵ�
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
				//���� ���ͷ� �Ѿ�� �ڵ����� �ּҸ� ����Ѵ�. ù ���ʹ� ����
				else if(lineList.get(i).startsWith("H")) {
					SectAdr = sectTable.startAdrList.get(j);
					j++;
				}
			}
	}
	
	//�ڵ带 �ε��ϴ� �޼ҵ�
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
