class Mnemonic {

    private String string;
    private boolean registerType;
    private boolean twoOperands;
    private boolean directive = false;

    private int size;
    private String opcode;

    Mnemonic(String string, boolean registerType, int size, String opcode, boolean twoOperands) {
        this.string = string;
        this.registerType = registerType;
        this.size = size;
        this.opcode = opcode;
        this.twoOperands = twoOperands;
    }

    Mnemonic(String directive) {
        this.string = directive;
        this.directive = true;
    }

    String getString() {
        return string;
    }

    boolean isRegisterType() {
        return registerType;
    }

    int getSize() {
        return size;
    }

    void setSize(int size) {
        this.size = size;
    }

    boolean isTwoOperands() {
        return twoOperands;
    }

    boolean isDirective() {
        return directive;
    }

     String getOpcode() {
        return opcode;
    }
}
