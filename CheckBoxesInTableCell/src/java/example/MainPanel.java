package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"user", "rwx"};
        Object[][] data = {
            {"owner", 7}, {"group", 6}, {"other", 5}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        //http://terai.xrea.jp/Swing/TerminateEdit.html
        //table.getTableHeader().setReorderingAllowed(false);
        //frame.setResizeable(false);
        //or
//         table.addMouseListener(new MouseAdapter() {
//             @Override public void mouseReleased(MouseEvent e) {
//                 JTable t = (JTable)e.getComponent();
//                 Point p  = e.getPoint();
//                 int row  = t.rowAtPoint(p);
//                 int col  = t.columnAtPoint(p);
//                 if(t.convertColumnIndexToModel(col)==1) {
//                     t.getCellEditor(row, col).stopCellEditing();
//                 }
//             }
//         });

        table.getColumnModel().getColumn(1).setCellRenderer(new CheckBoxesRenderer());
        table.getColumnModel().getColumn(1).setCellEditor(new CheckBoxesEditor());
        //table.getColumnModel().getColumn(1).setCellEditor(new CheckBoxEditorRenderer2());

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class CheckBoxesPanel extends JPanel {
    protected final String[] title = {"r", "w", "x"};
    public final JCheckBox[] buttons;
    public CheckBoxesPanel() {
        super();
        setOpaque(false);
        setBackground(new Color(0,0,0,0));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        buttons = new JCheckBox[title.length];
        for(int i=0; i<buttons.length; i++) {
            JCheckBox b = new JCheckBox(title[i]);
            b.setOpaque(false);
            b.setFocusable(false);
            b.setRolloverEnabled(false);
            b.setBackground(new Color(0,0,0,0));
            buttons[i] = b;
            add(b);
            add(Box.createHorizontalStrut(5));
        }
    }
    protected void updateButtons(Object v) {
        Integer i = (Integer)(v==null?0:v);
        buttons[0].setSelected((i&(1<<2))!=0);
        buttons[1].setSelected((i&(1<<1))!=0);
        buttons[2].setSelected((i&(1<<0))!=0);
    }
}
class CheckBoxesRenderer extends CheckBoxesPanel implements TableCellRenderer, java.io.Serializable {
    public CheckBoxesRenderer() {
        super();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateButtons(value);
        return this;
    }
    public static class UIResource extends CheckBoxesRenderer implements javax.swing.plaf.UIResource{}
}

class CheckBoxesEditor extends CheckBoxesPanel implements TableCellEditor, java.io.Serializable {
    public CheckBoxesEditor() {
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        };
        ActionMap am = getActionMap();
        for(int i=0; i<buttons.length; i++) {
            final JCheckBox b = buttons[i];
            b.addActionListener(al);
            am.put(title[i], new AbstractAction(title[i]) {
                @Override public void actionPerformed(ActionEvent e) {
                    b.setSelected(!b.isSelected());
                    fireEditingStopped();
                }
            });
        }
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), title[0]);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), title[1]);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), title[2]);
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        updateButtons(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        int i = 0;
        if(buttons[0].isSelected()) i|=1<<2;
        if(buttons[1].isSelected()) i|=1<<1;
        if(buttons[2].isSelected()) i|=1<<0;
        return i;
    }

    //Copid from AbstractCellEditor
    //protected EventListenerList listenerList = new EventListenerList();
    transient protected ChangeEvent changeEvent = null;

    @Override public boolean isCellEditable(java.util.EventObject e) {
        return true;
    } 
    @Override public boolean shouldSelectCell(java.util.EventObject anEvent) {
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
        return (CellEditorListener[])listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==CellEditorListener.class) {
                // Lazily create the event:
                if(changeEvent == null) changeEvent = new ChangeEvent(this);
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
                if(changeEvent == null) changeEvent = new ChangeEvent(this);
                ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
            }
        }
    }
}

