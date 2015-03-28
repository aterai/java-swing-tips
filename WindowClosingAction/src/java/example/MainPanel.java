package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JPopupMenu popup = new JPopupMenu();
        initMenu(popup);

        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("File");
        initMenu(menu);
        bar.add(menu);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new JLabel("Floatable JToolBar:"));
        toolBar.add(Box.createGlue());
        toolBar.add(new ExitAction());

        JTree tree = new JTree();
        tree.setComponentPopupMenu(popup);
        add(bar, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        add(toolBar, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static class ExitAction extends AbstractAction {
        public ExitAction() {
            super("Exit");
        }
        @Override public void actionPerformed(ActionEvent e) {
            JComponent c = (JComponent) e.getSource();
            Window window = null;
            Container parent = c.getParent();
            if (parent instanceof JPopupMenu) {
                JPopupMenu popup = (JPopupMenu) parent;
                JComponent invoker = (JComponent) popup.getInvoker();
                window = SwingUtilities.getWindowAncestor(invoker);
            } else if (parent instanceof JToolBar) {
                JToolBar toolbar = (JToolBar) parent;
                if (((BasicToolBarUI) toolbar.getUI()).isFloating()) {
                    window = SwingUtilities.getWindowAncestor(toolbar).getOwner();
                } else {
                    window = SwingUtilities.getWindowAncestor(toolbar);
                }
            } else {
                Component invoker = c.getParent();
                window = SwingUtilities.getWindowAncestor(invoker);
            }
            if (Objects.nonNull(window)) {
                //window.dispose();
                window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
            }
        }
    }
    private static void initMenu(JComponent p) {
        for (JComponent c: Arrays.asList(
                new JMenuItem("Open(dummy)"), new JMenuItem("Save(dummy)"),
                new JSeparator(), new JMenuItem(new ExitAction()))) {
            p.add(c);
        }
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
