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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jef.common.log.LogUtil;
import jef.storage.FileManager.JFileInRecycleBin;
import jef.storage.common.Storage;
import jef.storage.common.StorageConfigurations;
import jef.tools.IOUtils;
import jef.tools.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

final class XMLHelper {
	private static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>(){
		protected DateFormat initialValue() {
			return new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss SSS"));
		}
	};

	/**
	 * 新产生一份xml文档
	 * @throws IOException 
	 */
	static void generateXML(String folder,String creatTimeString) throws IOException {
		Document document=XMLUtils.newDocument();
		Element rootElement = XMLUtils.addElement(document,"FileLog");

		Element DirectoryElement = XMLUtils.addElement(rootElement,Storage.STORAGE_TYPE_ZIP);
		DirectoryElement.setAttribute("createTime",creatTimeString);
		//DirectoryElement.setTextContent("本文夹所拥有的文件");
		XMLUtils.addComment(DirectoryElement,"==== Files following is zipped!====");

		Element DirectoryElement2 = XMLUtils.addElement(rootElement,Storage.STORAGE_TYPE_ORIGINAL);
		DirectoryElement2.setAttribute("createTime", creatTimeString);
		//DirectoryElement2.setTextContent("本文夹所拥有的文件");
		XMLUtils.addComment(DirectoryElement2,"==== Files flowing is not zipped!====");

		String logPath=folder + File.separator + StorageConfigurations.fileLogName;
		XMLUtils.saveDocument(document, new File(logPath));
	}

	/**
	 * 在指定的xml文档上，新增一个文件节点
	 */
	static void addFileLogElement(JFile fileDO) {
		String logPath=fileDO.getStorageFolder() + File.separator + StorageConfigurations.fileLogName;
		try {
			Document document = XMLUtils.loadDocument(new File(logPath));
			Element root = document.getDocumentElement();
			String type = fileDO.getStorageType();

			Element fileElement = XMLUtils.addElement(XMLUtils.first(root,type),"File");
			fileElement.setAttribute("ID", fileDO.getUuid());
			fileElement.setAttribute("createTime", dateFormat.get().format(fileDO.getCreateTime()));
			XMLUtils.addElement(fileElement,"FileRealName").setTextContent(fileDO.getRealName());
			XMLUtils.addElement(fileElement,"StorageDirectory").setTextContent(fileDO.getStorageFolder());
			XMLUtils.addElement(fileElement,"Description").setTextContent(fileDO.getDescription());
			XMLUtils.saveDocument(document, new File(logPath));
		} catch (Exception e) {
			LogUtil.exception(e);
		}
	}

	static List<String> getFilesNotExistInXml(List<JFile> restoredFdos){
		List<String> result = new ArrayList<String>();
		HashMap<String,String> map=new HashMap<String,String>();
		for(JFile fdo:restoredFdos){
			map.put(fdo.getUuid(), fdo.getUuid());
		}

		if(restoredFdos.isEmpty())return result;
		File dir=new File(restoredFdos.get(0).getStorageFolder());
		for(File file: dir.listFiles()){
			if(file.isFile() && !file.getName().endsWith(".xml")){
				if(!map.containsKey(IOUtils.removeExt(file.getName()))){
					result.add(file.getName());
				}
			}
		}
		return result;
	}

	static List<JFile> restoreFilesFromXml(File xmlFile, String appCode) {
		List<JFile> fdoToReturn = new ArrayList<JFile>();
		List<Element> eleToRemove = new ArrayList<Element>();
		try {
			Document document = XMLUtils.loadDocument(xmlFile);
			Element root = document.getDocumentElement();
			Element zipFiles = XMLUtils.first(root, Storage.STORAGE_TYPE_ZIP);
			NodeList nds = zipFiles.getElementsByTagName("File");
			processElements(nds, Storage.STORAGE_TYPE_ZIP, fdoToReturn, eleToRemove, appCode);
			Element normalFiles = XMLUtils.first(root, Storage.STORAGE_TYPE_ORIGINAL);
			nds = normalFiles.getElementsByTagName("File");
			processElements(nds, Storage.STORAGE_TYPE_ORIGINAL, fdoToReturn, eleToRemove, appCode);
			// 清除已经无效的文件链接
			if (!eleToRemove.isEmpty()) {
				for (Element e : eleToRemove) {
					e.getParentNode().removeChild(e);
					LogUtil.show("File ID:" + e.getAttribute("ID") +" file content was deleted. clearing record in XML.");
				}
				XMLUtils.saveDocument(document, new File(xmlFile.getCanonicalPath()));
			}
		} catch (IOException e) {
			LogUtil.exception(e);
		} catch (ParseException e) {
			LogUtil.exception(e);
		} catch (SAXException e) {
			LogUtil.exception(e);
		}
		return fdoToReturn;
	}
	
