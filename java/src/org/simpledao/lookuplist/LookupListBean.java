package org.simpledao.lookuplist;

public class LookupListBean
{
	private String description = null;
	private int id ;

	public LookupListBean(int id, String name)
	{
		this.id = id;
		this.description = name;
	}

	public LookupListBean() {}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}}
