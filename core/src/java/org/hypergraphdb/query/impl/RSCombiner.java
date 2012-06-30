/* 
 * This file is part of the HyperGraphDB source distribution. This is copyrighted 
 * software. For permitted uses, licensing options and redistribution, please see  
 * the LicensingInformation file at the root level of the distribution.  
 * 
 * Copyright (c) 2005-2010 Kobrix Software, Inc.  All rights reserved. 
 */
package org.hypergraphdb.query.impl;

import org.hypergraphdb.HGSearchResult;

public interface RSCombiner<T>  extends HGSearchResult<T>
{
	void init(HGSearchResult<T> l, HGSearchResult<T> r);
	
	void reset();
}
