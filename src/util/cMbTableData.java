package util;


/**
 * JSA
 * Thomas Jefferson National Accelerator Facility
 * *
 * This software was developed under a United States
 * Government license, described in the NOTICE file
 * included as part of this distribution.
 * *
 * Copyright (c)
 *
 * @author gurjyan
 */
public class cMbTableData {
    private String name;
    private String columnName;
    private int type;
    private Object[] values;
    private Object value;
    private Float[] floatArrayOfValues;
    private Float floatOfValue;
    private String[] stringArrayOfValues;
    private String stringOfValue;


    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Float[] getFloatArrayOfValues() {
        return floatArrayOfValues;
    }

    public void setFloatArrayOfValues(Float[] floatArrayOfValues) {
        this.floatArrayOfValues = floatArrayOfValues;
    }

    public Float getFloatOfValue() {
        return floatOfValue;
    }

    public void setFloatOfValue(Float floatOfValue) {
        this.floatOfValue = floatOfValue;
    }

    public String[] getStringArrayOfValues() {
        return stringArrayOfValues;
    }

    public void setStringArrayOfValues(String[] stringArrayOfValues) {
        this.stringArrayOfValues = stringArrayOfValues;
    }

    public String getStringOfValue() {
        return stringOfValue;
    }

    public void setStringOfValue(String stringOfValue) {
        this.stringOfValue = stringOfValue;
    }
}
