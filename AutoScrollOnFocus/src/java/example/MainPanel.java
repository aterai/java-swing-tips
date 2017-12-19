package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.stream.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2, 5, 5));

        Box box = makeTestBox();
        box.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
            @Override public Component getComponentAfter(Container focusCycleRoot, Component cmp) {
                Component c = super.getComponentAfter(focusCycleRoot, cmp);
                // TEST1: box.scrollRectToVisible(c.getBounds());
                // TEST2:
                // Optional.ofNullable(SwingUtilities.getAncestorOfClass(JViewport.class, focusCycleRoot)).ifPresent(v -> {
                //     Component vc = SwingUtilities.getUnwrappedView((JViewport) v);
                //     if (vc instanceof JComponent) {
                //         ((JComponent) vc).scrollRectToVisible(c.getBounds());
                //     }
                // });
                if (focusCycleRoot instanceof JComponent) {
                    ((JComponent) focusCycleRoot).scrollRectToVisible(c.getBounds());
                }
                return c;
            }
            @Override public Component getComponentBefore(Container focusCycleRoot, Component cmp) {
                Component c = super.getComponentBefore(focusCycleRoot, cmp);
                if (focusCycleRoot instanceof JComponent) {
                    ((JComponent) focusCycleRoot).scrollRectToVisible(c.getBounds());
                }
                return c;
            }
        });
        add(new JScrollPane(makeTestBox()));
        add(new JScrollPane(box));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Box makeTestBox() {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        IntStream.range(0, 20).forEach(i -> {
            box.add(new JTextField("test" + i));
            box.add(Box.createVerticalStrut(5));
        });
        box.add(Box.createVerticalGlue());
        box.setFocusCycleRoot(true);
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
