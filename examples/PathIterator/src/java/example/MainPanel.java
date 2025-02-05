// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import javax.swing.*;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
  private final SpinnerNumberModel outer = new SpinnerNumberModel(40, 10, 1000, 1);
  private final SpinnerNumberModel inner = new SpinnerNumberModel(30, 10, 1000, 1);
  private final SpinnerNumberModel vcModel = new SpinnerNumberModel(20, 3, 100, 1);
  private final JTextField styleField = new JTextField("stroke:none; fill:pink");
  private final JCheckBox check = new JCheckBox("Antialias", true);
  private final JLabel label = new JLabel();
  private final JTextArea textArea = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    initStar();
    ChangeListener cl = e -> initStar();
    outer.addChangeListener(cl);
    inner.addChangeListener(cl);
    vcModel.addChangeListener(cl);
    check.addChangeListener(cl);

    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setHorizontalAlignment(SwingConstants.CENTER);

    check.setHorizontalAlignment(SwingConstants.RIGHT);

    JTabbedPane tab = new JTabbedPane();
    tab.addTab("Preview", makePreviewPanel());
    tab.addTab("SVG", makeSvgPanel());

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(makePreferencesPanel(), BorderLayout.NORTH);
    panel.add(tab);

    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  private Component makePreviewPanel() {
    JPanel p = new JPanel(new BorderLayout());
    p.add(check, BorderLayout.SOUTH);
    p.add(new JScrollPane(label));
    return p;
  }

  private Component makePreferencesPanel() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("Preferences"));
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.LINE_END;
    p.add(new JLabel("Addendum Circle Radius:"), c);
    p.add(new JLabel("Dedendum Circle Radius:"), c);
    p.add(new JLabel("Count of Teeth:"), c);

    c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(new JSpinner(outer), c);
    p.add(new JSpinner(inner), c);
    p.add(new JSpinner(vcModel), c);
    return p;
  }

  private Component makeSvgPanel() {
    JButton button = new JButton("set");
    button.addActionListener(e -> initStar());

    JPanel sp = new JPanel(new BorderLayout(2, 2));
    sp.add(new JLabel("style:"), BorderLayout.WEST);
    sp.add(styleField);
    sp.add(button, BorderLayout.EAST);

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(sp, BorderLayout.SOUTH);
    p.add(new JScrollPane(textArea));

    // JLabel l = new JLabel("Starburst.svg(drag here)");
    // DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
    //     l, DnDConstants.ACTION_MOVE, new MyDragGestureListener());
    // p.add(l, BorderLayout.NORTH);
    return p;
  }

  // class MyDragGestureListener implements DragGestureListener {
  //   @Override public void dragGestureRecognized(DragGestureEvent dge) {
  //     File outfile;
  //     try {
  //       outfile = File.createTempFile("starburst", ".svg");
  //       FileWriter w = new FileWriter(outfile);
  //       w.writeData(textArea.getText());
  //       // outfile.deleteOnExit();
  //     } catch (IOException ex) {
  //       Toolkit.getDefaultToolkit().beep();
  //       JOptionPane.showMessageDialog(
  //           null, "Could not create file.", "Error", JOptionPane.ERROR_MESSAGE);
  //       return;
  //     }
  //     if (Objects.isNull(outfile)) {
  //       return;
  //     }
  //     File tmpFile = outfile;
  //     Transferable tran = new Transferable() {
  //       @Override public Object getTransferData(DataFlavor flavor) {
  //         return Arrays.asList(tmpFile);
  //       }
  //
  //       @Override public DataFlavor[] getTransferDataFlavors() {
  //         return new DataFlavor[] {DataFlavor.javaFileListFlavor};
  //       }
  //
  //       @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
  //         return flavor.equals(DataFlavor.javaFileListFlavor);
  //       }
  //     };
  //     DragSourceAdapter dsa = new DragSourceAdapter() {
  //       @Override public void dragDropEnd(DragSourceDropEvent e) {
  //         if (e.getDropSuccess()) {
  //           System.out.println(e);
  //         }
  //       }
  //     };
  //     dge.startDrag(DragSource.DefaultMoveDrop, tran, dsa);
  //   }
  // }

  public void initStar() {
    int r1 = outer.getNumber().intValue();
    int r2 = inner.getNumber().intValue();
    int vc = vcModel.getNumber().intValue();
    boolean antialias = check.isSelected();
    // outer.setMinimum(r2 + 1);
    Path2D star = SvgUtils.makeStar(r1, r2, vc);
    label.setIcon(new StarIcon(star, antialias));
    int min = Math.min(r1, r2);
    int max = Math.max(r1, r2);
    String fmt1 = "addendum_circle_radius=\"%d\" ";
    String fmt2 = "dedendum_circle_radius =\"%d\" ";
    String fmt3 = "number_of_teeth=\"%dT\"";
    String fmt = fmt1 + fmt2 + fmt3;
    String desc = String.format(fmt, max, min, vc);
    String style = styleField.getText().trim();
    String str = SvgUtils.makeStarburstSvg(star.getPathIterator(null), max * 2, style, desc);
    textArea.setText(str);
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

final class SvgUtils {
  private SvgUtils() {
    /* HideUtilityClassConstructor */
  }

  public static String makeStarburstSvg(PathIterator pi, int sz, String style, String desc) {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    String dtd = "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd";
    String type = String.format("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"%s\">", dtd);
    String xmlns = "http://www.w3.org/2000/svg";
    StringBuilder sb = new StringBuilder(200);
    sb.append(String.format("%s%n%s%n", xml, type))
        .append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"%s\">%n", sz, sz, xmlns))
        .append(String.format("  <desc>%s</desc>%n", desc))
        .append("  <path d=\"");
    double[] c = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(c)) {
        case PathIterator.SEG_MOVETO:
          sb.append(String.format("M%.2f,%.2f ", c[0], c[1]));
          break;
        case PathIterator.SEG_LINETO:
          sb.append(String.format("L%.2f,%.2f ", c[0], c[1]));
          break;
        case PathIterator.SEG_QUADTO:
          sb.append(String.format("Q%.2f,%.2f,%.2f,%.2f ", c[0], c[1], c[2], c[3]));
          break;
        case PathIterator.SEG_CUBICTO:
          sb.append(String.format(
              "C%.2f,%.2f,%.2f,%.2f,%.2f,%.2f ", c[0], c[1], c[2], c[3], c[4], c[5]));
          break;
        case PathIterator.SEG_CLOSE:
          sb.append('Z');
          break;
        default:
          // Should never happen
          throw new InternalError("unrecognized path type");
      }
      pi.next();
    }
    sb.append(String.format("\" style=\"%s\" />%n</svg>%n", style));
    return sb.toString();
  }

  public static Path2D makeStar(int r1, int r2, int vc) {
    double or = Math.max(r1, r2);
    double ir = Math.min(r1, r2);
    double agl = 0d;
    double add = Math.PI / vc;
    Path2D p = new Path2D.Double();
    p.moveTo(or, 0d);
    for (int i = 0; i < vc * 2 - 1; i++) {
      agl += add;
      double r = i % 2 == 0 ? ir : or;
      p.lineTo(r * Math.cos(agl), r * Math.sin(agl));
    }
    p.closePath();
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2d, or, 0d);
    return new Path2D.Double(p, at);
  }
}

class StarIcon implements Icon {
  private final Shape star;
  private final boolean antialias;

  protected StarIcon(Shape s, boolean a) {
    star = s;
    antialias = a;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.PINK);
    if (antialias) {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    g2.fill(star);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return star.getBounds().width;
  }

  @Override public int getIconHeight() {
    return star.getBounds().height;
  }
}

// class FileWriter {
//   private final File file;
//
//   protected FileWriter(File file) {
//     this.file = file;
//   }
//
//   public void writeData(String str) {
//     try (Writer bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
//       bw.write(str, 0, str.length());
//     } catch (IOException ex) {
//       ex.printStackTrace();
//     }
//   }
// }
