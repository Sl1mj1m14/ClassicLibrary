package fields;

import java.io.IOException;

import io.Reader;
import io.Writer;

/** Field for a short primitive */
public class ShortField extends Field {
	short fieldValue;
	
	public ShortField(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Object getField() {
		return (Short) fieldValue;
	}
	
	@Override
	public void read() throws IOException {
		fieldValue = Reader.dis.readShort();
	}

	@Override
	public void write() throws IOException {
		Writer.dos.writeShort(fieldValue);
	}

	@Override
	public ShortField clone() {
		return new ShortField(fieldName);
	}
}
