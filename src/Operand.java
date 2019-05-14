class Operand {

    private boolean indirect;
    private boolean immediate;
    private boolean simple;

    private String line;
    private String filteredLine;

    Operand(String line) {
        this.line = line;
        indirect = line.startsWith("@");
        immediate = line.startsWith("#");
        simple = !indirect && !immediate;

        if (indirect) {
            filteredLine = line.split("@")[1];
        }

        if (immediate) {
            filteredLine = line.split("#")[1];
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

    String getLine() {
        return line;
    }

    public String getFilteredLine() {
        return filteredLine;
    }
}
