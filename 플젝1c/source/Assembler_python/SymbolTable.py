#SymbolTable.py
class SymbolTable:

    def __init__(self):
        self.symbolList=[]
        self.locationList=[]

    def putSymbol(self,symbol,location):
        self.symbolList.append(symbol)
        self.locationList.append(location)

    def search(self,symbol):
        try:
            return self.locationList[self.symbolList.index(symbol)]
        except:
            return 0
