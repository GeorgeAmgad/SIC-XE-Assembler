# SIC-XE-Assembler

1. A parser that is capable of handling source lines that are instructions, storage declaration, comments, and assembler directives.
2. For instructions, the parser is capable of decoding 2, 3 and 4-byte instructions as follows:

	a) 2-byte with 1 or 2 symbolic register reference (e.g., TIXR A, ADDR S, A).

	b) 3-byte PC-relative with symbolic operand to include immediate, indirect, and indexed addressing.

	c) 3-byte absolute with non-symbolic operand to include immediate, indirect, and indexed addressing.

	d) 4-byte absolute with symbolic or non-symbolic operand to include immediate, indirect, and indexed addressing .

	
3. The parser handles all storage directives (BYTE, WORD, RESW, and RESB).
4. The parser handles EQU and ORG statements.
5. Simple expression evaluation. (A <op> B) operand arithmetic, where <op> is one of +, -, *, / and no spaces surround the operation, e.g. A+B.
6. The output of this assembler contain:

	a) The symbol table.
	
	b) The source program in a format like the listing file described in the text book.
  
	c) A meaningful error message printed above the line in which the error occurred.
	
	d) Object-code file whose format is the same as the one described in the textbook in section 2.1.1 and 2.3.5.
	
	
