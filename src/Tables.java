import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class Tables {

    private static final String OPTABLE_PATH = "tables/operationTable.txt";
    private static final String ERRORS_PATH = "tables/errors";
    private static final String DIRECT_PATH = "tables/directives.txt";

    private static final HashMap<String, Integer> SYMTABLE;
    private static final List<OpCode> OPTABLE;
    private static final List<Error> ERRORS_TABLE;

    static {
        SYMTABLE = new HashMap<>();
        OPTABLE = new ArrayList<>();
        ERRORS_TABLE = new ArrayList<>();
        setDirectivesTable(new File(DIRECT_PATH));
        setOperationTable(new File(OPTABLE_PATH));
        setErrorsTable(new File(ERRORS_PATH));
    }

    private static void setDirectivesTable(File file) {
        try {
            BufferedReader opTable = new BufferedReader(new FileReader(file));
            String line;

            while ((line = opTable.readLine()) != null) {
                OPTABLE.add(new OpCode(line));
            }
        } catch (IOException ignored) {
        }

    }

    private Tables() {
        // Prevents instantiation.
    }


    static HashMap<String, Integer> getSymbolTable() {
        return SYMTABLE;
    }

    static List<OpCode> getOperationTable() {
        return OPTABLE;
    }

    static List<Error> getErrorsTable() {
        return ERRORS_TABLE;
    }

    private static void setOperationTable(File table) {
        try {
            BufferedReader opTable = new BufferedReader(new FileReader(table));
            String line;
            String[] lineTokens;

            while ((line = opTable.readLine()) != null) {
                lineTokens = line.split("\\s+");
                boolean regType = true;
                boolean twoOperands = false;
                if (lineTokens[1].equals("m")) {
                    regType = false;
                } else {
                    String[] operands = lineTokens[1].split(",");
                    if (operands.length > 1) {
                        twoOperands = true;
                    }
                }
                int size;
                if (lineTokens[2].equals("2")) {
                    size = 2;
                } else {
                    size = 3;
                }
                OPTABLE.add(new OpCode(lineTokens[0], regType, size, lineTokens[3], twoOperands));
            }
        } catch (IOException ignored) {
        }
    }

    private static void setErrorsTable(File table) {
        try {
            BufferedReader opTable = new BufferedReader(new FileReader(table));
            String line;

            while ((line = opTable.readLine()) != null) {
                ERRORS_TABLE.add(new Error(line));
            }
        } catch (IOException ignored) {
        }

    }
}
