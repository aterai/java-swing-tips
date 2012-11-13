package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final BarFactory barFactory;
    private final JFrame frame;

    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        barFactory = new BarFactory("resources.Main");
        initActions(getActions());
        JPanel menupanel = new JPanel(new BorderLayout());
        JMenuBar menuBar = barFactory.createMenubar();
        if(menuBar!=null) {
            menupanel.add(menuBar, BorderLayout.NORTH);
        }
        JToolBar toolBar = barFactory.createToolbar();
        if(toolBar!=null) {
            menupanel.add(toolBar, BorderLayout.SOUTH);
        }
        add(menupanel, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setPreferredSize(new Dimension(320,200));
    }

    protected void initActions(Action[] actlist) {
        barFactory.initActions(actlist);
    }
    private Action[] getActions() {
        return defaultActions;
    }
    private Action[] defaultActions = {
        new NewAction(),
        //new OpenAction(),
        new ExitAction(),
        new HelpAction(),
        new VersionAction(),
    };

//     protected JToolBar createToolbar() {
//         return barFactory.createToolbar();
//     }
//     protected JMenuBar createMenubar() {
//         return barFactory.createMenubar();
//     }
//     protected JButton getToolButton(String cmd) {
//         return barFactory.getToolButton(cmd);
//     }
//     protected JMenuItem getMenuItem(String cmd) {
//         return barFactory.getMenuItem(cmd);
//     }
//     protected JMenu getMenu(String cmd) {
//         return barFactory.getMenu(cmd);
//     }
//     protected Action getAction(String cmd) {
//         return barFactory.getAction(cmd);
//     }

    private static class NewAction extends AbstractAction{
        public NewAction() {
            super("new");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            // dummy
        }
    }

    private static class SaveAsAction extends AbstractAction{
        public SaveAsAction() {
            super("saveAs");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            // dummy
        }
    }

    private class ExitAction extends AbstractAction{
        public ExitAction() {
            super("exit");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            //exitActionPerformed();
            //saveLocation(prefs);
            frame.dispose();
            //System.exit(0);
        }
    }

    protected static class HelpAction extends AbstractAction{
        public HelpAction() {
            super("help");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            // dummy
        }
    }

    private static final String COPYRIGHT = "Copyright(C) 2006";
    private static final String APP_NAME  = "@title@";
    private static final String VERSION   = "0.0";
    private static final int    RELEASE   = 1;
    protected class VersionAction extends AbstractAction{
        public VersionAction() {
            super("version");
        }
        @Override public void actionPerformed(ActionEvent evt) {
            Object[] obj = {APP_NAME + " - Version " + VERSION + "." + RELEASE, COPYRIGHT};
            JOptionPane.showMessageDialog(MainPanel.this, obj, APP_NAME,
                                          JOptionPane.INFORMATION_MESSAGE);
        }
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
