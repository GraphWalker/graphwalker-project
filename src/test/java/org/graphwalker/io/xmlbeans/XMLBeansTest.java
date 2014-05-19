package org.graphwalker.io.xmlbeans;

import org.apache.xmlbeans.XmlException;
import org.graphdrawing.graphml.xmlns.GraphmlDocument;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class XMLBeansTest {

    @Test
    public void doTest() throws IOException, XmlException {
        GraphmlDocument graphmlDocument = GraphmlDocument.Factory.parse(getClass().getResourceAsStream("/graphml/UC01.graphml"));
        Assert.assertThat(graphmlDocument.selectPath(
                "declare namespace xq='http://graphml.graphdrawing.org/xmlns';" +
                        "$this/xq:graphml/xq:graph/xq:node"
        ).length, is(8));
        Assert.assertThat(graphmlDocument.selectPath(
                "declare namespace xq='http://graphml.graphdrawing.org/xmlns';" +
                "$this/xq:graphml/xq:graph/xq:edge"
        ).length, is(12));
    }
}
