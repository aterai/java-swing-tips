package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] items = {
            "<html><font color='red'>Sunday</font> <font color='gray'>(Sun.)",
            "<html><font color='black'>Monday</font> <font color='gray'>(Mon.)",
            "<html><font color='black'>Tuesday</font> <font color='gray'>(Tue.)",
            "<html><font color='black'>Wednesday</font> <font color='gray'>(Wed.)",
            "<html><font color='black'>Thursday</font> <font color='gray'>(Thu.)",
            "<html><font color='black'>Friday</font> <font color='gray'>(Fri.)",
            "<html><font color='blue'>Saturday</font> <font color='gray'>(Sat.)"};

        JPanel p1 = new JPanel(new BorderLayout(5, 5));
        p1.add(new JSpinner(new SpinnerListModel(items)));
        p1.setBorder(BorderFactory.createTitledBorder("JSpinner"));

        JPanel p2 = new JPanel(new BorderLayout(5, 5));
        p2.add(makeColorSpinner(items));
        p2.setBorder(BorderFactory.createTitledBorder("ColorSpinner(JComboBox)"));

        // // TEST:
        // JPanel p3 = new JPanel(new BorderLayout(5, 5));
        // JSpinner spinner = new JSpinner(new SpinnerListModel(items)) {
        //     @Override public void setEditor(JComponent editor) {
        //         JComponent oldEditor = getEditor();
        //         if (!editor.equals(oldEditor) && oldEditor instanceof HTMLListEditor) {
        //             ((HTMLListEditor) oldEditor).dismiss(this);
        //         }
        //         super.setEditor(editor);
        //     }
        // };
        // spinner.setEditor(new HTMLListEditor(spinner));
        // p3.add(spinner);

        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.add(p1, BorderLayout.NORTH);
        panel.add(p2, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeColorSpinner(String... items) {
        UIManager.put("ComboBox.squareButton", Boolean.FALSE);
        JComboBox<String> comboBox = new JComboBox<String>(items) {
            @Override public void updateUI() {
                super.updateUI();
                setUI(new NoPopupComboBoxUI());
                setFocusable(false);
                ListCellRenderer<? super String> r = getRenderer();
                setRenderer(new ListCellRenderer<String>() {
                    @Override public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
                        JComponent c = (JComponent) r.getListCellRendererComponent(list, value, index, false, false);
                        c.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                        return c;
                    }
                });
            }
        };
        JButton nb = createArrowButton(SwingConstants.NORTH);
        nb.addActionListener(e -> {
            e.setSource(comboBox);
            comboBox.getActionMap().get("selectPrevious2").actionPerformed(e);
        });
        JButton sb = createArrowButton(SwingConstants.SOUTH);
        sb.addActionListener(e -> {
            e.setSource(comboBox);
            comboBox.getActionMap().get("selectNext2").actionPerformed(e);
        });
        Box box = Box.createVerticalBox();
        box.add(nb);
        box.add(sb);

        JPanel p = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 20;
                return d;
            }
        };
        p.add(comboBox);
        p.add(box, BorderLayout.EAST);
        return p;
    }
    private static JButton createArrowButton(int direction) {
        return new BasicArrowButton(direction) {
            @Override public void updateUI() {
                super.updateUI();
                Border buttonBorder = UIManager.getBorder("Spinner.arrowButtonBorder");
                if (buttonBorder instanceof UIResource) {
                    // Wrap the border to avoid having the UIResource be replaced by
                    // the ButtonUI. This is the opposite of using BorderUIResource.
                    setBorder(new CompoundBorder(buttonBorder, null));
                } else {
                    setBorder(buttonBorder);
                }
                setInheritsPopupMenu(true);
            }
        };
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

class NoPopupComboBoxUI extends BasicComboBoxUI {
    @Override protected JButton createArrowButton() {
        JButton button = new JButton(); // .createArrowButton();
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setVisible(false);
        return button;
    }
    // @Override public void setPopupVisible(JComboBox c, boolean v) {
    //     System.out.println("setPopupVisible: " + v);
    //     if (v) {
    //         popup.show();
    //     } else {
    //         popup.hide();
    //     }
    // }
    @Override protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
            @Override public void show() {
                System.out.println("togglePopup");
                // super.show();
            }
        };
    }
}

// // TEST:
// class HTMLListEditor extends JLabel implements ChangeListener {
//     @Override public Dimension getPreferredSize() {
//         Dimension d = super.getPreferredSize();
//         d.width = 200;
//         return d;
//     }
//     private final JSpinner spinner;
//
//     protected HTMLListEditor(JSpinner spinner) {
//         super();
//
//         if (!(spinner.getModel() instanceof SpinnerListModel)) {
//             throw new IllegalArgumentException("model not a SpinnerListModel");
//         }
//         this.spinner = spinner;
//         spinner.addChangeListener(this);
//
//         setText(Objects.toString(spinner.getValue()));
//         setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
//         setOpaque(true);
//         setBackground(Color.WHITE);
//         setInheritsPopupMenu(true);
//
//         String toolTipText = spinner.getToolTipText();
//         if (toolTipText != null) {
//             setToolTipText(toolTipText);
//         }
//     }
//     @Override public void stateChanged(ChangeEvent e) {
//         JSpinner spinner = (JSpinner) e.getSource();
//         setText(Objects.toString(spinner.getValue()));
//     }
//     public void dismiss(JSpinner spinner) {
//         spinner.removeChangeListener(this);
//     }
// }
