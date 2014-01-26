package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel(final JFrame frame) {
        super(new GridLayout(1,2));
        frame.setGlassPane(new LightboxGlassPane());
        frame.getGlassPane().setVisible(false);

        JButton button = new JButton(new AbstractAction("Open") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.getGlassPane().setVisible(true);
            }
        });
        add(makeDummyPanel());
        add(button);
        setPreferredSize(new Dimension(320, 240));
    }
    private JPanel makeDummyPanel() {
        JButton b = new JButton("Button&Mnemonic");
        b.setMnemonic(KeyEvent.VK_B);
        JTextField t = new JTextField("TextField&ToolTip");
        t.setToolTipText("ToolTip");
        JPanel p = new JPanel(new BorderLayout(5,5));
        p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        p.add(b, BorderLayout.NORTH);
        p.add(t, BorderLayout.SOUTH);
        p.add(new JScrollPane(new JTree()));
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class LightboxGlassPane extends JComponent implements HierarchyListener {
    private final ImageIcon image;
    private final AnimeIcon animatedIcon = new AnimeIcon();
    private float alpha = 0.0f;
    private int w = 0;
    private int h = 0;
    private Rectangle rect = new Rectangle();
    private Timer animator;
    public LightboxGlassPane() {
        setOpaque(false);
        super.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        image = new ImageIcon(getClass().getResource("test.png"));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                setVisible(false);
            }
        });
        addHierarchyListener(this);
    }
    @Override public void setVisible(boolean isVisible) {
        boolean oldVisible = isVisible();
        super.setVisible(isVisible);
        JRootPane rootPane = getRootPane();
        if(rootPane!=null && isVisible()!=oldVisible) {
            rootPane.getLayeredPane().setVisible(!isVisible);
        }
        boolean b = animator==null || !animator.isRunning();
        if(isVisible && b) {
            w = 40;
            h = 40;
            alpha = 0.0f;
            animator = new Timer(10, new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    animatedIcon.next();
                    repaint();
                }
            });
            animator.start();
        }else{
            if(animator!=null) {
                animator.stop();
            }
        }
        animatedIcon.setRunning(isVisible);
    }
    @Override public void paintComponent(Graphics g) {
        JRootPane rootPane = getRootPane();
        if(rootPane!=null) {
            rootPane.getLayeredPane().print(g);
        }
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;

        if(h<image.getIconHeight()+5+5) {
            h += image.getIconHeight()/16;
        }else if(w<image.getIconWidth()+5+5) {
            h  = image.getIconHeight()+5+5;
            w += image.getIconWidth()/16;
        }else if(alpha<1.0) {
            w  = image.getIconWidth()+5+5;
            alpha = alpha + 0.1f;
        }else{
            animatedIcon.setRunning(false);
            animator.stop();
        }
        rect.setSize(w, h);
        Rectangle screen = getBounds();
        rect.setLocation(screen.x + screen.width/2  - rect.width/2,
                         screen.y + screen.height/2 - rect.height/2);

        g2d.setColor(new Color(100,100,100,100));
        g2d.fill(screen);
        g2d.setColor(new Color(255,255,255,200));
        g2d.fill(rect);

        if(alpha>0) {
            if(alpha>1.0f) { alpha = 1.0f; }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(image.getImage(), rect.x+5, rect.y+5,
                          image.getIconWidth(),
                          image.getIconHeight(), this);
        }else{
            animatedIcon.paintIcon(this, g2d,
                                   screen.x + screen.width/2  - animatedIcon.getIconWidth()/2,
                                   screen.y + screen.height/2 - animatedIcon.getIconHeight()/2);
        }
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && animator!=null && !isDisplayable()) {
            animator.stop();
        }
    }
}

class AnimeIcon implements Icon {
    private static final Color cColor = new Color(0.5f,0.5f,0.5f);
    private static final double r  = 2.0d;
    private static final double sx = 0d;
    private static final double sy = 0d;
    private static final Dimension dim = new Dimension((int)(r*8+sx*2), (int)(r*8+sy*2));
    private final List<Shape> list = new ArrayList<Shape>(Arrays.asList(
        new Ellipse2D.Double(sx+3*r, sy+0*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+5*r, sy+1*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+6*r, sy+3*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+5*r, sy+5*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+3*r, sy+6*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+1*r, sy+5*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+0*r, sy+3*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+1*r, sy+1*r, 2*r, 2*r)));

    private boolean isRunning = false;
    public void next() {
        if(isRunning) { list.add(list.remove(0)); }
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    @Override public int getIconWidth()  { return dim.width;  }
    @Override public int getIconHeight() { return dim.height; }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        //g2d.setPaint((c!=null)?c.getBackground():Color.WHITE);
        g2d.setPaint(new Color(0,0,0,0));
        g2d.fillRect(x, y, getIconWidth(), getIconHeight());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(cColor);
        float alpha = 0.0f;
        g2d.translate(x, y);
        for(Shape s: list) {
            alpha = isRunning?alpha+0.1f:0.5f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.fill(s);
        }
        g2d.translate(-x, -y);
    }
}
