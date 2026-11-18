package br.com.avembrbc;

import java.sql.Connection;
import java.sql.SQLException;

final class Massa {

    private static final String VET = " Medicação só com o veterinário responsável.";
    private static final String OFICIAL = " Comunicar o serviço veterinário oficial e não medicar para encobrir o quadro.";

    private static int seq;
    private static long baseArea;
    private static Carga.Bloco bloco;

    private Massa() {
    }

    static void carregar(Connection c) throws SQLException {
        sanidade(c);
        ambiencia(c);
        nutricao(c);
        manejo(c);
        incubacao(c);
        biosseguridade(c);
        if (seq < 420) {
            throw new SQLException("massa " + seq);
        }
    }

    private static void abrir(Connection c, String area) throws SQLException {
        bloco = Carga.Bloco.existente(c, area);
        baseArea = 0;
    }

    private static void por(Connection c, String fonte, String solucao, String... valores) throws SQLException {
        boolean pendente = ++seq % 12 == 0;
        long id = bloco.gravar(c, !pendente, fonte, solucao, valores);
        if (!pendente && baseArea == 0) {
            baseArea = id;
        }
        if (pendente) {
            bloco.complemento(c, id, "Ainda sem validação. " + valores[valores.length - 1]
                    + ". Separar as aves afetadas e chamar o veterinário antes de medicar.");
            if (baseArea != 0) {
                bloco.vincular(c, id, baseArea);
            }
        } else if (seq % 17 == 0) {
            bloco.complemento(c, id, "Complemento de campo: conferir a mortalidade do dia e a ficha de vacina. " + valores[valores.length - 1] + ".");
        }
    }

    private static void idades(Connection c, String fonte, String especie, String sistema, String idades,
            String quadro, String sinal, String solucao) throws SQLException {
        for (String idade : idades.split(" ")) {
            String texto = solucao.replace("{idade}", idade).replace("{especie}", especie).replace("{sistema}", sistema);
            por(c, fonte, texto, especie, idade, sistema, quadro + " aos " + idade + " dias", sinal);
        }
    }

