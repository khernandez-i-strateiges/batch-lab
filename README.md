# Spring-batch concept lab

**Repositorio de GitHub:**
https://github.com/khernandez-i-strateiges/batch-lab.git

La rama en la que se trabajó el laboratorio es:  development

Parametros de entrada al Job:

- start_dt = 2020-09
- Archivos de entrada
    - fileInput = input/product.json (para la lectura).
    - fileOutput = output/product.xml (para la escritura de nuevos archivos).

## Lectura de archivos

**Leer archivos CSV.**

Extracción de los datos que se presentan en dicho archivo y leerlo, mostrarlos en consola.

```java
@StepScope
@Bean
public StaxEventItemReader xlmItemReader( @Value("#{jobParameters['fileInput']}") FileSystemResource inputFile) {
    StaxEventItemReader reader = new StaxEventItemReader();
    reader.setResource(inputFile);
    reader.setFragmentRootElementName("product");
    reader.setUnmarshaller(new Jaxb2Marshaller() {
        {
            setClassesToBeBound(Product.class);
        }
    });
    return reader;
}
```

**Leer archivos XML.**

Extracción de los datos que se presentan en dicho archivo y leerlo para mostrarlos en consola.


```java
@StepScope
@Bean
public StaxEventItemReader xlmItemReader( @Value("#{jobParameters['fileInput']}") FileSystemResource inputFile) {
    StaxEventItemReader reader = new StaxEventItemReader();
    reader.setResource(inputFile);
    reader.setFragmentRootElementName("product");
    reader.setUnmarshaller(new Jaxb2Marshaller() {
        {
            setClassesToBeBound(Product.class);
        }
    });
    return reader;
}
```

**Leer archivos txt de un tamaño fijo.**

Extracción de los datos de un archivo de texto con una precisión definida de donde viene la información de los datos, es decir a través de un rango poder saber que dentro de ese rango viene información útil para mostrarlo en consola.

```java
@StepScope
@Bean
public FlatFileItemReader flatFixFileItemReader(@Value("#{jobParameters['fileInput']}") FileSystemResource inputFile) {
    FlatFileItemReader reader = new FlatFileItemReader();
    // step 1 let reader know where is the file
    reader.setResource(inputFile);
    //create the line Mapper
    reader.setLineMapper(
            new DefaultLineMapper<Product>() {
                {
                    setLineTokenizer(new FixedLengthTokenizer() {
                        {
                            setNames(new String[]{"prodId", "productName", "prodDesc", "price", "unit"});
                            setColumns(
                                    new Range(1, 9), new Range(10, 28), new Range(29, 50), new Range(51, 62), new Range(63, 69)
                            );
                        }
                    });
                    setFieldSetMapper(new BeanWrapperFieldSetMapper<Product>() {
                        {
                            setTargetType(Product.class);
                        }
                    });
                }
            }
    );
    //step 3 tell reader to skip the header
    reader.setLinesToSkip(1);
    return reader;
}
```


**Leer datos de la base de datos desde una conexión: JdbcCursorItemReader .**

Desde la conexión de la base de dato apuntando a una tabla en específico.


```java
 @Bean
    public JdbcCursorItemReader jdbcCursorItemReader() {
        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        reader.setDataSource(this.dataSource);
        reader.setSql("select * from \"spring_batch_lab_dev\".product");
        reader.setRowMapper(new BeanPropertyRowMapper() {
            {
                setMappedClass(Product.class);
            }
        });
        return reader;
    }
```

**Leer datos de la base de datos desde una conexión: RepositoryItemReader.**

Desde la conexión de la base de dato apuntando a una tabla en específico.

Conexión a través de JpaRepository.

```java
@Bean
    public ItemReader itemReader() {
        RepositoryItemReader<Producto> reader = new RepositoryItemReader<Producto>();
        reader.setRepository(productoRespository);
        reader.setMethodName("findAll");
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("productId", Sort.Direction.ASC);
        reader.setSort(sorts);
        return reader;
    }
```


**Leer archivos Json.**

Desde un Reader se hace un llamado a una función que realiza una lectura del archivo de json para poder mostrarlo a consola.

```java
@StepScope
    @Bean
    public JsonItemReader jsonItemReader(
            @Value("#{jobParameters['fileInput']}")
                    FileSystemResource inputFile) {
        JsonItemReader reader = new JsonItemReader(inputFile, new JacksonJsonObjectReader<>(Product.class));
        return reader;
    }
```


**Leer datos de un web service.**

