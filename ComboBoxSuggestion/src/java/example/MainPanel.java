package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        String[] array = {
            "aaaa", "aaaabbb", "aaaabbbcc", "aaaabbbccddd",
            "abcde", "abefg", "bbb1", "bbb12"};
        JComboBox<String> combo = makeComboBox(array);
        combo.setEditable(true);
        combo.setSelectedIndex(-1);
        JTextField field = (JTextField) combo.getEditor().getEditorComponent();
        field.setText("");
        field.addKeyListener(new ComboKeyHandler(combo));

        // InputMap im = combo.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed2");
        // combo.getActionMap().put("enterPressed2", new AbstractAction() {
        //     @Override public void actionPerformed(ActionEvent e) {
        //         String text = field.getText();
        //         if (!model.contains(text)) {
        //             model.addElement(text);
        //             Collections.sort(model);
        //             setModel(getSuggestedModel(model, text), text);
        //         }
        //         combo.hidePopup();
        //         // hide_flag = true;
        //     }
        // });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Auto-Completion ComboBox"));
        p.add(combo, BorderLayout.NORTH);

        Box box = Box.createVerticalBox();
        box.add(makeHelpPanel());
        box.add(Box.createVerticalStrut(5));
        box.add(p);
        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComboBox<String> makeComboBox(String... model) {
        return new JComboBox<>(model);
        // // Test: Remove ArrowButtn(look like a JTextField)
        // // UIManager.put("ComboBox.squareButton", Boolean.FALSE);
        // return new JComboBox(model) {
        //     @Override public void updateUI() {
        //         super.updateUI();
        //         setUI(new BasicComboBoxUI() {
        //             @Override protected JButton createArrowButton() {
        //                 JButton button = new JButton() {
        //                     @Override public int getWidth() {
        //                         return 0;
        //                     }
        //                 };
        //                 button.setBorder(BorderFactory.createEmptyBorder());
        //                 button.setVisible(false);
        //                 return button;
        //             }
        //             @Override public void configureArrowButton() {}
        //         });
        //         // // Remove click on a JComboBox Border
        //         // for (MouseListener ml: getMouseListeners()) {
        //         //     removeMouseListener(ml);
        //         // }
        //     }
        //     // Remove click on a JComboBox Border
        //     @Override public boolean contains(int x, int y) {
        //         Insets i = getInsets();
        //         int w = getWidth() - i.left - i.right;
        //         int h = getHeight() - i.top - i.bottom;
        //         return (x >= i.left) && (x < w) && (y >= i.top) && (y < h);
        //     }
        // };
    }
    private static Component makeHelpPanel() {
        JPanel lp = new JPanel(new GridLayout(2, 1, 2, 2));
        lp.add(new JLabel("Char: show Popup"));
        lp.add(new JLabel("ESC: hide Popup"));

        JPanel rp = new JPanel(new GridLayout(2, 1, 2, 2));
        rp.add(new JLabel("RIGHT: Completion"));
        rp.add(new JLabel("ENTER: Add/Selection"));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Help"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 5, 0, 5);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1d;

        c.weightx = 1d;
        p.add(lp, c);

        c.weightx = 0d;
        p.add(new JSeparator(SwingConstants.VERTICAL), c);

        c.weightx = 1d;
        p.add(rp, c);

        return p;
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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

class ComboKeyHandler extends KeyAdapter {
    private final JComboBox<String> comboBox;
    private final List<String> list = new ArrayList<>();
    private boolean shouldHide;

    protected ComboKeyHandler(JComboBox<String> combo) {
        super();
        this.comboBox = combo;
        for (int i = 0; i < comboBox.getModel().getSize(); i++) {
            list.add(comboBox.getItemAt(i));
        }
    }
    @Override public void keyTyped(KeyEvent e) {
        EventQueue.invokeLater(() -> {
            String text = ((JTextField) e.getComponent()).getText();
            ComboBoxModel<String> m;
            if (text.isEmpty()) {
                m = new DefaultComboBoxModel<>(list.toArray(new String[0]));
                setSuggestionModel(comboBox, m, "");
                comboBox.hidePopup();
            } else {
                m = getSuggestedModel(list, text);
                if (m.getSize() == 0 || shouldHide) {
                    comboBox.hidePopup();
                } else {
                    setSuggestionModel(comboBox, m, text);
                    comboBox.showPopup();
                }
            }
        });
    }
    @Override public void keyPressed(KeyEvent e) {
        JTextField textField = (JTextField) e.getComponent();
        String text = textField.getText();
        shouldHide = false;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                for (String s: list) {
                    if (s.startsWith(text)) {
                        textField.setText(s);
                        return;
                    }
                }
                break;
            case KeyEvent.VK_ENTER:
                if (!list.contains(text)) {
                    list.add(text);
                    Collections.sort(list);
                    // setSuggestionModel(comboBox, new DefaultComboBoxModel(list), text);
                    setSuggestionModel(comboBox, getSuggestedModel(list, text), text);
                }
                shouldHide = true;
                break;
            case KeyEvent.VK_ESCAPE:
                shouldHide = true;
                break;
            default:
                break;
        }
    }
    private static void setSuggestionModel(JComboBox<String> comboBox, ComboBoxModel<String> mdl, String str) {
        comboBox.setModel(mdl);
        comboBox.setSelectedIndex(-1);
        ((JTextField) comboBox.getEditor().getEditorComponent()).setText(str);
    }
    private static ComboBoxModel<String> getSuggestedModel(List<String> list, String text) {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        for (String s: list) {
            if (s.startsWith(text)) {
                m.addElement(s);
            }
        }
        return m;
    }
}

