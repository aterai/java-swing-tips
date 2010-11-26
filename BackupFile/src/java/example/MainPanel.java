package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    private final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(0, 0, 6, 1));
    private final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(2, 0, 6, 1));
    private final JLabel label      = new JLabel("2", SwingConstants.RIGHT);
    private final JTextPane jtp     = new JTextPane();
    private final JButton ok        = new JButton("backup-testファイルを生成");
    public MainPanel() {
        super(new BorderLayout());
        jtp.setEditable(false);
        ok.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                int i1 = ((Integer)spinner1.getValue()).intValue();
                int i2 = ((Integer)spinner2.getValue()).intValue();
                try{
                    File file = new File(System.getProperty("java.io.tmpdir"), "backup-test");
                    if(!file.exists()) {
                        if(!file.createNewFile()) {
                            append(file.getName()+"の生成に失敗しました。", true);
                            return;
                        }
                        //file.deleteOnExit();
                        append(file.getName()+"を生成しました。", false);
                    }else{
                        File nf = makeBackupFile(file, i1, i2);
                        if(nf==null) {
                            append("バックアップファイルの生成に失敗しました。", true);
                        }else if(!nf.createNewFile()) {
                            append(nf.getName()+"の生成に失敗しました。", true);
                        }
                        //append(nf.getName()+"を更新しました。", false);
                    }
                }catch(IOException ioe) {
                    ioe.printStackTrace();
                    append("ファイルの生成に失敗しました。", true);
                }
                append("----------------------------------", true);
            }
        });
        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
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
                int i1 = ((Integer)spinner1.getValue()).intValue();
                int i2 = ((Integer)spinner2.getValue()).intValue();
                label.setText(""+(i1+i2));
            }
        };
        spinner1.addChangeListener(cl);
        spinner2.addChangeListener(cl);

        label.setBorder(BorderFactory.createEmptyBorder(0,0,0,16));

        //Box nbox = Box.createHorizontalBox();
        JPanel nbox = new JPanel(new GridLayout(3,2,5,5));
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
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }

    private File makeBackupFile(File file, int intold, int intnew) {
        File testFile = null;
        String newfilename = file.getAbsolutePath();
        if(intold==0 && intnew==0) {
            if(file.delete()) {
                return new File(newfilename);
            }else{
                append("古いバックアップファイル削除に失敗", true);
                return null;
            }
        }
        boolean testFileFlag = false;
        for(int i=1;i<=intold;i++) {
            testFile = new File(file.getParentFile(), file.getName()+"."+i+"~");
            if(!testFile.exists()) {
                testFileFlag = true;
                break;
            }
        }
        if(!testFileFlag) {
            for(int i=intold+1;i<=intold+intnew;i++) {
                testFile = new File(file.getParentFile(), file.getName()+"."+i+"~");
                if(!testFile.exists()) {
                    testFileFlag = true;
                    break;
                }
            }
        }
        if(testFileFlag) {
            if(file.renameTo(testFile)) {
                append("古い同名ファイルをリネーム", true);
                append("    "+file.getName()+" -> "+testFile.getName(), false);
            }else{
                append("ファイルのリネームに失敗", true);
                return null;
            }
        }else{
            File tmpFile3 = new File(file.getParentFile(), file.getName()+"."+(intold+1)+"~");
            append("古いパックアップファイルを削除", true);
            append("    del:"+tmpFile3.getAbsolutePath(),false);
            if(!tmpFile3.delete()) {
                append("古いバックアップファイル削除に失敗", true);
                return null;
            }
            for(int i=intold+2;i<=intold+intnew;i++) {
                File tmpFile1 = new File(file.getParentFile(), file.getName()+"."+i+"~");
                File tmpFile2 = new File(file.getParentFile(), file.getName()+"."+(i-1)+"~");
                if(!tmpFile1.renameTo(tmpFile2)) {
                    append("ファイルのリネームに失敗", true);
                    return null;
                }
                append("古いパックアップファイルの番号を更新", true);
                append("    "+tmpFile1.getName()+" -> "+tmpFile2.getName(), false);
            }
            File tmpFile = new File(file.getParentFile(), file.getName()+"."+(intold+intnew)+"~");
            append("古い同名ファイルをリネーム", true);
            append("    "+file.getName()+" -> "+tmpFile.getName(), false);
            if(!file.renameTo(tmpFile)) {
                append("ファイルのリネームに失敗", true);
                return null;
            }
        }
        return new File(newfilename);
    }

    private void append(final String str, final boolean flg) {
        SimpleAttributeSet sas = null;
        if(!flg) {
            sas = new SimpleAttributeSet();
            StyleConstants.setForeground(sas, Color.RED);
        }
        try{
            Document doc = jtp.getDocument();
            doc.insertString(doc.getLength(), str+"\n", sas);
            jtp.setCaretPosition(doc.getLength());
        }catch(BadLocationException e) { e.printStackTrace(); }
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
