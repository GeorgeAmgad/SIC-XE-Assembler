import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Assembler {
    private static final HashMap<String, Integer> SYMTABLE;
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

            if (statement.getOpCode().getMnemonic().equalsIgnoreCase("START") && !started) {
                // Save #[OPERAND] as starting address, and #[LABEL] as program name.
                // parseInt converts a string of base radix (16) to an integer
                progStartAddr = Integer.parseInt(statement.getFirstOperand(), 16);
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

            if (statement.getOpCode().getMnemonic().equalsIgnoreCase("END") && !ended) {
                statement.setAddress(locctr);
                if (statement.hasFirstOperand()) {
                    if (!statement.getFirstOperand().equalsIgnoreCase(progName)) {
                        statement.setError(ERRORS.get(12));
                    }
                }
                statements.add(statement);
                progLength = locctr - progStartAddr;
                ended = true;
                break;
            }

            if (statement.hasLabel()) {
                // Search SYMTABLE for LABEL.
                if (SYMTABLE.containsKey(statement.getLabel())) {
                    statement.setError(ERRORS.get(0));
                } else {
                    // Insert (LABEL, LOCCTR) into SYMTAB.
                    SYMTABLE.put(statement.getLabel(), locctr);
                }
            }

            if (statement.getOpCode().isDirective()) {

                switch (statement.getOpCode().getMnemonic()) {
                    case "WORD":
                        growthSize = 3;
                        break;

                    case "RESW":
                        growthSize = Integer.parseInt(statement.getFirstOperand()) * 3;
                        break;

                    case "RESB":
                    case "ORG":
                        growthSize = Integer.parseInt(statement.getFirstOperand());
                        break;

                    case "BYTE":
                        // Find length of constant in bytes.
                        // Add length to LOCCTR.
                        String[] operand = statement.getFirstOperand().split("'");
                        String dataType = operand[0];
                        int length = operand[1].length();

                        switch (dataType) {
                            case "c":
                            case "C":
                                growthSize = length;
                                break;
                            case "x":
                            case "X":
                                if (length % 2 != 0) {
                                    length++;
                                }
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
                    default:
                        // Set error flag (invalid operation code).
                        statement.setError(ERRORS.get(4));
                }
                statement.setAddress(locctr);
                statements.add(statement);
                locctr += growthSize;
                continue;
            }

            growthSize = statement.getOpCode().getSize();
            statement.setAddress(locctr);
            statements.add(statement);

            locctr += growthSize;

        }

        // UNDEFINED label error handler
        for (Statement statement : statements) {
            if (!statement.isComment() && statement.getOpCode() != null) {
                if (!statement.getOpCode().isRegisterType() && !statement.getOpCode().isDirective() && statement.hasFirstOperand()) {
                    if (!statement.getFirstOperand().startsWith("0") && !statement.getFirstOperand().startsWith("#")
                    && !statement.getFirstOperand().startsWith("*") && !statement.getFirstOperand().startsWith("$") &&
                            !statement.getFirstOperand().startsWith("@")) {
                        if (!SYMTABLE.containsKey(statement.getFirstOperand())) {
                            statement.setError(ERRORS.get(5));
                        }
                    }
                }
            }
        }

        // illegal address for a register handler
        for (Statement statement : statements) {
            if (!statement.isComment() && statement.getOpCode() != null) {
                if (statement.getOpCode().isRegisterType() && !statement.getOpCode().isDirective() &&
                        statement.hasFirstOperand() && statement.hasSecondOperand()) {
                    if (!statement.isRegister(statement.getFirstOperand()) || !statement.isRegister(statement.getSecondOperand())) {
                        statement.setError(ERRORS.get(8));
                    }
                }
                if (statement.getOpCode().isRegisterType() && !statement.getOpCode().isDirective() &&
                        statement.hasFirstOperand()) {
                    if (!statement.isRegister(statement.getFirstOperand())) {
                        statement.setError(ERRORS.get(8));
                    }
                }
            }
        }

        for (Statement statement : statements) {
            if (!statement.isComment() && statement.getOpCode() != null) {
                if (!statement.getOpCode().isRegisterType() && !statement.getOpCode().isDirective() &&
                        statement.hasFirstOperand()) {
                    if (statement.getFirstOperand().startsWith("#0")) {
                        String[] spliter = statement.getFirstOperand().split("#");
                        if (!isHex(spliter[1])) {
                            statement.setError(ERRORS.get(6));
                        }
                    }
                    if (statement.hasSecondOperand()) {
                        if (statement.getSecondOperand().startsWith("#0")) {
                            String[] spliter = statement.getFirstOperand().split("#");
                            if (!isHex(spliter[1])) {
                                statement.setError(ERRORS.get(6));
                            }
                        }
                    }
                }
            }
        }

        for (Statement statement : statements) {
            if (!statement.isComment() && statement.getOpCode() != null) {
                if (!statement.getOpCode().isRegisterType() && !statement.getOpCode().isDirective() &&
                        statement.hasFirstOperand()) {
                    if (statement.getFirstOperand().startsWith("#") && !statement.getFirstOperand().startsWith("#0")) {
                        String[] spliter = statement.getFirstOperand().split("#");
                        if (!SYMTABLE.containsKey(spliter[1])) {
                            statement.setError(ERRORS.get(5));
                        }
                    }
                    if (statement.hasSecondOperand()) {
                        if (statement.getSecondOperand().startsWith("#")&& statement.getFirstOperand().startsWith("#0")) {
                            String[] spliter = statement.getFirstOperand().split("#");
                            if (!SYMTABLE.containsKey(spliter[1])) {
                                statement.setError(ERRORS.get(5));
                            }
                        }
                    }
                }
            }
        }


        for (Statement statement : statements) {
            if (!statement.isComment() && statement.getOpCode() != null) {
                if (!statement.getOpCode().isRegisterType() && !statement.getOpCode().isDirective() &&
                        statement.hasFirstOperand()) {
                    if (statement.getFirstOperand().startsWith("@")) {
                        String[] splintedOperand = statement.getFirstOperand().split("@");
                        if (!SYMTABLE.containsKey(splintedOperand[1])) {
                            statement.setError(ERRORS.get(5));
                        }
                    }
                    if (statement.hasSecondOperand()) {
                        if (statement.getSecondOperand().startsWith("@")) {
                            String[] splintedOperand = statement.getSecondOperand().split("@");
                            if (!SYMTABLE.containsKey(splintedOperand[1])) {
                                statement.setError(ERRORS.get(5));
                            }
                        }
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
                list.write(String.format("%06x\t%s\n", statement.getAddress(), statement.getLine()));
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
            list.write(String.format("\t\t\t%s\t" + (s.length() < 4 ? "\t" : "") + "\t\t%06x\n", s, SYMTABLE.get(s)));
        }

        list.close();
    }

    private  void secondPass() throws IOException {

        BufferedWriter object = new BufferedWriter(new FileWriter(objectFile));

        for (Statement statement : statements) {
            if (statement.getError() != null) {
                object.write("\t\t***Error in assembly cannot generate object file***");
                return;
            }
        }


    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isHex(String s) {
        return s.matches("^[a-fA-F0-9]*$");
    }
}
