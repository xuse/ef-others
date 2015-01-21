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
package jef.http.client.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PostMethod extends HttpMethod{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4540828804731606672L;
	protected List<Part> parts = new ArrayList<Part>();
	
	public void addPart(Part part){
		parts.add(part);
	}
	
	public List<Part> getParts() {
		return parts;
	}

	public void setParts(List<Part> parts) {
		this.parts = parts;
	}
	
	public void doMethod(URLConnection conn) throws IOException {
		conn.setDoOutput(true);
		PrintWriter out = new PrintWriter(conn.getOutputStream());
		StringBuilder sb=new StringBuilder();
		for(Part p: parts){
			if(p instanceof FilePart){
				throw new IOException("PostMethod doesn't support upliading a FilePart, please use 'MultipartPostMethod'.");
			}else{
				NormalPart np=(NormalPart) p;
				if(sb.length()>0)sb.append('&');
				sb.append(np.getName()).append('=').append(np.getContent());				
			}
		}
		out.print(sb.toString());
		out.flush();
		out.close();
	}
	
	public long getLength(){
		long n=0;
		for(Part p: parts){
			n+=p.getLength();
		}
		return n;
	}

	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for(Part p: parts){
			if(p instanceof FilePart){
				sb.append("Invalid Part: FilePart");
			}else{
				NormalPart np=(NormalPart) p;
				if(sb.length()>0)sb.append('&');
				sb.append(np.getName()).append('=').append(np.getContent());				
			}
		}
		return sb.toString();
	}
	
}
