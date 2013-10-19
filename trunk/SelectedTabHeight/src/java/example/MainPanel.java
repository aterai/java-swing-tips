package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public class MainPanel extends JPanel {
    private static enum TabPlacements {
        TOP(JTabbedPane.TOP), BOTTOM(JTabbedPane.BOTTOM), LEFT(JTabbedPane.LEFT), RIGHT(JTabbedPane.RIGHT);
        public final int tabPlacement;
        private TabPlacements(int tabPlacement) {
            this.tabPlacement = tabPlacement;
        }
    }
    private final JComboBox<TabPlacements> comboBox = new JComboBox<>(TabPlacements.values());
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

    public MainPanel() {
        super(new BorderLayout());

        comboBox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    tabbedPane.setTabPlacement(((TabPlacements)e.getItem()).tabPlacement);
                }
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JLabel("TabPlacement: "));
        box.add(Box.createHorizontalStrut(2));
        box.add(comboBox);
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if(tabbedPane.getUI() instanceof WindowsTabbedPaneUI) {
            tabbedPane.setUI(new WindowsTabbedPaneUI() {
                private static final int tabAreaHeight = 32;
                @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                    return tabAreaHeight; //super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 4;
                }
                //@Override public Rectangle getTabBounds(JTabbedPane pane, int i) {
                //    Rectangle tabRect = super.getTabBounds(pane, i);
                //    tabRect.translate(0, -16);
                //    tabRect.height = 16;
                //    return tabRect;
                //}
                @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                    if(tabPane.getSelectedIndex()!=tabIndex && tabPlacement!=JTabbedPane.LEFT && tabPlacement!=JTabbedPane.RIGHT) {
                        int tabHeight = tabAreaHeight/2 + 3;
                        rects[tabIndex].height = tabHeight;
                        if(tabPlacement==JTabbedPane.TOP) {
                            rects[tabIndex].y = tabAreaHeight - tabHeight + 3;
                        }
                    }
                    super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
                }
            });
        }else{
            //t.setUI(new javax.swing.plaf.metal.MetalTabbedPaneUI() {
            tabbedPane.setUI(new BasicTabbedPaneUI() {
                private static final int tabAreaHeight = 32;
                @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                    return tabAreaHeight;
                }
                @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                    if(tabPane.getSelectedIndex()!=tabIndex && tabPlacement!=JTabbedPane.LEFT && tabPlacement!=JTabbedPane.RIGHT) {
                        int tabHeight = tabAreaHeight/2 + 3;
                        rects[tabIndex].height = tabHeight;
                        if(tabPlacement==JTabbedPane.TOP) {
                            rects[tabIndex].y = tabAreaHeight - tabHeight + 3;
                        }
                    }
                    super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
                }
            });
        }
        tabbedPane.addTab("00000", new JLabel("aaaaaaaaaaa"));
        tabbedPane.addTab("111112", new JLabel("bbbbbbbbbbbbbbbb"));
        tabbedPane.addTab("22222232", new JScrollPane(new JTree()));
        tabbedPane.addTab("3333333333", new JSplitPane());
        add(tabbedPane);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
