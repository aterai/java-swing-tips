package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final String MENUITEM_NEWTAB    = "New tab";
    private static final String MENUITEM_CLOSEPAGE = "Close";
    private static final String MENUITEM_CLOSEALL  = "Close all";
    private static final String MENUITEM_CLOSEALLBUTACTIVE = "Close all bat active";
    private static final String MENUITEM_SORT = "Sort";
    private static int count;

    private final JTabbedPane tab;
    private final AbstractAction closePageAction = new ClosePageAction(MENUITEM_CLOSEPAGE, null);
    private final AbstractAction closeAllAction  = new CloseAllAction(MENUITEM_CLOSEALL, null);
    private final AbstractAction closeAllButActiveAction = new CloseAllButActiveAction(MENUITEM_CLOSEALLBUTACTIVE, null);
    private final AbstractAction sortAction = new SortAction(MENUITEM_SORT, null);
    private final JPopupMenu pop = new JPopupMenu() {
        @Override public void show(Component c, int x, int y) {
            sortAction.setEnabled(tab.getTabCount()>1);
            closePageAction.setEnabled(tab.indexAtLocation(x, y)>=0);
            closeAllAction.setEnabled(tab.getTabCount()>0);
            closeAllButActiveAction.setEnabled(tab.getTabCount()>0);
            super.show(c, x, y);
        }
    };

    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        tab = new EditableTabbedPane(frame);
        pop.add(new NewTabAction(MENUITEM_NEWTAB, null));
        pop.add(sortAction);
        pop.addSeparator();
        pop.add(closePageAction);
        pop.addSeparator();
        pop.add(closeAllAction);
        pop.add(closeAllButActiveAction);
        tab.setComponentPopupMenu(pop);
        tab.addTab("Title", new JLabel("Tab"));
        add(tab);
        setPreferredSize(new Dimension(320, 240));
    }
    class NewTabAction extends AbstractAction {
        public NewTabAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.addTab("Title: "+count, new JLabel("Tab: "+count));
            tab.setSelectedIndex(tab.getTabCount()-1);
            count++;
        }
    }
    class ClosePageAction extends AbstractAction {
        public ClosePageAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.remove(tab.getSelectedIndex());
        }
    }
    class CloseAllAction extends AbstractAction {
        public CloseAllAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.removeAll();
        }
    }
    class CloseAllButActiveAction extends AbstractAction {
        public CloseAllButActiveAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            int tabidx = tab.getSelectedIndex();
            String title = tab.getTitleAt(tabidx);
            Component cmp = tab.getComponentAt(tabidx);
            tab.removeAll();
            tab.addTab(title, cmp);
        }
    }
    class SortAction extends AbstractAction {
        public SortAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            setSortedTab(tab, makeSortedList(tab));
        }
        private List<ComparableTab> makeSortedList(JTabbedPane t) {
            List<ComparableTab> l = new ArrayList<>();
            for(int i=0;i<t.getTabCount();i++) {
                l.add(new ComparableTab(t.getTitleAt(i), t.getComponentAt(i)));
            }
            Collections.sort(l);
            //Collections.<ComparableTab>sort(l);
//             Collections.sort(l, new Comparator<ComparableTab>() {
//                 @Override public int compare(ComparableTab o1, ComparableTab o2) {
//                     return o1.compareTo(o2);
//                 }
//             });
            return l;
        }
        private void setSortedTab(JTabbedPane tabbedPane, List<ComparableTab> list) {
            tabbedPane.setVisible(false);
            tabbedPane.removeAll();
            for(ComparableTab c: list) {
                tabbedPane.addTab(c.title, c.comp);
            }
            tabbedPane.setVisible(true);
        }
    }
    static class ComparableTab implements Comparable<ComparableTab> {
        private final String title;
        private final Component comp;
        public ComparableTab(String title, Component comp) {
            this.title = title;
            this.comp  = comp;
        }
        @Override public int compareTo(ComparableTab o) {
            return title.compareTo(o.title);
        }
        @Override public boolean equals(Object o) {
            if(o instanceof ComparableTab) {
                return compareTo((ComparableTab)o)==0;
            }else{
                return false;
            }
        }
        @Override public int hashCode() {
            return title.hashCode()+comp.hashCode();
        }
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class EditableTabbedPane extends JTabbedPane {
    private final MyGlassPane panel  = new MyGlassPane();
    private final JTextField  editor = new JTextField();
    private Rectangle rect;

//     private final Rectangle2D lineRect = new Rectangle2D.Double();
//     private final Color lineColor = new Color(
//         SystemColor.activeCaption.getRed(),
//         SystemColor.activeCaption.getGreen(),
//         SystemColor.activeCaption.getBlue(),
//         128);
//     private int dragTab = -1;

    public EditableTabbedPane(JFrame frame) {
        super();
        editor.setBorder(BorderFactory.createEmptyBorder(0,3,0,3));
        editor.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(final FocusEvent e) {
                ((JTextField)e.getSource()).selectAll();
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                    renameTab();
                }else if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                if(me.getClickCount()==2) {
                    startEditing();
                }
            }
//             @Override public void mousePressed(MouseEvent e) {
//                 dragTab = getUI().tabForCoordinate(EditableTabbedPane.this, e.getX(), e.getY());
//             }
//             @Override public void mouseReleased(MouseEvent e) {
//                 if(dragTab>=0) {
//                     if(dragTab!=getUI().tabForCoordinate(EditableTabbedPane.this, e.getX(), e.getY())) {
//                         convertTab(dragTab, getTargetTabIndex(e.getPoint()));
//                     }
//                 }
//                 dragTab = -1;
//                 repaint();
//                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//             }
        });
