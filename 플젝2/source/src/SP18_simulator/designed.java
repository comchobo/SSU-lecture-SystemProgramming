package SP18_simulator;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextArea;
import java.awt.Font;

public class designed {

	ResourceManager resourcemanager;
	private JFrame frmSpsimulator;
	JTextArea FilenameArea;
	JTextArea InstructionArea;
	JTextArea InstructionArea2;
	String filename;
	JTextArea ARegArea;
	JTextArea BRegArea;
	JTextArea LRegArea;
	JTextArea XRegArea;
	JTextArea SRegArea;
	JTextArea PCRegArea;
	JTextArea SWRegArea;
	JTextArea FRegArea;
	JTextArea TRegArea;
	JTextArea DeviceArea;
	private JTextField GotoField;
	boolean flag = true; // ����ó���� ���� flag ����
	
	
	//���ҽ��Ŵ����� �ùķ����͸� ��ũ��Ų��.
	public designed(VisualSimulator Simulator,ResourceManager resourceManager) {
		initialize(Simulator);
		resourcemanager = resourceManager;
	}
	
	//�������� ���� �޼ҵ�
	public void replace(JTextArea obj,String input) {
		obj.setText(null);
		obj.append(input);
	}
	
	//�� �������Ϳ� device �׸��� ������Ʈ�ϴ� �޼ҵ�
	public void updateReg() {
		replace(ARegArea,Integer.toHexString(resourcemanager.getRegister(0)));
		replace(XRegArea,Integer.toHexString(resourcemanager.getRegister(1)));
		replace(LRegArea,Integer.toHexString(resourcemanager.getRegister(2)));
		replace(BRegArea,Integer.toHexString(resourcemanager.getRegister(3)));
		replace(SRegArea,Integer.toHexString(resourcemanager.getRegister(4)));
		replace(TRegArea,Integer.toHexString(resourcemanager.getRegister(5)));
		replace(FRegArea,Integer.toHexString(resourcemanager.getRegister(6)));
		replace(PCRegArea,Integer.toHexString(resourcemanager.getRegister(8)));
		replace(SWRegArea,Integer.toHexString(resourcemanager.getRegister(9)));
		
		if(!(resourcemanager.recentDevice.equals("NULL"))) {
			replace(DeviceArea,resourcemanager.recentDevice);
		}
		else {
			DeviceArea.setText(null);
		}
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(VisualSimulator Simulator) {

		frmSpsimulator = new JFrame();
		frmSpsimulator.setTitle("SP18_Simulator");
		frmSpsimulator.setBounds(100, 100, 703, 415);
		frmSpsimulator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSpsimulator.getContentPane().setLayout(null);
		
		//onestep ��ư����, �������Ϳ� ��ɾ ������Ʈ�Ѵ�.
		JButton oneStep = new JButton("oneStep");
		oneStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					InstructionArea2.setText(null);
					InstructionArea2.append(Simulator.oneStep());
					updateReg();
					InstructionArea.setText(null);
					InstructionArea.append(Simulator.show(resourcemanager.getRegister(8)));
				}
				catch(EndException e) {
					JOptionPane.showMessageDialog(null, "Program Ended", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				catch(loadException e) {
					JOptionPane.showMessageDialog(null, "File wasn't loaded", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		});
		oneStep.setBounds(159, 86, 125, 29);
		frmSpsimulator.getContentPane().add(oneStep);
		
		//allstep ��ư���� ����ñ��� onestep�� �ݺ��Ѵ�.
		JButton allStep = new JButton("allStep");
		allStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					Simulator.allStep();
				}
				catch(EndException e) {
					updateReg();
					JOptionPane.showMessageDialog(null, "Program Ended", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				catch(loadException e) {
					JOptionPane.showMessageDialog(null, "File wasn't loaded", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		});
		allStep.setBounds(301, 86, 125, 29);
		frmSpsimulator.getContentPane().add(allStep);
		
		//������ ������ �ε��ϴ� �޼ҵ�
		JButton load = new JButton("load file");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(flag) {
				try{Simulator.load(FilenameArea.getText());}
				catch(FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, "File must be chosen.", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				InstructionArea.append(Simulator.show());
				flag = false;
				}
				else {
					JOptionPane.showMessageDialog(null, "File was loaded already", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		load.setBounds(17, 44, 125, 29);
		frmSpsimulator.getContentPane().add(load);
		
		//Ư�� �ּҷ� ���ϴ� ��ư. Ÿ���� �̻��� ����� ����ó��.
		JButton btnGoto = new JButton("GoTo");
		btnGoto.setBounds(456, 86, 103, 29);
		frmSpsimulator.getContentPane().add(btnGoto);
		btnGoto.addActionListener(new ActionListener(){
			JFileChooser chooser = new JFileChooser();
			public void actionPerformed(ActionEvent arg0) {
				try {
				InstructionArea.setText(null);
				InstructionArea.append(Simulator.show(Integer.parseInt(GotoField.getText(),16)));
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "target was wrong!", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
		}});
		
		//������ �����ϴ� �޼ҵ�
		JButton btnOpenFile = new JButton("open file");
		btnOpenFile.addActionListener(new ActionListener() {
			JFileChooser chooser = new JFileChooser();
			public void actionPerformed(ActionEvent arg0) {
				if(flag) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"txt file only","txt");
				chooser.setFileFilter(filter);
				
				int ret = chooser.showOpenDialog(null);
				if(ret!=JFileChooser.APPROVE_OPTION) {
					JOptionPane.showMessageDialog(null, "File was not chosen", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
						
				String name = chooser.getSelectedFile().getPath();
				FilenameArea.append(name);
				}
				else {
					JOptionPane.showMessageDialog(null, "File was loaded already", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnOpenFile.setBounds(17, 8, 125, 29);
		frmSpsimulator.getContentPane().add(btnOpenFile);
		
		JLabel lblNewLabel = new JLabel("A");
		lblNewLabel.setBounds(17, 109, 19, 21);
		frmSpsimulator.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("B");
		lblNewLabel_1.setBounds(17, 229, 19, 21);
		frmSpsimulator.getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("X");
		lblNewLabel_2.setBounds(17, 133, 19, 21);
		frmSpsimulator.getContentPane().add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("L");
		lblNewLabel_3.setBounds(17, 156, 19, 21);
		frmSpsimulator.getContentPane().add(lblNewLabel_3);
		
		JLabel lblRegisters = new JLabel("Registers");
		lblRegisters.setFont(new Font("����", Font.PLAIN, 20));
		lblRegisters.setBounds(27, 86, 78, 21);
		frmSpsimulator.getContentPane().add(lblRegisters);
		
		JLabel lblPc = new JLabel("PC");
		lblPc.setBounds(5, 180, 31, 21);
		frmSpsimulator.getContentPane().add(lblPc);
		
		JLabel lblNextInstruction = new JLabel("Memory map");
		lblNextInstruction.setBounds(159, 204, 108, 21);
		frmSpsimulator.getContentPane().add(lblNextInstruction);
		
		JLabel lblFileName = new JLabel("File name:");
		lblFileName.setBounds(173, 34, 94, 21);
		frmSpsimulator.getContentPane().add(lblFileName);
		
		JLabel lblAddress = new JLabel("Instruction");
		lblAddress.setBounds(173, 154, 94, 24);
		frmSpsimulator.getContentPane().add(lblAddress);
		
		JLabel lblS = new JLabel("S");
		lblS.setBounds(17, 254, 19, 21);
		frmSpsimulator.getContentPane().add(lblS);
		
		JLabel lblT = new JLabel("T");
		lblT.setBounds(17, 280, 19, 21);
		frmSpsimulator.getContentPane().add(lblT);
		
		JLabel lblF = new JLabel("F");
		lblF.setBounds(17, 301, 19, 21);
		frmSpsimulator.getContentPane().add(lblF);
		
		JLabel lblSw = new JLabel("SW");
		lblSw.setBounds(5, 204, 31, 21);
		frmSpsimulator.getContentPane().add(lblSw);
		
		FilenameArea = new JTextArea();
		FilenameArea.setEditable(false);
		FilenameArea.setBounds(284, 35, 138, 24);
		frmSpsimulator.getContentPane().add(FilenameArea);
		
		InstructionArea2 = new JTextArea();
		InstructionArea2.setEditable(false);
		InstructionArea2.setBounds(284, 157, 138, 24);
		frmSpsimulator.getContentPane().add(InstructionArea2);
		
		InstructionArea = new JTextArea();
		InstructionArea.setFont(new Font("Arial", Font.PLAIN, 20));
		InstructionArea.setLineWrap(true);
		InstructionArea.setEditable(false);
		InstructionArea.setBounds(284, 205, 380, 134);
		frmSpsimulator.getContentPane().add(InstructionArea);
		
		ARegArea = new JTextArea();
		ARegArea.setEditable(false);
		ARegArea.setBounds(37, 110, 86, 20);
		frmSpsimulator.getContentPane().add(ARegArea);
		
		XRegArea = new JTextArea();
		XRegArea.setEditable(false);
		XRegArea.setBounds(37, 134, 86, 20);
		frmSpsimulator.getContentPane().add(XRegArea);
		
		PCRegArea = new JTextArea();
		PCRegArea.setEditable(false);
		PCRegArea.setBounds(37, 181, 86, 20);
		frmSpsimulator.getContentPane().add(PCRegArea);
		
		BRegArea = new JTextArea();
		BRegArea.setEditable(false);
		BRegArea.setBounds(37, 230, 86, 20);
		frmSpsimulator.getContentPane().add(BRegArea);
		
		TRegArea = new JTextArea();
		TRegArea.setEditable(false);
		TRegArea.setBounds(37, 281, 86, 20);
		frmSpsimulator.getContentPane().add(TRegArea);
		
		SRegArea = new JTextArea();
		SRegArea.setEditable(false);
		SRegArea.setBounds(37, 255, 86, 20);
		frmSpsimulator.getContentPane().add(SRegArea);
		
		SWRegArea = new JTextArea();
		SWRegArea.setEditable(false);
		SWRegArea.setBounds(37, 205, 86, 20);
		frmSpsimulator.getContentPane().add(SWRegArea);
		
		LRegArea = new JTextArea();
		LRegArea.setEditable(false);
		LRegArea.setBounds(38, 157, 86, 20);
		frmSpsimulator.getContentPane().add(LRegArea);
		
		FRegArea = new JTextArea();
		FRegArea.setEditable(false);
		FRegArea.setBounds(37, 305, 86, 20);
		frmSpsimulator.getContentPane().add(FRegArea);
		
		GotoField = new JTextField();
		GotoField.setBounds(588, 87, 76, 27);
		frmSpsimulator.getContentPane().add(GotoField);
		GotoField.setColumns(10);
		
		JLabel lblUsingDevice = new JLabel("Using Device:");
		lblUsingDevice.setBounds(461, 34, 110, 21);
		frmSpsimulator.getContentPane().add(lblUsingDevice);
		
		DeviceArea = new JTextArea();
		DeviceArea.setEditable(false);
		DeviceArea.setBounds(588, 35, 76, 24);
		frmSpsimulator.getContentPane().add(DeviceArea);
	
		frmSpsimulator.setVisible(true);
	}
}
