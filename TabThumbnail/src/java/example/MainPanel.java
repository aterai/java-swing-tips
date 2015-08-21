package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTabbedPane tabbedPane = new TabThumbnailTabbedPane();

    public MainPanel() {
        super(new BorderLayout());
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        ImageIcon icon = new ImageIcon(getClass().getResource("wi0124-48.png"));
        tabbedPane.addTab("wi0124-48.png", null, new JLabel(icon), "dummy");
        //addImageTab(tab, getClass().getResource("wi0124-48.png"));
        addImageTab(tabbedPane, getClass().getResource("tokeidai.jpg"));
        addImageTab(tabbedPane, getClass().getResource("CRW_3857_JFR.jpg")); //http://sozai-free.com/
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void addImageTab(JTabbedPane tabbedPane, URL url) {
        JScrollPane scroll = new JScrollPane(new JLabel(new ImageIcon(url)));
        File f = new File(url.getFile());
        tabbedPane.addTab(f.getName(), null, scroll, "dummy");
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TabThumbnailTabbedPane extends JTabbedPane {
    private int current = -1;
    private static final double SCALE = .15;
    private Component getTabThumbnail(int index) {
        Component c = getComponentAt(index);
        Icon icon = null;
        if (c instanceof JScrollPane) {
            c = ((JScrollPane) c).getViewport().getView();
            Dimension d = c.getPreferredSize();
            int newW = (int) (d.width  * SCALE);
            int newH = (int) (d.height * SCALE);
            BufferedImage image = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.scale(SCALE, SCALE);
            c.paint(g2);
            g2.dispose();
            icon = new ImageIcon(image);
        } else if (c instanceof JLabel) {
            icon = ((JLabel) c).getIcon();
        }
        return new JLabel(icon);
    }
    @Override public JToolTip createToolTip() {
        int index = current;
        if (index < 0) {
            return null;
        }

        final JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder());
        p.add(new JLabel(getTitleAt(index)), BorderLayout.NORTH);
        p.add(getTabThumbnail(index));

        JToolTip tip = new JToolTip() {
            @Override public Dimension getPreferredSize() {
                Insets i = getInsets();
                Dimension d = p.getPreferredSize();
                return new Dimension(d.width + i.left + i.right, d.height + i.top + i.bottom);
            }
        };
        tip.setComponent(this);
        LookAndFeel.installColorsAndFont(p, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
        tip.setLayout(new BorderLayout());
        tip.add(p);
        return tip;
    }
    @Override public String getToolTipText(MouseEvent e) {
        int index = indexAtLocation(e.getX(), e.getY());
        String str = (current == index) ? super.getToolTipText(e) : null;
        current = index;
        return str;
    }
}
