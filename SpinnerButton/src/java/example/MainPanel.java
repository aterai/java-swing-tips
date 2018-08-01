package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;

import com.sun.java.swing.plaf.windows.WindowsSpinnerUI;

public final class MainPanel extends JPanel {
    private final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
    private final JSpinner spinner3 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));

    private MainPanel() {
        super(new BorderLayout());

        spinner1.setUI(new MySpinnerUI());
        searchSpinnerButtons(spinner2);
        if (spinner3.getUI() instanceof WindowsSpinnerUI) {
            spinner3.setUI(new MyWinSpinnerUI());
        } else {
            searchSpinnerButtons(spinner3);
        }

        Box box = Box.createVerticalBox();
        box.add(makeTitledPanel("BasicSpinnerUI", spinner1));
        box.add(makeTitledPanel("getName()", spinner2));
        box.add(makeTitledPanel("WindowsSpinnerUI", spinner3));
        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static void searchSpinnerButtons(Container comp) {
        for (Component c: comp.getComponents()) {
            // System.out.println(c.getName());
            if ("Spinner.nextButton".equals(c.getName())) {
                ((JButton) c).setToolTipText("getName: next next");
            } else if ("Spinner.previousButton".equals(c.getName())) {
                ((JButton) c).setToolTipText("getName: prev prev");
            } else if (c instanceof Container) {
                searchSpinnerButtons((Container) c);
            }
        }
    }
    private static class MySpinnerUI extends BasicSpinnerUI {
        @Override protected Component createNextButton() {
            JComponent nextButton = (JComponent) super.createNextButton();
            nextButton.setToolTipText("SpinnerUI: next next");
            // nextButton.setBackground(Color.GREEN);
            return nextButton;
        }
        @Override protected Component createPreviousButton() {
            JComponent previousButton = (JComponent) super.createPreviousButton();
            previousButton.setToolTipText("SpinnerUI: prev prev");
            // previousButton.setBackground(Color.RED);
            return previousButton;
        }
    }
    private static class MyWinSpinnerUI extends WindowsSpinnerUI {
        @Override protected Component createNextButton() {
            JComponent nextButton = (JComponent) super.createNextButton();
            nextButton.setToolTipText("WindowsSpinnerUI: next next");
            return nextButton;
        }
        @Override protected Component createPreviousButton() {
            JComponent previousButton = (JComponent) super.createPreviousButton();
            previousButton.setToolTipText("WindowsSpinnerUI: prev prev");
            return previousButton;
        }
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
