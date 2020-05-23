#pragma once

#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

char *input_file;
char *inst_file;
char *output_file;

struct inst_unit {
	char *name;
	int opcode;
	int format;
	int operandN;
};
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
static int inst_index; //명령어 테이블 관련 변수선언

char *input_data[MAX_LINES];
static int line_num = 0;
int label_num; //인풋데이터 선언

struct token_unit {
	char *label;
	char *operator;
	char *operand[MAX_OPERAND];
	int Noperand;
	char *comment;
	int adr;
	char nixbpe;
	int operator_length;			//명령어의 길이, 토큰의 주소, nixbpe 정보 등을 담음
};
typedef struct token_unit token;
static token *token_table[MAX_LINES];
static int token_line; //토큰데이터 선언

struct symbol_unit {
	char symbol[20];
	int addr;
};
typedef struct symbol_unit symbol;
symbol *sym_table[MAX_LINES]; //심볼테이블 선언
static int sym_line;

struct machine_unit {
	int opcode;
	int xbpe;
	int adr;			//disp이다.
	int format;
	int TF_extAdr;
	int csect;			//csect operator일 경우 1 저장
	int realadr;		//머신코드가 저장될 주소를 가리킨다.
};
typedef struct machine_unit mUnit;
mUnit *code_table[MAX_LINES]; //머신코드 테이블 선언
static int code_line;

struct LTORG_unit {
	char name[15];
	char val[20];
	int Xval;
	int length;
	int adr;
};
typedef struct LTORG_unit LTORG;
LTORG *literal_table[500];
static int literal_line;
//LTORG 테이블 선언

struct EXT_Adr {
	int CSECTnum;
	char AdrLists[10][15];
	int TF_ExtR;
};
typedef struct EXT_Adr ExtAdr;
ExtAdr *ExtAdr_table[750];
static int ExtAdr_line;
//외부주소 테이블 선언

int SectAdr[100];
// 각 섹터의 길이를 저장한다.

char start_labels[100][20];
int k = 0;		//인덱싱용 전역변수 선언

int locctr=0;

int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);

int assem_pass1(void);
int token_parsing(char *str, token *tk, inst **it);

int search_format(char *str, inst **it);
int search_opcode(char *str, inst **it);
int search_reg(char *reg);
int search_ascii(char input);

char* search_adr_operand(char *name, int code_adr, int CSECT);
//코드의 오퍼랜드를 갖고온다.

int search_symbolAdr(char *name, symbol **table, int start);
int process_locctr(int format, char *opr, char *oprn);
void make_symtab_output(char *file_name);

char setAddressing(char nixbpe, char *operand, int format);
char setRelative(char nixbpe, char *operand, char *operand2, char *operator,int format);
int symbol_cal(char *operand); //심볼연산을 수행한다.

void copystring(char *A, char* toA);		//주소가 아닌 문자열 값을 복사하는 함수
void clearstring(char *A, int maxindex);	//포인터에 저장된 값을 비우는 함수
void freeall();								//여태까지 동적할당된 변수들을 지우는 함수
int stringtoint(char *a);					//문자열을 숫자로 바꿔주는 함수

static int assem_pass2(void);
void make_objectcode_output(char *file_name);
