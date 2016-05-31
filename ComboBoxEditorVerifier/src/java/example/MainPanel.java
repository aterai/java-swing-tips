package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private static final String ENTER_PRESSED = "enterPressed";
    private final String[] model = {"123456", "7890"};
    public MainPanel() {
        super(new BorderLayout());
        JComboBox combo = new JComboBox<String>(model);
        combo.setEditable(true);

        JComboBox<String> comboBox = new JComboBox<String>(model) {
            @Override public void updateUI() {
                getActionMap().put(ENTER_PRESSED, null);
                super.updateUI();
                final JComboBox<String> cb = this;
                final Action defaultEnterPressedAction = getActionMap().get(ENTER_PRESSED);
                getActionMap().put(ENTER_PRESSED, new AbstractAction() {
                    @Override public void actionPerformed(ActionEvent e) {
                        boolean isPopupVisible = isPopupVisible();
                        setPopupVisible(false);
                        DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) getModel();
                        String str = Objects.toString(getEditor().getItem(), "");
                        if (m.getIndexOf(str) < 0 && getInputVerifier().verify(cb)) {
                            m.removeElement(str);
                            m.insertElementAt(str, 0);
                            if (m.getSize() > 10) {
                                m.removeElementAt(10);
                            }
                            setSelectedIndex(0);
                            setPopupVisible(isPopupVisible);
                        } else {
                            defaultEnterPressedAction.actionPerformed(e);
                        }
                    }
                });
            }
        };
        comboBox.setEditable(true);
        comboBox.setInputVerifier(new LengthInputVerifier());
        comboBox.setEditor(new BasicComboBoxEditor() {
            private Component editorComponent;
            ////@see javax/swing/plaf/synth/SynthComboBoxUI.java
            //@Override public JTextField createEditorComponent() {
            //    JTextField f = new JTextField("", 9);
            //    f.setName("ComboBox.textField");
            //    return f;
            //}
            @Override public Component getEditorComponent() {
                if (editorComponent == null) {
                    JTextComponent tc = (JTextComponent) super.getEditorComponent();
                    editorComponent = new JLayer<JTextComponent>(tc, new ValidationLayerUI());
                }
                return editorComponent;
            }
        });
        comboBox.addPopupMenuListener(new SelectItemMenuListener());

        JPanel p = new JPanel(new GridLayout(5, 1));
        p.add(new JLabel("Default:", SwingConstants.LEFT));
        p.add(combo);
        p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("6 >= str.length()", SwingConstants.LEFT));
        p.add(comboBox);
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class SelectItemMenuListener implements PopupMenuListener {
    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JComboBox c = (JComboBox) e.getSource();
        c.setSelectedItem(c.getEditor().getItem());
    }
    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { /* not needed */ }
    @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
}

//@see http://docs.oracle.com/javase/tutorial/uiswing/examples/misc/FieldValidatorProject/src/FieldValidator.java
class ValidationLayerUI extends LayerUI<JTextComponent> {
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        //JLayer jlayer = (JLayer) c;
        //JTextComponent tc = (JTextComponent) jlayer.getView();
        Container p = SwingUtilities.getAncestorOfClass(JComboBox.class, c);
        if (p instanceof JComboBox) {
            JComboBox cb = (JComboBox) p;
            getInputVerifier(cb).filter(iv -> !iv.verify(cb)).ifPresent(iv -> {
                int w = c.getWidth();
                int h = c.getHeight();
                int s = 8;
                int pad = 5;
                int x = w - pad - s;
                int y = (h - s) / 2;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(Color.RED);
                g2.fillRect(0, 0, s + 1, s + 1);
                g2.setPaint(Color.WHITE);
                g2.drawLine(0, 0, s, s);
                g2.drawLine(0, s, s, 0);
                g2.dispose();
            });
        }
    }
    private static Optional<? extends InputVerifier> getInputVerifier(JComponent c) {
        return Optional.ofNullable(c.getInputVerifier());
    }
}

class LengthInputVerifier extends InputVerifier {
    private static final int MAX_LEN = 6;
    @Override public boolean verify(JComponent c) {
        if (c instanceof JComboBox) {
            JComboBox cb = (JComboBox) c;
            String str = Objects.toString(cb.getEditor().getItem(), "");
            return MAX_LEN - str.length() >= 0;
        }
        return false;
    }
}
