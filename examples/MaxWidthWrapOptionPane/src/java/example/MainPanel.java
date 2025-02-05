// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import javax.swing.*;
import javax.swing.text.BadLocationException;

public final class MainPanel extends JPanel {
  // https://stackoverflow.com/questions/35405672/use-width-and-max-width-to-wrap-text-in-joptionpane
  private final JTextArea textArea = new JTextArea(1, 1) {
    @Override public void updateUI() {
      super.updateUI();
      setLineWrap(true);
      setWrapStyleWord(true);
      setEditable(false);
      setOpaque(false);
      // setBorder(BorderFactory.createLineBorder(Color.RED));
      setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    @Override public void setText(String t) {
      super.setText(t);
      try {
        // System.out.println("fw: " + getColumnWidth());
        // int cc = Math.round(300f / getColumnWidth());
        // setColumns(cc);
        // System.out.format("Columns: %d%n", cc);

        setColumns(50);
        // https://docs.oracle.com/javase/8/docs/api/javax/swing/text/JTextComponent.html#modelToView-int-
        // i.e. layout cannot be computed until the component has been sized.
        // The component does not have to be visible or painted.
        setSize(super.getPreferredSize()); // setSize: looks like ugly hack...
        // System.out.println(super.getPreferredSize());

        Rectangle r = modelToView(t.length());
        // Java 9: Rectangle rect = modelToView2D(t.length()).getBounds();
        int rc = Math.round((float) r.getMaxY() / getRowHeight());
        setRows(rc);
        // System.out.format("Rows: %d%n", rc);
        // System.out.println(super.getPreferredSize());
        boolean isOnlyOneColumn = rc == 1;
        if (isOnlyOneColumn) {
          setSize(getPreferredSize());
          setColumns(1);
        }
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    }
  };

  private MainPanel() {
    super();
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    String txt = "This is a long error message. 1, 22, 333, 4444, 55555.";
    String msgLong = String.join(" ", Collections.nCopies(10, txt));
    JButton longButton = new JButton("JOptionPane: long");
    longButton.addActionListener(e -> {
      textArea.setText(msgLong);
      JOptionPane.showMessageDialog(getRootPane(), scroll, "Error", JOptionPane.ERROR_MESSAGE);
    });

    String msgShort = "This is a short error message.";
    JButton shortButton = new JButton("JOptionPane: short");
    shortButton.addActionListener(e -> {
      textArea.setText(msgShort);
      JOptionPane.showMessageDialog(getRootPane(), scroll, "Error", JOptionPane.ERROR_MESSAGE);
    });

    add(longButton);
    add(shortButton);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
