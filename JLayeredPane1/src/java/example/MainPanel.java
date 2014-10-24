package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
@author Taka
*/
public final class MainPanel extends JPanel {
    private static final int BACKLAYER = 1;
    //private static final int FORELAYER = 2;
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final int[] COLORS = {0xDDDDDD, 0xAAAAFF, 0xFFAAAA, 0xAAFFAA, 0xFFFFAA, 0xFFAAFF, 0xAAFFFF};
    private final JLayeredPane layerPane;

    public MainPanel() {
        super(new BorderLayout());

        layerPane = new BGImageLayeredPane(new ImageIcon(getClass().getResource("tokeidai.jpg")).getImage());
        for (int i = 0; i < 7; i++) {
            JPanel p = createPanel(i);
            p.setLocation(i * 70 + 20, i * 50 + 15);
            layerPane.add(p, BACKLAYER);
        }
        add(layerPane);
        setPreferredSize(new Dimension(320, 240));
    }

    private static Color getColor(int i, float f) {
        int r = (int) ((i >> 16 & 0xFF) * f);
        int g = (int) ((i >> 8  & 0xFF) * f);
        int b = (int) ((i >> 0  & 0xFF) * f);
        return new Color(r, g, b);
    }

    private JPanel createPanel(int i) {
        String s = "<html><font color=#333333>ヘッダーだよん:" + i + "</font></html>";
        JLabel label = new JLabel(s);
        label.setFont(FONT);
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(getColor(COLORS[i], 0.85f));
        Border border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        label.setBorder(border1);

        JTextArea text = new JTextArea();
        text.setMargin(new Insets(4, 4, 4, 4));
        text.setLineWrap(true);
        text.setOpaque(false);

        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(new Color(COLORS[i]));

        Color col = getColor(COLORS[i], .5f);
        Border border = BorderFactory.createLineBorder(col, 1);
        p.setBorder(border);

        //ウインド移動用の処理
        DragMouseListener li = new DragMouseListener(layerPane);
        p.addMouseListener(li);
        p.addMouseMotionListener(li);

        p.setLayout(new BorderLayout());
        p.add(label, BorderLayout.NORTH);
        p.add(text);
        p.setSize(new Dimension(120, 100));
        return p;
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

//タイトル部分のマウスクリックでパネルを最上位にもってくる。ドラッグで移動。
class DragMouseListener extends MouseAdapter {
    private final JLayeredPane parent;
    private Point origin;
    public DragMouseListener(JLayeredPane parent) {
        super();
        this.parent = parent;
    }
    @Override public void mousePressed(MouseEvent e) {
        JComponent panel = (JComponent) e.getComponent();
        origin = e.getPoint();
        //選択された部品を上へ
        parent.moveToFront(panel);
    }
    @Override public void mouseDragged(MouseEvent e) {
        if (origin == null) {
            return;
        }
        JComponent panel = (JComponent) e.getComponent();
        //ずれた分だけ JPanel を移動させる
        int dx = e.getX() - origin.x;
        int dy = e.getY() - origin.y;
        Point pt = panel.getLocation();
        panel.setLocation(pt.x + dx, pt.y + dy);
    }
}

//背景画像を描画する JLayeredPane
class BGImageLayeredPane extends JLayeredPane {
    private final Image bgImage;
    public BGImageLayeredPane(Image img) {
        super();
        this.bgImage = img;
    }
    @Override public boolean isOptimizedDrawingEnabled() {
        return false;
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            int imageh = bgImage.getHeight(null);
            int imagew = bgImage.getWidth(null);
            Dimension d = getSize();
            for (int h = 0; h < d.getHeight(); h += imageh) {
                for (int w = 0; w < d.getWidth(); w += imagew) {
                    g.drawImage(bgImage, w, h, this);
                }
            }
        }
    }
}
