# 🕹️ Jogo de Damas Online – Cliente/Servidor com Sockets TCP

## Autores:
* Herbert Andrade Nascimento
* Thiago Souza Oliveira

## 📌 Visão Geral

Este projeto implementa um jogo de damas online com arquitetura cliente-servidor utilizando sockets TCP. Dois clientes se conectam ao servidor e jogam em tempo real via linha de comando. O servidor mantém o estado do tabuleiro, alterna os turnos, valida os movimentos e envia atualizações aos jogadores.

## 🎯 Propósito

* Proporcionar uma experiência prática de redes TCP com programação concorrente em Java.
* Aplicar conceitos de protocolos de aplicação personalizados.
* Modelar um sistema interativo baseado em estados, eventos e troca de mensagens.

## 🚧 Funcionamento Geral do Software

* O servidor (DamasServer) escuta conexões TCP na porta 5555.
* Os clientes (DamasClient) se conectam e, ao haver dois jogadores, o servidor inicia o jogo.
* O jogo segue regras tradicionais de damas: movimentos diagonais, capturas obrigatórias, promoção a dama, alternância de turnos e vitória quando um jogador não possui peças ou movimentos.
* Toda a interação é textual e ocorre via terminal.
* Cada comando do cliente é interpretado pelo servidor, que responde com mensagens apropriadas.
* O estado do jogo (tabuleiro) é transmitido após cada jogada.

---

## 📡 Protocolo da Camada de Aplicação

### 📑 Formato das Mensagens

As mensagens trocadas entre cliente e servidor são strings de texto simples com palavras-chave seguidas de parâmetros, quando necessário.

| Mensagem           | Origem   | Destino  | Significado                                    |
| ------------------ | -------- | -------- | ---------------------------------------------- |
| CONNECT <nome>     | Cliente  | Servidor | (Opcional) requisita conexão inicial           |
| INICIO <cor>       | Servidor | Cliente  | Informa ao cliente sua cor (BRANCAS ou PRETAS) |
| MOVIMENTO A3 B4    | Cliente  | Servidor | Solicita movimentar peça de A3 para B4         |
| TABULEIRO <estado> | Servidor | Cliente  | Envia estado do tabuleiro serializado          |
| TURNO <cor>        | Servidor | Cliente  | Informa qual cor deve jogar                    |
| ERRO <motivo>      | Servidor | Cliente  | Informa erro de jogada inválida                |
| HIST               | Cliente  | Servidor | Solicita o histórico de jogadas                |
| HIST <jogada>      | Servidor | Cliente  | Retorna uma jogada do histórico                |
| VITORIA <cor>      | Servidor | Cliente  | Informa o vencedor do jogo                     |
| SAIR               | Cliente  | Servidor | Cliente solicita encerrar a conexão            |

---

### 🔄 Modelo de Estados e Eventos

* Estado INICIAL: servidor aguardando conexões.
* Estado EM\_PARTIDA: duas conexões ativas, jogo iniciado.
* Evento JOGADA\_RECEBIDA → Valida jogada → Atualiza tabuleiro → Alterna turno → Envia atualização.
* Estado TERMINADO: um jogador vence ou envia SAIR.

Exemplo de ciclo:

1. Cliente envia: MOVIMENTO 3A 4B
2. Servidor valida e executa jogada
3. Servidor envia:

   * TABULEIRO atualizado
   * TURNO PRETAS

---

## 📦 Estrutura do Projeto

* DamasServer.java → Cria socket TCP, aceita clientes e gerencia o jogo.
* ClientHandler.java → Thread que escuta comandos de um cliente e interage com o tabuleiro.
* DamasClient.java → Cliente CLI que envia comandos e exibe mensagens.
* ClienteSocket.java → Encapsula comunicação via socket para clientes que usam a UI separada.
* Tabuleiro.java → Lógica do jogo, regras, histórico e impressão do tabuleiro.

---

## ⚙️ Requisitos Mínimos de Funcionamento

* Java 8 ou superior
* Dois terminais ou dois computadores em rede
* Porta TCP 5555 aberta no firewall (se remoto)

---

## 📡 Por que usar TCP?

A escolha do protocolo TCP se baseia na necessidade de:

* Transmissão confiável e ordenada dos dados.
* Garantia de entrega de comandos de jogada.
* Conexão persistente para comunicação bidirecional.

UDP foi descartado por não garantir ordem ou entrega, o que comprometeria a integridade da partida.

---

## 🚀 Como Executar

1. Compile os arquivos:

   ```bash
   javac jogo/*.java
   ```

2. Inicie o servidor:

   ```bash
   java jogo.DamasServer
   ```

3. Inicie os dois clientes em terminais separados:

   ```bash
   java jogo.DamasClient
   ```

---

4. Tenha um bom jogo 😉
