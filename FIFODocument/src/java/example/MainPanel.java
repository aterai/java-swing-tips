package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

public class MainPanel extends JPanel{
    private static final int MAX_LINES = 10;
    private final JTextArea jta = new JTextArea();
    private final JButton start = new JButton("Start");
    private final JButton stop  = new JButton("Stop");
    private final JButton clr   = new JButton("Clear");

    public MainPanel() {
        super(new BorderLayout());
        jta.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                final Document doc = jta.getDocument();
                final Element root = doc.getDefaultRootElement();
                if(root.getElementCount()<=MAX_LINES) return;
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        removeLines(doc, root);
                    }
                });
                jta.setCaretPosition(doc.getLength());
            }
            private void removeLines(Document doc, Element root) {
                Element fl = root.getElement(0);
                try{
                    doc.remove(0, fl.getEndOffset());
                }catch(BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
            @Override public void removeUpdate(DocumentEvent e) {}
            @Override public void changedUpdate(DocumentEvent e) {}
        });
        jta.setEditable(false);
        final Timer timer = new Timer(200, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                String s = new Date().toString();
                jta.append(jta.getDocument().getLength()>0 ? "\n"+s : s);
            }
        });
        jta.addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && !jta.isDisplayable()) {
                    timer.stop();
                }
            }
        });

        start.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                timer.start();
            }
        });
        stop.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                timer.stop();
            }
        });
        clr.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                jta.setText("");
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(start);
        box.add(stop);
        box.add(Box.createHorizontalStrut(5));
        box.add(clr);

        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getViewport().add(jta);

        add(scroll);
        add(box, BorderLayout.SOUTH);
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
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
