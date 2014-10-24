package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final URL url = getClass().getResource("16x16transparent.png");
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        final JRadioButton r1 = new JRadioButton("img=null");
        final JRadioButton r2 = new JRadioButton("img=new ImageIcon(\"\").getImage()");
        final JRadioButton r3 = new JRadioButton("img=new BufferedImage(1, 1, TYPE_INT_ARGB)");
        final JRadioButton r4 = new JRadioButton("img=toolkit.createImage(url_16x16transparent)");

        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                AbstractButton b = (AbstractButton) e.getSource();
                Image image = null;
                if (b.equals(r2)) {
                    //JDK 1.5
                    image = new ImageIcon("").getImage();
                } else if (b.equals(r3)) {
                    //size=(1x1)
                    image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                } else if (b.equals(r4)) {
                    //16x16transparent.png
                    image = Toolkit.getDefaultToolkit().createImage(url);
                }
                frame.setIconImage(image);
            }
        };
        r4.setSelected(true);
        Image image = Toolkit.getDefaultToolkit().createImage(url);
        frame.setIconImage(image);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder("frame.setIconImage(img)"));
        ButtonGroup bg = new ButtonGroup();
        for (AbstractButton b: Arrays.asList(r1, r2, r3, r4)) {
            b.addActionListener(al);
            bg.add(b);
            box.add(b);
            box.add(Box.createVerticalStrut(5));
        }
        add(box);
        setPreferredSize(new Dimension(320, 180));
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
