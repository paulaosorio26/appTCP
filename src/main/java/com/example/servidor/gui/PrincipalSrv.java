package com.example.servidor.gui;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class PrincipalSrv extends JFrame {
    private final int PORT = 12345;
    private JTextArea textArea;
    private JButton startButton, stopButton;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final Set<PrintWriter> clientes = new HashSet<>();

    public PrincipalSrv() {
        setTitle("Servidor");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), "Center");

        JPanel panel = new JPanel();
        startButton = new JButton("Iniciar Servidor");
        stopButton = new JButton("Detener Servidor");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> iniciarServidor());
        stopButton.addActionListener(e -> detenerServidor());

        panel.add(startButton);
        panel.add(stopButton);
        add(panel, "South");
    }

    private void iniciarServidor() {
        if (running) return;
        running = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                textArea.append("Servidor iniciado en el puerto " + PORT + "\n");

                while (running) {
                    Socket socket = serverSocket.accept();
                    new Thread(new ManejadorCliente(socket)).start();
                }
            } catch (IOException e) {
                textArea.append("Error en el servidor.\n");
            }
        }).start();
    }

    private void detenerServidor() {
        running = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        try {
            for (PrintWriter out : clientes) {
                out.println("SERVIDOR_CERRADO");
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
            textArea.append("Servidor detenido.\n");
        } catch (IOException e) {
            textArea.append("Error al detener el servidor.\n");
        }
    }

    private class ManejadorCliente implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String nombre;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clientes.add(out);

                nombre = in.readLine();
                textArea.append(nombre + " se ha conectado.\n");

                // Avisar a los demás clientes que alguien se conectó
                for (PrintWriter cliente : clientes) {
                    cliente.println(nombre + " se ha unido al chat.");
                }

                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    if (!running) break;
                    textArea.append("Mensaje recibido: " + mensaje + "\n");
                    for (PrintWriter cliente : clientes) {
                        cliente.println(mensaje);
                    }
                }
            } catch (IOException e) {
                textArea.append("Error con el cliente " + nombre + "\n");
            } finally {
                if (out != null) {
                    clientes.remove(out);
                }
                textArea.append(nombre + " se ha desconectado.\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrincipalSrv().setVisible(true));
    }
}

