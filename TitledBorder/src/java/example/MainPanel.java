package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private final JComboBox<? extends Enum> verticalOrientationChoices = new JComboBox<>(VerticalOrientation.values());
    private final JComboBox<? extends Enum> justificationChoices       = new JComboBox<>(Justification.values());
    private final TitledBorder border = BorderFactory.createTitledBorder("Test Test");
    private final JPanel panel = new JPanel();

    public MainPanel() {
        super(new BorderLayout(5, 5));
        verticalOrientationChoices.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    initTitleBorder();
                }
            }
        });
        justificationChoices.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    initTitleBorder();
                }
            }
        });
        panel.setBorder(border);

        JPanel p2 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 5);
        c.anchor  = GridBagConstraints.LINE_END;
        p2.add(new JLabel("TitlePosition:"), c);
        p2.add(new JLabel("TitleJustification:"), c);

        c.gridx   = 1;
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        p2.add(verticalOrientationChoices, c);
        p2.add(justificationChoices, c);

        add(p2, BorderLayout.NORTH);
        add(panel);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private void initTitleBorder() {
        VerticalOrientation vo = (VerticalOrientation) verticalOrientationChoices.getSelectedItem();
        border.setTitlePosition(vo.mode);
        Justification jc = (Justification) justificationChoices.getSelectedItem();
        border.setTitleJustification(jc.mode);
        panel.repaint();
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
