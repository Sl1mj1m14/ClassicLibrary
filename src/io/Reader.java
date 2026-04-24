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
 * indicate it was a Minecraft file. 
 * 
 * This class contains methods to read the different components of the grammar used
 * by the serializer. The grammar is documented at the following website:
 * https://docs.oracle.com/javase/6/docs/platform/serialization/spec/protocol.html
 * 
 * @author bluecrab2
 * @author sl1mj1m
 */
public class Reader {
	/** The data input stream from the chosen file. */
	public static DataInputStream dis;
	
	/** List of all classes that are saved with (newHandle) in
	 * the grammar. They can be referenced later and read by
	 * {@link Grammar#readPrevObject()}. The counting in the file
	 * begins at {@link #baseWireHandle} and so that must be
	 * subtracted to get the position in the list.*/
	public static ArrayList<Class> handles;
	
	/**
	 * Reads the file at the given path then returns the Class that was
	 * serialized.
	 * 
	 * @throws IllegalArgumentException if file is not a classic file or 
	 * contains an error
	 */
	
	private static void read(byte[] stream) throws IOException {

		ByteArrayInputStream bin = new ByteArrayInputStream(stream);
        dis = new DataInputStream(bin);

		//Ensure first two bytes are magic number 0xACED
		int magic = dis.readShort();
		if(magic != Grammar.STREAM_MAGIC) {
			throw new IllegalArgumentException("Invalid starting magic bytes");
		}
		
		//Check version number is 5
		int version = dis.readUnsignedShort();
		if(version != Grammar.STREAM_VERSION) {
			throw new IllegalArgumentException("Invalid version number");
		}
		
		//Read the main class
		Class readClass = readContent();

		//Ensure no more bytes remain
		if(dis.read() != -1) {
			throw new IllegalArgumentException("Excess bytes inside file");
		}
		
		//Close the streams to prevent resource leak
		dis.close();
		
		System.out.println("This is a test message, assuming everything worked correctly...");
		//return readClass;
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
		int cDeterminer = dis.readUnsignedByte();
		
		if(cDeterminer == Grammar.TC_OBJECT) {
			return readNewObject();
		} else if(cDeterminer == Grammar.TC_REFERENCE) {
			return readPrevObject();
		} else if(cDeterminer == Grammar.TC_STRING || cDeterminer == Grammar.TC_LONGSTRING) { 
			return readNewString();
		} else if(cDeterminer == Grammar.TC_NULL) {
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
		int reference = dis.readInt();
		Class prototype = handles.get(reference - Grammar.baseWireHandle);
		return prototype.clone();
	}
	
	/**
	 * Read the "newString" variable from grammar and returns a class
	 * with the name set to the string.
	 */
	private static Class readNewString() throws IOException {
		//Set class name to the string
		Class returnClass = new Class(dis.readUTF());
		
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
		int oDeterminer = dis.readUnsignedByte();
		
		if(oDeterminer == Grammar.TC_CLASSDESC) {
			return readNewClassDesc();
		} else if(oDeterminer == Grammar.TC_REFERENCE) {
			return readPrevObject();
		} else if(oDeterminer == Grammar.TC_NULL) {
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
		String className = dis.readUTF();
		returnClass = new Class(className);
		
		//Read serialVersionUID
		long serialVersionUID = dis.readLong();
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
		int classDescFlags = dis.readUnsignedByte();
		if(classDescFlags != Grammar.SC_SERIALIZABLE && 
				classDescFlags != (Grammar.SC_SERIALIZABLE | Grammar.SC_WRITE_METHOD)) {
			throw new IllegalArgumentException("Illegal classDescFlags");
		}
		
		//Read all the class' fields
		returnClass = readFields(returnClass);
		
		//Ensure block data ends
		byte endBlockData = dis.readByte();
		if(endBlockData != Grammar.TC_ENDBLOCKDATA) {
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
		short numFields = dis.readShort();
		
		//Read each field
		for(int i = 0; i < numFields; i++) {
			Field f = readFieldDesc();
			inputClass.addField(f);
		}
		
		return inputClass;
	}
	
	public static Field readFieldDesc() throws IOException {
		//Get type of field
		char type = (char) dis.readByte();
		
		//Get name of field
		String fieldName = dis.readUTF();
		
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
