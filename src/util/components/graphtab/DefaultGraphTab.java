package util.components.graphtab;

import java.awt.Component;

public class DefaultGraphTab extends GraphTab {

	private TitleComponent title;
	private Component content;
	
	public DefaultGraphTab(TitleComponent title, Component content) {
		this.title = title;
		this.content = content;
	}

	@Override
	protected TitleComponent createTitleComponent() {
		return this.title;
	}

	@Override
	protected Component createContentComponent() {
		return this.content;
	}

}
