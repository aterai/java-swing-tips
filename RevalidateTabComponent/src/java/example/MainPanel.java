package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicButtonUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
//         TabTitleEditListener l = new TabTitleEditListener(tabbedPane);
//         tabbedPane.addChangeListener(l);
//         tabbedPane.addMouseListener(l);
        for (int i = 0; i < 3; i++) {
            String title = "Tab " + i;
            tabbedPane.add(title, new JLabel(title));
            tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
        }
        tabbedPane.setComponentPopupMenu(new TabTitleRenamePopupMenu());
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
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

// How to Use Tabbed Panes (The Java Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
// http://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    protected ButtonTabComponent(final JTabbedPane pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (Objects.isNull(pane)) {
            throw new IllegalArgumentException("TabbedPane cannot be null");
        }
        this.pane = pane;
        setOpaque(false);
        JLabel label = new JLabel() {
            @Override public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
        add(label);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        JButton button = new TabButton();
        TabButtonHandler handler = new TabButtonHandler();
        button.addActionListener(handler);
        button.addMouseListener(handler);
        add(button);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }
    private class TabButtonHandler extends MouseAdapter implements ActionListener {
        @Override public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                pane.remove(i);
            }
        }
        @Override public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }
        @Override public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    }
}

class TabButton extends JButton {
    private static final int SIZE  = 17;
    private static final int DELTA = 6;

    protected TabButton() {
        super();
        setUI(new BasicButtonUI());
        setToolTipText("close this tab");
        setContentAreaFilled(false);
        setFocusable(false);
        setBorder(BorderFactory.createEtchedBorder());
        setBorderPainted(false);
        setRolloverEnabled(true);
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(SIZE, SIZE);
    }
    @Override public void updateUI() {
        //we don't want to update UI for this button
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(new BasicStroke(2));
        g2.setPaint(Color.BLACK);
        if (getModel().isRollover()) {
            g2.setPaint(Color.ORANGE);
        }
        if (getModel().isPressed()) {
            g2.setPaint(Color.BLUE);
        }
        g2.drawLine(DELTA, DELTA, getWidth() - DELTA - 1, getHeight() - DELTA - 1);
        g2.drawLine(getWidth() - DELTA - 1, DELTA, DELTA, getHeight() - DELTA - 1);
        g2.dispose();
    }
}

class TabTitleRenamePopupMenu extends JPopupMenu {
    private final JTextField textField = new JTextField(10);
    private final Action renameAction = new AbstractAction("rename") {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane t = (JTabbedPane) getInvoker();
            int idx = t.getSelectedIndex();
            String title = t.getTitleAt(idx);
            textField.setText(title);
            int result = JOptionPane.showConfirmDialog(
                t, textField, "Rename", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String str = textField.getText();
                if (!str.trim().isEmpty()) {
                    t.setTitleAt(idx, str);
                    JComponent c = (JComponent) t.getTabComponentAt(idx);
                    c.revalidate();
                }
            }
        }
    };
    private final Action newTabAction = new AbstractAction("new tab") {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane t = (JTabbedPane) getInvoker();
            int count = t.getTabCount();
            String title = "Tab " + count;
            t.add(title, new JLabel(title));
            t.setTabComponentAt(count, new ButtonTabComponent(t));
        }
    };
    private final Action closeAllAction = new AbstractAction("close all") {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane t = (JTabbedPane) getInvoker();
            t.removeAll();
        }
    };
    protected TabTitleRenamePopupMenu() {
        super();
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent e)   { /* not needed */ }
            @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        });
        add(renameAction);
        addSeparator();
        add(newTabAction);
        add(closeAllAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTabbedPane) {
            JTabbedPane t = (JTabbedPane) c;
            renameAction.setEnabled(t.indexAtLocation(x, y) >= 0);
            super.show(c, x, y);
        }
    }
}

// class TabTitleEditListener extends MouseAdapter implements ChangeListener {
//     private final JTextField editor = new JTextField();
//     private final JTabbedPane tabbedPane;
//     public TabTitleEditListener(final JTabbedPane tabbedPane) {
//         this.tabbedPane = tabbedPane;
//         editor.setBorder(BorderFactory.createEmptyBorder());
//         editor.addFocusListener(new FocusAdapter() {
//             @Override public void focusLost(FocusEvent e) {
//                 renameTabTitle();
//             }
//         });
//         editor.addKeyListener(new KeyAdapter() {
//             @Override public void keyPressed(KeyEvent e) {
//                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                     renameTabTitle();
//                 } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                     cancelEditing();
//                 } else {
//                     editor.setPreferredSize((editor.getText().length() > len) ? null : dim);
//                     tabbedPane.revalidate();
//                 }
//             }
//         });
//         tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(
//             KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
//         tabbedPane.getActionMap().put("start-editing", new AbstractAction() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 startEditing();
//             }
//         });
//     }
//     @Override public void stateChanged(ChangeEvent e) {
//         renameTabTitle();
//     }
//     @Override public void mouseClicked(MouseEvent e) {
//         Rectangle rect = tabbedPane.getUI().getTabBounds(tabbedPane, tabbedPane.getSelectedIndex());
//         if (Objects.nonNull(rect) && rect.contains(e.getPoint()) && e.getClickCount() == 2) {
//             startEditing();
//         } else {
//             renameTabTitle();
//         }
//     }
//     private int editing_idx = -1;
//     private int len = -1;
//     private Dimension dim;
//     private Component tabComponent = null; //<----add----
//     private void startEditing() {
//         editing_idx = tabbedPane.getSelectedIndex();
//         tabComponent = tabbedPane.getTabComponentAt(editing_idx); //<----add----
//         tabbedPane.setTabComponentAt(editing_idx, editor);
//         editor.setVisible(true);
//         editor.setText(tabbedPane.getTitleAt(editing_idx));
//         editor.selectAll();
//         editor.requestFocusInWindow();
//         len = editor.getText().length();
//         dim = editor.getPreferredSize();
//         editor.setMinimumSize(dim);
//     }
//     private void cancelEditing() {
//         if (editing_idx >= 0) {
//             tabbedPane.setTabComponentAt(editing_idx, tabComponent); //<----add----
//             editor.setVisible(false);
//             editing_idx = -1;
//             len = -1;
//             editor.setPreferredSize(null);
//             tabbedPane.requestFocusInWindow();
//         }
//     }
//     private void renameTabTitle() {
//         String title = editor.getText().trim();
//         if (editing_idx >= 0 && !title.isEmpty()) {
//             tabbedPane.setTitleAt(editing_idx, title);
//         }
//         cancelEditing();
//     }
// }
