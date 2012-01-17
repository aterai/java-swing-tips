package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new StarburstSVGMaker().makeUI());
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class StarburstSVGMaker {
    private final SpinnerNumberModel outer   = new SpinnerNumberModel(40, 10, 1000, 1);
    private final SpinnerNumberModel inner   = new SpinnerNumberModel(30, 10, 1000, 1);
    private final SpinnerNumberModel vcModel = new SpinnerNumberModel(20, 3,  100,  1);
    private final JSpinner spinner1          = new JSpinner(outer);
    private final JSpinner spinner2          = new JSpinner(inner);
    private final JSpinner vcSpinner         = new JSpinner(vcModel);
    private final JTextArea textArea         = new JTextArea();
    private final JTextField styleField      = new JTextField("stroke:none; fill:pink");
    private final JCheckBox check            = new JCheckBox("Antialias", true);
    private final JLabel label               = new JLabel();

    public JComponent makeUI() {
        initStar();
        ChangeListener cl = new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                initStar();
            }
        };
        spinner1.addChangeListener(cl);
        spinner2.addChangeListener(cl);
        vcSpinner.addChangeListener(cl);
        check.addChangeListener(cl);

        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        check.setHorizontalAlignment(SwingConstants.RIGHT);

        JTabbedPane tab = new JTabbedPane();
        tab.add("Preview", makePreviewPanel());
        tab.add("SVG",     makeSVGPanel());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(makePreferencesPanel(), BorderLayout.NORTH);
        panel.add(tab);
        //panel.setPreferredSize(new Dimension(320, 320));
        return panel;
    }
    private JComponent makePreviewPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(check, BorderLayout.SOUTH);
        p.add(new JScrollPane(label));
        return p;
    }
    private JComponent makePreferencesPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Preferences"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;

        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; p.add(new JLabel("Addendum Circle Radius:"), c);
        c.gridy   = 1; p.add(new JLabel("Dedendum Circle Radius:"), c);
        c.gridy   = 2; p.add(new JLabel("Count of Teeth:"), c);

        c.gridx   = 1;
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; p.add(spinner1,  c);
        c.gridy   = 1; p.add(spinner2,  c);
        c.gridy   = 2; p.add(vcSpinner, c);
        return p;
    }
    private JComponent makeSVGPanel() {
        JPanel sp = new JPanel(new BorderLayout(2,2));
        sp.add(new JLabel("style:"), BorderLayout.WEST);
        sp.add(styleField);
        sp.add(new JButton(new AbstractAction("set") {
            @Override public void actionPerformed(ActionEvent e) {
                initStar();
            }
        }), BorderLayout.EAST);

        JPanel p = new JPanel(new BorderLayout(5,5));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.add(sp, BorderLayout.SOUTH);
        p.add(new JScrollPane(textArea));

//         JLabel l = new JLabel("Starburst.svg(drag here)");
//         DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
//             l, DnDConstants.ACTION_MOVE, new MyDragGestureListener());
//         p.add(l, BorderLayout.NORTH);
        return p;
    }
    private void initStar() {
        int r1 = outer.getNumber().intValue();
        int r2 = inner.getNumber().intValue();
        int vc = vcModel.getNumber().intValue();
        boolean antialias = check.isSelected();
        //outer.setMinimum(r2+1);
        Path2D.Double star = makeStar(r1, r2, vc);
        label.setIcon(new StarIcon(star, antialias));
        String desc = String.format("addendum_circle_radius=\"%d\" dedendum_circle_radius =\"%d\" number_of_teeth=\"%dT\"", Math.max(r1,r2), Math.min(r1,r2), vc);
        StringBuilder sb = makeStarburstSvg(star.getPathIterator(null), Math.max(r1,r2)*2, styleField.getText().trim(), desc);

//         Font font = new Font("Monospace", Font.PLAIN, 200);
//         FontRenderContext frc = new FontRenderContext(null, true, true);
//         Shape copyright = new TextLayout("\u3042", font, frc).getOutline(null);
//         Rectangle r = copyright.getBounds();
//         AffineTransform at = AffineTransform.getTranslateInstance(0d, r.getHeight());
//         StringBuilder sb = makeStarburstSvg(copyright.getPathIterator(at), 200, styleField.getText().trim(), desc);

        textArea.setText(sb.toString());
    }
    private StringBuilder makeStarburstSvg(PathIterator pi, int sz, String style, String desc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
        sb.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">%n", sz, sz));
        sb.append(String.format("  <desc>%s</desc>%n", desc));
        sb.append("  <path d=\"");
        double[] c = new double[6];
        while(!pi.isDone()) {
            switch(pi.currentSegment(c)) {
              case PathIterator.SEG_MOVETO:
                sb.append(String.format("M%.2f,%.2f ", c[0], c[1])); break;
              case PathIterator.SEG_LINETO:
                sb.append(String.format("L%.2f,%.2f ", c[0], c[1])); break;
              case PathIterator.SEG_QUADTO:
                sb.append(String.format("Q%.2f,%.2f,%.2f,%.2f ", c[0], c[1], c[2], c[3])); break;
              case PathIterator.SEG_CUBICTO:
                sb.append(String.format("C%.2f,%.2f,%.2f,%.2f,%.2f,%.2f ", c[0], c[1], c[2], c[3], c[4], c[5])); break;
              case PathIterator.SEG_CLOSE:
                sb.append("Z"); break;
            }
            pi.next();
        }
        sb.append(String.format("\" style=\"%s\" />%n</svg>%n", style));
        return sb;
    }
    private Path2D.Double makeStar(int r1, int r2, int vc) {
        int or = Math.max(r1, r2);
        int ir = Math.min(r1, r2);
        double agl = 0.0;
        double add = 2*Math.PI/(vc*2);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(or*1, or*0);
        for(int i=0;i<vc*2-1;i++) {
            agl+=add;
            int r = i%2==0?ir:or;
            p.lineTo(r*Math.cos(agl), r*Math.sin(agl));
        }
        p.closePath();
        AffineTransform at = AffineTransform.getRotateInstance(-Math.PI/2,or,0);
        return new Path2D.Double(p, at);
    }
    private JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(title), BorderLayout.WEST);
        p.add(c);
        return p;
    }
