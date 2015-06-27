package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JSpinner s0 = new JSpinner(makeSpinnerNumberModel());
    private final JSpinner s1 = makeSpinner1(makeSpinnerNumberModel());
    private final JSpinner s2 = makeSpinner2(makeSpinnerNumberModel());
    private final JSpinner s3 = makeSpinner3(makeSpinnerNumberModel());

    public MainPanel() {
        super(new BorderLayout());
        JCheckBox cbx = new JCheckBox(new AbstractAction("setEnabled") {
            Object old;
            @Override public void actionPerformed(ActionEvent e) {
                boolean flg = ((JCheckBox) e.getSource()).isSelected();
                for (JSpinner c: Arrays.asList(s0, s1, s2, s3)) {
                    c.setEnabled(flg);
                }
                if (flg) {
                    for (JSpinner c: Arrays.asList(s2, s3)) {
                        c.setValue(old);
                    }
                } else {
                    old = s2.getValue();
                    for (JSpinner c: Arrays.asList(s2, s3)) {
                        c.setValue(Double.NaN);
                    }
                }
            }
        });
        cbx.setSelected(true);
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        for (JComponent c: Arrays.asList(s0, s1, s2, s3)) {
            c.setEnabled(true);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            int h = c.getPreferredSize().height;
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            box.add(c);
            box.add(Box.createVerticalStrut(5));
        }
        box.add(Box.createVerticalGlue());
        add(cbx, BorderLayout.NORTH);
        add(box);
        setPreferredSize(new Dimension(320, 240));
    }
    private static SpinnerNumberModel makeSpinnerNumberModel() {
        return new SpinnerNumberModel(
            Double.valueOf(10), Double.valueOf(0), Double.valueOf(100),
            Double.valueOf(1));
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
        ((NumberFormatter) displayFormatter).setValueClass(Double.class);
        NumberFormatter editFormatter = new NumberFormatter(format);
        ((NumberFormatter) editFormatter).setValueClass(Double.class);
        DefaultFormatterFactory dff = new DefaultFormatterFactory(
            displayFormatter, displayFormatter, editFormatter);
        return dff;
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
