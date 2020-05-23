import java.util.*;

//��ū���̺��� ������ ����ִ� Ŭ����
public class TokenTable {
	public static int locctr=0;
	public static final int MAX_OPERAND=3;
	public static int i=0; // index
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	InstTable instTab;
	LitTable litTab;
	ArrayList<Token> tokenList;
	ExtTable ExtTab;
	String FinalCode="";	//������ �ڵ带 ����
	
	public TokenTable() {
		tokenList = new ArrayList<Token>();
	}
	
	// �ɺ����̺�� ���ͷ����̺�,�ܺ��ּ� ���̺��� �����Ѵ�.
	// ��ū���̺� �����ڿ��� �����ϴ� ��� ���Ŀ� ��ũ�ϵ��� �����Ͽ���.
	public void linkInst(InstTable a) {
		instTab = a;
	}
	public void linkLit(LitTable a) {
		litTab = a;
	}
	public void linkSym(SymbolTable a) {
		symTab = a;
	}
	public void linkExt(ExtTable a) {
		ExtTab = a;
	}
	
	//��ɾ� �� ������ ��ū���� ����.
	public void putToken(String line,InstTable instTab) throws SectorChange{
		tokenList.add(new Token(line,instTab));
		if(tokenList.get(tokenList.size()-1).operator.equals("CSECT"))
			throw new SectorChange();
	}
	
	
	//����Ʈ�������� �Ǿ������Ƿ� Ǫ��,�� �޼ҵ带 �����Ѵ�.
	public void pushToken(Token a) {
		tokenList.add(a);
	}
	public Token popToken() {
		Token temp = tokenList.get(tokenList.size()-1);
		tokenList.remove(tokenList.size()-1);
		return temp;
	}
	
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	//�ɺ� ���̺��� �����ϴ� �Լ�.
	public void makeSymtab () throws SectorChange {
		symTab.init();
		
		for(int i =0; i<tokenList.size();i++) {
			if(tokenList.get(i).label == "") continue;
			else {
				//�̹� ���Ե� �ɺ��� �н�
				if(symTab.symbolList.size()==0 || 
						!(symTab.symbolList.contains(tokenList.get(i).label))) 
					symTab.putSymbol(tokenList.get(i).label, tokenList.get(i).location);
				else continue;
			}
		}
		throw new SectorChange(); // ���Ͱ� �ٲ� �� ����ü������ ���ο�.
	}
	
	//���ͷ� ���̺��� �����ϴ� �޼ҵ�
	public void makeLit() throws SectorChange {
		int LTORG_adr=0; // LTORG ���θ� �˻�
		int i;
		litTab.init();
		
		for(i =0; i<tokenList.size();i++) {
			//LTORG�� ��� �ش� ��ġ�� �����Ѵ�.
			if (tokenList.get(i).operator.equals("LTORG")) {
				LTORG_adr = tokenList.get(i).location;
				litTab.LTORG_flag = true;
			}
			else if(tokenList.get(i).operand[0] == "") continue;
			
			else if(tokenList.get(i).operand[0].startsWith("=")) {
				if(litTab.nameList.size()==0 || 
						!(litTab.nameList.contains(tokenList.get(i).operand[0]))) {
					if(tokenList.get(i).operand[0].startsWith("=X")) {
						litTab.nameList.add(tokenList.get(i).operand[0]);
						String[] temp = tokenList.get(i).operand[0].split("'");
						litTab.valueList.add(Integer.parseInt(temp[1],16));
						litTab.StringList.add("");
						litTab.IsStringList.add(false);
						litTab.Table_Size +=1;
						litTab.SizeList.add(1);
					}
					
					//���ڿ��� ���
					else if (tokenList.get(i).operand[0].startsWith("=C")) {
						litTab.nameList.add(tokenList.get(i).operand[0]);
						String[] temp = tokenList.get(i).operand[0].split("'");
						litTab.valueList.add(-1);
						litTab.StringList.add(temp[1]);
						litTab.IsStringList.add(true);
						litTab.Table_Size +=3;
						litTab.SizeList.add(3);
					}
				}
				else continue;
					
			}
		}
		
		//���ͷ����� �ּҸ� ����Ѵ�.
		litTab.calSize(LTORG_adr,tokenList.get(i-1).location);
		
		//LTORG ������ �� ���� ��� ��ū�� �߰��Ѵ�.
		
		throw new SectorChange();
	}
	
