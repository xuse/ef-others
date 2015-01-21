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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;

class GetFile {
	String s;

	public String getFile() {// 读取文件里的用户信息
		try {
			byte[] buf = new byte[1024];
			File infile = new File("text.txt");
			FileInputStream inFileStream = new FileInputStream(infile);
			int len = inFileStream.read(buf);// 读文件
			if (len == -1){// 没有读到
				int a = JOptionPane.showConfirmDialog(null, "还没有相应的信息,是否添加？");
				if (a == 0)	{// 要添加
					AddUser au = new AddUser();
					au.addUser();
					JOptionPane.showMessageDialog(null, "信息添加成功！");
					System.exit(0);
				} else {
					JOptionPane.showMessageDialog(null, "GOODBYE!");
					System.exit(0);
				}
			} else {
				s = new String(buf, 0, len);
				inFileStream.close();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
		return s;
	}
}

class WriteFile {
	public void writeFile(String s) {
		File outfile = new File("text.txt");
		try {
			FileOutputStream outFileStream = new FileOutputStream(outfile);
			outFileStream.write(s.getBytes());
			outFileStream.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
}

class CheckUser {// 检查用户信息用来开门
	public void checkUser() {
		GetFile gf = new GetFile();
		String gets = gf.getFile();// 读取文件

		int len = gets.length(); // 获取文件长度

		//int usernamepos = gets.indexOf("username:");
		int roomnumpos = gets.indexOf("roomnum:");
		int passwordpos = gets.indexOf("password:");

		if (len == 0) {
			JOptionPane.showMessageDialog(null, "用户列表为空!");// 文件中还没有记录
		} else {
			String usernameIn = JOptionPane.showInputDialog(null, "请输入你的用户名");
			String roomnumIn = JOptionPane.showInputDialog(null, "请输入你的房间号");
			String passwordIn = JOptionPane.showInputDialog(null, "请输入你的密码");

			String usernamec = gets.substring(9, roomnumpos);// 在处理过程中要注意位置的偏移量别算错了哦
			String roomnumc = gets.substring(roomnumpos + 8, passwordpos);
			String passwordc = gets.substring(passwordpos + 9, len);

			if (usernameIn.equals(usernamec) && passwordIn.equals(passwordc) && roomnumIn.equals(roomnumc)) {// 验证用户名和密码
				JOptionPane.showMessageDialog(null, "欢迎回来!");
				System.exit(0);
			} else {
				int i = JOptionPane.showConfirmDialog(null, "没有房子？买一个？");
				if (i == 0) // 要买
				{
					AddUser au = new AddUser();
					au.addUser();
					int d = JOptionPane.showConfirmDialog(null, "是否马上搬进新家?");
					if (d == 0)// 要搬进去
					{
						CheckUser cu = new CheckUser();
						cu.checkUser();
					} else // 不进去就再见
					{
						JOptionPane.showMessageDialog(null, "GOODBYE！");
						System.exit(0);
					}

				} else {// 不买也再见
					JOptionPane.showMessageDialog(null, "GoodBye!");
					System.exit(0);
				}
			}
		}
	}
}

class AddUser{ // 添加用户
	public void addUser() {
		String s0 = JOptionPane.showInputDialog(null, "请输入你要增加的用户名和密码");
		String s1 = JOptionPane.showInputDialog(null, "请输入你的房间号");
		String s2 = JOptionPane.showInputDialog(null, "请输入你的密码");

		String information = "username:" + s0 + "roomnum:" + s1 + "password:" + s2;
		WriteFile wf = new WriteFile();
		wf.writeFile(information);
	}
}

class CheckFile {// 检查文件是否存在
	public void checkFile() {
		File f = new File("text.txt");
		if (!f.exists()) {
			try {
				File dirFile = new File("text.txt"); // 创建新的文件
				dirFile.createNewFile();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

	}
}

public class HomeManage {
	public static void main(String[] args) {
		CheckFile cf = new CheckFile();// 检查文件
		cf.checkFile();
		CheckUser cu = new CheckUser();// 检查用户信息
		cu.checkUser();

	}
}
