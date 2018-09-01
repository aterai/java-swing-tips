package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        Box list1 = Box.createVerticalBox();

        DefaultListModel<CheckBoxNode> model = new DefaultListModel<>();
        JList<CheckBoxNode> list2 = new CheckBoxList<>(model);

        JTree list3 = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                setCellEditor(null);
                super.updateUI();
                setEditable(true);
                setRootVisible(false);
                setShowsRootHandles(false);
                setCellRenderer(new CheckBoxNodeRenderer());
                setCellEditor(new CheckBoxNodeEditor());
            }
        };

        JPanel p = new JPanel(new GridLayout(1, 3));
        p.add(makeTitledPanel("Box", new JScrollPane(list1)));
        p.add(makeTitledPanel("JList", new JScrollPane(list2)));
        p.add(makeTitledPanel("JTree", new JScrollPane(list3)));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
        Stream.of("aaaa", "bbbbbbb", "ccc", "dddddd", "eeeeeee", "fffffffff", "gggggg", "hhhhh", "iiii", "jjjjjjjjjj").forEach(title -> {
            boolean isSelected = title.length() % 2 == 0;
            JCheckBox c = new JCheckBox(title, isSelected);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            list1.add(c);
            model.addElement(new CheckBoxNode(title, isSelected));
            root.add(new DefaultMutableTreeNode(new CheckBoxNode(title, isSelected)));
        });
        list3.setModel(new DefaultTreeModel(root));

        add(new JLabel("JCheckBox in ", SwingConstants.CENTER), BorderLayout.NORTH);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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

class CheckBoxNode {
    public final String text;
    public final boolean selected;
    protected CheckBoxNode(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
    @Override public String toString() {
        return text;
    }
}

class CheckBoxList<E extends CheckBoxNode> extends JList<E> {
    private transient CheckBoxCellRenderer<E> renderer;
    protected CheckBoxList(ListModel<E> model) {
        super(model);
    }
    @Override public void updateUI() {
        setForeground(null);
        setBackground(null);
        setSelectionForeground(null);
        setSelectionBackground(null);
        removeMouseListener(renderer);
        removeMouseMotionListener(renderer);
        super.updateUI();
        renderer = new CheckBoxCellRenderer<>();
        setCellRenderer(renderer);
        addMouseListener(renderer);
        addMouseMotionListener(renderer);
        putClientProperty("List.isFileList", Boolean.TRUE);
    }
    // @see SwingUtilities2.pointOutsidePrefSize(...)
    private boolean pointOutsidePrefSize(Point p) {
        int i = locationToIndex(p);
        E cbn = getModel().getElementAt(i);
        Component c = getCellRenderer().getListCellRendererComponent(this, cbn, i, false, false);
        Rectangle rect = getCellBounds(i, i);
        rect.width = c.getPreferredSize().width;
        return i < 0 || !rect.contains(p);
    }
    @Override protected void processMouseEvent(MouseEvent e) {
        if (!pointOutsidePrefSize(e.getPoint())) {
            super.processMouseEvent(e);
        }
    }
    @Override protected void processMouseMotionEvent(MouseEvent e) {
        if (pointOutsidePrefSize(e.getPoint())) {
            MouseEvent ev = new MouseEvent(e.getComponent(), MouseEvent.MOUSE_EXITED, e.getWhen(),
                                           e.getModifiersEx(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(),
                                           e.getClickCount(), e.isPopupTrigger(), MouseEvent.NOBUTTON);
            super.processMouseEvent(ev);
        } else {
            super.processMouseMotionEvent(e);
        }
    }
}

class CheckBoxCellRenderer<E extends CheckBoxNode> extends MouseAdapter implements ListCellRenderer<E> {
    private final JCheckBox checkBox = new JCheckBox();
    private int rollOverRowIndex = -1;
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        checkBox.setOpaque(true);
        if (isSelected) {
            checkBox.setBackground(list.getSelectionBackground());
            checkBox.setForeground(list.getSelectionForeground());
        } else {
            checkBox.setBackground(list.getBackground());
            checkBox.setForeground(list.getForeground());
        }
        checkBox.setSelected(value.selected);
        checkBox.getModel().setRollover(index == rollOverRowIndex);
        checkBox.setText(value.text);
        return checkBox;
    }
    @Override public void mouseExited(MouseEvent e) {
        if (rollOverRowIndex >= 0) {
            JList<?> l = (JList<?>) e.getComponent();
            l.repaint(l.getCellBounds(rollOverRowIndex, rollOverRowIndex));
            rollOverRowIndex = -1;
        }
    }
    @Override public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            JList<?> l = (JList<?>) e.getComponent();
            Point p = e.getPoint();
            int index = l.locationToIndex(p);
            if (index >= 0) {
                @SuppressWarnings("unchecked")
                DefaultListModel<CheckBoxNode> m = (DefaultListModel<CheckBoxNode>) l.getModel();
                CheckBoxNode n = m.get(index);
                m.set(index, new CheckBoxNode(n.text, !n.selected));
                l.repaint(l.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseMoved(MouseEvent e) {
        JList<?> l = (JList<?>) e.getComponent();
        int index = l.locationToIndex(e.getPoint());
        if (index != rollOverRowIndex) {
            rollOverRowIndex = index;
            l.repaint();
        }
    }
}

class CheckBoxNodeRenderer implements TreeCellRenderer {
    private final JCheckBox checkBox = new JCheckBox();
    private final TreeCellRenderer renderer = new DefaultTreeCellRenderer();
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (leaf && value instanceof DefaultMutableTreeNode) {
            checkBox.setOpaque(false);
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode) userObject;
                checkBox.setText(node.text);
                checkBox.setSelected(node.selected);
            }
            return checkBox;
        }
        return renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
// delegation pattern
class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
    private final JCheckBox checkBox = new JCheckBox() {
        protected transient ActionListener handler;
        @Override public void updateUI() {
            removeActionListener(handler);
            super.updateUI();
            setOpaque(false);
            setFocusable(false);
            handler = e -> stopCellEditing();
            addActionListener(handler);
        }
    };
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        if (leaf && value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CheckBoxNode) {
                checkBox.setSelected(((CheckBoxNode) userObject).selected);
            } else {
                checkBox.setSelected(false);
            }
            checkBox.setText(value.toString());
        }
        return checkBox;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(checkBox.getText(), checkBox.isSelected());
    }
    @Override public boolean isCellEditable(EventObject e) {
        return e instanceof MouseEvent;
    }
    // // AbstractCellEditor
    // @Override public boolean shouldSelectCell(EventObject anEvent) {
    //     return true;
    // }
    // @Override public boolean stopCellEditing() {
    //     fireEditingStopped();
    //     return true;
    // }
    // @Override public void cancelCellEditing() {
    //     fireEditingCanceled();
    //     }
}

