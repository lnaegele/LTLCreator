package editor.toolpane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import util.Utils;

public class StatePainter extends JComponent {
	private static final long serialVersionUID = 1L;

	private Color backgroundColor1;
	private Color backgroundColor2;
	private Color borderColor;
	private String description;
	private String displayedDescription = null;
	private boolean mouseOver = false;
	
	public StatePainter(String description, Color backgroundColor1, Color backgroundColor2, Color borderColor) {
		this.description = description;
		this.backgroundColor1 = backgroundColor1;
		this.backgroundColor2 = backgroundColor2;
		this.borderColor = borderColor;
		this.setPreferredSize(new Dimension(50,50));
		this.setFont(new Font("Arial", Font.PLAIN, 10));
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				StatePainter.this.mouseOver = true;
				StatePainter.this.repaint();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				StatePainter.this.mouseOver = false;
				StatePainter.this.repaint();
			}
		});
	}
	
	public String getDescription() {
		return this.description;
	}
	
	@Override
	public void paint(Graphics g) {
		Color backgroundColor1 = this.backgroundColor1;
		Color backgroundColor2 = this.backgroundColor2;
		Color borderColor = this.borderColor;
		
		if (this.mouseOver) {
			double factor = 0.3;
			backgroundColor1 = Utils.computeColor(backgroundColor1, factor);
			backgroundColor2 = Utils.computeColor(backgroundColor2, factor);
			borderColor = Utils.computeColor(borderColor, factor);
		}
		
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		GradientPaint redtowhite = new GradientPaint(0, 0, backgroundColor1, this.getWidth()-1, this.getHeight()-1, backgroundColor2);
		g2.setPaint(redtowhite);
		
		g2.fillRoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, 8, 8);

		g2.setColor(borderColor);
		g2.drawRoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, 8, 8);
		
		if (this.displayedDescription==null) {
			this.setToolTipText(null);
			this.displayedDescription = this.description;
			FontMetrics fm = g2.getFontMetrics();
			int maxWidth = this.getPreferredSize().width - 10;
			if (fm.stringWidth(this.description)>maxWidth) {
				while (fm.stringWidth(this.displayedDescription+"...")>maxWidth) {
					this.displayedDescription = this.displayedDescription.substring(0, this.displayedDescription.length()-1);
				}
				this.setToolTipText(this.description);
				this.displayedDescription = displayedDescription + "...";
			}
		}
		
		Utils.paintTextCentered(g2, this.displayedDescription, this.getWidth()/2, this.getHeight()/2);
	}
}
