# AvembRBC

Sistema desktop de apoio à decisão com Raciocínio Baseado em Casos (RBC) para problemas de avicultura. A descrição do problema é um conjunto de atributos com peso; a busca compara o problema novo com os casos da área e devolve as soluções mais parecidas.

O ciclo recupera os casos parecidos, reutiliza a solução, deixa a revisão para uma pessoa e retém o caso novo como pendente. Não é um sistema oficial da Embrapa e não substitui o médico veterinário.

Referente ao projeto TCC de 2010 da UNC que originou esse sistema. A massa de casos foi recomposta a partir de publicações da Embrapa, em linguagem própria.

## Como executar

Requisito: Java 11.

```bat
set JAVA_HOME=C:\Apps\Java\jdk-11.0.33.0.2
..\digirbc\mvnw.cmd -q exec:java
```

O comando roda dentro da pasta `avembrbc`.

O banco padrão é o H2 em memória. A aplicação sobe nele, sem servidor. A base carregada e os casos retidos somem quando o programa fecha.

O PostgreSQL já está configurado (`localhost:5432/avembrbc`, usuário e senha `postgres`). Para usá-lo, abra o menu Banco ou o botão PostgreSQL. O banco precisa existir; as tabelas são criadas na conexão.

Na primeira subida, o banco vazio recebe as áreas, os campos, o dicionário, a similaridade local e a base de casos. Suspeita de Newcastle, influenza aviária, salmonelose ou micoplasmose deve ser comunicada ao serviço veterinário oficial.

## Base

- Embrapa Suínos e Aves. Ageitec: sanidade, doenças imunossupressoras, doenças da produção, ambiência e incubação.
- Embrapa. Documentos 107: manejo de frango de corte colonial.
- Embrapa. Instrução técnica para evitar o estresse calórico (Abreu).
- Embrapa. Alta densidade na produção de frangos de corte.
- Embrapa. Emissão de amônia na produção de frangos de corte.
- Embrapa. O que é e como reduzir a ascite em frangos.
- Embrapa Meio-Norte. Documentos 261: incubação de ovos de galinhas.
- Embrapa. Krabbe e Silva: ações e medidas da avicultura sustentável.
- Embrapa. Manejo ambiental na avicultura.
- Embrapa Suínos e Aves. Papel na evolução da avicultura (cama, forro, luz, aquecimento e telas).

As áreas são Sanidade, Ambiência, Nutrição, Manejo, Incubação e Biosseguridade. 
