package util.components.graphtab;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import util.Utils;

public class TabPane extends JComponent implements LayoutManager, AWTEventListener {
	private static final long serialVersionUID = 1L;
	
	private GraphTabPane graphTabPane;
	private JPanel empty = new JPanel();
	private int selectedIndex = -1;
	private List<GraphTab> tabs =  new ArrayList<GraphTab>();
	
	private List<FunctionTab> functions =  new ArrayList<FunctionTab>();
	
	private int mouseOver = -1;
	private int closeButtonHovered = -1; 
	private ImageIcon close = new ImageIcon(getClass().getResource("close.png"));
	private ImageIcon close_inact = new ImageIcon(getClass().getResource("close_inact.png"));
	
	private int leftSpace = 5;
	private Insets tabInsets = new Insets(10, 12, 10, 7);
	private int arc = 40;
	
	protected TabPane(GraphTabPane parent) {
		this.graphTabPane = parent;
		this.setLayout(this);
		this.addMouseListener(new MouseAdapter() {});
		this.addMouseMotionListener(new MouseMotionAdapter() {});
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
	}
	
	protected void addGraphTab(GraphTab graphTab, boolean select) {
		this.tabs.add(graphTab);
		this.add(graphTab.getTitleComponent());
		this.revalidate();
		this.repaint();
		
		if (this.tabs.size()==1 || select) this.setSelectedIndex(this.tabs.size()-1);
	}
	
	protected void addFunctionTab(FunctionTab functionTab) {
		this.functions.add(functionTab);
		this.add(functionTab.getTitleComponent());
		this.revalidate();
		this.repaint();
	}
	
	protected GraphTab removeGraphTab(int index) {
		if (index==this.selectedIndex) {
			if (this.selectedIndex<this.tabs.size()-1) setSelectedIndex(this.selectedIndex+1);
			else setSelectedIndex(this.tabs.size()-2);
		}
		
		GraphTab graphTab = this.tabs.remove(index);
		this.remove(graphTab.getTitleComponent());
		this.revalidate();
		this.repaint();
		
		if (this.selectedIndex>index) this.selectedIndex -= 1;
		return graphTab;
	}
	
	protected List<GraphTab> getGraphTabs() {
		return Collections.unmodifiableList(this.tabs);
	}
	
	protected void setSelectedIndex(int index) {
		if (index==this.selectedIndex) return;

		if (this.selectedIndex!=-1) {
			GraphTab tab = this.tabs.get(this.selectedIndex);
			tab.getTitleComponent().setActive(false);
		}
		
		if (index==-1) {
			this.graphTabPane.setContentComponent(this.empty);
		}
		else {
			GraphTab tab = this.tabs.get(index);
			this.graphTabPane.setContentComponent(tab.getContentComponent());
			tab.getTitleComponent().setActive(true);
		}
		
		this.selectedIndex = index;
		this.repaint();
		this.graphTabPane.notifyGraphTabListeners_selectedIndexChanged(index);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	
		g2.setColor(Color.gray);
		g2.drawLine(this.getWidth()-1, 0, this.getWidth()-1, this.getHeight());
		g2.setColor(Color.lightGray);
		g2.drawLine(this.getWidth()-2, 0, this.getWidth()-2, this.getHeight());

		for (int i=0; i<this.tabs.size(); i++) {
			RoundRectangle2D r = this.tabs.get(i).getRectangle();
			drawRectangle(r, g2, i==this.selectedIndex, i==this.mouseOver);
			// Close img
			Image img;
			if (this.closeButtonHovered==i) img = this.close.getImage();
			else img = this.close_inact.getImage();
			g2.drawImage(img, (int)r.getX() + (int)r.getWidth() - (this.close.getIconWidth() + this.tabInsets.right + this.arc), (int)r.getY() + this.tabInsets.top, this);
		}

		for (int i=0; i<this.functions.size(); i++) {
			RoundRectangle2D r = this.functions.get(i).getRectangle();
			drawRectangle(r, g2, false, i+this.tabs.size()==this.mouseOver);
			
		}
	}
	