// class DefaultSuggestionComboBoxUI extends BasicComboBoxUI {
//     public static BasicComboBoxUI createUI(JComponent c) {
//         return new DefaultSuggestionComboBoxUI();
//     }
//     private KeyListener editorKeyListener = null;
//     private Vector<String> list = new Vector<>();
//     @Override protected void configureEditor() {
//         super.configureEditor();
//         JTextField textField = (JTextField) editor;
//         comboBox.setSelectedIndex(-1);
//         if (editorKeyListener == null) {
//             editorKeyListener = new ComboKeyHandler();
//         }
//         editor.addKeyListener(editorKeyListener);
//         for (int i = 0; i < comboBox.getModel().getSize(); i++) {
//             list.addElement(comboBox.getItemAt(i));
//         }
//         textField.setText("");
//     }
//     @Override protected void unconfigureEditor() {
//         super.unconfigureEditor();
//         if (editorKeyListener != null) {
//             editor.removeKeyListener(editorKeyListener);
//         }
//     }
//     class ComboKeyHandler extends KeyAdapter {
//         private boolean shouldHide = false;
//         @Override public void keyTyped(KeyEvent e) {
//             EventQueue.invokeLater(new Runnable() {
//                 @Override public void run() {
//                     String text = ((JTextField) e.getSource()).getText();
//                     ComboBoxModel m = getSuggestedModel(list, text);
//                     if (m.getSize() == 0 || shouldHide) {
//                         comboBox.hidePopup();
//                     } else {
//                         setSuggestionModel(m, text);
//                         comboBox.showPopup();
//                     }
//                 }
//             });
//         }
//         @Override public void keyPressed(KeyEvent e) {
//             JTextField textField = (JTextField) e.getSource();
//             String t = textField.getText();
//             shouldHide = false;
//             switch (e.getKeyCode()) {
//               case KeyEvent.VK_RIGHT:
//                 for (String s: list) {
//                     if (s.startsWith(t)) {
//                         textField.setText(s);
//                         return;
//                     }
//                 }
//                 break;
//               case KeyEvent.VK_ENTER:
//                 if (!list.contains(t)) {
//                     list.addElement(t);
//                     Collections.sort(list);
//                     setSuggestionModel(new DefaultComboBoxModel(list), t);
//                 }
//                 shouldHide = true;
//                 break;
//               case KeyEvent.VK_ESCAPE:
//                 shouldHide = true;
//                 break;
//             }
//         }
//         private void setSuggestionModel(ComboBoxModel mdl, String str) {
//             comboBox.setModel(mdl);
//             comboBox.setSelectedIndex(-1);
//             ((JTextField) comboBox.getEditor().getEditorComponent()).setText(str);
//         }
//         private ComboBoxModel getSuggestedModel(Vector<String> list, String text) {
//             DefaultComboBoxModel m = new DefaultComboBoxModel();
//             if (Objects.nonNull(text) && text.isEmpty()) {
//                 for (String s: list) {
//                     if (s.startsWith(text)) {
//                         m.addElement(s);
//                     }
//                 }
//             }
//             return m;
//         }
//     }
// }
