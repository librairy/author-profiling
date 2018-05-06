package es.linkeddata.librairy.model;

import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class DOI {

    private static final Logger LOG = LoggerFactory.getLogger(DOI.class);

    private final String value;


    public DOI(String value) {
        this.value = value;
    }


    public List<String> getPDFs(){
        if (Strings.isNullOrEmpty(value)) return Collections.emptyList();
        List<String> pdfs = new ArrayList<>();
        try {
            Random random = new Random();

            HttpResponse<JsonNode> jsonResponse = Unirest.get("http://api.unpaywall.org/v2/" + value + "?email=example"+random.nextInt(1000)+"@yopmail.com").asJson();

            JSONObject jsonData = jsonResponse.getBody().getObject();

            if (jsonData.has("is_oa") && jsonData.getBoolean("is_oa")){

                Iterator<Object> locationsIt = jsonData.getJSONArray("oa_locations").iterator();
                while(locationsIt.hasNext()){

                    JSONObject locationJson = (JSONObject) locationsIt.next();
                    Object res = locationJson.get("url_for_pdf");
                    if (res instanceof String) pdfs.add(locationJson.getString("url_for_pdf"));

                }

            }


        } catch (Exception e) {
            LOG.error("Unexpected remote error",e);
        } finally{
            return pdfs;
        }
    }
}
