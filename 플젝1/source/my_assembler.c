#include "my_assembler_20142249.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include<math.h>
#include <fcntl.h>

int main(int args, char *argv[])
{
	inst_file = (char *)malloc(sizeof(char) * 100);
	input_file = (char *)malloc(sizeof(char) * 100);
	output_file = (char *)malloc(sizeof(char) * 100);
	printf("��ɾ� ��� ���ϸ��� �Է����ּ���\n");
	scanf("%s", inst_file);
	printf("�ҽ��ڵ��� ���ϸ��� �Է����ּ���\n");
	scanf("%s", input_file);

	if (init_my_assembler()< 0)
	{
		printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n");
		return -1;
	}
	if (assem_pass1() < 0) {
		printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}
	
	printf("����� �ɺ����̺��� ���ϸ��� �Է����ּ���\n");
	scanf("%s", output_file);
	make_symtab_output(output_file);

	if(assem_pass2() < 0 ){
	printf(" assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n") ;
	return -1 ;
	}

	char machine_code[100];
	printf("����� �ӽ��ڵ��� ���ϸ��� �Է����ּ���\n");
	scanf("%s", machine_code);
	make_objectcode_output(machine_code);

	free(inst_file);
	free(input_file);
	free(output_file);
	freeall();

	return 0;
}

int init_my_assembler(void)
{
	int result = 0;

	if ((result = init_inst_file(inst_file)) < 0) return -1;
	if ((result = init_input_file(input_file)) < 0) return -1;
	return result;
}

int init_inst_file(char *input)
{
	FILE* file;
	int err = 0;
	int i, j;
	char temp = ' ';
	file = fopen(input, "r");

	if (file == NULL) {
		printf("%s�� �����ϴ�.\n", input);
		err = -1;
		return err;
	}

	for (i = 0;; i++) {
		inst_index++;
		inst_table[i] = (inst *)malloc(sizeof(inst));
		inst_table[i]->name = (char *)malloc(sizeof(char) * 10);
		for (j = 0;; j++) {
			temp = fgetc(file);
			if (temp != '\t') inst_table[i]->name[j] = temp;
			else {
				inst_table[i]->name[j] = '\0';
				break;
			}
		}
		fscanf(file, "%x", &inst_table[i]->opcode);
		fscanf(file, "\t%d\t%d\n", &inst_table[i]->format, &inst_table[i]->operandN);
		if (feof(file)) break;
	}

	return err;
}

int init_input_file(char *input)
{
	FILE * file;
	int err = 0;
	int i;
	file = fopen(input, "r");

	if (file == NULL) {
		printf("%s�� �����ϴ�.\n", input);
		errno = -1;
		return err;
	}
	for (i = 0;; i++) {
		line_num++;
		input_data[i] = (char *)malloc(sizeof(char) * 256);
		fgets(input_data[i], 256, file);
		if (feof(file)) break;
	}

	fclose(input);
	return err;
}

int token_parsing(char *str, token *tk, inst **it){
	int i;							//str�� index
	int j = 0;						//��ū�� �� ��������� index
	int handle = 0;					//�� ���ڿ��� �д� for���� �����ϴ� ����
	int error = -1;					//error�� �� ���
	int copy = 0;
	char temp[50];					//�� ���ڿ��� �����ϱ� ���� ����

	//label ������ ���� for��. ���� cooment�� ��� �ǳʶڴ�.
	for (i = 0; handle < 1; i++) {
		if (str[i] == '\t') {
			temp[i] = '\0';
			handle++;
		}
		else if (str[i] == '.') {
			tk->operator = NULL;
			tk->label = NULL;
			tk->comment = NULL;
			return 0;
		}
		else {
			temp[j] = str[i];
			j++;
		}
	}
	if (str[0] != '\t') {
		tk->label = (char *)calloc(20, sizeof(char));
		copystring(temp, tk->label);
	}
	else tk->label = NULL;
	clearstring(temp, 50);
	j = 0;

	//operator ������ ���� for��.
	for (handle = 0; handle < 1;) {	
		if ((str[i] == '\n')) {
			handle++;
			temp[i] = '\0';
		}
		else if ((str[i] == '\t')) {
			handle++;
			temp[i] = '\0';
			i++;
		}
		else {
			temp[j] = str[i];
			j++;
			i++;
		}
	}
	tk->operator = (char *)calloc(20, sizeof(char));
	copystring(temp, tk->operator);
	if (strcmp("CSECT", tk->operator) == 0 || strcmp("START", tk->operator) == 0) {//�� ���Ͱ� ���۵� ��� ���̺��� ����
		copystring(tk->label, start_labels[k]);
		k++;
	}
	clearstring(temp, 50);
	j = 0;

	//operand ������ ���� for��
	int n = 0;
	for (handle = 0; handle < 1;) {			
		if ((str[i] == '\t') || (str[i] == '\n')) handle++;
		else if (str[i] == ',') {								
			//,�� ���� = operand�� ������ �� �ִ� ��� �����Ͽ�
			tk->operand[n - 1] = (char *)calloc(20, sizeof(char));	
			//,�� ���翩�ο� ���� �б⸦ �����Ͽ���.
			copystring(temp, tk->operand[n - 1]);
			j = 0;
			i++;
			clearstring(temp, 50);
		}
		else {
			for (; ((str[i] == '\t') || (str[i] == '\n') || (str[i] == ',')) == 0;) {
				//�ϳ��� operand�� ������ ����
				temp[j] = str[i];
				j++;
				i++;
			}
			n++;
		}
	}
	if (n != 0) {
		tk->operand[n - 1] = (char *)calloc(20, sizeof(char));
		copystring(temp, tk->operand[n - 1]);
	}
	else tk->operand[0] = NULL;
	tk->Noperand = n;
	j = 0;

	//comment�� �����ϴ� for��
	for (handle = 0; handle < 1;) {
		if (str[i] == '\n') {
			temp[i] = '\0';
			handle++;
		}
		else {
			temp[j] = str[i];
			j++;
			i++;	//point
			copy++;
		}
	}
	tk->comment = (char *)calloc(50, sizeof(char));
	if (copy != 0) {
		copystring(temp, tk->comment);
		clearstring(temp, 50);
		copy = 0;
	}

	//��ɾ��� ������ �����ϸ� �� ������ �ּҸ� ����ϴ� ��Ʈ
	if (strcmp(tk->operator, "CSECT") == 0) locctr = 0;
	int format = search_format(tk->operator,it);
	tk->adr = locctr;
	tk->operator_length = process_locctr(format, tk->operator, tk->operand[0]);

	tk->nixbpe = 0;			//nixbpeó��, �ʱ�ȭ
	if (tk->operator_length > 2) {
		tk->nixbpe = setAddressing(tk->nixbpe, tk->operand[0], format);
		if (tk->Noperand == 2)
			tk->nixbpe = setRelative(tk->nixbpe, tk->operand[0], tk->operand[1], tk->operator,format);
		else
			tk->nixbpe = setRelative(tk->nixbpe, tk->operand[0], NULL, tk->operator,format);
	}
	if (strcmp(tk->operator,"END") == 0) return error;		//������ ������ ���� ������ ���� ���ϰ� -1�� ����Ѵ�.
	return 0;
}

