package io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import fields.ArrayField;
import fields.BooleanField;
import fields.ByteField;
import fields.CharField;
import fields.Class;
import fields.ClassField;
import fields.DoubleField;
import fields.Field;
import fields.FloatField;
import fields.IntField;
import fields.LongField;
import fields.ShortField;

/**
 * The main reader class for the Minecraft Classic file. It works to deserialize 
 * any class that was serialized using the java.io.Serializable interface. Notch
 * used that interface on the Level class for the save file with the only change
 * being that he added a few bytes of magic numbers at the start of the file to 
 * indicate it was a Minecraft file. A generic deserializer had to be written to
 * enable for this program to work on all Classic versions, even for versions that
 * are no longer archived.
 * 
 * This class contains methods to read the different components of the grammar used
 * by the serializer. The grammar is documented at the following website:
 * https://docs.oracle.com/javase/6/docs/platform/serialization/spec/protocol.html
 * 
 * Contact info:
 * Discord - bluecrab2#1996
 * Email - bluecrab2mc@gmail.com
 * 
 * @author bluecrab2
 */
public class Reader {
	/** The data input stream from the chosen file. */
	public static DataInputStream din;
	
	/** List of all classes that are saved with (newHandle) in
	 * the grammar. They can be referenced later and read by
	 * {@link #readPrevObject()}. The counting in the file
	 * begins at {@link #baseWireHandle} and so that must be
	 * subtracted to get the position in the list.*/
	public static ArrayList<Class> handles;
	
	/**
	 * Important constants for the grammar provided by the
	 * documentation at:
	 * https://docs.oracle.com/javase/6/docs/platform/serialization/spec/protocol.html
	 */
	final static short STREAM_MAGIC = (short)0xaced;
    final static short STREAM_VERSION = 5;

    final static byte TC_NULL = 0x70;
    final static byte TC_REFERENCE = 0x71;
    final static byte TC_CLASSDESC = 0x72;
    final static byte TC_OBJECT = 0x73;
    final static byte TC_STRING = 0x74;
    final static byte TC_ARRAY = 0x75;
    final static byte TC_CLASS = 0x76;
    final static byte TC_BLOCKDATA = 0x77;
    final static byte TC_ENDBLOCKDATA = 0x78;
    final static byte TC_RESET = 0x79;
    final static byte TC_BLOCKDATALONG = 0x7A;
    final static byte TC_EXCEPTION = 0x7B;
    final static byte TC_LONGSTRING = 0x7C;
    final static byte TC_PROXYCLASSDESC = 0x7D;
    final static byte TC_ENUM = 0x7E;

    final static int baseWireHandle = 0x7E0000;

    final static byte SC_WRITE_METHOD = 0x01; //if SC_SERIALIZABLE
    final static byte SC_BLOCK_DATA = 0x08; //if SC_EXTERNALIZABLE
    final static byte SC_SERIALIZABLE = 0x02;
    final static byte SC_EXTERNALIZABLE = 0x04;
    final static byte SC_ENUM = 0x10;
	
	/**
	 * Reads the file at the given path then returns the Class that was
	 * serialized.
	 * 
	 * @throws IllegalArgumentException if file is not a classic file or 
	 * contains an error
	 */
	
	private static Class read(byte[] stream) throws IOException {

		ByteArrayInputStream bin = new ByteArrayInputStream(stream);
        din = new DataInputStream(bin);

		//Ensure first two bytes are magic number 0xACED
		int magic = din.readShort();
		if(magic != STREAM_MAGIC) {
			throw new IllegalArgumentException("Invalid starting magic bytes");
		}
		
		//Check version number is 5
		int version = din.readUnsignedShort();
		if(version != STREAM_VERSION) {
			throw new IllegalArgumentException("Invalid version number");
		}
		
		//Read the main class
		Class readClass = readContent();
		return readClass;
	}
	
	/**
	 * Read the "content" variable from grammar and returns the class
	 * contained in it.
	 */
	public static Class readContent() throws IOException {
		Class returnClass =  readObject();
		
		return returnClass;
	}

	/**
	 * Read the "object" variable from grammar and returns the class
	 * contained in it. Object, string, reference, block data, and
	 * null are all covered, others were not needed for classic file.
	 */
	public static Class readObject() throws IOException {
		//Content determiner dictates what type of content it is
		int cDeterminer = din.readUnsignedByte();
		
		if(cDeterminer == TC_OBJECT) {
			return readNewObject();
		} else if(cDeterminer == TC_REFERENCE) {
			return readPrevObject();
		} else if(cDeterminer == TC_STRING) { 
			return readNewString();
		} else if(cDeterminer == TC_NULL) {
			return null;
		} else {
			//There are other valid next bytes but they aren't used in classic files
			throw new IllegalArgumentException("Invalid cDeterminer, was: " + cDeterminer);
		}
	}
	
