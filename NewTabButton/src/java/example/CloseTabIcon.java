package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

class CloseTabIcon implements Icon {
    private final Color color;
    public CloseTabIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        //g.translate(x, y);
        g.setColor(color);
        g.drawLine(2, 2, 9, 9);
        g.drawLine(2, 3, 8, 9);
        g.drawLine(3, 2, 9, 8);
        g.drawLine(9, 2, 2, 9);
        g.drawLine(9, 3, 3, 9);
        g.drawLine(8, 2, 2, 8);
        //g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}

class PlusIcon implements Icon {
    private static Rectangle viewRect = new Rectangle();
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);

        Insets i = c instanceof JComponent ? ((JComponent) c).getInsets() : new Insets(0, 0, 0, 0);
        Dimension size = c.getSize();

        viewRect.x      = i.left;
        viewRect.y      = i.top;
        viewRect.width  = size.width  - i.right  - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        OperaTabViewButtonUI.tabPainter(g2, viewRect);

        g2.setPaint(Color.WHITE);
        int w = viewRect.width;
        int a = w / 2;
        int b = w / 3;
        w -= 2;
        g2.drawLine(a,     b,     a,     w - b);
        g2.drawLine(a - 1, b,     a - 1, w - b);
        g2.drawLine(b,     a,     w - b, a);
        g2.drawLine(b,     a - 1, w - b, a - 1);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 24;
    }
    @Override public int getIconHeight() {
        return 24;
    }
}
