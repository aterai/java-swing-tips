package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class OperaTabViewButtonUI extends BasicTabViewButtonUI {
    private static Dimension size = new Dimension();
    private static Rectangle viewRect = new Rectangle();
    private static Rectangle iconRect = new Rectangle();
    private static Rectangle textRect = new Rectangle();
    private static final int CLOSEICON_WIDTH = 12;
    //private static final TabViewButtonUI tabViewButtonUI = new OperaTabViewButtonUI();
    public static ComponentUI createUI(JComponent c) {
        return new OperaTabViewButtonUI();
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        c.setBorder(BorderFactory.createEmptyBorder());
        c.setForeground(Color.WHITE);

        tabViewButton.setTextColor(new Color(230, 245, 255));
        tabViewButton.setPressedTextColor(Color.WHITE);
        tabViewButton.setRolloverTextColor(Color.WHITE);
        tabViewButton.setRolloverSelectedTextColor(Color.WHITE);
        tabViewButton.setSelectedTextColor(Color.WHITE);
    }
    @Override public synchronized void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D)g;
        AbstractButton b = (AbstractButton) c;
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = c.getFontMetrics(f);
        Icon icon = b.getIcon();

        Insets i = c.getInsets();
        b.getSize(size);
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = size.width - i.right - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.x = textRect.y = textRect.width = textRect.height = 0;

        //g2.setColor(Color.CYAN); //c.getBackground());
        //g2.fillRect(0, 0, size.width-1, size.height);
        //g2.fill(viewRect);
        tabPainter(g2, viewRect);

        viewRect.width = size.width - i.right - viewRect.x - CLOSEICON_WIDTH;
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), icon, //altIcon != null ? altIcon : getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            b.getText() == null ? 0 : b.getIconTextGap());
        if(text==null) {
            return;
        }

        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if(v==null) {
            textRect.x += 4;
            paintText(g, b, textRect, text);
        }else{
            v.paint(g, textRect);
        }
        if(icon!=null) {
            icon.paintIcon(c, g, iconRect.x+4, iconRect.y+2);
        }

        ButtonModel model = b.getModel();
        if(!model.isSelected() && !model.isArmed() && !model.isRollover()) {
            g2.setPaint(new Color(0,0,0,100));
            g2.fillRect(0,0,size.width,size.height);
            //g2.fill(viewRect);
        }
    }

    public static void tabPainter(Graphics2D g2, Rectangle r) {
        Rectangle r1 = new Rectangle(r.x, r.y,              r.width, r.height/2);
        Rectangle r2 = new Rectangle(r.x, r.y+r.height/2,   r.width, r.height/2);
        Rectangle r3 = new Rectangle(r.x, r.y+r.height/2-2, r.width, r.height/4);

        g2.setPaint(new GradientPaint(0, r1.y, new Color(132,162,180), 0, r1.y+r1.height, new Color(103,133,152), true));
        g2.fill(r1);
        g2.setPaint(new GradientPaint(0, r2.y, new Color(50,73,87),    0, r2.y+r2.height, new Color(60,86,101), true));
        g2.fill(r2);
        g2.setPaint(new GradientPaint(0, r3.y, new Color(0,0,0,30),    0, r3.y+r3.height, new Color(0,0,0,5), true));
        g2.fill(r3);

        g2.setPaint(new Color(39,56,67)); //g2.setPaint(Color.GREEN);
        g2.drawLine(r.x, r.y, r.x+r.width, r.y);

        g2.setPaint(new Color(255,255,255,30)); //g2.setPaint(Color.RED);
        g2.drawLine(r.x+1, r.y+1, r.x+r.width, r.y+1);

        g2.setPaint(new Color(255,255,255,60)); //g2.setPaint(Color.BLUE);
        g2.drawLine(r.x, r.y, r.x, r.y+r.height);

        g2.setPaint(new Color(39,56,67,250)); //g2.setPaint(Color.YELLOW);
        g2.drawLine(r.x+r.width-1, r.y, r.x+r.width-1, r.y+r.height);

        //g2.setPaint(Color.PINK);
        g2.drawLine(r.x, r.y+r.height-1, r.x+r.width-1, r.y+r.height-1);
    }
}
