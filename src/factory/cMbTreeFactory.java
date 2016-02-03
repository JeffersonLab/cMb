package factory;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Position;
import javax.swing.tree.*;
import java.util.Enumeration;
import java.util.ArrayList;

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
public class cMbTreeFactory {
    /** jTree root objec*/
    private DefaultMutableTreeNode root;
    private JTree AgentTree;
    //** root path*/
    TreePath rootPath;
    private String rootName;


    public cMbTreeFactory(String rootnodename){
        root = new DefaultMutableTreeNode(rootnodename);
        rootName = rootnodename;
    }
    
    public JTree createTree(String name, TreeSelectionListener tl){
        AgentTree =  new JTree(root);
        AgentTree.addTreeSelectionListener(tl);
        AgentTree.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
//        AgentTree.setFont(Font.getFont("Luxi Sans-Bold-13"));
        AgentTree.setName(name);
       return AgentTree;
    }

    public JTree getTree(){
        return AgentTree;
    }


    /**
     * Adds the node to the path
     * @param path  TreePath object
     * @param name  the name of the branch
     */
    public void addTreeNode(TreePath path, String name) {

        DefaultTreeModel model = (DefaultTreeModel) AgentTree.getModel();

        MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();

        // Creating branch node of the root node
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);

        // Insert new session node as last child of the expid node
        model.insertNodeInto(newNode, node, node.getChildCount());
        AgentTree.validate();

