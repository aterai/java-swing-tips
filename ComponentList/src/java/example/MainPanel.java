package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final Box box = Box.createVerticalBox();
    private final Component glue = Box.createVerticalGlue();
    public MainPanel() {
        super(new BorderLayout());
        box.setBorder(BorderFactory.createLineBorder(Color.RED, 10));
        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getViewport().add(box);
        add(makeToolBar(), BorderLayout.NORTH);
        add(scroll);
        addComp(new JLabel("aaaaaaaaaaaaaaaaaaaaaa"));
        addComp(MakeComponentUtil.makeButton());
        addComp(MakeComponentUtil.makeCheckBox());
        addComp(MakeComponentUtil.makeLabel());
        setPreferredSize(new Dimension(320, 240));
    }
    private void addComp(final JComponent comp) {
        comp.setMaximumSize(new Dimension(Short.MAX_VALUE, comp.getPreferredSize().height));
        box.remove(glue);
        box.add(Box.createVerticalStrut(5));
        box.add(comp);
        box.add(glue);
        box.revalidate();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                comp.scrollRectToVisible(comp.getBounds());
            }
        });
    }

    private JToolBar makeToolBar() {
        JToolBar bar = new JToolBar();
        bar.add(new AbstractAction("add JLabel") {
            @Override public void actionPerformed(ActionEvent ae) {
                addComp(MakeComponentUtil.makeLabel());
            }
        });
        bar.addSeparator();
        bar.add(new AbstractAction("add JButton") {
            @Override public void actionPerformed(ActionEvent ae) {
                addComp(MakeComponentUtil.makeButton());
            }
        });
        bar.addSeparator();
        bar.add(new AbstractAction("add JCheckBox") {
            @Override public void actionPerformed(ActionEvent ae) {
                addComp(MakeComponentUtil.makeCheckBox());
            }
        });
        return bar;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

final class MakeComponentUtil {
    private MakeComponentUtil() { /* Singleton */ }
    public static JComponent makeLabel() {
        JLabel label = new JLabel("Height: 50");
        label.setOpaque(true);
        label.setPreferredSize(new Dimension(0, 50));
        label.setBackground(Color.YELLOW.brighter());
        return label;
    }
    public static JComponent makeButton() {
        return new JButton(new AbstractAction("Beep Test") {
            @Override public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }
    public static JComponent makeCheckBox() {
        return new JCheckBox("bbbbbbbbbbbbbbbbbbbb", true);
    }
}
