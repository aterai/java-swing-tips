package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    //http://stackoverflow.com/questions/35405672/use-width-and-max-width-to-wrap-text-in-joptionpane
    private final JTextArea textArea = new JTextArea(1, 1) {
        @Override public void updateUI() {
            super.updateUI();
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
            setOpaque(false);
            //setBorder(BorderFactory.createLineBorder(Color.RED));
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        }
        @Override public void setText(String t) {
            super.setText(t);
            try {
//                 System.out.println("fw: " + getColumnWidth());
//                 int cc = (int) (.5 + 300 / (float) getColumnWidth());
//                 setColumns(cc);
//                 System.out.format("Columns: %d%n", cc);

                setColumns(50);
                // http://docs.oracle.com/javase/8/docs/api/javax/swing/text/JTextComponent.html#modelToView-int-
                // i.e. layout cannot be computed until the component has been sized.
                // The component does not have to be visible or painted.
                setSize(super.getPreferredSize()); //setSize: looks like ugly hack...
                System.out.println(super.getPreferredSize());

                Rectangle r = modelToView(t.length());
                int rc = (int) (.5 + (r.y + r.height) / (float) getRowHeight());
                setRows(rc);
                System.out.format("Rows: %d%n", rc);
                System.out.println(super.getPreferredSize());
                if (rc == 1) {
                    setSize(getPreferredSize());
                    setColumns(1);
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    };
    private final JScrollPane scroll = new JScrollPane(textArea);

    public MainPanel() {
        super();

        String msgShort = "This is a short error message.";
        String msgLong = String.join(" ", Collections.nCopies(10, "This is a long error message. 1, 22, 333, 4444, 55555."));

        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        add(new JButton(new AbstractAction("JOptionPane: long") {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.setText(msgLong);
                JOptionPane.showMessageDialog(getRootPane(), scroll, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }));
        add(new JButton(new AbstractAction("JOptionPane: short") {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.setText(msgShort);
                JOptionPane.showMessageDialog(getRootPane(), scroll, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }));
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
