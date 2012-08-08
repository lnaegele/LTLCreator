package util.components.graphtab;


public class DefaultFunctionTab extends FunctionTab {

	private TitleComponent title;
	private Runnable function;
	
	public DefaultFunctionTab(TitleComponent title, Runnable function) {
		this.title = title;
		this.function = function;
	}
	
	@Override
	protected TitleComponent createTitleComponent() {
		return this.title;
	}

	@Override
	protected void execute(GraphTabPane graphTabPane, int selectedIndex) {
		this.function.run();
	}

}
