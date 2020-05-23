#TokenTable.py
import ExtTable,SymbolTable,InstTable,SectorChange,LitTable

class TokenTable:

    def __init__(self):
        self.tokenList=[ ]
        self.FinalCode=""
    def linkInst(self,a):
        self.instTab = a
    def linkLit(self,a):
        self.litTab = []
        self.litTab = a
    def linkSym(self,a):
        self.symTab = a
    def linkExt(self,a):
        self.extTab = a

    #토큰을 삽입하는 메소드
    def putToken(self,line):
        if line[0] == '.':
            return
        self.tokenList.append(Token(line,self.instTab.instMap))
        if self.tokenList[len(self.tokenList)-1].operator == "CSECT":
            raise SectorChange.SectorChange

    #리스트 형식으로 돼 있어 최근의 푸쉬/팝 토큰을 수행
    def pushToken(self,a):
        self.tokenList.append(a)
    def popToken(self):
        return self.tokenList.pop()

    #리터럴 테이블을 생성하는 과정, X와 C일 경우로 나눈다.
    def makeLit(self):
        LTORG_adr = 0
        for i in self.tokenList:
            if i.operator == "LTORG":
                LTORG_adr = i.location
                self.litTab.LTORG_flag = True
            elif i.operandN== 0 :
                continue
            elif i.operand[0][0] == "=":
                if len(self.litTab.nameList) == 0 or i.operand[0] not in self.litTab.nameList:
                    if i.operand[0][1]=="X":
                        self.litTab.nameList.append(i.operand[0])
                        temp = i.operand[0].strip().split("'")
                        self.litTab.valueList.append(temp[1])
                        self.litTab.StringList.append("")
                        self.litTab.IsStringList.append(False)
                        self.litTab.Table_Size +=1
                        self.litTab.SizeList.append(1)
                    elif i.operand[0][1]=="C":
                        self.litTab.nameList.append(i.operand[0])
                        temp = i.operand[0].strip().split("'")
                        self.litTab.valueList.append(-1)
                        self.litTab.StringList.append(temp[1])
                        self.litTab.IsStringList.append(True)
                        self.litTab.Table_Size +=3
                        self.litTab.SizeList.append(3)
                else:
                    continue
            else:
                continue
        self.litTab.calSize(LTORG_adr,self.tokenList[len(self.tokenList)-1].location)
        raise SectorChange.SectorChange

    #LTORG일 경우 명령어들의 주소를 보정한다.
    def calLit(self):
        temp = 0
        flag = False
        if self.litTab.LTORG_flag == True:
           for i in self.tokenList:
               if flag == True:
                   i.location +=temp
               if i.operator == "LTORG":
                   flag = True
                   temp = self.litTab.Table_Size

    #심볼테이블을 생성한다.
    def makeSymtab(self):
        for i in self.tokenList:
            if i.label == "":
                continue
            else:
                if len(self.symTab.symbolList) == 0 or i.label not in self.symTab.symbolList:
                    self.symTab.putSymbol(i.label,i.location)
        raise SectorChange.SectorChange

    #Extdef,Extref에 따라 외부주소 테이블을 생성한다.
    def makeExt(self):
        for i in self.tokenList:
            if i.operator == "EXTDEF":
                for j in i.operand:
                   self.extTab.ExtName.append(j)
                   self.extTab.Is_ExtR.append(False)
                   self.extTab.ExtAdr.append(self.symTab.search(j))
            elif i.operator == "EXTREF":
                for j in i.operand:
                   self.extTab.ExtName.append(j)
                   self.extTab.Is_ExtR.append(True)
                   self.extTab.ExtAdr.append(-1)
        raise SectorChange.SectorChange

    #각 토큰을 기계어 코드로 변환한다.
    def makeObjectCode(self):
        for i in self.tokenList:
            if i.operator in self.instTab.instMap:
                opcode = int(self.instTab.instMap[i.operator].opcode,16)
                nixbpe = i.nixbpe
                format = int(self.instTab.instMap[i.operator].format)
                
                #nixbpe 계산
                ni=0
                if nixbpe & 0b100000 == 0b100000:
                    ni += 2
                if nixbpe & 0b010000 == 0b010000:
                    ni+=1
                opcode+=ni
                xbpe = nixbpe & 0b001111
                i.objectCode+= "%02x" % opcode

                #각 포맷일 때를 분기로 삼아 코드 삽입
                if format == 2:
                    if i.operandN == 1:
                        i.objectCode+= self.search_reg(i.operand[0])
                        i.objectCode+="0"
                    else:
                        i.objectCode+= self.search_reg(i.operand[0])
                        i.objectCode+= self.search_reg(i.operand[1])
                elif format == 3:
                    if i.operandN != 0:
                        i.objectCode += str(xbpe)
                        if i.operand[0][0] == '#':
                            temp_operand = i.operand[0].split('#')
                            i.objectCode+= "%03x" % int(temp_operand[1])

                        elif i.operand[0][0] == '@':
                            temp_operand = i.operand[0].split('@')
                            TA = self.symTab.search(temp_operand[1])
                            PC = i.location + i.byteSize
                            disp = TA-PC
                            if disp <0:
                                disp = disp & 0xFFF
                            i.objectCode+= "%03x" % disp

                        elif i.operand[0][0] == '=':
                            TA = self.litTab.locationList[self.litTab.nameList.index(i.operand[0])]
                            PC = i.location + i.byteSize
                            disp = TA-PC
                            if disp< 0 :
                                disp = disp & 0xFFF
                            i.objectCode+= "%03x" % disp

                        else:
                            TA = self.symTab.search(i.operand[0])
                            PC = i.location + i.byteSize
                            disp = TA-PC
                            if disp <0:
                                disp = disp & 0xFFF
                            i.objectCode+= "%03x" % disp
                    else:
                        i.objectCode += str(xbpe)
                        i.objectCode += "000"

                elif format == 4:
                    i.objectCode += str(xbpe)
                    if i.operand[0][0] == '#':
                        i.objectCode += int(i.operand[0].split('#'))
                    else:
                        i.objectCode += "00000"

            #word나 byte일 때도 코드 삽입
            else:
                if i.operator == "WORD":
                    i.byteSize = 3
                    if "'" in i.operand[0]:
                        temp_operand = i.operand[0].split("'")[1]
                        i.objectCode += "%02x" % temp_operand[0]
                        i.objectCode += "%02x" % temp_operand[1]
                        i.objectCode += "%02x" % temp_operand[2]
                    else:
                        if i.operand[0].isdigit():
                            self.objectCode += "%06x" % i.operand[0]
                        else:
                            i.ExtFlag = True
                            i.objectCode += "000000"
                elif i.operator == "BYTE":
                    i.byteSize=1
                    temp_operand=i.operand[0].split("'")[1]
                    i.objectCode += "%02x" % int(temp_operand,16)
        raise SectorChange.SectorChange

    #레지스터 번호를 찾는 함수
    def search_reg(self,input):
        if input == "A" :
            return "0"
        elif input == "X" :
            return "1"
        elif  input == "L" :
            return "2"
        elif input == "B" :
            return "3"
        elif input == "S" :
            return "4"
        elif input == "T" :
            return "5"
        elif input == "F" :
            return "6"
        elif input == "PC" :
            return "8"
        elif input == "SW" :
            return "9"
        return "-1"

    def buildObjectCode(self):

        #초기 레이블 출력
        self.FinalCode += "%06s\t000000" % self.tokenList[0].label
        temp = 0
        TOKEN = self.tokenList[len(self.tokenList)-1] # 가독성을 위한 초기화
        if self.litTab.LTORG_flag == True:
            temp = TOKEN.location + TOKEN.byteSize
        else :
            temp = TOKEN.location + TOKEN.byteSize + self.litTab.Table_Size
        self.FinalCode += "%06x\n" % temp

        #외부테이블 출력
        i=0
        if self.extTab.Is_ExtR[i] == False:
            self.FinalCode += "D"
            while self.extTab.Is_ExtR[i]==False:
                self.FinalCode+= "%s%06x" % (self.extTab.ExtName[i],self.extTab.ExtAdr[i])
                i+=1
            self.FinalCode +="\n"
        self.FinalCode += "R"
        while i<len(self.extTab.Is_ExtR):
                self.FinalCode+= "%s" % self.extTab.ExtName[i]
                i+=1
        self.FinalCode += "\n"

        #코드테이블 출력
        i=0
        while i<len(self.tokenList):
            codelength=0
            temp_start = 0
            if self.tokenList[i].byteSize!=0:
                temp_start = self.tokenList[i].location
            start_index = i

            #한번에 나올 코드길이 계산 (codelength)
            while True:
                if i>=len(self.tokenList) or self.tokenList[i].byteSize==0 or codelength>27:
                    if self.litTab.LTORG_flag==False and i==len(self.tokenList)-1:
                        codelength += self.litTab.Table_Size
                    break
                codelength += self.tokenList[i].byteSize
                i+=1
                
            if i>=len(self.tokenList):
                k=0
            elif self.tokenList[i].operator == "LTORG":
                self.FinalCode+= "T%06x%02x" % (self.litTab.locationList[0], self.litTab.Table_Size)
                j=0
                while j<len(self.litTab.SizeList):
                    if self.litTab.IsStringList[j] == True:
                        k=0
                        while k<3:
                            self.FinalCode+= "%02x" % ord(str(self.litTab.StringList[j][k]))
                            k+=1
                        j+=1
                    else:
                        self.FinalCode += "%02x" % int(self.litTab.valueList[j])
                        j+=1
                self.FinalCode+="\n"

            #T코드 출력
            if codelength!=0:
                self.FinalCode+= "T%06x%02x" % (temp_start, codelength)
                k=start_index
                while k!=i:
                    self.FinalCode+= "%s" % self.tokenList[k].objectCode
                    k+=1
                if self.litTab.LTORG_flag == False and i==len(self.tokenList)-1:
                    j=0
                    while j<len(self.litTab.SizeList):
                        if self.litTab.IsStringList[j] == True:
                            k=0
                            while k<3:
                                self.FinalCode+= "%02x" % ord(str(self.litTab.StringList[j][k]))
                                k+=1
                            j+=1
                        else:
                            self.FinalCode += "%02x" % int(self.litTab.valueList[j])
                            j+=1
                    i+=1

                self.FinalCode+="\n"
            else:
                i+=1

        #Mod 출력, 코드를 한번 더 훑는다.
        for l in self.tokenList:
            if l.byteSize == 4:
                self.FinalCode += "M%06x05+%s\n" % (l.location+1,l.operand[0])
           #extflag가 true일 경우 mod연산
            elif l.ExtFlag == True:
                tempString = ""
                isminus = False
                operatorIndex = 0

                m=0
                while m<len(l.operand[0]):
                    if operatorIndex==0:
                        z=0
                    elif l.operand[0][operatorIndex] == '-':
                        isminus=True
                    else:
                        isminus = False

                    while l.operand[0][m]!='+' and l.operand[0][m] != '-':
                        tempString+= l.operand[0][m]
                        if m==len(l.operand[0])-1:
                            break
                        m+=1
                    if isminus==False:
                        self.FinalCode += "M%06x06+%s\n"% (l.location,tempString)
                    else:
                        self.FinalCode += "M%06x06-%s\n"% (l.location,tempString)
                    operatorIndex=m
                    tempString=""
                    m+=1
                self.FinalCode+= "\n"
        self.FinalCode+="\n"
        raise SectorChange.SectorChange

