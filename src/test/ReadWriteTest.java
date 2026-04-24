package test;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import fields.Class;

import io.Reader;
import io.Writer;

public class ReadWriteTest {
    private static final String testDir = "test_writes";

    public static void main(String [] args) throws Exception  {
        Path path = Paths.get(testDir);

        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }

        testReadWrite(Simple.class);
        testReadWrite(Subclass.class);
        testReadWrite(Superclass.class);
    }

    private static void testReadWrite(java.lang.Class<?> c) throws Exception {
        String className = c.getName();
        String serializableFileName = testDir + "/" + className + ".java_serializer.dat";
        Object obj = c.getDeclaredConstructor().newInstance();

        // Write out file using Java's serializer
        FileOutputStream fileOut = new FileOutputStream(serializableFileName);
        ObjectOutputStream createOut = new ObjectOutputStream(fileOut);
        createOut.writeObject(obj);
        createOut.close();

        // Read in serialized file
        Path path = Paths.get(serializableFileName);
        byte[] readBytes = Files.readAllBytes(path);
        Class readClass = Reader.read(readBytes);

        // Write out file using our writer
        byte[] writeBytes = Writer.write(readClass);
        Path outPath = Paths.get(testDir, className + ".classic_library_serializer.dat");
        Files.write(outPath, writeBytes);

        if(Arrays.equals(readBytes, writeBytes)) {
            System.out.println("Successful write match for class " + className);
        } else {
            System.out.println("[FAILED] Written files did not match for class " + className);
        }
    }
}
