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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import jef.tools.Assert;
import jef.tools.ResourceUtils;
import jef.tools.maven.MavenDependencyParser;

public class MavenClassloader extends URLClassLoader  {
	private File pomFile;
	
	public MavenClassloader(URL[] urls,ClassLoader ploader) {
		super(urls,ploader);
		init();
	}
	public MavenClassloader(URL... urls) {
		super(urls);
		init();
	}
	
	private void init() {
		pomFile=new File(System.getProperty("user.dir"),"pom.xml");
		Assert.isTrue(pomFile.exists(),"Can not locate pom.xml file!");
		List<File> files=MavenDependencyParser.parseDependency(pomFile);
		for(File jar:files){
			System.out.println("loading "+ jar.getAbsolutePath()+" ...");
			super.addURL(ResourceUtils.fileToURL(jar));
		}
	}
}
