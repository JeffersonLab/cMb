package factory;

/**
 * <font size = 1 >JSA: Thomas Jefferson National Accelerator Facility<br>
 * This software was developed under a United States Government license,<br>
 * described in the NOTICE file included as part of this distribution.<br>
 * Copyright (c), Nov 3, 2010 <br></font>
 * </p>
 *
 * @author Vardan Gyurjyan
 * @version 1.3
 */

public class cMbTableColumn {
    private String name;
    private int width;
    private int index;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
