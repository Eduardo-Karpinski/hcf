# GUIDE
Guia para as principais ações do HCF

# CLASSE USADA PARA O GUIA
```
@Entity
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private BigDecimal price;
  private LocalDateTime dataOne;
  private LocalDateTime dataTwo;
  private Type type;

  // constructors and getters + setters and override of toString

}

enum Type {
  INPUT("Input"), EXIT("Exit");

  private final String description;

  Type(final String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}

@Entity
class StyleOne {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;

	// constructors and getters + setters and override of toString

}

@Entity
class StyleTwo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	
	// constructors and getters + setters and override of toString
}

```

## INSERINDO REGISTRO
```
LocalDateTime dateTime = LocalDateTime.now();
Product product = new Product("product 01", BigDecimal.valueOf(3.33), dateTime, dateTime.plusDays(1), Type.INPUT);
new HCFConnection<>(Product.class).save(product);
```

## ATUALIZANDO REGISTRO
```
LocalDateTime dateTime = LocalDateTime.now();
Product product = new Product("product 01 UPDATED", BigDecimal.valueOf(3.33), dateTime, dateTime.plusDays(1), Type.INPUT);
product.setId(1L);
new HCFConnection<>(Product.class).save(product);
```

## DELETANDO REGISTRO
```
Product product = new Product();
product.setId(1L);
new HCFConnection<>(Product.class).delete(product);
```

#### INFORMATIVO
* Sim, o mesmo metodo usado para para salvar tambem atualiza.
* Os metodos save e delete tem outra implementação onde se passa uma lista e um boolean
onde o mesmo informa se deve fazer **commit** do que foi possivel executar antes de
algum erro ou fazer um **rollback** geral.

## PEGANDO REGISTRO POR ID
```
Product product;
product = new HCFConnection<>(Product.class).getById(1);
System.out.println(product);
product = new HCFConnection<>(Product.class).getById("1");
System.out.println(product);
product = new HCFConnection<>(Product.class).getById(1L);
System.out.println(product);
product = new HCFConnection<>(Product.class).getById(1.0);
System.out.println(product);
```

#### INFORMATIVO
* Todas as formas são validas e retornam o respectivo registro mas
recomenda-se usar o mesmo tipo do atributo.
* deve-se reparar por mais que a busca seja por id o metodo não pergunta
o nome do atributo, isso devesse ao fato do mesmo pegar o primeiro atributo
que estiver com a **annotation id**, caso o mesmo não existe ira retornar a
exception NullPointerException.

## PEGANDO TODOS OS REGISTROS
```
List<Product> products = new HCFConnection<>(Product.class).all();
products.forEach(System.out::println);
```

## TRAZENDO TOTAL DE REGISTROS NA TABELA
```
Long totalRecords = new HCFConnection<>(Product.class).count();
System.out.println(totalRecords);
```

#### INFORMATIVO
* Existe outro metodo, **countDistinct** faz o mesmo que o count mas aplicando um **distinct** na pesquisa.

## PEGANDO PRIMEIRO OU ULTIMO REGISTRO
```
HCFOrder order = new HCFOrder(false, "id", null, null);
Product products = new HCFConnection<>(Product.class).getFirstOrLast(order);
System.out.print(products);
```

#### INFORMATIVO
* O objeto **HCFOrder** possui os campos asc, field, limit e offset sendo asc
o orientador da procura, se o mesmo estiver true ele pegara o primeiro registro,
se estiver false pegara o ultimo, ele ira fazer isso com base no field, ou seja,
pegará com base na ultima inserção da coluna não do registro, o campo limit serve
para limitar o tamanho do retorno, ou seja, se a minha procura devolver 100 registros
mas no meu order estiver com limit 10, ele ira trazer somente 10 registro, já o offset
ira servir como intervalo, alterando o exemplo anterior do limit se nos agora quisermos
pegar do 11 registro a frente ainda limitando 10 e 10 colocariamos limit ainda com 10
mas o offset tambem com 10 assim o range que sera pego 11 a 21, podesse fazer uma procura
so utilizando o objeto **HCFOrder** com o metodo **getByOrders**.

## TRAZENDO SOMA DE COLUNAS
```
Object allPricesAddedWithoutParameters  = new HCFConnection<>(Product.class).bringAddition(Collections.singletonList("price"), "");
System.out.println(allPricesAddedWithoutParameters);
		
Object[] allPricesAddedWithParameters  = (Object[]) new HCFConnection<>(Product.class).bringAddition(Arrays.asList("price", "id"),
	"id", 1, HCFParameter.EQUAL, HCFOperator.DEFAULT,
	"id", 2, HCFParameter.EQUAL, HCFOperator.OR);
System.out.println(allPricesAddedWithParameters[0]);
System.out.println(allPricesAddedWithParameters[1]);
```

#### INFORMATIVO
* no caso de ser passado mais de uma coluna para o bringAddition o mesmo não devolvera um resultado mais sim
a quantidade de colunas passadas, existe uma outra implementação do mesmo metodo onde se passa um string para
o mesmo fazer um **group by** com a parametro passado.

