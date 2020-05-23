package SP18_simulator;

import java.io.File;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;
	boolean jump=false;

	public SicSimulator(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {
		rMgr.MovePCReg(0);
		for(int i =0;i<10;i++) {
			rMgr.register[i]=0;
		}
	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
	 */
	public void oneStep() throws EndException {
		parseInstruction();
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
	}	
	
	public String parseInstruction() throws EndException{
		jump=false;
		if(rMgr.getMemory(rMgr.PCReg(),6).equals("3e2000")) {
			throw new EndException();
		}
		
		int operator = Integer.parseInt(rMgr.getMemory(rMgr.PCReg(),2),16);
		int xbpe = Integer.parseInt(rMgr.getMemory(rMgr.PCReg()+1,1),16);
		String target;
		int targetAdr=0;
		
		if(operator == 0xb4 || operator == 0xb8 || operator == 0xa0) {
			target = rMgr.getMemory(rMgr.PCReg(),4);
		}
		else if((xbpe & 1) == 1){
			target = rMgr.getMemory(rMgr.PCReg(),8);
		}
		else {
			target = rMgr.getMemory(rMgr.PCReg(),6);
		}
		
		//��ɾ�� ũ�� �ε巹������,����������,������������,compare,device,rsub,���������������� ������.
		if(operator==0x01||operator==0x03 || operator == 0x77 || operator == 0x53) {
			this.loadReg(target);
		}
		else if(operator==0x17 ||operator == 0x57||operator==0x0F || operator == 0x13) {
			this.storeReg(target);
		}
		else if(operator == 0x33 || operator == 0x4b || operator == 0x3f || operator ==0x3E
				|| operator == 0x3b) {
			targetAdr = this.JReg(target);
		}
		else if(operator == 0x29 || operator == 0xa0 ) {
			this.comp(target);
		}
		else if(operator == 0xE3 || operator == 0xDF || operator == 0xDB) {
			this.device(target);
		}
		else if(operator==0x4f) {
			targetAdr = this.rsub(target);
		}
		else if(operator == 0xB4 || operator == 0xB8) {
			this.manageReg(target);
		}
		
		//PCReg�� �����Ѵ�.
		if(operator == 0xB4 || operator == 0xB8 || operator == 0xa0) {
			rMgr.AddPCReg(2);
		}
		else if(jump == true) {
			rMgr.MovePCReg(targetAdr);
		}
		else if((xbpe & 1) == 1) {
			rMgr.AddPCReg(4);
		}
		
		else {
			rMgr.AddPCReg(3);
		}
		return target;
		//1. �Ϲ� JEQ���� ��ɾ �ƴ� ��
		//1-1. n,i �÷��� �з�
		//1-2. xbpe �÷��� �з�
		//2. JEQ���� ��ɾ��� ��
	}
	
	public void loadReg(String target) {
		
		//LDA immediate 
		if(target.substring(0, 2).equals("01")) {
			rMgr.setRegister(0,Integer.parseInt(target.substring(3, 6),16));
		}
		//LDA simple (TA-PCó��)
		if(target.substring(0, 2).equals("03")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr += rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			rMgr.setRegister(0, Integer.parseInt(rMgr.getMemory(targetAdr,6),16));
		}
		//LDT simple(TA-PCó��)
		else if (target.substring(0, 2).equals("77")) {
			if((target.charAt(2) & 1) == 1) {
				rMgr.setRegister(5,Integer.parseInt(rMgr.getMemory(Integer.parseInt(target.substring(3, 8),16),6),16));
			}
			else {
				int targetAdr;
				if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
					targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
					targetAdr += rMgr.PCReg()-targetAdr;
				}
				else {
					targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
				}
				rMgr.setRegister(5,Integer.parseInt(rMgr.getMemory(targetAdr,6),16));
			}
		}
		//LDCH X
		else if(target.substring(0, 2).equals("53")) {
			int targetAdr = Integer.parseInt(target.substring(3,8),16) + rMgr.getRegister(1);
			rMgr.setRegister(0, Integer.parseInt(rMgr.getMemory(targetAdr, 2),16));
		} 
	}
	
	
	public void storeReg(String target) {
		//STL simple only (TA-PCó��)
		if(target.substring(0, 2).equals("17")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr += rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			rMgr.setMemory(targetAdr,String.format("%06x",rMgr.getRegister(0)),6);
		}
		//STCH X
		else if(target.substring(0, 2).equals("57")) {
			int targetAdr = Integer.parseInt(target.substring(3,8),16) + rMgr.getRegister(1);
			rMgr.setMemory(targetAdr, String.format("%02x",rMgr.getRegister(0)), 2);
		} 
		//STA simple
		else if(target.substring(0, 2).equals("0f")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr += rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			rMgr.setMemory(targetAdr, String.format("%06x",rMgr.getRegister(0)), 6);
			
		} 
		//STX +simple 
		else if(target.substring(0, 2).equals("13")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr += rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 8),16);
			}
			
			rMgr.setMemory(targetAdr, String.format("%06x",rMgr.getRegister(1)), 6);
		} 
		
	}
	public int JReg(String target) {
		//JEQ,��� �б⹮�� ������ �����ϴ��� ���캻 ���� jump���� ����
		if(target.substring(0, 2).equals("33")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr = rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			if(rMgr.getRegister(9)==0) {
				jump=true;
				return targetAdr;
			}
		}
		//JSUB+
		else if(target.substring(0, 2).equals("4b")) {
			int targetAdr = Integer.parseInt(target.substring(3, 8),16);
			rMgr.setRegister(2, rMgr.PCReg()+4);
			jump=true;
			return targetAdr;
		}
		//J simple
		else if(target.substring(0, 2).equals("3f")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr = rMgr.PCReg()-targetAdr+3;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			jump=true;
			return targetAdr;
		}
		//J indirect
		else if(target.substring(0, 2).equals("3e")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr = rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			targetAdr = Integer.parseInt(rMgr.getMemory(targetAdr,6),16);
			jump=true;
			return targetAdr;

		}
		//JLT simple
		else if(target.substring(0, 2).equals("3b")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr = rMgr.PCReg()-targetAdr+3;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			if(rMgr.getRegister(9)==-1) {
				jump=true;
				return targetAdr;
			}
		}
		return 0;
	}
	public void comp(String target) {
		//comp 0
		if(target.substring(0, 2).equals("29")) {
			if(rMgr.getRegister(0)>0) {
				rMgr.setRegister(9,1);
			}
			else if(rMgr.getRegister(0)<0) {
				rMgr.setRegister(9,-1);
			}
			else {
				rMgr.setRegister(9,0);
			}
		}
		//compr
		if(target.substring(0,2).equals("a0")) {
			if(rMgr.getRegister(Integer.parseInt(target.substring(2,3)))
					> rMgr.getRegister(Integer.parseInt(target.substring(3,4)))) {
				rMgr.setRegister(9,1);
			}
			if(rMgr.getRegister(Integer.parseInt(target.substring(2,3)))
					> rMgr.getRegister(Integer.parseInt(target.substring(3,4)))) {
				rMgr.setRegister(9,-1);
			}
			else {
				rMgr.setRegister(9,0);
			}
		}
	}
	public void device(String target) {
		//TD
		if(target.substring(0,2).equals("e3")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr = rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			
			rMgr.testDevice(rMgr.getMemory(targetAdr, 2));
			rMgr.setRegister(9,1);
		}
		//WD
		if(target.substring(0,2).equals("df")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr = rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			
			String temp = rMgr.getMemory(targetAdr, 2);
			int tempobj = rMgr.getRegister(0) & 0b11111111;
			rMgr.writeDevice(temp, tempobj);
		}
		//RD
		if(target.substring(0,2).equals("db")) {
			int targetAdr;
			if(Integer.parseInt(target.substring(3, 6),16)>0xf00) {
				targetAdr = 0x1000-Integer.parseInt(target.substring(3, 6),16);
				targetAdr = rMgr.PCReg()-targetAdr;
			}
			else {
				targetAdr = Integer.parseInt(target.substring(3, 6),16)+rMgr.PCReg()+3;
			}
			int temp;
			temp = (int) rMgr.readDevice(rMgr.getMemory(targetAdr, 2)).charAt(0);
			if (temp == 0x30) {temp = 0;}
			rMgr.setRegister(0, temp);
			//rMgr.setRegister(9,rMgr.getRegister(2));
		}
	}
	public int rsub(String target) {
		//RSUB
		if(target.substring(0,2).equals("4f")) {
			jump=true;
		}
		return rMgr.getRegister(2);
	}
	public void manageReg(String target) {
		//CLEAR
		if(target.substring(0,2).equals("b4")) {
			rMgr.setRegister(Integer.parseInt(target.substring(2,3)),0);
		}
		//TIXR
		if(target.substring(0,2).equals("b8")) {
			rMgr.setRegister(1,rMgr.getRegister(1)+1);
			
			if(rMgr.getRegister(1)>rMgr.getRegister(Integer.parseInt(target.substring(2,3)))) {
				rMgr.setRegister(9,1);
			}
			if(rMgr.getRegister(1)<rMgr.getRegister(Integer.parseInt(target.substring(2,3)))) {
				rMgr.setRegister(9,-1);
			}
			else {
				rMgr.setRegister(9,0);
			}
		}
	}
}

