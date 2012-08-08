package editor.toolpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import model.AbstractOperator;

import util.Utils;
import util.dnd.Draggable;
import util.dnd.DraggableProvider;
import util.dnd.DropTarget;

public class OperatorProvider extends Component implements DraggableProvider, DropTarget {
	private static final long serialVersionUID = 1L;
	
	private AbstractOperator operator;
	private String description;
	private Dimension size = new Dimension(50, 50);
	private Font font = new Font("Arial", Font.PLAIN, 10);
	private boolean draggingMouseOver = false;
	
	private OperatorProvider(String description, AbstractOperator operator) {
		this.operator = operator;
		this.description = description;
		this.setPreferredSize(this.size);
	}
	
	public static OperatorProvider createOperatorProvider(String description, AbstractOperator operator) {
		return new OperatorProvider(description, operator);
	}
	
	@Override
	public void paint(Graphics g) {
		Color bg1 = this.operator.getBackgroundColor1(false);
		Color bg2 = this.operator.getBackgroundColor2(false);
		Color border = this.operator.getBorderColor(false);
		
		if (this.draggingMouseOver) {
			double factor = 0.3;
			bg1 = Utils.computeColor(bg1, factor);
			bg2 = Utils.computeColor(bg2, factor);
			border = Utils.computeColor(border, factor);
		}
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		GradientPaint redtowhite = new GradientPaint(0, 0, bg1, this.getWidth()-1, this.getHeight()-1, bg2);
		g2.setPaint(redtowhite);
		
		g.fillRoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, 8, 8);

		g.setColor(border);
		g.drawRoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, 8, 8);
		
		g2.setFont(this.font);
		Utils.paintTextCentered(g2, this.description, this.getWidth()/2, this.getHeight()/2);
		
	}

	@Override
	public Draggable getDraggable() {
		return this.operator.createNewInstance();
	}

	@Override
	public boolean accepts(Draggable draggable) {
		Draggable d = this.getDraggable();
		return (draggable instanceof AbstractOperator) &&
				draggable.getCurrentParent()!=null &&
				((AbstractOperator)d).getComponents().length>0 &&
				((AbstractOperator)d).getComponent(0) instanceof DropTarget;
	}

	@Override
	public void drop(Draggable draggable, Point point, DropTarget previousParent) {
		Draggable d = this.getDraggable();
		((DropTarget)((AbstractOperator)d).getComponent(0)).drop(draggable, new Point(), previousParent);
		previousParent.drop(d, new Point(), null);
	}

	@Override
	public void undock(Draggable draggable) {}

	@Override
	public void setDraggingMouseOver(boolean hoverActive) {
		this.draggingMouseOver = hoverActive;
		this.repaint();
	}
	
}
