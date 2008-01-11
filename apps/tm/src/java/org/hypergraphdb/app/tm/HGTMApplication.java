package org.hypergraphdb.app.tm;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGApplication;
import org.hypergraphdb.app.management.HGManagement;
import org.hypergraphdb.atom.HGRelType;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.tmapi.core.Locator;

public class HGTMApplication extends HGApplication 
{
	public static final String INFERRED_TYPES_RESOURCE = "/org/hypergraphdb/app/tm/inferredTypes.properties";
	private void loadPredefinedTopics(HyperGraph graph)
	{
		HGTopic t = null;
		Locator l = null;
		
		l = U.makeLocator(HGTM.typeInstanceIdentifier);
		graph.define(HGTM.hTypeInstanceLocator, l, null);
		t = new HGTopic();
		t.graph = graph;
		graph.define(HGTM.hTypeInstanceTopic, t,null);
		t.addSourceLocator(l);
		
		l = U.makeLocator(HGTM.typeRoleIdentifier);
		graph.define(HGTM.hTypeRoleLocator, l, null);
		t = new HGTopic();
		t.graph = graph;
		graph.define(HGTM.hTypeRoleTopic, t,null);
		t.addSourceLocator(l);
		
		l = U.makeLocator(HGTM.instanceRoleIdentifier);
		graph.define(HGTM.hInstanceRoleLocator, l, null);
		t = new HGTopic();
		t.graph = graph;
		graph.define(HGTM.hInstanceRoleTopic, t,null);
		t.addSourceLocator(l);
		
		l = U.makeLocator(HGTM.subtypingIdentifier);
		graph.define(HGTM.hSubtypingLocator, l, null);
		t = new HGTopic();
		t.graph = graph;
		graph.define(HGTM.hSubtypingTopic, t,null);
		t.addSourceLocator(l);
		
		l = U.makeLocator(HGTM.subTypeRoleIdentifier);
		graph.define(HGTM.hSubTypeLocator, l, null);
		t = new HGTopic();
		t.graph = graph;
		graph.define(HGTM.hSubTypeTopic, t,null);
		t.addSourceLocator(l);
		
		l = U.makeLocator(HGTM.superTypeRoleIdentifier);
		graph.define(HGTM.hSuperTypeLocator, l, null);
		t = new HGTopic();
		t.graph = graph;
		graph.define(HGTM.hSuperTypeTopic, t,null);
		t.addSourceLocator(l);
		
		l = U.makeLocator(HGTM.topicNameIdentifier);
		graph.define(HGTM.hTopicNameLocator, l, null);
		t = new HGTopic();
		t.graph = graph;
		graph.define(HGTM.hTopicNameTopic, t,null);
		t.addSourceLocator(l);
		
		l = U.makeLocator(HGTM.schemaAnyType);
		graph.define(HGTM.hSchemaAnyType, l, null);
		l = U.makeLocator(HGTM.schemaAnyURI);
		graph.define(HGTM.hSchemaAnyURI, l, null);
		l = U.makeLocator(HGTM.schemaStringType);
		graph.define(HGTM.hSchemaStringType, l, null);		
	}
	
	private void unloadPredefinedTopics(HyperGraph graph)
	{
		graph.remove(HGTM.hTypeInstanceLocator);
		graph.remove(HGTM.hTypeInstanceTopic);
		
		graph.remove(HGTM.hTypeRoleLocator);
		graph.remove(HGTM.hTypeRoleTopic);
		
		graph.remove(HGTM.hInstanceRoleLocator);
		graph.remove(HGTM.hInstanceRoleTopic);
		
		graph.remove(HGTM.hSubtypingLocator);
		graph.remove(HGTM.hSubtypingTopic);
		
		graph.remove(HGTM.hSubTypeLocator);
		graph.remove(HGTM.hSubTypeTopic);
		
		graph.remove(HGTM.hSuperTypeLocator);
		graph.remove(HGTM.hSuperTypeTopic);
		
		graph.remove(HGTM.hTopicNameLocator);
		graph.remove(HGTM.hTopicNameTopic);
		
		graph.remove(HGTM.hSchemaAnyType);
		graph.remove(HGTM.hSchemaAnyURI);
		graph.remove(HGTM.hSchemaStringType);
	}
	
