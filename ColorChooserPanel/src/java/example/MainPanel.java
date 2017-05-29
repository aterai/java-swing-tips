package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.colorchooser.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout(10, 10));

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "JColorChooser", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("OK"));
        buttonPanel.add(new JButton("Cancel"));
        buttonPanel.add(new JButton("Reset"));

        JCheckBox swatches = new JCheckBox(UIManager.getString("ColorChooser.swatchesNameText", getLocale()));
        JCheckBox hsv = new JCheckBox(UIManager.getString("ColorChooser.hsvNameText", getLocale()));
        JCheckBox hsl = new JCheckBox(UIManager.getString("ColorChooser.hslNameText", getLocale()));
        JCheckBox rgb = new JCheckBox(UIManager.getString("ColorChooser.rgbNameText", getLocale()));
        JCheckBox cmyk = new JCheckBox(UIManager.getString("ColorChooser.cmykNameText", getLocale()));
        List<JCheckBox> list = Arrays.asList(swatches, hsv, hsl, rgb, cmyk);

        JButton button = new JButton("open JColorChooser");
        button.addActionListener(e -> {
            List<String> selected = list.stream().filter(AbstractButton::isSelected).map(AbstractButton::getText).collect(Collectors.toList());
            if (selected.isEmpty()) { // use default JColorChooser
                JColorChooser.showDialog(getRootPane(), "JColorChooser", null);
            } else {
                JColorChooser cc = new JColorChooser();
                for (AbstractColorChooserPanel p: cc.getChooserPanels()) {
                    if (!selected.contains(p.getDisplayName())) {
                        cc.removeChooserPanel(p);
                    }
                }
                dialog.getContentPane().removeAll();
                dialog.getContentPane().add(cc);
                dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
                dialog.pack();
                dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(getRootPane()));
                dialog.setVisible(true);
            }
        });

        Box box = Box.createVerticalBox();
        for (AbstractButton b: list) {
            box.add(b);
            box.add(Box.createVerticalStrut(5));
        }
        add(box, BorderLayout.NORTH);
        add(button, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(320, 240));
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
