package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    private final JCheckBox check = new JCheckBox("Palette", true);
    public MainPanel() {
        super(new BorderLayout());

        JInternalFrame palette = new JInternalFrame("Palette", true, false, true, true);
        palette.setBounds(0, 0, 120, 120);
        palette.setMinimumSize(new Dimension(50, 50));
        palette.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        palette.add(new JScrollPane(new JTree()));
        palette.setVisible(true);

        desktop.add(palette, JDesktopPane.PALETTE_LAYER);

        desktop.add(createFrame(0));
        desktop.add(createFrame(1));
        desktop.add(createFrame(2));

        check.addActionListener(e -> {
            Object c = e.getSource();
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                palette.setVisible(b.isSelected());
            }
        });

        add(check, BorderLayout.NORTH);
        add(desktop);
        setPreferredSize(new Dimension(320, 240));
    }
    private JInternalFrame createFrame(int i) {
        JInternalFrame f = new JInternalFrame("title: " + i, true, true, true, true);
        f.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        f.setSize(160, 120);
        f.setVisible(true);
        f.setLocation(100 + 20 * i, 10 + 20 * i);
        return f;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        // try {
        //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        // } catch (ClassNotFoundException | InstantiationException
        //        | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        //     ex.printStackTrace();
        // }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
