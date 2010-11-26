package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        ImageIcon image = new ImageIcon(getClass().getResource("16x16.png"));
        int w = image.getIconWidth();
        int h = image.getIconHeight();

        JTextField field = new JTextField("bbbbbbbbbb");
        Insets m = field.getMargin();
        field.setMargin(new Insets(m.top,m.left+w,m.bottom,m.right));

        JLabel label = new JLabel(image);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        label.setBorder(null);
        label.setBounds(m.left,m.top,w,h);
        field.add(label);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(makePanel("Default", new JTextField("aaaaaaaaaaaa")));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("add Image(JLabel)", field));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 160));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
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
