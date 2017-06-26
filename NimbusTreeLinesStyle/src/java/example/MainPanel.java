package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout());

        JTree tree = new JTree();
        UIDefaults d = new UIDefaults();
        d.put("Tree.drawVerticalLines", Boolean.TRUE);
        d.put("Tree.drawHorizontalLines", Boolean.TRUE);
        d.put("Tree.linesStyle", "dashed");
        tree.putClientProperty("Nimbus.Overrides", d);

        add(makeTitledPane("Default", new JScrollPane(new JTree())));
        add(makeTitledPane("linesStyle: dashed", new JScrollPane(tree)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPane(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        // swing - Nimbus JTree presentation error under java version 1.8 - Stack Overflow
        // https://stackoverflow.com/questions/44655203/nimbus-jtree-presentation-error-under-java-version-1-8
        // // UIManager.put("Tree.drawVerticalLines", true); // bug?
        // UIManager.getLookAndFeelDefaults().put("Tree.drawVerticalLines", true);
        // UIManager.put("Tree.drawHorizontalLines", true);
        // UIManager.put("Tree.linesStyle", "dashed");

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
