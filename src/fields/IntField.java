package fields;

import java.io.IOException;

import io.Reader;
import io.Writer;

/** Field for an int primitive */
public class IntField extends Field {
	int fieldValue;
	
	public IntField(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Object getField() {
		return (Integer) fieldValue;
	}
	
	@Override
	public void read() throws IOException {
		fieldValue = Reader.dis.readInt();
	}

	@Override
	public void write() throws IOException {
		Writer.dos.writeInt(fieldValue);
	}

	@Override
	public IntField clone() {
		return new IntField(fieldName);
	}
}
