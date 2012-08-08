package model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import model.impl.AndOperator;
import model.impl.OrOperator;
import util.Utils;
import util.dnd.Draggable;
import util.dnd.DropTarget;

/**
 * This class represents an abstract operator. Each operator can contain several
 * child operators, but this depends on the specific subclass.
 * 
 * @author ludwig
 * 
 */
public abstract class AbstractOperator extends JComponent implements Draggable {
	private static final long serialVersionUID = 1L;

	private static final int margin = 3;
	private static final double transluscent_factor = 0.6;
	private static final boolean paintSqueezed = true;
	
	private final String monitor = "";
	
	private boolean transluscent = false;
	private Rectangle mainRectangle;
	private List<Rectangle> subRectangles = new ArrayList<Rectangle>();
	private OperatorChangeListener listener;
	
	public AbstractOperator() {
		this.setLayout(new AbstractOperatorLayoutManager(this, paintSqueezed));
		this.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
		this.listener = new OperatorChangeListener() {
			@Override
			public void operatorChanged(AbstractOperator operator) {
				AbstractOperator.this.notifyChangeListeners_operatorChanged();
			}
		};
	}
	
	/**
	 * Returns the corresponding LTL formula for this operator and all
	 * suboperators.
	 * 
	 * @return the corresponding LTL formula for this operator.
	 * @throws NotCompleteException
	 *             if at least one (sub-)operator is not complete.
	 */
	public abstract String getLTL() throws NotCompleteException;
	
	/**
	 * Returns the specific color of the implemented operator type.
	 * 
	 * @return the specific color of the implemented operator type.
	 */
	public abstract Color getColor();
	
	/**
	 * Creates a new instance of the implemented operator type.
	 * 
	 * @return a new instance of the implemented operator type.
	 */
	public abstract AbstractOperator createNewInstance();
	
	protected abstract void paintOperator(Graphics2D g, boolean considerHighlighting);
	protected abstract void layoutBuckets();
	protected abstract int getCenterLineX();
	protected abstract Dimension getOperatorSize();
	
	public Color getBackgroundColor1(boolean considerHighlighting) {
		Color col = Utils.computeColor(this.getColor(), 0.6);
		if (considerHighlighting && this.transluscent) col = Utils.computeColor(col, transluscent_factor);
		return col;
	}
	
	public Color getBackgroundColor2(boolean considerHighlighting) {
		Color col = Utils.computeColor(this.getColor(), -0.2);
		if (considerHighlighting && this.transluscent) col = Utils.computeColor(col, transluscent_factor);
		return col;
	}
	
	public Color getBorderColor(boolean considerHighlighting) {
		Color col = Utils.computeColor(this.getColor(), -0.4);
		if (considerHighlighting && this.transluscent) col = Utils.computeColor(col, transluscent_factor);
		return col;
	}

	protected void setHoveredOperator(AbstractOperator operator) {
		AbstractOperator next = (operator==this ? null : operator);

		boolean transluscent = next!=null;
		if (this.transluscent!=transluscent) {
			this.transluscent = transluscent;
			this.repaint();
		}
		
		for (Component c : getComponents()) {
			if (!(c instanceof Bucket)) continue;
			((Bucket)c).setHoveredOperator(next);
		}
	}
	
	@Override
	public void setMouseOver(boolean mouseOver) {
		AbstractOperator parent = this;
		Container mem = parent.getParent();
		while (mem!=null && mem!=mem.getParent()) {
			if (mem instanceof AbstractOperator) parent = (AbstractOperator)mem;
			mem = mem.getParent();
		}
		
		parent.setHoveredOperator(mouseOver ? this : null);
	}
	
	@Override
	public DropTarget getCurrentParent() {
		return (DropTarget)getParent();
	}
	
	@Override
	public void mouseClick(Point point) {}
	
