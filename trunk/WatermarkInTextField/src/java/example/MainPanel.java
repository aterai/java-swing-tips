package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        JTextField field1 = new JTextField("Please enter your E-mail address");
        field1.addFocusListener(new GhostFocusListener(field1));
        JTextField field2 = new WatermarkTextField();

        Action a = new AbstractAction("xxx") {
            @Override public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
            }
        };
        field2.getActionMap().put("xxx", a);
        InputMap im = field2.getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "xxx");

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(makePanel("E-mail", field1));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("Search", field2));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 180));
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

class WatermarkTextField extends JTextField implements FocusListener {
    private final ImageIcon image;
    private boolean showWatermark = true;
    public WatermarkTextField() {
        super();
        image = new ImageIcon(getClass().getResource("watermark.png"));
        addFocusListener(this);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(showWatermark) {
            Graphics2D g2d = (Graphics2D)g;
            //Insets i = getMargin();
            Insets i = getInsets();
            int yy = (getHeight()-image.getIconHeight())/2;
            g2d.drawImage(image.getImage(), i.left, yy, this);
        }
    }
    @Override public void focusGained(FocusEvent e) {
        showWatermark = false;
        repaint();
    }
    @Override public void focusLost(FocusEvent e) {
        showWatermark = "".equals(getText().trim());
        repaint();
    }
}
class GhostFocusListener implements FocusListener {
    private static final Color INACTIVE_COLOR = UIManager.getColor("TextField.inactiveForeground");
    private static final Color ORIGINAL_COLOR = UIManager.getColor("TextField.foreground");
    private final String ghostMessage;
    public GhostFocusListener(final JTextComponent tf) {
        ghostMessage = tf.getText();
        tf.setForeground(INACTIVE_COLOR);
    }
    @Override public void focusGained(final FocusEvent e) {
        JTextComponent textField = (JTextComponent)e.getSource();
        String str = textField.getText();
        Color col  = textField.getForeground();
        if(ghostMessage.equals(str) && INACTIVE_COLOR.equals(col)) {
            textField.setForeground(ORIGINAL_COLOR);
            textField.setText("");
        }
    }
    @Override public void focusLost(final FocusEvent e) {
        JTextComponent textField = (JTextComponent)e.getSource();
        String str = textField.getText().trim();
        if("".equals(str)) {
            textField.setForeground(INACTIVE_COLOR);
            textField.setText(ghostMessage);
        }
    }
}
