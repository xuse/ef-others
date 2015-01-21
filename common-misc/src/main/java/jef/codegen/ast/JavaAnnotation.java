package jef.codegen.ast;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class JavaAnnotation implements JavaElement{
	String name;
	Map<String,Object> properties=new HashMap<String,Object>();
	
	public JavaAnnotation(Class<? extends Annotation> clz){
		this.name=clz.getName();
	}
	
	public JavaAnnotation(String name){
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	public void put(String key,Object value){
		properties.put(key, value);
	}
	public String toCode(JavaUnit main) {
		StringBuilder sb=new StringBuilder();
		sb.append("@").append(main.getJavaClassName(name));
		if(properties.size()>0){
			sb.append("(");
			int n=0;
			for(String key: properties.keySet()){
				Object v=properties.get(key);
				if(v==null)continue;
				if(n>0)sb.append(",");
				sb.append(key).append("=");
				if(v instanceof CharSequence){
					sb.append("\"").append((String)v).append("\"");
				}else{
					sb.append(String.valueOf(v));
				}
				n++;
			}
			sb.append(")");
		}
		return sb.toString();
	}
	public void buildImport(JavaUnit javaUnit) {
	}
	
}
