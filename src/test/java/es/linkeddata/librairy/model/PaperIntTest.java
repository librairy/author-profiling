package es.linkeddata.librairy.model;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class PaperIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(PaperIntTest.class);

    @Test
    public void remotePDF(){

        String url ="http://ceur-ws.org/Vol-1931/paper-03.pdf";

        Paper paper = new Paper(url,"","");

        String text = paper.getText();

        Assert.assertFalse(text.isEmpty());

        LOG.info(text);


    }
}
