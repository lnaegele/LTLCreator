package util.components.graphtab;

import javax.swing.JComponent;

public abstract class TitleComponent extends JComponent {
	private static final long serialVersionUID = 1L;

	public abstract void setMouseOver(boolean mouseOver);
	public abstract void setActive(boolean active);
}
