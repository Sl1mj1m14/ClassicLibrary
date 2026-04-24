package fields;

import java.io.IOException;

import io.Reader;
import io.Writer;

/** Field for a long primitive */
public class LongField extends Field {
	long fieldValue;
	
	public LongField(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Object getField() {
		return (Long) fieldValue;
	}

	@Override
	public void read() throws IOException {
		fieldValue = Reader.dis.readLong();
	}

	@Override
	public void write() throws IOException {
		Writer.dos.writeLong(fieldValue);
	}

	@Override
	public LongField clone() {
		return new LongField(fieldName);
	}
}
