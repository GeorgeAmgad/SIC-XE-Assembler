class Operand {

    private boolean indirect;
    private boolean immediate;
    private boolean simple;
    private boolean SymAddress;

    private String line;
    private String filteredLine;
    private String filteredLine2;

    Operand(String line) {
        this.line = line;
        indirect = line.startsWith("@");
        immediate = line.startsWith("#");
        boolean expression = line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/");

        simple = !indirect && !immediate;

        if (indirect) {
            filteredLine = line.split("@")[1];
            filteredLine2 = line.split("@")[1];
        } else if (immediate) {
            filteredLine = line.split("#")[1];
            filteredLine2 = line.split("#")[1];
        } else {
            filteredLine = line;
            filteredLine2 = line;
        }

        if (expression) {
            filteredLine2 = filteredLine;
            filteredLine = filteredLine.split("[+\\-*/]")[0];
        }

    }

    boolean isIndirect() {
        return indirect;
    }

    boolean isImmediate() {
        return immediate;
    }

    boolean isSimple() {
        return simple;
    }

    boolean isSymAddress() {
        return SymAddress;
    }

    void setSymAddress(boolean symAddress) {
        this.SymAddress = symAddress;
    }

    String getLine() {
        return line;
    }

    String getFilteredLine() {
        return filteredLine;
    }



    String getFilteredLine2() {
        return filteredLine2;
    }
}
