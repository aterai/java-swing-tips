package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final BarFactory BAR_FACTORY = new BarFactory("resources.Main");

    public MainPanel() {
        super(new BorderLayout());
        initActions(getActions());
        JPanel menupanel = new JPanel(new BorderLayout());
        JMenuBar menuBar = BAR_FACTORY.createMenubar();
        //if (menuBar != null)
        menupanel.add(menuBar, BorderLayout.NORTH);

        JToolBar toolBar = BAR_FACTORY.createToolbar();
        if (toolBar != null) {
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
        //return defaultActions;
    }
//     private final Action[] defaultActions = {
//         new NewAction(),
//         new ExitAction(),
//         new HelpAction(),
//         new VersionAction(),
//     };

    private static class NewAction extends AbstractAction {
        public NewAction() {
            super("new");
        }
        @Override public void actionPerformed(ActionEvent e) {
            // dummy
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

class SaveAsAction extends AbstractAction {
    public SaveAsAction() {
        super("saveAs");
    }
    @Override public void actionPerformed(ActionEvent e) {
        // dummy
    }
}

class ExitAction extends AbstractAction {
    public ExitAction() {
        super("exit");
    }
    @Override public void actionPerformed(ActionEvent e) {
        //exitActionPerformed();
        //saveLocation(prefs);
        Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
        if (w != null) {
            w.dispose();
        }
        //System.exit(0);
    }
}

class HelpAction extends AbstractAction {
    public HelpAction() {
        super("help");
    }
    @Override public void actionPerformed(ActionEvent e) {
        // dummy
    }
}

class VersionAction extends AbstractAction {
    public static final String APP_NAME   = "@title@";
    private static final String COPYRIGHT = "Copyright(C) 2006";
    private static final String VERSION   = "0.0";
    private static final int    RELEASE   = 1;
    public VersionAction() {
        super("version");
    }
    @Override public void actionPerformed(ActionEvent e) {
        JComponent c = (JComponent) e.getSource();
        Object[] obj = {APP_NAME + " - Version " + VERSION + "." + RELEASE, COPYRIGHT};
        JOptionPane.showMessageDialog(c.getRootPane(), obj, APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }
}