	protected void setPaintedRectangles(Rectangle mainRectangle, List<Rectangle> subRectangles) {
		synchronized (this.monitor) {
			this.mainRectangle = mainRectangle;
			this.subRectangles.clear();
			for (Rectangle r : subRectangles) this.subRectangles.add(r);
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintOperator(g, true, false, false);
	}
	
	/**
	 * This function paints this operator on a graphics object.
	 * 
	 * @param g
	 *            the graphics object.
	 * @param considerHighlighting
	 *            determines, whether mouse hover effects shall be painted.
	 * @param recursive
	 *            if true, all suboperators will be painted recursively.
	 * @param doLayout
	 *            causes the operator to lay out before painting.
	 */
	public void paintOperator(Graphics g, boolean considerHighlighting, boolean recursive, boolean doLayout) {
		if (doLayout) this.doLayout();
		
		// TODO: diry solution: determination, if border should be painted or not (hirarchical or's or and's).
		boolean candidateForBorderDisabling = (this instanceof AndOperator && this.getParent().getParent() instanceof AndOperator)
				|| (this instanceof OrOperator && this.getParent().getParent() instanceof OrOperator);
		boolean disableBorder = candidateForBorderDisabling && !(!this.transluscent && ((AbstractOperator)this.getParent().getParent()).transluscent);
		
		if (!disableBorder) {
			Graphics2D g2 = (Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			Color bg1 = this.getBackgroundColor1(considerHighlighting);
			Color bg2 = this.getBackgroundColor2(considerHighlighting);
			Color border = this.getBorderColor(considerHighlighting);
			
			Dimension ps = this.getPreferredSize();
			GradientPaint redtowhite = new GradientPaint(0, 0, bg1, ps.width-1, ps.height-1, bg2);
			
			synchronized (this.monitor) {
				g2.setPaint(redtowhite);
				g2.fillRoundRect(this.mainRectangle.x, this.mainRectangle.y, this.mainRectangle.width-1, this.mainRectangle.height-1, 8, 8);
				g2.setColor(border);
				g2.drawRoundRect(this.mainRectangle.x, this.mainRectangle.y, this.mainRectangle.width-1, this.mainRectangle.height-1, 8, 8);
				
				for (Rectangle r : this.subRectangles) {
					g2.setPaint(redtowhite);
					g2.fillRoundRect(r.x, r.y, r.width-1, r.height-1, 8, 8);
					g2.setColor(border);
					g2.drawRoundRect(r.x, r.y, r.width-1, r.height-1, 8, 8);
				}
				
				g2.setPaint(redtowhite);
				g2.fillRoundRect(this.mainRectangle.x+1, this.mainRectangle.y+1, this.mainRectangle.width-2, this.mainRectangle.height-2, 8, 8);
			}
		}
		
		Graphics2D gb = (Graphics2D)g.create();
		gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gb.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		this.paintOperator(gb, considerHighlighting);
		
		if (recursive) {
			for (Component c : this.getComponents()) {
				Rectangle b = c.getBounds();
				g.translate(b.x, b.y);
				((Bucket)c).paintSelf(g, considerHighlighting, recursive, doLayout);
				g.translate(-b.x, -b.y);
			}
		}
	}
	
	@Override
	public boolean contains(int x, int y) {
		if (!paintSqueezed) return super.contains(x, y);
		synchronized (this.monitor) {
			if (this.mainRectangle==null) return super.contains(x, y);
			if (this.mainRectangle.contains(x, y)) return true;
			for (Rectangle r : this.subRectangles) if (r.contains(x, y)) return true;
		}
		return false;
	}
	
	/**
	 * Returns whether this operator is similar to another one.
	 * 
	 * @param op
	 *            the operator to compare with.
	 * @return true, if this operator is similar to <a>op</a>
	 */
	public boolean isSimilar(AbstractOperator op) {
		if (!op.getClass().equals(this.getClass())) return false;
		
		Bucket[] thisBuckets = this.getBuckets();
		Bucket[] opBuckets = op.getBuckets();
		if (thisBuckets.length!=opBuckets.length) return false;
		for (int i=0; i<thisBuckets.length; i++) {
			AbstractOperator thisOp = thisBuckets[i].getOperator();
			AbstractOperator opOp = opBuckets[i].getOperator();
			if ((thisOp==null)!=(opOp==null)) return false;
			if (thisOp!=null && !thisOp.isSimilar(opOp)) return false;
		}
		return true;
	}
	
	private Bucket[] getBuckets() {
		Component[] components = this.getComponents();
		Bucket[] result = new Bucket[components.length];
		for (int i=0; i<components.length; i++) {
			result[i] = (Bucket)components[i];
		}
		return result;
	}
	
	protected void bucketAdded(Bucket bucket) {
		bucket.addChangeListener(this.listener);
	}
	
	protected void bucketRemoved(Bucket bucket) {
		bucket.removeChangeListener(this.listener);
	}
	
	private List<OperatorChangeListener> operatorChangeListeners = new ArrayList<OperatorChangeListener>();
	
	/**
	 * Add a <a>ChangeListener</a> to this operator. It will be notified about
	 * all changes within this or all sub-operators.
	 * 
	 * @param l
	 *            the <a>ChangeListener</a> to add.
	 */
	public void addChangeListener(OperatorChangeListener l) {
		synchronized(this.operatorChangeListeners) {
			this.operatorChangeListeners.add(l);
		}
	}
	
	/**
	 * Removes a <a>ChangeListener</a>.
	 * 
	 * @param l
	 *            the <a>ChangeListener</a> to remove.
	 */
	public void removeChangeListener(OperatorChangeListener l) {
		synchronized(this.operatorChangeListeners) {
			this.operatorChangeListeners.remove(l);
		}
	}
	
	/**
	 * Notifies all <a>ChangeListener</a>s about a change within this or any
	 * sub-operator.
	 */
	public void notifyChangeListeners_operatorChanged() {
		synchronized(this.operatorChangeListeners) {
			for (OperatorChangeListener l : this.operatorChangeListeners) {
				l.operatorChanged(this);
			}
		}
	}

}
