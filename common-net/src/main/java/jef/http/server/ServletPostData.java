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
package jef.http.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import jef.http.client.DLHelper;
<<<<<<< HEAD
import jef.net.Headers;
=======
import jef.inner.sun.Headers;
>>>>>>> 83fdddc646e6b87855bb33b18a749b3eda1ada60
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.string.StringParser;

/**
 * 描述和解析POST请求当中的数据
 * @author jiyi
 *
 */
final class ServletPostData implements PostData{

	
	private WebExchange parent;
	
	private Headers parameters=new Headers(10,false);//All parameters in URL & BODY, if duplicate, keep inf from body.
	
	private List<UploadFile> tmpFiles=new ArrayList<UploadFile>();
	
	private byte[] rawdata;
	
	private boolean formData=true;
	
	public ServletPostData(HttpServletRequest req,WebExchange parent,boolean ignorCase) throws IOException{
		this.parent=parent;
		init(DLHelper.getHeaders(req), req);
		
		Headers urlParam=DLHelper.getParamsInUrl(req.getQueryString(),ignorCase);
		for(String key:urlParam.keySet()){
			if(!this.parameters.containsKey(key)){//采用补充法填入，一旦冲突则不填入
				this.parameters.put(key,urlParam.get(key));
			}
		}
	}
	
	private void init(Headers headers, HttpServletRequest req) throws IOException {
		Map<String,String> contType=headers.getAsMap("Content-Type");
		if(contType==null){
			return;
		}
		InputStream stream = req.getInputStream();
		if(contType.containsKey(SIMPLE_FORM_DATA)){
			if(!processSimpleFormData(stream)){
				Map<String,String[]> map=req.getParameterMap();
				for(Entry<String,String[]> e:map.entrySet()){
					parameters.put(e.getKey(), e.getValue());
				}
			}
		}else if(contType.containsKey(MULTIPART_FORM_DATA)){
			MultipartStream ms=new MultipartStream(stream, contType.get("boundary").getBytes(), 4096);
			ms.setHeaderEncoding(parent.getCharset());
			processMultiParts(ms);
		}else{
			this.rawdata=IOUtils.toByteArray(stream);
			this.formData=false;
		}
	}
	
	private boolean processSimpleFormData(InputStream stream) throws IOException{
		BufferedReader br=new BufferedReader(new InputStreamReader(stream));
		String s=br.readLine();
		br.close();
		if(s==null){//容器已经取走了流数据，参数需要从容器获取
			return false;
		}else{
			String charSet=parent.getCharset();
			String[] keys=StringUtils.split(s,'&');
			for(String key:keys){
				int n=key.indexOf("=");
				if(n>-1){
					parameters.add(URLDecoder.decode(key.substring(0,n), charSet),URLDecoder.decode(key.substring(n+1), charSet));
				}else{
					parameters.add(URLDecoder.decode(key, charSet),null);
				}
			}
			return true;
		}
	}
	
	private void processMultiParts(MultipartStream ms) throws IOException {
		ms.skipPreamble();
		do{
			String partHead=ms.readHeaders();
			Map<String,String> partHeaders=StringParser.tokeyMaps(StringParser.extractKeywords(partHead,";:= \n",false),"name","filename","Content-Type");
			String name=partHeaders.get("name");
			String contType=partHeaders.get("Content-Type");
			if(name!=null){
				if(contType==null){//parse Url code content
					ByteArrayOutputStream out=new ByteArrayOutputStream();
					ms.readBodyData(out);
					out.flush();
					parameters.add(name, out.toString(parent.getCharset()));
					out.close();
				}else{//save to a file
					String fileName=partHeaders.get("filename");
					if(StringUtils.isNotEmpty(fileName)){
						File tmpFile=File.createTempFile("~up", "."+StringUtils.substringAfterLast(fileName, "."));
						FileOutputStream out=new FileOutputStream(tmpFile);  
						ms.readBodyData(out);
						out.flush();
						out.close();
						UploadFile file=new UploadFile(tmpFile,fileName,partHeaders.get("name"));
						tmpFiles.add(file);
					}
				}
			}
		}while (ms.readBoundary());
	}

	/**
	 * 删除上传的临时文件，释放所有数据
	 */
	public void destroy() {
		for(UploadFile file: tmpFiles){
			if(file.getTmpFile().exists())file.getTmpFile().delete();
		}
		tmpFiles.clear();
		parameters.clear();
	}
	
	@SuppressWarnings("unused")
	private Object rapper=new Object(){
		protected void finalize() throws Throwable {
			destroy();
			super.finalize();
		}
	};
	
	public Headers getParameters() {
		return parameters;
	}

	public List<UploadFile> getFormFiles() {
		return tmpFiles;
	}
	
	public String[] getParameterValues(String key){
		String[] str=parameters.get(key);
		return str;
	}
	
	public String getParameter(String key) {
		return this.parameters.getFirstIgnorCase(key);
	}

	public Map<String, String[]> getParameterMap() {
		return parameters;
	}
	
	public boolean isFormData() {
		return formData;
	}

	public String getRawDataAsString() {
		if(formData){
			throw new IllegalArgumentException("This is a form request, can not get Raw data");
		}
		try {
			return new String(rawdata,parent.getCharset());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public byte[] getRawData() {
		if(formData){
			throw new IllegalArgumentException("This is a form request, can not get Raw data");
		}
		return rawdata;
	}
}