// class CheckBoxEditorRenderer2 extends CheckBoxesPanel implements TableCellEditor, java.io.Serializable {
//     protected EditorDelegate delegate;
//     protected int clickCountToStart = 1;
//
//     public CheckBoxEditorRenderer2() {
//         delegate = new EditorDelegate() {
//             public void setValue(Object value) {
//                 updateButtons(value);
//             }
//             public Object getCellEditorValue() {
//                 int i = 0;
//                 if(buttons[0].isSelected()) i|=1<<2;
//                 if(buttons[1].isSelected()) i|=1<<1;
//                 if(buttons[2].isSelected()) i|=1<<0;
//                 return i;
//             }
//         };
//         ActionListener al = new ActionListener() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 fireEditingStopped();
//             }
//         };
//         ActionMap am = getActionMap();
//         for(int i=0; i<buttons.length; i++) {
//             final JCheckBox b = buttons[i];
//             b.addActionListener(al);
//             am.put(title[i], new AbstractAction(title[i]) {
//                 public void actionPerformed(ActionEvent e) {
//                     b.setSelected(!b.isSelected());
//                     fireEditingStopped();
//                 }
//             });
//         }
//         InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//         im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), title[0]);
//         im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), title[1]);
//         im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), title[2]);
//     }
//     @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//         updateButtons(value);
//         return this;
//     }
//
//     protected EventListenerList listenerList = new EventListenerList();
//     transient protected ChangeEvent changeEvent = null;
//     public void addCellEditorListener(CellEditorListener l) {
//         listenerList.add(CellEditorListener.class, l);
//     }
//     public void removeCellEditorListener(CellEditorListener l) {
//         listenerList.remove(CellEditorListener.class, l);
//     }
//     public CellEditorListener[] getCellEditorListeners() {
//         return (CellEditorListener[])listenerList.getListeners(
//             CellEditorListener.class);
//     }
//     protected void fireEditingStopped() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length-2; i>=0; i-=2) {
//             if (listeners[i]==CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (changeEvent == null)
//                   changeEvent = new ChangeEvent(this);
//                 ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
//             }
//         }
//     }
//     protected void fireEditingCanceled() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for(int i = listeners.length-2; i>=0; i-=2) {
//             if(listeners[i]==CellEditorListener.class) {
//                 // Lazily create the event:
//                 if(changeEvent == null) changeEvent = new ChangeEvent(this);
//                 ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
//             }
//         }
//     }
//
//     public Object getCellEditorValue() {
//         return delegate.getCellEditorValue();
//     }
//     public boolean isCellEditable(EventObject anEvent) { 
//         return delegate.isCellEditable(anEvent); 
//     }
//     public boolean shouldSelectCell(EventObject anEvent) { 
//         return delegate.shouldSelectCell(anEvent); 
//     }
//     public boolean stopCellEditing() {
//         return delegate.stopCellEditing();
//     }
//     public void cancelCellEditing() {
//         delegate.cancelCellEditing();
//     }
//     protected class EditorDelegate implements ActionListener, ItemListener, java.io.Serializable {
//         protected Object value;
//         public Object getCellEditorValue() {
//             return value;
//         }
//         public void setValue(Object value) {
//             this.value = value;
//         }
//         public boolean isCellEditable(EventObject anEvent) {
//             if (anEvent instanceof MouseEvent) {
//                 return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
//             }
//             return true;
//         }
//         public boolean shouldSelectCell(EventObject anEvent) {
//             return true;
//         }
//         public boolean startCellEditing(EventObject anEvent) {
//             return true;
//         }
//         public boolean stopCellEditing() {
//             fireEditingStopped();
//             return true;
//         }
//         public void cancelCellEditing() {
//             fireEditingCanceled();
//         }
//         @Override public void actionPerformed(ActionEvent e) {
//             CheckBoxEditorRenderer2.this.stopCellEditing();
//         }
//         @Override public void itemStateChanged(ItemEvent e) {
//             CheckBoxEditorRenderer2.this.stopCellEditing();
//         }
//     }
// }

