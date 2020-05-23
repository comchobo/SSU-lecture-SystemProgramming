package SP18_simulator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator {
	
	static ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	static designed GUI;
	File input;
	boolean flag=false;
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(String program) throws FileNotFoundException {
		File input = new File(program);
		sicLoader.load(input);				//테이블을 생성하고 코드를 로드
		sicSimulator.load(input);				//
		resourceManager.initializeResource();	//테이블에 따라 코드를 수정
		show();
		flag=true;
	}

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public String oneStep() throws EndException,loadException {
		if(flag==false) throw new loadException();
		return sicSimulator.parseInstruction();
	}

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep() throws EndException,loadException {
		if(flag==false) throw new loadException();
		while(true) {
			sicSimulator.parseInstruction();
		}
	}
	
	//이는 코드의 일부를 돌려준다. 혹은 시작주소부터의 일부를 돌려준다.
	public String show() {
		String temp = resourceManager.getMemory(0, 100);
		return temp;
	}
	public String show(int Adr) {
		String temp = resourceManager.getMemory(Adr, 100);
		return temp;
	}
	
	public static void main(String[] args) {
		VisualSimulator Simulator = new VisualSimulator();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI = new designed(Simulator,resourceManager);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
