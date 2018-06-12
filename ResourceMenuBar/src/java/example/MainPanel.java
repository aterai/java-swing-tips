package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
    private static final BarFactory BAR_FACTORY = new BarFactory("resources.Main");

    private MainPanel() {
        super(new BorderLayout());
        initActions(getActions());
        JPanel menupanel = new JPanel(new BorderLayout());
        JMenuBar menuBar = BAR_FACTORY.createMenuBar();
        menupanel.add(menuBar, BorderLayout.NORTH);

        JToolBar toolBar = BAR_FACTORY.createToolBar();
        if (Objects.nonNull(toolBar)) {
            menupanel.add(toolBar, BorderLayout.SOUTH);
        }
        add(menupanel, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setPreferredSize(new Dimension(320, 240));
    }

    protected void initActions(Action... actlist) {
        BAR_FACTORY.initActions(actlist);
    }
    private Action[] getActions() {
        return new Action[] {
            new NewAction(),
            new ExitAction(),
            new HelpAction(),
            new VersionAction()
        };
        // return defaultActions;
    }
    // private final Action[] defaultActions = {
    //     new NewAction(),
    //     new ExitAction(),
    //     new HelpAction(),
    //     new VersionAction(),
    // };

    private static class NewAction extends AbstractAction {
        protected NewAction() {
            super("new");
        }
        @Override public void actionPerformed(ActionEvent e) {
            // dummy
        }
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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

class SaveAsAction extends AbstractAction {
    protected SaveAsAction() {
        super("saveAs");
    }
    @Override public void actionPerformed(ActionEvent e) {
        // dummy
    }
}

class ExitAction extends AbstractAction {
    protected ExitAction() {
        super("exit");
    }
    @Override public void actionPerformed(ActionEvent e) {
        Component root = null;
        Container parent = SwingUtilities.getUnwrappedParent((Component) e.getSource());
        if (parent instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu) parent;
            root = SwingUtilities.getRoot(popup.getInvoker());
        } else if (parent instanceof JToolBar) {
            JToolBar toolbar = (JToolBar) parent;
            if (((BasicToolBarUI) toolbar.getUI()).isFloating()) {
                root = SwingUtilities.getWindowAncestor(toolbar).getOwner();
            } else {
                root = SwingUtilities.getRoot(toolbar);
            }
        } else {
            root = SwingUtilities.getRoot(parent);
        }
        if (root instanceof Window) {
            Window window = (Window) root;
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }
}

class HelpAction extends AbstractAction {
    protected HelpAction() {
        super("help");
    }
    @Override public void actionPerformed(ActionEvent e) {
        // dummy
    }
}

class VersionAction extends AbstractAction {
    public static final String APP_NAME = "@title@";
    private static final String COPYRIGHT = "Copyright(C) 2006";
    private static final String VERSION = "0.0";
    private static final int RELEASE = 1;
    protected VersionAction() {
        super("version");
    }
    @Override public void actionPerformed(ActionEvent e) {
        JComponent c = (JComponent) e.getSource();
        Object[] obj = {APP_NAME + " - Version " + VERSION + "." + RELEASE, COPYRIGHT};
        JOptionPane.showMessageDialog(c.getRootPane(), obj, APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }
}
