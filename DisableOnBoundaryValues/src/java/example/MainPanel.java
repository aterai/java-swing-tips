package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        boolean flg = UIManager.getLookAndFeelDefaults().getBoolean("Spinner.disableOnBoundaryValues");
        System.out.println(flg);

        // UIManager.put("Spinner.disableOnBoundaryValues", Boolean.TRUE);
        SpinnerModel model = new SpinnerNumberModel(0, 0, 10, 1);
        JSpinner spinner1 = new JSpinner(model);
        spinner1.setFont(spinner1.getFont().deriveFont(32f));
        JSpinner spinner2 = new JSpinner(model);
        spinner2.setFont(spinner2.getFont().deriveFont(32f));

        JCheckBox check = new JCheckBox("Spinner.disableOnBoundaryValues", flg);
        check.addActionListener(e -> {
            UIManager.put("Spinner.disableOnBoundaryValues", ((JCheckBox) e.getSource()).isSelected());
            // TEST:
            // System.out.println(sun.swing.DefaultLookup.get(spinner2, spinner2.getUI(), "Spinner.disableOnBoundaryValues"));
            // System.out.println(UIManager.get("Spinner.disableOnBoundaryValues", spinner2.getLocale()));
            SwingUtilities.updateComponentTreeUI(spinner2);
        });

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(spinner2);
        p.add(check);

        Box box = Box.createVerticalBox();
        box.add(makeTitledPanel("default", spinner1));
        box.add(Box.createVerticalStrut(15));
        box.add(makeTitledPanel("JSpinner disableOnBoundaryValues", p));

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
