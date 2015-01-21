/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jef.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import jef.common.log.LogUtil;

/**
* @author root
   * 
   */
public class JExplorer {

    public JExplorer() {
    }

    /**
    * @param args
    */
    public static void main(String[] args) {
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			LogUtil.exception(e);
		} catch (InstantiationException e) {
			LogUtil.exception(e);
		} catch (IllegalAccessException e) {
			LogUtil.exception(e);
		} catch (UnsupportedLookAndFeelException e) {
			LogUtil.exception(e);
		}
        // JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new UI(frame));
        frame.pack();
        
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int left = (screen.width - frame.getWidth()) / 2;
        int top = (screen.height - frame.getHeight()) / 2;

        frame.setLocation(left, top);
        frame.setVisible(true);
    }
}

class UI extends JPanel {
    // implements I_menuHandler{
    static final long serialVersionUID = 0l;
    static int LEFT_WIDTH = 200;
    static int RIGHT_WIDTH = 300;
    static int WINDOW_HEIGHT = 300;
    
    JFrame frame = null;

    public UI(JFrame frame) {
        // EmptyBorder eb = new EmptyBorder(1,1,1,1);
        this.frame = frame;
        setPreferredSize(new Dimension(800, 600));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new BorderLayout());

        FileList list = new FileList();
        FileTree tree = new FileTree(list);
        tree.setDoubleBuffered(true);
        list.setDoubleBuffered(true);

        JScrollPane treeView = new JScrollPane(tree);
        treeView.setPreferredSize(new Dimension(LEFT_WIDTH, WINDOW_HEIGHT));

        JScrollPane listView = new JScrollPane(list);
        listView.setPreferredSize(new Dimension(RIGHT_WIDTH, WINDOW_HEIGHT));

        JSplitPane pane =    new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView,    listView);

        pane.setDividerLocation(300);
        pane.setDividerSize(4);
        // pane.setDoubleBuffered(true);

        add(pane);
    }
}
class FileList extends JList {
    //PathNode theNode;
    FileListModel dataModel;
    static final long serialVersionUID = 10;

    /**
    * 
    */
    public FileList() {
        dataModel = new FileListModel();
        setModel(dataModel);
        this.setCellRenderer(new MyCellRenderer());
    }

    public void fireTreeSelectionChanged(I_fileSystem node) {
        //Vector files = node.getFiles();
        //theNode = node;
        dataModel.setNode(node);
        updateUI();
    }
}

class FileListModel implements ListModel {
    FileList theList;
    I_fileSystem node;
    char fileType = I_fileSystem.ALL;
    public void setNode(I_fileSystem node) {
        this.node = node;
    }

    public Object getElementAt(int index) {
        if (node != null) {
            return ((I_fileSystem) node).getChild(fileType, index);
        } else {
            return null;
        }
    }

    public int getSize() {
        if (node != null) {
            return ((I_fileSystem) node).getChildCount(fileType);
        } else {
            return 0;
        }
    }

    public void addListDataListener(ListDataListener l) {

    }

    public void removeListDataListener(ListDataListener l) {
    }
}

class MyCellRenderer extends JLabel implements ListCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7762688000524503252L;

	public MyCellRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list,    Object value,    int index,    boolean isSelected,    boolean cellHasFocus) {
        FolderNode node = (FolderNode) value;
        setIcon(node.getIcon());
        setText(value.toString());
        setBackground(isSelected ? Color.BLUE.darker().darker() : Color.WHITE);
        setForeground(isSelected ? Color.WHITE : Color.BLACK);
        return this;
    }
}
class FileTree extends JTree {
    public FileTree() {
    }

    static final long serialVersionUID = 0;

    private FileList theList;

    public FileTree(FileList list) {
        theList = list;
        setModel(new FileSystemModel(new FolderNode()));
        this.setCellRenderer(new FolderRenderer());
        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse) {
            }
        });
        this.setSelectionRow(0);
    }

    public void fireValueChanged(TreeSelectionEvent tse) {
        TreePath tp = tse.getNewLeadSelectionPath();
        Object o = tp.getLastPathComponent();
        // theList.fireTreeSelectionChanged((PathNode)o);
        theList.fireTreeSelectionChanged((FolderNode) o);
    }

    public void fireTreeCollapsed(TreePath path) {
        super.fireTreeCollapsed(path);
        TreePath curpath = getSelectionPath();
        if (path.isDescendant(curpath)) {
            setSelectionPath(path);
        }
    }

    public void fireTreeWillExpand(TreePath path) {
        System.out.println("Path will expand is " + path);
    }

    public void fireTreeWillCollapse(TreePath path) {
        System.out.println("Path will collapse is " + path);
    }

    class ExpansionListener implements TreeExpansionListener {
        FileTree tree;

        public ExpansionListener(FileTree ft) {
            tree = ft;
        }

        public void treeCollapsed(TreeExpansionEvent tee) {
        }

        public void treeExpanded(TreeExpansionEvent tee) {
        }
    }
}

