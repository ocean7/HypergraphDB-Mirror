package org.hypergraphdb.viewer.hg;

import giny.model.Edge;
import giny.model.Node;
import giny.view.GraphView;
import giny.view.NodeView;
import java.util.*;
import org.hypergraphdb.*;
import org.hypergraphdb.storage.BAtoHandle;
import org.hypergraphdb.storage.DefaultIndexImpl;
import org.hypergraphdb.type.*;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.atom.HGSubsumes;
import org.hypergraphdb.atom.HGTypeStructuralInfo;
import org.hypergraphdb.handle.*;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.query.*;
import org.hypergraphdb.viewer.AppConfig;
import org.hypergraphdb.viewer.HGVEdge;
import org.hypergraphdb.viewer.HGVNetwork;
import org.hypergraphdb.viewer.HGVNetworkView;
import org.hypergraphdb.viewer.HGVNode;
import org.hypergraphdb.viewer.HGVKit;
import org.hypergraphdb.viewer.dialogs.DialogDisplayer;
import org.hypergraphdb.viewer.dialogs.NotifyDescriptor;
import org.hypergraphdb.viewer.util.GUIUtilities;
import static org.hypergraphdb.HGQuery.hg;

/**
 * 
 */
public class HGUtils
{
	// temporary here, will go away
	public static Set<HGAtomType> getAllAtomTypes(HyperGraph graph)
	{
		Set<HGAtomType> types = new HashSet<HGAtomType>();
		HGSearchResult it = graph.find(hg.all()); //ind.scanValues();
		while (it.hasNext())
		{
			Object obj = graph.get((HGHandle) it.next());
			HGAtomType type = graph.getTypeSystem().getAtomType(obj);
			types.add(type);
		}
		it.close();
		return types;
	}

	public static HGPersistentHandle lookup(HyperGraph hg, String typeAlias,
			String keyProperty, Object keyValue)
	{
		HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(typeAlias);
		ByPartIndexer byProperty = new ByPartIndexer(typeHandle, new String[] { keyProperty });
		HGIndex<String, HGPersistentHandle> index = hg.getIndexManager().register(byProperty);
		return index.findFirst((String)keyValue);
	}

	public static HGPersistentHandle[] getAllRecordTypes(HyperGraph graph)
	{
		HGSearchResult it = null;
		try
		{
			it = graph.find(hg.type(HGSubsumes.class));
			Set<HGHandle> list = new HashSet<HGHandle>();
			while (it.hasNext())
			{
				it.next();
				HGSubsumes h = (HGSubsumes) graph.get((HGHandle)it.current());
				list.add(graph.getPersistentHandle(
						h.getSpecific()));
			}
			return (HGPersistentHandle[]) list.toArray(new HGPersistentHandle[list
					.size()]);
		}
		finally
		{
			if(it!=null)
			   it.close();
		}
	}

	public static RecordType getRecordType(HyperGraph hg, HGPersistentHandle h)
	{
		Object obj = hg.get(h);
		if (obj instanceof JavaBeanBinding)
			return (RecordType) ((JavaBeanBinding) obj).getHGType();
		return (RecordType) hg.get(h);
	}

	public static HGPersistentHandle[] getAllForType(HyperGraph graph, HGHandle handle)
	{
		HGSearchResult ite = null;
		try
		{
			ite = graph.find(hg.type(handle));
			List<HGHandle> list = new ArrayList<HGHandle>();
			while (ite.hasNext())
			{
				ite.next();
				list.add((HGHandle) ite.current());
			}
			return (HGPersistentHandle[]) list.toArray(new HGPersistentHandle[list
					.size()]);
		}
		finally
		{
			ite.close();
		}
	}

	public static void expandSelectedNode(HyperGraph hg)
	{
		GraphView view = HGVKit.getCurrentView();
		List selected_nodeViews = view.getSelectedNodes();
		for (Object obj : selected_nodeViews)
		{
			HGVNode node = (HGVNode) ((NodeView) obj).getNode();
			expandNode(hg, node, false);
		}
		HGVKit.getCurrentView().redrawGraph();
	}

	public static void expandNode(HyperGraph hg, HGVNode node)
	{
		expandNode(hg, node, true);
		HGVKit.getCurrentView().redrawGraph();
	}

	private static void expandNode(HyperGraph hg, HGVNode node, boolean select)
	{
		Thread.currentThread().setContextClassLoader(
				AppConfig.getInstance().getClassLoader());
		if (select)
		{
			HGVNetworkView view = HGVKit.getCurrentView();
			// select the view, because the popup doesnt do this automaticaly
			view.getNetwork().getFlagger().unflagAllNodes();
			view.getNodeView(node).setSelected(true);
		}
		addNode(hg, node.getHandle());
	}

