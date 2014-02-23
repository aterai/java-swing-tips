package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    private final JCheckBox check      = new JCheckBox("Icons should be relocated", true);
    private final JButton button       = new JButton("relocate");
    private final JButton addButton    = new JButton("add");
    public MainPanel() {
        super(new BorderLayout());
        desktop.setDesktopManager(new ReIconifyDesktopManager());
        desktop.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (!check.isSelected()) {
                    return;
                }
                doReIconify((JDesktopPane) e.getComponent());
            }
        });
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                doReIconify(desktop);
            }
        });
        addButton.addActionListener(new ActionListener() {
            private int n;
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame f = createFrame("#" + n, n * 10, n * 10);
                desktop.add(f);
                desktop.getDesktopManager().activateFrame(f);
                n++;
            }
        });
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        toolbar.add(addButton);
        toolbar.addSeparator();
        toolbar.add(button);
        toolbar.addSeparator();
        toolbar.add(check);

        addIconifiedFrame(createFrame("Frame", 30, 10));
        addIconifiedFrame(createFrame("Frame", 50, 30));
        add(desktop);
        add(toolbar, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static class ReIconifyDesktopManager extends DefaultDesktopManager {
        public void reIconifyFrame(JInternalFrame jif) {
            deiconifyFrame(jif);
            Rectangle r = getBoundsForIconOf(jif);
            iconifyFrame(jif);
            jif.getDesktopIcon().setBounds(r);
        }
    }
    private static void doReIconify(JDesktopPane desktopPane) {
        DesktopManager dm = desktopPane.getDesktopManager();
        if (dm instanceof ReIconifyDesktopManager) {
            ReIconifyDesktopManager rdm = (ReIconifyDesktopManager) dm;
            for (JInternalFrame f: desktopPane.getAllFrames()) {
                if (f.isIcon()) { rdm.reIconifyFrame(f); }
            }
        }
    }
    private static JInternalFrame createFrame(String t, int x, int y) {
        //title, resizable, closable, maximizable, iconifiable
        JInternalFrame f = new JInternalFrame(t, false, true, true, true);
        f.setSize(200, 100);
        f.setLocation(x, y);
        f.setVisible(true);
        return f;
    }
    private void addIconifiedFrame(JInternalFrame f) {
        desktop.add(f);
        try {
            f.setIcon(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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
