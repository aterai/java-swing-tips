package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());

        JLabel label1 = new JLabel("Mail Adress:", SwingConstants.RIGHT);
        label1.setDisplayedMnemonic('M');
        JTextComponent textField1 = new JTextField(12);
        label1.setLabelFor(textField1);

        JLabel label2 = new JLabel("Password:", SwingConstants.RIGHT);
        label2.setDisplayedMnemonic('P');
        JTextComponent textField2 = new JPasswordField(12);
        label2.setLabelFor(textField2);

        JLabel label3 = new JLabel("Dummy:", SwingConstants.RIGHT);
        JTextComponent textField3 = new JTextField(12);

        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());

        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;
        addRow(label1, textField1, panel, c);

        c.gridy = 1;
        addRow(label2, textField2, panel, c);

        c.gridy = 2;
        addRow(label3, textField3, panel, c);











        panel.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void addRow(JComponent c1, JComponent c2, JPanel p, GridBagConstraints c) {
        c.gridx = 0;
        c.weightx = 0.0;
        c.insets = new Insets(5, 5, 5, 0);
        c.anchor = GridBagConstraints.EAST;
        p.add(c1, c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(c2, c);
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
