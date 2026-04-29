package top.fifthlight.fabazel.tokenhelper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ModrinthTokenSaver {
    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("Modrinth Token Saver");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            frame.setLayout(new GridLayout(3, 2));
            frame.add(new JLabel("Token ID:"));
            var tokenIdField = new JTextField();
            frame.add(tokenIdField);
            frame.add(new JLabel("Token Secret:"));
            var tokenSecretField = new JPasswordField();
            frame.add(tokenSecretField);
            frame.add(new JButton("Save") {{
                addActionListener(e -> {
                    var tokenId = tokenIdField.getText();
                    var tokenSecret = new String(tokenSecretField.getPassword());
                    if (tokenId == null) {
                        JOptionPane.showMessageDialog(frame, "Token ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (tokenSecret.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Token Secret cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        var tokenBackend = TokenBackend.getDefault();
                        tokenBackend.saveToken(tokenId, tokenSecret);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }

                    frame.dispose();
                });
            }});
            frame.pack();

            frame.setVisible(true);
        });
    }
}
