package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final Icon HSTRUT = new ColorIcon(new Color(0x0, true)); //MetalLookAndFeel
    private MainPanel() {
        super(new BorderLayout());
        //UIManager.put("MenuItem.disabledAreNavigable", Boolean.FALSE);
        UIManager.put("MenuItem.disabledForeground", Color.BLACK);

        JMenuItem item0 = new JMenuItem("JMenuItem.setEnabled(false);");
        item0.setEnabled(false);

        JLabel item1 = new JLabel("JLabel + EmptyBorder");
        //item1.setIcon(HSTRUT);
        item1.setBorder(BorderFactory.createEmptyBorder(2, 32, 2, 2));

        JPanel item2 = new JPanel(new BorderLayout()) {
            @Override public void updateUI() {
                super.updateUI();
                setOpaque(false); //NimbusLookAndFeel
            }
        };
        item2.add(new JMenuItem("JPanel with JMenuItem", HSTRUT) {
            @Override public boolean contains(int x, int y) {
                return false; //disable mouse events
            }
        });

        JMenuItem item3 = new JMenuItem("\u200B"); //, HSTRUT);
        //item3.setLayout(new BorderLayout());
        item3.setBorder(BorderFactory.createEmptyBorder()); //NimbusLookAndFeel
        item3.setEnabled(false);
        //item3.setDisabledIcon(HSTRUT);
        item3.add(new JMenuItem("JMenuItem(disabled) with JMenuItem", HSTRUT) {
            @Override public boolean contains(int x, int y) {
                return false; //disable mouse events
            }
        });

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(makeMenu("Test0", item0));
        menuBar.add(makeMenu("Test1", item1));
        menuBar.add(makeMenu("Test2", item2));
        menuBar.add(makeMenu("Test3", item3));
        // EventQueue.invokeLater(() -> ((JFrame) SwingUtilities.getWindowAncestor(this)).setJMenuBar(menuBar));
        EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

        add(new JScrollPane(new JTextArea()));
        setPreferredSize(new Dimension(320, 240));
    }

    private static JMenu makeMenu(String title, JComponent item) {
        JMenu menu = new JMenu(title);
        menu.add(item);
        menu.addSeparator();
        menu.add("JMenuItem").addActionListener(e -> System.out.println("actionPerformed"));
        menu.add("JMenuItem + Icon").setIcon(new ColorIcon(Color.RED));
        menu.add("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        return menu;
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

class ColorIcon implements Icon {
    private final Color color;
    protected ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(color);
        g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
