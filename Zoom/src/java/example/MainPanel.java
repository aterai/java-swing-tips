package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
        final ZoomImage zoom = new ZoomImage(icon);

        JButton button1 = new JButton(new AbstractAction("Zoom In") {
            @Override public void actionPerformed(ActionEvent ae) {
                zoom.changeScale(-5);
            }
        });
        JButton button2 = new JButton(new AbstractAction("Zoom Out") {
            @Override public void actionPerformed(ActionEvent ae) {
                zoom.changeScale(5);
            }
        });
        JButton button3 = new JButton(new AbstractAction("Original size") {
            @Override public void actionPerformed(ActionEvent ae) {
                zoom.initScale();
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(button1);
        box.add(button2);
        box.add(button3);

        add(zoom);
        add(box,  BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    static class ZoomImage extends JComponent implements MouseWheelListener {
        private final transient ImageIcon icon;
        private final int iw;
        private final int ih;
        private double scale = 1.0d;
        public ZoomImage(ImageIcon icon) {
            super();
            this.icon = icon;
            iw = icon.getIconWidth();
            ih = icon.getIconHeight();
            addMouseWheelListener(this);
        }
        @Override public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.scale(scale, scale);
            g2.drawImage(icon.getImage(), 0, 0, iw, ih, this);
            g2.dispose();
        }
        @Override public void mouseWheelMoved(MouseWheelEvent e) {
            changeScale(e.getWheelRotation());
        }
        public void initScale() {
            scale = 1.0d;
            repaint();
        }
        public void changeScale(int iv) {
            scale = Math.max(0.05d, Math.min(5.0d, scale-iv*0.05d));
            repaint();
//             double v = scale - iv * 0.1d;
//             if(v-1.0d>-1.0e-2) {
//                 scale = Math.min(10.0d, v);
//             }else{
//                 scale = Math.max(0.01d, scale - iv * 0.01d);
//             }
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
