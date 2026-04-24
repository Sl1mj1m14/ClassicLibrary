package io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import fields.BooleanField;
import fields.ByteField;
import fields.CharField;
import fields.Class;
import fields.DoubleField;
import fields.Field;
import fields.FloatField;
import fields.IntField;
import fields.LongField;
import fields.ShortField;

//TODO add java doc
public class Writer {
    /** The data input stream from the chosen file. */
    public static DataOutputStream dos;

    //TODO add java doc
	public static ArrayList<Class> handles;

    //TODO add java doc
    public static byte[] write(Class writeClass) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);

        //Initialize handles array (clear from previous run)
		handles = new ArrayList<Class>();

        writeStream(writeClass);

        byte[] result = baos.toByteArray();
        dos.close();

        return result;
    }

    /**
     * Writes the stream variable from the grammar for the provided class
     */
    private static void writeStream(Class writeClass) throws IOException {
        writeMagic();
        writeVersion();
        writeContent(writeClass); // For Java Classic file, every contents variable has only one content that describes object
    }

    /**
     * Writes the magic variable from the grammar (hard set magic number)
     */
    private static void writeMagic() throws IOException {
        dos.writeShort(Grammar.STREAM_MAGIC);
    }

    /**
     * Writes the version variable from the grammar, always the same
     */
    private static void writeVersion() throws IOException {
        dos.writeShort(Grammar.STREAM_VERSION);
    }

    /**
     * Writes the content variable from the grammar, in Minecraft Classic this only describes an object class
     */
    private static void writeContent(Class writeClass) throws IOException {
        writeObject(writeClass);
    }

    private static void writeObject(Class writeClass) throws IOException {
        //TODO: only handling newClass case currently, add other cases
        writeNewObject(writeClass);
    }

    //TODO: Javadoc
    private static void writeNewObject(Class writeClass) throws IOException {
        dos.writeByte(Grammar.TC_OBJECT);
        writeClassDesc(writeClass); // write class description
        handles.add(writeClass); // add class to handles list in case referenced later
        writeClass.write(); // write data inside class fields
    }

    //TODO: Javadoc
    private static void writeClassDesc(Class writeClass) throws IOException {
        //TODO: only handling newClassDesc and null case currently, add prevObject case
        if(writeClass != null) {
            writeNewClassDesc(writeClass);
        } else {
            dos.writeByte(Grammar.TC_NULL);
        }
    }

    //TODO: Javadoc
    private static void writeNewClassDesc(Class writeClass) throws IOException {
        dos.writeByte(Grammar.TC_CLASSDESC); // TC_CLASSDESC
        dos.writeUTF(writeClass.getName()); // className
        dos.writeLong(writeClass.getSerialVersionUID()); // serialVersionUID
        handles.add(writeClass); // add class description to handles in case it's needed later
        writeClassDescInfo(writeClass);
    }

    //TODO: Javadoc
    private static void writeClassDescInfo(Class writeClass) throws IOException {
        writeClassDescFlags(writeClass);
        writeFields(writeClass);
        dos.writeByte(Grammar.TC_ENDBLOCKDATA); // classAnnotation > endBlockData
        writeClassDesc(writeClass.getSuperClass()); // write super class class desc (just null for no super class)
    }

    //TODO: Javadoc
    private static void writeClassDescFlags(Class writeClass) throws IOException {
        dos.writeByte(Grammar.SC_SERIALIZABLE); //TODO: figure out when to add SC_WRITE_METHOD to flags?
    }

    //TODO: Javadoc
    private static void writeFields(Class writeClass) throws IOException {
        ArrayList<Field> classFields = writeClass.getFields();

        // write count of fields
        dos.writeShort((short) classFields.size());

        // write each field
        for(Field f : classFields) {
            writeFieldDesc(f);
        }
    }

    private static void writeFieldDesc(Field f) throws IOException {
        // prim_typecode (use writeByte instead of writeChar since writeChar creates two bytes)
        if(f instanceof ByteField) {
            dos.writeByte('B');
        } else if(f instanceof CharField) {
            dos.writeByte('C');
        } else if(f instanceof DoubleField) {
            dos.writeByte('D');
        } else if(f instanceof FloatField) {
            dos.writeByte('F');
        } else if(f instanceof IntField) {
            dos.writeByte('I');
        } else if(f instanceof LongField) {
            dos.writeByte('J');
        } else if(f instanceof ShortField) {
            dos.writeByte('S');
        } else if(f instanceof BooleanField) {
            dos.writeByte('Z');
        } //TODO Array Field and Class Field

        // fieldName
        dos.writeUTF(f.getFieldName());
    }
}