// // inheritence to extend a class
// class CheckBoxNodeEditor extends JCheckBox implements TreeCellEditor {
//     private transient ActionListener handler;
//     @Override public void updateUI() {
//         removeActionListener(handler);
//         super.updateUI();
//         setOpaque(false);
//         setFocusable(false);
//         handler = e -> stopCellEditing();
//         addActionListener(handler);
//     }
//     @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
//         if (leaf && value instanceof DefaultMutableTreeNode) {
//             Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
//             if (userObject instanceof CheckBoxNode) {
//                 this.setSelected(((CheckBoxNode) userObject).selected);
//             } else {
//                 this.setSelected(false);
//             }
//             this.setText(value.toString());
//         }
//         return this;
//     }
//     @Override public Object getCellEditorValue() {
//         return new CheckBoxNode(getText(), isSelected());
//     }
//     @Override public boolean isCellEditable(EventObject e) {
//         return e instanceof MouseEvent;
//     }
//     // Copied from AbstractCellEditor
//     // protected EventListenerList listenerList = new EventListenerList();
//     // protected transient ChangeEvent changeEvent;
//     @Override public boolean shouldSelectCell(EventObject anEvent) {
//         return true;
//     }
//     @Override public boolean stopCellEditing() {
//         fireEditingStopped();
//         return true;
//     }
//     @Override public void cancelCellEditing() {
//         fireEditingCanceled();
//     }
//     @Override public void addCellEditorListener(CellEditorListener l) {
//         listenerList.add(CellEditorListener.class, l);
//     }
//     @Override public void removeCellEditorListener(CellEditorListener l) {
//         listenerList.remove(CellEditorListener.class, l);
//     }
//     public CellEditorListener[] getCellEditorListeners() {
//         return listenerList.getListeners(CellEditorListener.class);
//     }
//     protected void fireEditingStopped() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
//             }
//         }
//     }
//     protected void fireEditingCanceled() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
//             }
//         }
//     }
// }
