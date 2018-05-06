package es.linkeddata.librairy;

import com.google.common.base.Strings;
import es.linkeddata.librairy.client.LibrAIryClient;
import es.linkeddata.librairy.model.Author;
import es.linkeddata.librairy.model.Publication;
import org.librairy.service.learner.facade.rest.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws IOException, InterruptedException {


        BufferedReader reader = new BufferedReader(new FileReader(new File("src/main/resources/authors.csv")));
        String line;

        Integer min_publications = 0;

        String user = System.getenv("LIBRAIRY_USER");
        String pwd = System.getenv("LIBRAIRY_PWD");

        LibrAIryClient librAIryClient = new LibrAIryClient("http://librairy.linkeddata.es/learner",user,pwd);


        ConcurrentHashMap<Publication,List<Author>> uniquePublications = new ConcurrentHashMap<>();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0l, TimeUnit.MILLISECONDS, new LinkedBlockingDeque(10), new ThreadPoolExecutor.CallerRunsPolicy());

        while((line = reader.readLine()) != null){
            final String text = line;
            executor.submit(() -> {
                String[] values = text.split(";;");
                Author author = new Author(values[0]);
                List<Publication> publications = author.getPublications();

                List<Publication> availablePublications = publications.stream().filter(pub -> pub.getPaper().getFilePath().isPresent()).collect(Collectors.toList());

                if (availablePublications.size() >= min_publications){
                    availablePublications.forEach( publication -> {
                        List<Author> commonAuthors = new ArrayList<>();

                        if (uniquePublications.containsKey(publication)){
                            commonAuthors = uniquePublications.get(publication);
                        }

                        commonAuthors.add(author);

                        uniquePublications.put(publication,commonAuthors);

                    });

                    LOG.info(author.getName() + ": Total=" + publications.stream().filter(pub -> pub.getPaper().getFilePath().isPresent()).count());
                }

            });
        }
        LOG.info("waiting for threads..");
        executor.shutdown();
        executor.awaitTermination(1,TimeUnit.HOURS);
        reader.close();

        AtomicInteger counter = new AtomicInteger(1);
        uniquePublications.entrySet().stream().forEach( entry -> {
            try{
                Publication publication = entry.getKey();
                List<String> labels = entry.getValue().stream().map(author -> author.getName().replace(" ", "_")).collect(Collectors.toList());
                String text = publication.getPaper().getText();
                if (!Strings.isNullOrEmpty(text)){
                    librAIryClient.addDocument(String.valueOf(counter.getAndIncrement()), publication.getTitle(), labels, publication.getPaper().getText());
                    LOG.info("Publication added: '" + entry.getKey().getTitle() + "' - " + entry.getValue());
                }
            }catch (Exception e){
                LOG.error("Unexpected error",e);
            }
        });

        librAIryClient.train();
        LOG.info("Topic Model ready to be created ..");

    }

}
