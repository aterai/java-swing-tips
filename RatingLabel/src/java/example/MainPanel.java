package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 2, 4, 4));
        //PI Diagona Icons Pack 1.0 - Download Royalty Free Icons and Stock Images For Web & Graphics Design
        //http://www.freeiconsdownload.com/Free_Downloads.asp?id=60
        ImageIcon defaultIcon = new ImageIcon(getClass().getResource("31g.png"));
        ImageProducer ip = defaultIcon.getImage().getSource();

        // 1
        List<ImageIcon> list = Arrays.asList(
            makeStarImageIcon(ip,  1f, .5f, .5f),
            makeStarImageIcon(ip, .5f,  1f, .5f),
            makeStarImageIcon(ip,  1f, .5f,  1f),
            makeStarImageIcon(ip, .5f, .5f,  1f),
            makeStarImageIcon(ip,  1f,  1f, .5f));
        add(makeStarRatingPanel("gap=0", new LevelBar(defaultIcon, list, 0)));

        // 2
        list = Arrays.asList(
            makeStarImageIcon(ip, .2f, .5f, .5f),
            makeStarImageIcon(ip,  0f,  1f, .2f),
            makeStarImageIcon(ip,  1f,  1f, .2f),
            makeStarImageIcon(ip, .8f, .4f, .2f),
            makeStarImageIcon(ip,  1f, .1f, .1f));
        add(makeStarRatingPanel("gap=1+1", new LevelBar(defaultIcon, list, 1) {
            @Override protected void repaintIcon(int index) {
                for (int i = 0; i < labelList.size(); i++) {
                    labelList.get(i).setIcon(i <= index ? iconList.get(index) : defaultIcon);
                }
                repaint();
            }
        }));

        // 3
        list = Arrays.asList(
            makeStarImageIcon(ip, .6f, .6f, 0f),
            makeStarImageIcon(ip, .7f, .7f, 0f),
            makeStarImageIcon(ip, .8f, .8f, 0f),
            makeStarImageIcon(ip, .9f, .9f, 0f),
            makeStarImageIcon(ip,  1f,  1f, 0f));
        add(makeStarRatingPanel("gap=2+2", new LevelBar(defaultIcon, list, 2)));

        // 4
        ImageIcon yStar = makeStarImageIcon(ip, 1f, 1f, 0f);
        list = Arrays.asList(yStar, yStar, yStar, yStar, yStar);
        add(makeStarRatingPanel("gap=1+1", new LevelBar(defaultIcon, list, 1)));
        setPreferredSize(new Dimension(320, 240));
    }
    private JPanel makeStarRatingPanel(String title, LevelBar label) {
        JButton button = new JButton("clear");
        button.addActionListener(e -> label.clear());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(button);
        p.add(label);
        return p;
    }
    private static ImageIcon makeStarImageIcon(ImageProducer ip, float rf, float gf, float bf) {
        return new ImageIcon(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(ip, new SelectedImageFilter(rf, gf, bf))));
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

class LevelBar extends JPanel implements MouseListener, MouseMotionListener {
    private final int gap;
    protected final List<ImageIcon> iconList;
    protected final List<JLabel> labelList = Arrays.asList(
        new JLabel(), new JLabel(), new JLabel(), new JLabel(), new JLabel()
    );
    protected final ImageIcon defaultIcon;
    private int clicked = -1;
    protected LevelBar(ImageIcon defaultIcon, List<ImageIcon> list, int gap) {
        super(new GridLayout(1, 5, gap * 2, gap * 2));
        this.defaultIcon = defaultIcon;
        this.iconList = list;
        this.gap = gap;
        for (JLabel l: labelList) {
            l.setIcon(defaultIcon);
            add(l);
        }
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    public void clear() {
        clicked = -1;
        repaintIcon(clicked);
    }
    public int getLevel() {
        return clicked;
    }
    public void setLevel(int l) {
        clicked = l;
        repaintIcon(clicked);
    }
    private int getSelectedIconIndex(Point p) {
        for (int i = 0; i < labelList.size(); i++) {
            Rectangle r = labelList.get(i).getBounds();
            r.grow(gap, gap);
            if (r.contains(p)) {
                return i;
            }
        }
        return -1;
    }
    protected void repaintIcon(int index) {
        for (int i = 0; i < labelList.size(); i++) {
            labelList.get(i).setIcon(i <= index ? iconList.get(i) : defaultIcon);
        }
        repaint();
    }
    @Override public void mouseMoved(MouseEvent e) {
        repaintIcon(getSelectedIconIndex(e.getPoint()));
    }
    @Override public void mouseEntered(MouseEvent e) {
        repaintIcon(getSelectedIconIndex(e.getPoint()));
    }
    @Override public void mouseClicked(MouseEvent e) {
        clicked = getSelectedIconIndex(e.getPoint());
    }
    @Override public void mouseExited(MouseEvent e) {
        repaintIcon(clicked);
    }
    @Override public void mouseDragged(MouseEvent e)  { /* not needed */ }
    @Override public void mousePressed(MouseEvent e)  { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
}

class SelectedImageFilter extends RGBImageFilter {
    private final float rf;
    private final float gf;
    private final float bf;
    protected SelectedImageFilter(float rf, float gf, float bf) {
        super();
        this.rf = Math.min(1f, rf);
        this.gf = Math.min(1f, gf);
        this.bf = Math.min(1f, bf);
        canFilterIndexColorModel = false;
    }
//     @Override public int filterRGB(int x, int y, int argb) {
//         Color color = new Color(argb, true);
//         float[] array = new float[4];
//         color.getComponents(array);
//         return new Color(array[0] * filter[0], array[1] * filter[1], array[2] * filter[2], array[3]).getRGB();
//     }
    @Override public int filterRGB(int x, int y, int argb) {
        int r = (int) (((argb >> 16) & 0xFF) * rf);
        int g = (int) (((argb >>  8) & 0xFF) * gf);
        int b = (int) (((argb)       & 0xFF) * bf);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | (b);
    }
}
