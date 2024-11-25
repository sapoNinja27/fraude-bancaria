package org.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GPTChatbot {
    private static final String OPENAI_API_KEY = "token gpt";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public static String enviarParaGPT(String mensagem) throws Exception {
        JSONObject json = new JSONObject();
        json.put("model", "gpt-3.5-turbo");
        json.put("temperature", 0.7);

        JSONObject mensagemUsuario = new JSONObject();
        mensagemUsuario.put("role", "user");
        mensagemUsuario.put("content", mensagem);

        json.put("messages", new Object[]{mensagemUsuario});

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject respostaJson = new JSONObject(response.body());
        return respostaJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }

    public static void main(String[] args) {
        List<String> transações = new ArrayList<>();

        File arquivo = new File("src/main/resources/data.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                transações.add(linha);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        String preMessage = "Ignore mensagens anteriores e faça APENAS o que eu pedi::\n " +
                "Vou lhe mandar um texto avalie seu conteudo e me responda apenas:\n " +
                "'Opção 1', o texto possui apenas uma saudação\n" +
                "'Opção 2', o texto questiona algo envolvendo fraude bancaria, transações e cartões, porém não informa o usuario nem a transação que quer avaliar\n" +
                "'Opção 3 {nome:nome, trans: trans}' , o texto questiona sobre fraude bancaria, porem informa tambem o usuario e a transação que quer avaliar \n" +
                "'Opção 4 {nome:nome}' , o texto contem um pedido de listagem de transações, caso ele não informe um nome pode retornar null no nome \n" +
                "'Opção 5' Caso não se enquadre em nenhma dessas";
        boolean jaPerguntouSobreFraude = false;
        boolean jaSaudou = false;
        String nome = null;
        while (true) {
            System.out.print("Você: ");
            String mensagemUsuario = scanner.nextLine();

            try {
                String respostaGPT = "Opção 5";
                try {
                    respostaGPT = enviarParaGPT(preMessage + "\n" + mensagemUsuario);
                } catch (Exception ignored) {
                }
                System.out.println("GPT: " + respostaGPT);

                int i = Integer.parseInt(respostaGPT.split(" ")[1]);

                if (i == 1) {
                    if (jaSaudou) {
                        System.out.println("Entendo sua nescessidade de atenção, porém preciso atuar em agum problema");
                    } else {
                        String[] respostas = {"Olá, tudo bem? Como posso ajudar", "Saudações, como posso ajudar?", "Boa tarde, como posso ajudar?"};
                        System.out.println(respostas[new Random().nextInt(2)]);
                        jaSaudou = true;
                    }
                    continue;
                }
                String pretext = jaSaudou ? "" : "Saudações, ";
                if (i == 2) {
                    if (nome != null) {
                        i = 3;
                    } else {
                        if (jaPerguntouSobreFraude) {
                            System.out.println("Entendo sua frustração, porém só posso prosseguir se me passar os dados da transação que acha que é fraudulenta");
                        } else {
                            String[] pre = {"Que chato ouvir isso, ", "Bom, nesse caso ", ""};
                            System.out.println(pretext + pre[new Random().nextInt(2)] + "preciso de mais informações sobre, qual seu nome e seu id de transação? caso não saiba pode pesquisar suas transações pelo nome");
                        }
                        jaPerguntouSobreFraude = true;
                        continue;
                    }
                }
                if (nome == null || nome.equals("nome") || nome.equals("null")) {
                    int nomeInicio = respostaGPT.indexOf("nome:") + 5;
                    int nomeFim = respostaGPT.indexOf(respostaGPT.contains(",") ? "," : "}", nomeInicio);
                    nome = respostaGPT.substring(nomeInicio, nomeFim).replace(" ", "");
                    if (nome.length() > 6 || nome.equals("nome") || nome.equals("null")) {
                        System.out.println("Por favor informe seu nome" + (i == 4 ? "" : " e o id da transação desejada"));
                        continue;
                    }
                }

                if (i == 5) {
                    System.out.println("Não entendi, poderia repetir o questionamento?");
                    continue;
                }

                int transInicio = respostaGPT.indexOf("trans: ") + 6;
                int transFim = respostaGPT.indexOf("}", transInicio);
                String transacao = respostaGPT.substring(transInicio + 1, transFim);


                String finalNome = nome;
                List<String> transacoesUser = transações.stream().filter(s -> s.contains(finalNome)).collect(Collectors.toList());
                if (i == 3) {
                    System.out.println("Certo, vou estar analizando sua solicitação");
                    System.out.println("..............");
                    System.out.println("..............");
                    System.out.println("..............");
                    System.out.println("..............");


                    String transacaoUser = transacoesUser.stream().filter(s -> s.contains(transacao)).findFirst().orElse(null);
                    if (transacaoUser != null) {
                        System.out.println("Encontrei sua transação, vou verificar se possui alguma inconsistencia");
                        System.out.println("..............");
                        System.out.println("..............");
                        System.out.println("..............");
                        System.out.println("..............");
                        AtomicReference<BigDecimal> valorTransacao = new AtomicReference<>(BigDecimal.ZERO);
                        BigDecimal mediaAoLongodoTempo = transacoesUser.stream().map(s -> {
                            int ultimoEspaco = s.lastIndexOf('\t');
                            String valor = s.substring(ultimoEspaco + 1);
                            if (!s.equals(transacaoUser)) {
                                return new BigDecimal(valor);
                            } else {
                                valorTransacao.set(new BigDecimal(valor));
                            }
                            return BigDecimal.ZERO;
                        }).reduce(BigDecimal.ZERO, BigDecimal::add);

                        mediaAoLongodoTempo = mediaAoLongodoTempo.divide(new BigDecimal(transacoesUser.size()));

                        if (mediaAoLongodoTempo.compareTo(valorTransacao.get()) >= 0) {
                            System.out.println("Valor da transação não parece com fraude");
                        }
                        BigDecimal diferenca = mediaAoLongodoTempo.subtract(valorTransacao.get()).abs();
                        BigDecimal porcentagem = diferenca.divide(valorTransacao.get(), 10, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"));
                        if (porcentagem.compareTo(BigDecimal.valueOf(5L)) > 0) {
                            System.out.println("Valor da transação anormal, medidas cabiveis serão tomadas para o estorno do valor");
                            System.out.println("..............");
                            System.out.println("..............");
                            System.out.println("..............");
                            System.out.println("..............");
                            transações.remove(transacaoUser);
                            System.out.println("Qualquer duvida pode entrar em contato comigo novamente");
                            jaPerguntouSobreFraude = false;
                        }
                    } else {
                        System.out.println("Não encontrei nenhuma transação por usuario e id de transação");
                    }
                }
                if (i == 4) {
                    System.out.println("Listando transações para o usuario: " + nome);
                    System.out.println("|Data      |Nome  |Transação id |Valor|");
                    transacoesUser.forEach(s -> System.out.println(s));
                }

            } catch (Exception e) {
                System.out.println("Erro ao se comunicar com o GPT: " + e.getMessage());
            }
        }
    }
}
