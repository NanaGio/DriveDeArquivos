import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try {
            boolean sair = false;
            boolean loginValido = false;

            Socket socket = new Socket("localhost", 12345);
            System.out.println("Conectado ao servidor!");

            ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);

            while (!sair) {
                System.out.println("Bem vindo ao gerenciador de arquivos!");
                System.out.println("1 - Login");
                System.out.println("2 - Cadastrar usuário");
                System.out.println("3 - Sair");
                System.out.print("Digite a opção: ");

                String opcao = scanner.nextLine();

                if (opcao.equals("1")) {
                    try {
                        System.out.println("====== LOGIN ======");
                        System.out.print("Digite o usuário: ");
                        String usuario = scanner.nextLine();
                        saida.writeObject(usuario);

                        System.out.print("Digite a senha: ");
                        String senha = scanner.nextLine();
                        saida.writeObject(senha);

                        String mensagemLogin = (String) entrada.readObject();
                        System.out.println(mensagemLogin);

                        if (mensagemLogin.equals("Login efetuado com sucesso!")) {
                            loginValido = true;
                            while (loginValido) {
                                System.out.println("1 - Enviar arquivo");
                                System.out.println("2 - Fazer download");
                                System.out.println("3 - Logout");
                                System.out.print("Digite a opção: ");

                                String opcaoMenu = scanner.nextLine();

                                if (opcaoMenu.equals("1")) {
                                    // Enviar arquivo
                                    System.out.println("Escolha a subpasta para enviar o arquivo:");
                                    System.out.println("1 - PDF");
                                    System.out.println("2 - JPG");
                                    System.out.println("3 - TXT");
                                    System.out.print("Digite a opção: ");
                                    String subPasta = scanner.nextLine();

                                    System.out.print("Digite o caminho do arquivo para enviar: ");
                                    String caminhoArquivo = scanner.nextLine();

                                    // Envia a operação (1 = enviar arquivo)
                                    saida.writeObject("1");
                                    // Envia a subpasta escolhida
                                    saida.writeObject(subPasta);
                                    // Envia o arquivo
                                    enviarArquivo(caminhoArquivo, saida);
                                } else if (opcaoMenu.equals("2")) {
                                    // Fazer download
                                    System.out.println("Escolha a subpasta para fazer download:");
                                    System.out.println("1 - PDF");
                                    System.out.println("2 - JPG");
                                    System.out.println("3 - TXT");
                                    System.out.print("Digite a opção: ");
                                    String subPasta = scanner.nextLine();

                                    System.out.print("Digite o nome do arquivo para fazer download: ");
                                    String nomeArquivo = scanner.nextLine();

                                    // Envia a operação (2 = fazer download)
                                    saida.writeObject("2");
                                    // Envia a subpasta escolhida
                                    saida.writeObject(subPasta);
                                    // Envia o nome do arquivo
                                    saida.writeObject(nomeArquivo);

                                    // Recebe o arquivo do servidor
                                    receberArquivo(nomeArquivo, entrada);
                                } else if (opcaoMenu.equals("3")) {
                                    loginValido = false;
                                    System.out.println("Logout realizado com sucesso!");
                                } else {
                                    System.out.println("Opção inválida!");
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (opcao.equals("2")) {
                    try {
                        System.out.println("====== CADASTRO ======");
                        System.out.print("Cadastrar login: ");
                        String cadastrarUsuario = scanner.nextLine();
                        saida.writeObject(cadastrarUsuario);

                        System.out.print("Cadastrar senha: ");
                        String cadastrarSenha = scanner.nextLine();
                        saida.writeObject(cadastrarSenha);

                        System.out.println("Cadastrado com sucesso!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (opcao.equals("3")) {
                    System.out.println("Finalizando conexão");
                    scanner.close();
                    sair = true;
                } else {
                    System.out.println("Opção inválida!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void enviarArquivo(String caminhoArquivo, ObjectOutputStream saida) {
        try {
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
                System.out.println("Arquivo enviado com sucesso!");
            } else {
                System.out.println("Arquivo não encontrado!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receberArquivo(String nomeArquivo, ObjectInputStream entrada) {
        try {
            // Salva o arquivo no diretório atual com o nome fornecido
            FileOutputStream fileOutputStream = new FileOutputStream(nomeArquivo);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = entrada.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
            System.out.println("Arquivo recebido com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}