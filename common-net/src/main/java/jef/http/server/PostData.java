package jef.http.server;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PostData {
	static final String SIMPLE_FORM_DATA = "application/x-www-form-urlencoded";
	static final String MULTIPART_FORM_DATA = "multipart/form-data";
	
	List<UploadFile> getFormFiles();

	String getParameter(String string);

	Map<String, String[]> getParameterMap();


}
