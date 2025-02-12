import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageSteganographyGUI {
    private JFrame frame;
    private JTextField messageField, passcodeField;
    private JLabel imageLabel, statusLabel;
    private File selectedFile;
    private BufferedImage img;

    public ImageSteganographyGUI() {
        frame = new JFrame("Image Steganography");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1));

        JButton selectImageButton = new JButton("Select Image");
        messageField = new JTextField();
        passcodeField = new JTextField();
        JButton encryptButton = new JButton("Encrypt");
        JButton decryptButton = new JButton("Decrypt");
        statusLabel = new JLabel("Status: Select an image");

        selectImageButton.addActionListener(e -> selectImage());
        encryptButton.addActionListener(e -> encryptMessage());
        decryptButton.addActionListener(e -> decryptMessage());

        panel.add(selectImageButton);
        panel.add(new JLabel("Enter Secret Message:"));
        panel.add(messageField);
        panel.add(new JLabel("Enter Passcode:"));
        panel.add(passcodeField);
        panel.add(encryptButton);
        panel.add(decryptButton);
        panel.add(statusLabel);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            try {
                img = ImageIO.read(selectedFile);
                ImageIcon icon = new ImageIcon(img.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
                statusLabel.setText("Selected: " + selectedFile.getName());
            } catch (IOException e) {
                statusLabel.setText("Error loading image.");
            }
        }
    }

    private void encryptMessage() {
        if (selectedFile == null || img == null) {
            statusLabel.setText("Please select an image first.");
            return;
        }

        String message = messageField.getText();
        String passcode = passcodeField.getText();

        if (message.isEmpty() || passcode.isEmpty()) {
            statusLabel.setText("Enter message and passcode.");
            return;
        }

        int width = img.getWidth();
        int height = img.getHeight();
        int n = 0, m = 0, z = 0;

        for (int i = 0; i < message.length(); i++) {
            int asciiValue = message.charAt(i);
            int rgb = img.getRGB(m, n);
            int[] colors = { (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF };

            colors[z] = asciiValue;
            int newRgb = (colors[0] << 16) | (colors[1] << 8) | colors[2];
            img.setRGB(m, n, newRgb);

            n = (n + 1) % height;
            m = (m + 1) % width;
            z = (z + 1) % 3;
        }

        try {
            File outputFile = new File("encryptedImage.jpg");
            ImageIO.write(img, "jpg", outputFile);
            statusLabel.setText("Message Encrypted! Saved as encryptedImage.jpg");
        } catch (IOException e) {
            statusLabel.setText("Error saving image.");
        }
    }

    private void decryptMessage() {
        if (selectedFile == null || img == null) {
            statusLabel.setText("Please select an encrypted image first.");
            return;
        }

        String enteredPasscode = passcodeField.getText();
        if (enteredPasscode.isEmpty()) {
            statusLabel.setText("Enter passcode for decryption.");
            return;
        }

        StringBuilder message = new StringBuilder();
        int width = img.getWidth();
        int height = img.getHeight();
        int n = 0, m = 0, z = 0;

        for (int i = 0; i < 100; i++) { // Max length assumption
            int rgb = img.getRGB(m, n);
            int[] colors = { (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF };

            if (colors[z] < 32 || colors[z] > 126) break; // Stop at non-printable characters

            message.append((char) colors[z]);

            n = (n + 1) % height;
            m = (m + 1) % width;
            z = (z + 1) % 3;
        }

        JOptionPane.showMessageDialog(frame, "Decrypted Message: " + message.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageSteganographyGUI::new);
    }
}