	/**
	 * Read the "newObject" variable from grammar and returns the class
	 * contained in it.
	 */
	private static Class readNewObject() throws IOException {
		Class returnClass = readClassDesc();
		
		//newHandle
		handles.add(returnClass);
		
		//Read contents into class (classdata)
		returnClass.read();
		
		return returnClass;
	}

	/**
	 * Read the "prevObject" variable from grammar and returns a clone
	 * of the referenced class in the handles list.
	 */
	private static Class readPrevObject() throws IOException {
		int reference = din.readInt();
		Class prototype = handles.get(reference - baseWireHandle);
		return prototype.clone();
	}
	
	/**
	 * Read the "newString" variable from grammar and returns a class
	 * with the name set to the string.
	 */
	private static Class readNewString() throws IOException {
		//Set class name to the string
		Class returnClass = new Class(din.readUTF());
		
		//newHandle
		handles.add(returnClass);
		
		return returnClass;
	}
	
	/**
	 * Read the "classDesc" variable from grammar and returns a class
	 * with the fields described (uninitialized).
	 */
	public static Class readClassDesc() throws IOException {
		//Object determiner dictates whether the object is a 
		//newClassDescription or reference
		int oDeterminer = din.readUnsignedByte();
		
		if(oDeterminer == TC_CLASSDESC) {
			return readNewClassDesc();
		} else if(oDeterminer == TC_REFERENCE) {
			return readPrevObject();
		} else if(oDeterminer == TC_NULL) {
			return null;
		} else {
			//Not covering proxy since it shouldn't be in classic file
			throw new IllegalArgumentException("Invalid object determiner");
		}
	}

	/**
	 * Read the "newClassDesc" variable from grammar and returns a class
	 * with the fields described (uninitialized).
	 */
	private static Class readNewClassDesc() throws IOException {
		Class returnClass;
		
		//Read class name
		String className = din.readUTF();
		returnClass = new Class(className);
		
		//Read serialVersionUID
		long serialVersionUID = din.readLong();
		returnClass.setSerialVersionUID(serialVersionUID);

		//Add the read class as the next handle
		handles.add(returnClass);
		
		//Read classDescInfo
		returnClass = readClassDescInfo(returnClass);
		
		return returnClass;
	}
	
	/**
	 * Read the "classDescInfo" variable from grammar and returns a class
	 * with the fields described (uninitialized).
	 */
	private static Class readClassDescInfo(Class returnClass) throws IOException {
		//classDescFlags (only two are expected in classic file)
		int classDescFlags = din.readUnsignedByte();
		if(classDescFlags != SC_SERIALIZABLE && 
				classDescFlags != (SC_SERIALIZABLE | SC_WRITE_METHOD)) {
			throw new IllegalArgumentException("Illegal classDescFlags");
		}
		
		//Read all the class' fields
		returnClass = readFields(returnClass);
		
		//Ensure block data ends
		byte endBlockData = din.readByte();
		if(endBlockData != TC_ENDBLOCKDATA) {
			throw new IllegalArgumentException("Missing block data end byte");
		}
		
		//Read super class
		returnClass.setSuperClass(readClassDesc());
				
		return returnClass;
	}
	
	/**
	 * Read the "readFields" variable from grammar and returns a class
	 * with the fields described (uninitialized).
	 */
	public static Class readFields(Class inputClass) throws IOException {
		//Read number of fields
		short numFields = din.readShort();
		
		//Read each field
		for(int i = 0; i < numFields; i++) {
			Field f = readFieldDesc();
			inputClass.addField(f);
		}
		
		return inputClass;
	}
	
	public static Field readFieldDesc() throws IOException {
		//Get type of field
		char type = (char) din.readByte();
		
		//Get name of field
		String fieldName = din.readUTF();
		
		//Return type of field described
		if('B' == type) {
			return new ByteField(fieldName);
		} else if('C' == type) {
			return new CharField(fieldName);
		} else if('D' == type) {
			return new DoubleField(fieldName);
		} else if('F' == type) {
			return new FloatField(fieldName);
		} else if('I' == type) {
			return new IntField(fieldName);
		} else if('J' == type) {
			return new LongField(fieldName);
		} else if('S' == type) {
			return new ShortField(fieldName);
		} else if('Z' == type) {
			return new BooleanField(fieldName);
		} else if('[' == type) {
			//Class representing the string name, the class name is set to the string
			Class stringName = readObject();
			
			return new ArrayField(fieldName, stringName.getName());
		} else if('L' == type) {
			//Class representing the string name, the class name is set to the string
			Class stringName = readObject();
			
			//Cut off first and last character
			//See table 4.2 of https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
			String className = stringName.getName();
			className = className.substring(1, className.length() - 1);
			
			return new ClassField(fieldName, className);
		} else {
			throw new IllegalArgumentException("Invalid field type");
		}
	}
}
