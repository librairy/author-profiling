package es.linkeddata.librairy.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Author {

    private static final Logger LOG = LoggerFactory.getLogger(Author.class);

    private final String name;


    static{
        com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        jacksonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);

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


        try {

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            } };


            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            Unirest.setHttpClient(httpclient);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Author(String name) {
        this.name = name.replace(" ","_");
    }


    public List<Publication> getPublications(){
        List<Publication> publications = new ArrayList<>();
        try {
            String requestUrl = "https://dblp.org/search/publ/api?q=author%3A"+ name +"%3A&format=json&h=500";

            HttpResponse<JsonNode> jsonResponse = Unirest.get(requestUrl).asJson();

            Iterator<Object> pubIterator = jsonResponse.getBody().getObject().getJSONObject("result").getJSONObject("hits").getJSONArray("hit").iterator();

            while(pubIterator.hasNext()){

                JSONObject jsonObj = (JSONObject) pubIterator.next();

                if (!jsonObj.has("info")) continue;

                JSONObject pubInfo = jsonObj.getJSONObject("info");

                Publication publication = new Publication();

                // Authors
                List<Author> authors = new ArrayList<>();
                if (pubInfo.has("authors")){


                    Object authorObj = pubInfo.getJSONObject("authors").get("author");

                    if (authorObj instanceof JSONArray){
                        Iterator<Object> authIterator = ((JSONArray) authorObj).iterator();
                        while(authIterator.hasNext()){

                            String authorName = (String) authIterator.next();
                            authors.add(new Author(authorName.replace(" ", "_")));
                        }

                    }else{
                        authors.add(new Author(((String) authorObj).replace(" ", "_")));
                    }

                }
                publication.setAuthors(authors);

                if (pubInfo.has("title")) publication.setTitle(pubInfo.getString("title"));
                if (pubInfo.has("year")) publication.setYear(pubInfo.getString("year"));

                String doi  = pubInfo.has("doi")?   pubInfo.getString("doi")    : "";
                String ee   = pubInfo.has("ee")?    pubInfo.getString("ee")     : "";
                String url  = pubInfo.has("url")?   pubInfo.getString("url")    : "";
                publication.setPaper(new Paper(url,ee,doi));

                publications.add(publication);

            }


        } catch (UnirestException e) {
            LOG.warn("Error getting list of publications",e);
        } finally{
            return publications;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;

        return name != null ? name.equals(author.name) : author.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Author{" +
                "name='" + name + '\'' +
                '}';
    }
}
