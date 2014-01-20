package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeList(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        setPreferredSize(new Dimension(320, 200));
    }
    @SuppressWarnings("unchecked")
    private static JList makeList() {
        DefaultListModel model = new DefaultListModel();
        model.addElement("asdfasdfasdfsadfas");
        model.addElement("qwerqwerqwerqwerweqr");
        model.addElement("zxcvzxcbzxcvzxcbzxcbzxcbzxcvzxcbzxbzxcvzxcbzcvbzxcvzxcvzx");
        model.addElement("tryurtirtiriu");
        model.addElement("jhkghjkfhjkghjkhjk");
        model.addElement("bnm,bnmvmvbm,vbmfmvbmn");
        model.addElement("1234123541514354677697808967867895678474567356723456245624");
        model.addElement("qwerqwerrqwettrtrytru");
        model.addElement("tiutyityityoiuo");
        model.addElement("hjklgkghkghk");
        model.addElement("zxcvzxcvbvnvbmvbmbm");
        JList list = new JList(model);
        list.setCellRenderer(new AnimeListCellRenderer(list));
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class AnimeListCellRenderer extends JPanel implements ListCellRenderer, HierarchyListener {
    private static final Color SELECTEDCOLOR = new Color(230,230,255);
    private final AnimeIcon icon = new AnimeIcon();
    private final MarqueeLabel label = new MarqueeLabel();
    private final Timer animator;
    private final JList list;
    private boolean isRunning = false;

    public AnimeListCellRenderer(final JList l) {
        super(new BorderLayout());
        this.list = l;
        animator = new Timer(80, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                int i = l.getSelectedIndex();
                if(i>=0) {
                    isRunning = true;
                    l.repaint(l.getCellBounds(i,i));
                }else{
                    isRunning = false;
                }
            }
        });
        setOpaque(true);
        add(icon, BorderLayout.WEST);
        add(label);
        list.addHierarchyListener(this);
        //animator.start();
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0) {
            if(list.isDisplayable()) {
                animator.start();
            }else{
                animator.stop();
            }
        }
    }
    @Override public Component getListCellRendererComponent(JList list, Object object, int index, boolean isSelected, boolean cellHasFocus) {
        setBackground(isSelected ? SELECTEDCOLOR : list.getBackground());
        label.setText((object==null) ? "" : object.toString());
        animate_index = index;
        return this;
    }
    private boolean isAnimatingCell() {
        return isRunning && animate_index==list.getSelectedIndex();
    }
    int animate_index = -1;
    private class MarqueeLabel extends JLabel {
        private float xx;
        public MarqueeLabel() {
            super();
            setOpaque(false);
        }
        @Override public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            Rectangle r = list.getVisibleRect();
            int cw = r.width-icon.getPreferredSize().width;
            FontRenderContext frc = g2d.getFontRenderContext();
            GlyphVector gv = getFont().createGlyphVector(frc, getText());
            if(isAnimatingCell() && gv.getVisualBounds().getWidth()>cw) {
                LineMetrics lm = getFont().getLineMetrics(getText(), frc);
                float yy = lm.getAscent()/2f + (float)gv.getVisualBounds().getY();
                g2d.drawGlyphVector(gv, cw-xx, getHeight()/2f-yy);
                xx = cw+gv.getVisualBounds().getWidth()-xx > 0 ? xx+8f : 0f;
            }else{
                super.paintComponent(g2d);
            }
        }
    }
    private class AnimeIcon extends JComponent {
        private static final double r  = 2.0d;
        private static final double sx = 1.0d;
        private static final double sy = 1.0d;
        private final List<Shape> icons = new ArrayList<Shape>(Arrays.asList(
            new Ellipse2D.Double(sx+3*r, sy+0*r, 2*r, 2*r),
            new Ellipse2D.Double(sx+5*r, sy+1*r, 2*r, 2*r),
            new Ellipse2D.Double(sx+6*r, sy+3*r, 2*r, 2*r),
            new Ellipse2D.Double(sx+5*r, sy+5*r, 2*r, 2*r),
            new Ellipse2D.Double(sx+3*r, sy+6*r, 2*r, 2*r),
            new Ellipse2D.Double(sx+1*r, sy+5*r, 2*r, 2*r),
            new Ellipse2D.Double(sx+0*r, sy+3*r, 2*r, 2*r),
            new Ellipse2D.Double(sx+1*r, sy+1*r, 2*r, 2*r)));
        public AnimeIcon() {
            super();
            int iw = (int)(r*8+sx*2);
            int ih = (int)(r*8+sy*2);
            setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
            setPreferredSize(new Dimension(iw+2, ih));
            setOpaque(false);
        }
        @Override public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            //g2d.setPaint(getBackground());
            //g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if(isAnimatingCell()) {
                float alpha = 0.1f;
                for(Shape s: icons) {
                    g2d.setPaint(new Color(0.5f,0.5f,0.5f,alpha));
                    g2d.fill(s);
                    alpha = alpha+0.1f;
                }
                icons.add(icons.remove(0));
            }else{
                g2d.setPaint(new Color(0.6f,0.6f,0.6f));
                for(Shape s: icons) {
                    g2d.fill(s);
                }
            }
        }
    }
}
