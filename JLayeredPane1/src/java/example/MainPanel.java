package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
@author Taka
*/
class MainPanel extends JPanel {
    private static final int BACKLAYER = 1;
    private static final int FORELAYER = 2;

    Font FONT = new Font("ＭＳ ゴシック", Font.PLAIN, 12);

    private final BGImageLayeredPane layerPane;

    public MainPanel() {
        super(new BorderLayout());

        Image image = new ImageIcon(getClass().getResource("tokeidai.jpg")).getImage();
        layerPane = new BGImageLayeredPane();
        layerPane.setImage(image);

        for(int i=0; i<7; i++) {
            JPanel p = createPanel(i);
            p.setLocation(i*70 + 20, i*50 + 15);
            layerPane.add(p, BACKLAYER);
        }
        add(layerPane);
        setPreferredSize(new Dimension(320, 240));
    }

    int[] colors = { 0xdddddd, 0xaaaaff, 0xffaaaa, 0xaaffaa, 0xffffaa, 0xffaaff, 0xaaffff };
    private Color getColor(int i, float f) {
        int b = (int)((i & 0xff) * f);
        i = i >> 8;
        int g = (int)((i & 0xff) * f);
        i = i >> 8;
        int r = (int)((i & 0xff) * f);
        return new Color(r,g,b);
    }

    private JPanel createPanel(int i) {
        String s = "<html><font color=#333333>ヘッダーだよん:"+ i +"</font></html>";
        JLabel label = new JLabel(s);
        label.setFont(FONT);
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground( getColor(colors[i], 0.85f));
        Border border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        label.setBorder(border1);

        JTextArea text = new JTextArea();
        text.setBackground( new Color(colors[i]));
        text.setMargin(new Insets(4,4,4,4));
        text.setLineWrap(true);

        JPanel p = new JPanel();

        Color col = getColor(colors[i], 0.5f);
        Border border = BorderFactory.createLineBorder(col, 1);
        p.setBorder(border);

        //ウインド移動用の処理
        DragMouseListener  li = new DragMouseListener(p);
        p.addMouseListener(li);
        p.addMouseMotionListener(li);

        p.setLayout( new BorderLayout());
        p.add(label, BorderLayout.NORTH);
        p.add(text);
        p.setSize( new Dimension(120, 100));
        return p;
    }

    //タイトル部分のマウスクリックでパネルを最上位にもってくる。ドラッグで移動。
    class DragMouseListener implements MouseListener, MouseMotionListener {
        Point origin;
        JPanel panel;

        DragMouseListener(JPanel p) {
            panel = p;
        }
        @Override public void mousePressed(MouseEvent e) {
            origin = new Point( e.getX(), e.getY());
            //選択された部品を上へ
            layerPane.moveToFront(panel);
        }
        @Override public void mouseDragged(MouseEvent e) {
            if(origin == null) return;
            //ずれた分だけ JPanel を移動させる
            int dx = e.getX() - origin.x;
            int dy = e.getY() - origin.y;
            Point p = panel.getLocation();
            panel.setLocation( p.x + dx, p.y + dy);
        }

        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseMoved(MouseEvent e) {}
    }

    //背景画像を描画する JLayeredPane
    static class BGImageLayeredPane extends JLayeredPane {
        public BGImageLayeredPane() {
            super();
        }

        void setImage(Image img) {
            bgImage = img;
        }
        private Image bgImage;

        //override
        @Override public void paint(Graphics g) {
            if(bgImage != null) {
                int imageh = bgImage.getHeight(null);
                int imagew = bgImage.getWidth(null);

                Dimension d = getSize();
                for(int h=0; h<d.getHeight(); h += imageh) {
                    for(int w=0; w<d.getWidth(); w += imagew) {
                        g.drawImage(bgImage, w, h, this);
                    }
                }
            }
            super.paint(g);
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