//         addMouseMotionListener(new MouseMotionAdapter() {
//             @Override public void mouseDragged(MouseEvent e) {
//                 int next = getTargetTabIndex(e.getPoint());
//                 if(next<0 || dragTab==next || next-dragTab==1) {
//                     lineRect.setRect(0,0,0,0);
//                     setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//                 }else if(next==getTabCount()) {
//                     Rectangle rect = getBoundsAt(getTabCount()-1);
//                     lineRect.setRect(rect.x+rect.width-2,rect.y,4,rect.height);
//                     setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                 }else if(next==0) {
//                     Rectangle rect = getBoundsAt(0);
//                     lineRect.setRect(-2,rect.y,4,rect.height);
//                     setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                 }else{
//                     Rectangle rect = getBoundsAt(next-1);
//                     lineRect.setRect(rect.x+rect.width-2,rect.y,4,rect.height);
//                     setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                 }
//                 repaint();
//             }
//         });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                    startEditing();
                }
            }
        });
        panel.add(editor);
        frame.setGlassPane(panel);
        panel.setVisible(false);
    }

//     @Override public void paintComponent(Graphics g) {
//         Graphics2D g2 = (Graphics2D)g;
//         g2.setPaint(this.getBackground());
//         g2.fillRect(0,0,500,50);
//         super.paintComponent(g2);
//         if(dragTab>=0) {
//             g2.setPaint(lineColor);
//             g2.fill(lineRect);
//         }
//     }
//
//     private int getTargetTabIndex(Point pt) {
//         for(int i=0;i<this.getTabCount();i++) {
//             Rectangle rect = this.getBoundsAt(i);
//             rect.setRect(rect.x-rect.width/2, rect.y, rect.width, rect.height);
//             if(rect.contains(pt)) {
//                 return i;
//             }
//         }
//         Rectangle rect = this.getBoundsAt(this.getTabCount()-1);
//         rect.setRect(rect.x+rect.width/2, rect.y, rect.width+100, rect.height);
//         if(rect.contains(pt)) {
//             return this.getTabCount();
//         }else{
//             return -1;
//         }
//     }
//
//     private void convertTab(int prev, int next) {
//         if(next<0 || prev==next) {
//             //System.out.println("press="+prev+" next="+next);
//             return;
//         }
//         Component cmp = this.getComponentAt(prev);
//         String str = this.getTitleAt(prev);
//         if(next==this.getTabCount()) {
//             //System.out.println("last: press="+prev+" next="+next);
//             this.remove(prev);
//             this.addTab(str, cmp);
//             this.setSelectedIndex(this.getTabCount()-1);
//         }else if(prev>next) {
//             //System.out.println("   >: press="+prev+" next="+next);
//             this.remove(prev);
//             this.insertTab(str, null, cmp, null, next);
//             this.setSelectedIndex(next);
//         }else{
//             //System.out.println("   <: press="+prev+" next="+next);
//             this.remove(prev);
//             this.insertTab(str, null, cmp, null, next-1);
//             this.setSelectedIndex(next-1);
//         }
//     }
    private void initEditor() {
        rect = getUI().getTabBounds(this, getSelectedIndex());
        rect.setRect(rect.x+2, rect.y+2, rect.width-2, rect.height-2);
        editor.setBounds(rect);
        editor.setText(getTitleAt(getSelectedIndex()));
    }
    private void startEditing() {
        initEditor();
        panel.setVisible(true);
        editor.requestFocusInWindow();
    }
    private void cancelEditing() {
        panel.setVisible(false);
    }
    private void renameTab() {
        if(editor.getText().trim().length()>0) {
            setTitleAt(getSelectedIndex(), editor.getText());
        }
        panel.setVisible(false);
    }
    class MyGlassPane extends JComponent {
        public MyGlassPane() {
            super();
            setOpaque(false);
            setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
                @Override public boolean accept(Component c) { return c==editor; }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent me) {
                    if(rect==null || rect.contains(me.getPoint())) { return; }
                    renameTab();
                }
            });
            requestFocusInWindow();
        }
        @Override public void setVisible(boolean flag) {
            super.setVisible(flag);
            setFocusTraversalPolicyProvider(flag);
            setFocusCycleRoot(flag);
        }
    }
}
