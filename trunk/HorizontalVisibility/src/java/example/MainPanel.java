package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final String TEXT = "javascript:(function(){var l=location,m=l.href.match('^(https?://)(.+)(api[^+]+|technotes[^+]+)');if(m)l.href=m[1]+'docs.oracle.com/javase/8/docs/'+decodeURIComponent(m[3]).replace(/\\+.*$/,'').replace(/\\[\\]/g,':A').replace(/, |\\(|\\)/g,'-');}());";
    private final JTextField textField = new JTextField(TEXT);
    private final JScrollBar scroller = new JScrollBar(JScrollBar.HORIZONTAL);
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

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(Box.createHorizontalStrut(5));
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
