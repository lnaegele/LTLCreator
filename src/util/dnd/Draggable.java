package util.dnd;

import java.awt.Point;




public interface Draggable {

	public void setMouseOver(boolean mouseOver);
	public DropTarget getCurrentParent();
	public void mouseClick(Point point);
	
}
