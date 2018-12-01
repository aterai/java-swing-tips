package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JSpinner s0 = new JSpinner(makeSpinnerNumberModel());
        JSpinner s1 = makeSpinner1(makeSpinnerNumberModel());
        JSpinner s2 = makeSpinner2(makeSpinnerNumberModel());
        JSpinner s3 = makeSpinner3(makeSpinnerNumberModel());

        JCheckBox cbx = new JCheckBox(new AbstractAction("setEnabled") {
            private Object old;
            @Override public void actionPerformed(ActionEvent e) {
                boolean flg = ((JCheckBox) e.getSource()).isSelected();
                Stream.of(s0, s1, s2, s3).forEach(c -> c.setEnabled(flg));
                if (flg) {
                    Stream.of(s2, s3).forEach(c -> c.setValue(old));
                } else {
                    old = s2.getValue();
                    Stream.of(s2, s3).forEach(c -> c.setValue(Double.NaN));
                }
            }
        });
        cbx.setSelected(true);
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        Stream.of(s0, s1, s2, s3).forEach(c -> {
            c.setEnabled(true);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            Dimension d = c.getPreferredSize();
            d.width = Integer.MAX_VALUE;
            c.setMaximumSize(d);
            box.add(c);
            box.add(Box.createVerticalStrut(5));
        });
        box.add(Box.createVerticalGlue());
        add(cbx, BorderLayout.NORTH);
        add(box);
        setPreferredSize(new Dimension(320, 240));
    }
    private static SpinnerNumberModel makeSpinnerNumberModel() {
        return new SpinnerNumberModel(Double.valueOf(10), Double.valueOf(0), Double.valueOf(100), Double.valueOf(1));
    }
    private static JSpinner makeSpinner1(SpinnerNumberModel m) {
        JSpinner s = new JSpinner(m);
        JFormattedTextField ftf = getJFormattedTextField(s);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        ftf.setFormatterFactory(makeFFactory(dfs));
        ftf.setDisabledTextColor(UIManager.getColor("TextField.disabledColor"));
        return s;
    }
    private static JSpinner makeSpinner2(SpinnerNumberModel m) {
        JSpinner s = new JSpinner(m);
        JFormattedTextField ftf = getJFormattedTextField(s);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setNaN(" ");
        ftf.setFormatterFactory(makeFFactory(dfs));
        return s;
    }
    private static JSpinner makeSpinner3(SpinnerNumberModel m) {
        JSpinner s = new JSpinner(m);
        JFormattedTextField ftf = getJFormattedTextField(s);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setNaN("----");
        ftf.setFormatterFactory(makeFFactory(dfs));
        return s;
    }
    private static JFormattedTextField getJFormattedTextField(JSpinner s) {
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) s.getEditor();
        JFormattedTextField ftf = (JFormattedTextField) editor.getTextField();
        ftf.setColumns(8);
        return ftf;
    }
    private static DefaultFormatterFactory makeFFactory(DecimalFormatSymbols dfs) {
        DecimalFormat format = new DecimalFormat("0.00", dfs);

        NumberFormatter displayFormatter = new NumberFormatter(format);
        displayFormatter.setValueClass(Double.class);

        NumberFormatter editFormatter = new NumberFormatter(format);
        editFormatter.setValueClass(Double.class);
        return new DefaultFormatterFactory(displayFormatter, displayFormatter, editFormatter);
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
