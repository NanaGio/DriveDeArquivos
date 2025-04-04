import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            boolean sair = false;
            boolean loginValido = false;

            while (!sair) {
                System.out.println("\nBem vindo ao gerenciador de arquivos!");
                System.out.println("1 - Login");
                System.out.println("2 - Cadastrar usuário");
                System.out.println("3 - Sair");
                System.out.print("Digite a opção: ");
                String opcao = scanner.nextLine();
                saida.writeObject(opcao);

                switch (opcao) {
                    case "1":
                        System.out.print("Digite o usuário: ");
                        String usuario = scanner.nextLine();
                        saida.writeObject(usuario);

                        System.out.print("Digite a senha: ");
                        String senha = scanner.nextLine();
                        saida.writeObject(senha);

                        String resposta = (String) entrada.readObject();
                        System.out.println(resposta);
                        loginValido = resposta.equals("Login efetuado com sucesso!");

                        while (loginValido) {
                            System.out.println("1 - Enviar arquivo");
                            System.out.println("2 - Fazer download");
                            System.out.println("3 - Logout");
                            System.out.print("Digite a opção: ");
                            String opcaoMenu = scanner.nextLine();
                            saida.writeObject(opcaoMenu);

                            switch (opcaoMenu) {
                                case "1":
                                    System.out.println("Escolha a subpasta: 1 - PDF | 2 - JPG | 3 - TXT");
                                    String subPasta = scanner.nextLine();
                                    saida.writeObject(subPasta);

                                    System.out.print("Digite o caminho do arquivo: ");
                                    String caminhoArquivo = scanner.nextLine();
                                    enviarArquivo(caminhoArquivo, saida);
                                    break;
                                case "2":
                                    System.out.println("Escolha a subpasta: 1 - PDF | 2 - JPG | 3 - TXT");
                                    String subPastaDownload = scanner.nextLine();
                                    saida.writeObject(subPastaDownload);

                                    System.out.print("Digite o nome do arquivo: ");
                                    String nomeArquivo = scanner.nextLine();
                                    saida.writeObject(nomeArquivo);
                                    receberArquivo(nomeArquivo, entrada);
                                    break;
                                case "3":
                                    loginValido = false;
                                    System.out.println("Logout realizado.");
                                    break;
                                default:
                                    System.out.println("Opção inválida!");
                                    break;
                            }
                        }
                        break;
                    case "2":
                        System.out.print("Cadastrar login: ");
                        String novoUsuario = scanner.nextLine();
                        saida.writeObject(novoUsuario);

                        System.out.print("Cadastrar senha: ");
                        String novaSenha = scanner.nextLine();
                        saida.writeObject(novaSenha);
                        System.out.println(entrada.readObject());
                        break;
                    case "3":
                        sair = true;
                        System.out.println("Encerrando conexão...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void enviarArquivo(String caminhoArquivo, ObjectOutputStream saida) {
        File file = new File(caminhoArquivo);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Arquivo não encontrado!");
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            saida.writeObject(file.getName());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                saida.write(buffer, 0, bytesRead);
            }
            saida.flush();
            System.out.println("Arquivo enviado com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receberArquivo(String nomeArquivo, ObjectInputStream entrada) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(nomeArquivo)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = entrada.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("Arquivo recebido com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
