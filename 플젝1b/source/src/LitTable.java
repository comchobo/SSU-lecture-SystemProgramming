import java.util.ArrayList;

public class LitTable {
	ArrayList<String> nameList;
	ArrayList<Integer> locationList;
	ArrayList<Integer> valueList;		//16���� ���ϰ�� ���, ��Ʈ���� ��� -1
	ArrayList<String> StringList;		//��Ʈ���� ��� ���, 16���� ���� ��� ""
	ArrayList<Boolean> IsStringList;	//��Ʈ�� ���� �Ǻ�
	ArrayList<Integer> SizeList;		//���ͷ��� ������
	
	int Table_Size=0;	//���̺��� ������
	boolean LTORG_flag;	//LTORG ���𿩺�
	
	//�� ����Ʈ�� �� �Ҵ��� ���
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
		
		if(LTORG_flag == true) { // LTORG ������ �־��� �� �ּ�ó��
			for(int j =1; j<nameList.size(); j++) {
				
				//���ڿ����� ���ο� ���� ���� ���ͷ��� �ּҸ� ����Ѵ�.
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
		
		else {		//LTORG ������ ������ �� �ּ�ó��
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
