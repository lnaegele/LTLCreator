package util.components.graphtab;

import java.awt.geom.RoundRectangle2D;

public abstract class FunctionTab  {

	private TitleComponent title = null;
	private RoundRectangle2D rectangle = new RoundRectangle2D.Double();
	
	public TitleComponent getTitleComponent() {
		if (this.title==null) this.title = createTitleComponent();
		return this.title;
	}
	
	protected RoundRectangle2D getRectangle() {
		return this.rectangle;
	}
	
	protected abstract TitleComponent createTitleComponent();
	protected abstract void execute(GraphTabPane graphTabPane, int selectedIndex);
}
