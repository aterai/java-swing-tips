// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(makeStyleSheet());
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);
    editor.setSelectedTextColor(null);
    editor.setSelectionColor(new Color(0x64_88_AA_AA, true));
    editor.setBackground(new Color(0x64_64_64)); // 0x33_33_33
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Optional.ofNullable(cl.getResource("example/test.html"))
        .ifPresent(url -> {
          try {
            editor.setPage(url);
          } catch (IOException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(editor);
            editor.setText(ex.getMessage());
          }
        });

    JScrollPane scroll = new JScrollPane(editor) {
      @Override public void updateUI() {
        super.updateUI();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    };
    JCheckBox check = new JCheckBox("HORIZONTAL_SCROLLBAR_NEVER", true);
    check.addActionListener(e -> {
      boolean f = ((JCheckBox) e.getSource()).isSelected();
      int p = f ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
          : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
      scroll.setHorizontalScrollBarPolicy(p);
    });

    JPanel box = new JPanel();
    box.add(check);

    add(new JLayer<>(scroll, new ScrollPaneLayerUI()));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static StyleSheet makeStyleSheet() {
    // https://github.com/google/code-prettify/blob/master/styles/desert.css
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str{color:#ffa0a0}");
    styleSheet.addRule(".kwd{color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".com{color:#87ceeb}");
    styleSheet.addRule(".typ{color:#98fb98}");
    styleSheet.addRule(".lit{color:#cd5c5c}");
    styleSheet.addRule(".pun{color:#ffffff}");
    styleSheet.addRule(".pln{color:#ffffff}");
    styleSheet.addRule(".tag{color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".atn{color:#bdb76b;font-weight:bold}");
    styleSheet.addRule(".atv{color:#ffa0a0}");
    styleSheet.addRule(".dec{color:#98fb98}");
    return styleSheet;
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
      Logger.getGlobal().severe(ex::getMessage);
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

class ScrollPaneLayerUI extends LayerUI<JScrollPane> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle rect = scroll.getViewportBorderBounds();
      BoundedRangeModel m = scroll.getHorizontalScrollBar().getModel();
      int extent = m.getExtent();
      int maximum = m.getMaximum();
      int value = m.getValue();
      if (value + extent < maximum) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new Color(0x08_00_00_00, true));
        int shd = 6;
        int w = rect.width;
        int h = rect.height;
        g2.translate(rect.x + w - shd, rect.y);
        for (int i = 0; i < shd; i++) {
          g2.fillRect(i, 0, shd - i, h);
        }
        // g2.setPaint(Color.RED);
        g2.fillRect(shd - 2, 0, 2, h); // Make the edge a bit darker
        g2.dispose();
      }
    }
  }
}

