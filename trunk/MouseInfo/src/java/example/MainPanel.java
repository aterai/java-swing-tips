package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel implements ActionListener, HierarchyListener {
    private static final Dimension panelDim = new Dimension(320, 240);
    private final Racket racket = new Racket(panelDim);
    private final JLabel absolute = new JLabel("absolute:");
    private final JLabel relative = new JLabel("relative:");
    private final Timer timer;
    public MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(absolute);
        box.add(relative);
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box);
        setPreferredSize(panelDim);
        timer = new Timer(10, this);
        addHierarchyListener(this);
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0) {
            if(isDisplayable()) {
                timer.start();
            }else{
                timer.stop();
            }
        }
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        racket.draw(g);
    }
    @Override public void actionPerformed(ActionEvent e) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point pt = pi.getLocation();
        absolute.setText("absolute:"+pt.toString());
        SwingUtilities.convertPointFromScreen(pt, this);
        relative.setText("relative:"+pt.toString());
        racket.move(pt.x);
        repaint();
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
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://d.hatena.ne.jp/aidiary/20070601/1251545490
class Racket {
    private static final int WIDTH  = 80;
    private static final int HEIGHT = 5;
    private int centerPos;
    private final Dimension parentSize;
    public Racket(Dimension parentSize) {
        this.parentSize = parentSize;
        centerPos = parentSize.width/2;
    }
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(centerPos-WIDTH/2, parentSize.height-HEIGHT, WIDTH, HEIGHT);
    }
    public void move(int pos) {
        centerPos = pos;
        if(centerPos<WIDTH/2) {
            centerPos = WIDTH/2;
        }else if(centerPos>parentSize.width-WIDTH/2) {
            centerPos = parentSize.width-WIDTH/2;
        }
    }
}
