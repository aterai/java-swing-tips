package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

class MainPanel extends JPanel {
    private static final Color BG_COLOR = new Color(1f, .8f, .8f, .2f);
    private final JTextField field0,field1,field2;
    private transient TexturePaint texture;

    public MainPanel() {
        super();
        field0 = new JTextField("aaaaaaaaa");
        field0.setBackground(BG_COLOR);

        field1 = new JTextField("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        field1.setOpaque(false);
        field1.setBackground(BG_COLOR);

        field2 = new JTextField("cccccccccccccccccccccc") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setPaint(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field2.setOpaque(false);
        field2.setBackground(BG_COLOR);

        initComponents();
        setPreferredSize(new Dimension(320, 240));
    }
    private void initComponents() {
        setLayout(new GridBagLayout());
        Border inside  = BorderFactory.createEmptyBorder(10,5+2,10,10+2);
        Border outside = BorderFactory.createTitledBorder("setBackground(1.0, 0.8, 0.8, 0.2)");
        setBorder(BorderFactory.createCompoundBorder(outside, inside));
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;

        c.gridx   = 0;
        c.insets  = new Insets(15, 15, 15, 0);
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; add(new JLabel("0. setOpaque(true)"), c);
        c.gridy   = 1; add(new JLabel("1. setOpaque(false)"), c);
        c.gridy   = 2; add(new JLabel("2. 1+paintComponent"), c);

        c.gridx   = 1;
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; add(field0, c);
        c.gridy   = 1; add(field1, c);
        c.gridy   = 2; add(field2, c);
    }
    private TexturePaint makeTexturePaint() {
        //Viva! edo>http://www.viva-edo.com/komon/edokomon.html
        URL url = getClass().getResource("unkaku_w.gif");
        BufferedImage bfimage = null;
        try{
            bfimage = ImageIO.read(url);
            //bfimage = makeBufferedImage(ImageIO.read(url), new float[] {1.0f,1.0f,0.5f});
        }catch(IOException ioe) {
            ioe.printStackTrace();
            throw new IllegalArgumentException(ioe);
        }
        int w = bfimage.getWidth();
        int h = bfimage.getHeight();
        return new TexturePaint(bfimage, new Rectangle2D.Float(0, 0, w, h));
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(texture==null) {
            texture = makeTexturePaint();
        }
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
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
            //for(UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
            //    if("Nimbus".equals(laf.getName())) { UIManager.setLookAndFeel(laf.getClassName()); }
            //}
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
