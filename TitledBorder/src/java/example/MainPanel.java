package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
    private final JComboBox<VerticalOrientation> verticalOrientationChoices = new JComboBox<>(VerticalOrientation.values());
    private final JComboBox<Justification> justificationChoices = new JComboBox<>(Justification.values());
    private final TitledBorder border = BorderFactory.createTitledBorder("Test Test");
    private final JPanel panel = new JPanel();

    public MainPanel() {
        super(new BorderLayout(5, 5));
        verticalOrientationChoices.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                initTitledBorder();
            }
        });
        justificationChoices.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                initTitledBorder();
            }
        });
        panel.setBorder(border);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.LINE_END;
        JPanel p2 = new JPanel(new GridBagLayout());
        p2.add(new JLabel("TitlePosition:"), c);
        p2.add(new JLabel("TitleJustification:"), c);

        c.gridx = 1;
        c.weightx = 1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        p2.add(verticalOrientationChoices, c);
        p2.add(justificationChoices, c);

        add(p2, BorderLayout.NORTH);
        add(panel);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private void initTitledBorder() {
        border.setTitlePosition(verticalOrientationChoices.getItemAt(verticalOrientationChoices.getSelectedIndex()).mode);
        border.setTitleJustification(justificationChoices.getItemAt(justificationChoices.getSelectedIndex()).mode);
        panel.repaint();
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

enum VerticalOrientation {
    DEFAULT_POSITION(TitledBorder.DEFAULT_POSITION, "Default Position"),
    ABOVE_TOP(TitledBorder.ABOVE_TOP,               "Above Top"),
    TOP(TitledBorder.TOP,                           "Top"),
    BELOW_TOP(TitledBorder.BELOW_TOP,               "Below Top"),
    ABOVE_BOTTOM(TitledBorder.ABOVE_BOTTOM,         "Above Bottom"),
    BOTTOM(TitledBorder.BOTTOM,                     "Bottom"),
    BELOW_BOTTOM(TitledBorder.BELOW_BOTTOM,         "Below Bottom");
    public final int mode;
    private final String description;
    VerticalOrientation(int mode, String description) {
        this.mode = mode;
        this.description = description;
    }
    @Override public String toString() {
        return description;
    }
}

enum Justification {
    DEFAULT_JUSTIFICATION(TitledBorder.DEFAULT_JUSTIFICATION, "Default Justification"),
    LEFT(TitledBorder.LEFT,                                   "Left"),
    CENTER(TitledBorder.CENTER,                               "Center"),
    RIGHT(TitledBorder.RIGHT,                                 "Right"),
    LEADING(TitledBorder.LEADING,                             "Leading"),
    TRAILING(TitledBorder.TRAILING,                           "Trailing");
    public final int mode;
    private final String description;
    Justification(int mode, String description) {
        this.mode = mode;
        this.description = description;
    }
    @Override public String toString() {
        return description;
    }
}
