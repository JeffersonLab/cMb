package factory;

import util.cMbTableData;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

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
public class cMbTableFactory {
    private DefaultTableModel mmm;
    private JTable myTable;
    private ArrayList<TableColumn> tableColumns = new ArrayList<TableColumn>();

    /**
     * Creates and returns a JTable object
     * @param name of the table
     * @param al ArrayList of column names
     * @param useRendere boolean shows if we need special rendering of the table ( collored cell )
     * @return jtable object
     *
     */
    public JTable createTabe(String name,ArrayList<String> al, boolean useRendere,boolean reordering){
        myTable = new JTable();
        Object[][] oa = new Object[0] [al.size()];
        String[] sa = new String[al.size()];
        final Class[] types = new Class[al.size()];
        final boolean[] canEdit = new boolean[al.size()];
        int i=0;
        for(String s:al){
            sa[i] = s;
            types[i] = java.lang.String.class;
            canEdit[i] = false;
            i++;
        }
        mmm = new javax.swing.table.DefaultTableModel(oa,sa) {

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        };
        myTable.getTableHeader().setReorderingAllowed(reordering);
        myTable.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        myTable.setFont(Font.getFont("Luxi Sans-Bold-13"));
        myTable.setModel(mmm);
        myTable.setColumnSelectionAllowed(true);
        if(useRendere){
            TableCellRenderer renderer = new cMbTableCellRenderer();
            try
            {
                myTable.setDefaultRenderer( Class.forName( "java.lang.String"), renderer );
            }
            catch( ClassNotFoundException ex )
            {
                System.exit( 0 );
            }
            if(al.size()>1 && al.get(1).equalsIgnoreCase("text")){
                myTable.getColumnModel().getColumn(1).setPreferredWidth(400);
            }
            myTable.getTableHeader().setFont(new Font("Serif", Font.BOLD, 12));
        } else {
            myTable.setBackground(Color.WHITE);
            myTable.setFont(new Font("Serif", Font.PLAIN, 10));
            myTable.getTableHeader().setFont(new Font("Serif", Font.BOLD, 10));
        }
        myTable.getTableHeader().setBackground(Color.orange);
        myTable.getTableHeader().setForeground(new Color(0,77,77));
        myTable.setName(name);
        for(int j=0; j<myTable.getColumnCount();j++){
            tableColumns.add(myTable.getColumnModel().getColumn(j));
        }
        return myTable;
    }

    public JTable getTable(){
        return myTable;
    }

    public ArrayList<TableColumn> getTableColumns() {
        tableColumns.clear();
        for(int j=0; j<myTable.getColumnCount();j++){
            tableColumns.add(myTable.getColumnModel().getColumn(j));
        }
        return tableColumns;
    }

    public ArrayList<cMbTableColumn> getTableColumnStructures() {
        tableColumns.clear();
        for(int j=0; j<myTable.getColumnCount();j++){
            tableColumns.add(myTable.getColumnModel().getColumn(j));
        }
        ArrayList<cMbTableColumn> al = new ArrayList<cMbTableColumn>();
        for(int i=0; i<tableColumns.size();i++){
            TableColumn t = tableColumns.get(i);
            cMbTableColumn tcs = new cMbTableColumn();
            tcs.setName((String)t.getHeaderValue());
            tcs.setWidth(t.getWidth());
            tcs.setIndex(i);
            al.add(tcs);
        }
        return al;

    }

    public ArrayList<String> getClNames(){
        tableColumns.clear();
        for(int j=0; j<myTable.getColumnCount();j++){
            tableColumns.add(myTable.getColumnModel().getColumn(j));
        }
        ArrayList<String> al = new ArrayList<String>();
        for(TableColumn t:tableColumns){
            al.add((String)t.getHeaderValue());
        }
        return al;
    }

    public String getColumnName(int j){
        return (String)myTable.getColumnModel().getColumn(j).getHeaderValue();
    }

    public DefaultTableModel getTableModel(){
        return mmm;
    }

    public void setColumnWidth(int index, int width){
        TableColumn col = myTable.getColumnModel().getColumn(index);
        col.setPreferredWidth(width);
    }
    
    public void  removeColumn(String cName){
        TableColumn tt = null;
        for(TableColumn t:tableColumns){
            if(t.getHeaderValue().equals(cName)){
                myTable.removeColumn(t);
                tt = t;
                break;
            }
        }
        if(tt!=null){
            tableColumns.remove(tt);
        }
    }

    @Deprecated
    /**
     * Creates table with dalog browser specific column structure
     * @param name of the table
     * @return jtable object
     */
    public JTable createcMbTable(String name){
        ArrayList<String> al = new ArrayList<String>();
        al.add("Component");
        al.add("Message");
        al.add("State");
        al.add("Severity");
        al.add("Time");
        return createTabe(name,al,true,true);
    }

    @Deprecated
    public JTable createMGenericTable(){
        ArrayList<String> l = new ArrayList<String>();
        l.add("sender");
        l.add("text");
        l.add("subject");
        l.add("type");
        l.add("# pItems");
        return  createTabe("MessageTextBrowser",l,false,true);
    }

    /**
     * Ads data as a new row to the table
     * @param d data array
     */
    public void addData(String[] d){
        getTableModel().addRow(d);
        myTable.scrollRectToVisible(myTable.getCellRect(getTableModel().getRowCount()-1, 0, true));

    }

