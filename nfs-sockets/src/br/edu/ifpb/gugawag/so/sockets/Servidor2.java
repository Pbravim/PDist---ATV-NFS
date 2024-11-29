package br.edu.ifpb.gugawag.so.sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor2 {
    public static void main(String[] args) throws IOException {
        System.out.println("== Servidor ==");

        // Configurando o socket
        ServerSocket serverSocket = new ServerSocket(7001);

        // Loop para aguardar múltiplos clientes
        while (true) {
            Socket socket = serverSocket.accept();
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            System.out.println("Cliente: " + socket.getInetAddress());

            try {
                File diretorio = new File("/home/ifpb/temp/PDist");

                if (!diretorio.exists()) {
                    System.out.println("Criando diretorio");
                    diretorio.mkdirs();
                }

                while (true) { // Loop de comunicação com o cliente
                    try {
                        String mensagem = dis.readUTF();

                        switch (mensagem) {
                            case "readdir":
                                dos.writeUTF("Digite o nome do diretório ou vazio para o padrão");
                                String diretorioNome = dis.readUTF();
                                File diretorioRead = new File(diretorio, diretorioNome);
                                String listaArquivo = String.join(", ", diretorioRead.list());
                                dos.writeUTF(listaArquivo);
                                break;
                            case "rename":
                                dos.writeUTF("Digite o arquivo a ser renomeado");
                                String nomeArquivoRename = dis.readUTF();
                                dos.writeUTF("Digite o novo nome");
                                String novoNomeArquivo = dis.readUTF();
                                File arquivoRename = new File(diretorio, nomeArquivoRename);
                                File arquivoNovo = new File(diretorio, novoNomeArquivo);
                                if (arquivoNovo.exists())
                                    throw new java.io.IOException("Novo nome ja existe");
                                boolean success = arquivoRename.renameTo(arquivoNovo);
                                if (!success){
                                    throw new java.io.IOException("Arquivo não encontrado");
                                }
                                    dos.writeUTF("Arquivo renomeado com sucesso");
                                break;
                            case "create":
                                dos.writeUTF("Digite o arquivo a ser criado");
                                String nomeArquivoCreate = dis.readUTF();
                                File arquivoCreate = new File(diretorio, nomeArquivoCreate);
                                try {
                                    arquivoCreate.createNewFile();
                                    dos.writeUTF("Arquivo criado");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "remove":
                                dos.writeUTF("Digite o arquivo a ser removido");
                                String nomeArquivoRemove = dis.readUTF();
                                File arquivoRemove = new File(diretorio, nomeArquivoRemove);
                                if (!arquivoRemove.exists())
                                    throw new java.io.IOException("Arquivo não encontrado");
                                try {
                                    arquivoRemove.delete();
                                    dos.writeUTF("Arquivo apagado");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                dos.writeUTF("Comando inválido");
                                break;
                        }
                    } catch (EOFException e) {
                        System.out.println("Cliente desconectou: " + socket.getInetAddress());
                        break; // Encerra o loop de comunicação quando o cliente desconectar
                    } catch (IOException e) {
                        System.out.println("Erro de comunicação: " + e.getMessage());
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Erro no servidor: " + e.getMessage());
            } finally {
                // Fechar o socket e os streams
                try {
                    dis.close();
                    dos.close();
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar o socket: " + e.getMessage());
                }
            }
        }
    }
}
