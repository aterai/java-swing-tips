package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.border.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2,1));
        JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.01));
        JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(spinner1, "0%");
        spinner1.setEditor(editor1);

        JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spinner2) {
            @Override public void updateUI() {
                if(getComponentCount()>0) {
                    JFormattedTextField f = getTextField();
                    f.setBorder(null); //Nimbus
                    super.updateUI();
                    initTextFieldBorder(f);
                }else{
                    super.updateUI();
                }
            }
        };
        spinner2.setEditor(editor2);
        initTextFieldBorder(editor2.getTextField());

//         // Component Border - Java Tips Weblog
//         // http://tips4java.wordpress.com/2009/09/27/component-border/
//         JLabel label = new JLabel("%");
//         label.setBorder(BorderFactory.createEmptyBorder());
//         label.setOpaque(true);
//         label.setBackground(Color.WHITE);
//         ComponentBorder cb = new ComponentBorder(label);
//         cb.setGap(0);
//         cb.install(editor2);

        add(makeTitlePanel(spinner1, "JSpinner+Default"));
        add(makeTitlePanel(spinner2, "JSpinner+StringBorder"));
        setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
        setPreferredSize(new Dimension(320, 200));
    }
    private static void initTextFieldBorder(final JTextField textField) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                Border b = new StringBorder(textField, "%");
                //if(textField.getUI() instanceof javax.swing.plaf.synth.SynthFormattedTextFieldUI) {
                if(textField.getUI().getClass().getName().startsWith("Synth")) {
                    Border c = textField.getBorder();
                    textField.setBorder((c==null)?b:BorderFactory.createCompoundBorder(c, b));
                }else{
                    textField.setBorder(b);
                }
            }
        });
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
class StringBorder implements Border {
    private final JComponent parent;
    private final Insets insets;
    private final Rectangle rect;
    private final String str;
    public StringBorder(JComponent parent, String str) {
        this.parent = parent;
        this.str = str;
        FontRenderContext frc = new FontRenderContext(null, true, true);
        rect = parent.getFont().getStringBounds(str, frc).getBounds();
        insets = new Insets(0,0,0,rect.width);
    }
    @Override public Insets getBorderInsets(Component c) {
        return insets;
    }
    @Override public boolean isBorderOpaque() {
        return false;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D)g;
        float tx = x + width - rect.width;
        float ty = y - rect.y + (height - rect.height)/2;
        //g2.setPaint(Color.RED);
        g2.drawString(str, tx, ty);
    }
}
