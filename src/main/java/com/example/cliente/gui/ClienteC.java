package com.example.cliente.gui;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteC extends JFrame {
    private final int PORT = 12345;
    private JTextArea textArea;
    private JTextField inputField;
    private JButton sendButton;
    private String nombre;
    private PrintWriter out;
    private Socket socket;

    public ClienteC() {
        setTitle("Cliente Común");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        nombre = JOptionPane.showInputDialog("Ingresa tu nombre:");
        if (nombre == null || nombre.trim().isEmpty()) nombre = "Cliente" + (int)(Math.random() * 1000);

        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), "Center");

        JPanel panel = new JPanel();
        inputField = new JTextField(20);
        sendButton = new JButton("Enviar");

        sendButton.addActionListener(e -> enviarMensaje());
        inputField.addActionListener(e -> enviarMensaje());

        panel.add(inputField);
        panel.add(sendButton);
        add(panel, "South");

        conectar();
    }

    private void conectar() {
        try {
            socket = new Socket("localhost", PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(nombre); // Enviar el nombre al servidor

            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        if ("SERVIDOR_CERRADO".equals(mensaje)) {
                            textArea.append("El servidor se ha caído.\n");
                            inputField.setEnabled(false);
                            sendButton.setEnabled(false);
                            break;
                        }
                        textArea.append(mensaje + "\n");
                    }
                } catch (IOException e) {
                    textArea.append("Conexión perdida.\n");
                    inputField.setEnabled(false);
                    sendButton.setEnabled(false);
                }
            }).start();
        } catch (IOException e) {
            textArea.append("Error al conectar con el servidor\n");
        }
    }

    private void enviarMensaje() {
        String mensaje = inputField.getText().trim();
        if (!mensaje.isEmpty() && out != null) {
            out.println(mensaje); // Enviar el mensaje al servidor
            textArea.append("Tú: " + mensaje + "\n"); // Mostrar el mensaje en la interfaz del cliente
            inputField.setText(""); // Limpiar el campo de texto

        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteC().setVisible(true));
    }
}