int assem_pass1(void)
{
	int i, j = 0, k = 0;
	char temp2[10];
	for (i = 0; k != -1; i++) {							// k=-1�Ͻ� ������ ���� for�� Ż��
		token_line++;
		token_table[i] = (token*)malloc(sizeof(token));	// *token_table[5000]�� �迭 ���Ҹ� �ϳ��� �����Ͽ� token���� ������ش�.
		k = token_parsing(input_data[i], token_table[i], inst_table);
		//input_data �� �ٰ� token_table �� ���� ��ġ���� �Լ� ����.
	}

	//���ͷ� ���̺� ����
	for (i = 0; i < token_line; i++) {
		if (token_table[i]->operator == NULL) continue;

		if (strcmp(token_table[i]->operator,"CSECT") == 0) {
			literal_table[literal_line] = (LTORG*)malloc(sizeof(LTORG));
			literal_line++;
			copystring("CSECT", literal_table[literal_line - 1]->name);
			continue;
		}

		if (token_table[i]->Noperand == 1) {
			if (token_table[i]->operand[0][0] == '=') {
				if (literal_line!=0){
					if (strcmp(literal_table[literal_line-1]->name, token_table[i]->operand[0]) == 0) continue;
				}
				// �̹� ���̺� ������ ���ͷ��� ��� ��ŵ

				literal_table[literal_line] = (LTORG*)malloc(sizeof(LTORG));
				literal_line++;
				if (token_table[i]->operand[0][1] == 'X') {
					literal_table[literal_line - 1]->length = 0;
					copystring(token_table[i]->operand[0], literal_table[literal_line - 1]->name);
					int temp = 16 * (token_table[i]->operand[0][3] - 48) + (token_table[i]->operand[0][4] - 48);
					//char���� int�� ��ȯ�ϱ� ����
					literal_table[literal_line - 1]->Xval = temp;
					copystring("NULL", literal_table[literal_line - 1]->val);
					literal_table[literal_line - 1]->length++;
				}
				//16������ ����� ���ͷ� ����, val = "NULL"�� �ʱ�ȭ

				else if (token_table[i]->operand[0][1] == 'C') {
					literal_table[literal_line - 1]->length = 0;
					copystring(token_table[i]->operand[0],literal_table[literal_line - 1]->name);
					for (j = 0; token_table[i]->operand[0][j + 3] != 39; j++) {
						temp2[j] = token_table[i]->operand[0][j + 3];
						literal_table[literal_line - 1]->length++;
					}
					temp2[j] = '\0';
					copystring(temp2,literal_table[literal_line - 1]->val);
					literal_table[literal_line - 1]->Xval = -1;
				}
				//���ڿ��� ����� ���ͷ� ����,xval = -1�� �ʱ�ȭ
			}
		}
	}
	//���ͷ��� ���� �ּ� ����
	int temp = 0;
	int CSECT = 0;
	k = 0;
	for (i = 0; i < token_line; i++) {
		if (token_table[i]->operator == NULL) continue;
		if (strcmp(token_table[i]->operator,"LTORG") == 0) {

			for (j = 0; ; j++) {
				if (strcmp(literal_table[j]->name, "CSECT") == 0) k++;
				if (k > CSECT) break;
				else if (k == CSECT) {
					literal_table[j]->adr = token_table[i]->adr + temp;
					temp += literal_table[j]->length;
				}
				else continue;
			}
			for (; strcmp(token_table[i]->operator,"CSECT") !=0; i++) {
				token_table[i+1]->adr += temp;
			}
			temp = 0;
		}
		else if (strcmp(token_table[i]->operator,"CSECT") == 0) {
			CSECT++;
		}
	}

	//�ɺ����̺� ����
	j = 0;
	for (i = 0; i < token_line; j++) {
		for (; i < token_line;) {
			if (token_table[i]->operator==NULL) {
				i++;
				continue;
			}
			if (token_table[i]->label != NULL || strcmp(token_table[i]->operator,"CSECT")==0) {
				sym_line++;
				sym_table[j] = (symbol*)malloc(sizeof(symbol));
				if (strcmp(token_table[i]->operator, "EQU") == 0) {			//EQU ��Ƽ�� ó��
					if (token_table[i]->operand[0][0] == 42) {
						sym_table[j]->addr = token_table[i]->adr;
						copystring(token_table[i]->label, sym_table[j]->symbol);
					}
					else {
						sym_table[j]->addr = symbol_cal(token_table[i]->operand[0]);
						copystring(token_table[i]->label, sym_table[j]->symbol);
					}
					i++;
					break;
				}
				else if (strcmp(token_table[i]->operator,"CSECT") == 0) {
					copystring("CSECT", sym_table[j]->symbol);
					i++;
					break;
				}
				else {													//�̿��� �ɺ��� ���
					sym_table[j]->addr = token_table[i]->adr;
					strcpy(sym_table[j]->symbol, token_table[i]->label);
					i++;
					break;
				}
			}
			else i++;
		}
	}

	return 0;
}

