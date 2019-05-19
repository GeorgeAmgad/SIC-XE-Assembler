import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class Assembler {
    private static final HashMap<String, Integer> SYMTABLE;
    private static final HashMap<String, Integer> REGTABLE;
    private static final List<Error> ERRORS;


    private final File source;
    private final File listFile;
    private final File objectFile;

    private List<Statement> statements = new ArrayList<>();

    private String progName;
    private int progStartAddr;
    private int progLength;

    private boolean started = false;
    private boolean ended = false;
    private int locctr; //location counter

    static {
        SYMTABLE = Tables.getSymbolTable();
        REGTABLE = Tables.getREGTABLE();
        ERRORS = Tables.getErrorsTable();
    }

    Assembler(String source, String listFile, String objectFile) {
        this.source = new File(source);
        this.listFile = new File(listFile);
        this.objectFile = new File(objectFile);

        this.progStartAddr = 0;
        this.progLength = 0;
        this.locctr = 0;
    }

    void assemble() {
        try {
            firstPass();
            secondPass();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void firstPass() throws IOException {

        BufferedReader src = new BufferedReader(new FileReader(source));
        BufferedWriter list = new BufferedWriter(new FileWriter(listFile));
        String line;

        while ((line = src.readLine()) != null) {
            Statement statement = new Statement(line);
            if (statement.isComment()) {
                statements.add(statement);
                continue;
            }

            if (statement.getError() != null) {
                statements.add(statement);
                continue;
            }

            if (statement.getMnemonic().getString().equalsIgnoreCase("START") && !started) {
                // Save #[OPERAND] as starting address, and #[LABEL] as program name.
                // parseInt converts a string of base radix (16) to an integer
                progStartAddr = Integer.parseInt(statement.getFirstOperand().getLine(), 16);
                progName = statement.getLabel();

                // Initialize LOCCTR to starting address.
                locctr = progStartAddr;

                // Write line to list file.
                statement.setAddress(locctr);
                statements.add(statement);
                SYMTABLE.put(statement.getLabel(), locctr);
                started = true;
                continue;
            } else if (!started) {
                // Initialize LOCCTR to 0.
                statement.setError(ERRORS.get(10));
                locctr = 0;
                break;
            }

            int growthSize = 0;

            if (statement.getMnemonic().getString().equalsIgnoreCase("END") && !ended) {
                statement.setAddress(locctr);
                if (statement.hasFirstOperand()) {
                    if (!statement.getFirstOperand().getLine().equalsIgnoreCase(progName)) {
                        statement.setError(ERRORS.get(12));
                    }
                }
                statements.add(statement);
                progLength = locctr - progStartAddr - 1;
                ended = true;
                break;
            }

            if (statement.hasLabel()) {
                // Search SYMTABLE for LABEL.
                if (SYMTABLE.containsKey(statement.getLabel()) || statement.getLabel().contains("[+\\-*/]")) {
                    statement.setError(ERRORS.get(0));
                } else {
                    // Insert (LABEL, LOCCTR) into SYMTABLE.
                    SYMTABLE.put(statement.getLabel(), locctr);
                }
            }
            boolean locctrChanged = false;
            if (statement.getMnemonic().isDirective()) {
                int expression;

                String[] operand;
                switch (statement.getMnemonic().getString()) {
                    case "WORD":
                        growthSize = 3;
                        statement.setInstruction(String.format("%06X", Integer.parseInt(statement.getFirstOperand().getLine())));
                        break;

                    case "RESW":
                        growthSize = Integer.parseInt(statement.getFirstOperand().getLine()) * 3;
                        break;

                    case "RESB":
                        growthSize = Integer.parseInt(statement.getFirstOperand().getLine());
                        break;

                    case "BYTE":
                        // Find length of constant in bytes.
                        // Add length to LOCCTR.
                        operand = statement.getFirstOperand().getLine().split("'");
                        String dataType = operand[0];
                        int length = operand[1].length();

                        switch (dataType) {
                            case "c":
                            case "C":
                                String s = operand[1];
                                StringBuilder sb = new StringBuilder();

                                for (char c : s.toCharArray())
                                    sb.append(Integer.toHexString((int) c));

                                BigInteger mInt = new BigInteger(sb.toString());
                                statement.setInstruction(mInt.toString());
                                growthSize = length;
                                break;
                            case "x":
                            case "X":
                                if (length % 2 != 0) {
                                    length++;
                                    statement.setError(ERRORS.get(20));
                                }
                                statement.setInstruction(operand[1]);
                                growthSize = length / 2;
                                break;
                            default:
                                statement.setError(ERRORS.get(11));
                        }

                        if (dataType.equalsIgnoreCase("x")) {
                            if (!isHex(operand[1])) {
                                statement.setError(ERRORS.get(6));
                            }
                        }
                        break;

                    case "ORG":
                        if (statement.hasLabel()) {
                            statement.setError(ERRORS.get(18));
                            break;
                        }
                        expression = evaluateExpression(statement);
                        if (expression != -1) {
                            statement.setAddress(locctr);
                            statements.add(statement);
                            locctr = expression;
                            locctrChanged = true;
                            break;

                        } else {
                            statement.setError(ERRORS.get(5));
                            break;
                        }
                    case "EQU":
                        expression = evaluateExpression(statement);
                        if (expression != -1) {
                            if (!statement.hasLabel()) {
                                statement.setError(ERRORS.get(17));
                                break;
                            }
                            SYMTABLE.replace(statement.getLabel(), expression);
                        }
                        break;
                    default:
                        // Set error flag (invalid operation code).
                        statement.setError(ERRORS.get(4));
                }

                // could be changed by ORG
                if (!locctrChanged) {
                    statement.setAddress(locctr);
                    statements.add(statement);
                    locctr += growthSize;
                }
                continue;
            }

            growthSize = statement.getMnemonic().getSize();
            statement.setAddress(locctr);
            statements.add(statement);

            locctr += growthSize;

        }

        // symtable and hex numbers errors handling
        for (Statement statement : statements) {
            if (!statement.isComment() && statement.getMnemonic() != null) {
                if (!statement.getMnemonic().isRegisterType() && !statement.getMnemonic().isDirective() &&
                        statement.hasFirstOperand()) {
                    if (statement.getFirstOperand().isIndirect()) {
                        boolean isLabel = false;
                        if (SYMTABLE.containsKey(statement.getFirstOperand().getFilteredLine())) {
                            isLabel = true;
                        }
                        if (!isLabel && !isHex(statement.getFirstOperand().getFilteredLine())) {
                            statement.setError(ERRORS.get(6));
                        }
                        statement.getFirstOperand().setSymAddress(isLabel);
                    }

                    if (statement.getFirstOperand().isImmediate()) {
                        boolean isLabel = false;
                        if (SYMTABLE.containsKey(statement.getFirstOperand().getFilteredLine())) {
                            isLabel = true;
                        }
                        if (!isLabel && !isValidNum(statement.getFirstOperand().getFilteredLine())) {
                            statement.setError(ERRORS.get(5));
                        }
                        statement.getFirstOperand().setSymAddress(isLabel);
                    }

                    if (statement.getFirstOperand().isSimple()) {
                        if (!SYMTABLE.containsKey(statement.getFirstOperand().getFilteredLine())) {
                            statement.setError(ERRORS.get(5));
                        }
                        statement.getFirstOperand().setSymAddress(true);
                    }
                }
            }
        }


        // printer
        for (Statement statement : statements) {
            if (statement.getError() != null) {
                list.write("\t" + statement.getError().getError() + "\n");
            }
            if (!statement.isComment()) {
                list.write(String.format("%06X\t%s\n", statement.getAddress(), statement.getLine()));
            } else {
                list.write(String.format("%s\n", statement.getLine()));
            }
        }

        if (!ended) {
            list.write(ERRORS.get(9).getError() + "\n");
        }

        list.write("\n\n-----------------------------------------------\n\n");
        list.write("\t\t\t\t SYMBOL TABLE \n\n");
        list.write("\t\t\tName\t\t\tValue\n");
        list.write("\t\t   -----------------------\n");

        for (String s : SYMTABLE.keySet()) {
            list.write(String.format("\t\t\t%s\t" + (s.length() < 4 ? "\t" : "") + "\t\t%06X\n", s, SYMTABLE.get(s)));
        }

        list.close();
    }

    private void secondPass() throws IOException {

        BufferedWriter object = new BufferedWriter(new FileWriter(objectFile));

        // if there is errors in statements skip pass 2
        for (Statement statement : statements) {
            if (statement.getError() != null) {
                object.write("\t\t\n\n\n *** Error in assembly cannot generate object file ***\n\n\n");
                object.close();
                return;
            }
        }


        // Remove comments
        statements.removeIf(Statement::isComment);
        statements.removeIf(statement -> statement.getMnemonic().isDirective() && statement.getInstruction() == null);

        boolean baseRelative = false;

        for (Statement statement : statements) {
            StringBuilder instruction = new StringBuilder();

            if (statement.getInstruction() != null) {
                continue;
            }

            // for register type instructions
            if (statement.getMnemonic().getSize() == 2 && statement.getMnemonic().isRegisterType()) {
                instruction.append(statement.getMnemonic().getOpcode());
                instruction.append(REGTABLE.get(statement.getFirstOperand().getLine()));
                if (statement.getMnemonic().isTwoOperands()) {
                    instruction.append(REGTABLE.get(statement.getSecondOperand().getLine()));
                } else {
                    instruction.append(0);
                }
                statement.setInstruction(instruction.toString());
                continue;
            }

            if (statement.getMnemonic().getSize() != 2) {

                int ni;
                if (statement.getFirstOperand().isIndirect()) {
                    ni = 2;
                } else if (statement.getFirstOperand().isImmediate()) {
                    ni = 1;
                } else {
                    assert statement.getFirstOperand().isSimple();
                    ni = 3;
                }

                // append the first char only of the opcode
                instruction.append(statement.getMnemonic().getOpcode().charAt(0));

                // add n and i to second half of the first byte
                int secondHalf = Integer.parseInt(String.valueOf(statement.getMnemonic().getOpcode().charAt(1)), 16);
                secondHalf += ni;

                instruction.append(String.format("%01X", secondHalf));

                // builder for the flags byte
                StringBuilder secondByte = new StringBuilder();

                // append indexed flag
                secondByte.append(statement.isIndexed() ? 1 : 0);

                // append base relative flag
                //noinspection ConstantConditions
                secondByte.append(baseRelative ? 1 : 0);

                // append pc relative flag
                secondByte.append(statement.getFirstOperand().isSymAddress() && !statement.isType4() ? 1 : 0);

                // append format 4 flag
                secondByte.append(statement.isType4() ? 1 : 0);

                // convert from binary to int
                int secondByteValue = Integer.parseInt(secondByte.toString(), 2);
                // convert from int to hex string then append to the instruction
                instruction.append(String.format("%01X", secondByteValue));

                int displacement;
                if (statement.isType4()) {

                    displacement = evaluateExpression(statement);
                    instruction.append(String.format("%05X", displacement));
                    statement.setInstruction(instruction.toString());
                    continue;
                }

                if (statement.getFirstOperand().isSymAddress()) {
                    int PC = statement.getAddress() + statement.getMnemonic().getSize();
                    int TA = evaluateExpression(statement);
                    displacement = TA - PC;
                    if (displacement < 0) {
                        String cutterDisp = String.format("%X", displacement);
                        cutterDisp = cutterDisp.substring(5);
                        assert cutterDisp.length() == 3;
                        instruction.append(cutterDisp);
                    } else {
                        instruction.append(String.format("%03X", displacement));
                    }
                    statement.setInstruction(instruction.toString());

                } else {
                    displacement = evaluateExpression(statement);
                    instruction.append(String.format("%03X", displacement));
                    statement.setInstruction(instruction.toString());
                }


            }


        }

        String headerRecord = "H";
        headerRecord += appendSpace(progName) + "^";
        headerRecord += String.format("%06X^", progStartAddr);
        headerRecord += String.format("%06X\n", progLength);
        object.write(headerRecord);

        int size = 0;
        StringBuilder record = new StringBuilder();
        boolean instructionsExist = false;
        for (Statement statement : statements) {
            if (statement.getInstruction() != null) {
                instructionsExist = true;
                break;
            }
        }

        if (instructionsExist) {
            object.write("T");
            object.write(String.format("%06X^", progStartAddr));
            for (Statement statement : statements) {
                if (size + (statement.getInstruction().length() / 2) <= 30 && !statement.getMnemonic().isDirective()) {
                    record.append(statement.getInstruction());
                    size += (statement.getInstruction().length() / 2);
                } else {
                    object.write(String.format("%02X^", size) + record.toString() + "\n");
                    size = 0;
                    record = new StringBuilder();
                    object.write("T" + String.format("%06X^", statement.getAddress()));
                    record.append(statement.getInstruction());
                    size += (statement.getInstruction().length() / 2);
                }
            }
            object.write(String.format("%02X^", size) + record.toString() + "\n");
        }


        object.write("E" + String.format("%06X", progStartAddr));

        object.close();
    }

    private static String appendSpace(String s) {
        StringBuilder progName = new StringBuilder(s);
        for (int i = 0; i < (6 - s.length()); i++) {
            progName.append(" ");
        }
        return progName.toString();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isHex(String s) {
        return s.matches("^[a-fA-F0-9]*$");
    }

    private int evaluateExpression(Statement statement) {
        int result;

        int firstOperand;
        int secondOperand;

        String[] tokens = statement.getFirstOperand().getFilteredLine2().split("[+\\-*/]");
        if (SYMTABLE.containsKey(tokens[0])) {
            firstOperand = SYMTABLE.get(tokens[0]);
        } else if (isNum(tokens[0])) {
            firstOperand = Integer.parseInt(tokens[0]);
        } else {
            statement.setError(ERRORS.get(5));
            return -1;
        }
        if (tokens.length > 1) {

            char op = statement.getFirstOperand().getFilteredLine2().charAt(tokens[0].length());

            if (SYMTABLE.containsKey(tokens[1])) {
                secondOperand = SYMTABLE.get(tokens[1]);
            } else if (isNum(tokens[1])) {
                secondOperand = Integer.parseInt(tokens[1]);
            } else {
                statement.setError(ERRORS.get(5));
                return -1;
            }

            switch (op) {
                case '+':
                    result = firstOperand + secondOperand;
                    break;

                case '-':
                    result = firstOperand - secondOperand;
                    break;

                case '*':
                    result = firstOperand * secondOperand;
                    break;

                case '/':
                    result = firstOperand / secondOperand;
                    break;

                default:
                    statement.setError(ERRORS.get(5));
                    return -1;
            }
        } else {
            result = firstOperand;
        }
        return result;
    }

    private boolean isValidNum(String s) {
        return s.matches("^[0-9]*$") && Integer.parseInt(s) < 4095;
    }

    private boolean isNum(String s) {
        return s.matches("^[0-9]*$");
    }
}
