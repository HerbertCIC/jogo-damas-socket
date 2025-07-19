/**
 * Classe responsável pela abstração da comunicação de rede do lado do cliente.
 * 
 * Estabelece a conexão com o servidor via socket TCP, configurando canais de envio
 * (PrintWriter) e recebimento (BufferedReader) de mensagens. Fornece métodos utilitários
 * para envio e recebimento de dados do servidor de forma simplificada.
 * 
 * Esta classe é usada pela interface do cliente (como DamasUI) para trocar informações
 * com o servidor sem precisar lidar diretamente com os detalhes do socket.
 */

package jogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteSocket {
    private Socket socket; // Socket TCP para comunicação com o servidor
    private PrintWriter out; // Canal de saída para enviar mensagens ao servidor
    private BufferedReader in; // Canal de entrada para receber mensagens do servidor

    /**
     * Construtor: estabelece a conexão com o servidor e inicializa os canais de entrada e saída.
     *
     * @param endereco Endereço IP ou hostname do servidor
     * @param porta Número da porta do servidor
     * @throws IOException Se não for possível conectar ou abrir os streams
     */
    public ClienteSocket(String endereco, int porta) throws IOException {
        socket = new Socket(endereco, porta); // Cria conexão com o servidor
        out = new PrintWriter(socket.getOutputStream(), true); // Inicializa o canal de envio
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Inicializa o canal de leitura
    }

    /**
     * Envia uma mensagem de texto para o servidor.
     *
     * @param msg Mensagem a ser enviada
     */
    public void enviarMensagem(String msg) {
        out.println(msg); // Envia a mensagem pelo canal de saída
    }

    /**
     * Aguarda e retorna uma linha recebida do servidor.
     *
     * @return Linha de texto enviada pelo servidor
     * @throws IOException Se ocorrer erro de leitura
     */
    public String receberMensagem() throws IOException {
        return in.readLine(); // Lê uma linha do canal de entrada
    }

    /**
     * Retorna o BufferedReader associado à entrada do socket.
     * Pode ser útil para leituras manuais mais avançadas.
     *
     * @return BufferedReader do socket
     */
    public BufferedReader getInput() {
        return in;
    }
}