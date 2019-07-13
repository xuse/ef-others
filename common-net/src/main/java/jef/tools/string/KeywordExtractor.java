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
package jef.tools.string;

import java.util.ArrayList;
import java.util.List;

import jef.tools.Exceptions;
import jef.tools.StringUtils;

/**
 * 基于分隔符的分词器，关键在于对引号的处理和对关键字的保留，适合CSV等一些语句的语法解析
 * @author jiyi
 */
public class KeywordExtractor{
	private String ctrlChars;
	private boolean uppercaseNonQuot=false;
	private String[] holdKeys=null;
	private boolean holdKeyIgnoreCase=false;
	private boolean keepQuot=false;
	private char quot1='"';
	private char quot2='"';

	public  List<String> extract(String p){
		List<String> list=extractKeywords(p);
		if(holdKeys==null) return list;
		return holdKeywords(list);
	}

	private List<String> holdKeywords(List<String> list) {
		for(String key:holdKeys){
			List<String> result=new ArrayList<String>();
			String[] keyCombo=splitByKey(key).toArray(new String[]{});
			for(int i=0;i<list.size();i++){
				String tmp=list.get(i);
				if(tmp.length()==0)continue;
				boolean flag=false;
				if(holdKeyIgnoreCase){
					if(keyCombo[0].equalsIgnoreCase(tmp) && i+keyCombo.length<=list.size()){
						flag=true;
						for(int x=1;x<keyCombo.length;x++){
							if(!keyCombo[x].equalsIgnoreCase(list.get(i+x))){
								flag=false;
								break;
							}
						}
					}
				}else{
					if(keyCombo[0].equals(tmp) && i+keyCombo.length<=list.size()){
						flag=true;
						for(int x=1;x<keyCombo.length;x++){
							if(!keyCombo[x].equals(list.get(i+x))){
								flag=false;
								break;
							}
						}
					}
				}
				if(flag){//found keywords
					result.add(key);
					for(int x=1;x<keyCombo.length;x++){
						list.set(i+x,"");
					}
				}else{
					result.add(tmp);
				}
			}

			list=result;
		}
		return list;
	}

	public KeywordExtractor(String ctrlChar){
		this.ctrlChars=ctrlChar;
	}
	
	public void setKeepQuot(boolean keepQuot) {
		this.keepQuot = keepQuot;
	}
	public void setHoldKeyIgnoreCase(boolean holdKeyIgnoreCase) {
		this.holdKeyIgnoreCase = holdKeyIgnoreCase;
	}

	public void setHoldKeys(String[] holdKeys) {
		this.holdKeys = holdKeys;
	}

	public void setQuot1(char quot1) {
		this.quot1 = quot1;
	}

	public void setQuot2(char quot2) {
		this.quot2 = quot2;
	}

	public void setUppercaseNonQuot(boolean uppercaseNonQuot) {
		this.uppercaseNonQuot = uppercaseNonQuot;
	}
	
	public List<String> extractKeywords(String p) {
		List<String> items=new ArrayList<String>();
		showItem(p, items);
		return items;
	}

	private void showItem(String s, List<String> items){
		//if(s.length()==0)return;
		int m=getItem(s,items);
		if(m>=s.length())return;
		showItem(s.substring(m+1),items);
	}

	private int getItem(String s, List<String> items){
		try{
			int x=-1;
			int n=-1;
			int m=-1;
			for(int j=0; j<s.length(); j++){// find first effect char
				char c=s.charAt(j);
				if(ctrlChars.indexOf(c)==-1){
					x=j;
					break;
				}
			}
			//find first ""
			if(quot1<=0){
				n=-1;
			}else{
				n=s.indexOf(quot1);	
			}
			//find second quot
			if(quot2<=0){
				m=-1;
			}else{
				if(n>-1){
					for(int j=n+1; j<s.length(); j++){
						char c=s.charAt(j);
						if(c==quot2){
							m=j;
							break;
						}
					}
				}	
			}
			
			if(x<n||n==-1){//if no " or first keyword is not wrap by "
				if(x==-1)return s.length();
				if(n==-1)n=s.length();
				m=-1;
				for(int j=n-1;j>=x;j--){
					char c=s.charAt(j);
					if(ctrlChars.indexOf(c)==-1){
						m=j;
						break;
					}
				}
				if(m==-1)m=n-1;
				if(x>-1)items.addAll(splitByKey(s.substring(x,m+1)));
				return n-1;
			}else{
				if(keepQuot){
					items.add(quot1+s.substring(n+1,m)+quot2);
				}else{
					items.add(s.substring(n+1,m));
				}
				
				return m;
			}
		}catch(Exception e){
			Exceptions.log(e);
			return s.length();
		}
	}

	private List<String> splitByKey(String string) {
		List<String> l=new ArrayList<String>();
		char to=quot1;
		for(int n=0;n<ctrlChars.length();n++){
			char c=ctrlChars.charAt(n);
			if(c!=to)string=string.replace(c, to);			
		}
		String[] tmp=StringUtils.split(string, to);
		for(String s: tmp){
			if(s.length()>0){
				if(uppercaseNonQuot){
					l.add(s.toUpperCase());
				}else{
					l.add(s);
				}
				
			}
		}
		return l;
	}
}
