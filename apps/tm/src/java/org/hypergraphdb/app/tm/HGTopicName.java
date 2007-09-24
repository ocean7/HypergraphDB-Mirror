package org.hypergraphdb.app.tm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.atom.HGRel;
import org.tmapi.core.Locator;
import org.tmapi.core.MergeException;
import org.tmapi.core.TMAPIException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicName;
import org.tmapi.core.Variant;

public class HGTopicName extends HGScopedObject implements TopicName
{
	String value;

	public HGTopicName()
	{
	}
	
	private void addVariant(HGVariant v, Collection scope)
	{
		graph.getTransactionManager().beginTransaction();
		try
		{
			HGHandle h = graph.add(v);
			v.graph = graph;
			if (scope != null)
			{
				for (Iterator i = scope.iterator(); i.hasNext(); )
				{
					Topic t = (Topic)i.next();
					graph.add(new HGRel(HGTM.ScopeOf, 
										new HGHandle [] {h, graph.getHandle(t)}), 
							  HGTM.hScopeOf);
				}
			}
			graph.add(new HGRel(HGTM.VariantOf, new HGHandle[] {h, graph.getHandle(this)}), 
					  HGTM.hVariantOf);
			graph.getTransactionManager().commit();
		}
		catch (RuntimeException ex)
		{
			graph.getTransactionManager().abort();
			throw ex;
		}		
	}	
	public Variant createVariant(String value, Collection scope)
	{
		HGVariant v = new HGVariant();
		v.setValue(value);
		addVariant(v, scope);
		return v;
	}

	public Variant createVariant(Locator dataType, Collection scope)
	{
		HGVariant v = new HGVariant();
		v.setDataType(dataType);
		addVariant(v, scope);
		return v;
	}

	public Variant createVariant(String value, Locator dataType, Collection scope)
	{
		HGVariant v = new HGVariant();
		v.setValue(value);
		v.setDataType(dataType);
		addVariant(v, scope);
		return v;		
	}
	
	@HGIgnore
	public Topic getReifier()
	{
		HGHandle h = U.getReifierOf(graph, graph.getHandle(this)); 
		return h != null ? (Topic)graph.get(h) : null;
	}
	
	@HGIgnore
	public void setReifier(Topic topic)
	{
		U.setReifierOf(graph, graph.getHandle(this), graph.getHandle(topic));
	}
	
	public Topic getTopic()
	{
		return (Topic)U.getOneRelated(graph, HGTM.hNameOf, graph.getHandle(this), null);
	}

	@HGIgnore
	public Topic getType()
	{
		HGHandle h = U.getTypeOf(graph, graph.getHandle(this));		
		return h != null ? (Topic)graph.get(h) : null;
	}

	public String getValue()
	{
		return value;
	}

	public Set<Variant> getVariants()
	{
		return U.getRelatedObjects(graph, HGTM.hVariantOf, null, graph.getHandle(this));
	}

	@HGIgnore
	public void setType(Topic type) throws UnsupportedOperationException, MergeException
	{
		U.setTypeOf(graph, graph.getHandle(this), graph.getHandle(type));
	}

	public void setValue(String value) throws MergeException
	{
		this.value = value;
	}
	
	public void remove() throws TMAPIException
	{
		HGHandle thisH = graph.getHandle(this);
		HGHandle reifier = U.getReifierOf(graph, thisH);
		if (reifier != null)
		{
			U.setReifierOf(graph, thisH, null);
			// TODO: should we really remove the reifier?
			((Topic)graph.get(reifier)).remove(); 
		}	
		for (Variant v : getVariants())
			v.remove();
		for (Locator l : getSourceLocators())
			removeSourceLocator(l);					
		graph.remove(thisH, false);
	}
}