	public static HGVNode addNode(HyperGraph hg, HGHandle handle)
	{
		return addNode(hg, handle, 1);
	}

	// The node should be presented in HG, this method adds it to the View
	public static HGVNode addNode(HyperGraph hg, HGHandle handle, int level)
	{
		HGVNetworkView view = HGVKit.getCurrentView();
		Object obj = hg.get(handle);
		if (obj instanceof HGLink && isDirectedLink(hg, handle))
			return addDirectedNode(hg, handle, level);
		HGVNode node = HGVKit
				.getHGVNode(hg.getPersistentHandle(handle), true);
		if(node == null) System.err.println("NULL NODE");
		view.getNetwork().addNode(node);
		NodeView nview = view.getNodeView(node.getRootGraphIndex());
		if(node == null) System.err.println("NULL NodeView");
		view.showGraphObject(nview);
		if (level > 0)
		{
			HGHandle[] h_links = hg.getIncidenceSet(handle);
			if(!confirmExpanding(h_links.length))
				return node;
		
			for (int i = 0; i < h_links.length; i++)
			{
				if (isDirectedLink(hg, h_links[i]))
				{
					addNode(hg, h_links[i], level - 1);
					continue;
				}
				HGVEdge edge = HGVKit.getHGVEdge(addNode(hg, h_links[i],
						level - 1), node, true);
				view.getNetwork().addEdge(edge);
				view
						.showGraphObject(view.getEdgeView(edge
								.getRootGraphIndex()));
			}
		}
		if (obj instanceof HGLink)
		{
			if (level < 1) return node;
			HGLink link = ((HGLink) obj);
			if(!confirmExpanding(link.getArity()))
				return node;
			for (int i = 0; i < link.getArity(); i++)
			{
				HGVEdge edge = HGVKit.getHGVEdge(node, addNode(hg, link
						.getTargetAt(i), level - 1), true);
				view.getNetwork().addEdge(edge);
				view.showGraphObject(view.getEdgeView(edge.getRootGraphIndex()));
			}
		}
		return node;
	}
	
	

	public static HGVNode addDirectedNode(HyperGraph hg, HGHandle handle,
			int level)
	{
		HGVNetworkView view = HGVKit.getCurrentView();
		Object obj = hg.get(handle);
		HGHandle src = ((HGLink) obj).getTargetAt(0);
		HGHandle trg = ((HGLink) obj).getTargetAt(1);
		HGVNode src_n = addNode(hg, src, level - 1);
		HGVNode trg_n = addNode(hg, trg, level - 1);
		HGVEdge edge = HGVKit.getHGVEdge(src_n, trg_n, true);
		view.getNetwork().addEdge(edge);
		view.showGraphObject(view.getNodeView(src_n.getRootGraphIndex()));
		view.showGraphObject(view.getNodeView(trg_n.getRootGraphIndex()));
		view.showGraphObject(view.getEdgeView(edge.getRootGraphIndex()));
		return trg_n;
	}

	public static boolean isDirectedLink(HyperGraph hg, HGHandle handle)
	{
		HGAtomType type = hg.getTypeSystem().getAtomType(hg.get(handle));
		for(HGAtomType t : getDirLinks(hg))
			if(t.equals(type))
				return true;
		return false;
	}
	
	private static boolean confirmExpanding(int edges_count)
	{
		if(edges_count > 50)
		{
			NotifyDescriptor d = new NotifyDescriptor.Confirmation(
					GUIUtilities.getFrame(), "The node contains " + edges_count + " edges. Are you sure that you want to expand them?", 
					NotifyDescriptor.OK_CANCEL_OPTION);
			if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.CANCEL_OPTION)
			{
				return false;
			}
		}
		return true;
	}
	
	private static Set<HGAtomType> dir_links = null;
   
	private static Set<HGAtomType> getDirLinks(HyperGraph hg)
	{
		if (dir_links != null) return dir_links;
		dir_links = new HashSet<HGAtomType>();
/*		HGHandle sHandle = hg.getTypeSystem().getTypeHandle(HGTypeStructuralInfo.class);
		AtomTypeCondition cond = new AtomTypeCondition(hg.getPersistentHandle(sHandle)); */
		HGHandle sHandle = hg.getHandle(HGTypeStructuralInfo.class);
		//TODO: ??? after last major HG changes this throws NPE
		if(sHandle == null || sHandle.equals(HGHandleFactory.nullHandle()))
			return dir_links;
	    AtomTypeCondition cond = new AtomTypeCondition(hg.getPersistentHandle(sHandle));
		HGQuery query = HGQuery.make(hg, cond);
		HGSearchResult it = query.execute();
		while (it.hasNext())
		{
			it.next();
			HGTypeStructuralInfo info = 
				(HGTypeStructuralInfo) hg.get((HGHandle)it.current());
			if(info != null && info.isOrdered() && info.getArity() == 2)
			{
				dir_links.add(hg.getTypeSystem().getType(info.getTypeHandle()));
			}
		}
		it.close();
		return dir_links;
	}
	

	public static void collapseNode(HyperGraph hg, HGVNode node)
	{
		removeNode(hg, node);
		// HGVKit.getCurrentNetworkView().redrawGraph(false, false);
	}
	
