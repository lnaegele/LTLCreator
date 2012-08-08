package util.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

public class ListLayout implements LayoutManager2 {
	
	public static final int FILL_BEHAVIOR_INDIVIDUAL = 1;
	public static final int FILL_BEHAVIOR_EQUAL = 2;
	public static final int FILL_BEHAVIOR_FILL = 3;
	
	private int vGap, hGap;
	private int fillBehavior;
	
	public ListLayout(int vGap, int hGap, int fillBehavior) {
		this.vGap = vGap;
		this.hGap = hGap;
		this.fillBehavior = fillBehavior;
	}
	
	public int getVGap() {
		return this.vGap;
	}
	
	public int getHGap() {
		return this.hGap;
	}
	
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
			int height = 0;
			int max_width = 0;
			for (Component comp : parent.getComponents()) {
				Dimension ps = comp.getPreferredSize();
				height += ps.height;
				max_width = Math.max(max_width, ps.width);
			}
			return new Dimension(max_width + i.left + i.right + (2 * this.hGap), height + i.top + i.bottom + ((parent.getComponents().length + 1) * vGap));
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return this.preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets i = parent.getInsets();
			int y = i.top + this.vGap;
			int max_width = 0;
			if (this.fillBehavior==FILL_BEHAVIOR_EQUAL) {
				for (Component comp : parent.getComponents()) {
					Dimension ps = comp.getPreferredSize();
					max_width = Math.max(max_width, ps.width);
				}
			}
			int parent_width = parent.getWidth() - (i.left + i.right + (2 * this.hGap));
			for (Component comp : parent.getComponents()) {
				Dimension ps = comp.getPreferredSize();
				
				int width = 0;
				if (this.fillBehavior==FILL_BEHAVIOR_INDIVIDUAL) width = ps.width;
				else if (this.fillBehavior==FILL_BEHAVIOR_EQUAL) width = max_width;
				else if (this.fillBehavior==FILL_BEHAVIOR_FILL) width = parent_width;
				
				Rectangle b = new Rectangle(i.left + this.hGap, y, width, ps.height);
				comp.setBounds(b);
				y += b.height + this.vGap;
			}
		}
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
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
