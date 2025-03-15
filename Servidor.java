import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Servidor {
    public static void main(String[] args) {
        boolean loginValido = false;
        String[] subPastas = {"PDF", "JPG", "TXT"};
        String[] usuario = new String[2];

        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Servidor aguardando conexão...");

            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado!");

            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());

            while (!loginValido) {
                String cadastrarUsuario = (String) entrada.readObject();
                usuario[0] = cadastrarUsuario;
                String cadastrarSenha = (String) entrada.readObject();
                usuario[1] = cadastrarSenha;

                String mensagemRecebida = (String) entrada.readObject();
                String mensagemRecebida1 = (String) entrada.readObject();

                if (mensagemRecebida.equals(usuario[0]) && mensagemRecebida1.equals(usuario[1])) {
                    String mensagemLogin = "Login efetuado com sucesso!";
                    saida.writeObject(mensagemLogin);
                    loginValido = true;
                } else {
                    String mensagemLogin = "Login inválido. Verifique seu usuário ou senha!";
                    saida.writeObject(mensagemLogin);
                }
            }

            if (loginValido) {
                System.out.printf("Login do cliente %s efetuado com sucesso!\n", usuario[0]);

                // Cria a pasta do usuário e subpastas (PDF, JPG, TXT)
                String folderPath = "armazenamento/usuarios/" + usuario[0];
                Path path = Path.of(folderPath);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    System.out.println("Pasta criada: " + path.toAbsolutePath());
                    for (String subPasta : subPastas) {
                        Path subPath = Path.of(folderPath + "/" + subPasta);
                        Files.createDirectories(subPath);
                        System.out.println("Subpasta criada: " + subPath.toAbsolutePath());
                    }
                } else {
                    System.out.println("A pasta já existe!");
                }

                while (true) {
                    // Recebe a operação (1 = enviar arquivo, 2 = fazer download)
                    String operacao = (String) entrada.readObject();

                    if (operacao.equals("1")) {
                        // Recebe a subpasta escolhida pelo cliente
                        String subPastaEscolhida = (String) entrada.readObject();
                        System.out.println("Subpasta escolhida: " + subPastaEscolhida);

                        // Recebe o nome do arquivo
                        String nomeArquivo = (String) entrada.readObject();
                        System.out.println("Recebendo arquivo: " + nomeArquivo);

                        // Define o caminho completo do arquivo
                        String caminhoArquivo = folderPath + "/" + subPastas[Integer.parseInt(subPastaEscolhida) - 1] + "/" + nomeArquivo;
                        System.out.println("Salvando arquivo em: " + caminhoArquivo);

                        // Cria um FileOutputStream para salvar o arquivo
                        FileOutputStream fileOutputStream = new FileOutputStream(caminhoArquivo);
                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        // Lê os bytes do arquivo e os escreve no disco
                        while ((bytesRead = entrada.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }

                        fileOutputStream.close();
                        System.out.println("Arquivo salvo com sucesso!");
                    } else if (operacao.equals("2")) {
                        // Fazer download
                        String subPastaEscolhida = (String) entrada.readObject();
                        String nomeArquivo = (String) entrada.readObject();

                        // Define o caminho completo do arquivo
                        String caminhoArquivo = folderPath + "/" + subPastas[Integer.parseInt(subPastaEscolhida) - 1] + "/" + nomeArquivo;
                        File file = new File(caminhoArquivo);

                        if (file.exists()) {
                            // Envia o nome do arquivo
                            saida.writeObject(file.getName());

                            // Envia o conteúdo do arquivo
                            FileInputStream fileInputStream = new FileInputStream(file);
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                saida.write(buffer, 0, bytesRead);
                            }
                            fileInputStream.close();
                            System.out.println("Arquivo enviado: " + nomeArquivo);
                        } else {
                            saida.writeObject("Arquivo não encontrado!");
                            System.out.println("Arquivo não encontrado: " + nomeArquivo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