- Los datos se reciben desde un servicio (end point ) propio, mapeado con un RestTemplate que viene junto con las dependencias de Spring web.
- Tenemos dos casos para este proceso, primero leerlos de una manera que se ha un loop infinito y otro con una ejecución controlada:
- Loop infinito:
    - Class:  ProductService.
    - Método:  getProducts.

```java
 @Bean
    public ItemReaderAdapter serviceItemReader() {
        ItemReaderAdapter reader = new ItemReaderAdapter();
        reader.setTargetObject(productService);
        reader.setTargetMethod("getProducts");
        return reader;
    }
```

```java
public List<Producto> getProducts() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/product";
        return Arrays.stream(restTemplate.getForObject(url, Producto[].class)).collect(Collectors.toList());
    }
```

Esta ejecución se realiza bajo la llamada al servicio de productos sin ninguna restricción y cada vez que Job realiza su proceso siempre tendrá la lista de productos completos, haciendo un loop infinito.



**Ejecución controlada:**

- Class:  ProductServiceAdapter.

- Method: netxProduct.
```java
 @Bean
    public ItemReaderAdapter serviceItemReader() {
        ItemReaderAdapter reader = new ItemReaderAdapter();
        reader.setTargetObject(productServiceAdapter);
        reader.setTargetMethod("nextProduct");
        return reader;
    }
```

```java
public Producto nextProduct() {
    if (flag) {
        getProducts();
    }
    flag = false;
    if (productos.size() > 0) {
        return productos.remove(0);
    } else {
        return null;
    }
}
```

Esta ejecución se realiza bajo una manera fácil de decrementar la lista que se mantienen dentro del job. Eliminando su ultimo índex.

## Escritura de archivos:

**Escritura de archivos .CSV**

Bajo la anotación: `@StepScope` definimos que tendremos a disposición los parametros recibidos con jobParameters.

```java
@StepScope
@Bean
public FlatFileItemWriter flagFileItemWriter(@Value("#{jobParameters['fileOutput']}") FileSystemResource output) {
    FlatFileItemWriter writer = new FlatFileItemWriter();
    writer.setResource(output);
    writer.setLineAggregator(new DelimitedLineAggregator() {
        {
            setDelimiter("|");
            setFieldExtractor(new BeanWrapperFieldExtractor() {
                {
                    setNames(new String[]{"productId", "prodName", "productDesc", "unit", "price"});
                }
            });
        }
    });

    //definimos una columna de header para el archivo de output de nuestro proceso batch
    writer.setHeaderCallback(new FlatFileHeaderCallback() {
        @Override
        public void writeHeader(Writer writer) throws IOException {
            writer.write("productId | prodName | productDesc | unit | price");
        }
    });
    // this properties have to permission to overwite in the file or write in the next Line False/True
    writer.setAppendAllowed(true);
    // create footer to output file
    writer.setFooterCallback(new FlatFileFooterCallback() {
        @Override
        public void writeFooter(Writer writer) throws IOException {
            writer.write("this file created on : " + new SimpleDateFormat().format(new Date()));
        }
    });
    return writer;
}
```

**Escritura de archivos XML**

```java
@Bean
@StepScope
public StaxEventItemWriter xmlWriten(@Value("#{jobParameters['fileOutput']}") FileSystemResource output) {
    XStreamMarshaller marshaller = new XStreamMarshaller();
    HashMap<String, Class> aliases = new HashMap<>();
    aliases.put("product", Producto.class);
    marshaller.setAliases(aliases);
    marshaller.setAutodetectAnnotations(true);
    StaxEventItemWriter staxEventItemWriter = new StaxEventItemWriter();
    staxEventItemWriter.setResource(output);
    staxEventItemWriter.setMarshaller(marshaller);
    staxEventItemWriter.setRootTagName("Products");
    return staxEventItemWriter;
}
```


**Insertar registros a la base de datos con una consulta nativa.**

```java
public JdbcBatchItemWriter dbWriter() {
    JdbcBatchItemWriter writer = new JdbcBatchItemWriter();
    writer.setDataSource(dataSource);
    writer.setSql("insert into spring_batch_lab_dev.product (product_name,product_desc,unit,price) " +
            "values (?,?,?,?)");
    writer.setItemPreparedStatementSetter(new ItemPreparedStatementSetter<Producto>() {
        @Override
        public void setValues(Producto item, PreparedStatement ps) throws SQLException {
            ps.setString(1, item.getProdName() + " new Product");
            ps.setString(2, item.getProductDesc());
            ps.setInt(3, item.getUnit());
            ps.setBigDecimal(4, item.getPrice());
        }
    });
    return writer;
}
```

**Insertar registros a la base de datos con una consulta nativa con JdbcBatchItemWriterBuilder.**

