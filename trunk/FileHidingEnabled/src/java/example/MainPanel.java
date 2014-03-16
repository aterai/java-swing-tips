package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextArea log = new JTextArea();
    public MainPanel() {
        super(new BorderLayout());

        Object showHiddenProperty = Toolkit.getDefaultToolkit().getDesktopProperty("awt.file.showHiddenFiles");
        System.out.println("awt.file.showHiddenFiles: " + showHiddenProperty);

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
        p.add(new JButton(new AbstractAction("showOpenDialog") {
            private JFileChooser chooser;
            @Override public void actionPerformed(ActionEvent ae) {
                if (chooser == null) {
                    chooser = new JFileChooser();
                    JPopupMenu pop = searchPopupMenu(chooser);
                    pop.addSeparator();
                    JCheckBoxMenuItem mi = new JCheckBoxMenuItem(new AbstractAction("isFileHidingEnabled") {
                        @Override public void actionPerformed(ActionEvent e) {
                            chooser.setFileHidingEnabled(((JCheckBoxMenuItem) e.getSource()).isSelected());
                        }
                    });
                    mi.setSelected(chooser.isFileHidingEnabled());
                    pop.add(mi);
                }
                int retvalue = chooser.showOpenDialog(getRootPane());
                if (retvalue == JFileChooser.APPROVE_OPTION) {
                    log.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        }));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPopupMenu searchPopupMenu(Container parent) {
        for (Component c: parent.getComponents()) {
            if (c instanceof JComponent && ((JComponent) c).getComponentPopupMenu() != null) {
                return ((JComponent) c).getComponentPopupMenu();
            } else {
                JPopupMenu pop = searchPopupMenu((Container) c);
                if (pop != null) {
                    return pop;
                }
            }
        }
        return null;
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
