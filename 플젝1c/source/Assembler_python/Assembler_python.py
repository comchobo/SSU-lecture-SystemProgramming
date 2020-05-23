#main
import Assembler

main = Assembler.Assembler()
main.loadInstFile("inst.data")
main.loadInputFile("input.txt")
main.pass1()
main.PrintSymbolTable("symtab_20142249.txt")
main.pass2()
main.PrintObjectCode("output_20142249.txt")

