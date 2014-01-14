package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(3, 1));
        JTabbedPane tab1 = new JTabbedPane();
        tab1.setBorder(BorderFactory.createTitledBorder("JTabbedPane"));
        tab1.addTab("1111", new JScrollPane(new JTree()));
        tab1.addTab("2222", new JLabel("bbbbbbbbb"));
        tab1.addTab("3333", new JLabel("cccccccccc"));
        tab1.addTab("4444", new JButton("dddddddddddddddd"));

        CardLayoutTabbedPane tab2 = new CardLayoutTabbedPane();
        tab2.setBorder(BorderFactory.createTitledBorder("CardLayout+JRadioButton(windows like)"));
        tab2.addTab("default", tab1);
        tab2.addTab("5555", new JScrollPane(new JTree()));
        tab2.addTab("6666", new JLabel("eeeee"));
        tab2.addTab("7777", new JLabel("fffffff"));
        tab2.addTab("8888", new JButton("gggggg"));

        UIManager.put("example.TabButton", "TabViewButtonUI");
        UIManager.put("TabViewButtonUI", "example.OperaTabViewButtonUI");
        CardLayoutTabbedPane tab3 = new CardLayoutTabbedPane();
        tab3.setBorder(BorderFactory.createTitledBorder("CardLayout+JRadioButton(opera like)"));
        tab3.addTab("9999", new JScrollPane(new JTree()));
        tab3.addTab("aaaaaaaaaaaaaaaaaaaaaaa", new JLabel("hhhhh"));
        tab3.addTab("bbbb", new JLabel("iiii"));
        tab3.addTab("cccc", new JButton("jjjjjj"));

        TableHeaderTabbedPane tab4 = new TableHeaderTabbedPane();
        tab4.setBorder(BorderFactory.createTitledBorder("CardLayout+JTableHeader"));
        tab4.addTab("dddd", new JScrollPane(new JTree()));
        tab4.addTab("eeee", new JLabel("kkk"));
        tab4.addTab("ffff", new JLabel("llllllllll"));
        tab4.addTab("gggg", new JButton("mmmmmm"));

        //add(tab1);
        add(tab2);
        add(tab3);
        add(tab4);
        setPreferredSize(new Dimension(320, 320));
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
            //for(UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
            //    if("Nimbus".equals(laf.getName())) UIManager.setLookAndFeel(laf.getClassName());
            //}
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

