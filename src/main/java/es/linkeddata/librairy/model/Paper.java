package es.linkeddata.librairy.model;

import com.google.common.base.Strings;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Paper {

    private static final Logger LOG = LoggerFactory.getLogger(Paper.class);
    private DOI doi;
    private final String ee;
    private String url;

    public Paper(String url, String ee, String doi) {
        this.url = url;
        this.ee = ee;
        this.doi = new DOI(doi);
    }

    public Optional<String> getFilePath(){
        Optional<String> path;
        if (!Strings.isNullOrEmpty(url) && url.endsWith(".pdf")){
            path = Optional.of(url);
        }else if (!Strings.isNullOrEmpty(ee) && ee.endsWith(".pdf")){
            path = Optional.of(ee);
        }else{
            path = doi.getPDFs().stream().filter(url ->{
                try{
                    InputStream stream = new URL(url).openStream();
                    stream.close();
                    return true;
                }catch(Exception e){
                    return false;
                }

            } ).findFirst();
        }
        return path;
    }

    public String getText(){
        try{
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            Optional<String> fileUrl = getFilePath();
            if (fileUrl.isPresent()){
                parser.parse(new URL(fileUrl.get()).openStream(),handler, metadata);
                return handler.toString();
            }else return "";
        }catch (Exception e){
            LOG.error("Unexpected parsing error",e);
            return "";
        }
    }

    @Override
    public String toString() {
        return "Paper{" +
                "doi='" + doi + '\'' +
                ", ee='" + ee + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