	private void drawRectangle(RoundRectangle2D r, Graphics2D g2, boolean active, boolean mouseOver) {
		// Shadow
		g2.setColor(Color.lightGray);
		g2.translate(0, 1);
		g2.draw(r);
		g2.translate(0, -1);
		
		// Background
		if (active) {
			g2.setColor(Color.white);
		}
		else if (mouseOver) {
			g2.setPaint(new GradientPaint((int)r.getX(), (int)r.getY(), Color.white, (int)(r.getX() + r.getWidth()), (int)(r.getY() + r.getHeight()), Utils.computeColor(Color.LIGHT_GRAY, 0.4)));
		}
		else {
			g2.setPaint(new GradientPaint((int)r.getX(), (int)r.getY(), Color.white, (int)(r.getX() + r.getWidth()), (int)(r.getY() + r.getHeight()), Color.LIGHT_GRAY));
		}
		g2.fill(r);
		
		// Border
		g2.setColor(Color.gray);
		g2.draw(r);
		
		// Line
		if (!active) {
			g2.drawLine(this.getWidth()-1, (int)r.getY(), this.getWidth()-1, (int)(r.getY() + r.getHeight())+1);
		}
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {}

	@Override
	public void removeLayoutComponent(Component comp) {}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int tabWidth = this.leftSpace;
		int tabHeight = 0;// -1;
		
		for (GraphTab tab : this.tabs) {
			Dimension ps = tab.getTitleComponent().getPreferredSize();
			tabWidth = Math.max(tabWidth, this.leftSpace + this.tabInsets.left + ps.width + this.tabInsets.right);
			tabHeight += this.tabInsets.top + ps.height + this.tabInsets.bottom;
		}
		
		for (FunctionTab tab : this.functions) {
			Dimension ps = tab.getTitleComponent().getPreferredSize();
			tabWidth = Math.max(tabWidth, this.leftSpace + this.tabInsets.left + ps.width + this.tabInsets.right);
			tabHeight += this.tabInsets.top + ps.height + this.tabInsets.bottom;
		}
		
		return new Dimension(tabWidth, Math.max(0, tabHeight));
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		int y = 0;
		for (int i=0; i<this.tabs.size(); i++) {
			GraphTab tab = this.tabs.get(i);
			RoundRectangle2D r = tab.getRectangle();
			
			Dimension ps = tab.getTitleComponent().getPreferredSize();
			tab.getTitleComponent().setBounds(this.getWidth() - (ps.width + this.tabInsets.right), y+this.tabInsets.top, ps.width, ps.height);
			int width = this.tabInsets.left + ps.width + this.tabInsets.right + this.arc;
			r.setRoundRect(this.getWidth() - (width-this.arc), y, width, this.tabInsets.top + ps.height + this.tabInsets.bottom, this.arc, this.arc);
			y += this.tabInsets.top + ps.height + this.tabInsets.bottom;
		}
		
		for (int i=0; i<this.functions.size(); i++) {
			FunctionTab tab = this.functions.get(i);
			RoundRectangle2D r = tab.getRectangle();
			
			Dimension ps = tab.getTitleComponent().getPreferredSize();
			tab.getTitleComponent().setBounds(this.getWidth() - (ps.width + this.tabInsets.right), y+this.tabInsets.top, ps.width, ps.height);
			int width = this.tabInsets.left + ps.width + this.tabInsets.right + this.arc;
			r.setRoundRect(this.getWidth() - (width-this.arc), y, width, this.tabInsets.top + ps.height + this.tabInsets.bottom, this.arc, this.arc);
			y += this.tabInsets.top + ps.height + this.tabInsets.bottom;
		}
	}

	private int getIndexAtPosition(Point p) {
		for (int i=0; i<this.tabs.size(); i++) {
			if (this.tabs.get(i).getRectangle().contains(p)) return i;
		}
		for (int i=0; i<this.functions.size(); i++) {
			if (this.functions.get(i).getRectangle().contains(p)) return i + this.tabs.size();
		}
		return -1;
	}
	
	private boolean isCloseButton(Point p, int index) {
		RoundRectangle2D r = this.tabs.get(index).getRectangle();
		return new Rectangle2D.Double((int)r.getX() + (int)r.getWidth() - (this.close.getIconWidth() + this.tabInsets.right + this.arc), (int)r.getY() + this.tabInsets.top, this.close.getIconWidth(), this.close.getIconHeight()).contains(p);
	}
	
	private void setMouseOver(Point p) {
		int index = -1;
		if (p!=null) index = getIndexAtPosition(p);
		int closeButtonHovered = -1;
		if (index!=-1 && index<this.tabs.size()) closeButtonHovered = isCloseButton(p, index) ? index : -1;
		
		if (index==this.mouseOver && closeButtonHovered==this.closeButtonHovered) return;
		this.mouseOver = index;
		this.closeButtonHovered = closeButtonHovered;
		this.repaint();
	}

	@Override
	public void eventDispatched(AWTEvent event) {
		if (!(event instanceof MouseEvent)) return;
		
		MouseEvent me = (MouseEvent) event;
		if (!SwingUtilities.isDescendingFrom(me.getComponent(), this)) return;

		Point p = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), this); 
		
		switch (me.getID()) {
		case MouseEvent.MOUSE_CLICKED:
			break;
		case MouseEvent.MOUSE_ENTERED:
			setMouseOver(p);
			break;
		case MouseEvent.MOUSE_EXITED:
			setMouseOver(null);
			break;
		case MouseEvent.MOUSE_MOVED:
			setMouseOver(p);
			break;
		case MouseEvent.MOUSE_PRESSED:
			int index = getIndexAtPosition(p);
			if (index==-1) return;
			if (index>=this.tabs.size()) {
				this.functions.get(index-this.tabs.size()).execute(this.graphTabPane, this.selectedIndex);
			}
			else {
				if (isCloseButton(p, index)) this.graphTabPane.removeGraphTab(index);
				else this.graphTabPane.setSelectedIndex(index);
			}
			break;
		case MouseEvent.MOUSE_RELEASED:
			break;
		case MouseEvent.MOUSE_DRAGGED:
			break;
		}
	}

}
