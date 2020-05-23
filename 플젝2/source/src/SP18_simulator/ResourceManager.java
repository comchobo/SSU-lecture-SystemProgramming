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
	//��Ʈ���� ��� �� ���ڰ� �����̹Ƿ� �α���(1byte) ���� �ּҸ� ������ �ʿ䰡 �ִ�.
	StringBuilder memory = new StringBuilder(65536);
	int[] register = new int[10];
	double register_F;
	String recentDevice=new String("NULL"); //�ֱٿ� �׽�Ʈ�� ����̽��� ����.
	Scanner temp;
	int buffer=0;
	File devNameFile;
	
	SymbolTable symtab;
	SectTable secttab;
	ModTable modtab;
	
	public ResourceManager(){
		//memory�� �������� ���� 0���� �ʱ�ȭ�Ѵ�.
			for(int i =0;i<65536;i++) {
				memory.insert(i, "0");
			}
				
	}

	//�ܺ��ּ� ���̺� ���� �ڵ带 �����Ѵ�.
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
	
	//�� ���̺���� �ε��Ѵ�.
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
	
	//Hashmap�� �����.
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

	//device ����¿� ���� ���� ���� ���Ƿ� 'string' ������ hashmap�� �������.
	//�̴� ���� closedevice���� ������ ��������� ���·� ���� �� �ִ� �ڿ��� �ȴ�.
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

	//�����δ� SIC/XE�� ǥ���� �޸𸮸� �״�� �ٷ�����, Memory �޼ҵ带
	//�����ϴ� ������ ���� ��Ʈ���� ������ Ȱ���� �� �ִ�. �ϳ��� sectadr����, �ϳ��� 0.5��ŭ ���� �����ϵ���, �ϳ��� ������.
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
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public String intToString(int data){
		return Integer.toString(data);
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int byteToInt(String data){
		return data.charAt(0);
		
	}
}