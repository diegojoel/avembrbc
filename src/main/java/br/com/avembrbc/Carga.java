package br.com.avembrbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class Carga {

    static final String SAN = "Embrapa. Ageitec Sanidade e DOC 107 (frango colonial).";
    static final String IMUNO = "Embrapa. Ageitec Doenças imunossupressoras.";
    static final String PROD = "Embrapa. Ageitec Doenças da produção.";
    static final String ASCITE = "Embrapa. O que é e como reduzir a ascite em frangos.";
    static final String CALOR = "Embrapa Suínos e Aves. Instrução técnica: estresse calórico (Abreu).";
    static final String DENS = "Embrapa. Alta densidade na produção de frangos de corte.";
    static final String NH3 = "Embrapa. Emissão de amônia na produção de frangos de corte.";
    static final String AMB = "Embrapa. Desafios da ambiência nos sistemas de produção de aves.";
    static final String EVOL = "Embrapa Suínos e Aves. Papel na evolução da avicultura.";
    static final String NUT = "Embrapa. Krabbe e Silva: ações da avicultura sustentável.";
    static final String DOC107 = "Embrapa. Documentos 107: manejo de frango de corte colonial.";
    static final String INC = "Embrapa Meio-Norte. Documentos 261 e Ageitec Incubação.";
    static final String AMBI = "Embrapa. Manejo ambiental na avicultura.";
    static final String MAREK = "Embrapa. AterMais Digital: controle de doenças e pragas.";

    private Carga() {
    }

    static void carregar(Connection c) throws SQLException {
        sanidade(c);
        ambiencia(c);
        nutricao(c);
        manejo(c);
        incubacao(c);
        biosseguridade(c);
        Massa.carregar(c);
        dicionario(c);
        pares(c);
    }

    private static void sanidade(Connection c) throws SQLException {
        Bloco b = Bloco.abrir(c, "Sanidade",
                new String[] {"Espécie", "TEXTO", "2"},
                new String[] {"Idade (dias)", "NUMERICO", "3"},
                new String[] {"Sistema", "TEXTO", "1"},
                new String[] {"Quadro", "TEXTO", "5"},
                new String[] {"Sinal", "TEXTO", "4"});
        String[] idades = {"14", "21", "28", "35"};
        for (String idade : idades) {
            b.caso(c, SAN,
                    "Coccidiose em frango de corte aos " + idade + " dias: vacinar na primeira semana, via água de bebida ou aspersão, "
                            + "e manter a cama seca, trocando os trechos úmidos ou encrostados. Medicação só com o veterinário responsável.",
                    "Frango de corte", idade, "Convencional",
                    "fezes com sangue e aves amontoadas", "lesão no intestino");
            b.caso(c, SAN,
                    "Coccidiose em galinha caipira aos " + idade + " dias, em sistema colonial: aplicar a vacina na primeira semana "
                            + "e reduzir a umidade da cama e do piquete em volta do abrigo. Separar as aves muito afetadas.",
                    "Galinha caipira", idade, "Colonial",
                    "fezes com sangue no pasto e no galpão", "aves arrepiadas");
        }
        b.caso(c, SAN,
                "Coccidiose em poedeira: revisar o calendário vacinal e a cama ou a esteira de dejetos. Queda de postura com sangue nas fezes "
                        + "pede apoio do responsável técnico antes de qualquer tratamento.",
                "Poedeira", "180", "Postura em gaiola",
                "queda de postura com fezes sanguinolentas", "crista pálida");
        for (String idade : new String[] {"18", "28", "42"}) {
            b.caso(c, SAN,
                    "Suspeita de Newcastle em frango de corte aos " + idade
                            + " dias: é doença de notificação obrigatória. Comunicar o serviço veterinário oficial, isolar o lote "
                            + "e restringir pessoas e veículos. A vacina das semanas 2, 4 e 9 não substitui a notificação.",
                    "Frango de corte", idade, "Convencional",
                    "mortalidade alta com torcicolo e dificuldade respiratória", "sinais nervosos");
        }
        b.caso(c, SAN,
                "Suspeita de Newcastle em galinha caipira: notificar o serviço veterinário oficial, suspender visitas e fechar o trânsito de aves. "
                        + "O calendário de caipira prioriza Newcastle, Marek, Gumboro, bronquite e bouba.",
                "Galinha caipira", "60", "Colonial",
                "torcicolo, diarréia e morte súbita no piquete", "aves caídas");
        for (String idade : new String[] {"16", "24"}) {
            b.caso(c, SAN,
                    "Doença de Gumboro em frango aos " + idade
                            + " dias: vacina viva por via ocular ou na água na 2ª, 4ª e 9ª semana, só em ave sadia no dia da aplicação. "
                            + "Controlar o cascudinho, que funciona como reservatório do vírus. O quadro imunossuprime e derruba outras vacinas.",
                    "Frango de corte", idade, "Dark house",
                    "prostração e diarréia esbranquiçada", "bolsa cloacal alterada");
        }
        b.caso(c, MAREK,
                "Doença de Marek: a vacina é obrigatória ainda no incubatório. Pintinho que chega com paralisia de perna ou asa não se trata no galpão; "
                        + "não misturar idades e exibir a origem vacinal do lote.",
                "Frango de corte", "12", "Convencional",
                "ave sentada com uma perna esticada", "paralisia de asa");
        b.caso(c, DOC107,
                "Bouba aviária em galinha caipira: vacinar conforme o calendário e reduzir mosquitos e ferimentos na crista e na barbela. "
                        + "Afastar as aves com crostas para não espalhar o vírus no piquete.",
                "Galinha caipira", "70", "Colonial",
                "crostas na crista, barbela e patas", "nódulos na pele");
        b.caso(c, DOC107,
                "Bronquite infecciosa em frango: entra no mesmo calendário de vacina viva da 2ª, 4ª e 9ª semana, junto de Gumboro e Newcastle. "
                        + "Baixar a amônia, porque o quadro respiratório piora com cama úmida e ar parado.",
                "Frango de corte", "25", "Convencional",
                "estertor e queda de desempenho sem sinal nervoso", "conjuntivite");
        b.caso(c, SAN,
                "Salmonela em frango: não reutilizar cama de lote suspeito. Limpar e desinfetar o galpão, controlar ratos e revisar farinha de origem animal na ração. "
                        + "Salmonelose é de notificação obrigatória ao serviço veterinário oficial.",
                "Frango de corte", "30", "Convencional",
                "mortalidade com cama reutilizada e ração de origem animal", "fezes soltas");
        b.caso(c, SAN,
                "Salmonela em poedeira: notificar o serviço oficial, recolher ovos do lote suspeito conforme a orientação veterinária e não repor cama contaminada. "
                        + "Ratos no depósito de ração mantêm a bactéria entre ciclos.",
                "Poedeira", "220", "Postura em gaiola",
                "queda de postura e diarréia em lote de ovos", "ovos sujos de fezes");
        b.caso(c, IMUNO,
                "Micotoxicose por aflatoxina: retirar a ração mofada ou empedrada, secar o depósito e só então recolocar alimento. "
                        + "A toxina derruba o ganho de peso e anula a resposta à vacina. O balanceamento da fase deve cobrir as vitaminas A, D, E e a biotina.",
                "Frango de corte", "28", "Dark house",
                "baixo ganho e vacina que não protege, com ração mofada", "fígado alterado");
        b.caso(c, IMUNO,
                "Micotoxicose em poedeira: ração empedrada e queda de postura pedem troca imediata do alimento e revisão da umidade do silo. "
                        + "Não contar com a vacina enquanto o lote seguir ingerindo grão úmido.",
                "Poedeira", "200", "Postura em gaiola",
                "queda de postura com ração empedrada", "crista pálida e refugo");
        b.caso(c, SAN,
                "Cascudinho no aviário: o besouro guarda vírus de Gumboro e de Marek e também salmonela. Piso de concreto melhora a lavagem e a desinfecção entre lotes. "
                        + "Chão batido tem desempenho zootécnico parecido, mas pior controle do inseto.",
                "Frango de corte", "32", "Convencional",
                "cama velha com cascudinho e pintinho refugo", "lesão de pele e inquietação");
        b.caso(c, PROD,
                "Celulite em frango no fim do lote: reduzir arranhão por densidade, cama úmida e captura apressada. A lesão de pele abre porta para Escherichia coli. "
                        + "Uniformizar o lote e não empilhar ave na apanha.",
                "Frango de corte", "40", "Dark house",
                "pele do abdômen amarelada depois de arranhão", "condenação de carcaça");
        b.caso(c, CALOR,
                "Onfalite na chegada: umbigo úmido vem do incubatório e do resfriamento na viagem. Não misturar esses pintinhos ao lote sadio. "
                        + "Na primeira semana a zona de conforto é 32 a 35 ºC, inclusive na cama.",
                "Frango de corte", "3", "Convencional",
                "mortalidade na chegada com umbigo úmido", "pintinho fraco");
        b.caso(c, DOC107,
                "Aspergilose em pintinho: não usar cama mofada, poeira de serra de móvel nem material acima de cerca de 20% de umidade. "
                        + "Partícula média, seca, sem produto químico. Trocar o lote de cama antes de alojar de novo.",
                "Frango de corte", "5", "Convencional",
                "dificuldade respiratória com cama embolorada", "nódulo no pulmão");
        b.caso(c, SAN,
                "Suspeita de micoplasmose: é de notificação obrigatória. Não tratar o lote por conta para 'apagar' o status sanitário. "
                        + "Comprar ave de origem livre e separar o quadro respiratório crônico de bronquite e de amônia alta.",
                "Poedeira", "160", "Postura em gaiola",
                "estertor crônico e queda de postura", "seios da face inchados");
        b.caso(c, SAN,
                "Suspeita de influenza aviária: notificar imediatamente o serviço veterinário oficial. Isolar o galpão, suspender visitas e não mover aves. "
                        + "No ciclo seguinte, telar as laterais com malha de no máximo 2 cm para barrar ave silvestre.",
                "Frango de corte", "27", "Convencional",
                "mortalidade súbita alta com crista arroxeada e ave silvestre no galpão", "edema de cabeça");
        b.caso(c, DOC107,
                "Pododermatite por cama compactada: trocar os trechos encrostados. A cama fica entre 5 e 10 cm, seca, até a saída do lote. "
                        + "Umidade na cama espalha doença e lesiona pata e peito.",
                "Frango de corte", "38", "Convencional",
                "calo de pata e peito em cama úmida e compactada", "mancando");
        b.caso(c, ASCITE,
                "Ascite em frango macho: não forçar o crescimento nas duas primeiras semanas, manter a temperatura estável (oscilação menor que 2 ºC) "
                        + "e a amônia abaixo de cerca de 11 ppm. Sódio da ração entre 0,16% e 0,20%. Evitar alojar macho acima de 1.500 m se o histórico de barriga d'água for alto. "
                        + "A medida reduz perda, não apaga a predisposição genética.",
                "Frango de corte", "28", "Convencional",
                "abdômen dilatado com líquido em macho", "morte com barriga d'água");
        b.caso(c, PROD,
                "Síndrome da morte súbita: ave pesada achada de barriga para cima, perto do comedouro, sem lesão evidente. O diagnóstico é por exclusão. "
                        + "Reduzir pico de consumo e exigência metabólica com o programa de luz e sem empurrar o ganho no calor.",
                "Frango de corte", "33", "Dark house",
                "ave morta de barriga para cima junto do comedouro", "sem lesão aparente");
    }

    private static void ambiencia(Connection c) throws SQLException {
        Bloco b = Bloco.abrir(c, "Ambiência",
                new String[] {"Espécie", "TEXTO", "2"},
                new String[] {"Idade (dias)", "NUMERICO", "3"},
                new String[] {"Temperatura (ºC)", "NUMERICO", "4"},
                new String[] {"Umidade (%)", "NUMERICO", "2"},
                new String[] {"Amônia (ppm)", "NUMERICO", "3"},
                new String[] {"Quadro", "TEXTO", "4"});
        int[] idades = {4, 10, 18, 25, 32, 40};
        int[] min = {32, 29, 26, 23, 20, 20};
        int[] max = {35, 32, 29, 26, 23, 20};
        for (int i = 0; i < idades.length; i++) {
            int quente = max[i] + 8;
            b.caso(c, CALOR,
                    "Aos " + idades[i] + " dias a faixa de conforto da Embrapa é " + min[i] + " a " + max[i]
                            + " ºC. Temperatura acima disso: ventilação e, em túnel, 2 a 2,5 m/s com nebulização ou pad cooling. Água fresca à vontade. "
                            + "Pintinho depende da temperatura do ar e da cama.",
                    "Frango de corte", String.valueOf(idades[i]), String.valueOf(quente), "60", "10",
                    "aves ofegantes com asas abertas e queda de consumo");
            int frio = Math.max(14, min[i] - 6);
            b.caso(c, CALOR,
                    "Aos " + idades[i] + " dias a faixa de conforto é " + min[i] + " a " + max[i]
                            + " ºC. Abaixo disso a ave amontoa e queima energia para aquecer. Usar círculo de proteção. "
                            + "Lenha, briquete e GLP deram desempenho parecido na Embrapa, desde que a zona do pintinho fique na faixa.",
                    "Frango de corte", String.valueOf(idades[i]), String.valueOf(frio), "65", "8",
                    "pintinhos amontoados e piando");
        }
        b.caso(c, NH3,
                "Amônia acima de 20 ppm já passa o teto usado por exportadores; acima de cerca de 60 ppm a ave fica predisposta a doença respiratória, "
                        + "e perto de 100 ppm cai a troca gasosa. Ventilar sem jogar corrente em cima do pintinho. Cama úmida e reutilizada segura o gás, sobretudo no fim do lote.",
                "Frango de corte", "35", "24", "70", "45",
                "olho irritado e chiado com cheiro forte de amônia");
        b.caso(c, NH3,
                "Pico de amônia no início do lote sobre cama reusada: adiar o aquecimento fechado sem renovação de ar. "
                        + "Medir o gás antes de vacinar, porque mucosa irritada e imunossupressão pioram a resposta.",
                "Frango de corte", "8", "32", "68", "70",
                "pintinho lacrimejando na primeira semana sobre cama velha");
        b.caso(c, AMB,
                "Umidade do ar muito acima da faixa de 60 a 70% citada para as primeiras semanas, com cama molhada por bebedouro. "
                        + "Trocar a placa úmida, regular a altura do nippel e ventilar. Cama úmida dissemina coccidiose e lesão de pata.",
                "Frango de corte", "22", "28", "88", "25",
                "cama encharcada debaixo do bebedouro");
        b.caso(c, DENS,
                "Densidade alta em galpão convencional, sem climatização, no calor: de 10 aves/m² pode-se ir a 15–18 com ambiente e equipamento adaptados. "
                        + "Perto de 22 aves/m² exige alta tecnologia. Sem conforto térmico a mortalidade sobe junto com o calor produzido pelo lote.",
                "Frango de corte", "36", "33", "65", "18",
                "mortalidade no verão em galpão lotado sem exaustor");
        b.caso(c, EVOL,
                "Cobertura quente sem forro: a Embrapa viu melhor desempenho, conforto e menor custo quando há forro. "
                        + "Tinta reflexiva, isolante na cobertura e árvore ou grama em volta reduzem a carga térmica.",
                "Frango de corte", "30", "34", "55", "12",
                "lote ofegante em galpão sem forro no meio da tarde");
        b.caso(c, EVOL,
                "Consumo desregulado pela luz: programa intermitente no inverno-primavera e quase contínuo no verão-outono, com cortina amarela ou azul, "
                        + "foi prática avaliada pela Embrapa para ajustar o consumo de ração.",
                "Frango de corte", "29", "26", "60", "10",
                "consumo alto e cama úmida com luz contínua no inverno");
    }

    private static void nutricao(Connection c) throws SQLException {
        Bloco b = Bloco.abrir(c, "Nutrição",
                new String[] {"Espécie", "TEXTO", "2"},
                new String[] {"Idade (dias)", "NUMERICO", "2"},
                new String[] {"Quadro", "TEXTO", "5"},
                new String[] {"Ração", "TEXTO", "4"});
        b.caso(c, NUT,
                "Ovo de casca fina: revisar cálcio, fósforo e vitamina D da fase de postura. Calor e micotoxina também afinam a casca. "
                        + "Não compensar só com calcário jogado no comedouro sem reformular a ração.",
                "Poedeira", "210", "ovos quebrados e casca fina", "ração de postura com pouco cálcio");
        b.caso(c, NUT,
                "Alimento alternativo derrubando o ganho: o fator antinutricional piora energia e proteína. Enzima exógena, probiótico ou prebiótico "
                        + "melhoram o aproveitamento. A Embrapa associa enzima à menor excreção de nitrogênio e fósforo.",
                "Frango de corte", "21", "conversão ruim depois da troca por ingrediente alternativo", "ração com farelo alternativo sem enzima");
        b.caso(c, NUT,
                "Cama muito rica em fósforo: incluir fitase e revisar o fósforo disponível em vez de subir o fosfato. "
                        + "Menos fósforo na excreta reduz o potencial poluidor do resíduo, que a Embrapa trata no plano de manejo de nutrientes.",
                "Frango de corte", "35", "cama escura e úmida com histórico de fósforo alto na ração", "ração sem fitase");
        b.caso(c, IMUNO,
                "Ração úmida no silo: interromper o trato, ventilar o depósito e não devolver sobra mofada ao comedouro. "
                        + "Aflatoxina, fumonisina e tricotecenos imunossuprimem. Grão deve entrar seco.",
                "Frango de corte", "19", "refugo depois de silo com ração empedrada", "milho úmido");
        b.caso(c, ASCITE,
                "Sede excessiva e cama molhada por sal: sódio da ração entre 0,16% e 0,20%. Na água, tolerar até cerca de 50 ppm de sódio. "
                        + "Excesso de sódio entra na lista de fatores de ascite da Embrapa.",
                "Frango de corte", "20", "aves sedentas e cama molhada sem vazamento de bebedouro", "ração salgada");
        b.caso(c, CALOR,
                "Queda de consumo no calor: a ave come menos quando passa da zona de conforto. Garantir água fresca e oferecer a ração nas horas mais frias. "
                        + "Concentrar nutriente sem antes baixar a temperatura não recupera o lote.",
                "Frango de corte", "34", "comedouro cheio e ave ofegante no meio do dia", "ração de crescimento");
        b.caso(c, IMUNO,
                "Falha vacinal com lote desuniforme: checar vitaminas A, D, E, biotina e os minerais ferro e zinco da fase, além de micotoxina. "
                        + "Nutrição fraca é fator de imunossupressão descrito pela Embrapa, junto com Gumboro, Marek e calor.",
                "Frango de corte", "15", "lote vacinado que adoece na semana seguinte", "pré-inicial abaixo do balanceamento");
        b.caso(c, DOC107,
                "Desperdício de ração no pintinho: partícula grossa demais para a primeira semana aumenta sobra na cama e atrai cascudinho e rato. "
                        + "Ajustar a granulometria à idade e a altura do comedouro.",
                "Frango de corte", "6", "ração no chão e pintinho refugo", "farelada grossa");
        b.caso(c, NUT,
                "Poedeira em muda forçada pelo calor e pela ração fraca: antes de restringir alimento, corrigir água, cálcio e a temperatura do galpão. "
                        + "Queda de postura por nutrição se confunde com salmonela; na dúvida, o veterinário decide a notificação.",
                "Poedeira", "240", "postura em queda no verão", "ração de postura velha");
        b.caso(c, AMBI,
                "Excreção alta de nitrogênio: formular mais perto da exigência e usar enzima, em vez de proteína bruta de sobra. "
                        + "A Embrapa aponta nitrogênio e fósforo como referência do plano de manejo de nutrientes da propriedade.",
                "Frango de corte", "37", "cama amoniacal mesmo com ventilação ligada", "ração com excesso de proteína");
    }

    private static void manejo(Connection c) throws SQLException {
        Bloco b = Bloco.abrir(c, "Manejo",
                new String[] {"Espécie", "TEXTO", "2"},
                new String[] {"Idade (dias)", "NUMERICO", "2"},
                new String[] {"Sistema", "TEXTO", "2"},
                new String[] {"Quadro", "TEXTO", "5"});
        b.caso(c, DOC107,
                "Cama comprada úmida: o material deve chegar seco, perto de no máximo 20% de umidade, partícula média, sem químico e sem pó de fábrica de móveis. "
                        + "Espalhar 5 a 10 cm só com o galpão já limpo e desinfetado.",
                "Frango de corte", "1", "Convencional", "cama empedrada no dia do alojamento");
        b.caso(c, EVOL,
                "Falta de maravalha na região: a Embrapa comparou capim napier, braquiária, casca de arroz, resto de soja, resto de milho e serragem. "
                        + "O uso depende de disponibilidade, qualidade, custo e do destino da cama depois do lote.",
                "Frango de corte", "1", "Convencional", "sem maravalha e com casca de arroz disponível");
        b.caso(c, EVOL,
                "Escolha da fonte de aquecimento do pinteiro: lenha, briquete, biocombustível e GLP tiveram desempenho semelhante em avaliação da Embrapa. "
                        + "Decidir por custo, segurança e pelo monóxido de carbono, não por um ganho de peso que a comparação não mostrou.",
                "Frango de corte", "5", "Convencional", "dúvida entre lenha e gás no círculo de proteção");
        b.caso(c, SAN,
                "Piso de chão batido com histórico de cascudinho: concreto e chão batido deram desempenho parecido, mas o concreto facilita a desinfecção "
                        + "e reduz praga que guarda Gumboro, Marek e salmonela.",
                "Frango de corte", "0", "Convencional", "galpão de chão batido com cascudinho entre lotes");
        b.caso(c, SAN,
                "Lote sem ficha: registrar vacina, tratamento, visita do veterinário e mortalidade. A Embrapa trata essa ficha como parte da sanidade, "
                        + "não como papel de escritório. Sem ela o caso seguinte não tem o que reutilizar.",
                "Frango de corte", "20", "Dark house", "mortalidade subindo sem registro de vacina");
        b.caso(c, EVOL,
                "Ave silvestre dentro do galpão: telar a lateral com malha fina, de no máximo 2 cm, para cortar pássaro que come ração e traz patógeno. "
                        + "A obrigação legal de tela muda conforme a norma do MAPA e o tipo de galpão; o risco sanitário permanece.",
                "Frango de corte", "18", "Convencional", "pássaro no comedouro e ração derramada");
        b.caso(c, AMBI,
                "Carcaça deixada no galpão ou no mato: atrai rato, mosca e ave silvestre. Destinar à compostagem e não lançar em corpo d'água. "
                        + "A Embrapa inclui o tratamento de resíduo e de carcaça no manejo ambiental da propriedade.",
                "Frango de corte", "26", "Convencional", "carcaça acumulada atrás do aviário");
        b.caso(c, DOC107,
                "Vazamento de bebedouro no meio do lote: a placa úmida vira foco de cama compactada, amônia e coccidiose. "
                        + "Trocar o bico, revolver ou substituir só o trecho molhado e conferir a pressão da linha.",
                "Frango de corte", "23", "Dark house", "poça embaixo da linha de nipple");
        b.caso(c, CALOR,
                "Refugo na primeira semana: apartar, medir a temperatura na altura do pintinho e garantir que todos alcancem água e ração. "
                        + "Amontoamento é frio; fuga da campânula é calor. A faixa da primeira semana é 32 a 35 ºC.",
                "Frango de corte", "4", "Convencional", "pintinho miúdo espalhado longe do comedouro");
        b.caso(c, EVOL,
                "Luz contínua o lote inteiro: usar o programa quase contínuo no verão-outono e o intermitente no inverno-primavera. "
                        + "Luz sem pausa no frio empurra consumo, umedece a cama e aumenta morte súbita em linhagem pesada.",
                "Frango de corte", "31", "Dark house", "luz acesa a noite toda com ave pesada junto do comedouro");
        b.caso(c, SAN,
                "Idades misturadas no mesmo piso: separar pinteiro de lote velho. Marek, Gumboro e salmonela passam com mais facilidade "
                        + "quando o vazio sanitário não existe e a cama é compartilhada.",
                "Frango de corte", "9", "Convencional", "pintinho alojado na cama do lote anterior ainda no galpão");
    }

    private static void incubacao(Connection c) throws SQLException {
        Bloco b = Bloco.abrir(c, "Incubação",
                new String[] {"Fase", "TEXTO", "2"},
                new String[] {"Temperatura (ºC)", "NUMERICO", "4"},
                new String[] {"Umidade (%)", "NUMERICO", "3"},
                new String[] {"Quadro", "TEXTO", "5"});
        b.caso(c, INC,
                "Temperatura alta na incubadora: manter cerca de 37,7 ºC. No incubatório industrial a Embrapa cita redução para perto de 36,8 ºC nos dias finais. "
                        + "Acima disso o embrião acelera e morre no fim. Ageitec trabalha a faixa de 37,3 a 37,7 ºC.",
                "Incubação", "39.5", "60", "mortalidade embrionária no fim do ciclo com pintos adiantados");
        b.caso(c, INC,
                "Temperatura baixa na chocadeira: o embrião para de crescer. Voltar para 37,7 ºC e checar o termômetro, não só o visor da máquina. "
                        + "Morte no início do ciclo também aparece quando o ovo foi guardado abaixo de 2 ºC.",
                "Incubação", "35", "60", "muitos embriões mortos no início e eclosão atrasada");
        b.caso(c, INC,
                "Umidade baixa: na chocadeira a umidade relativa interna fica entre 55% e 65%, com bulbo úmido de 28 a 30 ºC. "
                        + "Ovo ressecado prende a membrana e o pinto não sai.",
                "Incubação", "37.7", "40", "pintos presos na membrana e casca seca");
        b.caso(c, INC,
                "Umidade alta demais: pinto mole, umbigo mal fechado e penugem colada. Baixar para a faixa de 55% a 65% na incubação "
                        + "e só subir a umidade na eclosão conforme o manual da máquina.",
                "Eclosão", "37.2", "82", "pintos moles com umbigo úmido");
        b.caso(c, INC,
                "Sem viragem: o embrião gruda na casca. Virar a cada 2 horas. No industrial, da fase inicial até cerca do 16º dia, "
                        + "movimentos de 45 graus de hora em hora. A viragem também ajuda a trocar calor.",
                "Incubação", "37.7", "60", "embrião aderido à casca na ovoscopia");
        b.caso(c, INC,
                "Ovo guardado demais em temperatura ambiente: não passar de 7 dias. Na geladeira, no máximo cerca de 25 dias e nunca abaixo de 8 ºC. "
                        + "Ponta para baixo e virar o ovo ao menos duas vezes ao dia.",
                "Armazenamento", "25", "70", "eclosão baixa de ovos guardados por quase duas semanas fora da geladeira");
        b.caso(c, INC,
                "Ovo que tomou calor antes de entrar na máquina: acima de cerca de 35 ºC o embrião começa a desenvolver e morre nos últimos dias. "
                        + "Recolher os ovos no mínimo duas vezes ao dia e guardar à sombra. Abaixo de 2 ºC sobe a morte inicial.",
                "Armazenamento", "36", "50", "mortalidade tardia em ovos deixados no ninho no calor");
        b.caso(c, INC,
                "Nascedouro sem diagnóstico: quebrar 200 a 300 ovos não eclodidos e anotar a idade provável da morte, a posição do embrião depois do 16º dia "
                        + "e a quantidade de ovos claros. Isso separa falha de matriz, de manejo do ovo e de máquina.",
                "Eclosão", "37.0", "65", "eclosão ruim sem saber se o ovo era claro ou embrião morto");
        b.caso(c, INC,
                "Ar da máquina parado: o embrião produz gás carbônico e precisa de ventilação conforme o fabricante, na incubadora e no nascedouro. "
                        + "Falta de ar se soma à temperatura alta no fim do ciclo.",
                "Incubação", "38.2", "70", "pintos fracos e cheiro forte dentro da incubadora");
        b.caso(c, INC,
                "Muitos ovos claros: o problema está na fertilidade e no manejo do ovo na granja, não no termostato. "
                        + "Cruzar o embriodiagnóstico com o galpão de matrizes antes de recalibrar a máquina.",
                "Eclosão", "37.7", "60", "nascedouro com maioria de ovos claros não fertilizados");
    }

    private static void biosseguridade(Connection c) throws SQLException {
        Bloco b = Bloco.abrir(c, "Biosseguridade",
                new String[] {"Sistema", "TEXTO", "2"},
                new String[] {"Quadro", "TEXTO", "5"},
                new String[] {"Agente", "TEXTO", "3"});
        b.caso(c, SAN,
                "Visita entra de roupa de rua e sem desinfecção de calçado: barreira sanitária na porta, roupa e bota do galpão, e livro de visitas. "
                        + "Pessoa e veículo são a via mais comum entre granjas vizinhas.",
                "Convencional", "pessoa de outro aviário entrou sem trocar de bota", "pessoa");
        b.caso(c, EVOL,
                "Ave silvestre no comedouro: tela de malha de no máximo 2 cm nas laterais e ração coberta. "
                        + "Pássaro transmite patógeno e come ração. Em postura, a norma do MAPA sobre tela em galpão californiano precisa ser lida na versão vigente; o risco não some com a exceção.",
                "Postura em gaiola", "pássaro pousado no comedouro e no ninho", "ave silvestre");
        b.caso(c, SAN,
                "Rato no depósito: além de comer ração, transmite salmonela. Fechar fresta, recolher sobra e manter isca fora do alcance da ave, com orientação técnica. "
                        + "Cama e ração contaminadas recolocam a bactéria no lote seguinte.",
                "Convencional", "fezes de roedor sobre o saco de ração", "roedor");
        b.caso(c, AMBI,
                "Carcaça a céu aberto: compostar. Não deixar no mato nem em curso d'água. Mosca e rato levam o problema de volta ao galpão e para o vizinho.",
                "Colonial", "carcaça jogada no pasto atrás do piquete", "carcaça");
        b.caso(c, SAN,
                "Caminhão de ração entra até a porta do galpão sem arco de desinfecção: descarregar no perímetro e não deixar o pneu cruzar a área limpa. "
                        + "O mesmo vale para o caminhão de apanha.",
                "Dark house", "caminhão de ração cruzou o portão sem desinfecção", "veículo");
        b.caso(c, SAN,
                "Cama de outro galpão espalhada sem tratamento: não reutilizar cama de lote com suspeita de salmonela. Entre lotes, lavar, desinfetar e só então colocar material novo e seco.",
                "Convencional", "cama velha de outro aviário espalhada no piso", "cama");
        b.caso(c, DOC107,
                "Vazio sanitário ignorado: alojar pintinho no mesmo dia da saída do lote anterior mantém cascudinho, vírus e bactéria. "
                        + "Esperar a lavagem e a secagem. A ficha do lote anterior diz o que ficou para trás.",
                "Convencional", "alojamento no mesmo dia da apanha", "cama");
        b.caso(c, MAREK,
                "Pintinho sem comprovante de vacina de Marek: não alojar. A vacina é feita no incubatório. Misturar esse pintinho com lote vacinado cria um foco dentro do galpão.",
                "Convencional", "nota do incubatório sem vacina de Marek", "pessoa");
    }

    private static void dicionario(Connection c) throws SQLException {
        palavra(c, "gumboro", "doença infecciosa da bursa", "ibd");
        palavra(c, "newcastle", "doença de newcastle", "dnc");
        palavra(c, "bouba", "varíola aviária", "varíola");
        palavra(c, "ascite", "barriga d'água", "hipertensão pulmonar");
        palavra(c, "morte", "síndrome da morte súbita", "ataque");
        palavra(c, "calor", "estresse calórico", "estresse térmico");
        palavra(c, "caipira", "galinha caipira");
        palavra(c, "frango", "frango de corte");
        palavra(c, "cama", "casca de arroz", "maravalha");
        palavra(c, "aflatoxina", "micotoxina");
        palavra(c, "coccidiose", "eimeria", "coccídia");
        palavra(c, "salmonela", "salmonelose");
        palavra(c, "micoplasma", "micoplasmose");
        palavra(c, "influenza", "gripe aviária");
        palavra(c, "bronquite", "bronquite infecciosa");
        palavra(c, "coriza", "coriza infecciosa");
        palavra(c, "colera", "cólera aviária");
        palavra(c, "colibacilose", "escherichia coli");
        palavra(c, "onfalite", "umbigo úmido");
        palavra(c, "aspergilose", "fungo de cama");
        palavra(c, "pododermatite", "calo de pata");
        palavra(c, "enterite", "enterite necrótica");
        palavra(c, "laringotraqueite", "laringotraqueíte");
        palavra(c, "reovirus", "artrite viral");
        palavra(c, "canibalismo", "bicagem");
        palavra(c, "prolapso", "oviduto");
        palavra(c, "raquitismo", "perna torta");
        palavra(c, "amonia", "amônia");
        palavra(c, "piolho", "malófago");
        palavra(c, "acaro", "ácaro vermelho");
        palavra(c, "verme", "verminose", "áscaris");
        palavra(c, "botulismo", "carcaça em decomposição");
        palavra(c, "cascudinho", "alphitobius");
        palavra(c, "eclosao", "eclosão");
        palavra(c, "biosseguridade", "barreira sanitária");
    }

    static void palavra(Connection c, String chave, String... equivalentes) throws SQLException {
        long id;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO dicionario (chave) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, chave);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getLong(1);
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO equivalente (dicionario_id, expressao) VALUES (?,?)")) {
            for (String equivalente : equivalentes) {
                ps.setLong(1, id);
                ps.setString(2, equivalente);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void pares(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.execute("INSERT INTO similaridade (campo_id, valor_consulta, valor_caso, similaridade) "
                    + "SELECT campo_id, valor, valor, 1 FROM ("
                    + "SELECT DISTINCT d.campo_id, d.valor FROM detalhe d "
                    + "JOIN campo f ON f.id = d.campo_id "
                    + "WHERE f.nome IN ('Espécie', 'Sistema', 'Fase', 'Agente') "
                    + "AND d.valor IS NOT NULL AND CHAR_LENGTH(d.valor) <= 80) t");
        }
        cruzar(c, "Espécie", "Frango de corte", "Galinha caipira", 0.45);
        cruzar(c, "Espécie", "Poedeira", "Galinha caipira", 0.5);
        cruzar(c, "Sistema", "Convencional", "Dark house", 0.6);
        cruzar(c, "Sistema", "Colonial", "Convencional", 0.4);
        cruzar(c, "Espécie", "Frango de corte", "Poedeira", 0.35);
        cruzar(c, "Sistema", "Postura em gaiola", "Convencional", 0.3);
        cruzar(c, "Sistema", "Dark house", "Colonial", 0.25);
        cruzar(c, "Fase", "Incubação", "Eclosão", 0.55);
        cruzar(c, "Fase", "Armazenamento", "Incubação", 0.4);
        cruzar(c, "Fase", "Transferência", "Eclosão", 0.65);
        cruzar(c, "Fase", "Ovoscopia", "Incubação", 0.5);
        cruzar(c, "Agente", "pessoa", "veículo", 0.5);
        cruzar(c, "Agente", "roedor", "ave silvestre", 0.45);
        cruzar(c, "Agente", "cama", "carcaça", 0.4);
        cruzar(c, "Agente", "inseto", "cama", 0.55);
        cruzar(c, "Agente", "ração", "água", 0.35);
    }

    static void cruzar(Connection c, String campo, String a, String b, double sim) throws SQLException {
        try (PreparedStatement busca = c.prepareStatement("SELECT id FROM campo WHERE nome = ?");
                PreparedStatement insere = c.prepareStatement(
                        "INSERT INTO similaridade (campo_id, valor_consulta, valor_caso, similaridade) VALUES (?,?,?,?)")) {
            busca.setString(1, campo);
            try (ResultSet rs = busca.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong(1);
                    insere.setLong(1, id);
                    insere.setString(2, a);
                    insere.setString(3, b);
                    insere.setDouble(4, sim);
                    insere.addBatch();
                    insere.setLong(1, id);
                    insere.setString(2, b);
                    insere.setString(3, a);
                    insere.setDouble(4, sim);
                    insere.addBatch();
                }
            }
            insere.executeBatch();
        }
    }

    static final class Bloco {
        final long area;
        final long[] campos;

        Bloco(long area, long[] campos) {
            this.area = area;
            this.campos = campos;
        }

        static Bloco abrir(Connection c, String nome, String[]... spec) throws SQLException {
            long area;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO area (nome) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nome);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    area = rs.getLong(1);
                }
            }
            long[] ids = new long[spec.length];
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO campo (area_id, nome, tipo, peso) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < spec.length; i++) {
                    ps.setLong(1, area);
                    ps.setString(2, spec[i][0]);
                    ps.setString(3, spec[i][1]);
                    ps.setInt(4, Integer.parseInt(spec[i][2]));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        ids[i] = rs.getLong(1);
                    }
                }
            }
            return new Bloco(area, ids);
        }

        void caso(Connection c, String fonte, String solucao, String... valores) throws SQLException {
            gravar(c, true, fonte, solucao, valores);
        }

        static Bloco existente(Connection c, String nome) throws SQLException {
            long area;
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM area WHERE nome = ?")) {
                ps.setString(1, nome);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(nome);
                    }
                    area = rs.getLong(1);
                }
            }
            java.util.ArrayList<Long> ids = new java.util.ArrayList<Long>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM campo WHERE area_id = ? ORDER BY id")) {
                ps.setLong(1, area);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getLong(1));
                    }
                }
            }
            long[] campos = new long[ids.size()];
            for (int i = 0; i < campos.length; i++) {
                campos[i] = ids.get(i);
            }
            return new Bloco(area, campos);
        }

        long gravar(Connection c, boolean valido, String fonte, String solucao, String... valores) throws SQLException {
            if (valores.length != campos.length) {
                throw new SQLException("Campos divergentes em " + fonte + ": " + valores.length);
            }
            if (solucao.length() > 2000) {
                throw new SQLException("Solução longa em " + fonte);
            }
            long id;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO caso (area_id, solucao, fonte, valido) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, area);
                ps.setString(2, solucao);
                ps.setString(3, fonte);
                ps.setBoolean(4, valido);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    id = rs.getLong(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO detalhe (caso_id, campo_id, valor) VALUES (?,?,?)")) {
                for (int i = 0; i < valores.length; i++) {
                    ps.setLong(1, id);
                    ps.setLong(2, campos[i]);
                    ps.setString(3, valores[i]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return id;
        }

        void complemento(Connection c, long casoId, String texto) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO complemento (caso_id, texto) VALUES (?,?)")) {
                ps.setLong(1, casoId);
                ps.setString(2, texto);
                ps.executeUpdate();
            }
        }

        void vincular(Connection c, long casoId, long origemId) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE caso SET origem_id = ?, similaridade = ?, adaptacao = ? WHERE id = ?")) {
                ps.setLong(1, origemId);
                ps.setDouble(2, 0.84);
                ps.setString(3, "Quadro e idade ajustados a partir do caso de origem.");
                ps.setLong(4, casoId);
                ps.executeUpdate();
            }
        }
    }
}
