package util;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class Utils {

	public static void paintTextCentered(Graphics g, String text, int centerX, int centerY) {
		FontMetrics textMetrics = g.getFontMetrics();  
		int textWidth = textMetrics.stringWidth(text);
	    int textPosX = centerX - (textWidth / 2);
	    int textPosY = centerY + (int)(textMetrics.getLineMetrics(text, g).getAscent() / 2);
	    
	    g.drawString(text, textPosX, textPosY);
	}
	
	public static void paintArrow(Graphics g, Point start, int length) {
			Graphics2D gb = (Graphics2D)g;
			
			// Pfeilspitze
	        double s_width = 10;
	        double s_height = 10;
	        
//	        BasicStroke s2 = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
//	        gb.setStroke(s2);
	        
	        Line2D line = new Line2D.Double(start.x, start.y, start.x + length - s_width, start.y);
	        gb.draw(line);
			
	        g.translate(start.x + length, start.y);
	        Path2D.Double path = new Path2D.Double();
	        path.moveTo ( -s_width, -s_height/2 );
	        path.quadTo ( -s_width/3, -s_height/5, 0, 0);
	        path.quadTo ( -s_width/3, s_height/5, -s_width, s_height/2);
	        path.lineTo (-s_width*0.9, 0);
	        path.lineTo (-s_width, -s_height/2);
	        gb.fill ( path );
	        g.translate(-(start.x + length), -start.y);   
			
	}
	
	/**
	 * Factor 0 doesn't change anything, -1 results in black and 1 in white. All values in between darken or lighten the color.
	 * @param color
	 * @param factor
	 * @return
	 */
	public static Color computeColor(Color color, double factor) {
		// -1 .. 1
		factor = Math.min(1, Math.max(-1, factor));
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		
		if (factor>0) {
			red += (255-red) * factor;
			green += (255-green) * factor;
			blue += (255-blue) * factor;
		}
		else if (factor<0) {
			red += red * factor;
			green += green * factor;
			blue += blue * factor;
		}
		
		return new Color(red, green, blue);
	}

}
