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

import java.util.List;

import jef.common.wrapper.Backup;
import jef.common.wrapper.Paginator;
import jef.tools.StringUtils;
import jef.ui.ConsoleConversation;
import jef.ui.ConsoleShell;

public abstract class AbstractEditConversation<T> extends ConsoleConversation<String>{
	Paginator<T> p;
	protected abstract boolean delete(T toDel);
	protected abstract void callSave();
	protected abstract void load();
	protected abstract boolean edit(T host);
	protected abstract void createNew() ;
	protected abstract void endConversation();
	public AbstractEditConversation(ConsoleShell app) {
		super(app);
	}

	
	protected final void execute() {
		boolean loop = true;
		List<T> hs = list();
		while (loop) {
			String command = getInput("命令 1-" + hs.size() + ":编辑 goto:跳页 next:下页 prev:上页 ls:列出  add:添加  del:删除 quit:退 save:存退");
			if (command.equalsIgnoreCase("add")||command.equalsIgnoreCase("a")) {
				createNew();
			} else if (command.equalsIgnoreCase("del")||command.equalsIgnoreCase("d")) {
				doDel(hs);
			} else if (command.equalsIgnoreCase("next") || command.equalsIgnoreCase("n")) {
				p.nextPage();
				hs =list();
			} else if (command.equalsIgnoreCase("prev") || command.equalsIgnoreCase("p")) {
				p.prevPage();
				hs =list();
			} else if (command.equalsIgnoreCase("goto") || command.equalsIgnoreCase("g")) {
				String nextPage=getInput("跳到：");
				int next=StringUtils.toInt(nextPage, p.getCurrentPage());
				p.setPage(next);
				hs=list();
			} else if (command.equalsIgnoreCase("ls")||command.equalsIgnoreCase("l")) {
				hs = list();
			} else if (command.equalsIgnoreCase("quit")||command.equalsIgnoreCase("q")) {
				loop = false;
			} else if (command.equalsIgnoreCase("save")||command.equalsIgnoreCase("s")) {
				callSave();
				loop = false;
			} else if (StringUtils.isNumeric(command)) {//编辑
				doEdit(command,hs);
			} else {
				prompt("Unknown command:" + command);
			}
		}
		endConversation();
	}

	protected void doDel(List<T> hs) {
		int order=getInputInt("要删除的序号:");
		T toDel = getOrder(hs,order);
		if (toDel != null) {
			if(!delete(toDel)){
				prompt("删除失败，对象不存在：" + toDel.toString());
			}
			load();
		}
	}
	private T getOrder(List<T> hs, int index) {
		if(index>hs.size())return null;
		if(index<1)return null;
		return hs.get(index-1);
	}
	private void doEdit(String command,List<T> hs) {
		int order=StringUtils.toInt(command, -1);
		T host=getOrder(hs,order);
		if(host!=null){
			Backup<T> b=new Backup<T>(host);
			if(!edit(host)){
				b.restore();
				prompt("更改已放弃。");
			}
		}
	}

	/**
	 * 列表
	 * @return
	 */
	private List<T> list() {
		if(p==null){
			load();
		}
		List<T> hs = p.get(p.getCurrentPage());
		for (int i = 0; i < hs.size(); i++) {
			T pf = hs.get(i);
			String msg = (i + 1) + " " + pf.toString();
			prompt(msg);
		}
		prompt("Records: " +p.getPageLimit() +"/"+ p.getTotal()+ "   Page:" + p.getCurrentPage() + "/" + p.getTotalPage());
		return hs;
	}

	

}
