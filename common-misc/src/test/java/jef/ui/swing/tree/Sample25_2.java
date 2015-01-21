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
package jef.ui.swing.tree;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
//定义该类继承自JFrame
public class Sample25_2 extends JFrame 
{
    /**
	 * @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;
	//创建并初始化工具栏中的工具按扭数组
    private JButton[] jbBar={new JButton("添加"),new JButton("删除")};
    //创建工具栏
    private JToolBar jtb=new JToolBar();
    //创建用来显示情况的文本框
    private JTextField jtf=new JTextField();
    //创建根节点
    private DefaultMutableTreeNode dmtnRoot=new DefaultMutableTreeNode("目录");
    //创建树的数据模型
    private DefaultTreeModel dtm=new DefaultTreeModel(dmtnRoot);
    //创建树状列表控件
    private JTree jt=new JTree(dtm);
    //为树状列表创建滚动窗口
    private JScrollPane jsp=new JScrollPane(jt);
    //用来记录当前选中节点的成员变量
    private DefaultMutableTreeNode tempNode;    
    //创建弹出式菜单
    private JPopupMenu jpm=new JPopupMenu();
    //创建菜单项数组
    private JMenuItem[] jmi={new JMenuItem("定义非叶子节点展开图标"),
    new JMenuItem("定义非叶子节点折叠图标"),new JMenuItem("定义叶子节点图标")};
    
    //用来作为树相关事件监听器的内部类
    private class MyTreeListener implements TreeExpansionListener,
                                TreeModelListener,TreeSelectionListener
    {
        //定义用来记录路径的节点数组
        private Object[] tempNodes;
        //创建StringBuffer对象
        private StringBuffer tempMsg=new StringBuffer();
        //声明用来记录路径TreePath
        private TreePath tp;
        //处理选中某节点后发生事件的方法
        public void valueChanged(TreeSelectionEvent tse)
        {
            //获得根节点到选中节点的路径
            tp=tse.getNewLeadSelectionPath();
            if(tp!=null)
            {
                //从路径中获得所有路径中的节点
                tempNodes=tp.getPath();
                //循环对路径中的每个节点进行处理
                for(int i=0;i<tempNodes.length;i++)
                {
                    //将各个节点的内容连接起来并添加到StringBuffer中
                    tempMsg.append(tempNodes[i]);
                    if(i!=tempNodes.length-1)
                    {//在各个节点中间添加“>>”符号
                        tempMsg.append(">>");
                    }
                }
                //在文本框中显示相应的信息
                jtf.setText("您选择了：“"+tempMsg.toString()+"”节点！！！");
                //删除StringBuffer中的所有内容
                tempMsg.delete(0,tempMsg.length());
                //记录选中的节点
                tempNode=(DefaultMutableTreeNode)tp.getLastPathComponent();
            }
        }
        //处理节点折叠后发生事件的方法
        public void treeCollapsed(TreeExpansionEvent tee)
        {
            //获得根节点到选中节点的路径
            tp=tee.getPath();
            //设置文本框显示相应的信息
            jtf.setText("您将"+tp.getLastPathComponent()+"节点折叠了！！！");
        }
        //处理节点展开后发生事件的方法
        public void treeExpanded(TreeExpansionEvent tee)
        {
            //获得根节点到选中节点的路径
            tp=tee.getPath();
            //设置文本框显示相应的信息
            jtf.setText("您将“"+tp.getLastPathComponent()+"”节点展开了！！！");        
        }
        //处理当节点名称更改之后发生事件的方法
        public void treeNodesChanged(TreeModelEvent tme)
        {
            //设置文本框显示节点名称更改信息
            jtf.setText("您将节点的标题修改了！！！");
        }        
        //以下三个方法由于本例中没有使用，所以均为空实现
        public void treeStructureChanged(TreeModelEvent tme){}
        public void treeNodesRemoved(TreeModelEvent tme){}
        public void treeNodesInserted(TreeModelEvent tme){}
    }
    //用来作为工具栏中按钮动作事件监听器的内部类
    class MyToolBarButtonListener implements ActionListener
    {
        //实现ActionListener监听接口中的方法
        public void actionPerformed(ActionEvent e)
        {
            //点击添加按扭执行的动作
            if(e.getSource()==jbBar[0])
            {
                //检查是否有选中节点
                if(tempNode==null)
                {
                    JOptionPane.showMessageDialog(Sample25_2.this,
                    "请您选择添加节点所在的目录！！！","错误"
                    ,JOptionPane.WARNING_MESSAGE);
                    return;    
                }
                //判断节点是否为代表“节”的节点
                if(tempNode.getLevel()==2)
                {
                    JOptionPane.showMessageDialog(Sample25_2.this,
                    "对不起，“节”节点不能添加子节点！！！","错误"
                    ,JOptionPane.WARNING_MESSAGE);
                    return;                        
                }
                //弹出输入对话框，要求输入节点名称
                String tempName=JOptionPane.showInputDialog(Sample25_2.this,
                "请输入添加节点的名称","请输入：",JOptionPane.INFORMATION_MESSAGE);
                //检查输入内容是否为null
                if(tempName==null) 
                {
                    return;//若为null方法返回
                }
                //去除输入内容两端的不可见字符
                tempName=tempName.trim();
                //检查输入的字符串是否为空字符串
                if(tempName.equals("")) 
                {
                    return;//若为空字符串则方法返回
                }
                //弹出输入对话框，要求输入节点的位置
                String tempID=JOptionPane.showInputDialog(Sample25_2.this,
                    "请输入（例如第一个节点的编号为1）：","请输入添加节点的编号",
                    JOptionPane.INFORMATION_MESSAGE);
                //检查输入内容是否为null
                if(tempID==null) 
                {
                    return;//若为null方法返回
                }
                //去除输入内容两端的不可见字符
                tempID=tempID.trim();
                //检查输入的字符串是否满足指定的格式要求
                if(!tempID.matches("[1-9][0-9]*"))
                {
                    JOptionPane.showMessageDialog(Sample25_2.this,
                        "输入数字格式无效！！！","错误",JOptionPane.WARNING_MESSAGE);
                    return;                        
                }
                //将输入的字符串转换为数值
                int id=Integer.parseInt(tempID);
                //检查数值是满足范围要求
                if(id<1||id>tempNode.getChildCount()+1)
                {
                    JOptionPane.showMessageDialog(Sample25_2.this,
                    "输入数字过大或太小！！！","错误",JOptionPane.WARNING_MESSAGE);
                    return;            
                }
                //创建新节点
                DefaultMutableTreeNode newNode=new DefaultMutableTreeNode(tempName);
                //将新节点插入到指定位置
                tempNode.insert(newNode,id-1);
                //更新树的数据模型
                ((DefaultTreeModel)jt.getModel()).reload(tempNode);
                //设置维持当前的选择路径
                jt.setExpandsSelectedPaths(true);
                //设置文本框中的提示信息
                jtf.setText("您在"+tempNode+"中添加了"+newNode+"节点！！！");    
            }
            else if(e.getSource()==jbBar[1])
            {//点击删除按扭执行的动作
                //检查当前是否选中了某节点
                if(tempNode==null) 
                {
                    JOptionPane.showMessageDialog(Sample25_2.this,
                    "请您选择要删除的节点！！！","错误",JOptionPane.WARNING_MESSAGE);
                    return;    
                }                
                //确认是否要删除选中的节点
                int confirm=JOptionPane.showConfirmDialog(Sample25_2.this,
                    "您确定要删除该节点么？","确认",JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                //确认需要删除执行的代码
                if(confirm==JOptionPane.YES_OPTION)
                {
                    //得到选中节点的父节点
                    DefaultMutableTreeNode node=
                                 (DefaultMutableTreeNode)tempNode.getParent();
                    if(node==null)
                    {
                        //若没有父节点说明该节点是根节点，警告不能删除
                        jtf.setText("根目录不能删除！！！");
                        JOptionPane.showMessageDialog(Sample25_2.this,
                        "根目录不能删除！！！","错误",JOptionPane.WARNING_MESSAGE);    
                    }
                    else
                    {//若具有父节点执行的代码
                        //设置提示信息
                        jtf.setText(tempNode+"被删除了！！！");
                        //删除以选中节点为根节点的子树
                        tempNode.removeFromParent();
                        //更新树的数据模型
                        ((DefaultTreeModel)jt.getModel()).reload(node);
                        //将记录选中节点的成员变量置为null值
                        tempNode=null;
                    }
                }
            }
        }
    }
  //用来作为鼠标事件监听器的内部类
    class MyJPopupListener extends MouseAdapter
    {
        public MyJPopupListener(JPopupMenu j)
        {//构造器,初始化弹出式菜单
            for(int i=0;i<jmi.length;i++)
            {
                //将菜单项添加到弹出式菜单中
                jpm.add(jmi[i]);
            }
        }
        public void mouseReleased(MouseEvent e)
        {//当鼠标释放时执行的动作
            //测试鼠标事件是否为平台无关的触发器
            if(e.isPopupTrigger())
            {//显示弹出式菜单
                jpm.show(e.getComponent(),e.getX(),e.getY());
            }
        }
    }    
    //用来作为菜单项动作事件监听器的内部类
    class MyPopMenuActionListener implements ActionListener
    {
        //文件选择器成员
        private JFileChooser jfc=new JFileChooser();
        //获取图片的getIcon方法
        public ImageIcon getIcon()
        {
            //定义图片变量
            ImageIcon icon=null;
            //弹出打开对话框
            jfc.showOpenDialog(Sample25_2.this);
            //获取选择的文件
            File f=jfc.getSelectedFile();
            if(f!=null)
            {//若获取的文件不为null，则根据此文件中的信息获取图片
                icon=new ImageIcon(f.getPath());
            }
            //返回图片
            return icon;
        }        
        //实现ActionListener监听接口中的事件处理方法
        public void actionPerformed(ActionEvent e)
        {
            //获取图片
            ImageIcon newIcon=this.getIcon();
            //获得树状列表当前的节点绘制器
            DefaultTreeCellRenderer dtcr
                =(DefaultTreeCellRenderer)jt.getCellRenderer();
            if(e.getSource()==jmi[0])
            {
                //更改展开节点的图标
                dtcr.setOpenIcon(newIcon);
                //设置提示信息
                jtf.setText("您重新定义了展开节点的图标！！！");
            }
            else if(e.getSource()==jmi[1])
            {
                //更改折叠节点的图标
                dtcr.setClosedIcon(newIcon);
                //设置提示信息
                jtf.setText("您重新定义了折叠节点的图标！！！");
            }
            else if(e.getSource()==jmi[2])
            {
                //更改叶子节点的图标
                dtcr.setLeafIcon(newIcon);
                //设置提示信息
                jtf.setText("您重新定义了叶子节点的图标！！！");
            }
            //记录当前选中的路径
            TreePath tempTp=jt.getSelectionPath();
            //更新树的数据模型
            ((DefaultTreeModel)jt.getModel()).reload();    
            //设置维持当前的选中的路径
            jt.setSelectionPath(tempTp);
        }
    }    
    public Sample25_2()
    {
        for(int i=0;i<jbBar.length;i++)
        {//将按扭添加到工具栏中
            jtb.add(jbBar[i]);
        }
        for(int i=0;i<5;i++)
        {//向根节点中添加5个预定义的章节点
            dmtnRoot.add(new DefaultMutableTreeNode("第"+(i+1)+"章"));
        }
        for(int i=0;i<dmtnRoot.getChildCount();i++)
        {
            DefaultMutableTreeNode temp=(DefaultMutableTreeNode)dmtnRoot.getChildAt(i);
            for(int j=0;j<2;j++)
            {//向每个章节节点中添加2个预定义的小节节点
                temp.add(new DefaultMutableTreeNode("第"+(j+1)+"节"));            
            }
        }
        //设置JTree为可编辑状态
        jt.setEditable(true);
        //设置JTree将显示根节点的控制图标
        jt.setShowsRootHandles(true);
        //设置文本框为不可编辑状态
        jtf.setEditable(false);
        //将工具栏添加进窗体
        this.add(jtb,BorderLayout.NORTH);
        //分别树状列表以及文本框添加进窗体的中间以及下部
        this.add(jsp,BorderLayout.CENTER);
        this.add(jtf,BorderLayout.SOUTH);
        //设置窗体的标题、大小位置以及可见性
        this.setTitle("目录管理器");
        this.setResizable(false);
        this.setBounds(100,100,350,400);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //创建监听器对象
        MyTreeListener mtl=new MyTreeListener();
        //为JTree注册选择事件的监听器
        jt.addTreeSelectionListener(mtl);
        //为JTree注册选择展开以及折叠事件的监听器
        jt.addTreeExpansionListener(mtl);
        //为JTree注册数据模型更改事件的监听器
        dtm.addTreeModelListener(mtl);        
        //创建监听器对象
        MyToolBarButtonListener mtbl=new MyToolBarButtonListener();
        //为添加按扭注册监听器
        jbBar[0].addActionListener(mtbl);
        //为添加按扭注册监听器
        jbBar[1].addActionListener(mtbl);
        //创建鼠标事件的监听器
        MyJPopupListener mjpl=new MyJPopupListener(jpm);
        //为JTree注册鼠标事件的监听器
        jt.addMouseListener(mjpl);
        //创建菜单项动作事件监听器对象
        MyPopMenuActionListener mpmal=new MyPopMenuActionListener();
        //为菜单项注册动作事件监听器
        for(int i=0;i<jmi.length;i++)
        {
            jmi[i].addActionListener(mpmal);
        }                
    }
    public static void main(String[] args)
    {
        //创建Sample25_2窗体对象
    	
    	
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
        new Sample25_2();
    }
}
