package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.URL;
import javax.imageio.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    public MainPanel() {
        super(new BorderLayout());
        URL url = getClass().getResource("9-0.gif");
        BufferedImage bi = null;
        try{
            bi = ImageIO.read(url);
        }catch(IOException ex){
            ex.printStackTrace();
            return;
        }
        final ImageIcon icon9 = new ImageIcon(bi);
        final ImageIcon animatedIcon = new ImageIcon(url);
        JButton button = new JButton(icon9) {
            @Override protected void fireStateChanged() {
                ButtonModel m = getModel();
                if(isRolloverEnabled() && m.isRollover()) {
                    textArea.append("JButton: Rollover, Image: flush\n");
                    animatedIcon.getImage().flush();
                }
                super.fireStateChanged();
            };
        };
        button.setRolloverIcon(animatedIcon);
        button.setPressedIcon(new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y, getIconWidth(), getIconHeight());
                g2.dispose();
            }
            @Override public int getIconWidth()  { return icon9.getIconWidth();  }
            @Override public int getIconHeight() { return icon9.getIconHeight(); }
        });

        JLabel label = new JLabel(animatedIcon);
        label.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                textArea.append("JLabel: mousePressed, Image: flush\n");
                animatedIcon.getImage().flush();
                repaint(getBounds());
            }
        });

        JPanel p = new JPanel(new GridLayout(1, 2, 5, 5));
        p.add(makeTitledPanel("JButton#setRolloverIcon", button));
        p.add(makeTitledPanel("mousePressed: flush", label));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p= new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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
