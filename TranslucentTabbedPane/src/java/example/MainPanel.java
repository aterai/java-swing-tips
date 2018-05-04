package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final Image bgImage = new ImageIcon(getClass().getResource("test.png")).getImage();
    private MainPanel() {
        super(new BorderLayout());
        Color bgc = new Color(110, 110, 0, 100);
        Color fgc = new Color(255, 255, 0, 100);

        UIManager.put("TabbedPane.shadow", fgc);
        UIManager.put("TabbedPane.darkShadow", fgc);
        UIManager.put("TabbedPane.light", fgc);
        UIManager.put("TabbedPane.highlight", fgc);
        UIManager.put("TabbedPane.tabAreaBackground", fgc);
        UIManager.put("TabbedPane.unselectedBackground", fgc);
        UIManager.put("TabbedPane.background", bgc);
        UIManager.put("TabbedPane.foreground", Color.WHITE);
        UIManager.put("TabbedPane.focus", fgc);
        UIManager.put("TabbedPane.contentAreaColor", fgc);
        UIManager.put("TabbedPane.selected", fgc);
        UIManager.put("TabbedPane.selectHighlight", fgc);
        UIManager.put("TabbedPane.borderHightlightColor", fgc);

        JPanel tab1panel = new JPanel();
        tab1panel.setBackground(new Color(0, 220, 220, 50));

        JPanel tab2panel = new JPanel();
        tab2panel.setBackground(new Color(220, 0, 0, 50));

        JPanel tab3panel = new JPanel();
        tab3panel.setBackground(new Color(0, 0, 220, 50));

        JCheckBox cb = new JCheckBox("setOpaque(false)");
        cb.setOpaque(false);
        cb.setForeground(Color.WHITE);
        tab3panel.add(cb);
        tab3panel.add(new JCheckBox("setOpaque(true)"));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tab 1", tab1panel);
        tabs.addTab("Tab 2", tab2panel);
        tabs.addTab("Tab 3", new AlphaContainer(tab3panel));

        add(tabs);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(320, 240));
    }

    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
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
//                  | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
//         }

        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// https://tips4java.wordpress.com/2009/05/31/backgrounds-with-transparency/
class AlphaContainer extends JPanel {
    private final JComponent component;
    protected AlphaContainer(JComponent component) {
        super(new BorderLayout());
        this.component = component;
        component.setOpaque(false);
        add(component);
    }
    @Override public boolean isOpaque() {
        return false;
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(component.getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lafRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
        lafItem.setActionCommand(lafClassName);
        lafItem.setHideActionText(true);
        lafItem.addActionListener(e -> {
            ButtonModel m = lafRadioGroup.getSelection();
            try {
                setLookAndFeel(m.getActionCommand());
            } catch (ClassNotFoundException | InstantiationException
                   | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        lafRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
