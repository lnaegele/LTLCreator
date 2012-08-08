package model;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class AbstractOperatorLayoutManager implements LayoutManager2 {
		
	private AbstractOperator parent;
	private boolean squeezed;
	private List<Bucket> registeredBuckets = new ArrayList<Bucket>();
	
	public AbstractOperatorLayoutManager(AbstractOperator parent, boolean squeezed) {
		this.parent = parent;
		this.squeezed = squeezed;
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		this.added(comp);
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		this.added(comp);
	}
	
	private void added (Component comp) {
		if (!(comp instanceof Bucket)) throw new RuntimeException("Only Buckets are allowed.");
		Bucket bucket = (Bucket) comp;
		if (this.registeredBuckets.contains(bucket)) return;
		this.registeredBuckets.add(bucket);
		this.parent.bucketAdded(bucket);
	}
	
	@Override
	public void removeLayoutComponent(Component comp) {
		if (!(comp instanceof Bucket)) throw new RuntimeException("Only Buckets are allowed.");
		Bucket bucket = (Bucket) comp;
		if (!this.registeredBuckets.contains(bucket)) return;
		this.registeredBuckets.remove(bucket);
		this.parent.bucketRemoved(bucket);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return this.parent.getOperatorSize();
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return this.preferredLayoutSize(parent);
	}
	
	@Override
	public void layoutContainer(Container parent) {
		this.parent.layoutBuckets();
		
		Rectangle mainRectangle;
		List<Rectangle> subRectangles = new ArrayList<Rectangle>();
		
		Dimension ps = parent.getPreferredSize();
		if (this.squeezed) {
			int minThickness = 50;
			
			// Viereck, dass alle Mittelpunkte der Buckets enthält
			Rectangle union = null;
			for (Component c : parent.getComponents()) {
				Rectangle b = c.getBounds();
				Rectangle middlePoint = new Rectangle(b.x + ((Bucket)c).getCenterLineX(), b.y + (b.height/2), 0, 0);
				union = (union==null ? middlePoint : union.union(middlePoint));
			}
			if (union==null) union = new Rectangle(ps.width/2, ps.height/2, 0, 0);
			
			// Viereck auf Mindestbreite und Mindesthöhe bringen
			if (union.width<minThickness) {
				union.x -= (minThickness - union.width) / 2;
				union.width = minThickness;
			}
			if (union.height<minThickness) {
				union.y -= (minThickness - union.height) / 2;
				union.height = minThickness;
			}
			
			// Wenn kein einziges Bucket den jew. Rand berührt, so wird die Fläche bis zu diesem Rand rausgezogen.
			boolean expandToLeft = true;
			boolean expandToTop = true;
			boolean expandToRight = true;
			boolean expandToBottom = true;
			Insets insets = parent.getInsets();
			for (Component c : parent.getComponents()) {
				Rectangle b = c.getBounds();
				if (b.x==insets.left) expandToLeft = false;
				if (b.y==insets.top) expandToTop = false;
				if (b.x+b.width==ps.width-insets.right) expandToRight = false;
				if (b.y+b.height==ps.height-insets.bottom) expandToBottom = false;
			}
			if (expandToLeft) {
				union.width += union.x;
				union.x = 0;
			}
			if (expandToTop) {
				union.height += union.y;
				union.y = 0;
			}
			if (expandToRight) {
				union.width = ps.width - union.x;
			}
			if (expandToBottom) {
				union.height = ps.height - union.y;
			}
	
			mainRectangle = union;
			
			Insets i = parent.getInsets();
			for (Component c : parent.getComponents()) {
				Rectangle b = c.getBounds();
				subRectangles.add(new Rectangle(b.x-i.left, b.y-i.top, b.width+i.left+i.right, b.height+i.top+i.bottom));
			}
		}
		else {
			mainRectangle = new Rectangle(0, 0, ps.width, ps.height);
		}
		
		this.parent.setPaintedRectangles(mainRectangle, subRectangles);
	}
	
	@Override
	public Dimension maximumLayoutSize(Container target) {
		return this.preferredLayoutSize(target);
	}
	
	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}
	
	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}
	
	@Override
	public void invalidateLayout(Container target) {}

}
