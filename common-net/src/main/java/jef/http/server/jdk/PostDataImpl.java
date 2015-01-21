package jef.http.server.jdk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jef.http.server.MultipartStream;
import jef.http.server.PostData;
import jef.jre5support.Headers;
import jef.tools.IOUtils;
import jef.tools.StringUtils;
import jef.tools.string.StringParser;

@SuppressWarnings("restriction")
final class PostDataImpl implements PostData {
	WebExchangeImpl parent;
	private Headers parameters;
	private byte[] rawdata;
	private boolean isRawRequest;
	private List<UploadFile> tmpFiles = new ArrayList<UploadFile>();

	public PostDataImpl(WebExchangeImpl parent) {
		this.parent = parent;
		try {
			init();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void init() throws IOException {
		Map<String, String> contType = asMap(parent.raw.getRequestHeaders().get("Content-Type"));
		InputStream stream = parent.raw.getRequestBody();
		if (contType.containsKey(SIMPLE_FORM_DATA)) {
			processSimpleFormData(stream);
		} else if (contType.containsKey(MULTIPART_FORM_DATA)) {
			MultipartStream ms = new MultipartStream(stream, contType.get("boundary").getBytes(), 4096);
			ms.setHeaderEncoding(parent.getCharset());
			processMultiParts(ms);
		} else {
			this.rawdata = IOUtils.toByteArray(stream);
			this.isRawRequest = false;
		}
	}

	private Map<String, String> asMap(List<String> contTypes) {
		Map<String, String> result = new HashMap<String, String>();
		for(String contType:contTypes){
			String[] args = StringUtils.split(contType, ";");
			for (String cell : args) {
				int n = cell.indexOf("=");
				if (n > -1) {
					result.put(cell.substring(0, n).toLowerCase().trim(), cell.substring(n + 1).trim());
				} else {
					result.put(cell.toLowerCase().trim(), null);
				}
			}			
		}
		return result;
	}

	public List<UploadFile> getFormFiles() {
		return tmpFiles;
	}

	public String getParameter(String string) {
		return parameters.getFirst(string);
	}

	public Map<String, String[]> getParameterMap() {
		return parameters;
	}

	public void close() {
		for(UploadFile file: tmpFiles){
			if(file.getTmpFile().exists())file.getTmpFile().delete();
		}
		tmpFiles.clear();
		parameters.clear();
	}

	private boolean processSimpleFormData(InputStream stream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String s = br.readLine();
		br.close();
		if (s == null) {// 容器已经取走了流数据，参数需要从容器获取
			return false;
		} else {
			String charSet = parent.getCharset();
			String[] keys = StringUtils.split(s, '&');
			for (String key : keys) {
				int n = key.indexOf("=");
				if (n > -1) {
					parameters.add(URLDecoder.decode(key.substring(0, n), charSet), URLDecoder.decode(key.substring(n + 1), charSet));
				} else {
					parameters.add(URLDecoder.decode(key, charSet), null);
				}
			}
			return true;
		}
	}

	private void processMultiParts(MultipartStream ms) throws IOException {
		ms.skipPreamble();
		do {
			String partHead = ms.readHeaders();
			Map<String, String> partHeaders = StringParser.tokeyMaps(StringParser.extractKeywords(partHead, ";:= \n", false), "name", "filename", "Content-Type");
			String name = partHeaders.get("name");
			String contType = partHeaders.get("Content-Type");
			if (name != null) {
				if (contType == null) {// parse Url code content
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ms.readBodyData(out);
					out.flush();
					parameters.add(name, out.toString(parent.getCharset()));
					out.close();
				} else {// save to a file
					String fileName = partHeaders.get("filename");
					if (StringUtils.isNotEmpty(fileName)) {
						File tmpFile = File.createTempFile("~up", "." + StringUtils.substringAfterLast(fileName, "."));
						FileOutputStream out = new FileOutputStream(tmpFile);
						ms.readBodyData(out);
						out.flush();
						out.close();
						UploadFile file = new UploadFile(tmpFile, fileName, partHeaders.get("name"));
						tmpFiles.add(file);
					}
				}
			}
		} while (ms.readBoundary());
	}

	public byte[] getRawdata() {
		return rawdata;
	}

	public boolean isRawRequest() {
		return isRawRequest;
	}
}
