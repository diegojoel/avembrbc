package br.com.avembrbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Banco implements AutoCloseable {

    public final Connection conn;
    public final String descricao;
    public final boolean memoria;

    private Banco(Connection conn, String descricao, boolean memoria) {
        this.conn = conn;
        this.descricao = descricao;
        this.memoria = memoria;
    }

    public static Banco h2() throws SQLException {
        Banco banco = new Banco(DriverManager.getConnection(
                "jdbc:h2:mem:avembrbc-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1", "sa", ""),
                "H2 em memória", true);
        banco.preparar();
        return banco;
    }

    public static Banco h2Quiet() {
        try {
            return h2();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Banco postgres(String host, String porta, String nome, String usuario, String senha) throws SQLException {
        String url = "jdbc:postgresql://" + host + ":" + porta + "/" + nome;
        DriverManager.setLoginTimeout(5);
        Banco banco = new Banco(DriverManager.getConnection(url, usuario, senha),
                "PostgreSQL " + host + ":" + porta + "/" + nome, false);
        banco.preparar();
        return banco;
    }

    public List<Area> areas() {
        List<Area> areas = new ArrayList<Area>();
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT id, nome FROM area ORDER BY id")) {
            while (rs.next()) {
                areas.add(new Area(rs.getLong(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return areas;
    }

    public long idArea(String nome) {
        for (Area area : areas()) {
            if (area.nome.equalsIgnoreCase(nome)) {
                return area.id;
            }
        }
        throw new IllegalArgumentException(nome);
    }

    public List<String> sugestoes(long campoId) {
        List<String> valores = new ArrayList<String>();
        String sql = "SELECT DISTINCT valor FROM detalhe WHERE campo_id = ? AND valor IS NOT NULL "
                + "AND CHAR_LENGTH(valor) <= 100 ORDER BY valor FETCH FIRST 40 ROWS ONLY";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, campoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    valores.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return valores;
    }

    public List<Campo> campos(long areaId) {
        List<Campo> campos = new ArrayList<Campo>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, nome, tipo, peso FROM campo WHERE area_id = ? ORDER BY id")) {
            ps.setLong(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    campos.add(new Campo(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return campos;
    }

    public List<Caso> casos(long areaId) {
        List<Caso> casos = new ArrayList<Caso>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, solucao, fonte, valido FROM caso WHERE area_id = ?")) {
            ps.setLong(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong(1);
                    casos.add(new Caso(id, areaId, rs.getString(2), rs.getString(3), rs.getBoolean(4), valores(id)));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return casos;
    }

    public List<Linha> listar() {
        List<Linha> linhas = new ArrayList<Linha>();
        String sql = "SELECT c.id, a.nome, c.valido, c.fonte, "
                + "(SELECT d.valor FROM detalhe d JOIN campo f ON f.id = d.campo_id "
                + "WHERE d.caso_id = c.id AND f.nome = 'Quadro') "
                + "FROM caso c JOIN area a ON a.id = c.area_id ORDER BY c.id DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                linhas.add(new Linha(rs.getLong(1), rs.getString(2), rs.getString(5), rs.getBoolean(3), rs.getString(4)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return linhas;
    }

    public int contar() {
        return escalar("SELECT COUNT(*) FROM caso");
    }

    public int contarValidos() {
        return escalar("SELECT COUNT(*) FROM caso WHERE valido = TRUE");
    }

    public int contarAreas() {
        return escalar("SELECT COUNT(*) FROM area");
    }

    public int contarChaves() {
        return escalar("SELECT COUNT(*) FROM dicionario");
    }

    public void salvarArea(Long id, String nome) {
        String texto = exigir(nome, "Informe o nome da área.");
        try {
            if (id == null) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO area (nome) VALUES (?)")) {
                    ps.setString(1, texto);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE area SET nome = ? WHERE id = ?")) {
                    ps.setString(1, texto);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Já existe uma área com esse nome.");
        }
    }

    public void excluirArea(long id) {
        if (escalar("SELECT COUNT(*) FROM caso WHERE area_id = " + id) > 0) {
            throw new IllegalArgumentException("A área tem casos. Exclua os casos antes.");
        }
        executar("DELETE FROM similaridade WHERE campo_id IN (SELECT id FROM campo WHERE area_id = " + id + ")");
        executar("DELETE FROM campo WHERE area_id = " + id);
        executar("DELETE FROM area WHERE id = " + id);
    }

    public void salvarCampo(Long id, long areaId, String nome, String tipo, int peso) {
        String texto = exigir(nome, "Informe o nome do campo.");
        if (!"TEXTO".equals(tipo) && !"NUMERICO".equals(tipo)) {
            throw new IllegalArgumentException("O tipo é TEXTO ou NUMERICO.");
        }
        if (peso < 1) {
            throw new IllegalArgumentException("O peso começa em 1.");
        }
        try {
            if (id == null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO campo (area_id, nome, tipo, peso) VALUES (?,?,?,?)")) {
                    ps.setLong(1, areaId);
                    ps.setString(2, texto);
                    ps.setString(3, tipo);
                    ps.setInt(4, peso);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE campo SET nome = ?, tipo = ?, peso = ? WHERE id = ?")) {
                    ps.setString(1, texto);
                    ps.setString(2, tipo);
                    ps.setInt(3, peso);
                    ps.setLong(4, id);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void excluirCampo(long id) {
        if (escalar("SELECT COUNT(*) FROM detalhe WHERE campo_id = " + id) > 0) {
            throw new IllegalArgumentException("O campo está em casos. Não dá para excluir.");
        }
        executar("DELETE FROM similaridade WHERE campo_id = " + id);
        executar("DELETE FROM campo WHERE id = " + id);
    }

    public List<String[]> frases() {
        List<String[]> frases = new ArrayList<String[]>();
        String sql = "SELECT d.chave, d.chave FROM dicionario d "
                + "UNION ALL SELECT e.expressao, d.chave FROM equivalente e "
                + "JOIN dicionario d ON d.id = e.dicionario_id";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                frases.add(new String[] {rs.getString(1), rs.getString(2)});
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        Collections.sort(frases, new java.util.Comparator<String[]>() {
            @Override
            public int compare(String[] a, String[] b) {
                return Integer.compare(b[0].length(), a[0].length());
            }
        });
        return frases;
    }

    public List<Palavra> palavras() {
        List<Palavra> lista = new ArrayList<Palavra>();
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT id, chave FROM dicionario ORDER BY chave")) {
            while (rs.next()) {
                long id = rs.getLong(1);
                lista.add(new Palavra(id, rs.getString(2), equivalentes(id)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return lista;
    }

    public void salvarPalavra(Long id, String chave, String equivalentes) {
        String texto = exigir(chave, "Informe a palavra-chave.");
        try {
            conn.setAutoCommit(false);
            long chaveId = id == null ? 0 : id;
            if (id == null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO dicionario (chave) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, texto);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        chaveId = rs.getLong(1);
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE dicionario SET chave = ? WHERE id = ?")) {
                    ps.setString(1, texto);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM equivalente WHERE dicionario_id = ?")) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO equivalente (dicionario_id, expressao) VALUES (?,?)")) {
                for (String linha : equivalentes.split("\\r?\\n")) {
                    String item = linha.trim();
                    if (item.isEmpty()) {
                        continue;
                    }
                    ps.setLong(1, chaveId);
                    ps.setString(2, item);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            rollback();
            throw new IllegalArgumentException("Já existe essa palavra-chave.");
        } finally {
            autoCommit();
        }
    }

    public void excluirPalavra(long id) {
        executar("DELETE FROM equivalente WHERE dicionario_id = " + id);
        executar("DELETE FROM dicionario WHERE id = " + id);
    }

    public List<Par> pares() {
        List<Par> lista = new ArrayList<Par>();
        String sql = "SELECT s.id, s.campo_id, a.nome, f.nome, s.valor_consulta, s.valor_caso, s.similaridade "
                + "FROM similaridade s JOIN campo f ON f.id = s.campo_id JOIN area a ON a.id = f.area_id "
                + "ORDER BY a.nome, f.nome, s.valor_consulta";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Par(rs.getLong(1), rs.getLong(2), rs.getString(3) + " — " + rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getDouble(7)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return lista;
    }

    public void salvarPar(long campoId, String consulta, String caso, int percentual) {
        String a = exigir(consulta, "Informe o valor na consulta.");
        String b = exigir(caso, "Informe o valor no caso.");
        if (percentual < 0 || percentual > 100) {
            throw new IllegalArgumentException("A similaridade fica entre 0 e 100.");
        }
        double valor = percentual / 100.0;
        try (PreparedStatement busca = conn.prepareStatement(
                "SELECT id FROM similaridade WHERE campo_id = ? AND LOWER(valor_consulta) = LOWER(?) AND LOWER(valor_caso) = LOWER(?)")) {
            busca.setLong(1, campoId);
            busca.setString(2, a);
            busca.setString(3, b);
            try (ResultSet rs = busca.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE similaridade SET similaridade = ?, valor_consulta = ?, valor_caso = ? WHERE id = ?")) {
                        ps.setDouble(1, valor);
                        ps.setString(2, a);
                        ps.setString(3, b);
                        ps.setLong(4, rs.getLong(1));
                        ps.executeUpdate();
                    }
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO similaridade (campo_id, valor_consulta, valor_caso, similaridade) VALUES (?,?,?,?)")) {
                ps.setLong(1, campoId);
                ps.setString(2, a);
                ps.setString(3, b);
                ps.setDouble(4, valor);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void excluirPar(long id) {
        executar("DELETE FROM similaridade WHERE id = " + id);
    }

    public List<Revisao> revisoes(Long areaId, boolean soPendentes) {
        List<Revisao> lista = new ArrayList<Revisao>();
        String sql = "SELECT c.id, a.nome, c.valido, c.solucao, c.adaptacao, c.origem_id, c.similaridade "
                + "FROM caso c JOIN area a ON a.id = c.area_id WHERE 1 = 1";
        if (areaId != null) {
            sql += " AND c.area_id = " + areaId;
        }
        if (soPendentes) {
            sql += " AND c.valido = FALSE";
        }
        sql += " ORDER BY c.id DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                long origem = rs.getLong(6);
                boolean semOrigem = rs.wasNull();
                Double similaridade = rs.getObject(7) == null ? null : rs.getDouble(7);
                lista.add(new Revisao(rs.getLong(1), rs.getString(2), rs.getBoolean(3), rs.getString(4),
                        rs.getString(5), semOrigem ? null : Long.valueOf(origem), similaridade));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return lista;
    }

    public String descricao(long casoId) {
        StringBuilder texto = new StringBuilder();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT f.nome, d.valor FROM detalhe d JOIN campo f ON f.id = d.campo_id WHERE d.caso_id = ? ORDER BY f.id")) {
            ps.setLong(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (texto.length() > 0) {
                        texto.append('\n');
                    }
                    texto.append(rs.getString(1)).append(": ").append(rs.getString(2) == null ? "—" : rs.getString(2));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return texto.toString();
    }

    public List<String> complementos(long casoId) {
        List<String> lista = new ArrayList<String>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT texto FROM complemento WHERE caso_id = ? ORDER BY id")) {
            ps.setLong(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return lista;
    }

    public void salvarRevisao(long casoId, String solucao, boolean valido) {
        String texto = exigir(solucao, "Informe a solução.");
        try (PreparedStatement ps = conn.prepareStatement("UPDATE caso SET solucao = ?, valido = ? WHERE id = ?")) {
            ps.setString(1, texto);
            ps.setBoolean(2, valido);
            ps.setLong(3, casoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void complementar(long casoId, String texto) {
        String item = exigir(texto, "Informe o complemento.");
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO complemento (caso_id, texto) VALUES (?,?)")) {
            ps.setLong(1, casoId);
            ps.setString(2, item);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private String equivalentes(long dicionarioId) throws SQLException {
        StringBuilder texto = new StringBuilder();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT expressao FROM equivalente WHERE dicionario_id = ? ORDER BY id")) {
            ps.setLong(1, dicionarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (texto.length() > 0) {
                        texto.append('\n');
                    }
                    texto.append(rs.getString(1));
                }
            }
        }
        return texto.toString();
    }

    private int escalar(String sql) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void executar(String sql) {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String exigir(String texto, String mensagem) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(mensagem);
        }
        return texto.trim();
    }

    public void reter(long areaId, Map<Long, String> problema, String solucao, Long origemId, Double similaridade, String trocas) {
        String quadro = quadro(areaId, problema);
        if (quadro == null) {
            throw new IllegalArgumentException("Informe o quadro para reter o caso.");
        }
        try (PreparedStatement existe = conn.prepareStatement(
                "SELECT c.id FROM caso c JOIN detalhe d ON d.caso_id = c.id "
                        + "JOIN campo f ON f.id = d.campo_id "
                        + "WHERE c.area_id = ? AND f.nome = 'Quadro' AND LOWER(d.valor) = LOWER(?) AND LOWER(c.solucao) = LOWER(?)")) {
            existe.setLong(1, areaId);
            existe.setString(2, quadro);
            existe.setString(3, solucao);
            try (ResultSet rs = existe.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Já existe um caso igual a este na base.");
                }
            }
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO caso (area_id, solucao, fonte, valido, origem_id, similaridade, adaptacao) VALUES (?,?,?,FALSE,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, areaId);
                ps.setString(2, solucao);
                ps.setString(3, "Retido na consulta");
                if (origemId == null) {
                    ps.setNull(4, java.sql.Types.BIGINT);
                    ps.setNull(5, java.sql.Types.DOUBLE);
                } else {
                    ps.setLong(4, origemId);
                    ps.setDouble(5, similaridade == null ? 0 : similaridade);
                }
                ps.setString(6, cortar(trocas, 500));
                ps.executeUpdate();
                long id;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getLong(1);
                }
                gravarDetalhes(id, problema);
            }
            conn.commit();
        } catch (SQLException e) {
            rollback();
            throw new IllegalStateException(e);
        } finally {
            autoCommit();
        }
    }

    public void validar(long casoId) {
        executar("UPDATE caso SET valido = TRUE WHERE id = ?", casoId);
    }

    public void excluir(long casoId) {
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement solta = conn.prepareStatement("UPDATE caso SET origem_id = NULL WHERE origem_id = ?")) {
                solta.setLong(1, casoId);
                solta.executeUpdate();
            }
            try (PreparedStatement detalhe = conn.prepareStatement("DELETE FROM detalhe WHERE caso_id = ?")) {
                detalhe.setLong(1, casoId);
                detalhe.executeUpdate();
            }
            try (PreparedStatement caso = conn.prepareStatement("DELETE FROM caso WHERE id = ?")) {
                caso.setLong(1, casoId);
                caso.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            rollback();
            throw new IllegalStateException(e);
        } finally {
            autoCommit();
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    private void preparar() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS area ("
                    + "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, nome VARCHAR(80) NOT NULL UNIQUE)");
            st.execute("CREATE TABLE IF NOT EXISTS campo ("
                    + "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, area_id BIGINT NOT NULL, "
                    + "nome VARCHAR(80) NOT NULL, tipo VARCHAR(12) NOT NULL, peso INT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS caso ("
                    + "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, area_id BIGINT NOT NULL, "
                    + "solucao VARCHAR(2000) NOT NULL, fonte VARCHAR(300) NOT NULL, valido BOOLEAN NOT NULL, "
                    + "origem_id BIGINT, similaridade DOUBLE PRECISION, adaptacao VARCHAR(500), "
                    + "criado TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS detalhe ("
                    + "caso_id BIGINT NOT NULL, campo_id BIGINT NOT NULL, valor VARCHAR(500), "
                    + "PRIMARY KEY (caso_id, campo_id))");
            st.execute("CREATE TABLE IF NOT EXISTS dicionario ("
                    + "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, chave VARCHAR(120) NOT NULL UNIQUE)");
            st.execute("CREATE TABLE IF NOT EXISTS equivalente ("
                    + "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, dicionario_id BIGINT NOT NULL, "
                    + "expressao VARCHAR(200) NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS similaridade ("
                    + "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, campo_id BIGINT NOT NULL, "
                    + "valor_consulta VARCHAR(200) NOT NULL, valor_caso VARCHAR(200) NOT NULL, "
                    + "similaridade DOUBLE PRECISION NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS complemento ("
                    + "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, caso_id BIGINT NOT NULL, "
                    + "texto VARCHAR(1000) NOT NULL)");
        }
        if (contar() == 0) {
            conn.setAutoCommit(false);
            try {
                Carga.carregar(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Map<Long, String> valores(long casoId) throws SQLException {
        Map<Long, String> valores = new LinkedHashMap<Long, String>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT campo_id, valor FROM detalhe WHERE caso_id = ?")) {
            ps.setLong(1, casoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    valores.put(rs.getLong(1), rs.getString(2));
                }
            }
        }
        return valores;
    }

    private void gravarDetalhes(long casoId, Map<Long, String> problema) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO detalhe (caso_id, campo_id, valor) VALUES (?,?,?)")) {
            for (Map.Entry<Long, String> entrada : problema.entrySet()) {
                String valor = Motor.limpar(entrada.getValue());
                if (valor == null) {
                    continue;
                }
                ps.setLong(1, casoId);
                ps.setLong(2, entrada.getKey());
                ps.setString(3, valor);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private String quadro(long areaId, Map<Long, String> problema) {
        for (Campo campo : campos(areaId)) {
            if ("Quadro".equals(campo.nome)) {
                return Motor.limpar(problema.get(campo.id));
            }
        }
        return null;
    }

    private void executar(String sql, long id) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void rollback() {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void autoCommit() {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    private static String cortar(String texto, int limite) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= limite ? texto : texto.substring(0, limite);
    }

    public static final class Area {
        public final long id;
        public final String nome;

        public Area(long id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        @Override
        public String toString() {
            return nome;
        }
    }

    public static final class Campo {
        public final long id;
        public final String nome;
        public final String tipo;
        public final int peso;

        public Campo(long id, String nome, String tipo, int peso) {
            this.id = id;
            this.nome = nome;
            this.tipo = tipo;
            this.peso = peso;
        }
    }

    public static final class Caso {
        public final long id;
        public final long areaId;
        public final String solucao;
        public final String fonte;
        public final boolean valido;
        public final Map<Long, String> valores;

        public Caso(long id, long areaId, String solucao, String fonte, boolean valido, Map<Long, String> valores) {
            this.id = id;
            this.areaId = areaId;
            this.solucao = solucao;
            this.fonte = fonte;
            this.valido = valido;
            this.valores = valores;
        }

        public String valor(String nome, List<Campo> campos) {
            for (Campo campo : campos) {
                if (campo.nome.equals(nome)) {
                    String valor = valores.get(campo.id);
                    return valor == null ? "" : valor;
                }
            }
            return "";
        }
    }

    public static final class Palavra {
        public final long id;
        public final String chave;
        public final String equivalentes;

        public Palavra(long id, String chave, String equivalentes) {
            this.id = id;
            this.chave = chave;
            this.equivalentes = equivalentes;
        }
    }

    public static final class Par {
        public final long id;
        public final long campoId;
        public final String campo;
        public final String consulta;
        public final String caso;
        public final double similaridade;

        public Par(long id, long campoId, String campo, String consulta, String caso, double similaridade) {
            this.id = id;
            this.campoId = campoId;
            this.campo = campo;
            this.consulta = consulta;
            this.caso = caso;
            this.similaridade = similaridade;
        }
    }

    public static final class Revisao {
        public final long id;
        public final String area;
        public final boolean valido;
        public final String solucao;
        public final String adaptacao;
        public final Long origemId;
        public final Double similaridade;

        public Revisao(long id, String area, boolean valido, String solucao, String adaptacao, Long origemId,
                Double similaridade) {
            this.id = id;
            this.area = area;
            this.valido = valido;
            this.solucao = solucao;
            this.adaptacao = adaptacao;
            this.origemId = origemId;
            this.similaridade = similaridade;
        }
    }

    public static final class Linha {
        public final long id;
        public final String area;
        public final String quadro;
        public final boolean valido;
        public final String fonte;

        public Linha(long id, String area, String quadro, boolean valido, String fonte) {
            this.id = id;
            this.area = area;
            this.quadro = quadro;
            this.valido = valido;
            this.fonte = fonte;
        }
    }
}
