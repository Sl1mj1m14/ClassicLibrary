package fields;

import java.io.IOException;

import io.Reader;
import io.Writer;

/** Field for a float primitive */
public class FloatField extends Field {
	float fieldValue;
	
	public FloatField(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public Object getField() {
		return (Float) fieldValue;
	}
	
	@Override
	public void read() throws IOException {
		fieldValue = Reader.dis.readFloat();
	}

	@Override
	public void write() throws IOException {
		Writer.dos.writeFloat(fieldValue);
	}
	
	@Override
	public FloatField clone() {
		return new FloatField(fieldName);
	}
}
