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
    private final LineCursorTextArea textArea= new LineCursorTextArea();
    public MainPanel() {
        super(new BorderLayout());
        textArea.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        textArea.setText("Line Cursor Test\n\naaaaaaaaaaafasdfas");
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.setLineWrap(check.isSelected());
                textArea.requestFocusInWindow();
            }
        });
        add(check, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 240));
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

class LineCursorTextArea extends JTextArea {
    private static final Color LINE_COLOR = Color.BLUE;
    private final DefaultCaret caret;
    public LineCursorTextArea() {
        super();
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
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        Insets i = getInsets();
        //int y = g2.getFontMetrics().getHeight()*getLineAtCaret(this)+i.top;
        int y = caret.y+caret.height-1;
        g2.setPaint(LINE_COLOR);
        g2.drawLine(i.left, y, getSize().width-i.left-i.right, y);
    }
//     public static int getLineAtCaret(JTextComponent component) {
//         int caretPosition = component.getCaretPosition();
//         Element root = component.getDocument().getDefaultRootElement();
//         return root.getElementIndex(caretPosition)+1;
//     }
}
