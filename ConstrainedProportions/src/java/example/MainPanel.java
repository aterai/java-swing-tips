package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final int MW = 300;
    private static final int MH = 200;
    private final JCheckBox checkbox = new JCheckBox("Fixed aspect ratio, Minimum size: " + MW + "*" + MH);

    private MainPanel() {
        super(new BorderLayout());
        EventQueue.invokeLater(() -> {
            Container c = getTopLevelAncestor();
            if (c instanceof JFrame) {
                JFrame frame = (JFrame) c;
                frame.setMinimumSize(new Dimension(MW, MH)); //JDK 1.6.0
                frame.addComponentListener(new ComponentAdapter() {
                    @Override public void componentResized(ComponentEvent e) {
                        initFrameSize(frame);
                    }
                });
            }
        });

        checkbox.addActionListener(e -> {
            Container c = getTopLevelAncestor();
            if (c instanceof JFrame) {
                initFrameSize((JFrame) c);
            }
        });
        //checkbox.setSelected(true);

        JLabel label = new JLabel();
        label.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                JLabel l = (JLabel) e.getComponent();
                Container c = getTopLevelAncestor();
                if (c instanceof JFrame) {
                    l.setText(c.getSize().toString());
                }
            }
        });

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(checkbox);
        Toolkit.getDefaultToolkit().setDynamicLayout(false);
        p.add(new JCheckBox(new AbstractAction("Toolkit.getDefaultToolkit().setDynamicLayout: ") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                Toolkit.getDefaultToolkit().setDynamicLayout(cb.isSelected());
            }
        }));
        add(p, BorderLayout.NORTH);
        add(label);
        setPreferredSize(new Dimension(320, 240));
    }
    protected void initFrameSize(JFrame frame) {
        if (!checkbox.isSelected()) {
            return;
        }
        int fw = frame.getSize().width;
        int fh = MH * fw / MW;
        frame.setSize(Math.max(MW, fw), Math.max(MH, fh));
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