//     class MyDragGestureListener implements DragGestureListener {
//         public void dragGestureRecognized(DragGestureEvent dge) {
//             File outfile;
//             try{
//                 outfile = File.createTempFile("starburst",".svg");
//                 FileWriter w = new FileWriter(outfile);
//                 w.writeData(textArea.getText());
//                 //outfile.deleteOnExit();
//             }catch(IOException ioe) {
//                 java.awt.Toolkit.getDefaultToolkit().beep();
//                 JOptionPane.showMessageDialog(null,"Could not create file.","Error",JOptionPane.ERROR_MESSAGE);
//                 return;
//             }
//             if(outfile==null) return;
//             final File tmpfile = outfile;
//             Transferable tran = new Transferable() {
//                 public Object getTransferData(DataFlavor flavor) {
//                     return Arrays.asList(tmpfile);
//                 }
//                 public DataFlavor[] getTransferDataFlavors() {
//                     return new DataFlavor[] { DataFlavor.javaFileListFlavor };
//                 }
//                 public boolean isDataFlavorSupported(DataFlavor flavor) {
//                     return flavor.equals(DataFlavor.javaFileListFlavor);
//                 }
//             };
//             DragSourceAdapter dsa = new DragSourceAdapter() {
//                 public void dragDropEnd(DragSourceDropEvent dsde) {
//                     if(dsde.getDropSuccess()) {
//                         System.out.println(dsde);
//                     }
//                 }
//             };
//             dge.startDrag(DragSource.DefaultMoveDrop, tran, dsa);
//         }
//     }
}
class StarIcon implements Icon{
    private final Shape star;
    private final boolean antialias;
    public StarIcon(Shape s, boolean a) {
        star = s;
        antialias = a;
    }
    @Override public int getIconWidth() {
        return star.getBounds().width;
    }
    @Override public int getIconHeight() {
        return star.getBounds().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(x, y);
        g2d.setPaint(Color.PINK);
        if(antialias) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.fill(star);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        //g2d.setPaint(Color.BLACK);
        //g2d.draw(star);
        g2d.translate(-x, -y);
    }
}
// class FileWriter{
//     private final File file;
//     public FileWriter(File file){
//         this.file = file;
//     }
//     public void writeData(String str) {
//         BufferedWriter bufferedWriter = null;
//         try{
//             bufferedWriter = new BufferedWriter(
//                 new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
//             bufferedWriter.write(str, 0, str.length());
//         }catch(IOException e){
//             e.printStackTrace();
//         }finally{
//             try{
//                 bufferedWriter.close();
//             }catch(IOException e){
//                 e.printStackTrace();
//             }
//         }
//     }
// }
