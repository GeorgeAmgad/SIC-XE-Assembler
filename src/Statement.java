class Statement {

    private static final String COMMENT_INDICATOR = ".";

    private Error error;
    private String label;
    private OpCode opCode;
    private int address;
    private String firstOperand;
    private String secondOperand;

    private String line;

    Statement(String line) {
        this.line = line;

        // Split the line using whitespaces and tabs as deliminators.
        // Also attempt to extract the second operand with a comma (,).
        String[] tokens;
        if (!isComment()) {
            tokens = line.split("\\s+|,");
            boolean type4 = false;
            String[] opCodeTokens = tokens[0].split("[()]");
            if (opCodeTokens[0].equals("+")) {
                type4 = true;
                tokens[0] = opCodeTokens[1];
            }
            if (tokens.length > 1) {
                opCodeTokens = tokens[1].split("[()]");
                if (opCodeTokens[0].equals("+")) {
                    type4 = true;
                    tokens[1] = opCodeTokens[1];
                }
            }

            boolean foundOp = false;
            for (int i = 0; i < Tables.getOperationTable().size(); i++) {
                if (tokens[0].equalsIgnoreCase(Tables.getOperationTable().get(i).getMnemonic())) {
                    opCode = Tables.getOperationTable().get(i);

                    foundOp = true;
                    if (tokens.length > 1) {
                        firstOperand = tokens[1];
                    }
                    if (tokens.length > 2) {
                        secondOperand = tokens[2];
                    }
                    break;
                }
            }

            if (!foundOp) {
                label = tokens[0];
            }
            boolean badLabel = false;

            for (int i = 0; i < Tables.getOperationTable().size(); i++) {
                if (tokens[1].equalsIgnoreCase(Tables.getOperationTable().get(i).getMnemonic())) {
                    opCode = Tables.getOperationTable().get(i);
                    if (foundOp) {
                        badLabel = true;
                    }
                    foundOp = true;
                    if (tokens.length > 2) {
                        firstOperand = tokens[2];
                    }
                    if (tokens.length > 3) {
                        secondOperand = tokens[3];
                    }
                    break;
                }
            }
            if (!foundOp) {
                error = Tables.getErrorsTable().get(4);
            }

            if (badLabel) {
                error = Tables.getErrorsTable().get(14);
            }

            if (foundOp) {
                if (type4 && opCode.getSize() != 3 ) {
                    error = Tables.getErrorsTable().get(7);
                }
            }


        }

    }

    boolean hasLabel() {
        return !label.equals("");
    }

    boolean hasFirstOperand() {
        return firstOperand != null;
    }

    boolean hasSecondOperand() {
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

    OpCode getOpCode() {
        return opCode;
    }

    int getAddress() {
        return address;
    }

    void setAddress(int address) {
        this.address = address;
    }

    String getFirstOperand() {
        return firstOperand;
    }

    String getSecondOperand() {
        return secondOperand;
    }

    String getLine() {
        return line;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isRegister(String register) {
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
                return true;
            default:
                return false;
        }
    }
}