	private void defineTypes(HyperGraph graph)
	{
		HGTypeSystem ts = graph.getTypeSystem();
		graph.getTransactionManager().beginTransaction();
		try
		{
			HGManagement.defineTypeClasses(graph, INFERRED_TYPES_RESOURCE);
			
			HGHandle [] locatorTopicTypes = 
				new HGHandle[] { ts.getTypeHandle(Locator.class), ts.getTypeHandle(HGTopic.class) };
			HGHandle [] locatorItemTypes =
				new HGHandle[] { ts.getTypeHandle(Locator.class), ts.getTypeHandle(HGTopicMapObjectBase.class) };			
			HGHandle [] topicTypes =
				new HGHandle[] { ts.getTypeHandle(HGTopic.class), ts.getTypeHandle(HGTopicMapObjectBase.class) };
			HGHandle [] scopedTopicTypes = 
				new HGHandle [] { ts.getTypeHandle(HGScopedObject.class), ts.getTypeHandle(HGTopic.class) };
			HGHandle [] nameTopicTypes = 
				new HGHandle [] { ts.getTypeHandle(HGTopicName.class), ts.getTypeHandle(HGTopic.class) };
			HGHandle [] variantNameTypes = 
				new HGHandle [] { ts.getTypeHandle(HGVariant.class), ts.getTypeHandle(HGTopicName.class) };
			HGHandle [] mapMemberTypes = 
				new HGHandle [] { ts.getTypeHandle(HGTopicMapObjectBase.class), ts.getTypeHandle(HGTopicMap.class) };			
			
			graph.define(HGTM.hSubjectIdentifier, new HGRelType(HGTM.SubjectIdentifier), locatorTopicTypes);
			graph.define(HGTM.hSubjectLocator, new HGRelType(HGTM.SubjectLocator), locatorTopicTypes);
			graph.define(HGTM.hSourceLocator, new HGRelType(HGTM.SourceLocator), locatorItemTypes);
			graph.define(HGTM.hTypeOf, new HGRelType(HGTM.TypeOf), topicTypes);
			graph.define(HGTM.hScopeOf, new HGRelType(HGTM.ScopeOf), scopedTopicTypes);
			graph.define(HGTM.hOccurrence, new HGRelType(HGTM.Occurence), scopedTopicTypes);
			graph.define(HGTM.hReifierOf, new HGRelType(HGTM.ReifierOf), scopedTopicTypes);
			graph.define(HGTM.hNameOf, new HGRelType(HGTM.NameOf), nameTopicTypes);
			graph.define(HGTM.hVariantOf, new HGRelType(HGTM.VariantOf), variantNameTypes);
			graph.define(HGTM.hMapMember, new HGRelType(HGTM.MapMember), mapMemberTypes);
			
			loadPredefinedTopics(graph);
			
			graph.getTransactionManager().commit();
		}
		catch (Throwable t)
		{
			graph.getTransactionManager().abort();
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;
			else
				throw new RuntimeException(t);
		}
	}

	private void undefineTypes(HyperGraph graph)
	{
//		graph.getTransactionManager().beginTransaction();
		try
		{
			graph.remove(HGTM.hSubjectIdentifier);
			graph.remove(HGTM.hSubjectLocator);
			graph.remove(HGTM.hSourceLocator);
			graph.remove(HGTM.hTypeOf);
			graph.remove(HGTM.hOccurrence);
			graph.remove(HGTM.hScopeOf);
			graph.remove(HGTM.hReifierOf);
			graph.remove(HGTM.hNameOf);
			graph.remove(HGTM.hVariantOf);
			graph.remove(HGTM.hMapMember);
			graph.remove(HGTM.hSchemaAnyType);
			graph.remove(HGTM.hSchemaAnyURI);
			graph.remove(HGTM.hSchemaStringType);		
			
			unloadPredefinedTopics(graph);
			
	//		graph.getTransactionManager().commit();			
		}
		catch (Throwable t)
		{
//			graph.getTransactionManager().abort();
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;
			else
				throw new RuntimeException(t);
		}
		HGManagement.undefineTypeClasses(graph, INFERRED_TYPES_RESOURCE);		
	}

	private void createIndices(HyperGraph graph)
	{
		HGHandle type = graph.getTypeSystem().getTypeHandle(URILocator.class);
		graph.getIndexManager().register(new ByPartIndexer(type, new String[]{"reference"}));
		type = graph.getTypeSystem().getTypeHandle(HGTopicName.class);
		graph.getIndexManager().register(new ByPartIndexer(type, new String[]{"value"}));
		type = graph.getTypeSystem().getTypeHandle(HGVariant.class);
		graph.getIndexManager().register(new ByPartIndexer(type, new String[]{"value"}));		
	}
	
	
	private void deleteIndices(HyperGraph graph)
	{
		HGHandle type = graph.getTypeSystem().getTypeHandle(URILocator.class);
		graph.getIndexManager().unregisterAll(type);
		type = graph.getTypeSystem().getTypeHandle(HGTopicName.class);
		graph.getIndexManager().unregisterAll(type);
		type = graph.getTypeSystem().getTypeHandle(HGVariant.class);
		graph.getIndexManager().unregisterAll(type);		
	}
	
	private void deleteLocators(HyperGraph graph)
	{
		graph.remove(graph.getTypeSystem().getTypeHandle(URILocator.class));
	}
	
	public HGTMApplication()
	{
		this.setName(HGTM.APPLICATION_NAME);
		this.setVersion("1.0");
	}
	public void install(HyperGraph graph) 
	{
		defineTypes(graph);
		createIndices(graph);
	}

	public void reset(HyperGraph graph) 
	{		
		uninstall(graph);
		install(graph);
	}

	public void uninstall(HyperGraph graph) 
	{
		deleteIndices(graph);
		undefineTypes(graph);
		deleteLocators(graph);
	}

	public void update(HyperGraph graph) 
	{
		throw new UnsupportedOperationException();
	}
}