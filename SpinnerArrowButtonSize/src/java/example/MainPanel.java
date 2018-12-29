// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    SpinnerModel model = new SpinnerNumberModel(5, 0, 10, 1);

    JSpinner spinner0 = new JSpinner(model);

    UIManager.put("Spinner.arrowButtonSize", new Dimension(60, 0));
    JSpinner spinner1 = new JSpinner(model);

    JSpinner spinner2 = new JSpinner(model) {
      @Override public void updateUI() {
        super.updateUI();
        stream(this)
            .filter(JButton.class::isInstance).map(JButton.class::cast)
            .forEach(b -> {
              Dimension d = b.getPreferredSize();
              d.width = 40;
              b.setPreferredSize(d);
            });
      }
    };

    JSpinner spinner3 = new JSpinner(model) {
      @Override public void setLayout(LayoutManager mgr) {
        super.setLayout(new SpinnerLayout());
      }
    };

    JPanel p = new JPanel(new GridLayout(2, 2));
    p.add(makeTitledPanel("default", spinner0));
    p.add(makeTitledPanel("Spinner.arrowButtonSize", spinner1));
    p.add(makeTitledPanel("setPreferredSize", spinner2));
    p.add(makeTitledPanel("setLayout", spinner3));

    JSpinner spinner4 = new JSpinner(model) {
      @Override public void updateUI() {
        super.updateUI();
        setFont(getFont().deriveFont(32f));
        stream(this)
            .filter(JButton.class::isInstance).map(JButton.class::cast)
            .forEach(b -> {
              Dimension d = b.getPreferredSize();
              d.width = 50;
              b.setPreferredSize(d);
            });
      }
    };

    Box box = Box.createVerticalBox();
    box.add(p);
    box.add(makeTitledPanel("setPreferredSize + setFont", spinner4));

    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Stream<Component> stream(Container parent) {
    return Stream.of(parent.getComponents())
      .filter(Container.class::isInstance).map(c -> stream(Container.class.cast(c)))
      .reduce(Stream.of(parent), Stream::concat);
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(mb);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see javax/swing/plaf/basic/BasicSpinnerUI.java
class SpinnerLayout implements LayoutManager {
  private Component nextButton;
  private Component previousButton;
  private Component editor;

  @Override public void addLayoutComponent(String name, Component c) {
    if ("Next".equals(name)) {
      nextButton = c;
    } else if ("Previous".equals(name)) {
      previousButton = c;
    } else if ("Editor".equals(name)) {
      editor = c;
    }
  }

  @Override public void removeLayoutComponent(Component c) {
    if (Objects.equals(c, nextButton)) {
      nextButton = null;
    } else if (Objects.equals(c, previousButton)) {
      previousButton = null;
    } else if (Objects.equals(c, editor)) {
      editor = null;
    }
  }

  private static Dimension preferredSize(Component c) {
    return Optional.ofNullable(c).map(Component::getPreferredSize).orElseGet(() -> new Dimension());
    // Objects.nonNull(c) ? new Dimension() : c.getPreferredSize();
  }

  @Override public Dimension preferredLayoutSize(Container parent) {
    Dimension nextD = preferredSize(nextButton);
    Dimension previousD = preferredSize(previousButton);
    Dimension editorD = preferredSize(editor);

    // Force the editors height to be a multiple of 2
    editorD.height = ((editorD.height + 1) / 2) * 2;

    Dimension size = new Dimension(editorD.width, editorD.height);
    size.width += Math.max(nextD.width, previousD.width);
    Insets insets = parent.getInsets();
    size.width += insets.left + insets.right;
    size.height += insets.top + insets.bottom;
    return size;
  }

  @Override public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  private static void setBounds(Component c, int x, int y, int width, int height) {
    if (Objects.nonNull(c)) {
      c.setBounds(x, y, width, height);
    }
  }

  @Override public void layoutContainer(Container parent) {
    int width = parent.getWidth();
    int height = parent.getHeight();

    Insets insets = parent.getInsets();

    if (Objects.isNull(nextButton) && Objects.isNull(previousButton)) {
      setBounds(editor, insets.left,  insets.top, width - insets.left - insets.right, height - insets.top - insets.bottom);
      return;
    }

    // Dimension nextD = preferredSize(nextButton);
    // Dimension previousD = preferredSize(previousButton);
    int buttonsWidth = 100; // Math.max(nextD.width, previousD.width);
    int editorHeight = height - (insets.top + insets.bottom);

    // The arrowButtonInsets value is used instead of the JSpinner's
    // insets if not null. Defining this to be (0, 0, 0, 0) causes the
    // buttons to be aligned with the outer edge of the spinner's
    // border, and leaving it as "null" places the buttons completely
    // inside the spinner's border.
    Insets buttonInsets = UIManager.getInsets("Spinner.arrowButtonInsets");
    if (Objects.isNull(buttonInsets)) {
      buttonInsets = insets;
    }

    // Deal with the spinner's componentOrientation property.
    int editorX;
    int editorWidth;
    int buttonsX;
    if (parent.getComponentOrientation().isLeftToRight()) {
      editorX = insets.left;
      editorWidth = width - insets.left - buttonsWidth - buttonInsets.right;
      buttonsX = width - buttonsWidth - buttonInsets.right;
    } else {
      buttonsX = buttonInsets.left;
      editorX = buttonsX + buttonsWidth;
      editorWidth = width - buttonInsets.left - buttonsWidth - insets.right;
    }

    int nextY = buttonInsets.top;
    int nextHeight = height / 2 + height % 2 - nextY;
    int previousY = buttonInsets.top + nextHeight;
    int previousHeight = height - previousY - buttonInsets.bottom;

    setBounds(editor, editorX, insets.top, editorWidth, editorHeight);
    setBounds(nextButton, buttonsX, nextY, buttonsWidth, nextHeight);
    setBounds(previousButton, buttonsX, previousY, buttonsWidth, previousHeight);
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() { /* Singleton */ }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafRadioGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
    }
    return menu;
  }

  private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = lafRadioGroup.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        ex.printStackTrace();
      }
    });
    lafRadioGroup.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window: Frame.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
