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
package jef.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jef.tools.StringUtils;

import org.apache.commons.lang.ObjectUtils;

public class TreeNode {
	
	public interface LazyInitCallback{
		void initChildren(TreeNode parent);
	}
	
	private static final TreeNode[] EMPTY=new TreeNode[0];
	
	private List<TreeNode> children;
	private TreeNode parent;
	protected Object value;
	private LazyInitCallback lazyCall;

	/**
	 * Constructs a new instance of <code>TreeNode</code>.
	 * 
	 * @param value
	 *            The value held by this node; may be anything.
	 */
	public TreeNode(final Object value) {
		this.value = value;
	}
	
	public boolean equals(final Object object) {
		if (object instanceof TreeNode) {
			return ObjectUtils.equals(this.value, ((TreeNode) object).value);
		}
		return false;
	}

	public TreeNode[] getChildren() {
		if (children ==null && lazyCall!=null) {
			lazyCall.initChildren(this);
		}
		if(children==null)return EMPTY;
		return children.toArray(EMPTY);
	}

	/**
	 * Returns the parent node.
	 * 
	 * @return The parent node; may be <code>null</code> if there are no
	 *         parent nodes.
	 */
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * Returns the value held by this node.
	 * 
	 * @return The value; may be anything.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Returns whether the tree has any children.
	 * 
	 * @return <code>true</code> if its array of children is not
	 *         <code>null</code> and is non-empty; <code>false</code>
	 *         otherwise.
	 */
	public boolean hasChildren() {
		boolean flag=children != null && children.size() > 0;
		if(!flag && this.lazyCall!=null)return true;
		return flag;
	}
	
	public int hashCode() {
		return ObjectUtils.hashCode(value);
	}

	/**
	 * Sets the children for this node.
	 * 
	 * @param children
	 *            The child nodes; may be <code>null</code> or empty. There
	 *            should be no <code>null</code> children in the array.
	 */
	public void setChildren(final TreeNode[] children) {
		this.children =Arrays.asList(children);
	}

	/**
	 * Sets the parent for this node.
	 * 
	 * @param parent
	 *            The parent node; may be <code>null</code>.
	 */
	public void setParent(final TreeNode parent) {
		this.parent = parent;
	}
	public String toString() {
		return StringUtils.toString(value);
	}
	public TreeNode createChild(Object o) {
		TreeNode child = new TreeNode(o);
		if(children==null)children=new ArrayList<TreeNode>();
		children.add(child);
		child.setParent(this);
		return child;
	}
	
	public void removeChildren() {
		if(children!=null)
			children.clear();
	}

	public boolean removeChild(TreeNode child) {
		if(children!=null)
			return children.remove(child);
		return false;
	}
	
	public void setValue(Object newUser) {
		this.value = newUser;
	}

	public LazyInitCallback getLazyCall() {
		return lazyCall;
	}

	public void setLazyCall(LazyInitCallback lazyCall) {
		this.lazyCall = lazyCall;
	}
}