class Token:
    locctr = 0

    #토큰생성자. 크게 파싱, 포맷길이 계산, locctr 계산과 nixbpe 계산으로 나눈다.
    def __init__(self,line,instMap):
        self.location = Token.locctr
        self.parsing(line)
        self.ExtFlag = False
        self.objectCode=""

        if self.operator == "CSECT":
            Token.locctr = 0
            self.location = 0

        if self.operator in instMap:
            self.byteSize = instMap[self.operator].format
            Token.locctr += self.byteSize
        else:
            self.process_locctr()

        self.setAdr()
        self.setRel()

    #라인을 토큰에 맞게 파싱
    def parsing(self,line):
        if line[0] == '\t':
            try:
                self.label =""
                self.operator = line.strip().split('\t')[0]
                temp_operand = line.strip().split('\t')[1]
                self.operand = temp_operand.split(',')
                self.operandN = len(self.operand)
                if temp_operand == "":
                    self.operandN=0
            except:
                self.operandN = 0
                return
        else:
            try:
                self.label = line.strip().split('\t')[0]
                self.operator = line.strip().split('\t')[1]
                temp_operand = line.strip().split('\t')[2]
                self.operand = temp_operand.split(',')
                self.operandN = len(self.operand)
                if temp_operand == "":
                    self.operandN=0
            except:
                self.operandN=0
                return

    #명령어가 명령어 테이블에 없을 경우 locctr을 보정
    def process_locctr(self):
        if self.operator == "WORD":
            Token.locctr += 3
            self.byteSize = 3
        elif self.operator == "BYTE":
            Token.locctr += 1
            self.byteSize = 1
        elif self.operator == "RESB":
            self.byteSize=0
            Token.locctr += 1 * int(self.operand[0])
        elif self.operator == "RESW":
            self.byteSize=0
            Token.locctr += 3 * int(self.operand[0])
        else :
            self.byteSize = 0

    #ni파트를 계산
    def setAdr(self):
        self.nixbpe= 0b000000
        if self.byteSize >2 :
            if self.operand[0]:
                if self.operand[0][0] == '#':
                    self.nixbpe += 0b010000
                elif self.operand[0][0] == '@':
                    self.nixbpe += 0b100000
                else:
                    self.nixbpe += 0b110000
            else :
                self.nixbpe += 0b110000

    #xbpe 파트를 계산
    def setRel(self):
        if self.byteSize == 3:
            if self.operand[0] :
                if self.operand[0][0] != '#':
                    self.nixbpe += 0b000010
        elif self.byteSize == 4:
            if self.operator[0] == '+':
                self.nixbpe += 0b000001

        if self.operandN >1:
            if self.operand[1][0] == 'X':
                self.nixbpe += 0b001000

