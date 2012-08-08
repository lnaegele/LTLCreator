package util.components.graphtab;

import java.awt.Component;
import java.awt.geom.RoundRectangle2D;

public abstract class GraphTab {

	private TitleComponent title = null;
	private Component content = null;
	private RoundRectangle2D rectangle = new RoundRectangle2D.Double();
	
	public TitleComponent getTitleComponent() {
		if (this.title==null) this.title = this.createTitleComponent();
		return this.title;
	}
	
	public Component getContentComponent() {
		if (this.content==null) this.content = this.createContentComponent();
		return this.content;
	}
	
	protected RoundRectangle2D getRectangle() {
		return this.rectangle;
	}
	
	protected abstract TitleComponent createTitleComponent();
	protected abstract Component createContentComponent();
}