	//���ͷ� ó�� ������ �ּҸ� ����Ѵ�.
	public void calLit() {
		int temp=0;
		boolean flag=false;
		if(litTab.LTORG_flag==true) {
			for(int i =0; i<tokenList.size();i++) {
				if(flag==true) {
					tokenList.get(i).location += temp;
				}
				
				if(tokenList.get(i).operator.equals("LTORG")) {
					flag = true;
					temp = litTab.Table_Size;
				}
			}
		}
	}
	
	//�ܺ��ּ� ���̺��� �����ϴ� �޼ҵ�
	public void makeExt() throws SectorChange {
		ExtTab.init();
		for(int i =0; i<tokenList.size();i++) {
			
			//ExtDef�� ���� ExtRef�� ���� �ٸ��� �����Ѵ�.
			if(tokenList.get(i).operator.equals("EXTDEF")) {
				for(int j = 0; j<tokenList.get(i).operandN;j++) {
					ExtTab.Is_ExtR.add(false);
					String temp_operand = tokenList.get(i).operand[j]; // ������
					ExtTab.ExtName.add(temp_operand);
					ExtTab.ExtAdr.add(symTab.search(temp_operand));
					
				}
			}
			else if(tokenList.get(i).operator.equals("EXTREF")) {
				for(int j = 0; j<tokenList.get(i).operandN;j++) {
					ExtTab.Is_ExtR.add(true);
					String temp_operand = tokenList.get(i).operand[j]; // ������
					ExtTab.ExtName.add(temp_operand);
					ExtTab.ExtAdr.add(-1);
				}
			}
		}
	}
	
