package fields;

import java.io.IOException;

import io.Reader;
import io.Writer;

/** Field for a byte primitive */
public class ByteField extends Field {
	byte fieldValue;
	
	public ByteField(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Object getField() {
		return (Byte) fieldValue;
	}
	
	public void setField(byte b) {
		fieldValue = b;
	}
	
	@Override
	public void read() throws IOException {
		fieldValue = Reader.dis.readByte();
	}

	@Override
	public void write() throws IOException {
		Writer.dos.writeByte(fieldValue);
	}
	
	@Override
	public ByteField clone() {
		return new ByteField(fieldName);
	}
}