// import java.awt.*;
// import java.awt.event.*;
// import java.util.*;
// import javax.swing.*;
// import javax.swing.event.*;
// import javax.swing.table.*;
// public class EnumSetTest {
//   String[] columnNames = { "user", "rwx" };
//   Object[][] data = {
//     {"owner", EnumSet.of(Permissions.READ, Permissions.WRITE, Permissions.EXECUTE)},
//     {"group", EnumSet.of(Permissions.READ)},
//     {"other", EnumSet.noneOf(Permissions.class)}
//   };
//   DefaultTableModel model = new DefaultTableModel(data, columnNames) {
//     @Override public Class<?> getColumnClass(int column) {
//       return getValueAt(0, column).getClass();
//     }
//   };
//   JTable table = new JTable(model);
//   public JComponent makeUI() {
//     CheckBoxEditorRenderer cer = new CheckBoxEditorRenderer();
//     TableColumn c = table.getColumnModel().getColumn(1);
//     c.setCellRenderer(cer);
//     c.setCellEditor(cer);
//     c.setPreferredWidth(180);
//
//     final EnumMap<Permissions, Integer> map =
//       new EnumMap<Permissions, Integer>(Permissions.class);
//     map.put(Permissions.READ,    1<<2);
//     map.put(Permissions.WRITE,   1<<1);
//     map.put(Permissions.EXECUTE, 1<<0);
//
//     JPanel p = new JPanel(new BorderLayout());
//     p.add(new JScrollPane(table));
//     p.add(new JButton(new AbstractAction("chmod") {
//       @Override public void actionPerformed(ActionEvent e) {
//         StringBuilder buf = new StringBuilder(9);
//         String M = "-";
//         for (int i=0; i<model.getRowCount(); i++) {
//           @SuppressWarnings("unchecked")
//           EnumSet<Permissions> v = (EnumSet<Permissions>)model.getValueAt(i, 1);
//           int flg = 0;
//           if (v.contains(Permissions.READ)) {
//             flg|=map.get(Permissions.READ);
//             buf.append("r");
//           } else {
//             buf.append(M);
//           }
//           if (v.contains(Permissions.WRITE)) {
//             flg|=map.get(Permissions.WRITE);
//             buf.append("w");
//           } else {
//             buf.append(M);
//           }
//           if (v.contains(Permissions.EXECUTE)) {
//             flg|=map.get(Permissions.EXECUTE);
//             buf.append("x");
//           } else {
//             buf.append(M);
//           }
//           System.out.print(flg);
//         }
//         System.out.println(" "+M+buf.toString());
//       }
//     }), BorderLayout.SOUTH);
//     return p;
//   }
//   public static void main(String[] args) {
//     EventQueue.invokeLater(new Runnable() {
//       @Override public void run() {
//         createAndShowGUI();
//       }
//     });
//   }
//   public static void createAndShowGUI() {
//     JFrame f = new JFrame();
//     f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//     f.getContentPane().add(new EnumSetTest().makeUI());
//     f.setSize(320,200);
//     f.setLocationRelativeTo(null);
//     f.setVisible(true);
//   }
// }
// enum Permissions { EXECUTE, WRITE, READ; }
// class CheckBoxesPanel extends JPanel {
//   protected final String[] title = {"r", "w", "x"};
//   public final JCheckBox[] buttons;
//   public CheckBoxesPanel() {
//     super();
//     setOpaque(false);
//     setBackground(new Color(0,0,0,0));
//     setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//     buttons = new JCheckBox[title.length];
//     for (int i=0; i<buttons.length; i++) {
//       JCheckBox b = new JCheckBox(title[i]);
//       b.setOpaque(false);
//       b.setFocusable(false);
//       b.setRolloverEnabled(false);
//       b.setBackground(new Color(0,0,0,0));
//       buttons[i] = b;
//       add(b);
//       add(Box.createHorizontalStrut(5));
//     }
//   }
//   protected void updateButtons(Object v) {
//     @SuppressWarnings("unchecked")
//     EnumSet<Permissions> f = (v==null)? EnumSet.noneOf(Permissions.class)
//                              :(EnumSet<Permissions>)v;
//     buttons[0].setSelected(f.contains(Permissions.READ));
//     buttons[1].setSelected(f.contains(Permissions.WRITE));
//     buttons[2].setSelected(f.contains(Permissions.EXECUTE));
//   }
// //   protected void updateButtons(Object v) {
// //     Integer i = (Integer)(v==null?0:v);
// //     buttons[0].setSelected((i&(1<<2))!=0);
// //     buttons[1].setSelected((i&(1<<1))!=0);
// //     buttons[2].setSelected((i&(1<<0))!=0);
// //   }
// }
//
// class CheckBoxesRenderer extends CheckBoxesPanel implements TableCellRenderer {
//   public CheckBoxesRenderer() {
//     super();
//     setName("Table.cellRenderer");
//   }
//   @Override public Component getTableCellRendererComponent(JTable table,
//       Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     updateButtons(value);
//     return this;
//   }
// }
//
// class CheckBoxesEditor extends CheckBoxesPanel implements TableCellEditor {
//   public CheckBoxesEditor() {
//     ActionListener al = new ActionListener() {
//       @Override public void actionPerformed(ActionEvent e) {
//         fireEditingStopped();
//       }
//     };
//   }
//   @Override public Component getTableCellEditorComponent(JTable table,
//       Object value, boolean isSelected, int row, int column) {
//     updateButtons(value);
//     return this;
//   }
//   @Override public Object getCellEditorValue() {
//     EnumSet<Permissions> f = EnumSet.noneOf(Permissions.class);
//     if (buttons[0].isSelected()) f.add(Permissions.READ);
//     if (buttons[1].isSelected()) f.add(Permissions.WRITE);
//     if (buttons[2].isSelected()) f.add(Permissions.EXECUTE);
//     return f;
//   }
// //   @Override public Object getCellEditorValue() {
// //     int i = 0;
// //     if (buttons[0].isSelected()) i|=1<<2;
// //     if (buttons[1].isSelected()) i|=1<<1;
// //     if (buttons[2].isSelected()) i|=1<<0;
// //     return i;
// //   }
//
//   //Copid from AbstractCellEditor
//   //protected EventListenerList listenerList = new EventListenerList();
//   transient protected ChangeEvent changeEvent = null;
//
//   @Override public boolean isCellEditable(java.util.EventObject e) {
//     return true;
//   }
//   @Override public boolean shouldSelectCell(java.util.EventObject anEvent) {
//     return true;
//   }
//   @Override public boolean stopCellEditing() {
//     fireEditingStopped();
//     return true;
//   }
//   @Override public void  cancelCellEditing() {
//     fireEditingCanceled();
//   }
//   @Override public void addCellEditorListener(CellEditorListener l) {
//     listenerList.add(CellEditorListener.class, l);
//   }
//   @Override public void removeCellEditorListener(CellEditorListener l) {
//     listenerList.remove(CellEditorListener.class, l);
//   }
//   public CellEditorListener[] getCellEditorListeners() {
//     return (CellEditorListener[])listenerList.getListeners(CellEditorListener.class);
//   }
//   protected void fireEditingStopped() {
//     Object[] listeners = listenerList.getListenerList();
//     for (int i = listeners.length-2; i>=0; i-=2) {
//       if (listeners[i]==CellEditorListener.class) {
//         if (changeEvent == null) changeEvent = new ChangeEvent(this);
//         ((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
//       }
//     }
//   }
//   protected void fireEditingCanceled() {
//     Object[] listeners = listenerList.getListenerList();
//     for (int i = listeners.length-2; i>=0; i-=2) {
//       if (listeners[i]==CellEditorListener.class) {
//         if (changeEvent == null) changeEvent = new ChangeEvent(this);
//         ((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
//       }
//     }
//   }
// }
