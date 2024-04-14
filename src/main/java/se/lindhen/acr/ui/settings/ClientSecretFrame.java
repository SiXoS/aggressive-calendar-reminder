package se.lindhen.acr.ui.settings;

import se.lindhen.acr.Settings;
import se.lindhen.acr.ui.ScreenSelector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ClientSecretFrame extends JFrame implements MouseListener {

    private final JTextField textField;
    private final Settings settings;
    private final Runnable onClientSecretPicked;

    public ClientSecretFrame(Settings settings, Runnable onClientSecretPicked) {
        super("Aggressive Calendar Reminder | Client Secret");
        this.settings = settings;
        this.onClientSecretPicked = onClientSecretPicked;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel inner = new JPanel();
        inner.setBorder(new EmptyBorder(10, 10, 5, 10));
        inner.setLayout(new BoxLayout(inner, BoxLayout.X_AXIS));
        inner.add(new JLabel("Specify secret"));


        JPanel textFieldPanel = new JPanel();
        textFieldPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        textField = new JTextField(20);
        textField.setMinimumSize(new Dimension(100, 20));
        textFieldPanel.add(textField);

        JButton button = new JButton("Save");
        button.addMouseListener(this);

        inner.add(textFieldPanel);
        inner.add(button);
        add(inner);

        pack();

        setScreen(ScreenSelector.getDefaultScreen());

        setVisible(true);

    }

    public void setScreen(GraphicsDevice screen) {
        JFrame dummy = new JFrame(screen.getDefaultConfiguration());
        setLocationRelativeTo(dummy);
        dummy.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        settings.updateClientSecret(textField.getText().trim());
        onClientSecretPicked.run();
        dispose();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
