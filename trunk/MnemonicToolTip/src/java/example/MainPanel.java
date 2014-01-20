package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JButton b = (JButton)e.getSource();
                int mnemonic = b.getMnemonic();
                if(mnemonic<KeyEvent.VK_A) {
                    mnemonic = KeyEvent.VK_A;
                }else if(mnemonic<KeyEvent.VK_K) {
                    mnemonic++;
                }else{
                    mnemonic = 0;
                }
                b.setMnemonic(mnemonic);
            }
        };
        JButton button1 = new JButton("Hello World");
        button1.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();
                JButton b = (JButton)e.getSource();
                if(prop == AbstractButton.MNEMONIC_CHANGED_PROPERTY) {
                    String str = "Alt+"+KeyEvent.getKeyText(b.getMnemonic());
                    b.setToolTipText("tooltip ("+str+")");
                }
            }
        });
        JButton button2 = new JButton("abcdefghijk") {
            @Override public JToolTip createToolTip() {
                JToolTip tip = new MnemonicToolTip();
                tip.setComponent(this);
                return tip;
            }
        };

        button1.addActionListener(al);
        button1.setMnemonic(KeyEvent.VK_E);
        String str = "Alt+"+KeyEvent.getKeyText(button1.getMnemonic());
        button1.setToolTipText("tooltip ("+str+")");

        button2.addActionListener(al);
        button2.setMnemonic(KeyEvent.VK_A);
        button2.setToolTipText("tooltip");

        Box box = Box.createVerticalBox();
        box.add(button1);
        box.add(Box.createVerticalStrut(20));
        box.add(button2);
        box.add(Box.createVerticalGlue());

        add(box);
        setBorder(BorderFactory.createEmptyBorder(20,40,20,40));
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

class MnemonicToolTip extends JToolTip {
    private final JLabel mnemonicLabel = new JLabel();
    public MnemonicToolTip() {
        setLayout(new BorderLayout());
        //LookAndFeel.installColorsAndFont(mnemonicLabel, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
        mnemonicLabel.setForeground(Color.GRAY);
        mnemonicLabel.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));
        add(mnemonicLabel, BorderLayout.EAST);
    }
    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if(mnemonicLabel.isVisible()) {
            d.width += mnemonicLabel.getPreferredSize().width;
        }
        return d;
    }
    @Override public void setComponent(JComponent c) {
        if(c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton)c;
            int mnemonic = b.getMnemonic();
            if(mnemonic>0) {
                mnemonicLabel.setVisible(true);
                mnemonicLabel.setText("Alt+"+KeyEvent.getKeyText(mnemonic));
            }else{
                mnemonicLabel.setVisible(false);
            }
        }
        super.setComponent(c);
    }
}
