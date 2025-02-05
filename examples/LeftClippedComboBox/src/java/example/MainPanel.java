// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.metal.MetalComboBoxUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box p = Box.createVerticalBox();
    ComboBoxModel<String> m = makeComboBoxModel();
    p.add(makeTitledPanel("Left clipped", new LeftClippedComboBox<>(m)));
    p.add(Box.createVerticalStrut(5));
    p.add(makeTitledPanel("Default", new JComboBox<>(m)));
    add(p, BorderLayout.NORTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder(title));
    box.add(Box.createVerticalStrut(2));
    box.add(c);
    return box;
  }

  private static ComboBoxModel<String> makeComboBoxModel() {
    String str = String.join("/", Collections.nCopies(5, "12345678901234567890"));
    DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
    m.addElement(str + ".jpg");
    m.addElement("aaa.tif");
    m.addElement("\\1234567890\\1234567890\\1234567890.avi");
    m.addElement("1234567890.pdf");
    m.addElement("c:/" + str + ".mpg");
    m.addElement("https://localhost/" + str + ".jpg");
    return m;
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

class LeftClippedComboBox<E> extends JComboBox<E> {
  protected LeftClippedComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setRenderer(null);
    super.updateUI();
    setRenderer(makeComboBoxRenderer(this));
  }

  private DefaultListCellRenderer makeComboBoxRenderer(JComboBox<?> combo) {
    return new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus);
        if (c instanceof JLabel) {
          String s = Objects.toString(value, "");
          FontMetrics fm = c.getFontMetrics(c.getFont());
          int w = getAvailableWidth(combo, index);
          ((JLabel) c).setText(fm.stringWidth(s) <= w ? s : getLeftClippedText(s, fm, w));
        }
        return c;
      }

      private int getAvailableWidth(JComboBox<?> combo, int index) {
        Optional<JButton> arrowButtonOp = descendants(combo)
            .filter(JButton.class::isInstance)
            .map(JButton.class::cast)
            .findFirst();
        Insets rendererIns = getInsets();
        Rectangle r = SwingUtilities.calculateInnerArea(combo, null);
        int availableWidth = r.width - rendererIns.left - rendererIns.right;
        availableWidth = getLookAndFeelDependWidth(combo, availableWidth);
        if (index < 0) {
          // @see BasicComboBoxUI#rectangleForCurrentValue
          int buttonSize = arrowButtonOp
              .map(JComponent::getWidth)
              .orElse(r.height - rendererIns.top - rendererIns.bottom);
          availableWidth -= buttonSize;
        }
        return availableWidth;
      }
    };
  }

  // <blockquote cite="https://tips4java.wordpress.com/2008/11/12/left-dot-renderer/">
  // @title Left Dot Renderer
  // @author Rob Camick
  // FontMetrics fm = getFontMetrics(getFont());
  // if (fm.stringWidth(text) > width) {
  //   String dots = "...";
  //   int textWidth = fm.stringWidth(dots);
  //   int nChars = text.length() - 1;
  //   while (nChars > 0) {
  //     textWidth += fm.charWidth(text.charAt(nChars));
  //     if (textWidth > width) {
  //       break;
  //     }
  //     nChars--;
  //   }
  //   setText(dots + text.substring(nChars + 1));
  // }
  // </blockquote>
  private static String getLeftClippedText(String text, FontMetrics fm, int availableWidth) {
    String dots = "...";
    int textWidth = fm.stringWidth(dots);
    int len = text.length();
    // @see Unicode surrogate programming with the Java language
    // https://www.ibm.com/developerworks/library/j-unicode/index.html
    // https://www.ibm.com/developerworks/jp/ysl/library/java/j-unicode_surrogate/index.html
    int[] acp = new int[text.codePointCount(0, len)];
    int j = acp.length;
    for (int i = len; i > 0; i = text.offsetByCodePoints(i, -1)) {
      int cp = text.codePointBefore(i);
      textWidth += fm.charWidth(cp);
      if (textWidth > availableWidth) {
        break;
      }
      acp[--j] = cp;
    }
    return dots + new String(acp, j, acp.length - j);
  }

  private static int getLookAndFeelDependWidth(JComboBox<?> combo, int width) {
    int availableWidth = width;
    Insets padding = UIManager.getInsets("ComboBox.padding");
    if (padding != null) {
      // NimbusComboBoxUI only?
      availableWidth -= padding.left + padding.right;
    }
    ComboBoxUI ui = combo.getUI();
    if (ui instanceof MetalComboBoxUI) {
      // Magic number in MetalComboBoxUI#paintCurrentValue(...)
      // This is really only called if we're using ocean.
      // if (MetalLookAndFeel.usingOcean()) {
      //   bounds.width -= 3;
      availableWidth -= 3;
    } else if (ui.getClass().getName().contains("Windows")) {
      // Magic number in WindowsComboBoxUI#paintCurrentValue(...)
      // XPStyle xp = XPStyle.getXP();
      // if (xp != null) {
      //   bounds.width -= 4;
      // } else {
      //   bounds.width -= 2;
      // }
      String lnfName = UIManager.getLookAndFeel().getName();
      if (Objects.equals(lnfName, "Windows")) {
        availableWidth -= 4;
      } else { // Windows Classic
        availableWidth -= 2;
      }
    }
    return availableWidth;
  }

  private static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance)
        .map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