int search_opcode(char *str, inst **it)
{
	int opcode = -1;						//�⺻ opcode�� -1. -1�� ��� �ƹ��͵� ������� ����
	int i;									//������ ó���Ѵ�.
	for (i = 0;; i++) {
		if ((it[i] == NULL) || (str == NULL)) break;				//input_data[i]�� ����� ������ ������ ����.
		else if (strcmp(str, it[i]->name) == 0) opcode = it[i]->opcode;
		//inst_data�� operator ����� �� �ѷ�����.
	}
	return opcode;
}
int search_format(char *str, inst **it)		//format�� ã�� �Լ�
{
	int format = -1;						//�⺻ format�� -1. -1�� ��� �ƹ��͵� ������� ����
	int i;									//������ ó���Ѵ�.
	for (i = 0;; i++) {
		if ((it[i] == NULL) || (str == NULL)) break;				//input_data[i]�� ����� ������ ������ ����.
		else if (strcmp(str, it[i]->name) == 0) format = it[i]->format;
		//inst_data�� operator ����� �� �ѷ�����.
	}
	return format;
}
int search_reg(char *reg) {
	if (strcmp(reg, "A") == 0) return 0;
	else if(strcmp(reg, "X") == 0) return 1;
	else if (strcmp(reg, "L") == 0) return 2;
	else if (strcmp(reg, "B") == 0) return 3;
	else if (strcmp(reg, "S") == 0) return 4;
	else if (strcmp(reg, "T") == 0) return 5;
	else if (strcmp(reg, "F") == 0) return 6;
	else if (strcmp(reg, "PC") == 0) return 8;
	else if (strcmp(reg, "SW") == 0) return 9;
}
int search_ascii(char input) {
	if (input > 48 && input < 58) return (input - 48);
	else if (input > 65 && input < 71) return (input - 55);
}

char* search_adr_operand(char *name, int code_adr, int CSECT) {
	int i;
	int sectIndex = 0;
	for (i = 0; i < token_line; i++) {
		if (token_table[i]->operator == NULL) continue;
		if (token_table[i]->adr == code_adr && sectIndex == CSECT) {
			copystring(token_table[i]->operand[0], name);
			break;
		}
		else if (sectIndex != CSECT) {
			if(strcmp(token_table[i]->operator,"CSECT")==0) sectIndex++;
			else continue;
		}
	}
	return name;
}

int search_symbolAdr(char *name, symbol **table, int start) {
	int i = 0, j;
	if (start != -1) {
		for (i = 0; (table[i] != NULL && start>0); i++) {
			if (strcmp(table[i]->symbol, "CSECT") == 0) start--;
		}
	}
	for (; (table[i] != NULL); i++) {
		if (strcmp(table[i]->symbol, name) == 0) return table[i]->addr;
	}
	return -1;
}
int process_locctr(int format, char *opr, char *oprn) {
	int temp=0;
	if (format == 3 || strcmp(opr,"WORD")==0) locctr += 3;	
	//operand�� format�� ���� locctr�� ����.
	else if (strcmp(opr,"START")==0) locctr = 0;
	else if (format == 4) locctr += 4;
	else if (format == 2) locctr += 2;
	else if (format == 1 || strcmp(opr,"BYTE") == 0) locctr += 1;
	else if (strcmp(opr, "RESB") == 0) {
		temp = stringtoint(oprn);
		locctr += (1 * temp);
	}
	else if (strcmp(opr, "RESW")==0) {
		temp = stringtoint(oprn);
		locctr += (3 * temp);
	}
	else {}
	return format;
}

void make_symtab_output(char *file_name) {
	FILE *file;
	int i;
	file = fopen(file_name, "w");
	for (i = 0; i < sym_line-1; i++) {
		if(strcmp(sym_table[i]->symbol,"CSECT")!=0)
		fprintf(file, "%s\t%x\n", sym_table[i]->symbol, sym_table[i]->addr);
	}
	fclose(file);
}

char setAddressing(char nixbpe, char *operand,int format) {
	if (format > 2) {
		if (operand != NULL) {
			if (operand[0] == '#') nixbpe += 16;
			else if (operand[0] == '@') nixbpe += 32;
			else nixbpe += 48;
		}
		else nixbpe += 48;
	}
	return nixbpe;
}
char setRelative(char nixbpe, char *operand, char *operand2,char *operator,int format) {
	if (format == 3) {
		if (operand != NULL) {
			if (operand[0] == '#');
			else nixbpe += 2;
			if (operator[0] == '+') nixbpe += 1;
			if (operand2 == NULL) return nixbpe;
			else if (operand2[0] == 'X') nixbpe += 8;
		}
	}
	return nixbpe;
}

//EQU���� �ɺ����� ���ϰ� ���� ������ ó���Ѵ�.
int symbol_cal(char *operand) {
	int i, j, k,l=0;
	int cal[15];
	int result = 0;
	char temp[15];

	j = 0;
	for (i = 0;; i++) {
		if (i != 0) {						//�ɺ��� �� �� �̻� ���յ� �� ������ �����Ѵ�.
			if (operand[k] == '-' && j>1) {	//����صξ��� �������� ��ġ�� ã�ư���
				result -= cal[j - 1];		//�����ڿ� ���� �б⸦ �����Ѵ�.
				if (operand[i] == '\0') break;
			}
			else if (operand[k] == '+'&& j>1) {
				result += cal[j - 1];
				if (operand[i] == '\0') break;
			}
		}
		for (; (operand[i] != '-' && operand[i] != '+' && operand[i] != '\0'); i++) {
			temp[l] = operand[i];
			l++;					//�����ڰ� �����ų� ���۷��尡 ������ �� temp�� �Է� ����
		}
		temp[l] = '\0';
		cal[j] = search_symbolAdr(temp, sym_table,-1);
		if (j==0) result = cal[j];
		if(operand[i] != '\0') k = i;	//�������� ��ġ�� ����Ѵ�.
		j++;
		l = 0;
	}

	return result;
}

void copystring(char *A, char* toA) {		//A�� ������ toA�� �����ϴ� �Լ�
	int i;
	for (i = 0;; i++) {
		if (A[i] != '\0') toA[i] = A[i];
		else {
			toA[i] = '\0';
			break;
		}
	}
}
void clearstring(char *A, int maxindex) {	//A�� ������ maxindex��ŭ �η� �ʱ�ȭ�ϴ� �Լ�
	int i;
	for (i = 0; i < maxindex; i++) A[i] = '\0';
}
void freeall()
{
	int i, j;
	for (i = 0; i < inst_index; i++) {
		free(inst_table[i]->name);
		free(inst_table[i]);
	}
	for (i = 0; i < token_line; i++) {
		if(token_table[i]->comment!=NULL) free(token_table[i]->comment);
		if (token_table[i]->operator != NULL) free(token_table[i]->operator);
		if (token_table[i]->label != NULL) free(token_table[i]->label);
		for (j = 0; j < token_table[i]->Noperand; j++) {
			free(token_table[i]->operand[j]);
		}
		free(token_table[i]);
	}
	for (i = 0; i < sym_line; i++) {
		free(sym_table[i]);
	}
	for (i = 0; i < line_num; i++) {
		free(input_data[i]);
	}
	for (i = 0; i < code_line; i++) {
		free(code_table[i]);
	}
	for (i = 0; i < literal_line; i++) {
		free(literal_table[i]);
	}
}
int stringtoint(char *a) {
	int result = 0,i,j=0;
	int arr[10] = { 0 };
	for (i = 0;; i++) {
		if (a[i] == '\0') break;
		arr[i] = 1;
	}
	for (i = 9;i>-1; i--) {
		if (arr[i] == 0) continue;
		else {
			result = result + (a[i] - 48) * pow(10, j);
			j++;
		}
	}
	return result;
}

