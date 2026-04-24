package test;

import java.io.Serializable;

public class Simple implements Serializable {
    private static final long serialVersionUID = 314L;
    byte byteField = 1;
    char charField = 'b';
    double doubleField = 3.14;
    float floatField = 2.718f;
    int intField = 5;
    long longField = 6;
    short shortField = (short) 7;
    boolean booleanField = true;

    public Simple() {}
}
