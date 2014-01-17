package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private TexturePaint makeImageTexture() {
        BufferedImage bi = null;
        try{
            bi = ImageIO.read(getClass().getResource("unkaku_w.png"));
        }catch(IOException ioe) {
            ioe.printStackTrace();
            throw new IllegalArgumentException(ioe);
        }
        return new TexturePaint(bi, new Rectangle(bi.getWidth(),bi.getHeight()));
    }
    private TableModel makeTableModel() {
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        return model;
    }
    public MainPanel() {
        super(new BorderLayout());

        final JTable table = new JTable(makeTableModel()) {
            @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if(c instanceof JTextField) {
                    JTextField tf = (JTextField)c;
                    tf.setOpaque(false);
                }else if(c instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox)c;
                    cb.setBackground(getSelectionBackground());
                }
                return c;
            }
        };
        table.setAutoCreateRowSorter(true);
        table.setRowSelectionAllowed(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        class TranslucentBooleanRenderer extends JCheckBox implements TableCellRenderer {
            private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
            public TranslucentBooleanRenderer() {
                super();
                setHorizontalAlignment(JLabel.CENTER);
                setBorderPainted(true);
            }
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if(isSelected) {
                    setOpaque(true);
                    setForeground(table.getSelectionForeground());
                    super.setBackground(table.getSelectionBackground());
                }else{
                    setOpaque(false);
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());
                }
                setSelected(value != null && ((Boolean)value).booleanValue());
                if(hasFocus) {
                    setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                }else{
                    setBorder(noFocusBorder);
                }
                return this;
            }
            @Override protected void paintComponent(Graphics g) {
                if(!isOpaque()) {
                    g.setColor(getBackground());
                    g.fillRect(0,0,getWidth(),getHeight());
                }
                super.paintComponent(g);
            }
            //Overridden for performance reasons. ---->
            @Override public boolean isOpaque() {
                Color back = getBackground();
                Component p = getParent();
                if(p != null) {
                    p = p.getParent();
                } // p should now be the JTable.
                boolean colorMatch = back != null && p != null && back.equals(p.getBackground()) && p.isOpaque();
                return !colorMatch && super.isOpaque();
            }
            @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                //System.out.println(propertyName);
                //String literal pool
                //if((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
                boolean flag = "font".equals(propertyName) || "foreground".equals(propertyName);
                if(flag && oldValue != newValue) {
                    super.firePropertyChange(propertyName, oldValue, newValue);
                }
            }
            @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
            @Override public void repaint(long tm, int x, int y, int width, int height) {}
            @Override public void repaint(Rectangle r) {}
            @Override public void repaint() {}
            @Override public void invalidate() {}
            @Override public void validate() {}
            @Override public void revalidate() {}
            //<---- Overridden for performance reasons.
        }
        table.setDefaultRenderer(Boolean.class, new TranslucentBooleanRenderer());

        JScrollPane scroll = new JScrollPane(table) {
            private final TexturePaint texture = makeImageTexture();
            @Override protected JViewport createViewport() {
                return new JViewport() {
                    @Override public void paintComponent(Graphics g) {
                        if(texture!=null) {
                            Graphics2D g2 = (Graphics2D)g;
                            g2.setPaint(texture);
                            g2.fillRect(0,0,getWidth(),getHeight());
                        }
                        super.paintComponent(g);
                    }
                };
            }
        };

        final Color alphaZero = new Color(0, true);
        table.setOpaque(false);
        table.setBackground(alphaZero);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(alphaZero);

        add(scroll);
        add(new JCheckBox(new AbstractAction("setBackground(new Color(255,0,0,50))") {
            private final Color color = new Color(255,0,0,50);
            @Override public void actionPerformed(ActionEvent e) {
                table.setBackground(((JCheckBox)e.getSource()).isSelected()?color:alphaZero);
            }
        }), BorderLayout.NORTH);
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
        }catch(Exception e) {
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
