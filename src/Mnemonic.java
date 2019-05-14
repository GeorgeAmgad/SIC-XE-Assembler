class Mnemonic {

    private String mnemonic;
    private boolean registerType;
    private boolean twoOperands;
    private boolean directive = false;

    private int size;
    private String opcode;

    Mnemonic(String mnemonic, boolean registerType, int size, String opcode, boolean twoOperands) {
        this.mnemonic = mnemonic;
        this.registerType = registerType;
        this.size = size;
        this.opcode = opcode;
        this.twoOperands = twoOperands;
    }

    Mnemonic(String directive) {
        this.mnemonic = directive;
        this.directive = true;
    }

    String getMnemonic() {
        return mnemonic;
    }

    boolean isRegisterType() {
        return registerType;
    }

    int getSize() {
        return size;
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
