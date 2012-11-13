package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel{
    private static final String ASTERISK_TITLEBAR = "unsaved";
    private final JTextArea textarea = new JTextArea();
    private final JButton saveButton = new JButton("save");
    private final JFrame frame;
    private final String title;
    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        this.title = frame.getTitle();
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                if(ASTERISK_TITLEBAR.equals(e.getPropertyName())) {
                    Boolean unsaved = (Boolean)e.getNewValue();
                    frame.setTitle(String.format("%s%s", unsaved ? "* " : "", title));
                }
            }
        });
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                System.out.println("windowClosing");
                maybeExit();
            }
            @Override public void windowClosed(WindowEvent e) {
                System.out.println("windowClosed");
                System.exit(0); //webstart
            }
        });
        textarea.setText("JFrame Conditional Close Test");
        textarea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                fireUnsavedFlagChangeEvent(true);
            }
            @Override public void removeUpdate(DocumentEvent e) {
                fireUnsavedFlagChangeEvent(true);
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });
        saveButton.setEnabled(false);
        saveButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent ae) {
                System.out.println("Save(dummy)");
                fireUnsavedFlagChangeEvent(false);
            }
        });
        add(new JScrollPane(textarea));
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("exit") {
            @Override public void actionPerformed(ActionEvent e) {
                maybeExit();
            }
        }));
        box.add(Box.createHorizontalStrut(5));
        box.add(saveButton);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private void maybeExit() {
        if(title.equals(frame.getTitle())) {
            System.out.println("The document has already been saved, exit without doing anything.");
            frame.dispose();
            return;
        }
        java.awt.Toolkit.getDefaultToolkit().beep();
        //String[] obj = {"unsaved documents", "Do you really want to exit?"};
        //int retValue = JOptionPane.showConfirmDialog(frame, obj, "Select an Option",
        //                                             JOptionPane.YES_NO_CANCEL_OPTION);
        Object[] options = { "Save", "Discard", "Cancel" };
        int retValue = JOptionPane.showOptionDialog(
            frame, "<html>Save: Exit & Save Changes<br>Discard: Exit & Discard Changes<br>Cancel: Continue</html>",
            "Exit Options", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if(retValue==JOptionPane.YES_OPTION) {
            System.out.println("exit");
            //boolean ret = dummyDocumentSaveMethod();
            //if(ret) { //saved and exit
            //    frame.dispose();
            //}else{ //error and cancel exit
            //    return;
            //}
            frame.dispose();
        }else if(retValue==JOptionPane.NO_OPTION) {
            System.out.println("Exit without save");
            frame.dispose();
        }else if(retValue==JOptionPane.CANCEL_OPTION) {
            System.out.println("Cancel exit");
        }
    }
    private void fireUnsavedFlagChangeEvent(boolean unsaved) {
        if(unsaved) {
            saveButton.setEnabled(true);
            firePropertyChange(ASTERISK_TITLEBAR, Boolean.FALSE, Boolean.TRUE);
        }else{
            saveButton.setEnabled(false);
            firePropertyChange(ASTERISK_TITLEBAR, Boolean.TRUE, Boolean.FALSE);
        }
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
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
