package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1, 2));
//         ImageIcon icon = new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg")); //http://sozai-free.com/
//         MouseAdapter dsl = new DragScrollListener();
//         JLabel l1 = new JLabel(icon);
//         l1.addMouseMotionListener(dsl);
//         l1.addMouseListener(dsl);
//         JLabel l2 = new JLabel(icon);
//         l2.addMouseMotionListener(dsl);
//         l2.addMouseListener(dsl);
//         add(makeScrollPane(l1));
//         add(makeTranslucentScrollBar(makeScrollPane(l2)));

        add(makeScrollPane(makeList()));
        add(makeTranslucentScrollBar(makeScrollPane(makeList())));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for(int i=0; i<50; i++) {
            Date d = new Date();
            model.addElement(String.format("%d: %s", i, d.toString()));
        }
        JList<String> list = new JList<>(model);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                JComponent c = (JComponent)e.getSource();
                Container p = c.getParent().getParent();
                p.repaint();
            }
        });
        list.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                JComponent c = (JComponent)e.getSource();
                Container p = c.getParent().getParent();
                p.repaint();
            }
            @Override public void focusGained(FocusEvent e) {
                JComponent c = (JComponent)e.getSource();
                Container p = c.getParent().getParent();
                p.repaint();
            }
        });
        return list;
    }
    private static JScrollPane makeScrollPane(JComponent c) {
        return new JScrollPane(
            c, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
    public JComponent makeTranslucentScrollBar(JScrollPane scrollPane) {
        scrollPane.setComponentZOrder(scrollPane.getVerticalScrollBar(), 0);
        scrollPane.setComponentZOrder(scrollPane.getViewport(), 1);
        scrollPane.getVerticalScrollBar().setOpaque(false);

        scrollPane.setLayout(new ScrollPaneLayout() {
            @Override public void layoutContainer(Container parent) {
                if(parent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)parent;

                    Rectangle availR = scrollPane.getBounds();
                    availR.x = availR.y = 0;

                    Insets insets = parent.getInsets();
                    availR.x = insets.left;
                    availR.y = insets.top;
                    availR.width  -= insets.left + insets.right;
                    availR.height -= insets.top  + insets.bottom;

                    Rectangle vsbR = new Rectangle();
                    vsbR.width  = 12;
                    vsbR.height = availR.height;
                    vsbR.x = availR.x + availR.width - vsbR.width;
                    vsbR.y = availR.y;

                    if(viewport != null) {
                        viewport.setBounds(availR);
                    }
                    if(vsb != null) {
                        vsb.setVisible(true);
                        vsb.setBounds(vsbR);
                    }
                }
            }
        });
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            private final Dimension d = new Dimension();
            @Override protected JButton createDecreaseButton(int orientation) {
                return new JButton() {
                    @Override public Dimension getPreferredSize() {
                        return d;
                    }
                };
            }
            @Override protected JButton createIncreaseButton(int orientation) {
                return new JButton() {
                    @Override public Dimension getPreferredSize() {
                        return d;
                    }
                };
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
//                 Graphics2D g2 = (Graphics2D)g.create();
//                 g2.setPaint(new Color(100,100,100,100));
//                 g2.fillRect(r.x,r.y,r.width-1,r.height-1);
//                 g2.dispose();
            }
            private final Color defaultColor  = new Color(220,100,100,100);
            private final Color draggingColor = new Color(200,100,100,100);
            private final Color rolloverColor = new Color(255,120,100,100);
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = null;
                JScrollBar sb = (JScrollBar)c;
                if(!sb.isEnabled() || r.width>r.height) {
                    return;
                }else if(isDragging) {
                    color = draggingColor;
                }else if(isThumbRollover()) {
                    color = rolloverColor;
                }else{
                    color = defaultColor;
                }
                g2.setPaint(color);
                g2.fillRect(r.x,r.y,r.width-1,r.height-1);
                g2.setPaint(Color.WHITE);
                g2.drawRect(r.x,r.y,r.width-1,r.height-1);
                g2.dispose();
            }
            @Override protected void setThumbBounds(int x, int y, int width, int height) {
                super.setThumbBounds(x, y, width, height);
                //scrollbar.repaint(x, 0, width, scrollbar.getHeight());
                scrollbar.repaint();
            }
        });
        return scrollPane;
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
// class DragScrollListener extends MouseAdapter {
//     private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
//     private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//     private final Point pp = new Point();
//     @Override public void mouseDragged(MouseEvent e) {
//         final JComponent jc = (JComponent)e.getSource();
//         Container c = jc.getParent();
//         if(c instanceof JViewport) {
//             JViewport vport = (JViewport)c;
//             Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
//             Point vp = vport.getViewPosition();
//             vp.translate(pp.x-cp.x, pp.y-cp.y);
//             jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
//             pp.setLocation(cp);
//         }
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         JComponent jc = (JComponent)e.getSource();
//         Container c = jc.getParent();
//         if(c instanceof JViewport) {
//             jc.setCursor(hndCursor);
//             JViewport vport = (JViewport)c;
//             Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
//             pp.setLocation(cp);
//         }
//     }
//     @Override public void mouseReleased(MouseEvent e) {
//         ((JComponent)e.getSource()).setCursor(defCursor);
//     }
// }
