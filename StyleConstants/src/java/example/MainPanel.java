package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JTextPane jtp = new JTextPane();
    private final JButton ok  = new JButton("Test");
    private final JButton err = new JButton("Error");
    private final JButton clr = new JButton("Clear");

    public MainPanel() {
        super(new BorderLayout(5,5));
        jtp.setEditable(false);
        ok.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                append("Test test test test", true);
            }
        });
        err.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                append("Error error error error", false);
            }
        });
        clr.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                jtp.setText("");
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(ok);
        box.add(err);
        box.add(Box.createHorizontalStrut(5));
        box.add(clr);

        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getViewport().add(jtp);

        add(scroll);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }

    private static final String SEPARATOR = "\n";
    private void append(final String str, final boolean flg) {
        SimpleAttributeSet sas = null;
        if(!flg) {
            //sas = new SimpleAttributeSet(jtp.getCharacterAttributes());
            sas = new SimpleAttributeSet();
            StyleConstants.setForeground(sas, Color.RED);
            //StyleConstants.setBold(sas, true);
            //StyleConstants.setFontFamily(sas, "Monospaced");
            //StyleConstants.setFontSize(sas, 32);
            //StyleConstants.setForeground(sas, Color.GREEN);
        }
        try{
            Document doc = jtp.getDocument();
            doc.insertString(doc.getLength(), str+SEPARATOR, sas);
            jtp.setCaretPosition(doc.getLength());
        }catch(BadLocationException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