class CardLayoutTabbedPane extends JPanel {
    protected final CardLayout cardLayout = new CardLayout();
    protected final JPanel tabPanel = new JPanel(new GridLayout(1, 0, 0, 0));
    protected final JPanel wrapPanel = new JPanel(new BorderLayout(0, 0));
    protected final JPanel contentsPanel = new JPanel(cardLayout);
    protected final ButtonGroup bg = new ButtonGroup();
    public CardLayoutTabbedPane() {
        super(new BorderLayout());
        int left  = 1;
        int right = 3;
        tabPanel.setBorder(BorderFactory.createEmptyBorder(1,left,0,right));
        contentsPanel.setBorder(BorderFactory.createEmptyBorder(4,left,2,right));
        wrapPanel.add(tabPanel);
        wrapPanel.add(new JLabel("test:"), BorderLayout.WEST);
        add(wrapPanel, BorderLayout.NORTH);
        add(contentsPanel);
    }
    protected JComponent createTabComponent(final String title, final Component comp) {
        TabButton tab = new TabButton(title);
        tab.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                ((AbstractButton)e.getSource()).setSelected(true);
                cardLayout.show(contentsPanel, title);
            }
        });
        tab.setLayout(new BorderLayout());
        //tab.setLayout(new OverlayLayout(tab));
        //tab.setPreferredSize(new Dimension(200, 24));
        JButton close = new JButton(new AbstractAction("") {
            @Override public void actionPerformed(ActionEvent e) {
                //@See http://java-swing-tips.googlecode.com/svn/trunk/NewTabButton
                System.out.println("close button");
            }
        });
        Dimension dim = new Dimension(12, 12);
        close.setPreferredSize(dim);
        close.setMaximumSize(dim);
        //close.setMinimumSize(dim);
        //close.setAlignmentX(0.9f);
        //close.setAlignmentY(0.1f);
        //close.setBorder(BorderFactory.createLineBorder(Color.GREEN,1));
        close.setBorder(BorderFactory.createEmptyBorder());
        close.setFocusPainted(false);
        close.setContentAreaFilled(false);
        close.setIcon(new CloseTabIcon(Color.GRAY));
        close.setPressedIcon(new CloseTabIcon(Color.BLACK));
        close.setRolloverIcon(new CloseTabIcon(Color.ORANGE));

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(close, BorderLayout.NORTH);
        tab.add(p, BorderLayout.EAST);
        bg.add(tab);
        tab.setSelected(true);
        return tab;
    }
    public void addTab(String title, Component comp) {
        tabPanel.add(createTabComponent(title, comp));
        contentsPanel.add(comp, title);
        cardLayout.show(contentsPanel, title);
    }
}
class TabButton extends JRadioButton {
    private static final String uiClassID = "TabViewButtonUI";
    @Override public void updateUI() {
        if(UIManager.get(getUIClassID())!=null) {
            setUI((TabViewButtonUI)UIManager.getUI(this));
        }else{
            setUI(new BasicTabViewButtonUI());
        }
    }
    @Override public String getUIClassID() {
        return uiClassID;
    }
//     @Override public void setUI(TabViewButtonUI ui) {
//         super.setUI(ui);
//     }
    public TabViewButtonUI getUI() {
        return (TabViewButtonUI)ui;
    }
    public TabButton() {
        this(null, null);
    }
    public TabButton(Icon icon) {
        this(null, icon);
    }
    public TabButton(String text) {
        this(text, null);
    }
    public TabButton(Action a) {
        this();
        super.setAction(a);
        updateUI();
    }
    public TabButton(String text, Icon icon) {
        super(text, icon);
        updateUI();
    }
    @Override protected void fireStateChanged() {
        ButtonModel model = getModel();
        if(!model.isEnabled()) {
            setForeground(Color.GRAY);
        }else if(model.isPressed() && model.isArmed()) {
            setForeground(getPressedTextColor());
        }else if(model.isSelected()) {
            setForeground(getSelectedTextColor());
        }else if(isRolloverEnabled() && model.isRollover()) {
            setForeground(getRolloverTextColor());
        }else{
            setForeground(getTextColor());
        }
        super.fireStateChanged();
    };
    private Color textColor = Color.BLACK;
    private Color pressedTextColor = Color.BLACK;
    private Color rolloverTextColor = Color.BLACK;
    private Color rolloverSelectedTextColor = Color.BLACK;
    private Color selectedTextColor = Color.BLACK;
    public Color getTextColor() {
        return textColor;
    }
    public Color getPressedTextColor() {
        return pressedTextColor;
    }
    public Color getRolloverTextColor() {
        return rolloverTextColor;
    }
    public Color getRolloverSelectedTextColor() {
        return rolloverSelectedTextColor;
    }
    public Color getSelectedTextColor() {
        return selectedTextColor;
    }
    public void setTextColor(Color color) {
        textColor = color;
    }
    public void setPressedTextColor(Color color) {
        pressedTextColor = color;
    }
    public void setRolloverTextColor(Color color) {
        rolloverTextColor = color;
    }
    public void setRolloverSelectedTextColor(Color color) {
        rolloverSelectedTextColor = color;
    }
    public void setSelectedTextColor(Color color) {
        selectedTextColor = color;
    }
}
class TableHeaderTabbedPane extends JPanel {
    protected final CardLayout cardLayout = new CardLayout();
    protected final JPanel tabPanel = new JPanel(new GridLayout(1, 0, 0, 0));
    protected final JPanel wrapPanel = new JPanel(new BorderLayout(0, 0));
    protected final JPanel contentsPanel = new JPanel(cardLayout);
    protected final TableColumnModel model;
    private final JTableHeader header;
    private Object selectedColumn = null;
    private int rolloverColumn = -1;
    public TableHeaderTabbedPane() {
        super(new BorderLayout());
        int left  = 1;
        int right = 3;
        String[] columnNames = {};
        final JTable table = new JTable(new DefaultTableModel(null, columnNames));
        model = table.getTableHeader().getColumnModel();
        tabPanel.setBorder(BorderFactory.createEmptyBorder(1,left,0,right));
        contentsPanel.setBorder(BorderFactory.createEmptyBorder(4,left,2,right));
        header = table.getTableHeader();
        MouseInputHandler handler = new MouseInputHandler();
        header.addMouseListener(handler);
        header.addMouseMotionListener(handler);
        final TabButton l = new TabButton();
//         final TableCellRenderer hr = header.getDefaultRenderer();
        header.setDefaultRenderer(new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
                l.setText((String)val);
                l.setSelected(val==selectedColumn || col==rolloverColumn);
                return l;
//                 JLabel l;
//                 if(val==selectedColumn) {
//                     l = (JLabel)hr.getTableCellRendererComponent(tbl, val, true, true, row, col);
//                     //l.setForeground(Color.RED);
//                 }else{
//                     l = (JLabel)hr.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
//                 }
//                 return l;
            }
        });
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setPreferredSize(new Dimension());
        wrapPanel.add(sp);
        add(wrapPanel, BorderLayout.NORTH);
        add(contentsPanel);
    }
    public void addTab(final String title, final Component comp) {
        contentsPanel.add(comp, title);
        TableColumn tc = new TableColumn(
            model.getColumnCount(), 75, header.getDefaultRenderer(), null);
        tc.setHeaderValue(title);
        model.addColumn(tc);
        if(selectedColumn==null) {
            cardLayout.show(contentsPanel, title);
            selectedColumn = title;
        }
    }
    private class MouseInputHandler extends MouseAdapter {
        @Override public void mousePressed(MouseEvent e) {
            JTableHeader header = (JTableHeader)e.getSource();
            int index = header.columnAtPoint(e.getPoint());
            if(index<0) return;
            Object title = model.getColumn(index).getHeaderValue();
            cardLayout.show(contentsPanel, (String)title);
            selectedColumn = title;
        }
        @Override public void mouseEntered(MouseEvent e) {
            updateRolloverColumn(e);
        }
        @Override public void mouseMoved(MouseEvent e) {
            updateRolloverColumn(e);
        }
        @Override public void mouseDragged(MouseEvent e) {
            rolloverColumn = -1;
            updateRolloverColumn(e);
        }
        @Override public void mouseExited(MouseEvent e) {
            //int oldRolloverColumn = rolloverColumn;
            rolloverColumn = -1;
        }
        //@see BasicTableHeaderUI.MouseInputHandler
        private void updateRolloverColumn(MouseEvent e) {
            if(header.getDraggedColumn()==null && header.contains(e.getPoint())) {
                int col = header.columnAtPoint(e.getPoint());
                if(col!=rolloverColumn) {
                    //int oldRolloverColumn = rolloverColumn;
                    rolloverColumn = col;
                    //rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);
                }
            }
        }
    }
}
class CloseTabIcon implements Icon {
    private int width;
    private int height;
    private final Color color;
    public CloseTabIcon(Color color) {
        this.color = color;
        width  = 12;
        height = 12;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        //g.translate(x, y);
        g.setColor(color);
        g.drawLine(2, 2, 9, 9);
        g.drawLine(2, 3, 8, 9);
        g.drawLine(3, 2, 9, 8);
        g.drawLine(9, 2, 2, 9);
        g.drawLine(9, 3, 3, 9);
        g.drawLine(8, 2, 2, 8);
        //g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return width;
    }
    @Override public int getIconHeight() {
        return height;
    }
}