        // expand the tree
        TreeNode base = (TreeNode) AgentTree.getModel().getRoot();
        expandAll(AgentTree, new TreePath(base), true);

    }


    /**
     * removes the node specified by its name
     * @param prefix of the name of the node
     */
    public void removeTreeNodeStartingWith(String prefix){
        int startRow = 0;
               DefaultTreeModel model = (DefaultTreeModel)AgentTree.getModel();
               TreePath path = AgentTree.getNextMatch(prefix, startRow, Position.Bias.Forward);
               if(path!=null){
               MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
               // Remove node; if node has descendants, all descendants are removed as well
               model.removeNodeFromParent(node);
               }
           }


    /**
     * removes the node specified by its name
     * @param name of the node
     */
    public void removeTreeNode(String name){
        int startRow = 0;
               DefaultTreeModel model = (DefaultTreeModel)AgentTree.getModel();
               TreePath path = getExactMatch(name, startRow, Position.Bias.Forward);
               if(path!=null){
               MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
               // Remove node; if node has descendants, all descendants are removed as well
               model.removeNodeFromParent(node);
               }
           }


    public TreePath getExactMatch(String name, int startingRow,
				 Position.Bias bias){

        int max = AgentTree.getRowCount();
	if (name == null) {
	    throw new IllegalArgumentException();
	}
	if (startingRow < 0 || startingRow >= max) {
	    throw new IllegalArgumentException();
	}
	name = name.toUpperCase();

	// start search from the next/previous element from the
	// selected element
	int increment = (bias == Position.Bias.Forward) ? 1 : -1;
	int row = startingRow;
	do {
	    TreePath path = AgentTree.getPathForRow(row);
	    String text = AgentTree.convertValueToText(
	        path.getLastPathComponent(), AgentTree.isRowSelected(row),
		AgentTree.isExpanded(row), true, row, false);

	    if (text.toUpperCase().equals(name)) {
		return path;
	    }
	    row = (row + increment + max) % max;
	} while (row != startingRow);
	return null;
    }


    /**
     * Finds the path in tree as specified by the arraylist of names. The names arraylist is a
     * sequence of names where the first is the root and i'th is a child of i-1.
     * Returns null if not found.
     * @param al array list of the names
     * @return object of TreePath
     */
    public TreePath findTreeNodeByName(ArrayList<String> al) {
        String[] names = al.toArray(new String[al.size()]);
        TreeNode root = (TreeNode)AgentTree.getModel().getRoot();
        return findTreeNode(AgentTree, new TreePath(root), names, 0, true);
    }


    private TreePath findTreeNode(JTree tree, TreePath parent, Object[] nodes, int depth, boolean byName) {
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        Object o = node;

        // If by name, convert node to a string
        if (byName) {
            o = o.toString();
        }

        // If equal, go down the branch
        if (o.equals(nodes[depth])) {
            // If at end, return match
            if (depth == nodes.length-1) {
                return parent;
            }

            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                    TreeNode n = (TreeNode)e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = findTreeNode(tree, path, nodes, depth+1, byName);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        // No match at this branch
        return null;
    }

    /**
     * Updates the tree based on the String array composed of agent expid, session, runtype, type, name
     * @param addr arr of Strings
     */
    public void updateTree(String[] addr){
        rootPath = findNode(rootName);
        TreePath parent = rootPath;
        TreePath tmpPath;
        String name = addr[addr.length-1];
        ArrayList<String> al = new ArrayList<String>();
        al.add(rootName);
        for (String anAddr : addr) {
            if (anAddr != null){
                al.add(anAddr);
                tmpPath = findTreeNodeByName(al);
                if(tmpPath==null){
                    removeTreeNode(name);
                  addTreeNode(parent,anAddr);
                }
                    parent = findTreeNodeByName(al);
            }
        }
    }

    /**
     * Adds the new node to the parent node. If there is a node with the
     * same name it will remove it before adding the new one.
     *
     * @param parent  The name of the parent node
     * @param child   The name of the child node
     */
    public void addNodeToParen(String parent, String child){
        TreePath tmpPath, tmpChildPath;
        tmpPath = findNode(parent);
        if(tmpPath!=null){
            tmpChildPath = findNode(child);
            if(tmpChildPath!=null){
                removeTreeNode(child);
            }
            addTreeNode(tmpPath,child);
        }
    }

    /** Finds the node starting from the root
     * @param nodename              The name of the node
     * @return node              MutableTreeNode object
     */
    public TreePath findNode(String nodename) {
        int startingRow = 0;
        return AgentTree.getNextMatch(nodename, startingRow, Position.Bias.Forward);
    }


    /**
     * Method helps to expand  or collapse jtree
     * @param tree    Jtree object
     * @param parent  JtreePath object
     * @param expand      true: expands, false: collapses
     */
    private void expandAll(JTree tree, TreePath parent, boolean expand) {

        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        // Expansion or collapse must be done buttom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }



    //----------------------------------------------------------------------------
    /**
     * Removes the node from the dalog browser tree.
     * If the node has descendants all descendants are removed as well.
     * @param nodename              The name of the node
     */
    public void removeNodeFromTheTree(String nodename) {
        int startingRow = 0;

        // Get the default model of the AgentTree
        DefaultTreeModel model = (DefaultTreeModel) AgentTree.getModel();

        // Find node to remove
        TreePath path = AgentTree.getNextMatch(nodename, startingRow, Position.Bias.Forward);
        MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();

        // Remove the node
        model.removeNodeFromParent(node);
        System.out.println(" removed the node - "+nodename);
    }

    /**
     *
     * @param nn node name to be remved
     */
    public void deleteTheNode(String nn){
        removeNode(searchNode(nn));

    }

    /**
     * This method takes the node string and
     * traverses the tree till it finds the node
     * matching the string. If the match is found
     * the node is returned else null is returned
     *
     * @param nodeStr node string to search for
     * @return tree node
     */
    public DefaultMutableTreeNode searchNode(String nodeStr)
    {
        DefaultMutableTreeNode node;

        //Get the enumeration
        Enumeration enu = root.breadthFirstEnumeration();

        //iterate through the enumeration
        while(enu.hasMoreElements())
        {
            //get the node
            node = (DefaultMutableTreeNode)enu.nextElement();

            //match the string with the user-object of the node
            if(nodeStr.equals(node.getUserObject().toString()))
            {
                //tree node with string found
                return node;
            }
        }

        //tree node with string node found return null
        return null;
    }
    /**
     * This method removes the passed tree node from the tree
     * and selects appropiate node
     *
     * @param selNode node to be removed
     */
    public void removeNode(DefaultMutableTreeNode selNode)
    {
        if (selNode != null)
        {
            //get the default model of the AgentTree
            DefaultTreeModel model = (DefaultTreeModel) AgentTree.getModel();

            //get the parent of the selected node
            MutableTreeNode parent = (MutableTreeNode)(selNode.getParent());

            // if the parent is not null
            if (parent != null)
            {
                //get the sibling node to be selected after removing the
                //selected node
                MutableTreeNode toBeSelNode = getSibling(selNode);

                //if there are no siblings select the parent node after removing the node
                if(toBeSelNode == null)
                {
                    toBeSelNode = parent;
                }

                //make the node visible by scroll to it
                TreeNode[] nodes = model.getPathToRoot(toBeSelNode);
                TreePath path = new TreePath(nodes);
                AgentTree.scrollPathToVisible(path);
                AgentTree.setSelectionPath(path);

                //remove the node from the parent
                model.removeNodeFromParent(selNode);
            }
        }
    }

    /**
     * This method returns the previous sibling node
     * if there is no previous sibling it returns the next sibling
     * if there are no siblings it returns null
     *
     * @param selNode selected node
     * @return previous or next sibling, or parent if no sibling
     */
    private MutableTreeNode getSibling(DefaultMutableTreeNode selNode)
    {
        //get previous sibling
        MutableTreeNode sibling = selNode.getPreviousSibling();

        if(sibling == null)
        {
            //if previous sibling is null, get the next sibling
            sibling    = selNode.getNextSibling();
        }

        return sibling;
    }
    //----------------------------------------------------------------------------
    /**
     * Adds the node to the root node
     * @param rootnode   the name of the root node
     * @param name       the name of the branch
     */
    public void addNodeToTheTree(String rootnode, String name) {

        int startingRow = 0;

        DefaultTreeModel model = (DefaultTreeModel) AgentTree.getModel();

        // Find the root node to which the new branch node is to be added
        TreePath path = AgentTree.getNextMatch(rootnode, startingRow, Position.Bias.Forward);
//        System.out.println(" Path .... " + path + " rootnode = " + rootnode);
        MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();

        // Creating branch node of the root node
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);

        // Insert new session node as last child of the expid node
        model.insertNodeInto(newNode, node, node.getChildCount());
        AgentTree.validate();

        // expand the tree
        TreeNode base = (TreeNode) AgentTree.getModel().getRoot();
        expandAll(AgentTree, new TreePath(base), true);

    }
    /**
     * Adds the component node to the typenode of the specific root (runtype, session, etc)
     * @param grandpa    the name of the parent of the parent node
     * @param pa         the name of the parent node
     * @param name       the name of the component node
     */
    public void addGeneticNode(String grandpa, String pa, String name) {

        DefaultMutableTreeNode tmpnode, typenode = null;

        DefaultTreeModel model = (DefaultTreeModel) AgentTree.getModel();

        DefaultMutableTreeNode node = searchNode(grandpa);
//        System.out.println(".... "+node.getChildCount());
        node.getChildAt(0);
        for(int i =0; i<node.getChildCount(); i++){
            tmpnode = (DefaultMutableTreeNode)node.getChildAt(i);
            if(tmpnode!=null){
//                System.out.println(".... "+tmpnode.getUserObject().toString()+" :: "+pa);
                if(tmpnode.getUserObject().toString().equals(pa)){
                    typenode = tmpnode;
                }
            }
        }

        if(typenode!=null){
            // Creating branch node of the root node
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);

            // Insert new session node as last child of the node
            model.insertNodeInto(newNode, typenode, typenode.getChildCount());
            AgentTree.validate();

            // expand the tree
            TreeNode base = (TreeNode) AgentTree.getModel().getRoot();
            expandAll(AgentTree, new TreePath(base), true);
        }
    }
    
}
