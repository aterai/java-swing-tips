package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super();
        add(new ImageCaptionLabel("Mini-size 86Key Japanese Keyboard\n  Model No: DE-SK-86BK\n  SEREIAL NO: 00000000",
                                  new ImageIcon(getClass().getResource("test.png"))));
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ImageCaptionLabel extends JLabel implements HierarchyListener {
    private float alpha = 0.0f;
    private javax.swing.Timer animator;
    private int yy = 0;
    private JToolBar toolBox = new JToolBar() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    };
    public ImageCaptionLabel(String caption, Icon image) {
        setIcon(image);
        toolBox.setFloatable(false);
        toolBox.setOpaque(false);
        toolBox.setBackground(new Color(0,0,0,0));
        toolBox.setForeground(Color.WHITE);
        toolBox.setBorder(BorderFactory.createEmptyBorder(2,4,4,4));

        //toolBox.setLayout(new BoxLayout(toolBox, BoxLayout.X_AXIS));
        toolBox.add(Box.createGlue());
        toolBox.add(makeToolButton("ATTACHMENT_16x16-32.png"));
        toolBox.add(Box.createHorizontalStrut(2));
        toolBox.add(makeToolButton("RECYCLE BIN - EMPTY_16x16-32.png"));

        MouseAdapter ma = new MouseAdapter() {
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
        toolBox.addMouseListener(ma);

        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222,222,222)),
            BorderFactory.createLineBorder(Color.WHITE, 4)));
        setLayout(new OverlayLayout(this) {
            @Override public void layoutContainer(Container parent) {
                //Insets insets = parent.getInsets();
                int ncomponents = parent.getComponentCount();
                if(ncomponents == 0) return;
                int width = parent.getWidth(); // - (insets.left + insets.right);
                int height = parent.getHeight(); // - (insets.left + insets.right);
                int x = 0; //insets.left; int y = insets.top;
                //for(int i=0;i<ncomponents;i++) {
                Component c = parent.getComponent(0); //= toolBox;
                c.setBounds(x, height-yy, width, c.getPreferredSize().height);
                //}
            }
        });
        add(toolBox);

        addMouseListener(new MouseAdapter() {
            private int delay = 8;
            private int count = 0;
            @Override public void mouseEntered(MouseEvent e) {
                if(animator!=null && animator.isRunning() ||
                   yy==toolBox.getPreferredSize().height) return;
                final double h = (double)toolBox.getPreferredSize().height;
                animator = new javax.swing.Timer(delay, new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        double a = easeInOut(++count/h);
                        yy = (int)(.5d+a*h);
                        toolBox.setBackground(new Color(0f,0f,0f,(float)(0.6*a)));
                        if(yy>=toolBox.getPreferredSize().height) {
                            yy = toolBox.getPreferredSize().height;
                            animator.stop();
                        }
                        revalidate();
                        repaint();
                    }
                });
                animator.start();
            }
            @Override public void mouseExited(MouseEvent e) {
                if(animator!=null && animator.isRunning() ||
                   contains(e.getPoint()) && yy==toolBox.getPreferredSize().height) return;
                final double h = (double)toolBox.getPreferredSize().height;
                animator = new javax.swing.Timer(delay, new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        double a = easeInOut(--count/h);
                        yy = (int)(.5d+a*h);
                        toolBox.setBackground(new Color(0f,0f,0f,(float)(0.6*a)));
                        if(yy<=0) {
                            yy = 0;
                            animator.stop();
                        }
                        revalidate();
                        repaint();
                    }
                });
                animator.start();
            }
        });
        addHierarchyListener(this);
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 &&
           animator!=null && !isDisplayable()) {
            animator.stop();
        }
    }
    private JButton makeToolButton(String name) {
        ImageIcon icon = new ImageIcon(getClass().getResource(name));
        JButton b = new JButton();
        b.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
//         b.addChangeListener(new javax.swing.event.ChangeListener() {
//             public void stateChanged(javax.swing.event.ChangeEvent e) {
//                 JButton button = (JButton)e.getSource();
//                 ButtonModel model = button.getModel();
//                 if(button.isRolloverEnabled() && model.isRollover()) {
//                     button.setBorder(BorderFactory.createLineBorder(Color.WHITE,1));
//                 }else{
//                     button.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
//                 }
//             }
//         });
        b.setIcon(makeRolloverIcon(icon));
        b.setRolloverIcon(icon);
        b.setContentAreaFilled(false);
        //b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setToolTipText(name);
        return b;
    }
    private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
        RescaleOp op = new RescaleOp(
            new float[] { .5f,.5f,.5f,1.0f },
            new float[] { 0f,0f,0f,0f }, null);
        BufferedImage img = new BufferedImage(
            srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        srcIcon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(op.filter(img, null));
    }

    //http://www.gehacktes.net/2009/03/math-easein-easeout-and-easeinout/
    //coders≫ Blog Archive ≫ Math: EaseIn EaseOut, EaseInOut and Bezier Curves
    public double easeIn(double t) {
        //range: 0.0<=t<=1.0
        return Math.pow(t, 3d);
    }
    public double easeOut(double t) {
        return Math.pow(t-1d, 3d) + 1d;
    }
    public double easeInOut(double t) {
        if(t<0.5d) {
            return 0.5d*Math.pow(t*2d, 3d);
        }else{
            return 0.5d*(Math.pow(t*2d-2d, 3d) + 2d);
        }
    }
//     public double delta(double t) {
//         return 1d - Math.sin(Math.acos(t));
//     }
}