	// ������Ʈ �ڵ带 ����� �޼ҵ�
	public void makeObjectCode() throws SectorChange {
		for(int i =0; i<tokenList.size();i++) {
			
			//��ɾ inst table ��Ͽ� ���� ���
			if(instTab.instMap.containsKey(tokenList.get(i).operator)) {
				Instruction temp = instTab.instMap.get(tokenList.get(i).operator);
				int opcode = temp.opcode;
				char nixbpe = tokenList.get(i).nixbpe;
				int format = temp.format;
				
				//ni�κп� ���� opcode ����
				int ni =0;
				if(tokenList.get(i).getFlag(nFlag)==32) ni+=2;
				if(tokenList.get(i).getFlag(iFlag)==16) ni+=1;
				int xbpe = tokenList.get(i).getFlag(xFlag|bFlag|pFlag|eFlag);
				if (ni == 2) opcode += 2;
				else if (ni==1) opcode += 1;
				else if (ni==3) opcode += 3;
				else opcode += 0;
				tokenList.get(i).objectCode += String.format("%02x",opcode);
				
				//��ɾ��� ���˿� ���� �б����� ������.
				//���� 2�� ��� ���������� ��ȣ�� �����Ѵ�.
				if(format == 2) {
					if(tokenList.get(i).operandN == 1) {
						tokenList.get(i).objectCode += search_reg(tokenList.get(i).operand[0]);
						tokenList.get(i).objectCode +="0";
					}
					else {
						tokenList.get(i).objectCode += search_reg(tokenList.get(i).operand[0]);
						tokenList.get(i).objectCode += search_reg(tokenList.get(i).operand[1]);
					}
				}
				
				//���� 3�� ��� �켱 operand�� ������ ������.
				else if (format == 3) {
					if(tokenList.get(i).operandN!=0) {
						tokenList.get(i).objectCode += xbpe;
						//���� immediate,indirect,literal,PC relative�� ���� �бⰡ ������.
						//immediate �б�
						if(tokenList.get(i).operand[0].startsWith("#")) {
							tokenList.get(i).objectCode += String.format("%03x",
									Integer.parseInt(tokenList.get(i).operand[0].split("#")[1]));
						}
						
						//indirect �б�
						else if(tokenList.get(i).operand[0].startsWith("@")) {
							Scanner temp_Scanner = new Scanner(tokenList.get(i).operand[0]).useDelimiter("@");
							int TA = symTab.search(temp_Scanner.next());
							int PC = tokenList.get(i).location + tokenList.get(i).byteSize;
							int disp = TA-PC;
							
							if(disp<0) {
								disp = Integer.parseUnsignedInt(Integer.toString(disp));
								disp = disp <<20;
								disp = disp >>20;
							}
							tokenList.get(i).objectCode += String.format("%03x", disp);
						}
						
						//literal �б�
						else if(tokenList.get(i).operand[0].startsWith("=")) {
							int TA = litTab.searchLit(tokenList.get(i).operand[0]);
							int PC = tokenList.get(i).location + tokenList.get(i).byteSize;
							int disp = TA-PC;
							
							if(disp<0) {
								disp = Integer.parseUnsignedInt(Integer.toString(disp));
								disp = disp <<20;
								disp = disp >>20;
							}
							tokenList.get(i).objectCode += String.format("%03x", disp);
						}
						
						//PC relative �б�
						else {
							int TA = symTab.search(tokenList.get(i).operand[0]);
							int PC = tokenList.get(i).location + tokenList.get(i).byteSize;
							int disp = TA-PC;
							
							if(disp<0) {
								long l = disp & 0xffffffffL;
								l = l & 0x00000fffL;
								disp = (int) (long) l;
							}
							tokenList.get(i).objectCode += String.format("%03x", disp);
						}
					}
					else {
						tokenList.get(i).objectCode += xbpe;
						tokenList.get(i).objectCode += "000";
					}
				}
				
				//4������ ��� �ܺ��ּҸ� 0, �ƴϸ�(immediate) ���� �״�� �޴´�.
				else if (format == 4) {
					tokenList.get(i).objectCode += xbpe;
					if(tokenList.get(i).operand[0].startsWith("#")) {
						Scanner temp_Scanner = new Scanner(tokenList.get(i).operand[0]).useDelimiter("#");
						tokenList.get(i).objectCode += String.format("%05x", temp_Scanner.next());
					}
					else{
						tokenList.get(i).objectCode += "00000";
					}
				}
			}
			
			//WORD�� BYTE�� ��츦 ó���Ѵ�.
			else {
				if(tokenList.get(i).operator.equals("WORD")){
					tokenList.get(i).byteSize=3;
					if(tokenList.get(i).operand[0].contains("'")) {
						Scanner temp_Scanner = new Scanner(tokenList.get(i).operand[0]).useDelimiter("'");
						temp_Scanner.next();
						String temp_String = temp_Scanner.next();
						tokenList.get(i).objectCode += String.format("%02x", temp_String.charAt(0));
						tokenList.get(i).objectCode = String.format("%02x", temp_String.charAt(1));
						tokenList.get(i).objectCode = String.format("%02x", temp_String.charAt(2));
					}
					else {
						//���� ������ ��� �״�� ����������, �ƴ� ��� �ܺ��ּ� ����̹Ƿ� �ܺ��ּ� �÷��׸� �Ҵ�.
						if(isInteger(tokenList.get(i).operand[0])) {
							tokenList.get(i).objectCode = String.format("%06x",Integer.parseInt(tokenList.get(i).operand[0]));
						}
						else {
						tokenList.get(i).ExtFlag = true;
						tokenList.get(i).objectCode = "000000";
						}
					}
				}
				else if (tokenList.get(i).operator.equals("BYTE")) {
					tokenList.get(i).byteSize=1;
					Scanner temp_Scanner = new Scanner(tokenList.get(i).operand[0]).useDelimiter("'");
					temp_Scanner.next();
					tokenList.get(i).objectCode = temp_Scanner.next();
				}
				
			}
		}
		
		throw new SectorChange();
	}
	
