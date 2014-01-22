package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        DefaultListModel<MyIcon> list = makeIconList();
        MyIconModel model = new MyIconModel(list);
        MyIconTable table = new MyIconTable(model, list);
        JPanel p = new JPanel(new GridBagLayout());
        p.add(table, new GridBagConstraints());
        p.setBackground(Color.WHITE);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    private DefaultListModel<MyIcon> makeIconList() {
        DefaultListModel<MyIcon> list = new DefaultListModel<>();
        list.addElement(new MyIcon("wi0009"));
        list.addElement(new MyIcon("wi0054"));
        list.addElement(new MyIcon("wi0062"));
        list.addElement(new MyIcon("wi0063"));
        list.addElement(new MyIcon("wi0064"));
        list.addElement(new MyIcon("wi0096"));
        list.addElement(new MyIcon("wi0111"));
        list.addElement(new MyIcon("wi0122"));
        list.addElement(new MyIcon("wi0124"));
        return list;
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
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class MyIconModel extends DefaultTableModel {
    public MyIconModel(DefaultListModel list) {
        super();
        addRow(new Object[] {list.elementAt(0), list.elementAt(1), list.elementAt(2) });
        addRow(new Object[] {list.elementAt(3), list.elementAt(4), list.elementAt(5) });
        addRow(new Object[] {list.elementAt(6), list.elementAt(7), list.elementAt(8) });
    }
    @Override public boolean isCellEditable(int row, int column) {
        return false;
    }
    @Override public int getColumnCount() {
        return 3;
    }
    @Override public String getColumnName(int col) {
        return "";
    }
}

class MyIcon {
    public final ImageIcon large;
    public final ImageIcon small;
    public MyIcon(String str) {
        large = new ImageIcon(getClass().getResource(str+"-48.png"));
        small = new ImageIcon(getClass().getResource(str+"-24.png"));
    }
}

class MyIconRenderer extends DefaultTableCellRenderer {
    public MyIconRenderer() {
        super();
        setHorizontalAlignment(JLabel.CENTER);
        //setOpaque(true);
        //setBorder(BorderFactory.createEmptyBorder());
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setIcon(((MyIcon)value).large);
        return this;
    }
}

class MyIconTable extends JTable {
    private static final int XOFF = 4;
    private final MyGlassPane panel = new MyGlassPane() {
        @Override public void paintComponent(Graphics g) {
            g.setColor(new Color(255,255,255,100));
            g.fillRect(0, 0, getWidth(), getHeight());
            BufferedImage bufimg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufimg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            g2.setPaint(Color.BLACK);
            for(int i=0;i<XOFF;i++) {
                g2.fillRoundRect(rect.x-i, rect.y+XOFF, rect.width+i+i, rect.height-XOFF+i, 5, 5);
            }
            g2.dispose();
            g.drawImage(bufimg, 0, 0, null);
        }
    };
    private final EditorFromList editor;
    private Rectangle rect;

    public MyIconTable(TableModel model, DefaultListModel<MyIcon> list) {
        super(model);
        setDefaultRenderer(Object.class, new MyIconRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initCellSize(50);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                startEditing();
            }
        });

        editor = new EditorFromList(list);
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                }
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                setEditorSelectedIconAt(me.getPoint());
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                if(rect==null || rect.contains(me.getPoint())) {
                    return;
                }
                setEditorSelectedIconAt(me.getPoint());
            }
        });
        panel.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
            @Override public boolean accept(Component c) {
                return c==editor;
            }
        });
        panel.add(editor);
        panel.setVisible(false);
    }
    private void initCellSize(int size) {
        setRowHeight(size);
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        TableColumnModel m = getColumnModel();
        for(int i=0;i<m.getColumnCount();i++) {
            TableColumn col = m.getColumn(i);
            col.setMinWidth(size);
            col.setMaxWidth(size);
        }
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    public void startEditing() {
        JFrame f = (JFrame)getTopLevelAncestor();
        f.setGlassPane(panel);
        Dimension dim = editor.getPreferredSize();
        rect = getCellRect(getSelectedRow(), getSelectedColumn(), true);
        int iv = (dim.width-rect.width)/2;
        Point p = SwingUtilities.convertPoint(this, rect.getLocation(), panel);
        rect.setRect(p.x-iv, p.y-iv, dim.width, dim.height);
        editor.setBounds(rect);
        editor.setSelectedValue(getValueAt(getSelectedRow(), getSelectedColumn()), true);
        panel.setVisible(true);
        editor.requestFocusInWindow();
    }
    private void cancelEditing() {
        panel.setVisible(false);
    }
    private void setEditorSelectedIconAt(Point p) {
        Object o = editor.getModel().getElementAt(editor.locationToIndex(p));
        if(o != null) {
            setValueAt(o, getSelectedRow(), getSelectedColumn());
        }
        panel.setVisible(false);
    }
}

class EditorFromList extends JList<MyIcon> {
    private static final int ins = 2;
    public EditorFromList(DefaultListModel<MyIcon> list) {
        super(list);
        ImageIcon icon = ((MyIcon)list.elementAt(0)).small;
        int iw = ins+icon.getIconWidth();
        int ih = ins+icon.getIconHeight();
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setFixedCellWidth(iw);
        setFixedCellHeight(ih);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setPreferredSize(new Dimension(iw*3+ins, ih*3+ins));
        setCellRenderer(new ListCellRenderer<MyIcon>() {
            private final JLabel label = new JLabel();
            private final Color selctedColor = new Color(200, 200, 255);
            @Override public Component getListCellRendererComponent(JList list, MyIcon value, int index, boolean isSelected, boolean cellHasFocus) {
                label.setOpaque(true);
                label.setHorizontalAlignment(JLabel.CENTER);
                if(index == rollOverRowIndex) {
                    label.setBackground(getSelectionBackground());
                }else if(isSelected) {
                    label.setBackground(selctedColor);
                }else{
                    label.setBackground(getBackground());
                }
                label.setIcon(value.small);
                return label;
            }
        });
        RollOverListener lst = new RollOverListener();
        addMouseMotionListener(lst);
        addMouseListener(lst);
    }
    private int rollOverRowIndex = -1;
    private class RollOverListener extends MouseInputAdapter {
        @Override public void mouseExited(MouseEvent e) {
            rollOverRowIndex = -1;
            repaint();
        }
        @Override public void mouseMoved(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if( row != rollOverRowIndex ) {
                rollOverRowIndex = row;
                repaint();
            }
        }
    }
}

class MyGlassPane extends JPanel {
    public MyGlassPane() {
        super((LayoutManager)null);
        setOpaque(false);
    }
    @Override public void setVisible(boolean flag) {
        super.setVisible(flag);
        setFocusTraversalPolicyProvider(flag);
        setFocusCycleRoot(flag);
    }
}
