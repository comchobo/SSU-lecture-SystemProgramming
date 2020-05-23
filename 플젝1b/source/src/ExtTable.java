import java.util.ArrayList;

public class ExtTable {
	ArrayList<String> ExtName;
	ArrayList<Integer> ExtAdr;
	ArrayList<Boolean> Is_ExtR;
	
	public void init(){
		Is_ExtR = new ArrayList<Boolean>();
		ExtAdr = new ArrayList<Integer>();
		ExtName = new ArrayList<String>();
	}
}
