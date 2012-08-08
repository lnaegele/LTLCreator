package util.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

public class CenterLayout implements LayoutManager2 {
	
	private Component comp;
	private Dimension emptySize = new Dimension(400, 400);
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets i = parent.getInsets();
			Dimension ps = (this.comp!=null ? this.comp.getPreferredSize() : this.emptySize);
			return new Dimension(i.left + ps.width + i.right, i.top + ps.height + i.bottom);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return this.preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			if (this.comp==null) return;
			
			Insets i = parent.getInsets();
			Dimension size = parent.getSize();
			Dimension ps = this.comp.getPreferredSize();
			
			int availableWidth = size.width - (i.top + i.bottom);
			int availableHeight = size.height - (i.top + i.bottom);
			
			int left = Math.max(0, (size.width - Math.min(ps.width, availableWidth)) / 2);
			int top = Math.max(0, (size.height - Math.min(ps.height, availableHeight)) / 2);
			
			this.comp.setBounds(left, top, ps.width, ps.height);
		}
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		this.comp = comp;
	}

	@Override
	public Dimension maximumLayoutSize(Container parent) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public float getLayoutAlignmentX(Container parent) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container parent) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container parent) {
	}

}
