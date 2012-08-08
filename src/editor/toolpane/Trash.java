package editor.toolpane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.ImageIcon;

import util.dnd.Draggable;
import util.dnd.DropTarget;

public class Trash extends Component implements DropTarget {
	private static final long serialVersionUID = 1L;

	private Image trash_empty = new ImageIcon(getClass().getResource("trash_empty.png")).getImage();
	private Image trash_full = new ImageIcon(getClass().getResource("trash_full.png")).getImage();
	
	private boolean open = false;
	
	public Trash() {
		this.setPreferredSize(new Dimension(48, 48));
	}
	
	@Override
	public void paint(Graphics g) {
		if (!open) g.drawImage(this.trash_empty, 0, 0, this);
		else g.drawImage(this.trash_full, 0, 0, this);
	}
	
	@Override
	public boolean accepts(Draggable object) {
		return true;
	}

	@Override
	public void drop(Draggable object, Point point, DropTarget previousParent) {
		// just ignore it
	}

	@Override
	public void setDraggingMouseOver(boolean hoverActive) {
		if (this.open==hoverActive) return;
		this.open = hoverActive;
		this.repaint();
	}

	@Override
	public void undock(Draggable draggable) {
		// Trash is only one way
	}

}
