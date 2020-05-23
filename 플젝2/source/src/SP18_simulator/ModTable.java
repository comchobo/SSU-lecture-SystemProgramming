package SP18_simulator;

import java.util.ArrayList;

public class ModTable {
	
	ArrayList<Integer> TargetAdrList;
	ArrayList<Integer> AdrList;
	ArrayList<Boolean> Is05List;
	ArrayList<Boolean> IsMinusList;
	
	public void init() {
		TargetAdrList = new ArrayList<Integer>();
		AdrList = new ArrayList<Integer>();
		Is05List = new ArrayList<Boolean>();
		IsMinusList = new ArrayList<Boolean>();
	}
	
	public void putMod(int targetadr,int adr, boolean Is05, boolean isMinus) {
		TargetAdrList.add(targetadr);
		AdrList.add(adr);
		Is05List.add(Is05);
		IsMinusList.add(isMinus);
	}
}
