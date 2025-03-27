package com.example.cliente.gui;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class PrincipalCli extends JFrame {
    private static final int PORT = 12345;
    private JTextArea textArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private Socket socket;
    private String nombre;

    public PrincipalCli() {
        setTitle("Cliente Admin");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Solicitar nombre de administrador
        nombre = JOptionPane.showInputDialog("Ingresa tu nombre de administrador:");
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "Admin";
        }

        // Configurar la interfaz gráfica
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), "Center");

        JPanel panel = new JPanel();
        inputField = new JTextField(20);
        sendButton = new JButton("Enviar");

        sendButton.addActionListener(e -> enviarMensaje());

        panel.add(inputField);
        panel.add(sendButton);
        add(panel, "South");

        conectar();
    }

    private void conectar() {
        try {
            socket = new Socket("localhost", PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("ADMIN:" + nombre); // Indicar que es admin

            // Hilo para recibir mensajes del servidor
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        textArea.append(mensaje + "\n");
                    }
                } catch (IOException e) {
                    textArea.append("Conexión perdida.\n");
                }
            }).start();
        } catch (IOException e) {
            textArea.append("Error al conectar con el servidor\n");
        }
    }

    private void enviarMensaje() {
        String mensaje = inputField.getText().trim();
        if (!mensaje.isEmpty() && out != null) {
            out.println(mensaje); // Enviar mensaje al servidor
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrincipalCli().setVisible(true));
    }
}
