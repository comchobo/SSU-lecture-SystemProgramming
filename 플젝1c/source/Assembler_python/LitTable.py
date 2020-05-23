#LitTable.py
import SectorChange

class LitTable:

    def __init__(self):
        self.nameList=[]
        self.locationList=[]
        self.valueList=[]
        self.StringList=[]
        self.IsStringList=[]
        self.SizeList=[]
        self.Table_Size=0
        self.LTORG_flag=False

    def searchLit(self,name):
        temp = self.nameList.index(name)
        if temp == 0:
            return 0
        else :
            return self.locationList.index(temp)

    #LTORG 여부에 따라 리터럴 테이블의 크기와 리터럴의 주소를 계산한다.
    def calSize(self,LTORG_adr,base_location):
        if self.Table_Size==0 :
            raise SectorChange.SectorChange

        if self.LTORG_flag == True:
            base = LTORG_adr
            self.locationList.append(base)
            i=0
            while i<len(self.nameList):
                if self.IsStringList[i] == True:
                    base +=3
                else :
                    base +=1
                self.locationList.append(base)
                i+=1
        else :
            base = base_location
            self.locationList.append(base)
            i=0
            while i<len(self.nameList):
                if self.IsStringList[i] == True:
                    base +=3
                else :
                    base +=1
                self.locationList.append(base)
                i+=1