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


import jef.common.JefException;
import jef.database.DbClient;
 
public class FileServiceFactory {
	private static final String CONTEXT_CONFIG_PATH="jef/storage/jefStorage.xml";
	private static final String MAIN_MANAGER_NAME="fileManager";
	private static final String BIND_DBCLIENT_NAME="dbClient";
	private static final String JEFFS_APPLICATION_CONTEXT="ApplicationContext.Of.Jef.FileSystem";
	
	private static void initWithoutWeb() throws JefException {
		//String filePath=FileServiceFactory.class.getResource(CONTEXT_CONFIG_PATH).getFile();
		
//		File file=ResourceUtil.getResourceFile(CONTEXT_CONFIG_PATH);
//		if(file==null)throw new JefException("The file "+ CONTEXT_CONFIG_PATH +" not exists.");
//		ContextFactory.holdContext(JEFFS_APPLICATION_CONTEXT, ContextFactory.initWithoutWeb(file));
	}
	
	public static FileManager getFileManage() throws JefException{
//		if(ContextFactory.getContextWithoutWeb(JEFFS_APPLICATION_CONTEXT)==null){
//			initWithoutWeb();
//		}
//		return (FileManager) ContextFactory.getContextWithoutWeb(JEFFS_APPLICATION_CONTEXT).getBean(MAIN_MANAGER_NAME);
		return null;
	}
	
	public static DbClient getBindDbClient() throws JefException{
//		if(ContextFactory.getContextWithoutWeb(JEFFS_APPLICATION_CONTEXT)==null){
//			initWithoutWeb();
//		}
//		return (DbClient) ContextFactory.getContextWithoutWeb(JEFFS_APPLICATION_CONTEXT).getBean(BIND_DBCLIENT_NAME);
		return null;
	}
}
