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
package jef.storage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public interface IFileManager {
	/**
	 * 储存指定的文件
	 * @param fileDO
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public JFile insert(JFile fileDO) throws IOException;

	/**
	 * 储存指定的文件
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public JFile insert(File file,String fileDesc) throws IOException;
	
	/**
	 * 储存指定的文件
	 * @param file
	 * @param fileDesc
	 * @return
	 * @throws IOException
	 */
	public JFile insert(byte[] file,String fileDesc) throws IOException;
	
	/**
	 * 将制定ID的存储文件替换为指定的文件
	 * @param oldId
	 * @param newFile
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public JFile update(String oldId, JFile newFile) throws IOException;

	/**
	 * 删除指定id的文件
	 * @param fileID
	 * @return
	 * @throws SQLException
	 */
	public boolean remove(String fileID) throws IOException;

	/**
	 * 获取指定id的文件
	 * @param fileID
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public JFile get(String fileID) throws IOException;
}
