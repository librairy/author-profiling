package es.linkeddata.librairy.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.librairy.service.learner.facade.rest.model.Document;
import org.librairy.service.learner.facade.rest.model.ModelParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class LibrAIryClient {

    private static final Logger LOG = LoggerFactory.getLogger(LibrAIryClient.class);


    static{
        Unirest.setDefaultHeader("Accept", "application/json");
        Unirest.setDefaultHeader("Content-Type", "application/json");

        com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
//        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jacksonObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private final String endpoint;
    private final String user;
    private final String pwd;

    public LibrAIryClient(String endpoint, String user, String pwd)  {

        this.endpoint = endpoint;
        this.user = user;
        this.pwd = pwd;

        try{
            Unirest.delete(endpoint + "/documents").basicAuth(user, pwd).asString();
        } catch (UnirestException e) {
            LOG.error("Remote error", e);
        }
    }

    public Document addDocument(String id, String name, List<String> labels, String text){

        Document document = new Document();
        document.setLabels(labels.stream().map(label -> new String(label.getBytes(), Charset.forName("UTF-8"))).collect(Collectors.toList()));
        document.setId(id);
        document.setName(name);
        document.setText(new String(text.getBytes(), Charset.forName("UTF-8")));

        try {
            Unirest.post(endpoint + "/documents").basicAuth(user, pwd).body(document).asString();
            LOG.info("Added document: '" + document.getName()+"'");
        } catch (UnirestException e) {
            LOG.error("Remote error", e);
        }finally {
            return document;
        }
    }

    public void train(){
        ModelParameters modelParameters = new ModelParameters();
        Map<String, String> parameters = ImmutableMap.of(
                "algorithm","llda",
                "language","en",
                "email","cbadenes@fi.upm.es"
        );
        modelParameters.setParameters(parameters);

        try {
            Unirest.post(endpoint + "/dimensions").basicAuth(user, pwd).body(modelParameters).asString();
            LOG.info("Training topic model");
        } catch (UnirestException e) {
            LOG.error("Remote error", e);
        }

    }

}
