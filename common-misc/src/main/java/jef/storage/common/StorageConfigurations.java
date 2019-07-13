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
package jef.storage.common;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jef.tools.Exceptions;
import jef.tools.ResourceUtils;
import jef.tools.XMLUtils;


public final class StorageConfigurations {
	public static final String fileLogName = "fileLog.xml";
	public static final String DEFAULT_APPLICATION_NAME = "FileAccessSystem";
	public static final String configPath = "/FileStorageConfig.xml";
	
	private static Map<String,Storage> Applications = null;
	
	public static Storage getApplicationContext(String applicationCode) {
		if(Applications==null){
			init();
		}
		if (applicationCode!=null && StorageConfigurations.Applications.containsKey(applicationCode)){
			return (Storage) StorageConfigurations.Applications.get(applicationCode);
		}else{
			return (Storage) StorageConfigurations.Applications.get(StorageConfigurations.DEFAULT_APPLICATION_NAME);
		}
	}

	private static void init() {
		Applications = new HashMap<String,Storage>();
		try{
			URL file=getFile();
			if(file!=null){
				Document document = XMLUtils.loadDocument(file);
				parseConfig(document);	
			}
		}catch(IOException e){
			Exceptions.log(e);
		} catch (SAXException e) {
			Exceptions.log(e);
		}
		StorageConfigurations.Applications.put(StorageConfigurations.DEFAULT_APPLICATION_NAME, getDefaultApp());// 加载系统内建的默认Application设置
	}

	private static URL getFile() {
		return ResourceUtils.getResource(StorageConfigurations.class, configPath);
	}

	public static Storage getDefaultApp() {
		Storage defaultApp = new Storage();
		defaultApp.setName(StorageConfigurations.DEFAULT_APPLICATION_NAME);
		defaultApp.setRoot("C:/FileSystem");
		defaultApp.setDirectory("/default");
		defaultApp.setStorageType("zipFile");
		defaultApp.setStorageNaming(Storage.StorageNaming.TIMESTAMP);
		defaultApp.setDirStructure(Storage.DirStructure.YM2);
		return defaultApp;
	}

	private static void parseConfig(Document document) throws IOException  {
		Element ApplicationSystemSElement =document.getDocumentElement();
		
		NodeList nds = ApplicationSystemSElement.getElementsByTagName("Application");
		for(int i=0;i<nds.getLength();i++){
			Storage app = new Storage();
			Element application = (Element)nds.item(i);
			app.setName(application.getAttribute("name").trim());
			app.setRoot(application.getAttribute("directory").trim());

			app.setDirectory(XMLUtils.nodeText(XMLUtils.first(application,"directory")));
			app.setStorageType(XMLUtils.nodeText(XMLUtils.first(application,"storageType")));

			app.setDirStructure(Enum.valueOf(Storage.DirStructure.class,
					XMLUtils.nodeText(XMLUtils.first(application,"dirStructure"))
			));
			app.setStorageNaming(Enum.valueOf(Storage.StorageNaming.class,
					XMLUtils.nodeText(XMLUtils.first(application,"storageFileNaming"))
			));

			Element nE = XMLUtils.first(application, "noCompress");
			if ("true".equals(nE.getAttribute("enable"))) {
				app.setEnableNativeStorage(true);
				Element ftE= XMLUtils.first(nE,"fileTypes");
				NodeList nds3=ftE.getElementsByTagName("fileType");
				for (int k=0;k<nds3.getLength();k++) {
					Element fileType = (Element)nds3.item(k);
					app.getNoZipFileTypes().put(fileType.getAttribute("ext").toLowerCase(), XMLUtils.nodeText(fileType));
				}
			} else {
				app.setEnableNativeStorage(false);
			}
			Applications.put(app.getName(), app);
		}
	}

}
