package GUI.src;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class LogPanel extends JPanel {
    private final JTextPane pane = new JTextPane();
    private final StyledDocument doc;

    public LogPanel() {
        setPreferredSize(new Dimension(320, 480));
        setLayout(new BorderLayout());
        pane.setEditable(false);
        doc = pane.getStyledDocument();
        add(new JScrollPane(pane), BorderLayout.CENTER);
    }

    public void log(String id, String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = pane.addStyle(id, null);
                if (style == null) {
                    style = pane.addStyle(id, null);
                    StyleConstants.setForeground(style, color);
                }

                doc.insertString(doc.getLength(), String.format("[%s] %s%n", id, message), style);
                pane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public void logSystem(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = pane.getStyle("SYS");

                if (style == null) {
                    style = pane.addStyle("SYS", null);
                    StyleConstants.setForeground(style, Color.BLUE);
                    StyleConstants.setBold(style, true);
                }

                doc.insertString(doc.getLength(), String.format("[SYSTEM] %s%n", message), style);
                pane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
}