package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private static final String DUMMY = "Hello";
    private final JTextArea textArea = new JTextArea();
    private final JTextField textField1 = new JTextField(DUMMY);
    private final JTextField textField2 = new JTextField(DUMMY);
    private final JTextField textField3 = new JTextField(DUMMY);
    private final JTextField textField4 = new JTextField(DUMMY);

    public MainPanel() {
        super(new BorderLayout());
        textField3.addHierarchyListener(new FocusHierarchyListener());
        textField4.addAncestorListener(new FocusAncestorListener());

        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        p.add(makePanel("Default",           textField1));
        p.add(makePanel2("WindowListener",   textField2));
        p.add(makePanel("HierarchyListener", textField3));
        p.add(makePanel("AncestorListener",  textField4));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private JPanel makePanel(String title, final JTextField textField) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JButton(new AbstractAction("show") {
            @Override public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(getRootPane(), textField, "Input Text", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    textArea.setText(textField.getText());
                }
            }
        }));
        return p;
    }
    private JPanel makePanel2(String title, final JTextField textField) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JButton(new AbstractAction("show") {
            @Override public void actionPerformed(ActionEvent e) {
                JOptionPane pane = new JOptionPane(textField, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, null);
                JDialog dialog = pane.createDialog(getRootPane(), "Input Text");
                dialog.addWindowListener(new WindowAdapter() {
                    @Override public void windowOpened(WindowEvent e) {
                        textField.requestFocusInWindow();
                    }
                });
                dialog.setVisible(true);
                Object selectedValue = pane.getValue();
                int result = JOptionPane.CLOSED_OPTION;
                if (selectedValue instanceof Integer) {
                    result = ((Integer) selectedValue).intValue();
                }
                if (result == JOptionPane.OK_OPTION) {
                    textArea.setText(textField.getText());
                }
            }
        }));
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

class FocusHierarchyListener implements HierarchyListener {
    @Override public void hierarchyChanged(HierarchyEvent e) {
        final JComponent c = (JComponent) e.getComponent();
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    c.requestFocusInWindow();
                }
            });
        }
    }
}

// https://community.oracle.com/thread/1354218 Input focus
class FocusAncestorListener implements AncestorListener {
    @Override public void ancestorAdded(AncestorEvent e) {
        e.getComponent().requestFocusInWindow();
    }
    @Override public void ancestorMoved(AncestorEvent e)   { /* not needed */ }
    @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
}
