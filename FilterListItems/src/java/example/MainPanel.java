// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private final transient ListItem[] defaultModel = {
      new ListItem("wi0009-32.png"),
      new ListItem("wi0054-32.png"),
      new ListItem("wi0062-32.png"),
      new ListItem("wi0063-32.png"),
      new ListItem("wi0064-32.png"),
      new ListItem("wi0096-32.png"),
      new ListItem("wi0111-32.png"),
      new ListItem("wi0122-32.png"),
      new ListItem("wi0124-32.png"),
      new ListItem("wi0126-32.png")
  };
  private final DefaultListModel<ListItem> model = new DefaultListModel<>();
  private final JList<ListItem> list = new JList<ListItem>(model) {
    @Override public void updateUI() {
      setSelectionForeground(null); // Nimbus
      setSelectionBackground(null); // Nimbus
      setCellRenderer(null);
      super.updateUI();
      setLayoutOrientation(HORIZONTAL_WRAP);
      setVisibleRowCount(0);
      setFixedCellWidth(82);
      setFixedCellHeight(64);
      setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      setCellRenderer(new ListItemListCellRenderer<>());
      getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
  };

  private MainPanel() {
    super(new BorderLayout(5, 5));
    for (ListItem item : defaultModel) {
      model.addElement(item);
    }

    JTextField field = new JTextField(15);
    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        filter(field.getText());
      }

      @Override public void removeUpdate(DocumentEvent e) {
        filter(field.getText());
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });

    add(field, BorderLayout.NORTH);
    add(new JScrollPane(list));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Optional<Pattern> getPattern(String regex) {
    Optional<Pattern> op;
    if (Objects.nonNull(regex) && !regex.isEmpty()) {
      try {
        op = Optional.of(Pattern.compile(regex));
      } catch (PatternSyntaxException ex) {
        op = Optional.empty();
      }
    } else {
      op = Optional.empty();
    }
    return op;
  }

  public void filter(String txt) {
    getPattern(txt).ifPresent(pattern -> {
      List<ListItem> selected = list.getSelectedValuesList();
      model.clear();
      Stream.of(defaultModel)
          .filter(item -> pattern.matcher(item.getTitle()).find())
          .forEach(model::addElement);
      selected.forEach(item -> {
        int i = model.indexOf(item);
        list.addSelectionInterval(i, i);
      });
    });
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

class SelectedImageFilter extends RGBImageFilter {
  @Override public int filterRGB(int x, int y, int argb) {
    return argb & 0xFF_FF_FF_00 | (argb & 0xFF) >> 1;
  }
}

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final JLabel icon = new JLabel((Icon) null, SwingConstants.CENTER);
  private final JLabel label = new JLabel("", SwingConstants.CENTER);
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder;

  protected ListItemListCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(label);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    icon.setOpaque(false);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(noFocusBorder);

    renderer.setOpaque(false);
    renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    renderer.add(icon);
    renderer.add(label, BorderLayout.SOUTH);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    label.setText(value.getTitle());
    label.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    if (isSelected) {
      icon.setIcon(value.getSelectedIcon());
      label.setForeground(list.getSelectionForeground());
      label.setBackground(list.getSelectionBackground());
      label.setOpaque(true);
    } else {
      icon.setIcon(value.getIcon());
      label.setForeground(list.getForeground());
      label.setBackground(list.getBackground());
      label.setOpaque(false);
    }
    return renderer;
  }
}

class ListItem {
  private final ImageIcon icon;
  private final ImageIcon selectedIcon;
  private final String title;

  protected ListItem(String title) {
    this.title = title;
    Image img = makeImage("example/" + title);
    this.icon = new ImageIcon(img);
    ImageProducer ip = new FilteredImageSource(img.getSource(), new SelectedImageFilter());
    this.selectedIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
  }

  public String getTitle() {
    return title;
  }

  public ImageIcon getIcon() {
    return icon;
  }

  public ImageIcon getSelectedIcon() {
    return selectedIcon;
  }

  public static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ListItem::makeMissingImage);
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (32 - iw) / 2, (32 - ih) / 2);
    g2.dispose();
    return bi;
  }
}
