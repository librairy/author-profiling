package es.linkeddata.librairy.model;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class AuthorIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorIntTest.class);


    @Test
    public void getPublications(){

        Author author = new Author("Óscar_Corcho");

        List<Publication> publications = author.getPublications();

        LOG.info("Publications: " + publications.size());

        Assert.assertTrue(publications.size() > 40);

        LOG.info("Publication: " + publications.get(0));

    }

    @Test
    public void listAvailable() throws IOException, InterruptedException {

        BufferedReader reader = new BufferedReader(new FileReader(new File("src/main/resources/authors.csv")));
        String line;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0l, TimeUnit.MILLISECONDS, new LinkedBlockingDeque(10), new ThreadPoolExecutor.CallerRunsPolicy());

        while((line = reader.readLine()) != null){
            final String text = line;
            executor.submit(() -> {
                String[] values = text.split(";;");
                Author author = new Author(values[0]);
                List<Publication> publications = author.getPublications();
                LOG.info(author.getName() + ": Total=" + publications.size() + ", Available=" + publications.stream().filter(pub -> pub.getPaper().getFilePath().isPresent()).count());
            });
        }
        LOG.info("waiting for threads..");
        executor.shutdown();
        executor.awaitTermination(1,TimeUnit.HOURS);
        reader.close();
    }


}
