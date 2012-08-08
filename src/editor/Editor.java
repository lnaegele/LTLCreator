package editor;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import model.AbstractOperator;
import util.components.graphtab.GraphTab;
import util.components.graphtab.GraphTabListener;
import util.components.graphtab.GraphTabPane;
import util.components.scrollcolumn.ScrollColumn;
import util.dnd.DndHandler;
import util.fsmmodel.Fsm;
import editor.tabpane.AddTab;
import editor.tabpane.MagicTab;
import editor.tabpane.OperatorTab;
import editor.toolpane.ToolPane;
import editor.validator.ConstraintValidator;
import editor.validator.impl.NuSMVModelCheckerWrapper;

/**
 * This class allows an easy integration of the LTLCreator tool into other swing
 * based java programs. <a>Editor</a> is a component containing a tab bar for
 * several dashboards and a tool bar for constraint creation.
 * 
 * @author ludwig
 * 
 */
public class Editor extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private ConstraintValidator validator = new NuSMVModelCheckerWrapper();
	private GraphTabPane graphTabPane = new GraphTabPane();
	private Map<GraphTab, OperatorAndModelChangeListener> graphTabs = new HashMap<GraphTab, OperatorAndModelChangeListener>();
	
	/**
	 * Creates a new LTLCreator component. It can be added to any swing container.
	 */
	public Editor() {
		this.setLayout(new BorderLayout());
		
		DndHandler.getInstance().registerTopLevelComponent(this);
		
		this.graphTabPane.addFunctionTab(new AddTab(this.graphTabPane));
		this.graphTabPane.addFunctionTab(new MagicTab(this.graphTabPane, this.validator));
		this.graphTabPane.addGraphTabListener(new MyGraphTabListener());
		
		this.add(this.graphTabPane, BorderLayout.CENTER);
		this.add(new ScrollColumn(new ToolPane()), BorderLayout.EAST);
	}
	
	/**
	 * Sets the new model. It causes all constraints to be revalidated, and the
	 * list from where all possible states can be choosen will be updated.
	 * 
	 * @param model
	 *            the new model.
	 */
	public void setModel(Fsm model) {
		this.validator.setModel(model);
	}
	
	/**
	 * Adds a new tab with an empty dashboard to the editor.
	 * 
	 * @param select
	 *            determines whether the tab should become selected.
	 */
	public void addNewEmptyDashboard(boolean select) {
		addNewDashboard(null, select);
	}
	
	/**
	 * Adds a new tab with a prefilled dashboard to the editor.
	 * 
	 * @param operator
	 *            the constraint contained in the dashboard.
	 * @param select
	 *            determines whether the tab should become selected.
	 */
	public void addNewDashboard(AbstractOperator operator, boolean select) {
		this.graphTabPane.addGraphTab(new OperatorTab(operator), select);
	}
	
	/**
	 * Returns a list of all operators, ordered in the same order as the tabs.
	 * 
	 * @return all operators of the editor.
	 */
	public List<AbstractOperator> getOperators() {
		List<AbstractOperator> result = new ArrayList<AbstractOperator>();
		for (GraphTab graphTab : this.graphTabPane.getGraphTabs()) result.add(((OperatorTab)graphTab).getDashboard().getOperator()); 
		return result;
	}
 
	private class MyGraphTabListener implements GraphTabListener {
		@Override
		public void selectedIndexChanged(int index) {}
		
		@Override
		public void graphTabAdded(GraphTab graphTab) {
			final OperatorTab operatorTab = (OperatorTab)graphTab;
			
			// Every dashboard has its own listener for changes on either the constraint or the model.
			OperatorAndModelChangeListener l = new OperatorAndModelChangeListener(operatorTab, Editor.this.validator);
			operatorTab.getDashboard().addChangeListener(l);
			Editor.this.validator.addModelChangeListener(l);
			Editor.this.graphTabs.put(graphTab, l);
			
			// initial validation
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					operatorTab.getDashboard().notifyChangeListeners_operatorChanged();
				}
			});
		}
		
		@Override
		public void graphTabRemoved(GraphTab graphTab) {
			OperatorTab operatorTab = (OperatorTab)graphTab;
			OperatorAndModelChangeListener l = Editor.this.graphTabs.remove(graphTab);
			Editor.this.validator.removeModelChangeListener(l);
			operatorTab.getDashboard().removeChangeListener(l);
		}
	}

}
