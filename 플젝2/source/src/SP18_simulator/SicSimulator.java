package SP18_simulator;

import java.io.File;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;
	boolean jump=false;

	public SicSimulator(ResourceManager resourceManager) {
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {
		rMgr.MovePCReg(0);
		for(int i =0;i<10;i++) {
			rMgr.register[i]=0;
		}
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 */
	public void oneStep() throws EndException {
		parseInstruction();
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
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
		
		//명령어는 크게 로드레지스터,스토어레지스터,점프레지스터,compare,device,rsub,레지스터조작으로 나뉜다.
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
		
		//PCReg를 조작한다.
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
		//1. 일반 JEQ등의 명령어가 아닐 때
		//1-1. n,i 플래그 분류
		//1-2. xbpe 플래그 분류
		//2. JEQ등의 명령어일 때
	}
	
	public void loadReg(String target) {
		
		//LDA immediate 
		if(target.substring(0, 2).equals("01")) {
			rMgr.setRegister(0,Integer.parseInt(target.substring(3, 6),16));
		}
		//LDA simple (TA-PC처리)
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
		//LDT simple(TA-PC처리)
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
		//STL simple only (TA-PC처리)
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
		//JEQ,모든 분기문은 조건을 만족하는지 살펴본 다음 jump변수 조작
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

