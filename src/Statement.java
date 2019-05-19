
@SuppressWarnings("unused")
class Statement {

    private static final String COMMENT_INDICATOR = ".";

    public static final int A = 0;
    public static final int X = 1;
    public static final int L = 2;
    public static final int B = 3;
    public static final int S = 4;
    public static final int T = 5;
    public static final int F = 6;

    public static final int PC = 8;
    public static final int SW = 9;

    private Error error;
    private String label;
    private Mnemonic mnemonic;
    private int address;
    private Operand firstOperand;
    private Operand secondOperand;

    private String instruction;

    private boolean type4 = false;

    private boolean indexed = false;

    private String line;

    Statement(String line) {
        this.line = line;

        // Split the line using whitespaces and tabs as deliminators.
        // Also attempt to extract the second operand with a comma (,).
        String[] tokens;
        if (!isComment()) {
            tokens = line.split("\\s+|,");

            if (tokens[0].startsWith("+")) {
                type4 = true;
                tokens[0] = tokens[0].substring(1);
            }
            if (tokens.length > 1) {
                if (tokens[1].startsWith("+")) {
                    type4 = true;
                    tokens[1] = tokens[1].substring(1);

                }
            }

            boolean foundOp = false;
            for (int i = 0; i < Tables.getOperationTable().size(); i++) {
                if (tokens[0].equalsIgnoreCase(Tables.getOperationTable().get(i).getString())) {
                    mnemonic = Tables.getOperationTable().get(i);

                    foundOp = true;
                    if (tokens.length > 1) {
                        firstOperand = new Operand(tokens[1]);
                    }
                    if (tokens.length > 2) {
                        secondOperand = new Operand(tokens[2]);
                    }
                    break;
                }
            }

            if (!foundOp) {
                label = tokens[0];
            }
            boolean badLabel = false;

            for (int i = 0; i < Tables.getOperationTable().size(); i++) {
                if (tokens[1].equalsIgnoreCase(Tables.getOperationTable().get(i).getString())) {
                    mnemonic = Tables.getOperationTable().get(i);
                    if (foundOp) {
                        badLabel = true;
                    }
                    foundOp = true;
                    if (tokens.length > 2) {
                        firstOperand = new Operand(tokens[2]);
                    }
                    if (tokens.length > 3) {
                        secondOperand = new Operand(tokens[3]);
                    }
                    break;
                }
            }

            if (!foundOp) {
                error = Tables.getErrorsTable().get(4);
                return;
            }

            if (badLabel) {
                error = Tables.getErrorsTable().get(14);
            }

            if (type4 && mnemonic.getSize() != 3) {
                error = Tables.getErrorsTable().get(7);
            }

            if (mnemonic.isRegisterType()) {
                if (hasFirstOperand()) {
                    if (!isRegister(firstOperand.getLine())) {
                        error = Tables.getErrorsTable().get(8);
                    }
                }

                if (hasSecondOperand()) {
                    if (!isRegister(secondOperand.getLine())) {
                        error = Tables.getErrorsTable().get(8);
                    }
                }

                if (mnemonic.isTwoOperands() && !hasSecondOperand()) {
                    error = Tables.getErrorsTable().get(16);
                }
            }

            if (!mnemonic.isRegisterType() && !mnemonic.isDirective()) {
                if (hasSecondOperand()) {
                    if (secondOperand.getLine().equalsIgnoreCase("x")) {
                        indexed = true;
                    }
                }
            }

            if (hasFirstOperand()) {
                if (indexed && (mnemonic.isDirective() || firstOperand.isImmediate() || firstOperand.isIndirect())) {
                    error = Tables.getErrorsTable().get(15);
                }
            }

            if (type4) {
                mnemonic.setSize(4);
            }

        }



    }

    boolean hasLabel() {
        return !label.equals("");
    }

    boolean hasFirstOperand() {
        return firstOperand != null;
    }

    private boolean hasSecondOperand() {
        return secondOperand != null;
    }

    boolean isComment() {
        return line.startsWith(COMMENT_INDICATOR);
    }

    Error getError() {
        return error;
    }

    void setError(Error error) {
        this.error = error;
    }

    String getLabel() {
        return label;
    }

    Mnemonic getMnemonic() {
        return mnemonic;
    }

    int getAddress() {
        return address;
    }

    void setAddress(int address) {
        this.address = address;
    }

    Operand getFirstOperand() {
        return firstOperand;
    }

    Operand getSecondOperand() {
        return secondOperand;
    }

    String getLine() {
        return line;
    }

    boolean isType4() {
        return type4;
    }

    boolean isIndexed() {
        return indexed;
    }

    String getInstruction() {
        return instruction;
    }

    void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isRegister(String register) {
        switch (register) {
            case "a":
            case "A":
            case "X":
            case "x":
            case "T":
            case "t":
            case "S":
            case "s":
            case "F":
            case "f":
            case "b":
            case "B":
            case "l":
            case "L":
            case "pc":
            case "PC":
            case "SW":
            case "sw":
                return true;
            default:
                return false;
        }
    }
}
