package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        URL url1 = getClass().getResource("favicon.png");
        URL url2 = getClass().getResource("animated.gif");
        JComboBox<Icon> combo1 = new JComboBox<>(new Icon[] {new ImageIcon(url1), new ImageIcon(url2)});
        JComboBox<Icon> combo2 = new JComboBox<>();
        combo2.setModel(new DefaultComboBoxModel<>(new Icon[] {new ImageIcon(url1), makeImageIcon(url2, combo2, 1)}));

        JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        p.add(new JLabel("Default ImageIcon"));
        p.add(combo1);
        p.add(new JLabel("ImageIcon#setImageObserver(ImageObserver)"));
        p.add(combo2);
        add(p, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Icon makeImageIcon(URL url, final JComboBox combo, final int row) {
        ImageIcon icon = new ImageIcon(url);
        //Wastefulness: icon.setImageObserver(combo);
        icon.setImageObserver(new ImageObserver() {
            //@see http://www2.gol.com/users/tame/swing/examples/SwingExamples.html
            @Override public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
                if (combo.isShowing() && (infoflags & (FRAMEBITS | ALLBITS)) != 0) {
                    if (combo.getSelectedIndex() == row) {
                        combo.repaint();
                    }
                    BasicComboPopup p = (BasicComboPopup) combo.getAccessibleContext().getAccessibleChild(0);
                    JList list = p.getList();
                    if (list.isShowing()) {
                        list.repaint(list.getCellBounds(row, row));
                    }
                }
                return (infoflags & (ALLBITS | ABORT)) == 0;
            }
        });
        return icon;
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
