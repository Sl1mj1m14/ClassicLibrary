package io;

public class Grammar {
    /**
	 * Important constants for the grammar provided by the
	 * documentation at:
	 * https://docs.oracle.com/javase/6/docs/platform/serialization/spec/protocol.html
	 */
	public final static short STREAM_MAGIC = (short)0xaced;
	public final static short STREAM_VERSION = 5;
	public final static byte TC_NULL = (byte)0x70;
	public final static byte TC_REFERENCE = (byte)0x71;
	public final static byte TC_CLASSDESC = (byte)0x72;
	public final static byte TC_OBJECT = (byte)0x73;
	public final static byte TC_STRING = (byte)0x74;
	public final static byte TC_ARRAY = (byte)0x75;
	public final static byte TC_CLASS = (byte)0x76;
	public final static byte TC_BLOCKDATA = (byte)0x77;
	public final static byte TC_ENDBLOCKDATA = (byte)0x78;
	public final static byte TC_RESET = (byte)0x79;
	public final static byte TC_BLOCKDATALONG = (byte)0x7A;
	public final static byte TC_EXCEPTION = (byte)0x7B;
	public final static byte TC_LONGSTRING = (byte) 0x7C;
	public final static byte TC_PROXYCLASSDESC = (byte) 0x7D;
	public final static byte TC_ENUM = (byte) 0x7E;
	public final static int  baseWireHandle = 0x7E0000;
	
	public final static byte SC_WRITE_METHOD = 0x01; //if SC_SERIALIZABLE
	public final static byte SC_BLOCK_DATA = 0x08;    //if SC_EXTERNALIZABLE
	public final static byte SC_SERIALIZABLE = 0x02;
	public final static byte SC_EXTERNALIZABLE = 0x04;
	public final static byte SC_ENUM = 0x10;
}
