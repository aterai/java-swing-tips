package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import javax.swing.*;
// import javax.swing.border.*;
// import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String MYSITE = "http://http://ateraimemo.com/";
    private final JTextArea textArea = new JTextArea();
    public MainPanel() {
        super(new BorderLayout());
        JButton label = new JButton(new AbstractAction(MYSITE) {
            @Override public void actionPerformed(ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(MYSITE));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        label.setUI(LinkViewButtonUI.createUI(label, MYSITE));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Draggable Hyperlink"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;

        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.EAST;
        c.gridy   = 0; p.add(new JLabel("D&D->Brouser: "), c);
        c.gridx   = 1;
        c.weightx = 1d;
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; p.add(label, c);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 240));
    }
//         //TransferHandler
//         final DataFlavor uriflavor = new DataFlavor(String.class, "text/uri-list");
//         final JLabel label = new JLabel(MYSITE);
//         label.setTransferHandler(new TransferHandler("text") {
//             @Override public boolean canImport(JComponent c, DataFlavor[] flavors) {
//                 return (flavors.length > 0 && flavors[0].equals(uriflavor));
//             }
//             @Override public Transferable createTransferable(JComponent c) {
//                 return new Transferable() {
//                     @Override public Object getTransferData(DataFlavor flavor) {
//                         return MYSITE;
//                     }
//                     @Override public DataFlavor[] getTransferDataFlavors() {
//                         return new DataFlavor[] { uriflavor };
//                     }
//                     @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
//                         return flavor.equals(uriflavor);
//                     }
//                 };
//             }
//         });
//         label.addMouseListener(new MouseAdapter() {
//             @Override public void mousePressed(MouseEvent e) {
//                 JComponent l = (JComponent) e.getSource();
//                 TransferHandler handler = l.getTransferHandler();
//                 handler.exportAsDrag(l, e, TransferHandler.COPY);
//             }
//         });
//         //DragGestureListener
//         DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_COPY, new DragGestureListener() {
//             @Override public void dragGestureRecognized(DragGestureEvent dge) {
//                 Transferable t = new Transferable() {
//                     @Override public Object getTransferData(DataFlavor flavor) {
//                         return MYSITE;
//                     }
//                     @Override public DataFlavor[] getTransferDataFlavors() {
//                         return new DataFlavor[] { uriflavor };
//                     }
//                     @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
//                         return flavor.equals(uriflavor);
//                     }
//                 };
//                 dge.startDrag(DragSource.DefaultCopyDrop, t, null);
//             }
//         });

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class LinkViewButtonUI extends BasicButtonUI {
    private static final LinkViewButtonUI LINK_VIEW_BUTTON_UI = new LinkViewButtonUI();
    //private static final DataFlavor URI_FLAVOR = new DataFlavor(String.class, "text/uri-list");
    private static final DataFlavor URI_FLAVOR = DataFlavor.stringFlavor;
    private final Dimension size;
    private final Rectangle viewRect;
    private final Rectangle iconRect;
    private final Rectangle textRect;

    public static ButtonUI createUI(JButton b, final String href) {
        b.setForeground(Color.BLUE);
        b.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setTransferHandler(new TransferHandler("text") {
            @Override public boolean canImport(JComponent c, DataFlavor... flavors) {
                return flavors.length > 0 && flavors[0].equals(URI_FLAVOR);
            }
            public Transferable createTransferable(JComponent c) {
                return new Transferable() {
                    @Override public Object getTransferData(DataFlavor flavor) {
                        //System.out.println(flavor.getMimeType());
                        return href;
                    }
                    @Override public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[] {URI_FLAVOR};
                    }
                    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                        //System.out.println(flavor.getMimeType());
                        return flavor.equals(URI_FLAVOR);
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
        return LINK_VIEW_BUTTON_UI;
    }
    public LinkViewButtonUI() {
        super();
        size = new Dimension();
        viewRect = new Rectangle();
        iconRect = new Rectangle();
        textRect = new Rectangle();
    }
    @Override public synchronized void paint(Graphics g, JComponent c) {
        if (!(c instanceof AbstractButton)) {
            return;
        }
        AbstractButton b = (AbstractButton) c;
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = c.getFontMetrics(f);

        Insets i = c.getInsets();
        b.getSize(size);
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = size.width - i.right - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        iconRect.setBounds(0, 0, 0, 0); //.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.setBounds(0, 0, 0, 0); //.x = textRect.y = textRect.width = textRect.height = 0;

        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), null, //altIcon != null ? altIcon : getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            0); //b.getText() == null ? 0 : b.getIconTextGap());

        if (c.isOpaque()) {
            g.setColor(b.getBackground());
            g.fillRect(0, 0, size.width, size.height);
        }

        ButtonModel model = b.getModel();
        if (!model.isSelected() && !model.isPressed() && !model.isArmed() && b.isRolloverEnabled() && model.isRollover()) {
            g.setColor(Color.BLUE);
            g.drawLine(viewRect.x,                viewRect.y + viewRect.height,
                       viewRect.x + viewRect.width, viewRect.y + viewRect.height);
        }
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v == null) {
            paintText(g, b, textRect, text);
        } else {
            v.paint(g, textRect);
        }
    }
}
