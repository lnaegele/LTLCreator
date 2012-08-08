package editor.tabpane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import model.AbstractOperator;
import model.OperatorChangeListener;
import util.components.graphtab.GraphTab;
import util.components.graphtab.TitleComponent;
import editor.dashboard.Dashboard;

public class OperatorTab extends GraphTab {
	private OperatorTabTitleComponent ottc;
	private Dashboard dashboard;
	
	public OperatorTab() {
		this(null);
	}
	
	public OperatorTab(AbstractOperator operator) {
		this.ottc = new OperatorTabTitleComponent();
		this.showNil();
		
		this.dashboard = new Dashboard();
		if (operator!=null) this.dashboard.drop(operator, new Point(), null);
		this.dashboard.addChangeListener(new OperatorChangeListener() {
			@Override
			public void operatorChanged(AbstractOperator operator) {
				OperatorTab.this.ottc.setDisplayedOperator(operator);
			}
		});
	}
	
	public Dashboard getDashboard() {
		return this.dashboard;
	}
	
	@Override
	protected TitleComponent createTitleComponent() {
		return this.ottc;
	}
	
	@Override
	protected Component createContentComponent() {
		JComponent panel = new JPanel(new BorderLayout());
		panel.add(this.getDashboard(), BorderLayout.CENTER);
		
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		return scroll;
	}
	
	public void showProgress() {
		this.ottc.showProgress();
	}
	
	public void showSuccess() {
		this.ottc.showSuccess();
	}
	
	public void showFailure() {
		this.ottc.showFailure();
	}

	public void showIncomplete() {
		this.ottc.showIncomplete();
	}
	
	public void showNil() {
		this.ottc.showNil();
	}

}
