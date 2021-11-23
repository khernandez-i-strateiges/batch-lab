package com.swt.helloworld.config;


import com.swt.helloworld.ProductoRespository;
import com.swt.helloworld.listener.HwJobExecutionListener;
import com.swt.helloworld.listener.HwStepExecutionListener;
import com.swt.helloworld.model.Product;
import com.swt.helloworld.model.Producto;
import com.swt.helloworld.processor.InMemeItemProcessor;
import com.swt.helloworld.reader.InMemReader;
import com.swt.helloworld.reader.ProductServiceAdapter;
import com.swt.helloworld.services.ProductService;
import com.swt.helloworld.writer.ConsoleItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.*;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.persistence.Column;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@Configuration
public class BatchCondifguration {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private HwJobExecutionListener hwJobExecutionListener;

    @Autowired
    private HwStepExecutionListener hwStepExecutionListener;

    @Autowired
    private InMemeItemProcessor inMemeItemProcessor;

    @Autowired
    private DataSource dataSource;

    @Autowired
    @Lazy
    private ProductoRespository productoRespository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductServiceAdapter productServiceAdapter;


    public Tasklet helloWorldTasklet() {
        return (new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello world  ");
                return RepeatStatus.FINISHED;
            }
        });
    }

    @Bean
    public Step step1() {
        return steps.get("step1")
                .listener(hwStepExecutionListener)
                .tasklet(helloWorldTasklet())
                .build();
    }

    @Bean
    public InMemReader reader() {
        return new InMemReader();
    }

    /**
     * methods to reader files
     */

    /**
     * method to read .XML file
     *
     * @param inputFile
     * @return
     */
    @StepScope
    @Bean
    public StaxEventItemReader xlmItemReader(
            @Value("#{jobParameters['fileInput']}")
                    FileSystemResource inputFile) {
        // where to read the xlm fule
        StaxEventItemReader reader = new StaxEventItemReader();
        reader.setResource(inputFile);
        // need reader to know  tags describre the domain object
        reader.setFragmentRootElementName("product");
        // tell reader to parse XML  and which domain object to be mapped
        reader.setUnmarshaller(new Jaxb2Marshaller() {
            {
                setClassesToBeBound(Product.class);
            }
        });
        return reader;
    }


    /**
     * method to read input file .CSV
     *
     * @param inputFile
     * @return
     */
    @StepScope
    @Bean
    public FlatFileItemReader flatFileItemReader(
            @Value("#{jobParameters['fileInput']}")
                    FileSystemResource inputFile) {
        FlatFileItemReader reader = new FlatFileItemReader();
        // step 1 let reader know where is the file
        reader.setResource(inputFile);
        //create the line Mapper
        reader.setLineMapper(
                new DefaultLineMapper<Product>() {
                    {
                        setLineTokenizer(new DelimitedLineTokenizer() {
                            {
                                setNames(new String[]{"prodId", "productName", "prodDesc", "price", "unit"});
                                setDelimiter("|");
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


    /**
     * method to read from Query native from data base.
     *
     * @return
     */
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

    /**
     * method to read from JPA repository.
     *
     * @return
     */
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

    /**
     * method to read file json from input batch process
     *
     * @param inputFile
     * @return
     */

    @StepScope
    @Bean
    public JsonItemReader jsonItemReader(
            @Value("#{jobParameters['fileInput']}")
                    FileSystemResource inputFile) {
        JsonItemReader reader = new JsonItemReader(inputFile, new JacksonJsonObjectReader<>(Product.class));
        return reader;
    }

    /**
     * method to read from service of product. (read Json response)
     *
     * @return
     */
    @Bean
    public ItemReaderAdapter serviceItemReader() {
        ItemReaderAdapter reader = new ItemReaderAdapter();
        reader.setTargetObject(productServiceAdapter);
        reader.setTargetMethod("nextProduct");
        return reader;
    }

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

    @Bean
    public Step step2() {
        return steps.get("step2").
                        <Integer, Integer>chunk(3)
                //.reader(flatFileItemReader(null)) archivos csv
                //.reader(xlmItemReader(null)) archivos xml
                //.reader(flatFixFileItemReader(null)) archivos txt con un tamaño fijo
                //.reader(jdbcCursorItemReader()) conexion cruda a la base de datos
                //.reader(itemReader()) conexion mediante JPA Repository
                //.reader(jsonItemReader(null)) lectura de archivos json
                //lecturas de web services.
                .reader(serviceItemReader())
                .writer(flagFileItemWriter(null))
                .build();
    }

    @Bean
    public Job helloWorldJob() {
        return jobs.get("helloWorldJob")
                .incrementer(new RunIdIncrementer())
                .listener(hwJobExecutionListener)
                .start(step1())
                .next(step2())
                .build();
    }

    /**
     * methods to writer files
     */

    /**
     * method to write output  file in the batch process
     *
     * @param output param that recibe route from write output file
     * @return writer
     */
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

        //definimos una columna de header para el archvi de output de nuestro proceso batch
        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("productId | prodName | productDesc | unit | price");
            }
        });
        // this properties have to permission to Override in the file or write in the next Line False/True
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
}
