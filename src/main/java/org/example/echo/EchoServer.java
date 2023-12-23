package org.example.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    private static final Map<Integer, PrintWriter> clientWriters = new LinkedHashMap<>();
    private static final Map<Integer, ClientHandler> clientHandlers = new LinkedHashMap<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static int clientCounter = 0;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Сервер запущен. Адрес - " + serverSocket.getLocalSocketAddress());
            System.out.println("Ожидание подключений...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Подключен новый клиент.");
                    clientCounter++;

                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientWriters.put(clientCounter, writer);

                    int clientPort = clientSocket.getPort();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientPort, clientCounter);
                    clientHandlers.put(clientCounter, clientHandler);
                    executorService.submit(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientHandlers.values().forEach(ClientHandler::closeClient);
            executorService.shutdown();
        }
    }

    static class ClientHandler implements Runnable {
        private final String clientName;
        private final Socket clientSocket;
        private final int clientId;

        public ClientHandler(Socket socket, int localPort, int clientId) {
            this.clientName = "Client-" + localPort;
            this.clientSocket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String message;
                while (true) {
                    try {
                        if (clientSocket.isClosed()) {
                            closeClient();
                            return;
                        } else {
                            message = reader.readLine();
                            if (message != null) {
                                System.out.printf("Получено сообщение от клиента: %s %s\n", clientName, message);
                                broadcastMessage(clientName + ": " + message);
                            }
                        }
                    } catch (IOException e) {
                        closeClient();
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter writer : clientWriters.values()) {
                writer.println(message);
                writer.flush();
            }
        }

        private void closeClient() {
            try {
                clientSocket.close();
                PrintWriter printWriter = clientWriters.remove(clientId);
                printWriter.close();
                ClientHandler clientHandler = clientHandlers.remove(clientId);
                System.out.printf("Клиент отключен: %s\n", clientHandler.clientName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
