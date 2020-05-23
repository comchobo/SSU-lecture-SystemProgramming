#InstTable.py
class InstTable:
    def __init__(self,instFile):
        self.instMap={}
        self.openFile(instFile)

    def openFile(self,fileName):
        try:
            f = open(fileName)
            while True:
                line = f.readline()
                if not line :
                    break
                temp = Instruction(line)
                self.instMap[temp.name] = temp
        except:
            print('파일로드 실패')
        finally:
            f.close()

class Instruction:
    def __init__(self,line):
        self.parsing(line)

    def parsing(self,line):
        (self.name,self.opcode,temp_format,self.operandN) = line.strip().split('\t')
        self.format = int(temp_format)