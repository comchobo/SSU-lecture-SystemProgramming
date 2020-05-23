package SP18_simulator;

import java.util.ArrayList;

public class SectTable {

	ArrayList<Integer> startAdrList;
	ArrayList<Integer> SizeList;
	ArrayList<String> SectNameList;
	
	public void init() {
		startAdrList = new ArrayList<Integer>();
		SizeList = new ArrayList<Integer>();
		SectNameList = new ArrayList<String>();
	}
	
	public void putSect(int adr, int size, String name) {
		startAdrList.add(adr);
		SizeList.add(size);
		SectNameList.add(name);
	}
	
	public int search(String symbol) {
		for(int i =0;i<SectNameList.size();i++) {
			if(SectNameList.get(i).equals(symbol)) return startAdrList.get(i);
		}
		return 0;
	}
}


