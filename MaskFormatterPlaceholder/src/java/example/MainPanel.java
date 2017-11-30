package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JFormattedTextField field0 = new JFormattedTextField();
    private final JFormattedTextField field1 = new JFormattedTextField();
    private final JFormattedTextField field2 = new JFormattedTextField();

    public MainPanel() {
        super(new BorderLayout());

        String mask = "###-####";
        try {
            MaskFormatter formatter0 = new MaskFormatter(mask);
            field0.setFormatterFactory(new DefaultFormatterFactory(formatter0));

            MaskFormatter formatter1 = new MaskFormatter(mask);
            formatter1.setPlaceholderCharacter('_');
            field1.setFormatterFactory(new DefaultFormatterFactory(formatter1));

            MaskFormatter formatter2 = new MaskFormatter(mask);
            formatter2.setPlaceholderCharacter('_');
            formatter2.setPlaceholder("000-0000");
            field2.setFormatterFactory(new DefaultFormatterFactory(formatter2));
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);
        for (JTextField f: Arrays.asList(field0, field1, field2)) {
            f.setFont(font);
            f.setColumns(8);
        }

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        box.add(makeTitledPanel("new MaskFormatter(\"###-####\")", field0));
        box.add(Box.createVerticalStrut(15));
        box.add(makeTitledPanel("MaskFormatter#setPlaceholderCharacter('_')", field1));
        box.add(Box.createVerticalStrut(15));
        box.add(makeTitledPanel("MaskFormatter#setPlaceholder(\"000-0000\")", field2));
        // box.add(Box.createVerticalGlue());

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
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
