package com.tianrui.top10downloadapps;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Generate the string to display on the list view, use code beatify to reform the code,then extract
 * the useful information from the xml file downloaded from Apple SSR. http://codebeautify.org/xmlviewer
 * Created by tianrui on 2017-01-03.
 */

public class ParseApplication {
    private static final String TAG = "ParseApplication";
    private ArrayList<FeedEntry> applications;

    public ParseApplication() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String xmlData) {
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;
        String textVaule = "";

        //setting xmlParser
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        //Log.d(TAG, "parse: Starting tag for " + tagName);
                        if ("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry(); //If meet a new app, new a FeedEntry to store the info.
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textVaule = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        //Log.d(TAG, "parse: Ending tag for " + tagName);
                        if (inEntry) {
                            if ("entry".equalsIgnoreCase(tagName)) {
                                applications.add(currentRecord);
                                inEntry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textVaule);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textVaule);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textVaule);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textVaule);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textVaule);
                            }
                        }
                        break;
                    default:
                        //nothing to do for the default case.
                }
                eventType = xpp.next();

            }

//            for (FeedEntry app : applications) {
//                Log.d(TAG, "*******************************");
//                Log.d(TAG, app.toString());
//            }

        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;
    }
}
