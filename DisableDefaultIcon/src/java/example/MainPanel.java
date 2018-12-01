package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final URL url = getClass().getResource("16x16transparent.png");

    public MainPanel() {
        super(new BorderLayout());

        JRadioButton r1 = new JRadioButton("img=null");
        r1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setIconImage(null);
            }
        });
        JRadioButton r2 = new JRadioButton("img=new ImageIcon(\"\").getImage()");
        r2.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setIconImage(new ImageIcon("").getImage()); // JDK 1.5
            }
        });

        JRadioButton r3 = new JRadioButton("img=new BufferedImage(1, 1, TYPE_INT_ARGB)");
        r3.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)); // size=(1x1)
            }
        });

        JRadioButton r4 = new JRadioButton("img=toolkit.createImage(url_16x16transparent)", true);
        r4.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setIconImage(Toolkit.getDefaultToolkit().createImage(url)); // 16x16transparent.png
            }
        });

        EventQueue.invokeLater(() -> setIconImage(Toolkit.getDefaultToolkit().createImage(url)));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder("frame.setIconImage(img)"));
        ButtonGroup bg = new ButtonGroup();
        Stream.of(r1, r2, r3, r4).forEach(b -> {
            bg.add(b);
            box.add(b);
            box.add(Box.createVerticalStrut(5));
        });
        add(box);
        setPreferredSize(new Dimension(320, 240));
    }
    protected void setIconImage(Image image) {
        Container c = getTopLevelAncestor();
        if (c instanceof JFrame) {
            ((JFrame) c).setIconImage(image);
        }
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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