    private static void sanidade(Connection c) throws SQLException {
        abrir(c, "Sanidade");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "21 28 35 42",
                "rosto inchado com catarro", "espirro e pálpebra colada",
                "Coriza infecciosa em {especie} aos {idade} dias, no {sistema}: isolar o galpão, baixar poeira e amônia e tratar só depois do exame. A vacina depende da cepa da região." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "14 21 28 35",
                "estertor e pena arrepiada sem sinal nervoso", "pericardite e aerossaculite na necropsia",
                "Colibacilose em {especie} aos {idade} dias: a Escherichia coli entra por arranhão, sacos aéreos irritados ou umbigo mal fechado. Corrigir cama, ventilação e a origem do pintinho antes de repetir antibiótico." + VET);
        idades(c, Carga.IMUNO, "Frango de corte", "Convencional", "14 18 22 28",
                "cama úmida com diarréia escura depois de coccidiose", "intestino com membrana necrótica",
                "Enterite necrótica em {especie} aos {idade} dias: o Clostridium aproveita a lesão de coccídia e a ração rica. Secar a cama, revisar o programa de coccidiose e não aumentar proteína para 'recuperar' o lote." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "16 23 30",
                "fezes aquosas e pintinho miúdo sem sangue vivo", "lesão no duodeno",
                "Coccidiose por Eimeria acervulina em {especie} aos {idade} dias: vacina na primeira semana e cama seca. O quadro emagrece sem o sangue típico da Eimeria tenella." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "18 26 34",
                "fezes alaranjadas e lote desuniforme", "lesão no meio do intestino",
                "Coccidiose por Eimeria maxima em {especie} aos {idade} dias: mesmo caminho da vacina precoce e da cama seca. Conteúdo alaranjado no intestino médio separa este quadro do sangue cecal." + VET);
        idades(c, Carga.PROD, "Frango de corte", "Convencional", "28 35 42",
                "ave mancando com calo escuro na pata", "lesão de coxim plantar",
                "Pododermatite em {especie} aos {idade} dias: trocar o trecho encrostado e manter a cama entre 5 e 10 cm, seca. Umidade, partícula fina e peso do fim de lote abrem a lesão.");
        idades(c, Carga.ASCITE, "Frango de corte", "Convencional", "21 35 42",
                "abdômen mole e crista escura em ave pesada", "líquido na cavidade",
                "Ascite em {especie} aos {idade} dias: não forçar ganho nas duas primeiras semanas, oscilação térmica menor que 2 ºC e amônia baixa, perto de 11 ppm. Sódio da ração entre 0,16% e 0,20%.");
        idades(c, Carga.PROD, "Frango de corte", "Convencional", "21 28 35",
                "ave boa encontrada de costas perto do prato", "sem lesão que explique a morte",
                "Síndrome da morte súbita em {especie} aos {idade} dias: o diagnóstico é por exclusão. Programa de luz com pausa e sem empurrar o ganho no calor reduz o pico de consumo.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "14 21 28",
                "tendão inchado e ave sentada", "artrite sem pus de bactéria óbvia",
                "Reovirose em {especie} aos {idade} dias: a artrite viral desuniformiza o lote. Separar os refugos, revisar a imunossupressão (Gumboro, micotoxina, calor) e não tratar como se todo mancado fosse estafilococo." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "21 28 35",
                "chiado úmido e condenação de saco aéreo", "espuma na traqueia",
                "ORT em {especie} aos {idade} dias: o Ornithobacterium piora com amônia e poeira. Ventilar sem corrente no pintinho e confirmar no laboratório antes de escolher o produto." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "25 32 40",
                "cabeça inchada e espirro em lote fechado", "edema ao redor do olho",
                "Síndrome da cabeça inchada em {especie} aos {idade} dias: pneumovírus mais bactéria de galpão. Baixar amônia, secar a cama e isolar o aviário. Não confundir com coriza sem olhar o seio infraorbitário." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "28 35",
                "ave esticando o pescoço para respirar e sangue na traqueia", "hemorragia na laringe",
                "Laringotraqueíte em {especie} aos {idade} dias: doença viral de galpão. Restringir visita e equipamento. Vacina só com orientação, porque a cepa vacinal também circula." + VET);
        idades(c, Carga.NUT, "Frango de corte", "Convencional", "14 18 21",
                "pintinho de lado com pescoço torcido e cerebelo hemorrágico", "encefalomalácia",
                "Deficiência de vitamina E em {especie} aos {idade} dias: ração oxidada ou premix velho. Trocar o alimento. O torcicolo aqui não é Newcastle: não há mortalidade explosiva do lote inteiro.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "21 28 35",
                "ferimento de dorso e ave bicada no comedouro", "canibalismo de pena e cloaca",
                "Bicagem em {especie} aos {idade} dias: revisar lotação, cama úmida, luz forte e falta de proteína ou sal. Escurecer o galpão e apartar os feridos. Não esperar a vacina resolver comportamento.");
        idades(c, Carga.PROD, "Frango de corte", "Convencional", "32 38 42",
                "pele amarela no abdômen depois da apanha apertada", "celulite em carcaça",
                "Celulite em {especie} aos {idade} dias: arranhão de densidade, cama ruim e captura violenta abre porta para Escherichia coli. Uniformizar e não empilhar ave na apanha.");
        idades(c, Carga.CALOR, "Frango de corte", "Convencional", "1 2 5",
                "pintinho morto na caixa com umbigo aberto", "onfalite e vitelo mal absorvido",
                "Onfalite em {especie} aos {idade} dias: o umbigo vem do incubatório, da viagem ou do frio na chegada. Não misturar esses pintinhos ao núcleo sadio. Na primeira semana, 32 a 35 ºC também na cama.");
        idades(c, Carga.DOC107, "Frango de corte", "Convencional", "3 6 9",
                "pintinho com bico aberto e cama com cheiro de mofo", "nódulo no pulmão e no saco aéreo",
                "Aspergilose em {especie} aos {idade} dias: trocar a cama mofada antes de alojar de novo. Partícula média, seca, sem pó de fábrica de móveis e sem umidade acima de cerca de 20%.");
        idades(c, Carga.IMUNO, "Frango de corte", "Convencional", "12 20 30",
                "prostração e fezes brancas em lote vacinado fora do dia", "bolsa cloacal edemaciada",
                "Gumboro em {especie} aos {idade} dias, no {sistema}: vacina viva só em ave sadia, na janela do calendário. O cascudinho guarda o vírus. O quadro derruba as outras vacinas." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "14 35",
                "queda brusca com ave de lado e estertor", "ovos de sangue na traqueia e torcicolo",
                "Suspeita de Newcastle em {especie} aos {idade} dias." + OFICIAL + " Isolar o lote e restringir pessoas e veículos. A vacina das semanas 2, 4 e 9 não substitui a notificação.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "4 7 11",
                "pintinho branco de fezes e mortalidade na primeira semana", "fígado e coração com ponto branco",
                "Salmonela em {especie} aos {idade} dias, no pinteiro." + OFICIAL + " Não reutilizar a cama. Rato e farinha de origem animal mantêm a bactéria.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "28 35 42",
                "chiado crônico que não passa com a ventilação ligada", "seio da face com catarro espesso",
                "Suspeita de micoplasmose em {especie} aos {idade} dias." + OFICIAL + " Comprar ave de origem livre. Não tratar só para limpar o laudo.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "22 33",
                "morte rápida com crista roxa e ave silvestre no telhado", "edema de cabeça e hemorragia",
                "Suspeita de influenza aviária em {especie} aos {idade} dias." + OFICIAL + " Isolar o galpão e não mover aves. No ciclo seguinte, tela de malha de no máximo 2 cm.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "20 30 40",
                "besouro na cama e refugo depois da reutilização", "cascudinho sob o comedouro",
                "Cascudinho em {especie} aos {idade} dias: o besouro guarda Gumboro, Marek e salmonela. Piso que lava entre lotes reduz o inseto. Chão batido produz, mas controla pior a praga.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "7 14 21",
                "articulação quente com pus em pintinho manco", "artrite estafilocócica",
                "Artrite por estafilococo em {especie} aos {idade} dias: entra por umbigo, arranhão e vacina com agulha suja. Separar os mancos e revisar a higiene da equipe de vacinação." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "30 38",
                "paralisia flácida de pescoço depois de carcaça na cama", "ave mole sem lesão de Marek",
                "Botulismo em {especie} aos {idade} dias: recolher carcaça todo dia. A toxina vem da decomposição, não de um vírus de vacina. Compostar fora do galpão.");
        idades(c, Carga.IMUNO, "Frango de corte", "Convencional", "12 18",
                "hematoma na asa e refugo depois de Gumboro", "anemia e medula pálida",
                "Anemia infecciosa das galinhas em {especie} aos {idade} dias: o vírus da anemia, junto com Gumboro, explica hematoma e vacina que não pega. Controlar a imunossupressão e a origem do ovo." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "21 28",
                "mortalidade com fígado aumentado e músculo pálido", "hepatite com corpúsculo de inclusão",
                "Hepatite por corpúsculo de inclusão em {especie} aos {idade} dias: associada a adenovirus e a lote imunossuprimido. Confirmar no laboratório. Não tratar às cegas como salmonela." + VET);
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "10 16",
                "sede extrema e ave caída depois da ração nova", "sal na ração acima do previsto",
                "Excesso de sódio em {especie} aos {idade} dias: a faixa usada na Embrapa para não empurrar ascite é 0,16% a 0,20% de sódio. Oferecer água limpa e trocar o lote de ração. Na água, o sódio alto também molha a cama.");
        idades(c, Carga.SAN, "Frango de corte", "Convencional", "8 12",
                "papo duro e pintinho que não come", "impactação de papo com cama",
                "Impactação de papo em {especie} aos {idade} dias: o pintinho come cama quando a ração está longe, a partícula é grossa ou a luz do pinteiro é fraca. Ajustar comedouro e retirar a cama do papo só com orientação.");
        idades(c, Carga.SAN, "Frango de corte", "Dark house", "18 26",
                "estertor no túnel sem torcicolo", "conjuntivite e queda de ganho",
                "Bronquite infecciosa em {especie} aos {idade} dias, no {sistema}: a vacina viva entra no calendário da 2ª, 4ª e 9ª semana, em ave sadia. Amônia alta no galpão fechado copia o chiado e piora a resposta.");
        idades(c, Carga.NH3, "Frango de corte", "Dark house", "21 28 35 42",
                "chiado que alivia quando o exaustor liga", "amônia alta e colibacilose",
                "Colibacilose de galpão fechado em {especie} aos {idade} dias: acima de cerca de 20 ppm a amônia já passa o teto de muitos exportadores, e perto de 60 ppm a ave adoece do aparelho respiratório. Ventilar sem jogar o ar em cima do pintinho." + VET);
        idades(c, Carga.PROD, "Frango de corte", "Dark house", "30 36 42",
                "pata queimada em cama úmida de túnel", "pododermatite de fim de lote",
                "Pododermatite no {sistema} em {especie} aos {idade} dias: a cama do dark house umedece se o nipple pinga e o exaustor não seca. Trocar a placa molhada e regular a pressão da linha.");
        idades(c, Carga.ASCITE, "Frango de corte", "Dark house", "24 31 38",
                "morte com barriga d'água e exaustor em falha", "ascite por ventilação irregular",
                "Ascite em {especie} no {sistema}, aos {idade} dias: falha de ventilação faz o papel do frio e da altitude. Manter a temperatura estável e a amônia baixa. Não compensar no escuro empurrando ração.");
        idades(c, Carga.PROD, "Frango de corte", "Dark house", "27 34",
                "morte súbita com a luz quase contínua", "ave pesada de barriga para cima",
                "Morte súbita em {especie} no {sistema}, aos {idade} dias: luz sem pausa no inverno aumenta o consumo. Usar programa intermitente no frio e quase contínuo só no calor, como a Embrapa comparou em cortina amarela ou azul.");
        idades(c, Carga.SAN, "Frango de corte", "Dark house", "15 22 29",
                "fezes pastosas e cama colada no túnel", "coccidiose de cama úmida",
                "Coccidiose em {especie} no {sistema}, aos {idade} dias: a cama úmida do nipple vale mais que o nome do galpão. Vacina na primeira semana e troca do trecho encharcado." + VET);
        idades(c, Carga.DOC107, "Galinha caipira", "Colonial", "45 60 90 120",
                "ave magra no piquete com fezes com verme", "áscaris no intestino",
                "Verminose em {especie} aos {idade} dias: o piquete úmido reinfesta. Rotacionar o pasto, secar o abrigo e desverminar com o veterinário, não com dose caseira." + VET);
        idades(c, Carga.DOC107, "Galinha caipira", "Colonial", "40 70 100",
                "ave inquieta à noite e crista pálida", "ácaro vermelho no poleiro",
                "Ácaro vermelho em {especie} aos {idade} dias: o ácaro sai à noite do poleiro e da fresta. Lavar o abrigo, trocar o ninho e tratar o ambiente com orientação. Piolho de pena é outro bicho, fica na ave de dia.");
        idades(c, Carga.DOC107, "Galinha caipira", "Colonial", "50 80 110",
                "pena quebrada e ave se coçando de dia", "piolho na pena",
                "Piolho em {especie} aos {idade} dias: inspecionar a pena em volta da cloaca. Apartar as mais infestadas. Não usar produto de bovino na ave." + VET);
        idades(c, Carga.DOC107, "Galinha caipira", "Colonial", "30 55 75",
                "crostas na boca e dificuldade de comer", "bouba diftérica",
                "Bouba diftérica em {especie} aos {idade} dias: além da crosta de crista, a placa na boca derruba o consumo. Vacinar o lote seguinte, reduzir mosquito e ferimento, e apartar as doentes.");
        idades(c, Carga.SAN, "Galinha caipira", "Colonial", "20 50 80",
                "emagrecimento no piquete com fezes escuras", "coccídia de pasto",
                "Coccidiose de piquete em {especie} aos {idade} dias: a vacina da primeira semana continua valendo, e o chão em volta do abrigo precisa secar. Separar as aves muito afetadas." + VET);
        idades(c, Carga.SAN, "Galinha caipira", "Colonial", "60 100",
                "pescoço mole depois de composteira aberta no pasto", "botulismo de carcaça",
                "Botulismo em {especie} aos {idade} dias: a ave come larva na carcaça. Fechar a composteira e recolher mortos todo dia. Não é Marek: a paralisia é flácida e recente.");
        idades(c, Carga.SAN, "Galinha caipira", "Colonial", "40 70 100",
                "morte súbita de ave gorda com crista roxa", "cólera aviária",
                "Cólera aviária em {especie} aos {idade} dias: a Pasteurella entra por ferimento, rato e água parada. Isolar o piquete e confirmar no laboratório." + VET);
        idades(c, Carga.SAN, "Galinha caipira", "Colonial", "25 45 90",
                "morte no pasto com torcicolo e diarréia verde", "suspeita de Newcastle em caipira",
                "Suspeita de Newcastle em {especie} aos {idade} dias." + OFICIAL + " Fechar visita e o trânsito de aves. O calendário de caipira prioriza Newcastle, Marek, Gumboro, bronquite e bouba, e mesmo assim a notificação é obrigatória.");
        idades(c, Carga.MAREK, "Galinha caipira", "Colonial", "30 50 80",
                "ave caipira sentada com perna esticada e sem vacina de incubatório", "Marek em lote sem comprovante",
                "Marek em {especie} aos {idade} dias: a vacina é no incubatório. Pintinho sem comprovante não deveria ter sido alojado. Não se trata a paralisia no galpão.");
        idades(c, Carga.SAN, "Galinha caipira", "Colonial", "40 70",
                "crista escura e fezes amarelas com enxofre", "histomoníase",
                "Histomoníase em {especie} aos {idade} dias: o protozoário viaja no verme cecal. Separar da terra úmida e não misturar com peru. Confirmar no fígado, que fica com área redonda de necrose." + VET);
        idades(c, Carga.DOC107, "Galinha caipira", "Colonial", "35 60",
                "dorso sem pena e ferimento no piquete", "canibalismo em caipira",
                "Bicagem em {especie} aos {idade} dias: lotação do abrigo, luz direta e proteína baixa. Dar espaço, escurecer o poleiro e apartar os feridos.");
        idades(c, Carga.DOC107, "Galinha caipira", "Colonial", "20 40",
                "ferida de predador infeccionada na coxa", "abscesso de mordida",
                "Ferimento de predador em {especie} aos {idade} dias: limpar com orientação veterinária, apartar a ave e fechar o buraco da tela. A infecção mata mais que o susto." + VET);
        idades(c, Carga.SAN, "Galinha caipira", "Colonial", "80 140",
                "chiado de meses e queda de postura no piquete", "micoplasma crônico em caipira",
                "Suspeita de micoplasmose em {especie} aos {idade} dias." + OFICIAL + " O quadro crônico não se apaga com uma dose. Separar das aves novas.");
        idades(c, Carga.SAN, "Galinha caipira", "Colonial", "15 40",
                "pintinho caipira com fezes brancas e morte no abrigo", "salmonela em criação colonial",
                "Salmonela em {especie} aos {idade} dias." + OFICIAL + " Não reaproveitar a cama do abrigo nem os ovos chocados desse lote sem orientação do serviço oficial.");
        idades(c, Carga.DOC107, "Galinha caipira", "Colonial", "55 85",
                "refugo no abrigo com cama velha e pasto bom", "verminose de cama de abrigo",
                "Verminose de abrigo em {especie} aos {idade} dias: o pasto pode estar bem e a cama do poleiro, não. Trocar a cama úmida do dormitório e revisar o verme." + VET);
        idades(c, Carga.SAN, "Poedeira", "Postura em gaiola", "180 240 320",
                "rosto inchado na gaiola e queda de ovo", "coriza em poedeira",
                "Coriza em {especie} aos {idade} dias: isolamentos, poeira de gaiola e amônia mantêm o quadro. Tratar com exame e revisar a água do nipple." + VET);
        idades(c, Carga.SAN, "Poedeira", "Postura em gaiola", "200 280 360",
                "postura caída e chiado pela manhã na gaiola", "micoplasma em poedeira",
                "Suspeita de micoplasmose em {especie} aos {idade} dias." + OFICIAL + " Queda de postura com chiado crônico não se resolve só com vitamina.");
        idades(c, Carga.SAN, "Poedeira", "Postura em gaiola", "190 250 310",
                "ovo com clara aquosa e casca rugosa sem diarréia sanguinolenta", "bronquite em poedeira",
                "Bronquite infecciosa em {especie} aos {idade} dias: a clara aguada e a casca rugosa denunciam o vírus mesmo sem chiado forte. Revisar o calendário e não confundir com falta de cálcio pura.");
        idades(c, Carga.SAN, "Poedeira", "Postura em gaiola", "210 270 350",
                "ovos de casca suja e morte no fundo da gaiola", "salmonela em postura",
                "Salmonela em {especie} aos {idade} dias." + OFICIAL + " Recolher os ovos conforme a orientação oficial. Rato no silo segura a bactéria entre ciclos.");
        idades(c, Carga.PROD, "Poedeira", "Postura em gaiola", "170 230 300",
                "ave com oviduto para fora depois do ovo grande", "prolapso de cloaca",
                "Prolapso em {especie} aos {idade} dias: ovo grande, ave gorda e luz estimulando cedo demais. Apartar para as outras não bicarem. Ajustar o programa de luz e o peso, não só empurrar o oviduto de volta.");
        idades(c, Carga.PROD, "Poedeira", "Postura em gaiola", "220 280 340",
                "ave empingada e morte com gema na cavidade", "peritonite por gema",
                "Peritonite por gema em {especie} aos {idade} dias: a gema cai na cavidade e infecciona. Separar a ave inchada. Bronquite, manejo do ovo interno e bactéria de gaiola entram na lista." + VET);
        idades(c, Carga.DOC107, "Poedeira", "Postura em gaiola", "160 260 400",
                "ave inquieta na gaiola com pena comida na cloaca", "piolho de gaiola",
                "Piolho em {especie} aos {idade} dias: olhar a pena da cloaca de várias gaiolas, não de uma ave só. Tratar o lote com produto indicado para galinha e limpar a esteira." + VET);
        idades(c, Carga.PROD, "Poedeira", "Postura em gaiola", "180 240",
                "cloaca bicada e ovo com sangue na gaiola", "canibalismo de cloaca",
                "Bicagem de cloaca em {especie} aos {idade} dias: luz forte, ovo grande e prolapsos atraem o bico. Escurecer e apartar. Sal e proteína da ração de postura precisam estar na fórmula, não no achismo.");
        idades(c, Carga.SAN, "Poedeira", "Postura em gaiola", "200 300",
                "morte aguda de poedeira com fígado cozido", "cólera em postura",
                "Cólera aviária em {especie} aos {idade} dias: morte rápida e fígado com ponto de necrose pedem laboratório. Fechar visita entre galpões." + VET);
        idades(c, Carga.SAN, "Poedeira", "Postura em gaiola", "190 260",
                "queda de postura abrupta com ave torta na gaiola", "suspeita de Newcastle em poedeira",
                "Suspeita de Newcastle em {especie} aos {idade} dias." + OFICIAL + " Parar a coleta para outros galpões e a saída de ovo até a orientação oficial.");
        idades(c, Carga.NUT, "Poedeira", "Postura em gaiola", "300 380",
                "ave inchada com urato branco no coração", "gota visceral",
                "Gota em {especie} aos {idade} dias: falta de água, proteína alta demais ou rim lesado. Restabelecer a água antes de 'fortalecer' a ração. Não é ascite: o depósito é urato, não só líquido claro.");
        idades(c, Carga.NUT, "Poedeira", "Postura em gaiola", "280 360",
                "ave deitada na gaiola com osso mole e ovo de casca fina", "fadiga de gaiola",
                "Fadiga de gaiola em {especie} aos {idade} dias: osteoporose de poedeira velha, cálcio e fósforo fora da fase. Não se resolve com uma farinha de osso jogada por cima da ração. Reformular.");
        idades(c, Carga.SAN, "Poedeira", "Postura em gaiola", "210 330",
                "morte alta na gaiola com crista roxa e sem prolapso", "suspeita de influenza em poedeira",
                "Suspeita de influenza aviária em {especie} aos {idade} dias." + OFICIAL + " Suspender visita, ovo e veículo até o serviço oficial assumir.");
        idades(c, Carga.DOC107, "Poedeira", "Postura em gaiola", "150 240",
                "pena falhada e ave nervosa na esteira de ovo", "ácaro de pena em poedeira",
                "Ácaro de pena em {especie} aos {idade} dias: a esteira e a fresta da gaiola escondem o ácaro. Limpar o equipamento junto com o tratamento da ave." + VET);
    }

    private static void ambiencia(Connection c) throws SQLException {
        abrir(c, "Ambiência");
        int[] idades = {1, 7, 14, 21, 28, 35, 42};
        for (int idade : idades) {
            int[] faixa = conforto(idade);
            por(c, Carga.CALOR, "Aos " + idade + " dias a faixa de conforto da Embrapa para frango de corte é " + faixa[0] + " a " + faixa[1]
                    + " ºC. Acima disso: ventilação e, em túnel, cerca de 2 a 2,5 m/s com nebulização ou pad. Água fresca. O pintinho sente a temperatura da cama, não só a do visor.",
                    "Frango de corte", String.valueOf(idade), String.valueOf(faixa[1] + 7), "60", "10",
                    "ofegação aos " + idade + " dias com " + (faixa[1] + 7) + " ºC");
            por(c, Carga.CALOR, "Aos " + idade + " dias a faixa é " + faixa[0] + " a " + faixa[1]
                    + " ºC. Abaixo disso a ave amontoa e gasta a ração para esquentar. Círculo de proteção na primeira semana. Lenha, briquete e GLP deram desempenho parecido na Embrapa quando a zona do pintinho ficou na faixa.",
                    "Frango de corte", String.valueOf(idade), String.valueOf(Math.max(12, faixa[0] - 6)), "65", "8",
                    "amontoamento aos " + idade + " dias com " + Math.max(12, faixa[0] - 6) + " ºC");
        }
        for (int idade : new int[] {7, 14, 21, 28, 35}) {
            int[] faixa = conforto(idade);
            por(c, Carga.AMB, "Umidade muito acima de 60 a 70% na idade de " + idade
                    + " dias encharca a cama mesmo com a temperatura na faixa. Regular o nipple, trocar a placa úmida e ventilar. Cama molhada espalha coccidiose e lesão de pata.",
                    "Frango de corte", String.valueOf(idade), String.valueOf((faixa[0] + faixa[1]) / 2), "88", "18",
                    "cama encharcada aos " + idade + " dias com 88% de umidade");
        }
        for (int idade : new int[] {14, 21, 28, 35, 42}) {
            int[] faixa = conforto(idade);
            por(c, Carga.AMB, "Ar seco demais aos " + idade
                    + " dias resseca mucosa e aumenta poeira, que irrita o aparelho respiratório. Voltar a umidade para perto de 60%. Não resolver poeira só fechando a cortina, porque a amônia sobe junto.",
                    "Frango de corte", String.valueOf(idade), String.valueOf((faixa[0] + faixa[1]) / 2), "38", "12",
                    "poeira e mucosa seca aos " + idade + " dias com 38% de umidade");
        }
        for (int idade : new int[] {21, 28, 35, 42}) {
            int[] faixa = conforto(idade);
            por(c, Carga.NH3, "Aos " + idade + " dias, amônia por volta de 55 ppm já passa o teto de cerca de 20 ppm usado por muitos exportadores e se aproxima da faixa em que a ave adoece do aparelho respiratório. Ventilar e secar a cama. Não vacinar em cima de mucosa irritada.",
                    "Frango de corte", String.valueOf(idade), String.valueOf((faixa[0] + faixa[1]) / 2), "68", "55",
                    "chiado e cheiro de amônia aos " + idade + " dias com 55 ppm");
        }
        for (int idade : new int[] {28, 35, 42}) {
            por(c, Carga.NH3, "Perto de 95 ppm aos " + idade
                    + " dias a troca gasosa cai. Abrir a renovação de ar antes de qualquer tratamento respiratório. Cama reusada e úmida segura o gás no fim do lote.",
                    "Frango de corte", String.valueOf(idade), "24", "72", "95",
                    "olhos fechados e amônia de 95 ppm aos " + idade + " dias");
        }
        for (int idade : new int[] {21, 28, 35, 42}) {
            int[] faixa = conforto(idade);
            por(c, Carga.CALOR, "No dark house, aos " + idade + " dias, exaustor parado leva a temperatura para " + (faixa[1] + 6)
                    + " ºC, acima da faixa de " + faixa[0] + " a " + faixa[1]
                    + " ºC. Religar o túnel, conferir o pad e a água. Mortalidade de calor em galpão fechado é falha de equipamento, não de linhagem.",
                    "Frango de corte", String.valueOf(idade), String.valueOf(faixa[1] + 6), "70", "22",
                    "túnel parado aos " + idade + " dias");
        }
        for (int idade : new int[] {21, 35, 50, 70}) {
            por(c, Carga.CALOR, "Galinha caipira aos " + idade
                    + " dias no pasto: sombra, água à vontade e abrigo que ventila. A faixa de conforto do pintinho (32 a 35 ºC na primeira semana, caindo cerca de 3 ºC por semana até perto de 20 ºC) vale no círculo; a ave maior sofre no sol da tarde mesmo com o abrigo 'no padrão'.",
                    "Galinha caipira", String.valueOf(idade), "34", "55", "8",
                    "ave ofegante no piquete sem sombra aos " + idade + " dias");
        }
        for (int idade : new int[] {7, 14}) {
            por(c, Carga.CALOR, "Pintinho caipira aos " + idade
                    + " dias amontoado no abrigo: a faixa ainda é alta (perto de 29 a 35 ºC conforme a semana). O piquete não aquece o pinteiro. Usar círculo e cama seca.",
                    "Galinha caipira", String.valueOf(idade), "22", "70", "6",
                    "pintinho caipira amontoado aos " + idade + " dias");
        }
        for (int idade : new int[] {180, 240, 300}) {
            por(c, Carga.CALOR, "Poedeira adulta aos " + idade
                    + " dias ofegante: o conforto da ave em postura fica perto de 18 a 24 ºC. Acima disso caem o ovo e a casca, e a ave come menos. Água fresca e ventilação antes de concentrar a ração.",
                    "Poedeira", String.valueOf(idade), "33", "60", "15",
                    "postura em queda com ofegação aos " + idade + " dias e 33 ºC");
        }
        for (int idade : new int[] {200, 280}) {
            por(c, Carga.CALOR, "Poedeira aos " + idade
                    + " dias abaixo de cerca de 12 ºC gasta energia para esquentar e a postura cai sem doença. Fechar cortina sem prender a amônia. A faixa prática da adulta é perto de 18 a 24 ºC.",
                    "Poedeira", String.valueOf(idade), "12", "65", "10",
                    "poedeira arrepiada aos " + idade + " dias com 12 ºC");
        }
        for (int idade : new int[] {220, 320}) {
            por(c, Carga.NH3, "Amônia no galpão de postura aos " + idade
                    + " dias: a esteira de dejeto parada e a cortina fechada passam de 20 ppm com facilidade. Limpar a esteira e renovar o ar. O chiado da poedeira nesse cenário não nasce como micoplasma.",
                    "Poedeira", String.valueOf(idade), "22", "70", "40",
                    "cheiro de amônia na gaiola aos " + idade + " dias");
        }
        for (int idade : new int[] {2, 5, 8}) {
            por(c, Carga.CALOR, "Corrente de ar direta no pintinho aos " + idade
                    + " dias esfria a cama mesmo com o termômetro do corredor em 33 ºC. A faixa da primeira semana é 32 a 35 ºC na altura da ave. Baixar a cortina do lado do vento e medir na cama.",
                    "Frango de corte", String.valueOf(idade), "33", "60", "8",
                    "pintinho piando na corrente de ar aos " + idade + " dias");
        }
        for (int idade : new int[] {12, 20, 30}) {
            por(c, Carga.AMB, "Poça sob o nipple aos " + idade
                    + " dias: a temperatura do galpão pode estar certa e a cama, não. Trocar o bico, a placa molhada e o trecho de cama. É porta de coccidiose, amônia e calo de pata.",
                    "Frango de corte", String.valueOf(idade), "28", "75", "20",
                    "poça embaixo do nipple aos " + idade + " dias");
        }
        for (int idade : new int[] {200, 260, 340}) {
            por(c, Carga.AMB, "Umidade alta no galpão de postura aos " + idade
                    + " dias molha a esteira e a casca do ovo. Ventilar e revisar vazamento. Ovo sujo de umidade vira problema de bactéria, não só de aparência.",
                    "Poedeira", String.valueOf(idade), "24", "90", "18",
                    "ovo úmido na esteira aos " + idade + " dias");
        }
        for (int idade : new int[] {40, 60, 90}) {
            por(c, Carga.NH3, "Abrigo de caipira fechado à noite aos " + idade
                    + " dias concentra amônia. Deixar saída de ar alta, sem corrente no poleiro. Cheiro forte de manhã é cama úmida, não 'gosto da ave'.",
                    "Galinha caipira", String.valueOf(idade), "18", "80", "45",
                    "cheiro forte no abrigo de manhã aos " + idade + " dias");
        }
    }

    private static void nutricao(Connection c) throws SQLException {
        abrir(c, "Nutrição");
        racao(c, Carga.NUT, "Frango de corte", "7 14 21",
                "perna mole e articulações inchadas", "ração sem vitamina D",
                "Raquitismo em {especie} aos {idade} dias: vitamina D, cálcio e fósforo da fase. Trocar a ração. Não misturar calcário por cima do comedouro.");
        racao(c, Carga.NUT, "Frango de corte", "10 16 22",
                "ave de lado com a ração cheirando a ranço", "gordura oxidada sem vitamina E",
                "Deficiência de vitamina E em {especie} aos {idade} dias: descartar a ração rançosa. O quadro nervoso do cerebelo não se trata com antibiótico.");
        racao(c, Carga.NUT, "Frango de corte", "14 28",
                "olho lacrimejando e refugo com milho velho", "milho armazenado sem vitamina A",
                "Deficiência de vitamina A em {especie} aos {idade} dias: mucosa seca e refugo. Corrigir o premix e a idade do milho. Não é só poeira do galpão.");
        racao(c, Carga.NUT, "Frango de corte", "21 35",
                "calo de pata e pena ruim com premix vencido", "ração sem biotina",
                "Deficiência de biotina em {especie} aos {idade} dias: a Embrapa cita biotina entre as vitaminas que a micotoxina e a ração fraca derrubam. Trocar o premix. A lesão de pata também pede cama seca.");
        racao(c, Carga.ASCITE, "Frango de corte", "10 18 28",
                "sede alta e cama molhada depois da ração nova", "ração com sódio alto",
                "Sódio alto em {especie} aos {idade} dias: a faixa da Embrapa para frango é 0,16% a 0,20%. Na água, sódio perto de 50 ppm já merece conta. Excesso molha a cama e entra na lista da ascite.");
        racao(c, Carga.NUT, "Frango de corte", "14 21",
                "lote bicando e conversão ruim com ração sem sal", "ração com sódio baixo",
                "Falta de sódio em {especie} aos {idade} dias: a ave bica e cresce mal. Corrigir a fórmula. Não oferecer sal grosso no comedouro.");
        racao(c, Carga.NUT, "Frango de corte", "21 35",
                "cama muito escura e fósforo sobrando na excreta", "ração sem fitase",
                "Fósforo alto na excreta de {especie} aos {idade} dias: fitase e fósforo disponível no lugar de subir o fosfato. A Embrapa liga isso ao plano de nutrientes da propriedade.");
        racao(c, Carga.DOC107, "Frango de corte", "4 8",
                "ração no chão e pintinho comendo cama", "farelada grossa demais para a semana",
                "Partícula grossa em {especie} aos {idade} dias: na primeira semana a farelada fina o bastante para o bico reduz sobra, cascudinho e rato.");
        racao(c, Carga.NUT, "Frango de corte", "21 35",
                "ave escolhe o pó e deixa o pelete inteiro", "pelete duro demais",
                "Pelete duro em {especie} aos {idade} dias: sobra no comedouro e desuniformidade. Ajustar a dureza com a fábrica. Não moer tudo de volta se a idade já aceita o grão.");
        racao(c, Carga.IMUNO, "Frango de corte", "18 28 36",
                "refugo e vacina falha com milho ardido", "milho com fumonisina",
                "Fumonisina em {especie} aos {idade} dias: tirar o milho ardido. A toxina imunossuprime junto com aflatoxina. Não contar com a vacina enquanto o silo seguir úmido.");
        racao(c, Carga.IMUNO, "Frango de corte", "16 24 32",
                "vômito de ingesta e lesão oral com trigo novo", "ração com vomitoxina",
                "Vomitoxina em {especie} aos {idade} dias: recusa de ração e lesão de boca. Trocar o cereal. Não forçar jejum para 'abrir o apetite'.");
        racao(c, Carga.NUT, "Frango de corte", "14 21",
                "queda de ganho no dia da troca para a ração seguinte", "troca de fase adiantada",
                "Troca de ração adiantada em {especie} aos {idade} dias: a fase de crescimento não substitui a inicial antes da hora. Voltar à fase se a troca foi precoce e o ganho caiu.");
        racao(c, Carga.IMUNO, "Frango de corte", "28 35",
                "comedouro com torrão e fígado alterado", "ração empedrada no silo",
                "Ração úmida em {especie} aos {idade} dias: parar o trato, ventilar o silo e não devolver a sobra mofada. Aflatoxina anula ganho e vacina.");
        racao(c, Carga.NUT, "Frango de corte", "14 28",
                "água marrom e fezes escuras sem sangue", "água de poço com ferro",
                "Ferro na água de {especie} aos {idade} dias: a ave bebe menos e a ração rende menos. Filtrar ou trocar a fonte. Não confundir fezes escuras com coccidiose se não houver lesão de intestino.");
        racao(c, Carga.AMBI, "Frango de corte", "21 35 42",
                "cama amoniacal com fórmula muito proteica", "excesso de proteína bruta",
                "Proteína sobrando em {especie} aos {idade} dias: formular perto da exigência e usar enzima. Nitrogênio a mais vira amônia. A Embrapa trata nitrogênio e fósforo no plano de nutrientes.");
        racao(c, Carga.NUT, "Frango de corte", "21 35",
                "conversão alta com ave com fome e ração farelada leve", "energia abaixo da fase",
                "Energia baixa em {especie} aos {idade} dias: a ave come mais e a cama molha. Ajustar a energia da fase. Não resolver só aumentando a lotação do comedouro.");
        racao(c, Carga.NUT, "Frango de corte", "20 30",
                "ração com cheiro de ranço e queda de consumo", "gordura rancificada",
                "Gordura rançosa em {especie} aos {idade} dias: descartar. A oxidação consome a vitamina E da fórmula. Limpar o silo antes da ração nova.");
        racao(c, Carga.NUT, "Poedeira", "180 240 300 360",
                "ovo quebrado e casca fina", "ração de postura com pouco cálcio",
                "Casca fina em {especie} aos {idade} dias: cálcio, fósforo e vitamina D da fase de postura. Calor e micotoxina também afinam a casca. Não corrigir só com calcário jogado no comedouro.");
        racao(c, Carga.NUT, "Poedeira", "200 280",
                "casca rugosa com fósforo acima da postura", "fósforo em excesso na postura",
                "Fósforo alto em {especie} aos {idade} dias atrapalha a casca tanto quanto a falta de cálcio. Reformular. Não empilhar as duas correções ao mesmo tempo sem conta.");
        racao(c, Carga.NUT, "Poedeira", "190 260",
                "ovo mole no fim do ciclo de um premix velho", "premix de postura vencido",
                "Vitamina D insuficiente em {especie} aos {idade} dias: premix vencido ou estoque quente. Trocar. A ave de gaiola não toma sol para compensar.");
        racao(c, Carga.NUT, "Poedeira", "150 170",
                "ave magra entrando em postura com ração de crescimento", "ração de frango na poedeira",
                "Ração errada em {especie} aos {idade} dias: crescimento de frango não sustenta a casca. Passar para a fórmula de pré-postura ou postura conforme o peso e a idade.");
        racao(c, Carga.IMUNO, "Poedeira", "210 300",
                "postura em queda com silo empedrado", "ração de postura mofada",
                "Micotoxina em {especie} aos {idade} dias: trocar a ração e secar o silo. Queda de ovo por toxina se confunde com salmonela; na dúvida, o veterinário decide a notificação.");
        racao(c, Carga.CALOR, "Poedeira", "220 280",
                "consumo baixo e ovo pequeno com água morna no verão", "água quente na linha",
                "Água quente em {especie} aos {idade} dias: a ave come menos no calor. Isolar o tanque, dar água fresca e oferecer ração nas horas frias. Concentrar nutriente sem baixar a temperatura não recupera a postura.");
        racao(c, Carga.DOC107, "Galinha caipira", "40 70 100",
                "ave leve no piquete recebendo só milho", "milho sem núcleo",
                "Só milho em {especie} aos {idade} dias: falta proteína, mineral e vitamina. O pasto não fecha a fórmula. Núcleo indicado para a fase, em quantidade que a ave de fato coma.");
        racao(c, Carga.DOC107, "Galinha caipira", "60 90",
                "postura fraca de ave solta sem suplemento", "pasto sem núcleo",
                "Pasto sem suplemento em {especie} aos {idade} dias: a galinha caipira precisa de ração balanceada além do verde. Sem isso a casca e a muda vêm juntas.");
        racao(c, Carga.NUT, "Galinha caipira", "120 180",
                "ovo de casca mole na criação colonial", "sem fonte de cálcio",
                "Casca mole em {especie} aos {idade} dias: oferecer cálcio da fase, não só resto de mesa. Conferir se a ave come o núcleo ou só escolhe o milho.");
        racao(c, Carga.IMUNO, "Galinha caipira", "30 50 80",
                "refugo depois de ração guardada em tambor aberto", "ração mofada no tambor",
                "Ração mofada em {especie} aos {idade} dias: tambor sem tampa umedece. Descartar o empedrado. A toxina derruba a vacina de Newcastle e de bouba do calendário de caipira.");
    }

    private static void racao(Connection c, String fonte, String especie, String idades, String quadro, String alimento,
            String solucao) throws SQLException {
        for (String idade : idades.split(" ")) {
            por(c, fonte, solucao.replace("{idade}", idade).replace("{especie}", especie),
                    especie, idade, quadro + " aos " + idade + " dias", alimento);
        }
    }

    private static void manejo(Connection c) throws SQLException {
        abrir(c, "Manejo");
        String[] linhas = {
                "Frango de corte|1|Convencional|cama úmida descarregada no dia do alojamento|Cama acima de cerca de 20% de umidade não entra no galpão. Secar ou devolver. Espalhar 5 a 10 cm só com o piso já lavado.",
                "Frango de corte|1|Convencional|cama com cheiro de produto químico no alojamento|Cama com químico ou pó de fábrica de móveis irrita o pintinho e traz aspergilose. Trocar antes de soltar a ave.",
                "Frango de corte|1|Convencional|maravalha em pó cobrindo o comedouro|Partícula fina demais vira poeira e o pintinho come pó. Preferir partícula média, seca, sem mofo.",
                "Frango de corte|1|Convencional|casca de arroz disponível no lugar da maravalha|A Embrapa comparou casca de arroz, capim, resto de cultura e serragem. Vale se estiver seca, sem químico, e se o destino da cama depois do lote estiver combinado.",
                "Frango de corte|1|Convencional|capim napier seco espalhado no pinteiro|Capim seco pode substituir maravalha quando a região não tem madeira. Tem de chegar seco e picado, não verde.",
                "Frango de corte|2|Convencional|cama com menos de 5 cm e piso aparecendo|Cama fina demais não isola o pintinho do piso. A faixa prática é 5 a 10 cm.",
                "Frango de corte|2|Convencional|cama passada de 10 cm só por sobra de material|Cama alta demais segura umidade no fundo e custa sem ganho. Ficar entre 5 e 10 cm.",
                "Frango de corte|3|Convencional|círculo pequeno com pintinho por cima do outro|Círculo apertado concentra calor e impede todos de chegarem na água. Ampliar e medir a temperatura na altura da ave.",
                "Frango de corte|3|Convencional|círculo grande demais e pintinho longe do aquecimento|Círculo largo demais esfria a borda. A primeira semana pede 32 a 35 ºC onde o pintinho está, não no meio do galpão.",
                "Frango de corte|4|Convencional|campânula alta e pintinho piando|Campânula alta demais não aquece a cama. Baixar até o comportamento espalhar o pintinho em volta, sem amontoar e sem fugir.",
                "Frango de corte|4|Convencional|campânula baixa e pintinho fugindo do centro|Calor direto em excesso. Subir a campânula. Fuga do centro é calor; amontoamento é frio.",
                "Frango de corte|1|Convencional|bebedouro vazio na hora de soltar o pintinho|Água antes da ração na chegada. Pintinho desidratado da viagem não espera o nipple encher.",
                "Frango de corte|3|Convencional|nipple na altura do olho do pintinho|Nipple alto faz o pintinho não beber. Na primeira semana a gota fica na altura do olho, e sobe com o dorso da ave.",
                "Frango de corte|10|Convencional|nipple baixo com poça na cama|Nipple baixo demais pinga e encharca. Subir a linha e trocar o trecho molhado.",
                "Frango de corte|5|Convencional|borda do comedouro na altura do pescoço|Comedouro alto aumenta sobra no chão e refugo. A borda acompanha o dorso da ave.",
                "Frango de corte|6|Convencional|ração espalhada na cama do pinteiro|Ração no chão atrai cascudinho e rato e o pintinho come cama. Ajustar a regulagem e a partícula.",
                "Frango de corte|12|Dark house|luz acesa sem pausa no inverno|No frio, programa intermitente. Luz contínua empurra consumo, umedece a cama e aumenta morte súbita em ave pesada.",
                "Frango de corte|2|Convencional|pinteiro escuro com pintinho que não acha a água|Na chegada a ave precisa ver o nipple e o comedouro. Escurecer vem depois, no programa da fase, não no dia 2.",
                "Frango de corte|6|Convencional|cortina fechada o dia inteiro e cheiro forte|Cortina fechada sem renovar o ar segura amônia e umidade. Abrir por cima, sem vento direto no pintinho.",
                "Frango de corte|5|Convencional|cortina aberta com vento batendo na cama|Vento direto esfria a primeira semana mesmo com o aquecedor ligado. Proteger o círculo.",
                "Frango de corte|30|Convencional|perto de 20 aves por metro sem exaustor no calor|Densidade alta sem climatização sobe a mortalidade no verão. De 10 aves/m² pode-se ir a 15–18 com ambiente adaptado. Perto de 22 exige alta tecnologia.",
                "Frango de corte|7|Convencional|galpão quase vazio e pintinho perdido no escuro|Densidade baixa demais no pinteiro esfria e a ave não acha o comedouro. Manter o círculo até o lote ocupar o espaço.",
                "Frango de corte|42|Convencional|apanha com ave empilhada e asa quebrada|Apanha violenta vira hematoma, asa quebrada e celulite. Treinar a equipe e não encher a caixa além do espaço.",
                "Frango de corte|42|Convencional|jejum longo demais antes do abate com ave debilitada|Jejum passado debilita e gera condenação de vesícula e papo vazio demais. Seguir a janela da integração, com água até o momento combinado.",
                "Frango de corte|42|Dark house|jejum curto e papo cheio na plataforma|Jejum curto demais leva ração ao abate e contamina a carcaça. Começar no horário calculado pela distância, não 'quando o caminhão chegar'.",
                "Frango de corte|21|Convencional|lote sem pesagem na troca de ração|Sem peso não se sabe se a fase da ração está certa. Pesar amostra em vários pontos do galpão.",
                "Frango de corte|15|Dark house|mortalidade do dia não anotada na ficha|A ficha de mortalidade, vacina e visita é parte da sanidade. Sem ela o caso seguinte não tem o que reutilizar.",
                "Frango de corte|14|Convencional|vacina na água com cloro sem neutralizar|Cloro mata a vacina viva. Neutralizar o cloro, usar a água no prazo e só vacinar ave sadia, como no calendário de Gumboro, bronquite e Newcastle.",
                "Frango de corte|16|Convencional|vacina aplicada em lote prostrado|Vacina viva em ave doente não protege e piora o quadro. Adiar com o veterinário. Gumboro e bronquite pedem ave sadia no dia.",
                "Frango de corte|12|Convencional|pintinho novo no mesmo piso do lote de 30 dias|Idades misturadas passam Marek, Gumboro e salmonela. Separar pinteiro de lote velho.",
                "Frango de corte|20|Convencional|tela lateral rasgada na altura do comedouro|Tela de malha de no máximo 2 cm corta ave silvestre. Buraco aberto desfaz a barreira. Remendar antes do fim do dia.",
                "Frango de corte|18|Convencional|pássaro comendo ração no corredor|Ave silvestre come ração e traz patógeno. Telar e cobrir a sobra. A obrigação da tela varia com a norma vigente; o risco não varia.",
                "Frango de corte|22|Convencional|rato atravessando o depósito de ração à noite|Rato come ração e transmite salmonela. Fechar fresta, recolher sobra e usar isca fora do alcance da ave, com orientação.",
                "Frango de corte|25|Convencional|carcaça do dia anterior no corredor|Carcaça no corredor atrai mosca, rato e botulismo. Recolher todo dia para a compostagem.",
                "Frango de corte|0|Convencional|alojamento no dia seguinte à apanha sem lavar|Vazio sanitário curto mantém cascudinho, vírus e bactéria. Lavar, desinfetar e secar antes do pintinho novo.",
                "Frango de corte|0|Dark house|desinfecção com o piso ainda enlameado|Desinfetante em cima de matéria orgânica não age. Lavar, enxaguar e só então desinfetar, com o piso secando depois.",
                "Frango de corte|1|Convencional|cama do lote anterior espalhada sem tratamento|Cama reusada sem fermentação ou tratamento devolve salmonela e cascudinho. Não reutilizar cama de lote suspeito.",
                "Frango de corte|1|Convencional|fermentação de cama interrompida no segundo dia|Fermentação curta não segura o cascudinho. Completar o processo ou trocar o material. Não alojar em cima de cama quente e úmida.",
                "Frango de corte|1|Colonial|bebedouro emprestado do vizinho sem lavar|Equipamento emprestado entra com a doença do outro galpão. Lavar e desinfetar antes de encostar na ave.",
                "Frango de corte|0|Convencional|caixa de pintinho molhada de chuva na descarga|Caixa molhada esfria e cola pintinho. Descarregar coberto. Umbigo e onfalite começam nessa hora.",
                "Frango de corte|0|Convencional|pintinho esperando mais de meio dia no caminhão|Atraso de alojamento desidrata. Água imediata e temperatura de primeira semana assim que soltar.",
                "Frango de corte|28|Convencional|balança do galpão descalibrada na pesagem|Peso errado troca a ração na hora errada. Conferir a balança com peso conhecido antes de decidir a fase.",
                "Galinha caipira|40|Colonial|piquete sem sombra na parte da tarde|Sombra e água no piquete. Ave ofegante no sol não se resolve com mais milho.",
                "Galinha caipira|30|Colonial|piquete enlameado em volta do abrigo|Lama na porta do abrigo é coccidiose e calo de pata. Drenar e trocar o trecho. Rotacionar o pasto.",
                "Galinha caipira|90|Colonial|apanha de caipira no escuro com ave amontoada|Apanha calma, de preferência com a ave no poleiro, sem empilhar. Fratura e sufocamento estragam o lote no último dia.",
                "Poedeira|200|Postura em gaiola|ninho e esteira com ovo quebrado acumulado|Ovo quebrado na esteira atrai mosca e bactéria. Coletar mais vezes e limpar a esteira no mesmo dia.",
                "Poedeira|180|Postura em gaiola|muitos ovos de cama porque o ninho está sujo|Ovo no chão suja a casca e ensina o lote. Limpar o ninho e recolher cedo. Na gaiola, a esteira suja faz o mesmo papel.",
                "Poedeira|220|Postura em gaiola|gaiola torta com ave presa na grade|Gaiola amassada prende a ave e machuca a crista. Ajustar a grade. Ferimento vira porta de cólera e de bico das vizinhas.",
                "Poedeira|160|Postura em gaiola|luz de postura com poucas horas e ave sem ovo|Poedeira que não recebe o programa de luz da fase atrasa a postura. Seguir as horas da linhagem. Não compensar com ração de frango.",
                "Poedeira|300|Postura em gaiola|muda de pena no meio da postura sem programação|Muda fora de hora vem de luz, peso ou ração. Corrigir a causa antes de forçar a ave magra a produzir.",
                "Frango de corte|20|Colonial|poço sem tratamento e água turva no tambor|Água turva reduz o consumo e carrega bactéria. Proteger o poço e a caixa. Cloro e análise com orientação, sem dose inventada.",
                "Galinha caipira|50|Colonial|poleiro liso e úmido com calo de peito|Poleiro úmido e sujo faz calo e ácaro. Secar, lixar a quina viva e trocar a cama debaixo do poleiro.",
        };
        for (String linha : linhas) {
            String[] p = linha.split("\\|");
            por(c, Carga.DOC107, p[4], p[0], p[1], p[2], p[3]);
        }
    }

    private static void incubacao(Connection c) throws SQLException {
        abrir(c, "Incubação");
        String[] linhas = {
                "Incubação|38.6|58|embrião adiantado com 38,6 ºC|Acima de cerca de 37,7 ºC o embrião acelera e morre no fim. No industrial, a Embrapa cita redução para perto de 36,8 ºC nos dias finais. Ageitec trabalha 37,3 a 37,7 ºC.",
                "Incubação|38.0|60|eclosão adiantada e pinto pequeno|Temperatura um pouco acima da faixa adianta o nascimento e deixa o pinto miúdo. Voltar para perto de 37,7 ºC e conferir o termômetro, não só o visor.",
                "Incubação|36.2|60|eclosão atrasada com 36,2 ºC|Temperatura baixa segura o embrião. Voltar à faixa. Morte no início também aparece se o ovo foi guardado abaixo de 2 ºC.",
                "Incubação|35.0|62|muitos embriões parados na primeira semana|35 ºC não incuba. Corrigir o termostato e o sensor de lugar, encostado no ovo e não na lâmpada.",
                "Incubação|37.7|42|casca seca e membrana colada no pinto|Umidade interna da chocadeira entre 55% e 65%, bulbo úmido de 28 a 30 ºC. Abaixo disso o ovo resseca e o pinto não sai.",
                "Incubação|37.7|48|pinto preso na membrana com 48% de umidade|Umidade ainda baixa. Completar a água do reservatório e medir de novo. Não abrir a máquina a toda hora para 'ajudar'.",
                "Eclosão|37.2|85|pinto mole e penugem colada com 85% de umidade|Umidade alta demais no nascedouro deixa o pinto mole e o umbigo aberto. Baixar para a faixa e só subir na eclosão conforme o manual da máquina.",
                "Eclosão|37.0|80|umbigo mal fechado em lote inteiro|Umidade de eclosão alta e temperatura caindo juntas. Ajustar as duas. Umbigo aberto vira onfalite no galpão.",
                "Incubação|37.7|60|embrião colado na casca sem viragem|Virar a cada 2 horas. No industrial, da fase inicial até cerca do 16º dia, movimentos de 45 graus de hora em hora.",
                "Incubação|37.7|60|viragem só de manhã e à noite|Duas vezes ao dia não bastam na chocadeira. O embrião gruda e a troca de calor piora. Voltar ao intervalo de cerca de 2 horas.",
                "Armazenamento|22|70|ovos de 10 dias em temperatura ambiente|Não passar de 7 dias fora da geladeira. Ponta para baixo e virar ao menos duas vezes ao dia.",
                "Armazenamento|24|65|ovos de 14 dias na despensa|Duas semanas em ambiente derrubam a eclosão. Geladeira, nunca abaixo de 8 ºC, até cerca de 25 dias.",
                "Armazenamento|18|75|ovos de 20 dias entre 8 e 18 ºC|Dá para guardar mais que 7 dias no frio, sem descer de 8 ºC e sem passar de cerca de 25 dias. Virar o ovo.",
                "Armazenamento|6|80|ovos guardados a 6 ºC|Abaixo de 8 ºC, e pior abaixo de 2 ºC, sobe a morte no início. Ajustar a geladeira.",
                "Armazenamento|36|50|ovos deixados no ninho no calor da tarde|Acima de cerca de 35 ºC o embrião começa e morre nos últimos dias. Recolher no mínimo duas vezes ao dia e guardar à sombra.",
                "Armazenamento|1|70|ovos que tomaram geada no galpão|Abaixo de 2 ºC a morte inicial aumenta. Descartar o lote que gelou e não misturar com ovo do dia na mesma bandeja.",
                "Eclosão|37.2|65|nascedouro aberto sem quebrar ovo não eclodido|Quebrar 200 a 300 ovos e anotar a idade da morte, a posição depois do 16º dia e os ovos claros. Isso separa matriz, manejo do ovo e máquina.",
                "Incubação|38.4|72|cheiro forte e pinto fraco com ar parado|O embrião produz gás carbônico. Ventilar conforme o fabricante. Ar parado se soma à temperatura alta no fim.",
                "Eclosão|37.7|60|maioria de ovos claros na bandeja|Ovo claro é fertilidade e manejo do ovo na granja, não termostato. Cruzar com o galpão de matrizes antes de recalibrar.",
                "Ovoscopia|37.7|60|ovoscopia só no 18º dia sem registro|Ovoscopia tardia não separa ovo claro de morte precoce. Anotar o que se vê. O diagnóstico do nascedouro começa aí.",
                "Ovoscopia|37.7|58|muitos ovos claros já no 7º dia|Falha de galo, de ovo velho ou de ovo que esquentou no ninho. Não é a umidade da máquina.",
                "Transferência|37.5|65|transferência no 15º dia|Transferir cedo demais interrompe a viragem. O habitual é perto do 18º dia, conforme a máquina. Antecipar aumenta embrião colado.",
                "Transferência|37.0|70|transferência no 20º dia com pinto já bicando|Transferência tardia quebra pinto e esfria o ovo no caminho. Fazer na janela da máquina, com a sala aquecida.",
                "Transferência|25|60|bandeja esfriando no corredor na transferência|O ovo não pode esfriar no corredor. Sala de transferência aquecida e caminho curto.",
                "Eclosão|37.8|75|janela de nascimento muito comprida|Nascimento espalhado em muitas horas mistura pinto seco e pinto atrasado. Uniformizar a idade do ovo e a temperatura. Não misturar ovo de 3 e de 10 dias na mesma máquina.",
                "Incubação|37.7|60|ovo pequeno e grande na mesma bandeja|Ovo de tamanho muito diferente não nasce junto. Separar as bandejas. O pequeno desidrata antes.",
                "Incubação|37.7|60|ovo de matriz velha com casca porosa|Casca porosa perde água rápido. Subir um pouco a umidade dentro da faixa e não misturar com ovo de matriz nova sem anotar.",
                "Incubação|37.7|55|ovo de matriz nova com clara grossa e eclosão ruim|Matriz no início de postura dá ovo que incuba pior. Não encher a máquina só com esse ovo sem avisar o nascedouro.",
                "Incubação|37.7|60|ovo trincado incubado com o lote|Ovo trincado contamina a bandeja. Tirar na ovoscopia. Não 'aproveitar' trinca na chocadeira.",
                "Incubação|37.7|60|ovo de cama sujo incubado sem seleção|Ovo sujo de cama estoura e contamina os vizinhos. Selecionar na granja. Lavar ovo fértil caseiro é risco, não solução.",
                "Incubação|37.7|60|ovo lavado com água fria antes da máquina|Água mais fria que o ovo puxa bactéria para dentro. Não lavar. Descartar o muito sujo.",
                "Armazenamento|15|85|ovo suando ao sair da geladeira|Suor na casca é porta de bactéria. Deixar o ovo chegar à temperatura da sala antes de incubar, sem corrente suja.",
                "Incubação|37.7|60|ovo com a ponta fina para cima|Ponta fina para cima aumenta mau posicionamento. Guardar e incubar com a câmara de ar para cima.",
                "Eclosão|37.2|68|pinto bicando na ponta fina|Mau posicionamento: ponta para cima na bandeja ou falta de viragem. Corrigir a posição no lote seguinte.",
                "Eclosão|37.0|60|pinto morto depois de bicar sem sair|Bicou e não saiu: umidade baixa, casca dura ou energia do ovo no fim. Medir a perda de peso do ovo e a umidade da eclosão.",
                "Eclosão|37.4|78|pinto com jarrete vermelho|Jarrete vermelho indica dificuldade de sair, umidade ou temperatura fora no fim. Ajustar a eclosão e não mandar esse pinto misturado sem olhar o umbigo.",
                "Incubação|39.0|60|queda de energia com a máquina fechada no calor|Depois da falta de luz, abrir a ventilação conforme o fabricante e medir. Embrião no fim morre rápido sem ar e com calor acumulado.",
                "Incubação|37.7|60|termômetro da tampa diferente do termômetro do ovo|Confiar no termômetro calibrado na altura do ovo. Visor errado explica eclosão ruim com 'temperatura certa'.",
                "Incubação|37.7|60|bulbo úmido seco porque a gaveta de água esvaziou|Bulbo úmido seco mente a umidade para cima do problema. Completar a água e refazer a leitura.",
                "Eclosão|37.2|65|bandeja de plástico suja do lote anterior|Bandeja com resto de ovo contamina o nascedouro. Lavar e secar entre cargas.",
                "Incubação|37.6|60|ovos de galinha e de pata na mesma chocadeira|Espécie e tempo de incubação diferentes não dividem a mesma regulagem. Separar as cargas.",
                "Eclosão|37.0|66|pinto retirado ainda molhado no meio da eclosão|Retirar cedo demais esfria quem ainda não nasceu. Esperar a janela e tirar os secos, com o umbigo fechado.",
                "Eclosão|36.5|60|nascedouro frio na retirada|Sala fria na retirada cola o pinto e abre umbigo. Ambiente aquecido até a caixa de expedição.",
                "Incubação|37.7|60|carga acima da lotação da chocadeira|Máquina cheia demais não ventila o centro. Respeitar a lotação do fabricante.",
                "Ovoscopia|37.7|60|ovo morto deixado na bandeja até o fim|Ovo morto deixado na máquina estoura e contamina. Tirar na ovoscopia.",
                "Armazenamento|28|60|ovo fértil ao sol na carroça entre a granja e a máquina|Sol na carroça passa de 35 ºC e mata o embrião no fim. Transportar à sombra e sem balanço que trinque a casca.",
        };
        for (String linha : linhas) {
            String[] p = linha.split("\\|");
            por(c, Carga.INC, p[4], p[0], p[1], p[2], p[3]);
        }
    }

    private static void biosseguridade(Connection c) throws SQLException {
        abrir(c, "Biosseguridade");
        String[] linhas = {
                "Convencional|visita de outro galpão sem trocar a bota|pessoa|Barreira na porta, bota e roupa do galpão, e livro de visitas. Pessoa é a via mais comum entre granjas.",
                "Convencional|equipe de vacina entrou com a mesma caixa do aviário anterior|pessoa|A equipe de vacina troca bota e desinfeta a caixa entre galpões. Agulha e frasco não seguem de um lote doente para o sadio.",
                "Convencional|apanha com a equipe de rua dentro do pinteiro|pessoa|Equipe de apanha usa a roupa da granja e não atravessa o pinteiro sujo para o galpão ao lado no mesmo turno.",
                "Dark house|veterinário atendeu o lote doente e em seguida o sadio sem troca|pessoa|A ordem da visita é do sadio para o doente, com troca de bota e macacão. O contrário leva o quadro no sapato.",
                "Convencional|funcionário com galinha no quintal de casa|pessoa|Quem tem ave em casa troca toda a roupa e a bota antes do galpão. Aves de quintal sustentam Newcastle e micoplasma.",
                "Colonial|criança entrou no piquete depois de visitar o vizinho|pessoa|Visita, mesmo da família, passa pela barreira. Piquete não é exceção.",
                "Postura em gaiola|coletor de ovo foi ao galpão doente e voltou à classificação|pessoa|O fluxo do ovo não volta para o galpão sadio. Separar pessoa e bandeja.",
                "Convencional|caminhão de ração encostou na porta do galpão|veículo|Descarregar no perímetro. O pneu não cruza a área limpa. O mesmo vale para o caminhão de apanha.",
                "Dark house|caminhão de pintinho entrou sem arco de desinfecção|veículo|Arco ou bomba na roda antes do pátio. Caixa de pintinho não encosta no chão sujo.",
                "Convencional|trator de cama usado no vizinho no mesmo dia|veículo|Trator e carreta de cama são veículo. Lavar antes de entrar. Cama do vizinho não atravessa a porteira.",
                "Postura em gaiola|caminhão de ovo voltou com bandeja suja|veículo|Bandeja que volta da classificação entra limpa. Bandeja suja recoloca salmonela no ninho.",
                "Colonial|moto do tratador entrou no piquete|veículo|Veículo leve também carrega fezes no pneu. Parar fora do piquete.",
                "Convencional|fezes de rato no saco de ração|roedor|Fechar fresta, recolher sobra e isca fora do alcance da ave, com orientação. Rato transmite salmonela.",
                "Dark house|toca de rato no rodapé do dark house|roedor|Rodapé aberto mantém o rato entre lotes. Fechar na lavagem, não só pôr isca em cima da toca.",
                "Postura em gaiola|camundongo na esteira de ovo|roedor|Esteira com rato suja o ovo. Vedar a sala de ovo e não deixar ovo quebrado de um dia para o outro.",
                "Colonial|rato no milho da tulha|roedor|Tulha fechada e saco fora do chão. Milho com fezes de rato não volta para a ave.",
                "Convencional|pombo no telhado comendo ração derramada|ave silvestre|Ração coberta e tela de no máximo 2 cm. Pombo e pardal levam patógeno e comem ração.",
                "Postura em gaiola|pássaro no comedouro da gaiola californiana|ave silvestre|Telar. A norma do MAPA sobre galpão californiano muda com a versão vigente; o pássaro no comedouro continua sendo risco.",
                "Colonial|garça no bebedouro do piquete|ave silvestre|Proteger o bebedouro e não usar açude aberto como água da ave. Ave aquática entra na suspeita de influenza quando há mortalidade.",
                "Convencional|ninho de pardal na tesoura do telhado|ave silvestre|Tirar o ninho na lavagem e fechar o vão. Pardal no telhado recontamina a cama nova.",
                "Colonial|carcaça no pasto atrás do piquete|carcaça|Compostar. Não deixar no mato nem em curso d'água. Mosca e rato devolvem o problema.",
                "Convencional|fossa de mortos aberta na chuva|carcaça|Fossa aberta é foco. Cobrir ou passar para compostagem. Carcaça não segue para o vizinho nem para o rio.",
                "Dark house|balde de mortos do dia esquecido no escritório|carcaça|O balde sai todo dia para a composteira. Mortos no escritório atraem mosca para dentro da área limpa.",
                "Convencional|composteira seca demais e carcaça inteira aparecendo|carcaça|Composteira seca não transforma a carcaça. Cobrir com o material e manter a umidade do processo, sem virar poça.",
                "Convencional|cama de outro aviário espalhada no piso|cama|Não reutilizar cama de lote com suspeita de salmonela. Lavar e desinfetar antes da cama nova.",
                "Dark house|cama úmida do lote anterior fermentada pela metade|cama|Fermentação incompleta devolve cascudinho. Terminar o processo ou descartar.",
                "Colonial|cama do abrigo jogada no piquete sem curtir|cama|Cama fresca no piquete concentra parasita na porta. Curtir longe da ave e não em cima do pasto de hoje.",
                "Convencional|cascudinho na recria da cama nova|inseto|Cascudinho guarda Gumboro, Marek e salmonela. Tratar o piso na lavagem. Inseto não se resolve só com veneno em cima da ave.",
                "Colonial|mosca em cima da composteira aberta|inseto|Mosca leva bactéria do morto para o comedouro. Fechar a composteira e recolher carcaça cedo.",
                "Colonial|mosquito no piquete com bouba no lote|inseto|Mosquito espalha bouba. Reduzir água parada e vacinar o lote seguinte. Apartar as aves com crosta.",
                "Convencional|poça de chuva usada sem querer na limpeza do bebedouro|água|Água de poça não entra no tanque. Caixa coberta e analisada. Água suja sustenta salmonela e cólera.",
                "Dark house|caixa d'água sem tampa|água|Caixa aberta recebe pássaro e rato. Tampar. A mesma água vai para a vacina.",
                "Postura em gaiola|biofilme na linha de nipple da postura|água|Linha com biofilme reduz o consumo e suja o ovo. Limpar a linha no vazio, com orientação, e não no meio da vacina.",
                "Convencional|saco de ração rasgado no corredor|ração|Saco rasgado é rato e pássaro. Não varrer ração do chão de volta para o comedouro.",
                "Colonial|sobra de ração do dia misturada com a nova|ração|Sobra úmida mofa. Não devolver para o saco. O tambor fecha no mesmo dia.",
                "Convencional|nebulizador emprestado do galpão doente|equipamento|Equipamento emprestado volta lavado e desinfetado. Nebulizador leva poeira de um aviário para o outro.",
                "Dark house|pá e ancinho guardados sujos entre lotes|equipamento|Ferramenta suja de cama entra no lote novo com o que a lavagem tirou do piso. Lavar junto com o galpão.",
                "Postura em gaiola|bandeja de ovo emprestada da granja vizinha|equipamento|Bandeja emprestada é veículo de salmonela. Usar a da própria granja, lavada.",
                "Colonial|galinha do vizinho solta na cerca|vizinho|Cerca fechada. Ave do vizinho no piquete mistura os status sanitários. Avisar e devolver, sem 'incorporar' a ave.",
                "Convencional|esterco do vizinho espalhado no pasto ao lado da cortina|vizinho|Esterco fresco na cerca é poeira e bactéria na entrada de ar. Não espalhar dejeto cru ao lado do galpão.",
        };
        for (String linha : linhas) {
            String[] p = linha.split("\\|");
            por(c, Carga.SAN, p[3], p[0], p[1], p[2]);
        }
    }

    private static int[] conforto(int idade) {
        if (idade <= 7) {
            return new int[] {32, 35};
        }
        if (idade <= 14) {
            return new int[] {29, 32};
        }
        if (idade <= 21) {
            return new int[] {26, 29};
        }
        if (idade <= 28) {
            return new int[] {23, 26};
        }
        if (idade <= 35) {
            return new int[] {20, 23};
        }
        return new int[] {20, 20};
    }
}
