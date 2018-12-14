// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String TEXT = "The quick brown fox jumps over the lazy dog.";

  private MainPanel() {
    super(new BorderLayout());

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
    box.add(makeExpandingTextArea1());
    box.add(Box.createVerticalStrut(10));
    box.add(makeExpandingTextArea2());

    add(box, BorderLayout.NORTH);
    add(new JButton("focus dummy"), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeExpandingTextArea1() {
    JPanel p = new JPanel(new BorderLayout());
    JTextArea textArea = new JTextArea(TEXT, 1, 10);
    textArea.setLineWrap(true);
    textArea.addFocusListener(new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        JTextArea ta = (JTextArea) e.getComponent();
        ta.setRows(3);
        p.revalidate();
      }

      @Override public void focusLost(FocusEvent e) {
        JTextArea ta = (JTextArea) e.getComponent();
        ta.setRows(1);
        p.revalidate();
      }
    });
    JScrollPane scroll = new JScrollPane(textArea);
    // scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    p.add(scroll, BorderLayout.NORTH);
    return p;
  }

  private static Component makeExpandingTextArea2() {
    CardLayout cardLayout = new CardLayout();
    JPanel cp = new JPanel(cardLayout);

    JTextArea textArea = new JTextArea(TEXT, 3, 10) {
      @Override public void updateUI() {
        super.updateUI();
        setLineWrap(true);
        setWrapStyleWord(true);
        setMargin(new Insets(1, 1, 1, 1));
      }
    };

    JLabel textField = new JLabel(TEXT) {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(true);
        setFocusable(true);
        setBackground(UIManager.getColor("TextField.background"));
        setForeground(UIManager.getColor("TextField.foreground"));
        setBorder(BorderFactory.createCompoundBorder(
            UIManager.getBorder("TextField.border"), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        setFont(UIManager.getFont("TextArea.font"));
      }
    };

    textArea.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        String text = textArea.getText();
        textField.setText(text.isEmpty() ? " " : text);
        cardLayout.show(cp, "TextField");
      }
    });
    textField.addFocusListener(new FocusAdapter() {
      @Override public void focusGained(FocusEvent e) {
        cardLayout.show(cp, "TextArea");
        textArea.requestFocusInWindow();
      }
    });
    textField.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        cardLayout.show(cp, "TextArea");
        textArea.requestFocusInWindow();
      }
    });
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(textField, BorderLayout.NORTH);
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    cp.add(panel, "TextField");
    cp.add(scroll, "TextArea");
    return cp;
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
