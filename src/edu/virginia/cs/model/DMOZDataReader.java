/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.model;

import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Wasi
 */
public class DMOZDataReader extends DefaultHandler {

    private final HashMap<String, ArrayList<String>> categoryToContent;
    private boolean isContent = false;
    private final StringBuilder content;
    private String currentTopicName;

    public DMOZDataReader() {
        content = new StringBuilder();
        categoryToContent = new HashMap<>();
    }

    public HashMap<String, ArrayList<String>> getCategoryToContent() {
        return categoryToContent;
    }

    @Override
    public void startElement(String uri,
            String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("DMOZ")) {
            System.out.println("Parsing started...");
        } else if (qName.equalsIgnoreCase("Topic")) {
            currentTopicName = attributes.getValue("name");
            ArrayList<String> tempList = new ArrayList<>();
            categoryToContent.put(currentTopicName, tempList);
        } else if (qName.equalsIgnoreCase("content")) {
            isContent = true;
            content.setLength(0);
        }
    }

    @Override
    public void endElement(String uri,
            String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("DMOZ")) {
            System.out.println("Parsing Completed...");
        } else if (qName.equalsIgnoreCase("Topic")) {
        } else if (qName.equalsIgnoreCase("content")) {
            isContent = false;
            if (!content.toString().isEmpty()) {
                ArrayList<String> tempList = categoryToContent.get(currentTopicName);
                tempList.add(content.toString());
            }
        }
    }

    @Override
    public void characters(char ch[],
            int start, int length) throws SAXException {
        if (isContent) {
            content.append(ch, start, length);
        }
    }
}
