package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private static final String TEXT = "javascript:(function(){var l=location,m=l.href.match('^(https?://)(.+)(api[^+]+|technotes[^+]+)');if(m)l.href=m[1]+'docs.oracle.com/javase/8/docs/'+decodeURIComponent(m[3]).replace(/\\+.*$/,'').replace(/\\[\\]/g,':A').replace(/, |\\(|\\)/g,'-');}());";
    private final JTextField textField = new JTextField(TEXT);
    private final JScrollBar scroller = new JScrollBar(JScrollBar.HORIZONTAL);
    private final EmptyThumbHandler handler = new EmptyThumbHandler(textField, scroller);

    public MainPanel() {
        super(new BorderLayout());

        scroller.setModel(textField.getHorizontalVisibility());

        Box p = Box.createVerticalBox();
        JScrollPane scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setViewportView(new JTextField(TEXT));
        p.add(new JLabel("JScrollPane + VERTICAL_SCROLLBAR_NEVER"));
        p.add(scroll);
        p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("BoundedRangeModel: textField.getHorizontalVisibility()"));
        p.add(textField, BorderLayout.SOUTH);
        p.add(Box.createVerticalStrut(5));
        p.add(scroller);
        p.add(Box.createVerticalStrut(2));
        p.add(new JCheckBox(new AbstractAction("add EmptyThumbHandler") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                if (c.isSelected()) {
                    textField.addComponentListener(handler);
                    textField.getDocument().addDocumentListener(handler);
                } else {
                    textField.removeComponentListener(handler);
                    textField.getDocument().removeDocumentListener(handler);
                }
            }
        }));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("setCaretPosition: 0") {
            @Override public void actionPerformed(ActionEvent e) {
                textField.requestFocusInWindow();
                textField.setCaretPosition(0);
                scroller.revalidate();
            }
        }));
        box.add(Box.createHorizontalStrut(5));
        box.add(new JButton(new AbstractAction("setScrollOffset: 0") {
            @Override public void actionPerformed(ActionEvent e) {
                textField.setScrollOffset(0);
                scroller.revalidate();
            }
        }));

        add(p, BorderLayout.NORTH);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
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

class EmptyThumbHandler extends ComponentAdapter implements DocumentListener {
    private final BoundedRangeModel emptyThumbModel = new DefaultBoundedRangeModel(0, 1, 0, 1);
    private final JTextField textField;
    private final JScrollBar scroller;
    public EmptyThumbHandler(JTextField textField, JScrollBar scroller) {
        super();
        this.textField = textField;
        this.scroller = scroller;
    }
    private void changeThumbModel() {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                BoundedRangeModel m = textField.getHorizontalVisibility();
                int iv = m.getMaximum() - m.getMinimum() - m.getExtent() - 1; // -1: bug?
                if (iv <= 0) {
                    scroller.setModel(emptyThumbModel);
                } else {
                    scroller.setModel(textField.getHorizontalVisibility());
                }
            }
        });
    }
    @Override public void componentResized(ComponentEvent e) {
        changeThumbModel();
    }
    @Override public void insertUpdate(DocumentEvent e) {
        changeThumbModel();
    }
    @Override public void removeUpdate(DocumentEvent e) {
        changeThumbModel();
    }
    @Override public void changedUpdate(DocumentEvent e) {
        changeThumbModel();
    }
}