static int assem_pass2(void)
{
	int i, j, k;
	char nixbpe;
	int CSECT_Adr = 0;
	int literal_flag = 1;

	//�� ������ �ּҸ� ����ϰ� ������ ���̸� sectadr�� �����Ѵ�.
	for (i = 0; i < token_line; i++) {
		if (token_table[i]->operator==NULL) continue;
		if (strcmp(token_table[i]->operator,"CSECT") == 0) {
			if (token_table[i - 1]->operator_length == -1 && strcmp(token_table[i - 1]->operator,"WORD") == 0)
				SectAdr[CSECT_Adr] = token_table[i - 1]->adr + 3;
			else if (token_table[i - 1]->operator_length == -1 && strcmp(token_table[i - 1]->operator,"BYTE") == 0)
				SectAdr[CSECT_Adr] = token_table[i - 1]->adr + 1;
			else if (token_table[i - 1]->operator_length == -1) SectAdr[CSECT_Adr] = token_table[i - 1]->adr;
			else SectAdr[CSECT_Adr] = token_table[i - 1]->adr + token_table[i - 1]->operator_length;
			CSECT_Adr++;
		}
	}
	SectAdr[CSECT_Adr] = token_table[i - 1]->adr;


	CSECT_Adr = 0;
	for (i = 0; i < token_line; i++) {
		if (token_table[i]->operator==NULL) continue;

		if (token_table[i]->operator_length != -1) {
			code_line++;
			code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));

			//ni�� ����ϴ� opcode�κ�, ��ɾ ���� ��츸 üũ
			code_table[code_line - 1]->opcode =
				search_opcode(token_table[i]->operator,inst_table);
			nixbpe = token_table[i]->nixbpe;
			if ((nixbpe & 32) && !(nixbpe & 16))
				code_table[code_line - 1]->opcode += 2;
			else if (!(nixbpe & 32) && (nixbpe & 16))
				code_table[code_line - 1]->opcode += 1;
			else if ((nixbpe & 32) && (nixbpe & 16))
				code_table[code_line - 1]->opcode += 3;
			else code_table[code_line - 1]->opcode += 0;

			//����� �ּҸ� ����ϴ� �κ��̴�.
			switch (token_table[i]->operator_length) {

				//format 2�� ��� adr�κп��� ���������� ���� �޾ƿ´�.
			case 2:
				code_table[code_line - 1]->format = 2;
				code_table[code_line - 1]->opcode = search_opcode(token_table[i]->operator,inst_table);
				if (token_table[i]->Noperand == 1) {
					code_table[code_line - 1]->adr =
						search_reg(token_table[i]->operand[0]) * 16;
				}
				else if (token_table[i]->Noperand == 2) {
					code_table[code_line - 1]->adr =
						search_reg(token_table[i]->operand[0]) * 16 + search_reg(token_table[i]->operand[1]);
				}
				break;

				//format 3�� ��� �ּ�ó���� �б����� ����.
			case 3:
				code_table[code_line - 1]->format = 3;
				int TA = 0;
				char temp[15];

				//non relative�� ���
				if (token_table[i]->Noperand != 0 && token_table[i]->operand[0][0] == '#') {
					for (j = 0; token_table[i]->operand[0][j + 1] != '\0'; j++) {
						temp[j] = token_table[i]->operand[0][j + 1];
					}
					temp[j] = '\0';
					code_table[code_line - 1]->adr = stringtoint(temp);
					code_table[code_line - 1]->xbpe = 0;
				}

				//pc relative (indirect)�� ���
				else if (token_table[i]->Noperand != 0 && token_table[i]->operand[0][0] == '@') {
					for (j = 0; token_table[i]->operand[0][j + 1] != '\0'; j++) {
						temp[j] = token_table[i]->operand[0][j + 1];
					}
					temp[j] = '\0';
					TA = search_symbolAdr(temp, sym_table, CSECT_Adr);
					code_table[code_line - 1]->adr = TA - (token_table[i]->adr + token_table[i]->operator_length);

					//ta-pc�� ������ ���
					if (code_table[code_line - 1]->adr < 0) {
						unsigned int minusadr;
						minusadr = code_table[code_line - 1]->adr;
						minusadr << 20;
						minusadr >> 20;
						code_table[code_line - 1]->adr = minusadr;
					}
					code_table[code_line - 1]->xbpe = 2;
				}

				//literal�� ���
				else if ((token_table[i]->Noperand != 0) && (token_table[i]->operand[0][0] == '=')) {
					int TF_LTORG = 0;
					int index_LTORG;
					int index = SectAdr[CSECT_Adr];
					int sectIndex = 0;
					for (j = i; j < token_line; j++) {
						if (strcmp(token_table[j]->operator,"CSECT") == 0) break;
						if (strcmp(token_table[j]->operator,"LTORG") == 0) {
							TF_LTORG = 1;
							index_LTORG = token_table[j]->adr;
						}

					}
					for (j = 0;; j++) {
						if (strcmp(literal_table[j]->name, "CSECT") == 0) {
							if (sectIndex < CSECT_Adr) {
								sectIndex++;
								continue;
							}
						}
						else if (sectIndex == CSECT_Adr) {
							if (strcmp(literal_table[j]->name, token_table[i]->operand[0]) == 0) {
								if (TF_LTORG == 1) {
									TA = index_LTORG;
									index_LTORG += literal_table[j]->length;
									break;
								}
								else {
									TA = index;
									index += literal_table[j]->length;
									break;
								}
							}
						}
					}

					code_table[code_line - 1]->adr = TA - (token_table[i]->adr + token_table[i]->operator_length);
					//ta-pc�� ������ ���
					if (code_table[code_line - 1]->adr < 0) {
						unsigned int minusadr;
						minusadr = code_table[code_line - 1]->adr;
						minusadr << 20;
						minusadr >> 20;
						code_table[code_line - 1]->adr = minusadr;
					}
					code_table[code_line - 1]->xbpe = 2;
					literal_flag = 1;

				}

				//pc relative�� ���
				else if (token_table[i]->Noperand != 0) {
					TA = search_symbolAdr(token_table[i]->operand[0], sym_table, CSECT_Adr);
					code_table[code_line - 1]->adr = TA - (token_table[i]->adr + token_table[i]->operator_length);

					//ta-pc�� ������ ���
					if (code_table[code_line - 1]->adr < 0) {
						unsigned int minusadr;
						minusadr = code_table[code_line - 1]->adr;
						minusadr = minusadr << 20;
						minusadr = minusadr >> 20;
						code_table[code_line - 1]->adr = minusadr;
					}
					code_table[code_line - 1]->xbpe = 2;
				}

				//operand�� ���� ���
				else {
					code_table[code_line - 1]->adr = 0;
					code_table[code_line - 1]->xbpe = 0;
				}
				if (token_table[i]->Noperand > 1) {
					if (strcmp(token_table[i]->operand[1], "X") == 0) code_table[code_line - 1]->xbpe += 8;
				}
				break;

				//format 4�� ��� �ܺ��ּ��� ��� 0, �ƴ� ��� ���� �״�� �޴´�.
			case 4:
				code_table[code_line - 1]->format = 4;
				for (j = 0; j < ExtAdr_line; j++) {
					if ((ExtAdr_table[j]->CSECTnum == CSECT_Adr) && (ExtAdr_table[j]->TF_ExtR == 1)) {
						for (k = 0; strcmp("NULL", ExtAdr_table[j]->AdrLists[k]) != 0; k++) {
							if (strcmp(ExtAdr_table[ExtAdr_line - 1]->AdrLists[k], token_table[i]->operand[0]) == 0) {
								code_table[code_line - 1]->TF_extAdr = 1;
								code_table[code_line - 1]->adr = 0;
								break;
							}
							else code_table[code_line - 1]->TF_extAdr = 0;
						}
					}
				}
				if (code_table[code_line - 1]->TF_extAdr == 0 && token_table[i]->operand[0][0] == '#') {
					char temp[20];
					int l;
					for (l = 0; token_table[i]->operand[0][l + 1] != '\0'; l++) {
						temp[l] = token_table[i]->operand[0][l + 1];
					}
					temp[l] = '\0';
					code_table[code_line - 1]->adr = search_symbolAdr(temp, sym_table, CSECT_Adr);
				}
				code_table[code_line - 1]->xbpe = 1;
				if (token_table[i]->Noperand > 1) {
					if (strcmp(token_table[i]->operand[1], "X") == 0) code_table[code_line - 1]->xbpe += 8;
				}
				code_table[code_line - 1]->realadr = token_table[i]->adr;
				break;
			}
		}
		//�Ϲ� ��ɾ��� ����� �ּ� ó�� �б� ����

		//�� �� �б�� �ܺ��ּ� ���� ��Ƽ�긦 ó���Ѵ�.
		else if (strcmp(token_table[i]->operator,"EXTDEF") == 0) {
			ExtAdr_line++;
			ExtAdr_table[ExtAdr_line - 1] = (ExtAdr*)malloc(sizeof(ExtAdr));
			ExtAdr_table[ExtAdr_line - 1]->CSECTnum = CSECT_Adr;
			ExtAdr_table[ExtAdr_line - 1]->TF_ExtR = 0;
			for (j = 0; j < token_table[i]->Noperand; j++) {
				copystring(token_table[i]->operand[j], ExtAdr_table[ExtAdr_line - 1]->AdrLists[j]);
			}
			copystring("NULL", ExtAdr_table[ExtAdr_line - 1]->AdrLists[token_table[i]->Noperand]);
		}
		else if (strcmp(token_table[i]->operator,"EXTREF") == 0) {
			ExtAdr_line++;
			ExtAdr_table[ExtAdr_line - 1] = (ExtAdr*)malloc(sizeof(ExtAdr));
			ExtAdr_table[ExtAdr_line - 1]->CSECTnum = CSECT_Adr;
			ExtAdr_table[ExtAdr_line - 1]->TF_ExtR = 1;
			for (j = 0; j < token_table[i]->Noperand; j++) {
				copystring(token_table[i]->operand[j], ExtAdr_table[ExtAdr_line - 1]->AdrLists[j]);
			}
			copystring("NULL", ExtAdr_table[ExtAdr_line - 1]->AdrLists[token_table[i]->Noperand]);
		}

		//csect�� ��� ���� �ε��� ������ �����Ѵ�. ���� LTORG�� ó������ ���� ���ͷ��� ó���Ѵ�.
		else if (strcmp(token_table[i]->operator,"CSECT") == 0) {
			if (literal_flag) {
				int l = 0;
				int sectIndex = 0;

				for (l = 0; l < literal_line; l++) {
					if (strcmp(literal_table[l]->name, "CSECT") == 0) sectIndex++;

					if (sectIndex < CSECT_Adr) {
						continue;
					}
					else if (sectIndex == CSECT_Adr) {
						if (strcmp(literal_table[l]->val, "NULL") != 0) {
							code_line++;
							code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
							code_table[code_line - 1]->opcode = literal_table[l]->val[0];
							int xbpe = literal_table[l]->val[1] & 240;
							int string = (literal_table[l]->val[1] & 15) * 256;
							string += literal_table[l]->val[2];
							code_table[code_line - 1]->xbpe = xbpe;
							code_table[code_line - 1]->adr = string;
							code_table[code_line - 1]->format = 3;
						}
						else if (literal_table[l]->Xval != -1) {
							code_line++;
							code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
							code_table[code_line - 1]->opcode = literal_table[l]->Xval;
							code_table[code_line - 1]->format = 1;
						}
					}
					else if (sectIndex > CSECT_Adr) break;
				}
			}
			code_line++;
			code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
			code_table[code_line - 1]->csect = 1;
			CSECT_Adr++;
			literal_flag = 0;
		}

		//word/byte�� ��� ������ �����Ѵ�.
		else if (strcmp(token_table[i]->operator,"WORD") == 0) {
			code_line++;
			code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
			if (token_table[i]->operand[0][1] == 39) {
				char temp = token_table[i]->operand[0][2];
				code_table[code_line - 1]->opcode = temp;
				temp = token_table[i]->operand[0][3];
				code_table[code_line - 1]->adr = temp * 256;
				temp = token_table[i]->operand[0][4];
				code_table[code_line - 1]->adr += temp;
				code_table[code_line - 1]->TF_extAdr = 0;

			}
			else {
				code_table[code_line - 1]->opcode = 0;
				code_table[code_line - 1]->adr = 0;
				code_table[code_line - 1]->xbpe = 0;
				code_table[code_line - 1]->TF_extAdr = 1;
			}
			code_table[code_line - 1]->format = 3;
			code_table[code_line - 1]->realadr = token_table[i]->adr;
		}
		else if (strcmp(token_table[i]->operator,"BYTE") == 0) {
			code_line++;
			code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
			if (token_table[i]->operand[0][1] == 39) {
				int temp = (16 * search_ascii(token_table[i]->operand[0][2])) + 
					search_ascii(token_table[i]->operand[0][3]);
				code_table[code_line - 1]->opcode = temp;
				code_table[code_line - 1]->TF_extAdr = 0;
			}
			else {
				code_table[code_line - 1]->opcode = 0;
				code_table[code_line - 1]->adr = 0;
				code_table[code_line - 1]->TF_extAdr = 1;
			}
			code_table[code_line - 1]->format = 1;
			code_table[code_line - 1]->realadr = token_table[i]->adr;
		}

		//LTORG�� ��� ó��
		else if (strcmp(token_table[i]->operator,"LTORG") == 0) {
			int l = 0;
			int sectIndex = 0;

			for (l = 0; l < literal_line; l++) {
				if (strcmp(literal_table[l]->name, "CSECT") == 0) sectIndex++;

				if (sectIndex < CSECT_Adr) {
					continue;
				}
				else if (sectIndex == CSECT_Adr) {
					if (strcmp(literal_table[l]->val, "NULL") != 0) {
						code_line++;
						code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
						code_table[code_line - 1]->opcode = literal_table[l]->val[0];
						int xbpe = (literal_table[l]->val[1] & 240)>>4;
						int string = (literal_table[l]->val[1] & 15) * 256;
						string += literal_table[l]->val[2];
						code_table[code_line - 1]->xbpe = xbpe;
						code_table[code_line - 1]->adr = string;
						code_table[code_line - 1]->format = 3;
					}
					else if (literal_table[l]->Xval != -1) {
						code_line++;
						code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
						code_table[code_line - 1]->opcode = literal_table[l]->Xval;
						code_table[code_line - 1]->format = 1;
					}
				}
				else if (sectIndex > CSECT_Adr) break;
			}
		}
		
		//���� ��� ������ ���ͷ� ó���� �Ѵ�.
		else if (strcmp(token_table[i]->operator,"END") == 0) {
			if (literal_flag) {
				int l = 0;
				int sectIndex = 0;

				for (l = 0; l < literal_line; l++) {
					if (strcmp(literal_table[l]->name, "CSECT") == 0) sectIndex++;

					if (sectIndex < CSECT_Adr) {
						continue;
					}
					else if (sectIndex == CSECT_Adr) {
						if (strcmp(literal_table[l]->val, "NULL") != 0) {
							code_line++;
							code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
							code_table[code_line - 1]->opcode = literal_table[l]->val[0];
							int xbpe = literal_table[l]->val[1] & 240;
							int string = (literal_table[l]->val[1] & 15) * 256;
							string += literal_table[l]->val[2];
							code_table[code_line - 1]->xbpe = xbpe;
							code_table[code_line - 1]->adr = string;
							code_table[code_line - 1]->format = 3;
						}
						else if (literal_table[l]->Xval != -1) {
							code_line++;
							code_table[code_line - 1] = (mUnit*)malloc(sizeof(mUnit));
							code_table[code_line - 1]->opcode = literal_table[l]->Xval;
							code_table[code_line - 1]->format = 1;
						}
					}
					else if (sectIndex > CSECT_Adr) break;
				}
			}
		}
		literal_flag = 0;
	}
	return 0;
}