class FileSystemModel implements TreeModel {
    I_fileSystem theRoot;
    char fileType = I_fileSystem.DIRECTORY;

    public FileSystemModel(I_fileSystem fs) {
        theRoot = fs;
    }

    public Object getRoot() {
        return theRoot;
    }

    public Object getChild(Object parent, int index) {
        return ((I_fileSystem) parent).getChild(fileType, index);
    }

    public int getChildCount(Object parent) {
        return ((I_fileSystem) parent).getChildCount(fileType);
    }

    public boolean isLeaf(Object node) {
        return ((I_fileSystem) node).isLeaf(fileType);
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((I_fileSystem) parent).getIndexOfChild(fileType, child);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
}

interface I_fileSystem {
    final public static char DIRECTORY = 'D';
    final public static char FILE = 'F';
    final public static char ALL = 'A';

    public Icon getIcon();
    public I_fileSystem getChild(char fileType, int index);
    public int getChildCount(char fileType);
    public boolean isLeaf(char fileType);
    public int getIndexOfChild(char fileType, Object child);
}

/**
* A data model for a JTree. This model explorer windows file system directly.
* <p>
* Perhaps there is a fatal bug with this design. For speed, each of instances
* of this model contains file objects of subdirectory, up to now, there isn't
* any method to release them until program be end. I'm afraid that the memory
* would be full of if the file system is large enough and JVM memery size
* setted too small.
* <p>
* I won't pay more attention to solve it. it isn't goal of current a exercise.
   *
* @author Jason
   */

class FolderNode implements I_fileSystem {
    // private static FolderNode theRoot;
    private static FileSystemView fsView;
    private static boolean showHiden = true;;
    private File theFile;
    private Vector<File> all = new Vector<File>();
    private Vector<File> folder = new Vector<File>();
    
    /**
    * set that whether apply hiden file.
    * @param ifshow
       */
    public void setShowHiden(boolean ifshow) {
        showHiden = ifshow;
    }

    public Icon getIcon() {
        return fsView.getSystemIcon(theFile);
    }

    public String toString() {
        // return fsView.
        return fsView.getSystemDisplayName(theFile);
    }

    /**
    * create a root node. by default, it should be the DeskTop in window file system.
       */
    public FolderNode() {
        fsView = FileSystemView.getFileSystemView();
        theFile = fsView.getHomeDirectory();
        prepareChildren();
    }
    
    private void prepareChildren() {
        File[] files = fsView.getFiles(theFile, showHiden);

        for (int i = 0; i < files.length; i++) {
            all.add(files[i]);
            if (files[i].isDirectory()    && !files[i].toString().toLowerCase().endsWith(".lnk")) {
                folder.add(files[i]);
            }
        }
    }

    private FolderNode(File file) {
        theFile = file;
        prepareChildren();
    }

    public FolderNode getChild(char fileType, int index) {
        if (I_fileSystem.DIRECTORY == fileType) {
            return new FolderNode(folder.get(index));
        } else if (I_fileSystem.ALL == fileType) {
            return new FolderNode(all.get(index));
        } else if (I_fileSystem.FILE == fileType) {
            return null;
        } else {
            return null;
        }
    }

    public int getChildCount(char fileType) {
        if (I_fileSystem.DIRECTORY == fileType) {
            return folder.size();
        } else if (I_fileSystem.ALL == fileType) {
            return all.size();
        } else if (I_fileSystem.FILE == fileType) {
            return -1;
        } else {
            return -1;
        }
    }

    public boolean isLeaf(char fileType) {
        if (I_fileSystem.DIRECTORY == fileType) {
            return folder.size() == 0;
        } else if (I_fileSystem.ALL == fileType) {
            return all.size() == 0;
        } else if (I_fileSystem.FILE == fileType) {
            return true;
        } else {
            return true;
        }
    }

    public int getIndexOfChild(char fileType, Object child) {
        if (child instanceof FolderNode) {
            if (I_fileSystem.DIRECTORY == fileType) {
                return folder.indexOf(((FolderNode) child).theFile);
            } else if (I_fileSystem.ALL == fileType) {
                return all.indexOf(((FolderNode) child).theFile);
            } else if (I_fileSystem.FILE == fileType) {
                return -1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}

class FolderRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 1L;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,    boolean hasFocus) {
        I_fileSystem node = (I_fileSystem) value;
        Icon icon = node.getIcon();

        setLeafIcon(icon);
        setOpenIcon(icon);
        setClosedIcon(icon);

        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }
}
