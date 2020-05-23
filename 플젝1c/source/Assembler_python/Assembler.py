#Assembler.py
import re,SectorChange,LitTable,ExtTable,SymbolTable,InstTable,TokenTable

class Assembler:
    def __init__(self):
        self.lineList=[ ]
        self.symtabList =[ ]
        self.TokenList =[ ]
        self.litList = [ ]
        self.codeList = [ ]
        self.ExtList =[ ]

        #명령어 테이블을 생성한다.
    def loadInstFile(self,inputFile):
        self.instTable = InstTable.InstTable(inputFile)

    def loadInputFile(self,inputFile):
        #라인을 입력받아 라인리스트에 저장
        try: 
            f = open(inputFile)
            while True:
              line = f.readline()
              if not line : break
              self.lineList.append(line)
        except:
            print('파일로드 실패')
        finally:
            f.close()
    
    def pass1(self):

        #라인에 따라 토큰테이블을 생성
        temp = TokenTable.TokenTable()
        for i in self.lineList:
            try:
                temp.linkInst(self.instTable)
                temp.putToken(i)
            except SectorChange.SectorChange: 
                tempToken = temp.popToken()
                self.TokenList.append(temp)
                temp = TokenTable.TokenTable() #수정 (push & pop)
                temp.pushToken(tempToken)
                continue
        self.TokenList.append(temp)

        #리터럴 테이블을 생성하고 리터럴 테이블의 주소를 계산
        for i in self.TokenList:
            try:
                temp = LitTable.LitTable()
                i.linkLit(temp)
                i.makeLit()
            except SectorChange.SectorChange:
                self.litList.append(temp)
                continue

        for i in self.TokenList:
            i.calLit()

        #심볼테이블을 생성하는 과정
        for i in self.TokenList:
            try:
                temp = SymbolTable.SymbolTable()
                i.linkSym(temp)
                i.makeSymtab()
            except SectorChange.SectorChange:
                self.symtabList.append(temp)
                continue

    #심볼테이블을 출력하는 과정
    def PrintSymbolTable(self,output):
        j=0
        f = open(output,'w')
        for i in self.symtabList:
            while j<len(i.symbolList):
                f.write(i.symbolList[j])
                f.write("\t")
                f.write(str(hex(i.locationList[j]))[2:])
                f.write("\n")
                j+=1
            f.write("\n")
            j=0
        f.close()

    def pass2(self):

        #외부주소 테이블을 만드는 과정
        for i in self.TokenList:
            try:
                temp = ExtTable.ExtTable()
                i.linkExt(temp)
                i.makeExt()
            except SectorChange.SectorChange:
                self.ExtList.append(temp)
                continue

        #기계어 코드를 생성하는 과정
        for i in self.TokenList:
            try:
                i.makeObjectCode()
            except SectorChange.SectorChange:
                continue

        #출력물을 생성하는 과정
        for i in self.TokenList:
            try:
                i.buildObjectCode()
            except SectorChange.SectorChange:
                continue

    def PrintObjectCode(self,output):
        j=0
        f = open(output,'w')
        for i in self.TokenList:
            f.write(i.FinalCode)
        f.close()