# USER GUIDE
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
System.out.println(product.toString());
product = new HCFConnection<>(Product.class).getById("1");
System.out.println(product.toString());
product = new HCFConnection<>(Product.class).getById(1L);
System.out.println(product.toString());
product = new HCFConnection<>(Product.class).getById(1.0);
System.out.println(product.toString());
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

## EXEMPLO 3
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