	//���������� ��ȣ�� ã�� �޼ҵ�
	public String search_reg(String a) {
		if (a.equals("A")) return "0";
		else if(a.equals("X")) return "1";
		else if (a.equals("L")) return "2";
		else if (a.equals("B")) return "3";
		else if (a.equals("S")) return "4";
		else if (a.equals("T")) return "5";
		else if (a.equals("F")) return "6";
		else if (a.equals("PC")) return "8";
		else if (a.equals("SW")) return "9";
		return "-1";
	}
	
	//�������� �Ǻ��ϴ� �Լ�
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    } catch(NullPointerException e) {
	        return false;
	    }
	    return true;
	}
	
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
	//���� ������Ʈ �ڵ带 ��Ʈ���� �����ϴ� �޼ҵ�
	public void buildObjectCode() throws SectorChange{
		
		//���� ���
		FinalCode=String.format("H%s\t000000",tokenList.get(0).label);
		int temp=0;
		//���ͷ� ó�� ���ο� ���� ���� ������ ����
		if(litTab.LTORG_flag==true) {
			temp= tokenList.get(tokenList.size()-1).location+ tokenList.get(tokenList.size()-1).byteSize;
		}
		else {
			temp= tokenList.get(tokenList.size()-1).location + litTab.Table_Size+ tokenList.get(tokenList.size()-1).byteSize;
		}
		FinalCode+=String.format("%06x\r\n",temp);
		
		//�ܺ����̺� ���
		int i = 0;
		if(ExtTab.Is_ExtR.get(0)==false) {
			FinalCode+="D";
			for(i=0; ExtTab.Is_ExtR.get(i)==false;i++) {
				FinalCode+=String.format("%s%06x",ExtTab.ExtName.get(i),ExtTab.ExtAdr.get(i));
			}
		}
		FinalCode += "\r\nR";
		for(; i<ExtTab.ExtName.size();i++) {
			FinalCode+=String.format("%s",ExtTab.ExtName.get(i));
		}
		FinalCode += "\r\n";
		
		//�ڵ����̺� ���
		for(i=0;i<tokenList.size();) {
			//���̺���
			int codelength=0;	//�ִ�� �Է��� �ڵ��� ����
			int temp_start=0;	//���������� �ּҸ� ����
			if(tokenList.get(i).byteSize!=0) temp_start = tokenList.get(i).location;
			int start_index=i;	//���������� �ε����� ����
			for(;;i++) {
				//���� �Է��� ������Ʈ �ڵ尡 ���ٸ� �ڵ���̴� 0����.
				//���� �ڵ尡 ����Ǳ� �����̶�� ���� LTORG������� ���� ���ͷ� ���̺��� ó���Ѵ�.
				if(i>=tokenList.size()||tokenList.get(i).byteSize==0 || codelength>27) {
					if(litTab.LTORG_flag==false && i==tokenList.size()-1) codelength += litTab.Table_Size;
					break;
				}
				codelength += tokenList.get(i).byteSize;
			}
			
			//LTORGó���� ���ͷ� ��º�, �ε����� �ʰ���Ű�� �ʱ� ���� i�� �ε����� üũ�� �� elseif�� �޴´�.
			if(i>=tokenList.size()) {}
			else if(tokenList.get(i).operator.equals("LTORG")) {
				FinalCode+=String.format("T%06x%02x",litTab.locationList.get(0),litTab.Table_Size);
				
				//�ڵ� ���
				for(int j=0;j<litTab.locationList.size();j++) {
					//String ���ο� ���� �б⸦ ������ ����Ѵ�.
					if(litTab.IsStringList.get(j)==true) {
						for(int k =0;k<3;k++) {
							FinalCode+=String.format("%02x",(int)litTab.StringList.get(j).charAt(k));
						}
					}
					else FinalCode+=String.format("%02x",(int)litTab.valueList.get(j));
				}
				FinalCode+="\r\n";
			}
			
			//T�ڵ� ���, �ڵ���̰� 0�̸� ��������
			if(codelength !=0) {
				FinalCode+=String.format("T%06x%02x",temp_start,codelength);
				
				//�ڵ� ���
				for(int j=start_index;j!=i;j++) 
					//�ִ� 30����Ʈ���� �ڵ� ���, ���� ��ū�� ������Ʈ�ڵ带 ���� �ʾƵ� break.
					FinalCode+=String.format("%s",tokenList.get(j).objectCode);
				
				//LTORG ó������ ���� ���ͷ��� ó���Ѵ�.
				if(litTab.LTORG_flag==false && i==tokenList.size()-1) {
					for(int j=0;j<litTab.nameList.size();j++) {
						if(litTab.IsStringList.get(j)==true) {
							for(int k =0;k<3;k++) {
								FinalCode+=String.format("%02x",(int)litTab.StringList.get(j).charAt(k));
							}
						}
						else FinalCode+=String.format("%02x",(int)litTab.valueList.get(j));
					}
					i++;
				}
				FinalCode+="\r\n";
			}
			else i++;
		}
		
		//Mod ���, �ڵ带 �ѹ� �� �ȴ´�.
		for(i=0;i<tokenList.size();i++) {
			if(tokenList.get(i).byteSize==4) {
				FinalCode+=String.format("M%06x05+%s\r\n"
						,tokenList.get(i).location+1,tokenList.get(i).operand[0]);
			}
			//token.ExtFlag�� ���� ����� mod����
			else if (tokenList.get(i).ExtFlag==true) {
				String tempString = "";		//���� �ϳ��� �޾� ���� ���̺��� ���
				boolean isminus = false;	//�տ��ִ� �� ���̳ʽ����� ����
				int operatorIndex=0;		//��ȣ�� �ִ� �ڸ��� �ε���
				
				//���ڿ��� ���̸�ŭ �ε����� �ö󰡸� ����.
				for(int j = 0;j<tokenList.get(i).operand[0].length();) {
						
					//isminus�� ���� ���۷����Ͱ� ���̳ʽ����� �ƴ��� �Ǻ�
					if(operatorIndex==0);
					else if(tokenList.get(i).operand[0].charAt(operatorIndex)=='-') isminus = true;
					else isminus = false;
				
					//+�� -�� ���� �� ���� ���ڸ� �ϳ��� �д´�.
					for(;tokenList.get(i).operand[0].charAt(j)!='+'&&tokenList.get(i).operand[0].charAt(j)!='-';j++) {
						tempString+=tokenList.get(i).operand[0].charAt(j);
						if(j==tokenList.get(i).operand[0].length()-1) break;
					}
					if(isminus == false) FinalCode+=String.format("M%06x06+%s\r\n",tokenList.get(i).location,tempString);
					else FinalCode+=String.format("M%06x06-%s\r\n",tokenList.get(i).location,tempString);
					operatorIndex = j;
					tempString="";
					j++;
				}
				FinalCode+="\r\n";
			}
		}
		//E ���
		FinalCode+="E\r\n";
		//���� ����ü������ �������ͷ�
		throw new SectorChange();
	}
	
}

