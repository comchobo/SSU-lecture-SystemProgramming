import java.util.*;

//토큰테이블의 정보가 들어있는 클래스
public class TokenTable {
	public static int locctr=0;
	public static final int MAX_OPERAND=3;
	public static int i=0; // index
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	InstTable instTab;
	LitTable litTab;
	ArrayList<Token> tokenList;
	ExtTable ExtTab;
	String FinalCode="";	//마지막 코드를 생성
	
	public TokenTable() {
		tokenList = new ArrayList<Token>();
	}
	
	// 심볼테이블과 리터럴테이블,외부주소 테이블을 연결한다.
	// 토큰테이블 생성자에서 삽입하는 대신 이후에 링크하도록 설정하였다.
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
	
	//명령어 한 라인을 토큰으로 삽입.
	public void putToken(String line,InstTable instTab) throws SectorChange{
		tokenList.add(new Token(line,instTab));
		if(tokenList.get(tokenList.size()-1).operator.equals("CSECT"))
			throw new SectorChange();
	}
	
	
	//리스트형식으로 되어있으므로 푸쉬,팝 메소드를 생성한다.
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
	
	//심볼 테이블을 생성하는 함수.
	public void makeSymtab () throws SectorChange {
		symTab.init();
		
		for(int i =0; i<tokenList.size();i++) {
			if(tokenList.get(i).label == "") continue;
			else {
				//이미 삽입된 심볼은 패스
				if(symTab.symbolList.size()==0 || 
						!(symTab.symbolList.contains(tokenList.get(i).label))) 
					symTab.putSymbol(tokenList.get(i).label, tokenList.get(i).location);
				else continue;
			}
		}
		throw new SectorChange(); // 섹터가 바뀔 때 섹터체인지를 쓰로우.
	}
	
