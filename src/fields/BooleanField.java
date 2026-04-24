package fields;

import java.io.IOException;

import io.Reader;
import io.Writer;

/** Field for a boolean primitive */
public class BooleanField extends Field {
	boolean fieldValue;
	
	public BooleanField(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Object getField() {
		return (Boolean) fieldValue;
	}
	
	@Override
	public void read() throws IOException {
		fieldValue = Reader.dis.readBoolean();
	}

	@Override
	public void write() throws IOException {
		Writer.dos.writeBoolean(fieldValue);
	}
	
	@Override
	public BooleanField clone() {
		return new BooleanField(fieldName);
	}
}
