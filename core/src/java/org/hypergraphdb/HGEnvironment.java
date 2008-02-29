/*
 * This file is part of the HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see 
 * the LicensingInformation file at the root level of the distribution. 
 *
 * Copyright (c) 2005-2006
 *  Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb;

import java.io.File;
import java.util.*;

/**
 * <p>
 * This class provides some facilities to manage several open HyperGraph databases
 * within a single virtual machine. This is useful when one needs to access a
 * currently open database by its location.   
 * </p>
 * 
 * <p>
 * The class essentially implements a static map of currently open databases. The
 * general name <code>HGEnvironment</code> reflects the intent to eventually
 * put JVM-wide operations here.  
 * </p>
 * 
 * @author Borislav Iordanov
 */
public class HGEnvironment 
{	
	private static Map<String, HyperGraph> dbs = new HashMap<String, HyperGraph>();
	
	synchronized static void set(String location, HyperGraph graph)
	{
		dbs.put(location, graph);
	}
	
	synchronized static void remove(String location)
	{
		dbs.remove(location);
	}
	
	/**
	 * <p>
	 * Retrieve an already opened or open a HyperGraph instance. Note that a new
	 * database instance will potentially be created via <code>new HyperGraph(location)</code>
	 * if it doesn't already exist.
	 * </p>
	 * 
	 * @param location The location of the HyperGraph instance.
	 * @return The HyperGraph database instance.
	 */
	public synchronized static HyperGraph get(String location) 
	{ 
		char last = location.charAt(location.length() - 1); 
		if (last == '/' || last == '\\')
			location = location.substring(0, location.length() - 1);
		HyperGraph hg = dbs.get(location);
		if (hg == null)
		{
			hg = new HyperGraph(location);
			dbs.put(location, hg);
		}
		else if (!hg.isOpen())
			hg.open(location);
		return hg;
	}
	
	/**
	 * <p>
	 * Same as <code>get</code>, but will return <code>null</code> if there is
	 * no database at that location.
	 * </p>
	 */
	public synchronized static HyperGraph getExistingOnly(String location)
	{
		char last = location.charAt(location.length() - 1); 
		if (last == '/' || last == '\\')
			location = location.substring(0, location.length() - 1);		
		HyperGraph hg = dbs.get(location);
		if (hg == null)
		{
			if (exists(location))
				hg = new HyperGraph(location);
		}
		return hg;
	}
	
	/**
	 * <p>
	 * Return <code>true</code> if there is a HyperGraph database at the given location 
	 * and <code>false</code> otherwise.
	 * </p> 
	 */	
	public synchronized static boolean exists(String location)
	{
		return new File(location, "hgstore_idx_HGATOMTYPE").exists();
	}
	
	/**
	 * <p>
	 * Return <code>true</code> if the database at the given location is already
	 * open and <code>false</code> otherwise.
	 * </p> 
	 */
	public synchronized static boolean isOpen(String location)
	{
		return dbs.containsKey(location);
	}
}
