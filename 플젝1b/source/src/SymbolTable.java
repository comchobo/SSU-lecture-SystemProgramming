import java.util.ArrayList;

/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	
	public void init(){
		symbolList = new ArrayList<String>();
		locationList = new ArrayList<Integer>();
	}
	
	//심볼을 하나 추가하는 메소드
	public void putSymbol(String symbol, int location) {
		symbolList.add(symbol);
		locationList.add(location);
	}
	
	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation) {
		
	}
	
	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol) {
		int address = 0;
		for(int i =0;i<symbolList.size();i++) {
			if(symbolList.get(i).equals(symbol)) address = locationList.get(i);
		}
		return address;
	}
	
	
	
}
