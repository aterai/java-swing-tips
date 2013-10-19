package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JTextComponent field1 = new JPasswordField();
        Box b = Box.createHorizontalBox();
        b.add(new JLabel("Password: "));
        b.add(field1);
        b.add(Box.createHorizontalGlue());
        JTextComponent field2 = new WatermarkPasswordField();

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        box.add(makePanel("JPasswordField", b));
        box.add(Box.createVerticalStrut(16));
        box.add(makePanel("InputHintPasswordField", field2));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
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

class WatermarkPasswordField extends JPasswordField implements FocusListener, DocumentListener {
    private boolean showWatermark = true;
    public WatermarkPasswordField() {
        super();
        addFocusListener(this);
        getDocument().addDocumentListener(this);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(showWatermark) {
            Graphics2D g2 = (Graphics2D)g.create();
            Insets i = getInsets();
            Font font = getFont();
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout tl = new TextLayout("Password", font, frc);
            g2.setPaint(hasFocus()?Color.GRAY:Color.BLACK);
            int baseline = getBaseline(getWidth(), getHeight());
            tl.draw(g2, i.left+1, baseline);
            g2.dispose();
        }
    }
    @Override public void focusGained(FocusEvent e) {
        repaint();
    }
    @Override public void focusLost(FocusEvent e) {
        showWatermark = getPassword().length==0;
        repaint();
    }
    @Override public void insertUpdate(DocumentEvent e) {
        showWatermark = e.getDocument().getLength()==0;
        repaint();
    }
    @Override public void removeUpdate(DocumentEvent e) {
        showWatermark = e.getDocument().getLength()==0;
        repaint();
    }
    @Override public void changedUpdate(DocumentEvent e) {}
}
