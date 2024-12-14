// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("example.TabButton", "TabViewButtonUI");
    UIManager.put("TabViewButtonUI", "example.OperaTabViewButtonUI");
    CardLayoutTabbedPane tab3 = new CardLayoutTabbedPane();
    tab3.setBorder(BorderFactory.createTitledBorder("CardLayout+JRadioButton(opera like)"));
    tab3.addTab("9999", new JScrollPane(new JTree()));
    tab3.addTab("000000000", new JLabel("JLabel 1"));
    tab3.addTab("1111", new JLabel("JLabel 2"));
    tab3.addTab("222", new JButton("JButton"));

    add(tab3);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

class CardLayoutTabbedPane extends JPanel {
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel tabPanel = new JPanel(new TabLayout());
  private final JPanel contentsPanel = new JPanel(cardLayout);
  private final ButtonGroup group = new ButtonGroup();
  private final JButton button = new JButton(new PlusIcon());
  private final Random rnd = new Random();
  // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
  private final List<ImageIcon> icons = Arrays.asList(
      new ImageIcon(makeImage("wi0009-16.png")),
      new ImageIcon(makeImage("wi0054-16.png")),
      new ImageIcon(makeImage("wi0062-16.png")),
      new ImageIcon(makeImage("wi0063-16.png")),
      new ImageIcon(makeImage("wi0064-16.png")),
      new ImageIcon(makeImage("wi0096-16.png")),
      new ImageIcon(makeImage("wi0111-16.png")),
      new ImageIcon(makeImage("wi0122-16.png")),
      new ImageIcon(makeImage("wi0124-16.png")),
      new ImageIcon(makeImage("wi0126-16.png"))
  );

  protected CardLayoutTabbedPane() {
    super(new BorderLayout());
    int left = 0;
    int right = 0;
    tabPanel.setBorder(BorderFactory.createMatteBorder(0, left, 0, right, new Color(0x14_1E_32)));
    contentsPanel.setBorder(BorderFactory.createEmptyBorder(4, left, 2, right));

    tabPanel.setOpaque(true);
    tabPanel.setBackground(new Color(0x14_1E_32));

    JPanel wrapPanel = new JPanel(new BorderLayout());
    wrapPanel.setOpaque(true);
    wrapPanel.setBackground(new Color(0x14_1E_32));

    // contentsPanel.setOpaque(true);
    // contentsPanel.setBackground(new Color(0x14_1E_32));

    wrapPanel.add(tabPanel);
    // TEST: wrapPanel.add(new JButton("a"), BorderLayout.WEST);

    // JPanel locPanel = new JPanel();
    // wrapPanel.add(new JButton("b"), BorderLayout.SOUTH);

    add(wrapPanel, BorderLayout.NORTH);
    add(contentsPanel);

    AtomicInteger count = new AtomicInteger();
    button.setBorder(BorderFactory.createEmptyBorder());
    button.addActionListener(e -> {
      int n = count.getAndIncrement();
      addTab("new tab:" + n, new JLabel("xxx:" + n));
    });
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  protected Component createTabComponent(String title, Component comp) {
    // TabButton tab = new TabButton(new AbstractAction(title) {
    //   @Override public void actionPerformed(ActionEvent e) {
    //     cardLayout.show(contentsPanel, title);
    //   }
    // });
    TabButton tab = new TabButton(title);
    tab.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        ((AbstractButton) e.getComponent()).setSelected(true);
        cardLayout.show(contentsPanel, title);
      }
    });
    tab.setIcon(icons.get(rnd.nextInt(icons.size())));
    tab.setLayout(new BorderLayout());
    JButton close = new JButton(new CloseTabIcon(Color.GRAY)) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(12, 12);
      }
    };
    close.addActionListener(e -> {
      tabPanel.remove(tab);
      contentsPanel.remove(comp);
      boolean oneOrMore = tabPanel.getComponentCount() > 1;
      if (oneOrMore) {
        tabPanel.revalidate();
        TabButton b = (TabButton) tabPanel.getComponent(0);
        b.setSelected(true);
        cardLayout.first(contentsPanel);
      }
      tabPanel.revalidate();
    });
    close.setBorder(BorderFactory.createEmptyBorder());
    close.setFocusPainted(false);
    close.setContentAreaFilled(false);
    close.setPressedIcon(new CloseTabIcon(new Color(0xFE_FE_FE)));
    close.setRolloverIcon(new CloseTabIcon(new Color(0xA0_A0_A0)));

    JPanel p = new JPanel(new BorderLayout());
    p.setOpaque(false);
    p.add(close, BorderLayout.NORTH);
    tab.add(p, BorderLayout.EAST);
    group.add(tab);
    tab.setSelected(true);
    return tab;
  }

  public void addTab(String title, Component comp) {
    tabPanel.remove(button);
    tabPanel.add(createTabComponent(title, comp));
    tabPanel.add(button);
    tabPanel.revalidate();
    contentsPanel.add(comp, title);
    cardLayout.show(contentsPanel, title);
  }

  private Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource("example/" + path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(this::makeMissingImage);
  }

  private Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
  }
}

