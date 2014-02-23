package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        final SplitPaneWrapper sp = new SplitPaneWrapper();
        final JCheckBox check = new JCheckBox("MAXIMIZED_BOTH: keep the same splitting ratio");
        check.setSelected(true);
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                sp.setTestFlag(check.isSelected());
            }
        });
        add(check, BorderLayout.NORTH);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
    private final JSplitPane splitPane;
    private final JTextArea log = new JTextArea();
    private boolean flag = true;
    private int prevState = Frame.NORMAL;
//     private final JSplitPane splitPane = new JSplitPane() {
//         @Override public void setDividerLocation(double proportionalLocation) {
//             if (proportionalLocation < 0.0 || proportionalLocation > 1.0) {
//                 throw new IllegalArgumentException("proportional location must " + "be between 0.0 and 1.0.");
//             }
//             int s = ((getOrientation() == VERTICAL_SPLIT) ? getHeight() : getWidth()) - getDividerSize();
//             setDividerLocation((int)Math.round(s * proportionalLocation));
//         }
// //         @Override public void setDividerLocation(double proportionalLocation) {
// //             if (proportionalLocation < 0.0 || proportionalLocation > 1.0) {
// //                 throw new IllegalArgumentException("proportional location must be between 0.0 and 1.0.");
// //             }
// //             if (getOrientation() == VERTICAL_SPLIT) {
// //                 setDividerLocation((int) ((double)(getHeight() - getDividerSize()) *
// //                                          proportionalLocation));
// //             } else {
// //                 setDividerLocation((int) ((double)(getWidth() - getDividerSize()) *
// //                                          proportionalLocation));
// //             }
// //         }
//     };
    public SplitPaneWrapper() {
        super(new BorderLayout());
        this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(log), new JScrollPane(new JTree()));
        add(splitPane);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                splitPane.setDividerLocation(0.5);
            }
        });
    }
    public SplitPaneWrapper(JSplitPane sp) {
        super(new BorderLayout());
        this.splitPane = sp;
        add(sp);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                splitPane.setDividerLocation(0.5);
            }
        });
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
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        int s = getOrientedSize(splitPane);
                        int iv = (int) Math.round(s * proportionalLocation);
                        log.append(String.format("DividerLocation: %d%n", iv));
                        splitPane.setDividerLocation(iv);
                    }
                });
                prevState = state;
            }
        } else {
            super.doLayout();
        }
    }
}