## TRAZENDO REGISTRO POR RELACIONAMENTO INVERTIDO
```
Product product = new HCFConnection<>(Product.class).getByInvertedRelation("StylesOne", "id", "15").get(0);
System.out.println(product);
		
List<Product> products = new HCFConnection<>(Product.class).getByInvertedRelation("StylesTwo", "id", "15");
products.forEach(System.out::println);
```

#### INFORMATIVO
* De uma forma direta o metodo **getByInvertedRelation** trás o pai pelo filho, retornando mais de um
elemento caso a direção não seja **1 para X**, o primeiro parametro é o nome da tabela, o segundo é
o nome da coluna aonde ambos pai e filho estão correlacionados e o terceiro é o identificador da
coluna.

## TRAZENDO REGISTRO POR RELACIONAMENTO
```
List<StyleOne> list = new HCFConnection<>(StyleOne.class).getRelations(Product.class, "StylesOne", 1);
list.forEach(System.out::println);
```

# METODO SEARCH
O metodo search é o principal metodo de busca da api com o mesmo podendo
fazer inumeras requisições, sendo que existem duas implementações, uma que
recebe lista de **HCFOrder** e um **Object...** e outra que recebe
lista de **HCFOrder** e uma lista de **HCFSearch**, tambem existe o metodo
**searchWithOneResult** onde retornara apenas um elemento os parametros são
lista de **HCFOrder** e um **Object...**, para os metodos que recebem um
**Object...** se o numero passado não for multiplo de 4 uma exception ira ocorrer.

## EXEMPLO 1
```
// buscando todos os registros que tenham 1 ou 3 na coluna id
List<Product> products = new HCFConnection<>(Product.class).search(null,
  "id", 1, HCFParameter.EQUAL, HCFOperator.DEFAULT,
  "id", 3, HCFParameter.EQUAL, HCFOperator.OR);
products.forEach(System.out::println);
```

## EXEMPLO 2
```
// buscando todos os registros que tenham exatamente product 03, product 06 e product 897 na coluna name
List<Product> products = new HCFConnection<>(Product.class).search(null,
  "name", "product 03", HCFParameter.EQUAL, HCFOperator.DEFAULT,
  "name", "product 06", HCFParameter.EQUAL, HCFOperator.OR,
  "name", "product 897", HCFParameter.EQUAL, HCFOperator.OR);
products.forEach(System.out::println);
```

## EXEMPLO 3
```
// busca todos os registros entre as datas inseridas
LocalDateTime dateTimeOne = LocalDateTime.of(2021, 2, 21, 0, 0, 0);
LocalDateTime dateTimeTwo = LocalDateTime.of(2021, 3, 3, 23, 59, 59);
		
List<Product> products = new HCFConnection<>(Product.class).search(null,
  "dataOne", dateTimeOne, HCFParameter.GREATERTHANOREQUALTO, HCFOperator.DEFAULT,
  "dataTwo", dateTimeTwo, HCFParameter.LESSTHANOREQUALTO, HCFOperator.AND);
products.forEach(System.out::println);
```

## EXEMPLO 4
```
// busca todos os registros entre as datas inseridas e todos que tenham 4 ou 5 no id
LocalDateTime dateTimeOne = LocalDateTime.of(2021, 2, 21, 0, 0, 0);
LocalDateTime dateTimeTwo = LocalDateTime.of(2021, 3, 3, 23, 59, 59);
		
List<Product> products = new HCFConnection<>(Product.class).search(null,
  "dataOne", dateTimeOne, HCFParameter.GREATERTHANOREQUALTO, HCFOperator.DEFAULT,
  "dataTwo", dateTimeTwo, HCFParameter.LESSTHANOREQUALTO, HCFOperator.AND,
  "id", 4, HCFParameter.EQUAL, HCFOperator.OR,
  "id", 5, HCFParameter.EQUAL, HCFOperator.OR);

products.forEach(System.out::println);
```

#### INFORMATIVO
* para aqueles que ficaram com duvidas sobre **HCFOperator** imagine que
a cada 4 parametros passados forme um "search", sendo assim o **HCFOperator**
tem como proposito fazer a operação **OR** ou **AND** com o "search" de cima,
sendo obrigatorio passar **DEFAULT** nos primeiros 4 parametros ou no primeiro "search"
pois não há nada acima para fazer uma operação.

# UTILIDADE
Talvez haja a necessidade de fazer a inicialização do HCF junto ao servidor web, para isso
siga os exemplos abaixo.

## ADICIONANDO LISTENER AO ARQUIVO WEB.XML
```
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
	id="WebApp_ID" version="4.0">
	<display-name>your project name</display-name>
	<welcome-file-list>
		<welcome-file>index.html</welcome-file>
	</welcome-file-list>
	<listener>
		<listener-class>br.com.your.package.Initializer</listener-class>
	</listener>
</web-app>
```

## CLASSE INICIALIZADORA
```
package br.com.your.package;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import br.com.hcf.HCFactory;

public class Initializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		HCFactory.getFactory();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		HCFactory.shutdown();
	}

}
```
