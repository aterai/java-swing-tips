package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 1));
        JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(0, 0, 1, .01));
        JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(spinner1, "0%");
        //editor1.getTextField().setEditable(false);
        spinner1.setEditor(editor1);

        JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(0, 0, 1, .01));
        JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spinner2, "0%");
        editor2.getTextField().setEditable(false);
        editor2.getTextField().setBackground(UIManager.getColor("FormattedTextField.background"));
        spinner2.setEditor(editor2);

        add(makeTitlePanel(spinner1, "JSpinner"));
        add(makeTitlePanel(spinner2, "getTextField().setEditable(false)"));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 200));
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
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