```java
@Bean
public ItemWriter itemWriterBuilder() {
    return new JdbcBatchItemWriterBuilder<Producto>()
            .dataSource(dataSource)
            .sql("insert into spring_batch_lab_dev.product (product_name,product_desc,unit,price) " +
                    "values (:prodName ,:productDesc ,:unit ,:price )")
            .beanMapped()
            .build();
}
```

## Clasificacion de datos

**Clasificación de datos y escrituras basadas en una descicion tomada por parte del sistema enfocada en una restriccion.**

Para ello debemos tener en cuenta que tenemos que definir un “Classifier” de java desde la dependencia de:
Org.springframework.batch.support.annotation.Classifier

Esto nos ayudara para poder tomar una decisión con respecto a la información recibida con el archivo de entrada para la lectura.


```java
@Classifier
public String identificarCancelados(Producto itemFile) {
    if (itemFile.getPrice().compareTo(new BigDecimal(55)) == 1) {
        return "C_APROBADO";
    } else {
        return "C_RECHAZADO";
    }

}
```
Función de implementacion para el classifier:

```java
@Bean
public ItemWriter<Producto> itemWriterClassfier() {
    BackToBackPatternClassifier classifier = new BackToBackPatternClassifier();
    classifier.setRouterDelegate(new myClassifier());
    classifier.setMatcherMap(new HashMap<String, ItemWriter<? extends Producto>>() {
        {
            put("C_APROBADO", xmlWritenAprobado());
            put("C_RECHAZADO", xmlWritenRechasado());
        }
    });
    ClassifierCompositeItemWriter<Producto> writen = new ClassifierCompositeItemWriter<>();
    writen.setClassifier(classifier);
    return writen;
}
```

metodos para escribir archivos xml basada en la claficación:

- xmlWritenAprobado
```java
@Bean
public StaxEventItemWriter xmlWritenAprobado() {
    XStreamMarshaller marshaller = new XStreamMarshaller();
    HashMap<String, Class> aliases = new HashMap<>();
    aliases.put("product", Producto.class);
    marshaller.setAliases(aliases);
    marshaller.setAutodetectAnnotations(true);
    StaxEventItemWriter staxEventItemWriter = new StaxEventItemWriter();
    staxEventItemWriter.setResource(new FileSystemResource("output/product_aprobados.xml"));
    staxEventItemWriter.setMarshaller(marshaller);
    staxEventItemWriter.setRootTagName("Products");
    return staxEventItemWriter;
}
```
- xmlWritenRechasado
```java
 @Bean
public StaxEventItemWriter xmlWritenRechasado() {
    XStreamMarshaller marshaller = new XStreamMarshaller();
    HashMap<String, Class> aliases = new HashMap<>();
    aliases.put("product", Producto.class);
    marshaller.setAliases(aliases);
    marshaller.setAutodetectAnnotations(true);
    StaxEventItemWriter staxEventItemWriter = new StaxEventItemWriter();
    staxEventItemWriter.setResource(new FileSystemResource("output/product_rechazados.xml"));
    staxEventItemWriter.setMarshaller(marshaller);
    staxEventItemWriter.setRootTagName("Products");
    return staxEventItemWriter;
}
```


Metodo `step` dentro de la ejecución del job que durante su proceso se hace la clasificación de información.


```java
 @Bean
public Step step2() {
    return steps.get("step2")
        .<Integer, Integer>chunk(3)
        .reader(serviceItemReader())
        .writer(xmlWriten(null)) 
        .writer(itemWriterClassfier()).//classifier para clasificacion de registros
            stream(xmlWritenAprobado()).stream(xmlWritenRechasado()) // seleccion de que metodo ejecutar
        .build();
}
```


## Processor data 

**Este proceso hace que se ignore los productos con id = 2**

```java
@Override
public Producto process(Producto item) throws Exception {
    if (item.getProductId() == 2) {
        return null;
    } else {
        item.setProductDesc(item.getProductDesc().toUpperCase());
    }
    return item;
}
```

Momento en el que se manda a llamar el processor:


```java
@Bean
public Step step2() {
    return steps.get("step2").
        <Integer, Integer>chunk(3)
        .reader(serviceItemReader())
        .writer(xmlWriten(null)) //escribe en un formato XML
        .processor(new ProductoProcessor())
        .writer(itemWriterClassfier()).//classifier para clasificacion de registros
              stream(xmlWritenAprobado()).stream(xmlWritenRechasado()) // seleccion de que metodo ejecutar
        .build();
}
```

## 🚀 About Me
I'm a full stack developer...

