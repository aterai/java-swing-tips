package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea("aaaaaaaaaaaaaaaaa\nbbbbb\nccc");
    private final JCheckBox hidingCaretCheck = new JCheckBox("Hide Caret");
    private final JCheckBox hidingHighlighterCheck = new JCheckBox("Hide Highlighter");
    private final JCheckBox editableCheck = new JCheckBox("Editable", true);
    private final JCheckBox focusableCheck = new JCheckBox("Focusable", true);

    private MainPanel() {
        super(new BorderLayout());

        Caret hidingCaret = new DefaultCaret() {
            @Override public boolean isVisible() {
                return false;
            }
        };
        Caret defaultCaret = textArea.getCaret();
        Highlighter defaultHighlighter = textArea.getHighlighter();

        hidingCaretCheck.addActionListener(e -> textArea.setCaret(((JCheckBox) e.getSource()).isSelected() ? hidingCaret : defaultCaret));
        hidingHighlighterCheck.addActionListener(e -> textArea.setHighlighter(((JCheckBox) e.getSource()).isSelected() ? null : defaultHighlighter));

        editableCheck.addActionListener(e -> textArea.setEditable(((JCheckBox) e.getSource()).isSelected()));
        focusableCheck.addActionListener(e -> textArea.setFocusable(((JCheckBox) e.getSource()).isSelected()));

        JPanel p1 = new JPanel();
        p1.add(hidingCaretCheck);
        p1.add(hidingHighlighterCheck);
        JPanel p2 = new JPanel();
        p2.add(editableCheck);
        p2.add(focusableCheck);
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.add(p1, BorderLayout.NORTH);
        p.add(p2, BorderLayout.SOUTH);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        add(new JTextField(), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
