package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(1,3));
        Box list1 = Box.createVerticalBox();

        DefaultListModel<CheckBoxNode> model = new DefaultListModel<>();
        JList<CheckBoxNode> list2 = new JList<CheckBoxNode>(model) {
            private CheckBoxCellRenderer renderer;
            @Override public void updateUI() {
                setForeground(null);
                setBackground(null);
                setSelectionForeground(null);
                setSelectionBackground(null);
                if(renderer!=null) {
                    removeMouseListener(renderer);
                    removeMouseMotionListener(renderer);
                }
                super.updateUI();
                renderer = new CheckBoxCellRenderer();
                setCellRenderer(renderer);
                addMouseListener(renderer);
                addMouseMotionListener(renderer);
            }
            //@see SwingUtilities2.pointOutsidePrefSize(...)
            private boolean pointOutsidePrefSize(Point p) {
                int i = locationToIndex(p);
                DefaultListModel m = (DefaultListModel)getModel();
                CheckBoxNode n = (CheckBoxNode)m.get(i);
                Component c = getCellRenderer().getListCellRendererComponent(this, n, i, false, false);
                Rectangle r = getCellBounds(i, i);
                r.width = c.getPreferredSize().width;
                return i < 0 || !r.contains(p);
            }
            @Override protected void processMouseEvent(MouseEvent e) {
                if(!pointOutsidePrefSize(e.getPoint())) {
                    super.processMouseEvent(e);
                }
            }
            @Override protected void processMouseMotionEvent(MouseEvent e) {
                if(!pointOutsidePrefSize(e.getPoint())) {
                    super.processMouseMotionEvent(e);
                }else{
                    e = new MouseEvent((Component)e.getSource(), MouseEvent.MOUSE_EXITED, e.getWhen(),
                                       e.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(),
                                       e.getClickCount(), e.isPopupTrigger(), MouseEvent.NOBUTTON);
                    super.processMouseEvent(e);
                }
            }
        };
        list2.putClientProperty("List.isFileList", Boolean.TRUE);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
        JTree list3 = new JTree();
        list3.setEditable(true);
        list3.setRootVisible(false);
        list3.setCellRenderer(new CheckBoxNodeRenderer());
        list3.setCellEditor(new CheckBoxNodeEditor());

        for(String title: Arrays.asList(
                "aaaa", "bbbbbbb", "ccc", "dddddd", "eeeeeee",
                "fffffffff", "gggggg", "hhhhh", "iiii", "jjjjjjjjjj")) {
            boolean flag = title.length()%2==0;
            addComp(list1, new JCheckBox(title, flag));
            model.addElement(new CheckBoxNode(title, flag));
            root.add(new DefaultMutableTreeNode(new CheckBoxNode(title, flag)));
        }
        list3.setModel(new DefaultTreeModel(root));
        p.add(makeTitledPanel("Box",   list1));
        p.add(makeTitledPanel("JList", list2));
        p.add(makeTitledPanel("JTree", list3));

        add(new JLabel("JCheckBox in ", SwingConstants.CENTER), BorderLayout.NORTH);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JComponent tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }
    private static void addComp(Box box, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(c);
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
        }catch(ClassNotFoundException | InstantiationException |
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

class CheckBoxCellRenderer extends JCheckBox implements ListCellRenderer<CheckBoxNode>, MouseListener, MouseMotionListener {
    @Override public Component getListCellRendererComponent(JList list, CheckBoxNode value, int index, boolean isSelected, boolean cellHasFocus) {
        this.setOpaque(true);
        if(isSelected) {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        }else{
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
        if(value instanceof CheckBoxNode) {
            this.setSelected(((CheckBoxNode)value).selected);
            this.getModel().setRollover(index==rollOverRowIndex);
        }
        this.setText(value.toString());
        return this;
    }
    private int rollOverRowIndex = -1;
    @Override public void mouseExited(MouseEvent e) {
        JList l = (JList)e.getSource();
        if(rollOverRowIndex>=0) {
            l.repaint(l.getCellBounds(rollOverRowIndex, rollOverRowIndex));
            rollOverRowIndex = -1;
        }
    }
    @Override public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON1) {
            JList l = (JList)e.getComponent();
            @SuppressWarnings("unchecked")
            DefaultListModel<CheckBoxNode> m = (DefaultListModel<CheckBoxNode>)l.getModel();
            Point p = e.getPoint();
            int index  = l.locationToIndex(p);
            if(index>=0) {
                CheckBoxNode n = m.get(index);
                m.set(index, new CheckBoxNode(n.text, !n.selected));
                l.repaint(l.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseMoved(MouseEvent e) {
        JList l = (JList)e.getSource();
        int index = l.locationToIndex(e.getPoint());
        if(index != rollOverRowIndex) {
            rollOverRowIndex = index;
            l.repaint();
        }
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
}

class CheckBoxNode {
    public final String text;
    public final boolean selected;
    public CheckBoxNode(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
    @Override public String toString() {
        return text;
    }
}

class CheckBoxNodeRenderer extends JCheckBox implements TreeCellRenderer {
    private TreeCellRenderer renderer = new DefaultTreeCellRenderer();
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if(leaf && value != null && value instanceof DefaultMutableTreeNode) {
            this.setOpaque(false);
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode)userObject;
                this.setText(node.text);
                this.setSelected(node.selected);
            }
            return this;
        }
        return renderer.getTreeCellRendererComponent(
            tree, value, selected, expanded, leaf, row, hasFocus);
    }
}

class CheckBoxNodeEditor extends JCheckBox implements TreeCellEditor {
    public CheckBoxNodeEditor() {
        super();
        setOpaque(false);
        setFocusable(false);
        addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                stopCellEditing();
            }
        });
    }
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        if(leaf && value != null && value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if(userObject!=null && userObject instanceof CheckBoxNode) {
                this.setSelected(((CheckBoxNode)userObject).selected);
            }else{
                this.setSelected(false);
            }
            this.setText(value.toString());
        }
        return this;
    }
    @Override public Object getCellEditorValue() {
        return new CheckBoxNode(getText(), isSelected());
    }
    @Override public boolean isCellEditable(EventObject e) {
        return e != null && e instanceof MouseEvent;
    }
    //Copid from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    //transient protected ChangeEvent changeEvent = null;
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void  cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
            }
        }
    }
}
