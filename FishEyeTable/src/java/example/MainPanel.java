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
        final FishEyeTable table = new FishEyeTable(makeTestModel());
        table.setRowSelectionInterval(0,0);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                table.componentResized(null);
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(320, 240));
        add(scroll, BorderLayout.NORTH);
    }
    private static TableModel makeTestModel() {
        TestModel m = new TestModel();
        for(int i=0;i<20;i++) {
            m.addTest(new Test("Name:"+i, (i%2==0?"Comment":"")));
        }
        return m;
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
class FishEyeRowContext{
    public final int height;
    public final Font font;
    public final Color color;
    public FishEyeRowContext(int height, Font font, Color color) {
        this.height = height;
        this.font   = font;
        this.color  = color;
    }
}
class FishEyeTable extends JTable implements MouseListener, MouseMotionListener,
                                         ListSelectionListener, ComponentListener {
    private final java.util.List<FishEyeRowContext> fishEyeRowList;
    private final Font font_s;
    public FishEyeTable(TableModel m) {
        super(m);
        Font font = getFont();
        font_s = font.deriveFont(8.0f);
        Font font12 = font.deriveFont(10.0f);
        Font font18 = font.deriveFont(16.0f);
        Font font24 = font.deriveFont(22.0f);
        Font font32 = font.deriveFont(30.0f);
        Color color12 = new Color(250,250,250);
        Color color18 = new Color(245,245,245);
        Color color24 = new Color(240,240,240);
        Color color32 = new Color(230,230,250);

        fishEyeRowList = java.util.Arrays.asList(
            new FishEyeRowContext(12,font12,color12),
            new FishEyeRowContext(18,font18,color18),
            new FishEyeRowContext(24,font24,color24),
            new FishEyeRowContext(32,font32,color32),
            new FishEyeRowContext(24,font24,color24),
            new FishEyeRowContext(18,font18,color18),
            new FishEyeRowContext(12,font12,color12)
        );

        //setIntercellSpacing(new Dimension(0,0));
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        setFillsViewportHeight(true);
        getSelectionModel().addListSelectionListener(this);
    }

    private int prev_row = 0;
    private int prev_height = 0;
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        int rowCount = getModel().getRowCount();
        Color color = Color.WHITE;
        Font font   = font_s;
        int ccRow   = prev_row;
        int index   = 0;
        int rd2     = (fishEyeRowList.size()-1)/2;
        for(int i=-rd2;i<rowCount;i++) {
            if(ccRow-rd2<=i && i<=ccRow+rd2) {
                if(i==row) {
                    color = fishEyeRowList.get(index).color;
                    font  = fishEyeRowList.get(index).font;
                    break;
                }
                index++;
            }
        }
        c.setBackground(color);
        c.setFont(font);
        if(isRowSelected(row)) {
            c.setBackground(getSelectionBackground());
        }
        return c;
    }

    private int getViewableColoredRowCount(int ridx) {
        int rd2 = (fishEyeRowList.size()-1)/2;
        int rc  = getModel().getRowCount();
        if(rd2-ridx>0 && ridx<rd2) {
            return rd2 + 1 + ridx;
        }else if(ridx>rc-1-rd2 && ridx<rc-1+rd2) {
            return rc - ridx + rd2;
        }
        return fishEyeRowList.size();
    }

    public void initRowHeigth(int height, int ccRow) {
        int rd2      = (fishEyeRowList.size()-1)/2;
        int rowCount = getModel().getRowCount();
        int view_rc  = getViewableColoredRowCount(ccRow);
        int view_h   = 0;
        for(int i=0;i<view_rc;i++) view_h += fishEyeRowList.get(i).height;
        int rest_rc  = rowCount - view_rc;
        int rest_h   = height - view_h;
        int rest_rh  = rest_h/rest_rc; rest_rh = rest_rh>0?rest_rh:1;
        int a        = rest_h - rest_rh*rest_rc;
//         String str = String.format("%d-%d=%dx%d+%d=%d",
//                                    height, view_h, rest_rc, rest_rh, a, rest_h);
//         System.out.println(str);
        int index = -1;
        for(int i=-rd2;i<rowCount;i++) {
            int crh;
            if(ccRow-rd2<=i && i<=ccRow+rd2) {
                index++;
                if(i<0) continue;
                crh = fishEyeRowList.get(index).height;
            }else{
                if(i<0) continue;
                crh = rest_rh+(a>0?1:0);
                a = a-1;
            }
            setRowHeight(i, crh);
        }
    }
    @Override public void mouseMoved(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        if(prev_row==row) return;
        initRowHeigth(prev_height, row);
        prev_row = row;
    }
    @Override public void mouseDragged(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        if(prev_row==row) return;
        initRowHeigth(prev_height, row);
        prev_row = row;
    }
    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting()) return;
        int row = getSelectedRow();
        if(prev_row==row) return;
        initRowHeigth(prev_height, row);
        prev_row = row;
    }
    @Override public void componentResized(ComponentEvent e) {
        Container p = getParent();
        if(p==null || !(p instanceof JViewport)) return;
        int h = ((JViewport)p).getExtentSize().height;
        if(h==prev_height) return;
        initRowHeigth(h, getSelectedRow());
        prev_height = h;
    }
    @Override public void mousePressed(MouseEvent e) {
        repaint();
    }
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
    @Override public void componentMoved(ComponentEvent e)  {}
    @Override public void componentShown(ComponentEvent e)  {}
}
