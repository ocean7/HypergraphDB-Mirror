//-------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2005/12/25 01:22:41 $
// $Author: bobo $
//-------------------------------------------------------------------------
package org.hypergraphdb.viewer.actions;
//-------------------------------------------------------------------------
import java.awt.event.ActionEvent;
import org.hypergraphdb.viewer.ActionManager;
import org.hypergraphdb.viewer.HGVKit;
import org.hypergraphdb.viewer.util.HGVAction;
//-------------------------------------------------------------------------
public class HideSelectedNodesAction extends HGVAction   {
    
    public HideSelectedNodesAction () {
        super(ActionManager.HIDE_NODE_SELECTION_ACTION);
        setAcceleratorCombo( java.awt.event.KeyEvent.VK_H, ActionEvent.CTRL_MASK );
    }

  public HideSelectedNodesAction ( boolean label ) {
    super();
       
  }

    public void actionPerformed ( ActionEvent e ) {
    	if(HGVKit.getCurrentView()!= null)
           GinyUtils.hideSelectedNodes( HGVKit.getCurrentView() );
    }

}
