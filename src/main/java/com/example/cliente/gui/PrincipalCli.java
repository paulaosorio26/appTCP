package com.example.cliente.gui;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PrincipalCli extends JFrame {
    private final int PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private JTextArea textArea;
    private JTextField inputField;
    private JButton sendButton;
    private String nombre;

    public PrincipalCli() {
        setTitle("Cliente Admin");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        nombre = JOptionPane.showInputDialog("Ingresa tu nombre de Administrador:");
        if (nombre == null || nombre.trim().isEmpty()) nombre = "Admin";

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
            out.println(nombre);
            textArea.append("Conectado como " + nombre + "\n");

            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        if ("SERVIDOR_CERRADO".equals(mensaje)) {
                            textArea.append("El servidor se ha caído.\n");
                            deshabilitarChat();
                            break;
                        }
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
            out.println(mensaje);
            textArea.append("Yo: " + mensaje + "\n");
            inputField.setText("");
        }
    }

    private void deshabilitarChat() {
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrincipalCli().setVisible(true));
    }
}

