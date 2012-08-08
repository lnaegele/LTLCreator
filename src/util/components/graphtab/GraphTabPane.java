package util.components.graphtab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import util.components.scrollcolumn.ScrollColumn;

public class GraphTabPane extends JComponent {
	private static final long serialVersionUID = 1L;

	private TabPane tabPane;
	private Component content = null;
	private List<GraphTabListener> listeners = new ArrayList<GraphTabListener>();
	
	public GraphTabPane() {
		this.setLayout(new BorderLayout());
		this.tabPane = new TabPane(this);
		this.add(new ScrollColumn(this.tabPane), BorderLayout.WEST);
	}
	
	public void addGraphTab(TitleComponent title, Component content) {
		addGraphTab(title, content, false);
	}
	
	public void addGraphTab(TitleComponent title, Component content, boolean select) {
		addGraphTab(new DefaultGraphTab(title, content), select);
	}
	
	public void addGraphTab(GraphTab graphTab) {
		addGraphTab(graphTab, false);
	}
	
	public void addGraphTab(GraphTab graphTab, boolean select) {
		this.tabPane.addGraphTab(graphTab, select);
		notifyGraphTabListeners_graphTabAdded(graphTab);
	}
	
	public void addFunctionTab(FunctionTab functionTab) {
		this.tabPane.addFunctionTab(functionTab);
	}
	
	public List<GraphTab> getGraphTabs() {
		return this.tabPane.getGraphTabs();
	}
	
	public void setSelectedIndex(int index) {
		this.tabPane.setSelectedIndex(index);
	}
	
	public void removeGraphTab(int index) {
		GraphTab graphTab = this.tabPane.removeGraphTab(index);
		notifyGraphTabListeners_graphTabRemoved(graphTab);
	}

	protected void setContentComponent(Component content) {
		if (this.content!=null) this.remove(this.content);
		this.content = content;
		this.add(this.content, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}
	
	protected void notifyGraphTabListeners_selectedIndexChanged(int index) {
		synchronized (this.listeners) {
			for (GraphTabListener l : this.listeners) {
				l.selectedIndexChanged(index);
			}
		}
	}
	
	protected void notifyGraphTabListeners_graphTabAdded(GraphTab graphTab) {
		synchronized (this.listeners) {
			for (GraphTabListener l : this.listeners) {
				l.graphTabAdded(graphTab);
			}
		}
	}
	
	protected void notifyGraphTabListeners_graphTabRemoved(GraphTab graphTab) {
		synchronized (this.listeners) {
			for (GraphTabListener l : this.listeners) {
				l.graphTabRemoved(graphTab);
			}
		}
	}
	
	public void addGraphTabListener(GraphTabListener l) {
		synchronized (this.listeners) {
			this.listeners.add(l);
		}
	}
	
	public void removeGraphTabListener(GraphTabListener l) {
		synchronized (this.listeners) {
			this.listeners.remove(l);
		}
	}
	
}
