package fields;

import java.io.IOException;

import io.Reader;
import io.Writer;

/** Field for a double primitive */
public class DoubleField extends Field {
	double fieldValue;
	
	public DoubleField(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Object getField() {
		return (Double) fieldValue;
	}
	
	@Override
	public void read() throws IOException {
		fieldValue = Reader.dis.readDouble();
	}

	@Override
	public void write() throws IOException {
		Writer.dos.writeDouble(fieldValue);
	}

	@Override
	public DoubleField clone() {
		return new DoubleField(fieldName);
	}
}
