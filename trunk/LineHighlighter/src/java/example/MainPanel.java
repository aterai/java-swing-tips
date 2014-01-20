package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("LineWrap");
    private final HighlightCursorTextArea textArea= new HighlightCursorTextArea();
    public MainPanel() {
        super(new BorderLayout());
        textArea.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        textArea.setText("Highlight Cursor Test\n\naaaaaaaaaaaasdfasdfasdfasdfsadffasdfas");

        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.setLineWrap(check.isSelected());
                textArea.requestFocusInWindow();
            }
        });
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.getViewport().setBackground(Color.WHITE);
        add(check, BorderLayout.NORTH);
        add(scroll);
        setPreferredSize(new Dimension(320, 200));
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

class HighlightCursorTextArea extends JTextArea {
    private static final Color linecolor = new Color(250,250,220);
    private final DefaultCaret caret;
    public HighlightCursorTextArea() {
        super();
        setOpaque(false);
        caret = new DefaultCaret() {
            @Override protected synchronized void damage(Rectangle r) {
                if(r!=null) {
                    JTextComponent c = getComponent();
                    x = 0;
                    y = r.y;
                    width  = c.getSize().width;
                    height = r.height;
                    c.repaint();
                }
            }
        };
        caret.setBlinkRate(getCaret().getBlinkRate());
        setCaret(caret);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Insets i = getInsets();
        int h = caret.height;
        int y = caret.y;
        g2.setPaint(linecolor);
        g2.fillRect(i.left, y, getSize().width-i.left-i.right, h);
        super.paintComponent(g);
    }
}
