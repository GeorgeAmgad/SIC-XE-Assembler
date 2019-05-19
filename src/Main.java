

public class Main {

    public static void main(String[] args) {
        Assembler assembler = new Assembler("source.txt", "listFile", "objectFile");
        assembler.assemble();

        StringBuilder instruction = new StringBuilder();
        instruction.append(Integer.toBinaryString(Integer.parseInt("00", 16) ));
        int secondByte = Integer.parseInt(String.valueOf('a'), 16) + 2;
        System.out.println(secondByte);

        System.out.println(String.format("%03X", -12));
    }
}
