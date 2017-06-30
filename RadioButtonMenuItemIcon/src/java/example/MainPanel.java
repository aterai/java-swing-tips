package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.plaf.UIResource;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JTextArea());
        setPreferredSize(new Dimension(320, 240));
    }
    public JMenuBar createMenuBar() {
        JMenu menu = new JMenu("RadioButtonMenuItem-Test");

        JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem("default", true);
        menu.add(rbmi);

        UIManager.put("RadioButtonMenuItem.checkIcon", new RadioButtonMenuItemIcon1());
        rbmi = new JRadioButtonMenuItem("ANTIALIASING", true);
        menu.add(rbmi);

        UIManager.put("RadioButtonMenuItem.checkIcon", new RadioButtonMenuItemIcon2());
        rbmi = new JRadioButtonMenuItem("fillOval", true);
        menu.add(rbmi);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        return menuBar;
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
        MainPanel p = new MainPanel();
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(p);
        frame.setJMenuBar(p.createMenuBar());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//com.sun.java.swing.plaf.windows.WindowsIconFactory.java
class RadioButtonMenuItemIcon1 implements Icon, UIResource, Serializable {
    private static final long serialVersionUID = 1L;
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        if (!(c instanceof AbstractButton)) {
            return;
        }
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        if (model.isSelected()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.fillRoundRect(3, 3, getIconWidth() - 6, getIconHeight() - 6, 4, 4);
            g2.dispose();
        }
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}

class RadioButtonMenuItemIcon2 implements Icon, UIResource, Serializable {
    private static final long serialVersionUID = 1L;
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        if (!(c instanceof AbstractButton)) {
            return;
        }
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        if (model.isSelected()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            //g2.fillRoundRect(3, 3, getIconWidth() - 6, getIconHeight() - 6, 4, 4);
            g2.fillOval(2, 2, getIconWidth() - 5, getIconHeight() - 5);
            //g2.fillArc(2, 2, getIconWidth() - 5, getIconHeight() - 5, 0, 360);
            g2.dispose();
        }
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
