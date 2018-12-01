package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.text.ParseException;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.MaskFormatter;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String mask = "###-####";

        MaskFormatter formatter0 = createFormatter(mask);
        JFormattedTextField field0 = new JFormattedTextField(formatter0);
        box.add(makeTitledPanel("new MaskFormatter(\"###-####\")", field0));
        box.add(Box.createVerticalStrut(15));

        MaskFormatter formatter1 = createFormatter(mask);
        formatter1.setPlaceholderCharacter('_');
        JFormattedTextField field1 = new JFormattedTextField(formatter1);

        MaskFormatter formatter2 = createFormatter(mask);
        formatter2.setPlaceholderCharacter('_');
        formatter2.setPlaceholder("000-0000");
        JFormattedTextField field2 = new JFormattedTextField(formatter2);
        box.add(makeTitledPanel("MaskFormatter#setPlaceholderCharacter('_')", field1));
        box.add(Box.createVerticalStrut(15));

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);
        Insets insets = new Insets(1, 1 + 18 / 2, 1, 1);
        Stream.of(field0, field1, field2).forEach(tf -> {
            tf.setFont(font);
            tf.setColumns(mask.length() + 1);
            tf.setMargin(insets);
        });
        box.add(makeTitledPanel("MaskFormatter#setPlaceholder(\"000-0000\")", field2));
        box.add(Box.createVerticalGlue());

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (ParseException ex) {
            System.err.println("formatter is bad: " + ex.getMessage());
        }
        return formatter;
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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
