// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextComponent field1 = new JPasswordField();
    Box b = Box.createHorizontalBox();
    b.add(new JLabel("Password: "));
    b.add(field1);
    b.add(Box.createHorizontalGlue());

    WatermarkPasswordField field2 = new WatermarkPasswordField();
    field2.getDocument().addDocumentListener(field2);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    box.add(makeTitledPanel("JPasswordField", b));
    box.add(Box.createVerticalStrut(16));
    box.add(makeTitledPanel("InputHintPasswordField", field2));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class WatermarkPasswordField extends JPasswordField implements DocumentListener {
  private boolean showWatermark = true;
  private transient FocusListener listener;

  @Override public void updateUI() {
    removeFocusListener(listener);
    super.updateUI();
    listener = new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        repaint();
      }

      @Override public void focusLost(FocusEvent e) {
        update();
      }
    };
    addFocusListener(listener);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (showWatermark) {
      Graphics2D g2 = (Graphics2D) g.create();
      Insets i = getInsets();
      Font font = getFont();
      FontRenderContext frc = g2.getFontRenderContext();
      TextLayout tl = new TextLayout("Password", font, frc);
      g2.setPaint(hasFocus() ? Color.GRAY : Color.BLACK);
      int baseline = getBaseline(getWidth(), getHeight());
      tl.draw(g2, i.left + 1f, baseline);
      g2.dispose();
    }
  }

  @Override public void insertUpdate(DocumentEvent e) {
    update();
  }

  @Override public void removeUpdate(DocumentEvent e) {
    update();
  }

  @Override public void changedUpdate(DocumentEvent e) {
    /* not needed */
  }

  protected void update() {
    // update(DocumentEvent e) {
    //   showWatermark = e.getDocument().getLength() == 0;
    showWatermark = getPassword().length == 0;
    repaint();
  }
}
