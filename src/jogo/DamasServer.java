// =================== DamasServer.java ===================
/**
 * Classe principal do servidor do jogo de Damas.
 * 
 * Este servidor aguarda conexões de dois clientes e gerencia a lógica do jogo de damas,
 * incluindo o controle de turnos, o envio do estado do tabuleiro e a verificação de vitória.
 * Cada cliente conectado é tratado em uma thread separada através da classe interna ClientHandler,
 * que processa os comandos enviados pelos jogadores (como MOVIMENTO e HIST).
 * A comunicação com os clientes é feita por sockets, utilizando mensagens de texto.
 */

package jogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DamasServer {
    // Porta padrão onde o servidor vai escutar
    private static final int PORT = 5555;

    // Lista de todos os clientes conectados
    private static List<ClientHandler> clients = new ArrayList<>();

    // Instância do tabuleiro compartilhado entre os jogadores
    private static Tabuleiro tabuleiro = new Tabuleiro();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de Damas iniciado na porta " + PORT);

            // Loop infinito esperando conexões de clientes
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + socket);

                // Cria e inicia uma nova thread para lidar com o cliente
                ClientHandler clientThread = new ClientHandler(socket, tabuleiro);
                clients.add(clientThread);
                clientThread.start();

                // Quando os dois jogadores se conectarem, inicia o jogo
                if (clients.size() == 2) {
                    notifyGameStart();
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    // Informa os clientes sobre o início do jogo
    private static void notifyGameStart() {
        clients.get(0).sendMessage("INICIO BRANCAS");
        clients.get(1).sendMessage("INICIO PRETAS");
        broadcastTabuleiro();
        broadcastMessage("TURNO BRANCAS");
    }

    // Envia o estado atual do tabuleiro para todos os clientes
    public static void broadcastTabuleiro() {
        String estado = tabuleiro.serializar();
        for (ClientHandler client : clients) {
            client.sendMessage("TABULEIRO " + estado);
        }
    }

    // Envia uma mensagem genérica para todos os clientes
    public static void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}

// Classe que representa uma thread para cada cliente
//Responsavel por: recepção de comandos, validação de jogadas e verificação de vitória
class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Tabuleiro tabuleiro;

    public ClientHandler(Socket socket, Tabuleiro tabuleiro) {
        this.socket = socket;
        this.tabuleiro = tabuleiro;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Erro ao criar handler: " + e.getMessage());
        }
    }

    // Envia mensagem para o cliente conectado
    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            String inputLine;
            // Lê comandos do cliente em loop
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Comando recebido: " + inputLine);

                synchronized (tabuleiro) {
                    // Verifica se o jogo já terminou antes de processar
                    int vencedor = tabuleiro.verificarVencedor();
                    if (vencedor != 0) {
                        String ganhador = (vencedor == 1) ? "BRANCAS" : "PRETAS";
                        DamasServer.broadcastMessage("VITORIA " + ganhador);
                        break;
                    }

                    // Comando de movimento de peça
                    if (inputLine.startsWith("MOVIMENTO")) {
                        String[] partes = inputLine.split(" ");
                        if (partes.length < 3) {
                            sendMessage("ERRO Formato inválido! Use: '3A 4B'");
                            continue;
                        }
                        int[] origem = Tabuleiro.converterCoordenada(partes[1]);
                        int[] destino = Tabuleiro.converterCoordenada(partes[2]);

                        // Valida e executa o movimento
                        if (tabuleiro.validarMovimento(origem[0], origem[1], destino[0], destino[1])) {
                            tabuleiro.moverPeca(origem[0], origem[1], destino[0], destino[1]);
                            tabuleiro.registrarJogada(partes[1] + " " + partes[2]);

                            DamasServer.broadcastTabuleiro();

                            // Verifica novamente se alguém venceu
                            vencedor = tabuleiro.verificarVencedor();
                            if (vencedor != 0) {
                                String ganhador = (vencedor == 1) ? "BRANCAS" : "PRETAS";
                                DamasServer.broadcastMessage("VITORIA " + ganhador);
                                break;
                            } else {
                                DamasServer.broadcastMessage("TURNO " + (tabuleiro.isVezBrancas() ? "BRANCAS" : "PRETAS"));
                            }
                        } else {
                            sendMessage("ERRO Movimento inválido");
                        }
                    }
                    // Comando para recuperar o histórico de jogadas
                    else if (inputLine.equalsIgnoreCase("HIST")) {
                        List<String> historico = tabuleiro.getHistorico();
                        for (String jogada : historico) {
                            sendMessage("HIST " + jogada);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no handler: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }
}
