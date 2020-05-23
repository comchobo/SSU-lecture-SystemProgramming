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
static int inst_index; //��ɾ� ���̺� ���� ��������

char *input_data[MAX_LINES];
static int line_num = 0;
int label_num; //��ǲ������ ����

struct token_unit {
	char *label;
	char *operator;
	char *operand[MAX_OPERAND];
	int Noperand;
	char *comment;
	int adr;
	char nixbpe;
	int operator_length;			//��ɾ��� ����, ��ū�� �ּ�, nixbpe ���� ���� ����
};
typedef struct token_unit token;
static token *token_table[MAX_LINES];
static int token_line; //��ū������ ����

struct symbol_unit {
	char symbol[20];
	int addr;
};
typedef struct symbol_unit symbol;
symbol *sym_table[MAX_LINES]; //�ɺ����̺� ����
static int sym_line;

struct machine_unit {
	int opcode;
	int xbpe;
	int adr;			//disp�̴�.
	int format;
	int TF_extAdr;
	int csect;			//csect operator�� ��� 1 ����
	int realadr;		//�ӽ��ڵ尡 ����� �ּҸ� ����Ų��.
};
typedef struct machine_unit mUnit;
mUnit *code_table[MAX_LINES]; //�ӽ��ڵ� ���̺� ����
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
//LTORG ���̺� ����

struct EXT_Adr {
	int CSECTnum;
	char AdrLists[10][15];
	int TF_ExtR;
};
typedef struct EXT_Adr ExtAdr;
ExtAdr *ExtAdr_table[750];
static int ExtAdr_line;
//�ܺ��ּ� ���̺� ����

int SectAdr[100];
// �� ������ ���̸� �����Ѵ�.

char start_labels[100][20];
int k = 0;		//�ε��̿� �������� ����

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
//�ڵ��� ���۷��带 ����´�.

int search_symbolAdr(char *name, symbol **table, int start);
int process_locctr(int format, char *opr, char *oprn);
void make_symtab_output(char *file_name);

char setAddressing(char nixbpe, char *operand, int format);
char setRelative(char nixbpe, char *operand, char *operand2, char *operator,int format);
int symbol_cal(char *operand); //�ɺ������� �����Ѵ�.

void copystring(char *A, char* toA);		//�ּҰ� �ƴ� ���ڿ� ���� �����ϴ� �Լ�
void clearstring(char *A, int maxindex);	//�����Ϳ� ����� ���� ���� �Լ�
void freeall();								//���±��� �����Ҵ�� �������� ����� �Լ�
int stringtoint(char *a);					//���ڿ��� ���ڷ� �ٲ��ִ� �Լ�

static int assem_pass2(void);
void make_objectcode_output(char *file_name);
