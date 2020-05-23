import java.util.ArrayList;

public class LitTable {
	ArrayList<String> nameList;
	ArrayList<Integer> locationList;
	ArrayList<Integer> valueList;		//16진수 값일경우 사용, 스트링일 경우 -1
	ArrayList<String> StringList;		//스트링일 경우 사용, 16진수 값일 경우 ""
	ArrayList<Boolean> IsStringList;	//스트링 여부 판별
	ArrayList<Integer> SizeList;		//리터럴의 사이즈
	
	int Table_Size=0;	//테이블의 사이즈
	boolean LTORG_flag;	//LTORG 선언여부
	
	//각 리스트의 값 할당을 담당
	public void init(){
		nameList = new ArrayList<String>();
		locationList = new ArrayList<Integer>();
		valueList = new ArrayList<Integer>();
		StringList = new ArrayList<String>();
		IsStringList = new ArrayList<Boolean>();
		SizeList = new ArrayList<Integer>();
	}
	
	public int searchLit(String name) {
		int address = 0;
		for(int i =0;i<nameList.size();i++) {
			if(nameList.get(i).equals(name)) address = locationList.get(i);
		}
		return address;
	}
	
	public void calSize(int LTORG_adr,int base_location) throws SectorChange {
		if(Table_Size ==0) throw new SectorChange();
		int base = base_location; 
		if(LTORG_flag==true) {
			base = LTORG_adr;
			locationList.add(base);
		}
		else locationList.add(base);
		
		if(LTORG_flag == true) { // LTORG 선언이 있었을 때 주소처리
			for(int j =1; j<nameList.size(); j++) {
				
				//문자열인지 여부에 따라 다음 리터럴의 주소를 계산한다.
				if(IsStringList.get(j)==true) {
					base += 3;
					locationList.add(base);
				}
				else {
					base +=1;
					locationList.add(base);
				}
			}
		}
		
		else {		//LTORG 선언이 없었을 때 주소처리
			for(int j =1; j<nameList.size(); j++) {
				if(IsStringList.get(j)==true) {
					base += 3;
					locationList.add(base);
				}
				else {
					base +=1;
					locationList.add(base);
				}
			}
		}
	}
	
}
