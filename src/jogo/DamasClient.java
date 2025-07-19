// =================== DamasClient.java ===================
/**
 * Classe principal do cliente do jogo de Damas.
 * 
 * Esta aplicação se conecta ao servidor por meio de um socket TCP,
 * escuta mensagens vindas do servidor em uma thread separada
 * (como atualização do tabuleiro, mensagens de erro e turno),
 * e permite que o jogador envie comandos de movimento via terminal.
 * O cliente interpreta comandos como "MOVIMENTO", "SAIR" e responde de acordo.
 */


package jogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class DamasClient {
    // Endereço IP do servidor (pode ser alterado para "localhost" ou outro IP)
    private static final String SERVER_ADDRESS = "25.50.18.211";
    // Porta em que o servidor está escutando
    private static final int PORT = 5555;
    // Armazena se o jogador é BRANCAS ou PRETAS
    private static String meuTime;

    public static void main(String[] args) {
        try (
            // Estabelece conexão com o servidor via socket TCP
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            // Canal de saída: envia dados para o servidor
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // Canal de entrada: recebe dados do servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Scanner para ler comandos digitados pelo jogador
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Conectado ao servidor de Damas");

            // Cria uma thread para escutar mensagens do servidor em tempo real
            new Thread(() -> {
                try {
                    String serverMessage;
                    // Lê continuamente as mensagens vindas do servidor
                    while ((serverMessage = in.readLine()) != null) {
                        // Mensagem inicial: informa se o jogador é BRANCAS ou PRETAS
                        if (serverMessage.startsWith("INICIO")) {
                            meuTime = serverMessage.split(" ")[1];
                            System.out.println("Você está jogando com as peças " + meuTime);
                        }
                        // Atualiza o tabuleiro com o novo estado vindo do servidor
                        else if (serverMessage.startsWith("TABULEIRO")) {
                            String estado = serverMessage.substring(9); // Remove prefixo "TABULEIRO"
                            Tabuleiro tabuleiro = Tabuleiro.desserializar(estado);
                            tabuleiro.imprimirTabuleiro(); // Mostra o tabuleiro atualizado no terminal
                        }
                        // Indica de quem é a vez de jogar
                        else if (serverMessage.startsWith("TURNO")) {
                            System.out.println("Vez das: " + serverMessage.substring(5));
                        }
                        // Informa algum erro ocorrido durante o envio do comando
                        else if (serverMessage.startsWith("ERRO")) {
                            System.out.println(serverMessage.substring(5));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexão com o servidor perdida");
                    System.exit(0); // Encerra o programa em caso de erro na conexão
                }
            }).start();

            // Loop principal para o jogador digitar e enviar comandos
            while (true) {
                String comando = scanner.nextLine();
                if (comando.equalsIgnoreCase("SAIR")) {
                    break; // Encerra o jogo se o jogador digitar "SAIR"
                }
                // Envia o comando para o servidor, no formato "MOVIMENTO origem destino"
                out.println("MOVIMENTO " + comando);
            }

        } catch (IOException e) {
            System.out.println("Não foi possível conectar ao servidor: " + e.getMessage());
        }
    }
}