    /**
     * Creates table used to display subject = * and type = * messages
     * @return  JTable object
     */
    public JTable createMessageSpaceTable(){
        ArrayList<String> l = new ArrayList<String>();
        l.add("Subject");
        l.add("Type");
        l.add("Sender");
        l.add("Text");
        l.add("ByteArray");
        l.add("PayloadItem");
        return  createTabe("MessageSpaceTable",l,true,false);
    }

    /**
     * If the message space table already contains received subject and type update the row,
     * otherwise create and add the new row.
     * @param data received message data
     */
    public void updateMessageSpaceTable(String[] data){
        boolean found = false;
        for(int i=0; i<myTable.getRowCount();i++){
            if(myTable.getValueAt(i,0).equals(data[0]) && myTable.getValueAt(i,1).equals(data[1])){
                myTable.setValueAt(data[2],i,2);
                myTable.setValueAt(data[3],i,3);
                myTable.setValueAt(data[4],i,4);
                myTable.setValueAt(data[5],i,5);
                found = true;
                break;
            }
        }
        if(!found){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            addData(data);
        }

    }

    /**
     * Creates table used to display daLog Messages, i.e. subject = name of the component, type rc/report/dalog
     * @return  JTable object
     */
    public JTable createDalogTable(){
        ArrayList<String> l = new ArrayList<String>();
        l.add("Sender");
        l.add("CodaClas");
        l.add("Session");
        l.add("Config");
        l.add("State");
        l.add("Text");
        l.add("Severity");
        l.add("Date");
        return  createTabe("DalogMessageTable",l,true,false);
    }

    /**
     * Creates table used to display NOT daLog Messages
     * @param ctitles ArrayList of column titles
     * @return  JTable object
     */
    public JTable createOtherMsgTable(ArrayList<String> ctitles){
        return  createTabe("AnyMessageTable",ctitles,true,false);
    }

    /**
     * Update table structure by recreating it with passed column titles
     * @param ctitles ArrayList of column titles
     * @return  JTable object
     */
    public JTable updateTableStructure(ArrayList<String> ctitles){
        return  createTabe("",ctitles,true,false);
    }



    /**
     * Clears created table. N.B. Table default model is null if createTable is not called.
     */
    public void clearTable(){
        if(mmm!=null){
            while (mmm.getRowCount()>0){
                mmm.removeRow(0);
            }
        }
    }


    /**
     * Removes the first row/rows if the table row count is larger then required size
     */
    public void trimTableTop(int size){
        if(mmm!=null){
            while(mmm.getRowCount()>size){
                mmm.removeRow(0);
            }
        }
    }

    public void trim(JTable table,DefaultTableModel model, int length, int firstLast){
        if(table.getRowCount()>=length){
            switch (firstLast){
                case 0:
                    //Remove first row
                    model.removeRow(0);
                    break;
                case 1:
                    //Remove lase row
                    model.removeRow(table.getRowCount()-1);
                    break;
            }
        }
    }


    public HashMap<String,String> getSelectedSubjectType(){
        HashMap<String,String> mp = new HashMap<String,String>();
        // Individual cell selection is enabled
//            myTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
//            myTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Get the min and max ranges of selected cells
        int rowIndexStart = myTable.getSelectedRow();
        int rowIndexEnd = myTable.getSelectionModel().getMaxSelectionIndex();
        int colIndexStart = myTable.getSelectedColumn();
        int colIndexEnd = myTable.getColumnModel().getSelectionModel().getMaxSelectionIndex();

        // Check each cell in the range
        for (int r=rowIndexStart; r<=rowIndexEnd; r++) {
            for (int c=colIndexStart; c<=colIndexEnd; c++) {
                if (myTable.isCellSelected(r, c)) {
                    String k = (String)myTable.getColumnModel().getColumn(c).getHeaderValue();
                    String v = (String)myTable.getValueAt(r,c);
                    if(k.equals("subject") || k.equals("type")){
                        mp.put(k,v.trim());
                        System.out.println(" ... selected "+v.trim());
                    }
                }
            }
        }
        return mp;
    }

    public cMbTableData getSelectedData(){
        cMbTableData td = new cMbTableData();

        int rowIndexEnd = myTable.getSelectionModel().getMaxSelectionIndex();
        int colIndexEnd = myTable.getColumnModel().getSelectionModel().getMaxSelectionIndex();

        td.setName((String)myTable.getValueAt(rowIndexEnd,0));
        td.setColumnName((String)myTable.getColumnModel().getColumn(colIndexEnd).getHeaderValue());

        return td;
    }

    public int[] getSelectedColumIndices(){
        System.out.println("\n ... "+myTable.getSelectedColumn());
        if (myTable.getColumnSelectionAllowed()) {
            // Column selection is enabled
            // Get the indices of the selected columns
            System.out.println(" ... "+myTable.getColumnModel().getColumn(myTable.getSelectedColumn()).getHeaderValue()+"\n");
            for(int i=0;i<myTable.getSelectedColumns().length;i++){
                System.out.println(i+" ... "+myTable.getColumnModel().getColumn(i).getHeaderValue());
            }
            return myTable.getSelectedColumns();
        } else {
            return null;
        }
    }

    public int[] getSelectedRowIndices(){
        if (myTable.getRowSelectionAllowed()) {
//        if (!myTable.getColumnSelectionAllowed() && myTable.getRowSelectionAllowed()) {
            // Row selection is enabled
            // Get the indices of the selected rows
            return myTable.getSelectedRows();
        } else {
            return null;
        }
    }
}
