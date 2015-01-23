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
    private final JPanel p = new JPanel();
    private final JButton b1 = new JButton("button");
    private final JButton b2 = new JButton();
    private final List<JButton> list = Arrays.asList(b1, b2);
    private final List<JCheckBox> clist = Arrays.asList(
        new JCheckBox(new AbstractAction("setFocusPainted") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean flg = ((JCheckBox) e.getSource()).isSelected();
                for (JButton b: list) {
                    b.setFocusPainted(flg);
                }
                p.revalidate();
            }
        }),
        new JCheckBox(new AbstractAction("setBorderPainted") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean flg = ((JCheckBox) e.getSource()).isSelected();
                for (JButton b: list) {
                    b.setBorderPainted(flg);
                }
                p.revalidate();
            }
        }),
        new JCheckBox(new AbstractAction("setContentAreaFilled") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean flg = ((JCheckBox) e.getSource()).isSelected();
                for (JButton b: list) {
                    b.setContentAreaFilled(flg);
                }
                p.revalidate();
            }
        }),
        new JCheckBox(new AbstractAction("setRolloverEnabled") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean flg = ((JCheckBox) e.getSource()).isSelected();
                for (JButton b: list) {
                    b.setRolloverEnabled(flg);
                }
                p.revalidate();
            }
        })
        //new JCheckBox(new AbstractAction("setBorder(null:BorderFactory.createLineBorder)") {
        //    @Override public void actionPerformed(ActionEvent e) {
        //        boolean flg = ((JCheckBox) e.getSource()).isSelected();
        //        Border border = flg ? BorderFactory.createLineBorder(Color.RED, 5) : null;
        //        for (JButton b: list) b.setBorder(border);
        //        p.revalidate();
        //    }
        //}),
        );


    public MainPanel() {
        super(new BorderLayout());

        ImageIcon rss = new ImageIcon(getClass().getResource("feed-icon-14x14.png")); //http://feedicons.com/
        b2.setIcon(rss);
        b2.setRolloverIcon(makeRolloverIcon(rss));

        p.add(b1);
        p.add(b2);
        p.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        Box box = Box.createVerticalBox();
        for (JCheckBox c: clist) {
            c.setSelected(true);
            box.add(c);
        }
        add(box, BorderLayout.NORTH);
        add(p);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
        RescaleOp op = new RescaleOp(
            new float[] {1.2f, 1.2f, 1.2f, 1f},
            new float[] {0f, 0f, 0f, 0f}, null);
        BufferedImage img = new BufferedImage(
            srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        //g2.drawImage(srcIcon.getImage(), 0, 0, null);
        srcIcon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return new ImageIcon(op.filter(img, null));
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
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
