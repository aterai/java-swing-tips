package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.Timer;

class MainPanel extends JPanel{
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    private final JLabel label = new JLabel(df.format(new Date()), SwingConstants.CENTER);
    private final Timer timer = new Timer(1000, new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
            label.setText(df.format(new Date()));
            if(label.getParent().isOpaque()) {
                repaintWindowAncestor(label);
            }
        }
    });
    private void repaintWindowAncestor(Component c) {
        Window w = SwingUtilities.getWindowAncestor(c);
        if(w instanceof JFrame) {
            JFrame f = (JFrame)w;
            JComponent cp = (JComponent)f.getContentPane();
            //cp.repaint();
            Rectangle r = c.getBounds();
            r = SwingUtilities.convertRectangle(c, r, cp);
            cp.repaint(r.x, r.y, r.width, r.height);
            //r = SwingUtilities.convertRectangle(c, r, f);
            //f.repaint(r.x, r.y, r.width, r.height);
        }else{
            c.repaint();
        }
    }
    private static TexturePaint makeImageTexture() {
        BufferedImage bi = null;
        try{
            //http://www.viva-edo.com/komon/edokomon.html
            bi = ImageIO.read(MainPanel.class.getResource("unkaku_w.png"));
        }catch(IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
        return new TexturePaint(bi, new Rectangle(bi.getWidth(),bi.getHeight()));
    }
    private static TexturePaint makeCheckerTexture() {
        int cs = 6;
        int sz = cs*cs;
        BufferedImage bi = new BufferedImage(sz,sz,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setPaint(new Color(200,150,100,50));
        g2.fillRect(0,0,sz,sz);
        for(int i=0;i*cs<sz;i++) {
            for(int j=0;j*cs<sz;j++) {
                if((i+j)%2==0) g2.fillRect(i*cs, j*cs, cs, cs);
            }
        }
        g2.dispose();
        return new TexturePaint(bi, new Rectangle(0,0,sz,sz));
    }
    private enum TexturePaints {
        Null    (null, "Color(.5f,.8f,.5f,.5f)"),
        Image   (makeImageTexture(), "Image TexturePaint"),
        Checker (makeCheckerTexture(), "Checker TexturePaint");
        private final String description;
        private final TexturePaint texture;
        private TexturePaints(TexturePaint texture, String description) {
            this.texture = texture;
            this.description = description;
        }
        @Override public String toString() {
            return description;
        }
        public TexturePaint getTexturePaint() {
            return this.texture;
        }
    }
    private final JComboBox combo = makeComboBox(TexturePaints.values());
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox(Object[] model) {
        return new JComboBox(model);
    }

    private final TexturePanel tp;

    public MainPanel() {
        super(new BorderLayout());
        tp = makeTexturePanel();
        combo.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    JComboBox cbox = (JComboBox)e.getSource();
                    TexturePaints t = (TexturePaints)cbox.getSelectedItem();
                    tp.setTexturePaint(t.getTexturePaint());
                    repaintWindowAncestor(tp);
                }
            }
        });
        JToggleButton button = new JToggleButton(new AbstractAction("timer") {
            private JFrame frame = null;
            @Override public void actionPerformed(ActionEvent e) {
                if(frame==null) {
                    frame = new JFrame();
                    frame.setUndecorated(true);
                    //frame.setAlwaysOnTop(true);
                    //com.sun.awt.AWTUtilities.setWindowOpaque(frame, false); //JDK 1.6.0
                    frame.setBackground(new Color(0,0,0,0)); //JDK 1.7.0
                    frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                    frame.getContentPane().add(tp);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                }
                if(((AbstractButton)e.getSource()).isSelected()) {
                    TexturePaints t = (TexturePaints)combo.getSelectedItem();
                    tp.setTexturePaint(t.getTexturePaint());
                    timer.start();
                    frame.setVisible(true);
                }else{
                    timer.stop();
                    frame.setVisible(false);
                }
            }
        });
        JPanel p = new JPanel();
        p.add(combo);
        p.add(button);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }

    private static Font makeFont(URL url) {
        Font font = null;
        InputStream is = null;
        try{
            is = url.openStream();
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12.0f);
            is.close();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(FontFormatException ffe) {
            ffe.printStackTrace();
        }finally{
            if(is!=null) {
                try{
                    is.close();
                }catch(IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }
        return font;
    }

    private TexturePanel makeTexturePanel() {
        //http://www.yourname.jp/soft/digitalfonts-20090306.shtml
        //Digital display font: Copyright (c) Yourname, Inc.
        Font font = makeFont(getClass().getResource("YournameS7ScientificHalf.ttf"));
        label.setFont(font.deriveFont(80.0f));
        label.setBackground(new Color(0,0,0,0));
        label.setOpaque(false);
        TexturePanel p = new TexturePanel(new BorderLayout(8,8));
        p.add(label);
        p.add(new JLabel("Digital display fonts by Yourname, Inc."), BorderLayout.NORTH);
        p.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        p.setBackground(new Color(.5f,.8f,.5f,.5f));
        DragWindowListener dwl = new DragWindowListener();
        p.addMouseListener(dwl);
        p.addMouseMotionListener(dwl);
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
class TexturePanel extends JPanel{
    public TexturePanel() {
        super();
    }
    public TexturePanel(LayoutManager lm) {
        super(lm);
    }
    protected TexturePaint texture;
    public void setTexturePaint(TexturePaint texture) {
        this.texture = texture;
        //setOpaque(false);
        setOpaque(texture==null);
    }
    @Override public void paintComponent(Graphics g) {
        if(texture!=null) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setPaint(texture);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g);
    }
}

class DragWindowListener extends MouseAdapter {
    private MouseEvent start;
    private Window window;
    @Override public void mousePressed(MouseEvent me) {
        if(window==null) {
            Object o = me.getSource();
            if(o instanceof Window) {
                window = (Window)o;
            }else if(o instanceof JComponent) {
                window = SwingUtilities.windowForComponent(me.getComponent());
            }
        }
        start = me;
    }
    @Override public void mouseDragged(MouseEvent me) {
        if(window!=null) {
            Point eventLocationOnScreen = me.getLocationOnScreen();
            window.setLocation(eventLocationOnScreen.x - start.getX(),
                               eventLocationOnScreen.y - start.getY());
        }
    }
}
