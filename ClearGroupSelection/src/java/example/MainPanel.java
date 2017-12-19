package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        ImageIcon nicon  = new ImageIcon(getClass().getResource("wi0063-32.png"));
        ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
        ImageIcon sicon  = new ImageIcon(createImage(ip));
        JToggleButton t1 = new JToggleButton(nicon);
        JToggleButton t2 = new JToggleButton(nicon, true);
        t1.setSelectedIcon(sicon);
        t2.setSelectedIcon(sicon);
        List<? extends AbstractButton> l = Arrays.asList(new JRadioButton("RadioButton1"), new JRadioButton("RadioButton2"), t1, t2);

        ButtonGroup bg = new ButtonGroup();
        JPanel p = new JPanel(new GridLayout(2, 2));
        p.setBorder(BorderFactory.createTitledBorder("ButtonGroup"));
        for (AbstractButton b: l) {
            bg.add(b);
            p.add(b);
        }

        JButton clear = new JButton("clearSelection");
        clear.addActionListener(e -> bg.clearSelection());

        add(p, BorderLayout.NORTH);
        add(clear, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
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

class SelectedImageFilter extends RGBImageFilter {
    //public SelectedImageFilter() {
    //    canFilterIndexColorModel = false;
    //}
    @Override public int filterRGB(int x, int y, int argb) {
        //Color color = new Color(argb, true);
        //float[] a = new float[4];
        //color.getComponents(a);
        //return new Color(a[0], a[1], a[2] * .5f, a[3]).getRGB();
        return (argb & 0xFFFFFF00) | ((argb & 0xFF) >> 1);
    }
}
