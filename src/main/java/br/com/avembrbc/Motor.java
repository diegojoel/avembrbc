package br.com.avembrbc;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class Motor {

    public static final int MAXIMO = 8;

    private static final Set<String> STOP = Set.of(
            "a", "o", "as", "os", "e", "de", "da", "do", "das", "dos", "em", "no", "na", "nos", "nas",
            "um", "uma", "com", "por", "para", "que", "se", "ou", "ao", "aos");

    private Motor() {
    }

    public static List<Recuperado> recuperar(Banco banco, long areaId, Map<Long, String> problema, boolean soValidos) {
        List<Banco.Campo> campos = banco.campos(areaId);
        if (!temValor(campos, problema)) {
            throw new IllegalArgumentException("Descreva o problema em ao menos um campo.");
        }
        List<Banco.Caso> casos = banco.casos(areaId);
        List<String[]> frases = banco.frases();
        Tabela tabela = Tabela.de(banco.pares());
        Map<Long, double[]> faixas = faixas(campos, casos, problema);
        List<Recuperado> achados = new ArrayList<Recuperado>();
        for (Banco.Caso caso : casos) {
            if (soValidos && !caso.valido) {
                continue;
            }
            List<Avaliado> avaliados = new ArrayList<Avaliado>();
            double peso = 0;
            double soma = 0;
            for (Banco.Campo campo : campos) {
                String consulta = limpar(problema.get(campo.id));
                String valorCaso = limpar(caso.valores.get(campo.id));
                double sim = consulta == null ? 0 : local(campo, consulta, valorCaso, faixas.get(campo.id), frases, tabela);
                peso += campo.peso;
                soma += campo.peso * sim;
                avaliados.add(new Avaliado(campo.nome, campo.peso, consulta, valorCaso, sim));
            }
            double global = peso <= 0 ? 0 : soma / peso;
            if (global > 0) {
                Adaptacao adaptacao = adaptar(caso.solucao, avaliados);
                achados.add(new Recuperado(caso, global, avaliados, adaptacao.texto, adaptacao.trocas));
            }
        }
        Collections.sort(achados, new Comparator<Recuperado>() {
            @Override
            public int compare(Recuperado a, Recuperado b) {
                int porSim = Double.compare(b.similaridade, a.similaridade);
                if (porSim != 0) {
                    return porSim;
                }
                int porValido = Boolean.compare(b.caso.valido, a.caso.valido);
                if (porValido != 0) {
                    return porValido;
                }
                return Long.compare(b.caso.id, a.caso.id);
            }
        });
        if (achados.size() > MAXIMO) {
            return new ArrayList<Recuperado>(achados.subList(0, MAXIMO));
        }
        return achados;
    }

    public static double global(double[] pesos, double[] sims) {
        double peso = 0;
        double soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            peso += pesos[i];
            soma += pesos[i] * sims[i];
        }
        return peso <= 0 ? 0 : soma / peso;
    }

    public static double dice(Collection<String> a, Collection<String> b) {
        int comum = 0;
        for (String termo : a) {
            if (b.contains(termo)) {
                comum++;
            }
        }
        int total = a.size() + b.size();
        return total == 0 ? 0 : 2.0 * comum / total;
    }

    static Set<String> termos(String texto, List<String[]> frases) {
        Set<String> termos = new LinkedHashSet<String>();
        String normalizado = normalizar(texto, frases);
        if (normalizado.isEmpty()) {
            return termos;
        }
        for (String palavra : normalizado.split(" ")) {
            if (palavra.length() > 1 && !STOP.contains(palavra)) {
                termos.add(palavra);
            }
        }
        return termos;
    }

    private static double local(Banco.Campo campo, String consulta, String valorCaso, double[] faixa,
            List<String[]> frases, Tabela tabela) {
        if (tabela.cobre(campo.id)) {
            if (valorCaso == null) {
                return 0;
            }
            Double par = tabela.consultar(campo.id, consulta, valorCaso);
            return par == null ? 0 : par;
        }
        if (valorCaso == null) {
            return 0;
        }
        if ("NUMERICO".equals(campo.tipo)) {
            Double a = numero(consulta);
            Double b = numero(valorCaso);
            if (a == null || b == null) {
                return 0;
            }
            return escalar(a, b, faixa[0], faixa[1]);
        }
        return dice(termos(consulta, frases), termos(valorCaso, frases));
    }

    private static double escalar(double a, double b, double minimo, double maximo) {
        double amplitude = maximo - minimo;
        if (amplitude <= 0) {
            return a == b ? 1 : 0;
        }
        return Math.max(0, Math.min(1, 1 - Math.abs(a - b) / amplitude));
    }

    private static Map<Long, double[]> faixas(List<Banco.Campo> campos, List<Banco.Caso> casos, Map<Long, String> problema) {
        Map<Long, double[]> faixas = new java.util.HashMap<Long, double[]>();
        for (Banco.Campo campo : campos) {
            if (!"NUMERICO".equals(campo.tipo)) {
                continue;
            }
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            Double consulta = numero(limpar(problema.get(campo.id)));
            if (consulta != null) {
                min = consulta;
                max = consulta;
            }
            for (Banco.Caso caso : casos) {
                Double valor = numero(limpar(caso.valores.get(campo.id)));
                if (valor == null) {
                    continue;
                }
                min = Math.min(min, valor);
                max = Math.max(max, valor);
            }
            if (min == Double.POSITIVE_INFINITY) {
                min = 0;
                max = 0;
            }
            faixas.put(campo.id, new double[] {min, max});
        }
        return faixas;
    }

    private static Adaptacao adaptar(String solucao, List<Avaliado> avaliados) {
        String texto = solucao == null ? "" : solucao;
        List<String> trocas = new ArrayList<String>();
        List<String[]> pares = new ArrayList<String[]>();
        for (Avaliado avaliado : avaliados) {
            if (avaliado.consulta == null || avaliado.caso == null) {
                continue;
            }
            if (avaliado.consulta.equalsIgnoreCase(avaliado.caso)) {
                continue;
            }
            if (avaliado.caso.length() < 2) {
                continue;
            }
            pares.add(new String[] {avaliado.caso, avaliado.consulta});
        }
        Collections.sort(pares, new Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                return Integer.compare(b[0].length(), a[0].length());
            }
        });
        for (String[] par : pares) {
            Pattern padrao = Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(par[0]) + "(?![\\p{L}\\p{N}])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            if (padrao.matcher(texto).find()) {
                texto = padrao.matcher(texto).replaceAll(par[1]);
                trocas.add(par[0] + " → " + par[1]);
            }
        }
        return new Adaptacao(texto, String.join("; ", trocas));
    }

    static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private static boolean temValor(List<Banco.Campo> campos, Map<Long, String> problema) {
        for (Banco.Campo campo : campos) {
            if (limpar(problema.get(campo.id)) != null) {
                return true;
            }
        }
        return false;
    }

    private static Double numero(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return Double.valueOf(valor.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String normalizar(String texto, List<String[]> frases) {
        String s = " " + base(texto) + " ";
        List<String[]> prontas = new ArrayList<String[]>();
        for (String[] frase : frases) {
            String origem = base(frase[0]);
            String termo = base(frase[1]).replace(' ', '_');
            if (!origem.isEmpty() && !termo.isEmpty()) {
                prontas.add(new String[] {origem, termo});
            }
        }
        Collections.sort(prontas, new Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                return Integer.compare(b[0].length(), a[0].length());
            }
        });
        for (String[] frase : prontas) {
            s = s.replace(" " + frase[0] + " ", " " + frase[1] + " ");
        }
        return s.trim();
    }

    static String base(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static final class Tabela {
        private final java.util.Map<Long, java.util.Map<String, Double>> pares = new java.util.HashMap<Long, java.util.Map<String, Double>>();

        static Tabela de(List<Banco.Par> linhas) {
            Tabela tabela = new Tabela();
            for (Banco.Par par : linhas) {
                java.util.Map<String, Double> doCampo = tabela.pares.get(par.campoId);
                if (doCampo == null) {
                    doCampo = new java.util.HashMap<String, Double>();
                    tabela.pares.put(par.campoId, doCampo);
                }
                doCampo.put(base(par.consulta) + "\n" + base(par.caso), par.similaridade);
            }
            return tabela;
        }

        boolean cobre(long campoId) {
            return pares.containsKey(campoId);
        }

        Double consultar(long campoId, String consulta, String caso) {
            java.util.Map<String, Double> doCampo = pares.get(campoId);
            return doCampo == null ? null : doCampo.get(base(consulta) + "\n" + base(caso));
        }
    }

    public static final class Avaliado {
        public final String nome;
        public final int peso;
        public final String consulta;
        public final String caso;
        public final double sim;

        Avaliado(String nome, int peso, String consulta, String caso, double sim) {
            this.nome = nome;
            this.peso = peso;
            this.consulta = consulta;
            this.caso = caso;
            this.sim = sim;
        }
    }

    public static final class Recuperado {
        public final Banco.Caso caso;
        public final double similaridade;
        public final List<Avaliado> avaliados;
        public final String solucaoAdaptada;
        public final String trocas;

        Recuperado(Banco.Caso caso, double similaridade, List<Avaliado> avaliados, String solucaoAdaptada, String trocas) {
            this.caso = caso;
            this.similaridade = similaridade;
            this.avaliados = avaliados;
            this.solucaoAdaptada = solucaoAdaptada;
            this.trocas = trocas;
        }
    }

    private static final class Adaptacao {
        final String texto;
        final String trocas;

        Adaptacao(String texto, String trocas) {
            this.texto = texto;
            this.trocas = trocas;
        }
    }
}
