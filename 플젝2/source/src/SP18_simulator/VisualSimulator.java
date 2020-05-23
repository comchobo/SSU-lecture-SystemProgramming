package SP18_simulator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * VisualSimulator�� ����ڿ��� ��ȣ�ۿ��� ����Ѵ�.<br>
 * ��, ��ư Ŭ������ �̺�Ʈ�� �����ϰ� �׿� ���� ������� ȭ�鿡 ������Ʈ �ϴ� ������ �����Ѵ�.<br>
 * �������� �۾��� SicSimulator���� �����ϵ��� �����Ѵ�.
 */
public class VisualSimulator {
	
	static ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);
	static designed GUI;
	File input;
	boolean flag=false;
	
	/**
	 * ���α׷� �ε� ����� �����Ѵ�.
	 */
	public void load(String program) throws FileNotFoundException {
		File input = new File(program);
		sicLoader.load(input);				//���̺��� �����ϰ� �ڵ带 �ε�
		sicSimulator.load(input);				//
		resourceManager.initializeResource();	//���̺� ���� �ڵ带 ����
		show();
		flag=true;
	}

	/**
	 * �ϳ��� ��ɾ ������ ���� SicSimulator�� ��û�Ѵ�.
	 */
	public String oneStep() throws EndException,loadException {
		if(flag==false) throw new loadException();
		return sicSimulator.parseInstruction();
	}

	/**
	 * �����ִ� ��� ��ɾ ������ ���� SicSimulator�� ��û�Ѵ�.
	 */
	public void allStep() throws EndException,loadException {
		if(flag==false) throw new loadException();
		while(true) {
			sicSimulator.parseInstruction();
		}
	}
	
	//�̴� �ڵ��� �Ϻθ� �����ش�. Ȥ�� �����ּҺ����� �Ϻθ� �����ش�.
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
