import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Servidor {
    private static final String USUARIOS_FILE = "usuarios.txt";
    private static final String STORAGE_DIR = "armazenamento/usuarios/";
    private static final String[] SUBPASTAS = {"PDF", "JPG", "TXT"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> TIPOS_VALIDOS = Set.of("pdf", "jpg", "jpeg", "txt");
    private static final String LOG_FILE = "servidor_log.txt";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor aguardando conexão...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado!");
                new Thread(new ClienteHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClienteHandler implements Runnable {
        private Socket socket;
        private String usuarioLogado = null;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream())) {

                while (true) {
                    String opcao = (String) entrada.readObject();

                    if (opcao.equals("1")) { // LOGIN
                        String usuario = (String) entrada.readObject();
                        String senha = (String) entrada.readObject();

                        if (verificarLogin(usuario, senha)) {
                            usuarioLogado = usuario;
                            saida.writeObject("Login efetuado com sucesso!");
                            prepararDiretorios(usuario);
                        } else {
                            saida.writeObject("Login inválido!");
                        }
                    } else if (opcao.equals("2")) { // CADASTRO
                        String usuario = (String) entrada.readObject();
                        String senha = (String) entrada.readObject();

                        if (cadastrarUsuario(usuario, senha)) {
                            saida.writeObject("Usuário cadastrado com sucesso!");
                        } else {
                            saida.writeObject("Usuário já existe!");
                        }
                    } else if (opcao.equals("3")) { // SAIR
                        break;
                    } else if (usuarioLogado == null) {
                        saida.writeObject("Erro: Você precisa fazer login primeiro!");
                    } else if (opcao.equals("4")) { // ENVIAR ARQUIVO
                        receberArquivo(entrada, saida, usuarioLogado);
                    } else if (opcao.equals("5")) { // FAZER DOWNLOAD
                        enviarArquivo(saida, entrada, usuarioLogado);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean verificarLogin(String usuario, String senha) throws IOException {
            File file = new File(USUARIOS_FILE);
            if (!file.exists()) return false;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] dados = line.split(",");
                    if (dados.length == 2 && dados[0].equals(usuario) && dados[1].equals(senha)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean cadastrarUsuario(String usuario, String senha) throws IOException {
            File file = new File(USUARIOS_FILE);
            if (!file.exists()) file.createNewFile();

            if (verificarLogin(usuario, senha)) return false;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(usuario + "," + senha);
                writer.newLine();
            }
            return true;
        }

        private void prepararDiretorios(String usuario) throws IOException {
            Path userPath = Paths.get(STORAGE_DIR, usuario);
            if (!Files.exists(userPath)) {
                Files.createDirectories(userPath);
                for (String sub : SUBPASTAS) {
                    Files.createDirectories(userPath.resolve(sub));
                }
            }
        }

        private void receberArquivo(ObjectInputStream entrada, ObjectOutputStream saida, String usuario) throws IOException, ClassNotFoundException {
            String subPasta = (String) entrada.readObject();
            String nomeArquivo = (String) entrada.readObject();
            String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();

            if (!TIPOS_VALIDOS.contains(extensao)) {
                saida.writeObject("Erro: Tipo de arquivo inválido!");
                return;
            }

            String caminhoArquivo = STORAGE_DIR + usuario + "/" + subPasta + "/" + nomeArquivo;
            try (FileOutputStream fileOutputStream = new FileOutputStream(caminhoArquivo)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytes = 0;
                while ((bytesRead = entrada.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                    if (totalBytes > MAX_FILE_SIZE) {
                        saida.writeObject("Erro: Arquivo excede o tamanho máximo permitido!");
                        return;
                    }
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
            logAtividade(usuario, "Upload", nomeArquivo);
            System.out.println("Arquivo salvo: " + caminhoArquivo);
        }

        private void enviarArquivo(ObjectOutputStream saida, ObjectInputStream entrada, String usuario) throws IOException, ClassNotFoundException {
            String subPasta = (String) entrada.readObject();
            String nomeArquivo = (String) entrada.readObject();
            String caminhoArquivo = STORAGE_DIR + usuario + "/" + subPasta + "/" + nomeArquivo;
            File file = new File(caminhoArquivo);

            if (file.exists()) {
                saida.writeObject("Arquivo encontrado! Iniciando download...");
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        saida.write(buffer, 0, bytesRead);
                    }
                }
                logAtividade(usuario, "Download", nomeArquivo);
            } else {
                saida.writeObject("Erro: Arquivo não encontrado!");
            }
        }

        private void logAtividade(String usuario, String operacao, String arquivo) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(usuario + " - " + operacao + " - " + arquivo + " - " + new Date());
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