//��ū Ŭ����
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe=0;
	int operandN=0; // ���Ǵ� ���۷����� ����
	

	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode="";
	int byteSize;
	boolean ExtFlag = false; // WORD���� �ܺ��ּҰ� ���� ���
	
	//�Ϲ����� ��ū ������
	public Token(String line,InstTable instTab){
		location = TokenTable.locctr;
		parsing(line);
		if(operator.equals("CSECT")) {
			TokenTable.locctr = 0;
			location=0;
		}
		
		//��ɾ instTable�� �ִٸ� byteSize��, �ƴϸ� process_locctr�� ����.
		if(instTab.instMap.get(operator) == null) {process_locctr();}
		else {
			byteSize = instTab.instMap.get(operator).format;
			TokenTable.locctr += byteSize;
		}
		
		setAddressing();
		setRelative();
		
	}
	
	//���α׷� �� ������ ��ū���� �����.
	public void parsing(String line) {
		if(line.startsWith(".")) return; // �ּ��̸� �׳� �� �޴´�.
		Scanner temp_line = new Scanner(line).useDelimiter("\t");
		
		//������ �����Ѵٸ� (���̺��� ����ִٸ�) �ٷ� ���۷����Ϳ� ���� �޴´�.
		if(!(line.startsWith("\t"))) {label = temp_line.next();}
		else label ="";
		
		operator = temp_line.next();
		operand = new String[3];
		try{ 
			String temp_operand = temp_line.next();
			
			// ���۷����� ��� �Ѳ����� ������ ���� ���۷��� ������ �����ش�.
			parse_operand(temp_operand);
		}
		catch(NoSuchElementException e) {operand[0] = "";}
		
		try {comment = temp_line.next();}
		catch(NoSuchElementException e) {comment = "";}
	}
	
	//���۷��� �������� �Լ�
	public void parse_operand(String line) {
		Scanner temp_line = new Scanner(line).useDelimiter(",");
		int i=0;
		try {
			for(;;i++){
				operandN = i;
				operand[i] = temp_line.next();
			}
		}
		catch(NoSuchElementException e) {
			if(i==3) return;
			operand[i]="";
			return;
			}
	}
	
	//operator�� insttable�� ���� ��� locctr�� ó���ϴ� �޼ҵ�
	public void process_locctr() {
		int temp=0;
		if (operator.equals("WORD")) TokenTable.locctr += 3;	
		else if (operator.equals("BYTE")) TokenTable.locctr += 1;
		else if (operator.equals("RESB")){
			TokenTable.locctr += (1 * Integer.parseInt(operand[0]));
		}
		else if (operator.equals("RESW")) {
			TokenTable.locctr += (3 * Integer.parseInt(operand[0]));
		}
		else {}
	}
	
	//flag ������ �ٷ�� �޼ҵ�
	public void setFlag(int flag, int value) {
		 if (value==1) {nixbpe += flag;}
	}
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	
	
	//n,i�� �� ������ ����Ѵ�.
	public void setAddressing() {
		if (byteSize > 2) {
			if (operand[0] != "") {
				if (operand[0].charAt(0) == '#') 
					setFlag(TokenTable.iFlag,1);
				else if (operand[0].charAt(0) == '@')
					setFlag(TokenTable.nFlag,1);
				else {
					setFlag(TokenTable.nFlag,1);
					setFlag(TokenTable.iFlag,1);
				}
			}
			else {
				setFlag(TokenTable.nFlag,1);
				setFlag(TokenTable.iFlag,1);
			}
		}
	}
	
	//x,b,p,e�� �� ������ ����Ѵ�.
	public void setRelative() {
		if (byteSize == 3) {
			if (operand[0] != "") {
				if (operand[0].charAt(0) == '#');
				else setFlag(TokenTable.pFlag,1);
			}
		}
		else if (byteSize == 4) {
			if (operator.contains("+")) setFlag(TokenTable.eFlag,1);
		}
		
		if (operandN <2) ;
		else if (operand[1].charAt(0) == 'X') setFlag(TokenTable.xFlag,1);
	}
}
