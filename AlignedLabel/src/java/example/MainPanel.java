package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        AlignedLabel fileNameLabel = new AlignedLabel("File Name:");
        AlignedLabel filesOfTypeLabel = new AlignedLabel("Files of Type:");
        AlignedLabel hostLabel = new AlignedLabel("Host:");
        AlignedLabel portLabel = new AlignedLabel("Port:");
        AlignedLabel userLabel = new AlignedLabel("User Name:");
        AlignedLabel passwordLabel = new AlignedLabel("Password:");
        AlignedLabel.groupLabels(Arrays.asList(fileNameLabel, filesOfTypeLabel, hostLabel, portLabel, userLabel, passwordLabel));

        Border innerBorder = BorderFactory.createEmptyBorder(5, 2, 5, 5);

        Box box1 = Box.createVerticalBox();
        TitledBorder border1 = BorderFactory.createTitledBorder("FileChooser");
        border1.setTitlePosition(TitledBorder.ABOVE_TOP);
        box1.setBorder(BorderFactory.createCompoundBorder(border1, innerBorder));
        box1.add(makeLabeledBox(fileNameLabel, new JTextField()));
        box1.add(Box.createVerticalStrut(5));
        box1.add(makeLabeledBox(filesOfTypeLabel, new JComboBox<String>()));

        Box box2 = Box.createVerticalBox();
        TitledBorder border2 = BorderFactory.createTitledBorder("HTTP Proxy");
        border2.setTitlePosition(TitledBorder.ABOVE_TOP);
        box2.setBorder(BorderFactory.createCompoundBorder(border2, innerBorder));
        box2.add(makeLabeledBox(hostLabel, new JTextField()));
        box2.add(Box.createVerticalStrut(5));
        box2.add(makeLabeledBox(portLabel, new JTextField()));
        box2.add(Box.createVerticalStrut(5));
        box2.add(makeLabeledBox(userLabel, new JTextField()));
        box2.add(Box.createVerticalStrut(5));
        box2.add(makeLabeledBox(passwordLabel, new JPasswordField()));

        Box box = Box.createVerticalBox();
        box.add(box1);
        box.add(Box.createVerticalStrut(10));
        box.add(box2);

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Box makeLabeledBox(Component label, Component c) {
        Box box = Box.createHorizontalBox();
        box.add(label);
        box.add(Box.createHorizontalStrut(5));
        box.add(c);
        return box;
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

// @see javax/swing/plaf/metal/MetalFileChooserUI.java
class AlignedLabel extends JLabel {
    private static final int INDENT = 10;
    // private AlignedLabel[] group;
    protected List<AlignedLabel> group;
    protected int maxWidth;

    protected AlignedLabel(String text) {
        super(text);
        // setAlignmentX(JComponent.LEFT_ALIGNMENT);
        setHorizontalAlignment(SwingConstants.RIGHT);
    }
    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        // Align the width with all other labels in group.
        return new Dimension(getMaxWidth() + INDENT, d.height);
    }
    private int getMaxWidth() {
        if (maxWidth == 0 && group != null) {
            // int max = 0;
            // for (AlignedLabel al: group) {
            //    max = Math.max(al.getSuperPreferredWidth(), max);
            // }
            // for (AlignedLabel al: group) {
            //     al.maxWidth = max;
            // }
            int max = group.stream()
                .map(AlignedLabel::getSuperPreferredWidth)
                .reduce(0, Integer::max);
            group.forEach(al -> al.maxWidth = max);
        }
        return maxWidth;
    }
    private int getSuperPreferredWidth() {
        return super.getPreferredSize().width;
    }
    public static void groupLabels(List<AlignedLabel> group) {
        for (AlignedLabel al: group) {
            al.group = group;
        }
    }
}
