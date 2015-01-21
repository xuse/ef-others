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
package jef.http.client.ui;

import org.apache.commons.lang.StringUtils;

import jef.common.wrapper.Paginator;
import jef.http.client.CookieManager;
import jef.http.client.CookieManager.Domain;
import jef.ui.ConsoleShell;

public class CookieEditConversation extends AbstractEditConversation<Domain> {
	CookieManager cm;
	
	public CookieEditConversation(ConsoleShell app,CookieManager cm) {
		super(app);
		this.cm=cm;
	}

	
	protected void callSave() {
		if (cm != null ) {
			cm.save();
		}
	}

	
	protected void createNew() {
		String name=getInput("Cookie name:");
		Domain host = new Domain(name);
		edit(host);
	}

	
	protected boolean delete(Domain toDel) {
		cm.removeCookie(toDel.getName());
		return true;
	}

	
	protected boolean edit(Domain domain) {
		prompt(domain.getName());
		prompt(domain.getRootCookie());
		String newCookie=getInput("New Cookie:");
		if(StringUtils.isNotEmpty(newCookie)){
			cm.setCookie(domain.getName(), newCookie, true);
			return true;
		}else{
			return false;
		}
	}

	
	protected void load() {
		int cutPage = -1;
		if(p!=null)cutPage=p.getCurrentPage();
		Domain[] s=cm.getDomains().values().toArray(new Domain[0]);
		p= new Paginator<Domain>(s,10);	
		if(cutPage>-1)p.setPage(cutPage);
	}

	
	protected void endConversation() {
	}
}
