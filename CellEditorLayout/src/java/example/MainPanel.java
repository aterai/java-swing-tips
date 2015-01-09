package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTable table = new JTable(8, 4);

        table.getColumnModel().getColumn(0).setCellEditor(
            new CustomComponentCellEditor(new JTextField()));

        table.getColumnModel().getColumn(1).setCellEditor(
            new CustomCellEditor(new JTextField()));

        table.getColumnModel().getColumn(2).setCellEditor(
            new CustomComponentCellEditor2(new CustomComponent()));

        add(new JScrollPane(table));
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
class CustomCellEditor extends DefaultCellEditor {
    private static final int BUTTON_WIDTH = 20;
    protected final JButton button = new JButton();
    public CustomCellEditor(final JTextField field) {
        super(field);
        field.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, BUTTON_WIDTH));
        field.addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && e.getComponent().isShowing()) {
                    //System.out.println("hierarchyChanged: SHOWING_CHANGED");
                    field.removeAll();
                    field.add(button);
                    Rectangle r = field.getBounds();
                    button.setBounds(r.width - BUTTON_WIDTH, 0, BUTTON_WIDTH, r.height);
                    //field.requestFocusInWindow();
                }
            }
        });
    }
    @Override public Component getComponent() {
        //@see JTable#updateUI()
        SwingUtilities.updateComponentTreeUI(button);
        return super.getComponent();
    }
}
//class CustomComponentCellEditor extends AbstractCellEditor implements TableCellEditor {
class CustomComponentCellEditor extends DefaultCellEditor {
    protected final JTextField field;
    protected JButton button;
    private final JPanel panel = new JPanel(new BorderLayout());
    public CustomComponentCellEditor(JTextField field) {
        super(field);
        this.field = field;
        button = new JButton();
        button.setPreferredSize(new Dimension(25, 0));
        field.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        panel.add(field);
        panel.add(button, BorderLayout.EAST);
        panel.setFocusable(false);
    }
//     public Object getCellEditorValue() {
//         //System.out.println("  " + field.getText());
//         return field.getText();
//     }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        //System.out.println("getTableCellEditorComponent");
        field.setText(Objects.toString(value, ""));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                field.setCaretPosition(field.getText().length());
                field.requestFocusInWindow();
            }
        });
        return panel;
    }
    @Override public boolean isCellEditable(final EventObject e) {
        //System.out.println("isCellEditable");
//         if (e instanceof KeyEvent) {
//             //System.out.println("KeyEvent");
//             EventQueue.invokeLater(new Runnable() {
//                 @Override public void run() {
//                     char kc = ((KeyEvent) e).getKeyChar();
//                     if (!Character.isIdentifierIgnorable(kc)) {
//                         field.setText(field.getText() + kc);
//                     }
//                     field.setCaretPosition(field.getText().length());
//                     //field.requestFocusInWindow();
//                 }
//             });
//         }
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                if (e instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) e;
                    char kc = ke.getKeyChar();
                    //int kc = ke.getKeyCode();
                    if (Character.isUnicodeIdentifierStart(kc)) {
                        field.setText(field.getText() + kc);
                    }
                }
            }
        });
        return super.isCellEditable(e);
    }
    @Override public Component getComponent() {
        return panel;
    }
}

class CustomComponent extends JPanel {
//     static class CustomTextField extends JTextField {
//         @Override protected boolean processKeyBinding (KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
//             return super.processKeyBinding(ks, e, condition, pressed);
//         }
//     }
//     public final CustomTextField field = new CustomTextField();
    public final JTextField field = new JTextField();
    protected JButton button;
    public CustomComponent() {
        super(new BorderLayout(0, 0));
        button = new JButton();
        //this.setFocusable(false);
        this.add(field);
        this.add(button, BorderLayout.EAST);
    }
    @Override protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, int condition, boolean pressed) {
        if (!field.isFocusOwner() && !pressed) {
            field.requestFocusInWindow();
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(field, e);
                }
            });
        }
        return super.processKeyBinding(ks, e, condition, pressed);
//         field.requestFocusInWindow();
//         return field.processKeyBinding(ks, e, condition, pressed);
    }
}
class CustomComponentCellEditor2 extends DefaultCellEditor {
    private final CustomComponent component;
    public CustomComponentCellEditor2(CustomComponent component) {
        super(component.field);
        this.component = component;
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        component.field.setText(Objects.toString(value, ""));
        return this.component;
    }
    @Override public Component getComponent() {
        return component;
    }
}