//	 The node should be presented in HG, this method removes it from the View
	public static int removeNode(HyperGraph hg, HGVNode node)
	{
		//Thread.currentThread().setContextClassLoader(
		//		AppConfig.getInstance().getClassLoader());
		if(node == null) return 0;
	    HGVNetworkView view = HGVKit.getCurrentView();
		HGVNetwork net = view.getNetwork();
		
		Set<Node> nodesToRemove = new HashSet<Node>();
		nodesToRemove.add(node);
		Set<Edge> edgesToRemove = new HashSet<Edge>();
		int[] edges = net.getAdjacentEdgeIndicesArray(
				node.getRootGraphIndex(), true, true, true);
		for(int i = 0; i < edges.length; i++)
		{
			Edge e = net.getEdge(edges[i]);
			Node out = getOppositeNode(node, e);
			int[] in_edges = net.getAdjacentEdgeIndicesArray(
					out.getRootGraphIndex(), true, true, true);
			if( in_edges.length <= 1)
			{
				nodesToRemove.add(out);
				for(int j = 0; j < in_edges.length; j++)
				   edgesToRemove.add(net.getEdge(in_edges[j]));
			}
			edgesToRemove.add(e);
		}
		
		for(Edge edge: edgesToRemove)
		{
			view.removeEdgeView(edge);
			net.getRootGraph().removeEdge(edge);
		}
		for(Node n: nodesToRemove)
		{
		   view.removeNodeView(n);
		   net.getRootGraph().removeNode(n);
		}
		//System.out.println("Removed - nodes: " + nodesToRemove.size() 
		//		+ " edges: "+ edgesToRemove.size());
		return nodesToRemove.size();
	}

	public static Node getOppositeNode(Node center, giny.model.Edge e)
	{
		if (e.getSource().equals(center)) return e.getTarget();
		return e.getSource();
	}
	
	public static String deduceAliasName(HyperGraph hg, HGPersistentHandle h)
	{
		HGTypeSystem ts = hg.getTypeSystem();
		String total = "";
		Iterator it = ts.findAliases(h);
		while (it.hasNext())
    		total += (String)it.next() + "/";// \s
		if (total.length() > 1)
			total = total.substring(0, total.length() - 1);
		if (total.length() == 0)
			total = hg.get(h).getClass().getName();
		return total;
	}

	public static Set<String> getEdgeTypes(HyperGraph h)
	{
		//if(edgeTypesCache.containsKey(h))
		// return edgeTypesCache.get(h);
//		Set set = h.getTypeSystem().getJavaTypeMappings().keySet();
		Set<String> filter_set = new TreeSet<String>();
/*		for (Iterator it = set.iterator(); it.hasNext();)
		{
			Class cl = (Class) it.next();
			if (HGLink.class.isAssignableFrom(cl))
				filter_set.add(cl.getName());
		} */
		// edgeTypesCache.put(h, filter_set);
		return filter_set;
	}

	public static Set<String> getNodeTypes(HyperGraph h)
	{
		//if(nodeTypesCache.containsKey(h))
		// return nodeTypesCache.get(h);
		Set<HGAtomType> set = getAllAtomTypes(h);
		Set<String> filter_set = new TreeSet<String>();
		
		for (HGAtomType t : set)
			filter_set.add(t.getClass().getName());
		return filter_set;
		
//		HGPersistentHandle[] handles = getAllRecordTypes(h);
//		System.out.println("NodeTypes: " + handles.length);
//		Set<String> filter_set = new TreeSet<String>();
//		for (HGPersistentHandle hh : handles)
//		   filter_set.add(deduceAliasName(h, hh)); //h.get(hh).getClass().getName());
//		
//		// nodeTypesCache.put(h, filter_set);
//		return filter_set;
	}

	// private static HashMap<HyperGraph, Set<String>> nodeTypesCache = new
	// HashMap<HyperGraph, Set<String>>();
	//private static HashMap<HyperGraph, Set<String>> edgeTypesCache = new
	// HashMap<HyperGraph, Set<String>>();
	private HGUtils()
	{ 
	}
}
