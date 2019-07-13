package jef.ejb.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import jef.tools.Exceptions;
import jef.tools.IOUtils;
import jef.tools.ZipUtils;

public class OpenejbServer {

	public static void main(String[] args){
		
		System.out.println("projectFolderPath   "+args[0]);
		System.out.println("openEjbFolderPath  "+args[1]);
		String projectFolderPath=args[0];
		String openEjbFolderPath=args[1];
		File openEjbFolder=new File(openEjbFolderPath);
		File projectFolder=new File(projectFolderPath);
		OpenejbServer server=new OpenejbServer();
		server.run(openEjbFolder,projectFolder);
	}

	protected void run(File openEjbFolder, File projectFolder) {
		try {
			prepareReource(projectFolder);
			prepareConf(projectFolder,openEjbFolder);
			runOpenEjbServer(projectFolder,openEjbFolder);
		} catch (Exception e) {
			Exceptions.log(e);
		}
	}
	
	private void runOpenEjbServer(File projectFolder, File openEjbFolder) throws IOException {
		File confFile=getEjbConfFile(projectFolder);
		File binFolder=new File(openEjbFolder, "bin");
		File customerCommandFile=new File(openEjbFolder, "customer.command");
		String command=null;
		if (customerCommandFile.exists()){
			command=IOUtils.asString(new FileInputStream(customerCommandFile));
		}
		if (command==null || command.trim().length()==0){
			if (isWindowOs())
				command="cmd /k start "+binFolder.getAbsolutePath()+"\\openejb start "+"--conf="+confFile.getAbsolutePath();
			else
				command="/bin/sh gnome-terminal -e \""+binFolder.getAbsolutePath()+"/openejb start "+"--conf="+confFile.getAbsolutePath()+"\"";
		}else{
			command=command+" "+binFolder.getAbsolutePath()+"/openejb start "+"--conf="+confFile.getAbsolutePath();
		}
		System.out.println(command);
		Runtime runtime = Runtime.getRuntime();
		runtime.exec(command);
	}
	
	public void prepareReource(File projectFolder) throws Exception{
		//先把文件 /target/name-version.ear拷贝到/target/ejbapp/debug.ear
		System.out.println("start to copy ear file");
		File ejbAppFolder=getEjbAppFolder(projectFolder);
		File earDebugFile=new File(ejbAppFolder, "debug.ear");
		if (earDebugFile.exists())
			earDebugFile.delete();
		File targetFolder=new File(projectFolder, "target");
		File[] earFiles=targetFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".ear");
			}
		});
		if (earFiles==null || earFiles.length==0){
			throw new FileNotFoundException("Can't find any ear files");
		}
		if (earFiles.length>1){
			throw new IllegalAccessError("only 1 ear file is need, but found "+earFiles.length);
		}
		File srcEarFile=earFiles[0];
		ZipUtils.unzip(srcEarFile.getAbsolutePath(), earDebugFile.getAbsolutePath());
		System.out.println("end to copy ear file");
	}
	
	private void prepareConf(File projectFolder, File openEjbFolder) throws IOException{
		File ejbDevConf=getEjbConfFile(projectFolder);
		if (!ejbDevConf.exists()){
			File confFolder=new File(openEjbFolder, "conf");
			File sourceConfFile=new File(confFolder, "openejb.xml");
			String ejbConf=IOUtils.asString(sourceConfFile, "utf-8");
			File ejbAppFolder=getEjbAppFolder(projectFolder);
			ejbConf=ejbConf.replace("dir=\"apps/\"", "dir=\""+ejbAppFolder.getAbsolutePath()+"\"");
			IOUtils.saveAsFile(ejbDevConf, ejbConf);
		}
	}
	
	
	
	private File getEjbAppFolder(File projectFolder){
		File classTargetFolder=new File(projectFolder,"target");
		File appFolder=new File(classTargetFolder, "ejbapp");
		if (!appFolder.exists())
			appFolder.mkdir();
		return appFolder;
	}
	
	private File getEjbConfFile(File projectFolder){
		File classTargetFolder=new File(projectFolder,"target");
		File ejbDevConf=new File(classTargetFolder, "ejbDevConf.xml");
		return ejbDevConf;
	}

	private boolean isWindowOs(){
		String os=System.getProperty("os.name");
		return os.startsWith("win") || os.startsWith("Win");
	}
}
