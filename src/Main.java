

public class Main {

    public static void main(String[] args) {
        Assembler assembler = new Assembler("source.txt", "listFile", "objectFile");
        assembler.assemble();

    }
}
