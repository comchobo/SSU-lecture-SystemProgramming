package SP18_simulator;
import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	
	public void init() {
		symbolList = new ArrayList<String>();
		addressList = new ArrayList<Integer>();
	}

	public void putSymbol(String symbol, int address) throws Exception{
		if (symbolList.contains(symbol)) throw new Exception();
		symbolList.add(symbol);
		addressList.add(address);
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newaddress : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newaddress) {
		
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = 0;
		for(int i =0;i<symbolList.size();i++) {
			if(symbolList.get(i).equals(symbol)) {
				address = addressList.get(i);
				break;
			}
		}
		return address;
	}
	
	
	
}
