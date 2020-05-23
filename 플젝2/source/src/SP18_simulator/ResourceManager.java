package SP18_simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class ResourceManager{
	
	HashMap<String,String> deviceManager = new HashMap<String,String>();
	//스트링의 경우 한 글자가 기준이므로 두글자(1byte) 기준 주소를 변형할 필요가 있다.
	StringBuilder memory = new StringBuilder(65536);
	int[] register = new int[10];
	double register_F;
	String recentDevice=new String("NULL"); //최근에 테스트한 디바이스를 저장.
	Scanner temp;
	int buffer=0;
	File devNameFile;
	
	SymbolTable symtab;
	SectTable secttab;
	ModTable modtab;
	
	public ResourceManager(){
		//memory의 안정성을 위해 0으로 초기화한다.
			for(int i =0;i<65536;i++) {
				memory.insert(i, "0");
			}
				
	}

	//외부주소 테이블에 따라 코드를 조작한다.
	public void initializeResource(){
		
		register[8] = 0;
		int temp=0;
		for(int i =0;i<modtab.TargetAdrList.size();i++) {
			if(modtab.Is05List.get(i)==true) {
				this.setMemory(modtab.TargetAdrList.get(i),0.5,String.format("%05x",modtab.AdrList.get(i)),5);
			}
			else {
				if(modtab.IsMinusList.get(i)==false) {
					temp += modtab.AdrList.get(i);
				}
				else {
					temp-=modtab.AdrList.get(i);
					this.setMemory(modtab.TargetAdrList.get(i),String.format("%06x",temp,16),6);
				}
			}
		}
	}
	
	//각 테이블들을 로드한다.
	public void loadtab(SymbolTable a) {
		symtab = a;
	}
	public void loadtab(SectTable a) {
		secttab = a;
	}
	public void loadtab(ModTable a) {
		modtab = a;
	}
	
	public void AddPCReg(int val) {
		register[8]+=val;
	}
	public void MovePCReg(int val) {
		register[8] = val;
	}
	public int PCReg() {
		return register[8];
	}
	
	//Hashmap을 만든다.
	public void testDevice(String devName) {
		recentDevice=devName;
		try {
		if(!deviceManager.containsKey(devName)) {
			devNameFile = new File(devName);
			temp = new Scanner(devNameFile);
			if(temp.hasNextLine()) {
				deviceManager.put(devName,new String(temp.nextLine()));
			}
			else {
				deviceManager.put(devName,new String());
			}
			buffer=0;
		}
		}
		catch (FileNotFoundException e) {
		}
	}

	//device 입출력에 대한 명세가 없어 임의로 'string' 형식의 hashmap을 만들었다.
	//이는 차후 closedevice에서 언제든 파일입출력 형태로 만들 수 있는 자원이 된다.
	public String readDevice(String devName) {//,int start,int num){
		recentDevice = "NULL";
		String target = deviceManager.get(devName).substring(buffer,buffer+1);
		buffer++;
		return target;
	}
	public void writeDevice(String devName, int data){
		recentDevice = "NULL";
		buffer++;
		deviceManager.put(devName,deviceManager.get(devName).concat(Character.toString((char)data)));
		this.closeDevice(devName);
	}
	public void closeDevice(String devName) {
		try{
			PrintWriter temp2 = new PrintWriter(devName);
			temp2.println(deviceManager.get(devName));
			temp2.close();
		}
		catch(FileNotFoundException e) {}
	}

	//겉으로는 SIC/XE에 표현된 메모리를 그대로 다루지만, Memory 메소드를
	//변형하는 것으로 실제 스트링의 조작을 활용할 수 있다. 하나는 sectadr포함, 하나는 0.5만큼 수정 가능하도록, 하나는 미포함.
	public String getMemory(int location, int num){
		return memory.substring(location*2, location*2+num);
	}
	public void setMemory(int locate,int SectAdr, String data,int num){
		memory.replace(locate*2+SectAdr*2, locate*2+num+SectAdr*2,data);
	}
	public void setMemory(int locate,double smallnum, String data,int num){
		int f = (int) (smallnum*2);
		memory.replace(locate*2+f, locate*2+num+f,data);
	}
	public void setMemory(int locate, String data,int num){
		memory.replace(locate*2, locate*2+num,data);
	}


	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
	 * @param data
	 * @return
	 */
	public String intToString(int data){
		return Integer.toString(data);
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. char[]값을 int형태로 변경한다.
	 * @param data
	 * @return
	 */
	public int byteToInt(String data){
		return data.charAt(0);
		
	}
}