class TabButton extends JRadioButton {
  private static final String UI_CLASS_ID = "TabViewButtonUI";
  private Color textColor; // = Color.WHITE;
  private Color pressedTc; // = Color.WHITE.darker();
  private Color rolloverTc; // = Color.WHITE;
  private Color rolloverSelTc; // = Color.WHITE;
  private Color selectedTc; // = Color.WHITE;

  protected TabButton() {
    super(null, null);
  }

  protected TabButton(Icon icon) {
    super(null, icon);
  }

  protected TabButton(String text) {
    super(text, null);
  }

  protected TabButton(Action a) {
    super(a);
    // super.setAction(a);
    // updateUI();
  }

  protected TabButton(String text, Icon icon) {
    super(text, icon);
    // updateUI();
  }

  @Override public void updateUI() {
    if (Objects.nonNull(UIManager.get(getUIClassID()))) {
      setUI((TabViewButtonUI) UIManager.getUI(this));
    } else {
      setUI(new BasicTabViewButtonUI());
    }
  }

  @Override public String getUIClassID() {
    return UI_CLASS_ID;
  }

  // @Override public void setUI(TabViewButtonUI ui) {
  //   super.setUI(ui);
  // }

  @Override public TabViewButtonUI getUI() {
    return (TabViewButtonUI) ui;
  }

  @Override protected void fireStateChanged() {
    ButtonModel model = getModel();
    if (model.isEnabled()) {
      if (model.isPressed() && model.isArmed()) {
        setForeground(getPressedTextColor());
      } else if (model.isSelected()) {
        setForeground(getSelectedTextColor());
      } else if (isRolloverEnabled() && model.isRollover()) {
        setForeground(getRolloverTextColor());
      } else {
        setForeground(getTextColor());
      }
    } else {
      setForeground(Color.GRAY);
    }
    super.fireStateChanged();
  }

  @Override public Dimension getMinimumSize() {
    Dimension d = super.getMinimumSize();
    d.width = 16;
    return d;
  }

  @Override public String toString() {
    return "TabButton";
  }

  public Color getTextColor() {
    return textColor;
  }

  public Color getPressedTextColor() {
    return pressedTc;
  }

  public Color getRolloverTextColor() {
    return rolloverTc;
  }

  public Color getRolloverSelectedTextColor() {
    return rolloverSelTc;
  }

  public Color getSelectedTextColor() {
    return selectedTc;
  }

  public void setTextColor(Color color) {
    textColor = color;
  }

  public void setPressedTextColor(Color color) {
    pressedTc = color;
  }

  public void setRolloverTextColor(Color color) {
    rolloverTc = color;
  }

  public void setRolloverSelectedTextColor(Color color) {
    rolloverSelTc = color;
  }

  public void setSelectedTextColor(Color color) {
    selectedTc = color;
  }
}

class TabLayout implements LayoutManager, Serializable {
  private static final long serialVersionUID = 1L;
  private static final int TAB_WIDTH = 100;

  @Override public void addLayoutComponent(String name, Component comp) {
    /* not needed */
  }

  @Override public void removeLayoutComponent(Component comp) {
    /* not needed */
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public Dimension preferredLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      int last = parent.getComponentCount() - 1;
      int w = 0;
      int h = 0;
      if (last >= 0) {
        Component comp = parent.getComponent(last);
        Dimension d = comp.getPreferredSize();
        w = d.width;
        h = d.height;
      }
      Insets i = parent.getInsets();
      return new Dimension(i.left + i.right + w, i.top + i.bottom + h);
    }
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public Dimension minimumLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      return new Dimension(100, 24);
    }
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      int count = parent.getComponentCount();
      if (count == 0) {
        return;
      }
      // int rowCount = 1;
      // boolean ltr = parent.getComponentOrientation().isLeftToRight();
      Insets insets = parent.getInsets();
      int colCount = count - 1;
      int lastWidth = parent.getComponent(count - 1).getPreferredSize().width;
      int width = parent.getWidth() - insets.left - insets.right - lastWidth;
      int h = parent.getHeight() - insets.top - insets.bottom;
      int w = width > TAB_WIDTH * colCount ? TAB_WIDTH : width / colCount;
      int gap = width - w * colCount;
      int x = insets.left;
      int y = insets.top;
      for (int i = 0; i < count; i++) {
        int a = gap > 0 ? 1 : 0;
        int cw = i == colCount ? lastWidth : w + a;
        parent.getComponent(i).setBounds(x, y, cw, h);
        x += cw;
        gap--;
      }
    }
  }

  @Override public String toString() {
    return getClass().getName();
  }
}
