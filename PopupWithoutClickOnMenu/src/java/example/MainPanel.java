package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String TXT = "JMenu: hover(show popup automatically) on cursor";
    private final JCheckBox check = new JCheckBox(TXT, true);

    private MainPanel() {
        super(new BorderLayout());
        add(check, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    public JMenuBar makeMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.add(makeMenuItem("Open"));
        menu.add(makeMenuItem("Save"));
        menu.add(makeMenuItem("Exit"));
        bar.add(menu);

        menu = new JMenu("Edit");
        menu.add(makeMenuItem("Undo"));
        menu.add(makeMenuItem("Redo"));
        menu.addSeparator();
        menu.add(makeMenuItem("Cut"));
        menu.add(makeMenuItem("Copy"));
        menu.add(makeMenuItem("Paste"));
        menu.add(makeMenuItem("Delete"));
        bar.add(menu);

        menu = new JMenu("Test");
        menu.add(makeMenuItem("JMenuItem1"));
        menu.add(makeMenuItem("JMenuItem2"));
        JMenu sub = new JMenu("JMenu");
        sub.add(makeMenuItem("JMenuItem4"));
        sub.add(makeMenuItem("JMenuItem5"));
        menu.add(sub);
        menu.add(makeMenuItem("JMenuItem3"));
        bar.add(menu);

        visitAll(bar, new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (check.isSelected()) {
                    ((AbstractButton) e.getComponent()).doClick();
                }
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (check.isSelected()) {
                    ((AbstractButton) e.getComponent()).doClick();
                }
            }
        });
        return bar;
    }
    private static void visitAll(Container p, MouseListener l) {
        for (Component comp: p.getComponents()) {
            if (comp instanceof JMenu) {
                ((JMenu) comp).addMouseListener(l);
            }
//             if (comp instanceof Container) {
//                 Container c = (Container) comp;
//                 if (c.getComponentCount() > 0) {
//                     visitAll(c, l);
//                 }
//                 if (c instanceof JMenu) {
//                     c.addMouseListener(l);
//                 }
//             }
        }
    }
    private static JMenuItem makeMenuItem(String str) {
        JMenuItem item = new JMenuItem(str);
        item.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
        return item;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        MainPanel mainPanel = new MainPanel();
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.setJMenuBar(mainPanel.makeMenuBar());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
