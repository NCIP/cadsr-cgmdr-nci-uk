/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

package org.exist.client.xacml;

public abstract class AbstractTreeNode implements XACMLTreeNode
{
	private NodeContainer parent;
	private boolean modified;

	private AbstractTreeNode() {}
	protected AbstractTreeNode(NodeContainer parent)
	{
		this.parent = parent;
		this.modified = true;
	}
	public NodeContainer getParent()
	{
		return parent;
	}
	public boolean isModified(boolean deep)
	{
		return modified;
	}
	public void revert(boolean deep)
	{
		modified = false;
		fireChanged();
	}
	public void commit(boolean deep)
	{
		modified = false;
	}
	public void setModified(boolean flag)
	{
		modified = flag;
		fireChanged();
	}
	protected void fireChanged()
	{
		if(parent != null)
			parent.nodeChanged(this);
	}
}
