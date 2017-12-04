package web.scraping.jsoup;

import web.scraping.jsoup.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;

import java.util.Iterator;

import java.util.List;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import net.minidev.json.JSONObject;

public class JournalJsoup {

	public static final String URL_SISIUS = "https://investigacion.us.es/sisius/sisius.php?inside=1&en=1&text2search=%%%";
	public static final String BASE_URL = "https://investigacion.us.es";

	private static List<org.bson.Document> arrayofJSON = new ArrayList();

	public static void main(String[] args) throws MalformedURLException, IOException {

		
		List<String> researchersListURL = new ArrayList<String>();
		Document doc = Jsoup.parse(new URL(URL_SISIUS), 10000);
		int i = 0;
		for (Iterator<Element> it = doc.getElementsByClass("data").iterator(); it.hasNext();) {
			Element e = it.next();
			if (i % 2 == 0) {
				Elements e2 = e.getElementsByTag("a");
				researchersListURL.add(e2.attr("href"));
			}
			i++;
		}

		JSONObject jsonJournal = new JSONObject();
		getJournalTitleFromSISIUS(researchersListURL, jsonJournal);
		//getJournalsInCommon(researchersListURL);
		
		MongoClientURI uri  = new MongoClientURI("mongodb://andopr:andopr@ds261745.mlab.com:61745/si1718-amo-journals");
	    MongoClient client = new MongoClient(uri);

	    MongoDatabase db = client.getDatabase(uri.getDatabase());
	    MongoCollection col = db.getCollection("journals");
	    
	    col.insertMany(arrayofJSON);

	 	client.close();
	
	}

	private static List<String> getJournalsInCommon(List<String> researchersListURL)
			throws MalformedURLException, IOException {
		List<String> commonList = new ArrayList<String>();
		List<String> journalTitlesFromScimago = new ArrayList<String>();
		journalTitlesFromScimago.add("Neurocomputing");
		List<String> journalsFromSisius = getJournalTitleFromSISIUSList(researchersListURL);
		if (journalsFromSisius.contains(journalTitlesFromScimago.get(0))) {
			System.out.println("contenido");
		} else {
			System.out.println("no contenido");
		}
		return commonList;
	}

	private static List<String> getJournalTitleFromSISIUSList(List<String> researchersListURL)
			throws MalformedURLException, IOException {
		for (String str : researchersListURL) {
			Document doc2 = Jsoup.parse(new URL(BASE_URL + str), 5000);
			for (Iterator<Element> iter = doc2.getElementsByTag("em").iterator(); iter.hasNext();) {
				Element e3 = iter.next();
				String e4 = e3.text();
				String e4Formatted = e4.replace("En: ", "");
			}
		}
		return researchersListURL;
	}

	private static void getJournalTitleFromSISIUS(List<String> researchersListURL, JSONObject jsonJournal)
			throws MalformedURLException, IOException {
		for (String str : researchersListURL) {
			Document doc2 = Jsoup.parse(new URL(BASE_URL + str), 5000);
			for (Iterator<Element> iter = doc2.getElementsByTag("em").iterator(); iter.hasNext();) {
				Element e3 = iter.next();
				String e4 = e3.text();
				String e4Formatted = e4.replace("En: ", "");
				jsonJournal.put("title", e4Formatted);
				org.bson.Document bson = org.bson.Document.parse(jsonJournal.toString());
				if (!arrayofJSON.contains(bson)) {
					//System.out.println(arrayofJSON.size());
					arrayofJSON.add(bson);
				}

			}

		}

	}

}