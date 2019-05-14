public class Mnemonic {

    private String mnemonic;
    private boolean registerType;
    private boolean twoOperands;
    private boolean directive = false;

    private int size;
    private String opcode;

    public Mnemonic(String mnemonic, boolean registerType, int size, String opcode, boolean twoOperands) {
        this.mnemonic = mnemonic;
        this.registerType = registerType;
        this.size = size;
        this.opcode = opcode;
        this.twoOperands = twoOperands;
    }

    public Mnemonic(String directive) {
        this.mnemonic = directive;
        this.directive = true;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public boolean isRegisterType() {
        return registerType;
    }

    public void setRegisterType(boolean registerType) {
        this.registerType = registerType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public boolean isTwoOperands() {
        return twoOperands;
    }

    public void setTwoOperands(boolean twoOperands) {
        this.twoOperands = twoOperands;
    }

    public boolean isDirective() {
        return directive;
    }

}
