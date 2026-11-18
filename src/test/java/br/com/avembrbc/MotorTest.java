package br.com.avembrbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MotorTest {

    @Test
    void mediaPonderadaDoLivro() {
        assertEquals(0.75, Motor.global(new double[] {3, 1}, new double[] {1, 0}), 1e-9);
        assertEquals(4.6 / 14, Motor.global(
                new double[] {5, 1, 2, 2, 2, 2},
                new double[] {0, 0.6, 1, 0, 1, 0}), 1e-9);
        assertEquals(1, Motor.dice(Arrays.asList("fezes", "sangue"), Arrays.asList("sangue", "fezes")), 1e-9);
        assertEquals(0, Motor.dice(Arrays.asList("fezes"), Arrays.asList("torcicolo")), 1e-9);
    }

    @Test
    void baseRecuperaCoccidiose() {
        try (Banco banco = Banco.h2Quiet()) {
            int casos = banco.contar();
            assertTrue(casos >= 500, "casos=" + casos);
            assertTrue(banco.contar() - banco.contarValidos() >= 20);
            assertTrue(banco.contarChaves() >= 25);
            java.util.List<Banco.Revisao> pendentes = banco.revisoes(null, true);
            assertTrue(pendentes.size() >= 20);
            assertTrue(!banco.complementos(pendentes.get(0).id).isEmpty());
            assertTrue(pendentes.get(0).origemId != null);
            long area = banco.idArea("Sanidade");
            Map<Long, String> problema = new LinkedHashMap<Long, String>();
            for (Banco.Campo campo : banco.campos(area)) {
                if ("Espécie".equals(campo.nome)) {
                    problema.put(campo.id, "Frango de corte");
                } else if ("Idade (dias)".equals(campo.nome)) {
                    problema.put(campo.id, "21");
                } else if ("Sistema".equals(campo.nome)) {
                    problema.put(campo.id, "Convencional");
                } else if ("Quadro".equals(campo.nome)) {
                    problema.put(campo.id, "fezes com sangue e aves amontoadas");
                } else if ("Sinal".equals(campo.nome)) {
                    problema.put(campo.id, "lesão no intestino");
                }
            }
            List<Motor.Recuperado> achados = Motor.recuperar(banco, area, problema, false);
            assertTrue(achados.get(0).similaridade >= 0.9);
            assertTrue(achados.get(0).caso.solucao.contains("Coccidiose em frango de corte aos 21"));
            assertTrue(!banco.sugestoes(banco.campos(area).get(0).id).isEmpty());
            int areas = banco.contarAreas();
            banco.salvarArea(null, "Genética");
            assertEquals(areas + 1, banco.contarAreas());
            int campos = banco.campos(area).size();
            banco.salvarCampo(null, area, "Lote", "TEXTO", 1);
            assertEquals(campos + 1, banco.campos(area).size());
        }
    }
}