	private static void processElements(NodeList nds, String storageType, List<JFile> fdoToReturn, List<Element> eleToRemove,String appCode) throws ParseException {
		for(int i=0;i<nds.getLength();i++){
			Element e= (Element) nds.item(i);
			JFile fdo=new JFile();
			fdo.setApplicationCode(appCode);
			fdo.setCreateTime(dateFormat.get().parse(e.getAttribute("createTime")));
			fdo.setDescription(XMLUtils.nodeText(e,"description"));
			fdo.setStorageFolder(XMLUtils.nodeText(e,"StorageDirectory"));
			fdo.setRealName(XMLUtils.nodeText(e,"FileRealName"));
			fdo.setStorageType(storageType);
			fdo.setUuid(e.getAttribute("ID"));
			File tmpFile=new File(fdo.getStroagePath());
			if(tmpFile.exists() && tmpFile.isFile()){
				fdoToReturn.add(fdo);
			}else{
				eleToRemove.add(e);
			}
		}
	}

	/**
	 * 删除一个JFile节点
	 * @param fileName
	 */
	public static void removeFileLogElement(String folder,String type, String uuid) {
		String logPath=folder + File.separator + StorageConfigurations.fileLogName;
		try {
			Document document = XMLUtils.loadDocument(new File(logPath));
			Element root = XMLUtils.first(document.getDocumentElement(),type);
			NodeList nds=root.getElementsByTagName("File");
			
			Element fileElement =null;
			for(int i=0;i<nds.getLength();i++){
				Element e= (Element) nds.item(i);
				if(e.getAttribute("ID").equals(uuid)){
					fileElement=e;
					break;
				}
			}
			if (fileElement != null) {
				root.removeChild(fileElement);
				XMLUtils.saveDocument(document, new File(logPath));
			}
		} catch (Exception e) {
			LogUtil.exception(e);
		}
	}

	/**
	 * 添加回收站记录
	 * @param fileDO
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void addRecycleBinLog(JFileInRecycleBin fileDO) throws SAXException, IOException {
		File logFile=new File(fileDO.getRecycleBinFolder()+"/"+StorageConfigurations.fileLogName);
		Document document=null;
		if(!logFile.exists()){
			document=XMLUtils.newDocument();
			XMLUtils.addElement(document,"RecycleBin");
		}else{
			document = XMLUtils.loadDocument(logFile);	
		}
		Element rootElement = document.getDocumentElement();
		Element fileElement=XMLUtils.addElement(rootElement, "File");
		
		fileElement.setAttribute("ID", fileDO.getUuid());
		fileElement.setAttribute("createTime", dateFormat.get().format(fileDO.getCreateTime()));
		fileElement.setAttribute("deleteTime", dateFormat.get().format(fileDO.getDeleteTime()));
		XMLUtils.addElement(fileElement,"FileRealName", fileDO.getRealName());
		XMLUtils.addElement(fileElement,"StorageDirectory", fileDO.getStorageFolder());
		XMLUtils.addElement(fileElement,"Description", fileDO.getDescription());
		XMLUtils.addElement(fileElement,"StorageType", fileDO.getStorageType());
		
		XMLUtils.saveDocument(document, logFile);
	}

	/**
	 * 删除回收站的记录
	 * @param fileDO
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void removeRecycleBinLog(JFileInRecycleBin fileDO) throws SAXException, IOException {
		File logFile=new File(fileDO.getRecycleBinFolder()+"/"+StorageConfigurations.fileLogName);
		Document document=null;
		if(!logFile.exists())return;
		
		document = XMLUtils.loadDocument(logFile);	
		
		Element rootElement = document.getDocumentElement();
		NodeList nds=rootElement.getElementsByTagName("File");
		Element fileElement =null;
		for(int i=0;i<nds.getLength();i++){
			Element e= (Element) nds.item(i);
			if(e.getAttribute("ID").equals(fileDO.getUuid())){
				fileElement=e;
				break;
			}
		}
		if (fileElement != null) {
			rootElement.removeChild(fileElement);
			XMLUtils.saveDocument(document, logFile);
		}
	}
}
