// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private static final String SITE = "https://ateraimemo.com/";

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
    p.setBorder(BorderFactory.createTitledBorder("Draggable Hyperlink"));
    p.add(new JLabel("D&D->Browser:"));

    JButton label = new JButton(SITE) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(LinkViewButtonUI.createUI(this, SITE));
      }
    };
    p.add(label);

    // label.addActionListener(e -> {
    //   System.out.println(e);
    //   if (Desktop.isDesktopSupported()) {
    //     try {
    //       Desktop.getDesktop().browse(new URI(SITE));
    //     } catch (IOException | URISyntaxException ex) {
    //       ex.printStackTrace();
    //       UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
    //     }
    //   }
    // });

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea("JTextArea")));
    setPreferredSize(new Dimension(320, 240));
  }

  // // TransferHandler
  // DataFlavor uriFlavor = new DataFlavor(String.class, "text/uri-list");
  // JLabel label = new JLabel(SITE);
  // label.setTransferHandler(new TransferHandler("text") {
  //   @Override public boolean canImport(JComponent c, DataFlavor[] flavors) {
  //     return (flavors.length > 0 && flavors[0].equals(uriFlavor));
  //   }
  //
  //   @Override protected Transferable createTransferable(JComponent c) {
  //     return new Transferable() {
  //       @Override public Object getTransferData(DataFlavor flavor) {
  //         return SITE;
  //       }
  //
  //       @Override public DataFlavor[] getTransferDataFlavors() {
  //         return new DataFlavor[] {uriFlavor};
  //       }
  //
  //       @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
  //         return flavor.equals(uriFlavor);
  //       }
  //     };
  //   }
  // });
  // label.addMouseListener(new MouseAdapter() {
  //   @Override public void mousePressed(MouseEvent e) {
  //     JComponent l = (JComponent) e.getSource();
  //     TransferHandler handler = l.getTransferHandler();
  //     handler.exportAsDrag(l, e, TransferHandler.COPY);
  //   }
  // });
  // // DragGestureListener
  // DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
  //       label, DnDConstants.ACTION_COPY, new DragGestureListener() {
  //   @Override public void dragGestureRecognized(DragGestureEvent dge) {
  //     Transferable t = new Transferable() {
  //       @Override public Object getTransferData(DataFlavor flavor) {
  //         return SITE;
  //       }
  //
  //       @Override public DataFlavor[] getTransferDataFlavors() {
  //         return new DataFlavor[] {uriFlavor};
  //       }
  //
  //       @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
  //         return flavor.equals(uriFlavor);
  //       }
  //     };
  //     dge.startDrag(DragSource.DefaultCopyDrop, t, null);
  //   }
  // });

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

class LinkViewButtonUI extends BasicButtonUI {
  // protected static final DataFlavor URI_FLAVOR = new DataFlavor(String.class, "text/uri-list");
  protected static final DataFlavor URI_FLAVOR = DataFlavor.stringFlavor;
  // protected final Dimension size = new Dimension();
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  public static ButtonUI createUI(JButton b, String href) {
    b.setForeground(Color.BLUE);
    b.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    b.setTransferHandler(new TransferHandler("text") {
      @Override public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return flavors.length > 0 && URI_FLAVOR.equals(flavors[0]);
      }

      @Override protected Transferable createTransferable(JComponent c) {
        return new Transferable() {
          @Override public Object getTransferData(DataFlavor flavor) {
            // System.out.println(flavor.getMimeType());
            return href;
          }

          @Override public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {URI_FLAVOR};
          }

          @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
            // System.out.println(flavor.getMimeType());
            return URI_FLAVOR.equals(flavor);
          }
        };
      }
    });
    b.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        JButton button = (JButton) e.getComponent();
        TransferHandler handler = button.getTransferHandler();
        handler.exportAsDrag(button, e, TransferHandler.COPY);
      }
    });
    return new LinkViewButtonUI();
  }

  @Override public void paint(Graphics g, JComponent c) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    Font f = b.getFont();
    g.setFont(f);
    SwingUtilities.calculateInnerArea(c, viewRect);
    iconRect.setBounds(0, 0, 0, 0);
    textRect.setBounds(0, 0, 0, 0);

    String text = SwingUtilities.layoutCompoundLabel(
        c,
        c.getFontMetrics(f),
        b.getText(),
        null, // icon != null ? icon : getDefaultIcon(),
        b.getVerticalAlignment(),
        b.getHorizontalAlignment(),
        b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        0); // b.getText() == null ? 0 : b.getIconTextGap());

    if (c.isOpaque()) {
      g.setColor(b.getBackground());
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }

    ButtonModel m = b.getModel();
    boolean isRollover = b.isRolloverEnabled() && m.isRollover();
    if (!m.isSelected() && !m.isPressed() && !m.isArmed() && isRollover) {
      g.setColor(Color.BLUE);
      int yh = viewRect.y + viewRect.height;
      g.drawLine(viewRect.x, yh, viewRect.x + viewRect.width, yh);
    }
    Object o = c.getClientProperty(BasicHTML.propertyKey);
    if (o instanceof View) {
      ((View) o).paint(g, textRect);
    } else {
      paintText(g, b, textRect, text);
    }
  }
}
