package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private final JTabbedPane tabs = new JTabbedPane() {
        @Override public void updateUI() {
            super.updateUI();
            UIManager.put("TabbedPane.highlight", Color.GRAY);
            setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            setUI(new IsoscelesTrapezoidTabbedPaneUI());
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        tabs.addTab("JTextArea",  new JScrollPane(new JTextArea()));
        tabs.addTab("JTree",      new JScrollPane(new JTree()));
        tabs.addTab("JButton",    new JButton("button"));
        tabs.addTab("JSplitPane", new JSplitPane());
        add(tabs);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
//         }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class IsoscelesTrapezoidTabbedPaneUI extends BasicTabbedPaneUI {
    private static final int ADJ2 = 3;
    private final Color selectedTabColor = UIManager.getColor("TabbedPane.selected");
    private final Color tabBackgroundColor = Color.LIGHT_GRAY;
    private final Color tabBorderColor = Color.GRAY;

    @Override protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) { //NOPMD
        int tabCount = tabPane.getTabCount();

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();

        for (int i = runCount - 1; i >= 0; i--) {
            int start = tabRuns[i];
            int next = tabRuns[(i == runCount - 1) ? 0 : i + 1];
            int end = next != 0 ? next - 1 : tabCount - 1; //NOPMD
            // for (int j = start; j <= end; j++) {
            // http://stackoverflow.com/questions/41566659/tabs-rendering-order-in-custom-jtabbedpane
            for (int j = end; j >= start; j--) {
                if (j != selectedIndex && rects[j].intersects(clipRect)) {
                    paintTab(g, tabPlacement, rects, j, iconRect, textRect);
                }
            }
        }
        if (selectedIndex >= 0 && rects[selectedIndex].intersects(clipRect)) {
            paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
        }
    }
    @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        // Do nothing
    }
    @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // Do nothing
    }
    @Override protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        super.paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
        Rectangle selRect = getTabBounds(selectedIndex, calcRect);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(selectedTabColor);
        g2.drawLine(selRect.x - ADJ2 + 1, y, selRect.x + selRect.width + ADJ2 - 1, y);
        g2.dispose();
    }
    @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int textShiftOffset = isSelected ? 0 : 1;

        Rectangle clipRect = g2.getClipBounds();
        clipRect.grow(ADJ2 + 1, 0);
        g2.setClip(clipRect);

        GeneralPath trapezoid = new GeneralPath();
        trapezoid.moveTo(x - ADJ2,     y + h);
        trapezoid.lineTo(x + ADJ2,     y + textShiftOffset);
        trapezoid.lineTo(x + w - ADJ2, y + textShiftOffset);
        trapezoid.lineTo(x + w + ADJ2, y + h);
        //trapezoid.closePath();

        //TEST: g2.setColor(isSelected ? tabPane.getBackground() : tabBackgroundColor);
        g2.setColor(isSelected ? selectedTabColor : tabBackgroundColor);
        g2.fill(trapezoid);

        g2.setColor(tabBorderColor);
        g2.draw(trapezoid);

        //GeneralPath shape = new GeneralPath();
        //shape.moveTo(x - ADJ2,     y + h);
        //shape.lineTo(x + ADJ2,     y + textShiftOffset);
        //shape.lineTo(x + w - ADJ2, y + textShiftOffset);
        //shape.lineTo(x + w + ADJ2, y + h);
        //shape.closePath();
        //g2.setColor(isSelected ? selectedTabColor : tabBackgroundColor);
        //g2.fill(shape);
        //
        //GeneralPath border = new GeneralPath();
        //border.moveTo(x - ADJ2,     y + h);
        // // if (isSelected || tabIndex == 0) {
        // //     border.moveTo(x - ADJ2, y + h);
        // // } else {
        // //     // pentagon
        // //     border.moveTo(x + ADJ2, y + h);
        // //     border.lineTo(x, (y + h - 1) / 2);
        // //   }
        //border.lineTo(x + ADJ2,     y + textShiftOffset);
        //border.lineTo(x + w - ADJ2, y + textShiftOffset);
        //border.lineTo(x + w + ADJ2, y + h);
        //
        //g2.setColor(tabBorderColor);
        //g2.draw(border);

        g2.dispose();
    }
}
