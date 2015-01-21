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
import java.sql.SQLException;
import java.util.List;
public interface FileDAO {

	/**
	 * 将文件存入到数据库信息中
	 * @param file
	 * @return
	 */
	void create(JFile file);
	
	/**
	 * 根据文件编号，获取文件相关信息
	 * @param fileID
	 * @return
	 */
	JFile get(String fileID);

   /**
    * 删除一个文件
    * @param fileID
    * @throws DataAccessException
    */
    int remove(String fileID);

    /**
     * 得到所有的文件
     * @param appCode
     * @return
     * @throws SQLException
     */
    List<JFile> getFdoList(String appCode);
}
