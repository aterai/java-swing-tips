package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String FILE_NAME = "example.txt";
    private final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(0, 0, 6, 1));
    private final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(2, 0, 6, 1));
    private final JLabel label      = new JLabel("2", SwingConstants.RIGHT);
    private final JTextPane jtp     = new JTextPane();
    private final JButton ok        = new JButton(new AbstractAction("Create new " + FILE_NAME) {
        @Override public void actionPerformed(ActionEvent e) {
            File file = new File(System.getProperty("java.io.tmpdir"), FILE_NAME);
            int i1 = ((Integer) spinner1.getValue()).intValue();
            int i2 = ((Integer) spinner2.getValue()).intValue();
            (new Task(file, i1, i2) {
                @Override protected void process(List<Message> chunks) {
                    for (Message m: chunks) {
                        append(m);
                    }
                }
                @Override public void done() {
                    try {
                        File nf = get();
                        if (nf == null) {
                            append(new Message("バックアップファイルの生成に失敗しました。", MessageType.ERROR));
                        } else if (nf.createNewFile()) {
                            append(new Message(nf.getName() + "を生成しました。", MessageType.REGULAR));
                        } else {
                            append(new Message(nf.getName() + "の生成に失敗しました。", MessageType.ERROR));
                        }
                    } catch (InterruptedException | ExecutionException | IOException ex) {
                        append(new Message(ex.getMessage(), MessageType.ERROR));
                    }
                    append(new Message("----------------------------------", MessageType.REGULAR));
                }
            }).execute();
        }
    });

    public MainPanel() {
        super(new BorderLayout());
        jtp.setEditable(false);
        StyledDocument doc = jtp.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = doc.addStyle(MessageType.REGULAR.toString(), def);
        //StyleConstants.setForeground(error, Color.BLACK);
        //Style error = doc.addStyle(ERROR, regular);
        StyleConstants.setForeground(doc.addStyle(MessageType.ERROR.toString(), regular), Color.RED);
        StyleConstants.setForeground(doc.addStyle(MessageType.BLUE.toString(),  regular), Color.BLUE);

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        box.add(Box.createHorizontalGlue());
        box.add(ok);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JButton(new AbstractAction("clear") {
            @Override public void actionPerformed(ActionEvent e) {
                jtp.setText("");
            }
        }));

        JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(spinner1, "0");
        editor1.getTextField().setEditable(false);
        spinner1.setEditor(editor1);

        JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spinner2, "0");
        editor2.getTextField().setEditable(false);
        spinner2.setEditor(editor2);

        ChangeListener cl = new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int i1 = ((Integer) spinner1.getValue()).intValue();
                int i2 = ((Integer) spinner2.getValue()).intValue();
                label.setText(String.valueOf(i1 + i2));
            }
        };
        spinner1.addChangeListener(cl);
        spinner2.addChangeListener(cl);

        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        //Box nbox = Box.createHorizontalBox();
        JPanel nbox = new JPanel(new GridLayout(3, 2, 5, 5));
        nbox.add(new JLabel("削除しないバックアップの数:", SwingConstants.RIGHT));
        nbox.add(spinner1);
        nbox.add(new JLabel("順に削除するバックアップの数:", SwingConstants.RIGHT));
        nbox.add(spinner2);
        nbox.add(new JLabel("合計バックアップ数:", SwingConstants.RIGHT));
        nbox.add(label);

        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getViewport().add(jtp);

        add(nbox, BorderLayout.NORTH);
        add(scroll);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private void append(Message m) {
        StyledDocument doc = jtp.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), m.text + "\n", doc.getStyle(m.type.toString()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

enum MessageType {
    REGULAR,
    ERROR,
    BLUE;
}

class Message {
    public final String text;
    public final MessageType type;
    public Message(String text, MessageType type) {
        this.text = text;
        this.type = type;
    }
}

class Task extends SwingWorker<File, Message> {
    private final File file;
    private final int intold;
    private final int intnew;
    public Task(File file, int intold, int intnew) {
        super();
        this.file = file;
        this.intold = intold;
        this.intnew = intnew;
    }
    @Override public File doInBackground() throws IOException {
         if (!file.exists()) {
             return file;
         }

         String newfilename = file.getAbsolutePath();

         if (intold == 0 && intnew == 0) { //= backup off
             if (file.delete()) {
                 return new File(newfilename);
             } else {
                 publish(new Message("古いバックアップファイル削除に失敗", MessageType.ERROR));
                 return null;
             }
         }

         File tmpFile = renameAndBackup(file, newfilename);
         if (tmpFile != null) {
             return tmpFile;
         }

         if (renameAndShiftBackup(file)) {
             return new File(newfilename);
         } else {
             return null;
         }
     }
    private File renameAndBackup(File file, String newfilename) throws IOException {
        boolean simpleRename = false;
        File testFile = null;
        for (int i = 1; i <= intold; i++) {
            testFile = new File(file.getParentFile(), makeBackupFileName(file.getName(), i));
            if (!testFile.exists()) {
                simpleRename = true;
                break;
            }
        }
        if (!simpleRename) {
            for (int i = intold + 1; i <= intold + intnew; i++) {
                testFile = new File(file.getParentFile(), makeBackupFileName(file.getName(), i));
                if (!testFile.exists()) {
                    simpleRename = true;
                    break;
                }
            }
        }
        if (simpleRename) {
            if (file.renameTo(testFile)) {
                publish(new Message("古い同名ファイルをリネーム", MessageType.REGULAR));
                publish(new Message("    " + file.getName() + " -> " + testFile.getName(), MessageType.BLUE));
                return new File(newfilename);
            } else {
                publish(new Message("ファイルのリネームに失敗", MessageType.ERROR));
                throw new IOException();
            }
        }
        return null;
    }
    private boolean renameAndShiftBackup(File file) {
        File tmpFile3 = new File(file.getParentFile(), makeBackupFileName(file.getName(), intold + 1));
        publish(new Message("古いバックアップファイルを削除", MessageType.REGULAR));
        publish(new Message("    del:" + tmpFile3.getAbsolutePath(), MessageType.BLUE));
        if (!tmpFile3.delete()) {
            publish(new Message("古いバックアップファイル削除に失敗", MessageType.ERROR));
            return false;
        }
        for (int i = intold + 2; i <= intold + intnew; i++) {
            File tmpFile1 = new File(file.getParentFile(), makeBackupFileName(file.getName(), i));
            File tmpFile2 = new File(file.getParentFile(), makeBackupFileName(file.getName(), i - 1));
            if (!tmpFile1.renameTo(tmpFile2)) {
                publish(new Message("ファイルのリネームに失敗", MessageType.ERROR));
                return false;
            }
            publish(new Message("古いバックアップファイルの番号を更新", MessageType.REGULAR));
            publish(new Message("    " + tmpFile1.getName() + " -> " + tmpFile2.getName(), MessageType.BLUE));
        }
        File tmpFile = new File(file.getParentFile(), makeBackupFileName(file.getName(), intold + intnew));
        publish(new Message("古い同名ファイルをリネーム", MessageType.REGULAR));
        publish(new Message("    " + file.getName() + " -> " + tmpFile.getName(), MessageType.BLUE));
        if (!file.renameTo(tmpFile)) {
            publish(new Message("ファイルのリネームに失敗", MessageType.ERROR));
            return false;
        }
        return true;
    }
    private static String makeBackupFileName(String name, int num) {
        return String.format("%s.%d~", name, num);
    }
}
