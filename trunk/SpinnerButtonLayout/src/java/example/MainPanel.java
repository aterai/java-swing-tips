package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import com.sun.java.swing.plaf.windows.WindowsSpinnerUI;

public class MainPanel extends JPanel{
    private final JSpinner spinner0 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JSpinner spinner3 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));

    public MainPanel() {
        super(new BorderLayout());
        spinner1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        spinner2.setUI(new MySpinnerUI());
        if(spinner3.getUI() instanceof WindowsSpinnerUI) {
            spinner3.setUI(new MyWinSpinnerUI());
        }
        //spinner3.setLayout(new SpinnerLayout2());

//         UIManager.addPropertyChangeListener(new PropertyChangeListener() {
//             @Override public void propertyChange(PropertyChangeEvent evt) {
//                 if("lookAndFeel".equals(evt.getPropertyName())) {
//                     EventQueue.invokeLater(new Runnable() {
//                         @Override public void run() {
//                             spinner3.setLayout(new SpinnerLayout2());
//                             spinner3.revalidate();
//                         }
//                     });
//                 }
//             }
//         });

        Box box = Box.createVerticalBox();
        box.add(makePanel("Default",          spinner0));
        box.add(makePanel("RIGHT_TO_LEFT",    spinner1));
        box.add(makePanel("L(Prev), R(Next)", spinner2));
        box.add(makePanel("L(Prev), R(Next)", spinner3));

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static class SpinnerLayout extends BorderLayout {
        @Override public void addLayoutComponent(Component comp, Object constraints) {
            if("Editor".equals(constraints)) {
                constraints = "Center";
            }else if("Next".equals(constraints)) {
                constraints = "East";
            }else if("Previous".equals(constraints)) {
                constraints = "West";
            }
            super.addLayoutComponent(comp, constraints);
        }
    }
//     private static class SpinnerLayout2 extends BorderLayout {
//         @Override public Dimension preferredLayoutSize(Container target) {
//             synchronized (target.getTreeLock()) {
//                 int nmembers = target.getComponentCount();
//                 Dimension dim = new Dimension(0, 0);
//                 boolean flag = true;
//                 for(int i=0;i<nmembers;i++) {
//                     Component c = target.getComponent(i);
//                     // if("Next".equals(c.getName())) {
//                     if(c instanceof JButton && flag) {
//                         Dimension d = c.getPreferredSize();
//                         dim.width += d.width + getHgap();
//                         dim.height = Math.max(d.height, dim.height);
//                         continue;
//                     }
//                     //if("Previous".equals(c.getName())) {
//                     if(c instanceof JButton && !flag) {
//                          Dimension d = c.getPreferredSize();
//                          dim.width += d.width + getHgap();
//                          dim.height = Math.max(d.height, dim.height);
//                         continue;
//                     }
//                     //if("Editor".equals(c.getName())) {
//                     if(c instanceof JSpinner.DefaultEditor) {
//                         Dimension d = c.getPreferredSize();
//                         dim.width += d.width;
//                         dim.height = Math.max(d.height, dim.height);
//                     }
//                 }
//                 Insets insets = target.getInsets();
//                 dim.width += insets.left + insets.right;
//                 dim.height += insets.top + insets.bottom;
//                 return dim;
//             }
//         }
//         @Override public void layoutContainer(Container target) {
//             synchronized (target.getTreeLock()) {
//                 Insets insets = target.getInsets();
//                 int top    = insets.top;
//                 int bottom = target.getHeight() - insets.bottom;
//                 int left   = insets.left;
//                 int right  = target.getWidth()  - insets.right;
//                 int nmembers = target.getComponentCount();
//
//                 boolean flag = true;
//                 for (int i = 0 ; i < nmembers ; i++) {
//                     Component c = target.getComponent(i);
//                     //if("Next".equals(c.getName())) {
//                     if(c instanceof JButton && flag) {
//                         c.setSize(c.getWidth(), bottom - top);
//                         Dimension d = c.getPreferredSize();
//                         c.setBounds(right - d.width, top, d.width, bottom - top);
//                         right -= d.width + getHgap();
//                         flag = false;
//                         continue;
//                     }
//                     //if("Previous".equals(c.getName())) {
//                     if(c instanceof JButton && !flag) {
//                         c.setSize(c.getWidth(), bottom - top);
//                         Dimension d = c.getPreferredSize();
//                         c.setBounds(left, top, d.width, bottom - top);
//                         left += d.width + getHgap();
//                         continue;
//                     }
//                     //if("Editor".equals(c.getName())) {
//                     if(c instanceof JSpinner.DefaultEditor) {
//                         c.setBounds(left, top, right - left, bottom - top);
//                     }
//                 }
//             }
//         }
//     }

    private static class MySpinnerUI extends BasicSpinnerUI {
        @Override protected LayoutManager createLayout() {
            return new SpinnerLayout();
        }
    }
    private static class MyWinSpinnerUI extends WindowsSpinnerUI {
        @Override protected LayoutManager createLayout() {
            return new SpinnerLayout();
        }
//         @Override protected LayoutManager createLayout() {
//             return new BorderLayout(0,0);
//         }
//         @Override public void installUI(JComponent c) {
//             this.spinner = (JSpinner)c;
//             installDefaults();
//             installListeners();
//             maybeAdd(createNextButton(), "East");     //"Next");
//             maybeAdd(createPreviousButton(), "West"); //"Previous");
//             maybeAdd(createEditor(), "Center");       //"Editor");
//             //updateEnabledState();
//             installKeyboardActions();
//         }
//         private void maybeAdd(Component c, String s) {
//             if (c != null) {
//                 spinner.add(c, s);
//             }
//         }
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
