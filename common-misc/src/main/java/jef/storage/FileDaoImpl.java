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

import javax.persistence.PersistenceException;

import jef.database.Session;
import jef.database.DbClient;
import jef.database.support.JefDbClientSupport;

/**
 * 使用JEF实现的FileDao
 */
public class FileDaoImpl implements FileDAO, JefDbClientSupport {
	private DbClient client;

	public void setClient(Session client) {
		this.client = (DbClient) client;
	}

	public void create(JFile file){
		client.insert(file);
	}

	public JFile get(String fileID) {
		JFile query = new JFile();
		query.getQuery().addCondition(JFile.Field.uuid, fileID);
		List<JFile> result;
		try {
			result = client.select(query);
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage() + " " + e.getSQLState(), e);
		}
		if (result.size() > 0)
			return (JFile) result.get(0);
		return null;
	}

	public List<JFile> getFdoList(String appCode) {
		JFile query = new JFile();
		query.getQuery().addCondition(JFile.Field.applicationCode, appCode);
		List<JFile> result;
		try {
			result = client.select(query);
			return result;
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage() + " " + e.getSQLState(), e);
		}
	}

	public int remove(String fileID) {
		JFile query = new JFile();
		query.getQuery().addCondition(JFile.Field.uuid, fileID);
		try {
			return client.delete(query);
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage() + " " + e.getSQLState(), e);
		}
	}

	public void checkTable() {
		try {
			client.createTable(JFile.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void afterPropertiesSet() throws Exception {
		checkTable();
	}
}
