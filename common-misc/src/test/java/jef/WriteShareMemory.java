package jef;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import jef.tools.ThreadUtils;

/**
 * 往 "共享内存" 写入数据
 */
public class WriteShareMemory implements Runnable{

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Thread t=ThreadUtils.doTask(new WriteShareMemory());
		read();
		t.join();
	}
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void read() throws Exception {
		RandomAccessFile raf = new RandomAccessFile("c:/swap.mm", "rw");
		FileChannel fc = raf.getChannel();
		MappedByteBuffer mbb = fc.map(MapMode.READ_WRITE, 0, 1024);
		int lastIndex = 0;

		for (int i = 1; i < 27; i++) {
			int flag = mbb.get(0); // 取读写数据的标志
			int index = mbb.get(1); // 读取数据的位置,2 为可读

			if (flag != 2 || index == lastIndex) { // 假如不可读，或未写入新数据时重复循环
				i--;
				continue;
			}

			lastIndex = index;
			System.out.println("程序 ReadShareMemory：" + System.currentTimeMillis() + "：位置：" + index + " 读出数据：" + (char) mbb.get(index));

			mbb.put(0, (byte) 0); // 置第一个字节为可读标志为 0

			if (index == 27) { // 读完数据后退出
				break;
			}
		}
	}
	public void run() {
		try{
			RandomAccessFile raf = new RandomAccessFile("c:/swap.mm", "rw");
			FileChannel fc = raf.getChannel();
			MappedByteBuffer mbb = fc.map(MapMode.READ_WRITE, 0, 1024);
			// 清除文件内容
			for (int i = 0; i < 1024; i++) {
				mbb.put(i, (byte) 0);
			}

			// 从文件的第二个字节开始，依次写入 A-Z 字母，第一个字节指明了当前操作的位置
			for (int i = 65; i < 91; i++) {
				int index = i - 63;
				int flag = mbb.get(0); // 可读标置第一个字节为 0
				if (flag != 0) { // 不是可写标示 0，则重复循环，等待
					i--;
					continue;
				}
				mbb.put(0, (byte) 1); // 正在写数据，标志第一个字节为 1
				mbb.put(1, (byte) (index)); // 写数据的位置

				System.out.println("程序 WriteShareMemory：" + System.currentTimeMillis() + "：位置：" + index + " 写入数据：" + (char) i);

				mbb.put(index, (byte) i);// index 位置写入数据
				mbb.put(0, (byte) 2); // 置可读数据标志第一个字节为 2
				ThreadUtils.doSleep(513);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
