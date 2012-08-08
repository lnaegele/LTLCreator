package editor.tabpane;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import util.components.graphtab.FunctionTab;
import util.components.graphtab.GraphTabPane;
import util.components.graphtab.TitleComponent;

public class AddTab extends FunctionTab {

	private GraphTabPane graphTabPane;
	
	public AddTab(GraphTabPane graphTabPane) {
		this.graphTabPane = graphTabPane;
	}
	
	@Override
	protected TitleComponent createTitleComponent() {
		TitleComponent tc = new TitleComponent() {
			private static final long serialVersionUID = 1L;
			@Override
			public void setMouseOver(boolean mouseOver) {}
			@Override
			public void setActive(boolean active) {}
		};
		tc.setLayout(new BorderLayout());
		tc.add(new JLabel(new ImageIcon(AddTab.class.getResource("plus.png"))));
		tc.setToolTipText("New Constraint");
		tc.revalidate();
		tc.repaint();
		return tc;
	}

	@Override
	protected void execute(GraphTabPane graphTabPane, int selectedIndex) {
		this.graphTabPane.addGraphTab(new OperatorTab(), true);
	}
	
}