void make_objectcode_output(char *file_name)
{
	int CSECT = 0;		// ���ͺ� ����� ���� �� ���� �ε���
	int i, j, k, l, extindex = 0;//extref���� ó���� ���� �迭 �ε���
	int startadr[500] = { -1 };		// �ӽ��ڵ忡�� �� ������ �����ּҸ� �ǹ�
	int codelength[500] = { -1 };	//�ӽ��ڵ��� ���̸� ��Ÿ�� ����
	int codes = 0;					//�ӽ��ڵ��� ���̸� ������ �ӽú���
	int sectIndex = 0;
	int literalflag = 0;
	int temp = 0;
	int starttemp;
	char *name = (char*)malloc(30);

	startadr[0] = 0;
	j = 1;				//�� �ڵ����� �ε���
	int flag = 0;		//�ӽ��ڵ带 �Է¹����� ���� �����ϴ� ����.
	for (i = 0; i < token_line; i++) {
		if (token_table[i]->operator==NULL) continue;

		//�Ϲ� �ӽ��ڵ��� ���
		if (flag != 0 && token_table[i]->operator_length != -1) {
			if (codes < 29) codes += token_table[i]->operator_length;
			else {
				startadr[j] = codes + startadr[j - 1];
				codelength[j - 1] = codes;
				codes = 0;
				i--;
				j++;
			}
		}

		//�̿��� ���
		else {
			if (strcmp(token_table[i]->operator,"EXTREF") == 0) flag = 1;
			else if (strcmp(token_table[i]->operator,"RESW") == 0
				|| strcmp(token_table[i]->operator,"RESB") == 0) {
				if (flag == 1) {
					flag = 0;
					codelength[j - 1] = codes;
					codes = 0;
					j++;
				}
			}
			//���ͷ� ���̺��� �����ϸ� csect �ε����� ���� ������ ���ͷ��� �����ּҿ� ���̸� �����Ѵ�.
			else if (strcmp(token_table[i]->operator,"LTORG") == 0) {
				for (k = 0;; k++) {
					if (sectIndex < CSECT) {
						k++;
						continue;
					}
					else if (sectIndex > CSECT) break;

					if (strcmp(literal_table[k]->name, "CSECT") == 0) {
						sectIndex++;
						literalflag = 0;
						continue;
					}
					if (!(literalflag)) {
						starttemp = literal_table[k]->adr;
					}
					literalflag = 1;
					temp += literal_table[k]->length;

				}
				startadr[j - 1] = starttemp;
				codelength[j - 1] = temp;
				temp = 0;
				j++;
			}
			else if (strcmp(token_table[i]->operator,"CSECT") == 0) {
				if (codes != 0) {
					codelength[j - 1] = codes;
					codes = 0;
					j++;
				}
				startadr[j - 1] = 0;
				CSECT++;
			}

			//word/byte ��ɹ��� ��� ��� ��ɾ�ó�� �ڵ���̸� �����Ѵ�.
			else if (strcmp(token_table[i]->operator,"BYTE") == 0) {
				if (codes < 29) codes += 1;
				else {
					startadr[j] = codes + startadr[j - 1];
					codelength[j - 1] = codes;
					codes = 0;
					j++;
				}
			}
			else if (strcmp(token_table[i]->operator,"WORD") == 0) {
				if (codes < 29) codes += 3;
				else {
					startadr[j] = codes + startadr[j - 1];
					codelength[j - 1] = codes;
					codes = 0;
					j++;
				}
			}

		}
	}
	if (codes != 0) codelength[j - 1] = codes; // �ڵ���̰� ������� �ʰ� ������� ��� �ڵ���̸� ���� �����Ѵ�.
		

	int left_literal; // LTORGó������ ���� ���ͷ��� ����
	k = 0;
	int m = 0;
	flag = 0;
	CSECT = 0;
	if (file_name != NULL) {
		FILE *file;
		file = fopen(file_name, "w");
		for (i = 0;;) {
			if (code_table[i] == NULL) break;
			//HCOPY�� EXTREF,EXTDEF�� ���
			fprintf(file,"\nH%s %06x%06x\n", start_labels[CSECT], 0, SectAdr[CSECT]);

			if (ExtAdr_table[extindex]->TF_ExtR != 1) {
				fprintf(file, "D");
				for (j = 0; strcmp(ExtAdr_table[extindex]->AdrLists[j], "NULL") != 0; j++) {
					fprintf(file, "%s", ExtAdr_table[extindex]->AdrLists[j]);
					int extadr = search_symbolAdr(ExtAdr_table[extindex]->AdrLists[j], sym_table, -1);
					fprintf(file, "%06x", extadr);
				}
				extindex++;
			}
			fprintf(file, "\n");
			if (ExtAdr_table[extindex]->TF_ExtR == 1) {
				fprintf(file, "R");
				for (j = 0; strcmp(ExtAdr_table[extindex]->AdrLists[j], "NULL") != 0; j++) {
					fprintf(file, "%s", ExtAdr_table[extindex]->AdrLists[j]);
				}
				extindex++;
			}
			//�ӽ��ڵ带 ����Ѵ�.
			for (;;) {
				//�ڵ����̺��� ����Ǿ��� �� �б�
				if (code_table[i] == NULL) {
					l = 0;
					for (; code_table[k]->csect != 1; k++) {
						if (code_table[k]->TF_extAdr == 1 && code_table[k]->format == 4) {
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);
							fprintf(file, "\nM%06x05+%s", code_table[k]->realadr + 1, name);
						}
						//WORD/BYTE������ �ܺ��ּҿ����� ����. ������ 4�� �ƴϸ� WORD 
						else if (code_table[k]->TF_extAdr == 1 && code_table[k]->format != 4) {
							char temp[50];
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);

							int minus = 0;
							for (; name[l] != '\0'; l++) {
								temp[l] = name[i];
								l++;
								if (name[l] == '-') {

									temp[l + 1] = '\0';
									if (minus) fprintf(file, "\nM%06x06+%s", code_table[k]->realadr, name);
									else fprintf(file, "\nM%06x06-%s", code_table[k]->realadr, name);
									minus = 1;
								}
								else if (name[l] != '+') {

									temp[l + 1] = '\0';
									if (minus)fprintf(file, "\nM%06x06+%s", code_table[k]->realadr, name);
									else fprintf(file, "\nM%06x06-%s", code_table[k]->realadr, name);
									minus = 0;
								}
							}
							if (minus) fprintf(file, "\nM%06x06+%s", code_table[k]->realadr, name);
							else fprintf(file, "\nM%06x06-%s", code_table[k]->realadr, name);

						}
						else if (code_table[k + 1] == NULL) break;
					}

					fprintf(file, "\nE\n");
					i++;
					break;
				}
				else if (code_table[i]->csect != 1) {
					//�ϳ��� �� �ӽ��ڵ带 ���� ����Ѵ�.
					codes = 0;
					fprintf(file, "\nT%06x%02x", startadr[m], codelength[m]);
					for (; codes < codelength[m]; i++) {
						switch (code_table[i]->format) {
						case 1:
							fprintf(file, "%02x", code_table[i]->opcode);
							codes += 1;
							break;
						case 2:
							fprintf(file, "%x%02x", code_table[i]->opcode, code_table[i]->adr);
							codes += 2;
							break;
						case 3:
							fprintf(file, "%02x%x%03x", code_table[i]->opcode, code_table[i]->xbpe, code_table[i]->adr);
							codes += 3;
							break;
						case 4:
							fprintf(file, "%x%x%05x", code_table[i]->opcode, code_table[i]->xbpe, code_table[i]->adr);
							codes += 4;
							break;
						}
					}
					m++;
				}

				//codeTable �� ���͸� ������ ��ĵ�Ͽ� �ڵ带 ���������� ���� �б�
				else if (code_table[i]->csect == 1) {
					l = 0;
					int temp_index = 0;
					for (; code_table[k]->csect != 1; k++) {
						if (code_table[k]->TF_extAdr == 1 && code_table[k]->format == 4) {
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);
							fprintf(file, "\nM%06x05+%s", code_table[k]->realadr + 1, name);
						}
						//WORD/BYTE������ �ܺ��ּҿ����� ����. ������ 4�� �ƴϸ� WORD 
						else if (code_table[k]->TF_extAdr == 1 && code_table[k]->format != 4) {
							char temp[50];
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);

							int minus = 0;
							for (; name[l] != '\0'; l++) {
								temp[temp_index] = name[l];
								if (name[l] == '-') {
									temp[temp_index] = '\0';
									if (minus) fprintf(file, "\nM%06x06-%s", code_table[k]->realadr, temp);
									else fprintf(file, "\nM%06x06+%s", code_table[k]->realadr, temp);
									minus = 1;
									temp_index = 0;
								}
								else if (name[l] == '+') {
									temp[temp_index] = '\0';
									if (minus) fprintf(file, "\nM%06x06-%s", code_table[k]->realadr, temp);
									else fprintf(file, "\nM%06x06+%s", code_table[k]->realadr, temp);
									minus = 0;
									temp_index = 0;
								}
								else temp_index++;
							}
							temp[temp_index] = '\0';
							if (minus) fprintf(file, "\nM%06x06-%s", code_table[k]->realadr, temp);
							else fprintf(file, "\nM%06x06+%s", code_table[k]->realadr, temp);

						}
					}

					if (code_table[k]->csect == 1) k++;
					fprintf(file, "\nE\n");
					CSECT++;
					i++;
					break;
				}
			}
		}
	}
	else {
		for (i = 0;;) {
			if (code_table[i] == NULL) break;

			//HCOPY�� EXTREF,EXTDEF�� ���
			printf("\nH%s %06x%06x\n",start_labels[CSECT],0, SectAdr[CSECT]);

			if (ExtAdr_table[extindex]->TF_ExtR != 1) {
				printf("D");
				for (j = 0; strcmp(ExtAdr_table[extindex]->AdrLists[j], "NULL") != 0; j++) {
					printf("%s", ExtAdr_table[extindex]->AdrLists[j]);
					int extadr = search_symbolAdr(ExtAdr_table[extindex]->AdrLists[j], sym_table, -1);
					printf("%06x", extadr);
				}
				extindex++;
			}
			printf("\n");
			if (ExtAdr_table[extindex]->TF_ExtR == 1) {
				printf("R");
				for (j = 0; strcmp(ExtAdr_table[extindex]->AdrLists[j], "NULL") != 0; j++) {
					printf("%s", ExtAdr_table[extindex]->AdrLists[j]);
				}
				extindex++;
			}
			//�ӽ��ڵ带 ����Ѵ�.
			for (;;) {
				//�ڵ����̺��� ����Ǿ��� �� �б�
				if (code_table[i] == NULL) {
					l = 0;
					for (; code_table[k]->csect != 1; k++) {
						if (code_table[k]->TF_extAdr == 1 && code_table[k]->format == 4) {
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);
							printf("\nM%06x05+%s", code_table[k]->realadr + 1, name);
						}
						//WORD/BYTE������ �ܺ��ּҿ����� ����. ������ 4�� �ƴϸ� WORD 
						else if (code_table[k]->TF_extAdr == 1 && code_table[k]->format != 4) {
							char temp[50];
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);

							int minus = 0;
							for (; name[l] != '\0'; l++) {
								temp[l] = name[i];
								l++;
								if (name[l] == '-') {

									temp[l + 1] = '\0';
									if (minus) printf("\nM%06x06+%s", code_table[k]->realadr, name);
									else printf("\nM%06x06-%s", code_table[k]->realadr, name);
									minus = 1;
								}
								else if (name[l] != '+') {

									temp[l + 1] = '\0';
									if (minus) printf("\nM%06x06+%s", code_table[k]->realadr, name);
									else printf("\nM%06x06-%s", code_table[k]->realadr, name);
									minus = 0;
								}
							}
							if (minus) printf("\nM%06x06+%s", code_table[k]->realadr, name);
							else printf("\nM%06x06-%s", code_table[k]->realadr, name);

						}
						else if (code_table[k + 1] == NULL) break;
					}

					printf("\nE\n");
					i++;
					break;
				}
				else if (code_table[i]->csect != 1) {
					//�ϳ��� �� �ӽ��ڵ带 ���� ����Ѵ�.
					codes = 0;
					printf("\nT%06x%02x", startadr[m], codelength[m]);
					for (; codes < codelength[m]; i++) {
						switch (code_table[i]->format) {
						case 1:
							printf("%02x", code_table[i]->opcode);
							codes += 1;
							break;
						case 2:
							printf("%x%02x", code_table[i]->opcode, code_table[i]->adr);
							codes += 2;
							break;
						case 3:
							printf("%02x%x%03x", code_table[i]->opcode, code_table[i]->xbpe, code_table[i]->adr);
							codes += 3;
							break;
						case 4:
							printf("%x%x%05x", code_table[i]->opcode, code_table[i]->xbpe, code_table[i]->adr);
							codes += 4;
							break;
						}
					}
					m++;
				}

				//codeTable �� ���͸� ������ ��ĵ�Ͽ� �ڵ带 ���������� ���� �б�
				else if (code_table[i]->csect == 1){
					l = 0;
					int temp_index = 0;
					for (; code_table[k]->csect != 1; k++) {
						if (code_table[k]->TF_extAdr == 1 && code_table[k]->format == 4) {
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);
							printf("\nM%06x05+%s", code_table[k]->realadr+1, name);
						}
						//WORD/BYTE������ �ܺ��ּҿ����� ����. ������ 4�� �ƴϸ� WORD 
						else if (code_table[k]->TF_extAdr == 1 && code_table[k]->format != 4) {
							char temp[50];
							name = search_adr_operand(name, code_table[k]->realadr, CSECT);

							int minus = 0;
							for (; name[l] != '\0'; l++) {
								temp[temp_index] = name[l];
								if (name[l] == '-') {
									temp[temp_index] = '\0';
									if (minus) printf("\nM%06x06-%s", code_table[k]->realadr, temp);
									else printf("\nM%06x06+%s", code_table[k]->realadr, temp);
									minus = 1;
									temp_index = 0;
								}
								else if (name[l] == '+') {
									temp[temp_index] = '\0';
									if (minus) printf("\nM%06x06-%s", code_table[k]->realadr, temp);
									else printf("\nM%06x06+%s", code_table[k]->realadr, temp);
									minus = 0;
									temp_index = 0;
								}
								else temp_index++;
							}
							temp[temp_index] = '\0';
							if (minus) printf("\nM%06x06-%s", code_table[k]->realadr, temp);
							else printf("\nM%06x06+%s", code_table[k]->realadr, temp);

						}
					}

					if (code_table[k]->csect == 1) k++;
					printf("\nE\n");
					CSECT++;
					i++;
					break;
				}
			}
		}
	}
}
