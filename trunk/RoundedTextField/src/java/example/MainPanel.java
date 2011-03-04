package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        JTextField textField01 = new JTextField(20) {
            //Unleash Your Creativity with Swing and the Java 2D API!
            //http://java.sun.com/products/jfc/tsc/articles/swing2d/index.html
            @Override protected void paintComponent(Graphics g) {
                if(!isOpaque()) {
                    int w = getWidth();
                    int h = getHeight();
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, w-1, h-1, h, h);
                    g2.setColor(Color.GRAY);
                    g2.drawRoundRect(0, 0, w-1, h-1, h, h);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        textField01.setOpaque(false);
        textField01.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        textField01.setText("aaaaaaaaaaa");

        JTextField textField02 = new JTextField(20);
        textField02.setUI(new RoundTextUI());
        textField02.setText("bbbbbbbbbbbb");

        JPanel p = new JPanel(new GridLayout(2,1,5,5));
        p.add(makeTitlePanel(textField01, "Override: JTextField#paintComponent(...)"));
        p.add(makeTitlePanel(textField02, "setUI: RoundTextUI()"));
        add(p);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        setPreferredSize(new Dimension(320, 200));
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
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

//http://forums.sun.com/thread.jspa?threadID=260846
class RoundTextUI extends BasicTextFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new RoundTextUI();
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        c.setBorder(new RoundBorder());
        c.setOpaque(false);
    }
    @Override protected void paintSafely(Graphics g) {
        JComponent c = getComponent();
        if(!c.isOpaque()) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setColor(c.getBackground());
            g2.fillRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, c.getHeight(), c.getHeight());
            g2.dispose();
        }
        super.paintSafely(g);
    }
    private static class RoundBorder extends AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setColor(Color.GRAY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawRoundRect(x, y, width-1, height-1, height, height);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 4;
            return insets;
        }
    }
}
