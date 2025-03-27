package com.example.servidor.gui;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class PrincipalSrv extends JFrame {
    private final int PORT = 12345;
    private JTextArea textArea;
    private JButton startButton, stopButton;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final Set<String> nombresClientes = new HashSet<>();
    private final Set<PrintWriter> clientesComunes = new HashSet<>();
    private PrintWriter adminOut = null;

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
            if (adminOut != null) {
                adminOut.println("SERVIDOR_CERRADO");
            }
            for (PrintWriter out : clientesComunes) {
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
        private boolean esAdmin;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Recibir nombre del cliente
                String linea = in.readLine();
                if (linea == null || linea.trim().isEmpty()) {
                    linea = "Cliente" + (int) (Math.random() * 1000);
                }

                if (linea.startsWith("ADMIN:")) {
                    esAdmin = true;
                    nombre = linea.substring(6);
                    adminOut = out;
                    textArea.append("Admin " + nombre + " conectado.\n");
                } else {
                    esAdmin = false;
                    nombre = generarNombreUnico(linea);
                    nombresClientes.add(nombre);
                    clientesComunes.add(out);
                    textArea.append(nombre + " se ha conectado.\n");

                    if (adminOut != null) {
                        adminOut.println(nombre + " se ha conectado.");
                    }
                }

                // Enviar el nombre final al cliente
                out.println("Tu nombre asignado: " + nombre);

                // Manejar los mensajes
                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    if (!running) break;

                    if (esAdmin) {
                        textArea.append("ADMIN -> Todos: " + mensaje + "\n");
                        for (PrintWriter cliente : clientesComunes) {
                            cliente.println("Admin " + nombre + ": " + mensaje);
                        }
                    } else {
                        textArea.append(nombre + " -> Admin: " + mensaje + "\n");
                        if (adminOut != null) {
                            adminOut.println(nombre + ": " + mensaje);
                        }
                    }
                }
            } catch (IOException e) {
                textArea.append("Error con el cliente " + nombre + "\n");
            } finally {
                if (out != null) {
                    clientesComunes.remove(out);
                }
                nombresClientes.remove(nombre);
                textArea.append(nombre + " se ha desconectado.\n");

                if (adminOut != null) {
                    adminOut.println(nombre + " se ha desconectado.");
                }
            }
        }

        private String generarNombreUnico(String nombreBase) {
            String nuevoNombre = nombreBase;
            int contador = 1;
            while (nombresClientes.contains(nuevoNombre)) {
                nuevoNombre = nombreBase + "_" + contador;
                contador++;
            }
            return nuevoNombre;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrincipalSrv().setVisible(true));
    }
}
