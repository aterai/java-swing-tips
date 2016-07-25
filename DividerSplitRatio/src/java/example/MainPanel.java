package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("MAXIMIZED_BOTH: keep the same splitting ratio", true);
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(new JTextArea()), new JScrollPane(new JTree()));
    private final SplitPaneWrapper spw = new SplitPaneWrapper(splitPane);
    public MainPanel() {
        super(new BorderLayout());
        check.addActionListener(e -> spw.setTestFlag(check.isSelected()));
        add(check, BorderLayout.NORTH);
        add(spw);
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

class SplitPaneWrapper extends JPanel {
    private boolean flag = true;
    private int prevState = Frame.NORMAL;
    private final JSplitPane splitPane;

    protected SplitPaneWrapper(JSplitPane splitPane) {
        super(new BorderLayout());
        this.splitPane = splitPane;
        add(splitPane);
        EventQueue.invokeLater(() -> splitPane.setDividerLocation(.5));
    }
    public void setTestFlag(boolean flag) {
        this.flag = flag;
    }
    private static int getOrientedSize(JSplitPane sp) {
        return (sp.getOrientation() == JSplitPane.VERTICAL_SPLIT)
            ? sp.getHeight() - sp.getDividerSize()
            : sp.getWidth()  - sp.getDividerSize();
    }
    @Override public void doLayout() {
        if (flag) {
            int size = getOrientedSize(splitPane);
            final double proportionalLocation = splitPane.getDividerLocation() / (double) size;
            super.doLayout();
            int state = ((Frame) SwingUtilities.getWindowAncestor(splitPane)).getExtendedState();
            if (splitPane.isShowing() && state != prevState) {
                EventQueue.invokeLater(() -> {
                    int s = getOrientedSize(splitPane);
                    int iv = (int) Math.round(s * proportionalLocation);
                    System.out.format("DividerLocation: %d%n", iv);
                    splitPane.setDividerLocation(iv);
                });
                prevState = state;
            }
        } else {
            super.doLayout();
        }
    }
}
