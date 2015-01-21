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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

//-------------------------MainApp.java
public class BookAdminSwing {
	public static void main(String[] args) {
		BookListView listView = new BookListView();
		listView.setSize(400, 300);
		listView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		listView.setTitle("图书管理");
		listView.setVisible(true);
	}
}

class Book implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9026243581015584464L;
	private String isbn; // ISBN
	private String title; // 书名
	private String author; // 作者
	private double price; // 价格

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		return title + " (" + author + ")";
	}
}

// --------------BookList.java
class BookList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2051042903678899290L;
	// 数据文件名
	private static final String FILE_NAME = "books.dat";
	private ArrayList<Book> books = new ArrayList<Book>();
	private BookListModel listModel;

	/**
	 * 添加书籍。该方法将更新列表。
	 * 
	 * @param book
	 *            待添加的书籍
	 */
	public void add(Book book) {
		books.add(book);
		getListModel().fireAdded();
	}

	/**
	 * 删除书籍。该方法将更新列表。
	 * 
	 * @param book
	 *            待删除的书籍
	 */
	public void remove(Book book) {
		int index = books.indexOf(book);
		books.remove(book);
		getListModel().fireRemoved(index);
	}

	/**
	 * 当Book对象发生改变时，可调用此方法通知列表进行刷新。
	 * 
	 * @param book
	 *            发生改变的书籍
	 */
	public void modified(Book book) {
		int index = books.indexOf(book);
		getListModel().fireModified(index);
	}

	/**
	 * 获取ListModel。
	 * 
	 * @return ListModel
	 */
	public BookListModel getListModel() {
		if (listModel == null) {
			listModel = new BookListModel();
		}
		return listModel;
	}

	/**
	 * 从文件中加载数据。
	 * 
	 * @return BookList对象
	 */
	public static BookList load() {
		ObjectInputStream ois = null;
		BookList bookList = null;
		try {
			File file = new File(FILE_NAME);
			if (file.exists()) { // 如果文件存在，从文件中读取数据
				ois = new ObjectInputStream(new FileInputStream(file));
				bookList = (BookList) ois.readObject();
			} else { // 否则创建一个新对象.
				bookList = new BookList();
				bookList.books = new ArrayList<Book>();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (ois != null)
				try {
					ois.close();
				} catch (IOException e1) {
				}
		}
		return bookList;
	}

	/**
	 * 保存数据到文件中。
	 */
	public void save() {
		ObjectOutputStream oos = null;
		try {
			File file = new File(FILE_NAME);
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this);
		} catch (Exception e) {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e1) {
				}
			JOptionPane.showMessageDialog(null, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * 图书列表模型
	 */
	class BookListModel extends AbstractListModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5937623189107633795L;

		public int getSize() {
			return books.size();
		}

		public Object getElementAt(int index) {
			return books.get(index);
		}

		public void fireAdded() {
			fireIntervalAdded(this, getSize() - 1, getSize() - 1);
		}

		public void fireRemoved(int index) {
			fireIntervalRemoved(this, index, index);
		}

		public void fireModified(int index) {
			fireContentsChanged(this, index, index);
		}
	}
}

// _________________BookListView.java
class BookListView extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6906547619323957901L;
	private JButton jbtAdd = new JButton("添加");
	private JButton jbtDelete = new JButton("删除");
	private JButton jbtEdit = new JButton("编辑");
	private JButton jbtSave = new JButton("保存");
	private BookList bookList = BookList.load();
	private JList list = new JList();

	public BookListView() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 4));
		p.add(jbtAdd);
		p.add(jbtEdit);
		p.add(jbtDelete);
		p.add(jbtSave);
		list.setModel(bookList.getListModel());
		getContentPane().add(new JLabel("图书列表:"), BorderLayout.NORTH);
		getContentPane().add(list, BorderLayout.CENTER);
		getContentPane().add(p, BorderLayout.SOUTH);
		// Register listeners
		jbtAdd.addActionListener(this);
		jbtEdit.addActionListener(this);
		jbtDelete.addActionListener(this);
		jbtSave.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		Book book = new Book();
		if (e.getSource() == jbtAdd) {
			BookPanel bookPanel = new BookPanel();
			int result = JOptionPane.showOptionDialog(this, bookPanel, "添加书籍", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "确定", "取消" }, null);
			if (result == 0) {
				// get the information from bookPanel;
				book.setAuthor(bookPanel.getAuthor().getText());
				book.setIsbn(bookPanel.getIsbn().getText());
				book.setPrice(Double.parseDouble(bookPanel.getPrice().getText()));
				book.setTitle(bookPanel.getTitle().getText());
				bookList.add(book);
			}
		} else if (e.getSource() == jbtEdit) {
			BookPanel bookPanel = new BookPanel();
			book = (Book) list.getSelectedValue();
			bookPanel.getAuthor().setText(book.getAuthor());
			bookPanel.getTitle().setText(book.getTitle());
			bookPanel.getPrice().setText(String.valueOf(book.getPrice()));
			bookPanel.getIsbn().setText(book.getIsbn());
			int result = JOptionPane.showOptionDialog(this, bookPanel, "编辑书籍", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] { "保存", "取消" }, null);
			if (result == 0) {
				// Book book = new Book();
				book.setAuthor(bookPanel.getAuthor().getText());
				book.setIsbn(bookPanel.getIsbn().getText());
				book.setPrice(Double.parseDouble(bookPanel.getPrice().getText()));
				book.setTitle(bookPanel.getTitle().getText());
				bookList.modified(book);
			}
		} else if (e.getSource() == jbtDelete) {
			int result = JOptionPane.showConfirmDialog(this, "你确定要删除吗?", "删除书籍", JOptionPane.CANCEL_OPTION);
			if (result == 0)
				book = (Book) list.getSelectedValue();
			bookList.remove(book);
		} else if (e.getSource() == jbtSave) {
			bookList.save();
		}
	}
}

// ------------------------BookPanel.java
class BookPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6927793298385999513L;
	private JTextField isbn = new JTextField();
	private JTextField price = new JTextField();
	private JTextField title = new JTextField();
	private JTextField author = new JTextField();

	public BookPanel() {
		this.setLayout(new GridLayout(4, 1));
		this.add(new JLabel("ISBN:"), BorderLayout.WEST);
		this.add(isbn, BorderLayout.CENTER);
		this.add(new JLabel("书名:"), BorderLayout.WEST);
		this.add(title, BorderLayout.CENTER);
		this.add(new JLabel("作者:"), BorderLayout.WEST);
		this.add(author, BorderLayout.CENTER);
		this.add(new JLabel("价格:"), BorderLayout.WEST);
		this.add(price, BorderLayout.CENTER);
	}

	public JTextField getAuthor() {
		return author;
	}

	public void setAuthor(JTextField author) {
		this.author = author;
	}

	public JTextField getIsbn() {
		return isbn;
	}

	public void setIsbn(JTextField isbn) {
		this.isbn = isbn;
	}

	public JTextField getPrice() {
		return price;
	}

	public void setPrice(JTextField price) {
		this.price = price;
	}

	public JTextField getTitle() {
		return title;
	}

	public void setTitle(JTextField title) {
		this.title = title;
	}
}