	//리터럴 테이블을 생성하는 메소드
	public void makeLit() throws SectorChange {
		int LTORG_adr=0; // LTORG 여부를 검사
		int i;
		litTab.init();
		
		for(i =0; i<tokenList.size();i++) {
			//LTORG일 경우 해당 위치를 저장한다.
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
					
					//문자열일 경우
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
		
		//리터럴들의 주소를 계산한다.
		litTab.calSize(LTORG_adr,tokenList.get(i-1).location);
		
		//LTORG 선언이 안 됐을 경우 토큰을 추가한다.
		
		throw new SectorChange();
	}
	
	//리터럴 처리 이후의 주소를 계산한다.
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
	
	//외부주소 테이블을 생성하는 메소드
	public void makeExt() throws SectorChange {
		ExtTab.init();
		for(int i =0; i<tokenList.size();i++) {
			
			//ExtDef일 때랑 ExtRef일 때랑 다르게 세팅한다.
			if(tokenList.get(i).operator.equals("EXTDEF")) {
				for(int j = 0; j<tokenList.get(i).operandN;j++) {
					ExtTab.Is_ExtR.add(false);
					String temp_operand = tokenList.get(i).operand[j]; // 가독성
					ExtTab.ExtName.add(temp_operand);
					ExtTab.ExtAdr.add(symTab.search(temp_operand));
					
				}
			}
			else if(tokenList.get(i).operator.equals("EXTREF")) {
				for(int j = 0; j<tokenList.get(i).operandN;j++) {
					ExtTab.Is_ExtR.add(true);
					String temp_operand = tokenList.get(i).operand[j]; // 가독성
					ExtTab.ExtName.add(temp_operand);
					ExtTab.ExtAdr.add(-1);
				}
			}
		}
	}
	
	// 오브젝트 코드를 만드는 메소드
	public void makeObjectCode() throws SectorChange {
		for(int i =0; i<tokenList.size();i++) {
			
			//명령어가 inst table 목록에 있을 경우
			if(instTab.instMap.containsKey(tokenList.get(i).operator)) {
				Instruction temp = instTab.instMap.get(tokenList.get(i).operator);
				int opcode = temp.opcode;
				char nixbpe = tokenList.get(i).nixbpe;
				int format = temp.format;
				
				//ni부분에 따른 opcode 조정
				int ni =0;
				if(tokenList.get(i).getFlag(nFlag)==32) ni+=2;
				if(tokenList.get(i).getFlag(iFlag)==16) ni+=1;
				int xbpe = tokenList.get(i).getFlag(xFlag|bFlag|pFlag|eFlag);
				if (ni == 2) opcode += 2;
				else if (ni==1) opcode += 1;
				else if (ni==3) opcode += 3;
				else opcode += 0;
				tokenList.get(i).objectCode += String.format("%02x",opcode);
				
				//명령어의 포맷에 따라 분기점을 나눈다.
				//포맷 2일 경우 레지스터의 번호를 저장한다.
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
				
				//포맷 3일 경우 우선 operand의 유무로 갈린다.
				else if (format == 3) {
					if(tokenList.get(i).operandN!=0) {
						tokenList.get(i).objectCode += xbpe;
						//이후 immediate,indirect,literal,PC relative일 경우로 분기가 나뉜다.
						//immediate 분기
						if(tokenList.get(i).operand[0].startsWith("#")) {
							tokenList.get(i).objectCode += String.format("%03x",
									Integer.parseInt(tokenList.get(i).operand[0].split("#")[1]));
						}
						
						//indirect 분기
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
						
						//literal 분기
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
						
						//PC relative 분기
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
				
				//4형식일 경우 외부주소면 0, 아니면(immediate) 값을 그대로 받는다.
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
			
			//WORD나 BYTE일 경우를 처리한다.
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
						//만약 숫자일 경우 그대로 삽입하지만, 아닐 경우 외부주소 사용이므로 외부주소 플래그를 켠다.
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
	
	//레지스터의 번호를 찾는 메소드
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
	
	//숫자인지 판별하는 함수
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
	
	//최종 오브젝트 코드를 스트링에 저장하는 메소드
	public void buildObjectCode() throws SectorChange{
		
		//서두 출력
		FinalCode=String.format("H%s\t000000",tokenList.get(0).label);
		int temp=0;
		//리터럴 처리 여부에 따라 섹터 사이즈 결정
		if(litTab.LTORG_flag==true) {
			temp= tokenList.get(tokenList.size()-1).location+ tokenList.get(tokenList.size()-1).byteSize;
		}
		else {
			temp= tokenList.get(tokenList.size()-1).location + litTab.Table_Size+ tokenList.get(tokenList.size()-1).byteSize;
		}
		FinalCode+=String.format("%06x\r\n",temp);
		
		//외부테이블 출력
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
		
		//코드테이블 출력
		for(i=0;i<tokenList.size();) {
			//테이블계산
			int codelength=0;	//최대로 입력할 코드의 길이
			int temp_start=0;	//시작지점의 주소를 저장
			if(tokenList.get(i).byteSize!=0) temp_start = tokenList.get(i).location;
			int start_index=i;	//시작지점의 인덱스를 저장
			for(;;i++) {
				//다음 입력할 오브젝트 코드가 없다면 코드길이는 0으로.
				//만약 코드가 종료되기 직전이라면 남은 LTORG선언되지 않은 리터럴 테이블을 처리한다.
				if(i>=tokenList.size()||tokenList.get(i).byteSize==0 || codelength>27) {
					if(litTab.LTORG_flag==false && i==tokenList.size()-1) codelength += litTab.Table_Size;
					break;
				}
				codelength += tokenList.get(i).byteSize;
			}
			
			//LTORG처리된 리터럴 출력부, 인덱스를 초과시키지 않기 위해 i의 인덱스를 체크한 후 elseif로 받는다.
			if(i>=tokenList.size()) {}
			else if(tokenList.get(i).operator.equals("LTORG")) {
				FinalCode+=String.format("T%06x%02x",litTab.locationList.get(0),litTab.Table_Size);
				
				//코드 출력
				for(int j=0;j<litTab.locationList.size();j++) {
					//String 여부에 따라 분기를 나누어 출력한다.
					if(litTab.IsStringList.get(j)==true) {
						for(int k =0;k<3;k++) {
							FinalCode+=String.format("%02x",(int)litTab.StringList.get(j).charAt(k));
						}
					}
					else FinalCode+=String.format("%02x",(int)litTab.valueList.get(j));
				}
				FinalCode+="\r\n";
			}
			
			//T코드 출력, 코드길이가 0이면 다음으로
			if(codelength !=0) {
				FinalCode+=String.format("T%06x%02x",temp_start,codelength);
				
				//코드 출력
				for(int j=start_index;j!=i;j++) 
					//최대 30바이트까지 코드 출력, 다음 토큰이 오브젝트코드를 갖지 않아도 break.
					FinalCode+=String.format("%s",tokenList.get(j).objectCode);
				
				//LTORG 처리되지 않은 리터럴을 처리한다.
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
		
		//Mod 출력, 코드를 한번 더 훑는다.
		for(i=0;i<tokenList.size();i++) {
			if(tokenList.get(i).byteSize==4) {
				FinalCode+=String.format("M%06x05+%s\r\n"
						,tokenList.get(i).location+1,tokenList.get(i).operand[0]);
			}
			//token.ExtFlag가 있을 경우의 mod연산
			else if (tokenList.get(i).ExtFlag==true) {
				String tempString = "";		//문자 하나씩 받아 만든 레이블을 출력
				boolean isminus = false;	//앞에있던 게 마이너스인지 여부
				int operatorIndex=0;		//부호가 있던 자리의 인덱스
				
				//문자열의 길이만큼 인덱스가 올라가면 종료.
				for(int j = 0;j<tokenList.get(i).operand[0].length();) {
						
					//isminus를 통해 오퍼레이터가 마이너스인지 아닌지 판별
					if(operatorIndex==0);
					else if(tokenList.get(i).operand[0].charAt(operatorIndex)=='-') isminus = true;
					else isminus = false;
				
					//+나 -를 만날 때 까지 글자를 하나씩 읽는다.
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
		//E 출력
		FinalCode+="E\r\n";
		//이후 섹터체인지로 다음섹터로
		throw new SectorChange();
	}
	
}

//토큰 클래스
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe=0;
	int operandN=0; // 사용되는 오퍼랜드의 갯수
	

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode="";
	int byteSize;
	boolean ExtFlag = false; // WORD에서 외부주소가 사용될 경우
	
	//일반적인 토큰 생성자
	public Token(String line,InstTable instTab){
		location = TokenTable.locctr;
		parsing(line);
		if(operator.equals("CSECT")) {
			TokenTable.locctr = 0;
			location=0;
		}
		
		//명령어가 instTable에 있다면 byteSize를, 아니면 process_locctr을 수행.
		if(instTab.instMap.get(operator) == null) {process_locctr();}
		else {
			byteSize = instTab.instMap.get(operator).format;
			TokenTable.locctr += byteSize;
		}
		
		setAddressing();
		setRelative();
		
	}
	
	//프로그램 한 라인을 토큰으로 만든다.
	public void parsing(String line) {
		if(line.startsWith(".")) return; // 주석이면 그냥 안 받는다.
		Scanner temp_line = new Scanner(line).useDelimiter("\t");
		
		//탭으로 시작한다면 (레이블이 비어있다면) 바로 오퍼레이터에 값을 받는다.
		if(!(line.startsWith("\t"))) {label = temp_line.next();}
		else label ="";
		
		operator = temp_line.next();
		operand = new String[3];
		try{ 
			String temp_operand = temp_line.next();
			
			// 오퍼랜드의 경우 한꺼번에 저장한 다음 오퍼랜드 구분을 지어준다.
			parse_operand(temp_operand);
		}
		catch(NoSuchElementException e) {operand[0] = "";}
		
		try {comment = temp_line.next();}
		catch(NoSuchElementException e) {comment = "";}
	}
	
	//오퍼랜드 구분짓는 함수
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
	
	//operator가 insttable에 없을 경우 locctr을 처리하는 메소드
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
	
	//flag 정보를 다루는 메소드
	public void setFlag(int flag, int value) {
		 if (value==1) {nixbpe += flag;}
	}
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	
	
	//n,i의 값 세팅을 담당한다.
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
	
	//x,b,p,e의 값 세팅을 담당한다.
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
