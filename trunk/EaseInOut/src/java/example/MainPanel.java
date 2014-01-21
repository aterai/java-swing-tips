package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
//import java.awt.geom.*;
//import java.beans.*;
//import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super();
        String txt = "Mini-size 86Key Japanese Keyboard\n  Model No: DE-SK-86BK\n  SEREIAL NO: 00000000";
        ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
        add(new ImageCaptionLabel(txt, icon));
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

class ImageCaptionLabel extends JLabel {
    private final LabelHandler handler;
    private final JTextArea textArea = new JTextArea() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
        //@Override public boolean contains(int x, int y) {
        //    return false;
        //}
    };
    private final MouseAdapter textAreaMouseListener = new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) {
            dispatchMouseEvent(e);
        }
        @Override public void mouseExited(MouseEvent e) {
            dispatchMouseEvent(e);
        }
        private void dispatchMouseEvent(MouseEvent e) {
            Component src = e.getComponent();
            Component tgt = ImageCaptionLabel.this;
            tgt.dispatchEvent(SwingUtilities.convertMouseEvent(src, e, tgt));
        }
    };
    public ImageCaptionLabel(String caption, Icon icon) {
        setIcon(icon);
        textArea.setFont(textArea.getFont().deriveFont(11f));
        textArea.setText(caption);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        //textArea.setFocusable(false);
        textArea.setBackground(new Color(0,true));
        textArea.setForeground(Color.WHITE);
        textArea.setBorder(BorderFactory.createEmptyBorder(2,4,4,4));
        textArea.addMouseListener(textAreaMouseListener);

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(222,222,222)),
                                                     BorderFactory.createLineBorder(Color.WHITE, 4)));
        setLayout(new OverlayLayout(this) {
            @Override public void layoutContainer(Container parent) {
                //Insets insets = parent.getInsets();
                int ncomponents = parent.getComponentCount();
                if(ncomponents == 0) { return; }
                int width = parent.getWidth(); // - insets.left - insets.right;
                int height = parent.getHeight(); // - insets.left - insets.right;
                int x = 0; //insets.left; int y = insets.top;
                int tah = handler.getTextAreaHeight();
                //for(int i=0;i<ncomponents;i++) {
                Component c = parent.getComponent(0); //= textArea;
                c.setBounds(x, height-tah, width, c.getPreferredSize().height);
                //}
            }
        });
        add(textArea);
        handler = new LabelHandler(textArea);
        addMouseListener(handler);
        addHierarchyListener(handler);
    }
}

class LabelHandler extends MouseAdapter implements HierarchyListener {
    private static int DELAY = 4;
    private Timer animator;
    private final JComponent textArea;
    private int txah = 0;
    public LabelHandler(JComponent textArea) {
        this.textArea = textArea;
    }
    public int getTextAreaHeight() {
        return txah;
    }
    private int count = 0;
    private Timer createTimer(final int dir) {
        final double height = (double)textArea.getPreferredSize().height;
        return new Timer(DELAY, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                double a = AnimationUtil.easeInOut(count/height);
                count += dir;
                txah = (int)(.5d+a*height);
                textArea.setBackground(new Color(0f,0f,0f,(float)(0.6*a)));
                if(dir>0) { //show
                    if(txah>=textArea.getPreferredSize().height) {
                        txah = textArea.getPreferredSize().height;
                        animator.stop();
                    }
                }else{ //hide
                    if(txah<=0) {
                        txah = 0;
                        animator.stop();
                    }
                }
                JComponent p = (JComponent)SwingUtilities.getUnwrappedParent(textArea);
                p.revalidate();
                p.repaint();
            }
        });
    }
    @Override public void mouseEntered(MouseEvent e) {
        if(animator!=null && animator.isRunning() || txah==textArea.getPreferredSize().height) {
            return;
        }
        animator = createTimer(1);
        animator.start();
    }
    @Override public void mouseExited(MouseEvent e) {
        JComponent parent = (JComponent)e.getSource();
        if(animator!=null && animator.isRunning() || parent.contains(e.getPoint()) && txah==textArea.getPreferredSize().height) {
            return;
        }
        animator = createTimer(-1);
        animator.start();
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        JComponent c = (JComponent)e.getComponent();
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && animator!=null && !c.isDisplayable()) {
            animator.stop();
        }
    }
}

class AnimationUtil {
    //http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
    //Math: EaseIn EaseOut, EaseInOut and Bezier Curves | Anima Entertainment GmbH
    private static int N = 3;
    public static double easeIn(double t) {
        //range: 0.0<=t<=1.0
        return Math.pow(t, N);
    }
    public static double easeOut(double t) {
        return Math.pow(t-1d, N) + 1d;
    }
    public static double easeInOut(double t) {
/*/
        if(t<0.5d) {
            return 0.5d*Math.pow(t*2d, N);
        }else{
            return 0.5d*(Math.pow(t*2d-2d, N) + 2d);
        }
    }
/*/
        if(t<0.5d) {
            return 0.5d*intpow(t*2d, N);
        }else{
            return 0.5d*(intpow(t*2d-2d, N) + 2d);
        }
    }
    //http://d.hatena.ne.jp/pcl/20120617/p1
    //http://d.hatena.ne.jp/rexpit/20110328/1301305266
    //http://c2.com/cgi/wiki?IntegerPowerAlgorithm
    //http://www.osix.net/modules/article/?id=696
    public static double intpow(double a, int b) {
        double d = 1.0;
        if(b < 0) {
            //return d / intpow(a, -b);
            throw new IllegalArgumentException("B must be a positive integer or zero");
        }
        for(; b > 0; a *= a, b >>>= 1) {
            if((b & 1) != 0) {
                d *= a;
            }
        }
        return d;
    }
//*/
//     public static double delta(double t) {
//         return 1d - Math.sin(Math.acos(t));
//     }
}
