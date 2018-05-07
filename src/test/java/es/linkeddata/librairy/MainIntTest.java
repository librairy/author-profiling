package es.linkeddata.librairy;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.linkeddata.librairy.client.LibrAIryClient;
import es.linkeddata.librairy.model.Publication;
import org.junit.Test;
import org.librairy.service.learner.facade.rest.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class MainIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(MainIntTest.class);



    @Test
    public void train() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("src/main/resources/papers.jsonl.gz"))));

        String user = System.getenv("LIBRAIRY_USER");
        String pwd = System.getenv("LIBRAIRY_PWD");

        LibrAIryClient librAIryClient = new LibrAIryClient("http://librairy.linkeddata.es/learner",user,pwd);

        String line;
        ObjectMapper jsonMapper = new ObjectMapper();
        while((line = reader.readLine()) != null){

            Document document = jsonMapper.readValue(line, Document.class);
            librAIryClient.addDocument(document.getId(), document.getName(), document.getLabels(), document.getText());

        }
        reader.close();

        librAIryClient.train();


    }

}
