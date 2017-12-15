package web.scraping.jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.Iterator;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import net.minidev.json.JSONObject;

import web.scraping.jsoup.JournalScimagoJsoup;

public class JournalJsoup {

	public static final String URL_SISIUS = "https://investigacion.us.es/sisius/sisius.php?inside=1&en=1&text2search=%%%";
	public static final String BASE_URL = "https://investigacion.us.es";

	public static final String MONGO_URI = "mongodb://andopr:andopr@ds261745.mlab.com:61745/si1718-amo-journals";

	private static List<org.bson.Document> arrayofJSON = new ArrayList<org.bson.Document>();

	public static List<String> lstTitles = new ArrayList<String>();
	public static Integer i = 0;

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

		System.out.println("GOT ALL OF THE JOURNALS!");

		MongoClientURI uri = new MongoClientURI(MONGO_URI);
		MongoClient client = new MongoClient(uri);

		MongoDatabase db = client.getDatabase(uri.getDatabase());
		MongoCollection<org.bson.Document> col = db.getCollection("journals");

		for (org.bson.Document d : arrayofJSON) {
			FindIterable<org.bson.Document> res = col.find(Filters.eq("title", d.get("title")));
			System.out.println(res);
			if (res == null || res.first() == null) {
				col.insertOne(d);
				System.out.println("========AÑADIDO: " + d.get("title") + d.get("issn"));
			} else {
				System.out.println("========Contained");
			}
		}

		client.close();

	}

	private static String getJournalID() {
		String nums = "1234567890";
		String t = "";
		for (int i = 0; i < 8; i++) {
			t += nums.charAt((int) (Math.random() * nums.length()));
		}
		return t;
	}

	private static void getJournalTitleFromSISIUS(List<String> researchersListURL, JSONObject jsonJournal)
			throws MalformedURLException, IOException {
		for (String str : researchersListURL) {
			Document doc2 = Jsoup.parse(new URL(BASE_URL + str), 25000);
			for (Iterator<Element> iter = doc2.getElementsByTag("em").iterator(); iter.hasNext();) {
				Element e3 = iter.next();
				String e4 = e3.text();
				String e4Formatted = e4.replace("En: ", "");
				if (!lstTitles.contains(e4Formatted)) {
					lstTitles.add(e4Formatted);
					jsonJournal.put("title", e4Formatted);
					String idJournal = getJournalID();
					jsonJournal.put("issn", idJournal);
					jsonJournal.put("idJournal", idJournal);
					// System.out.println(jsonJournal.get("issn"));
					org.bson.Document bson = org.bson.Document.parse(jsonJournal.toString());
					arrayofJSON.add(bson);
					// System.out.println("Added: " + bson.get("title") + " with issn: " +
					// bson.get("issn"));
					System.out.println(arrayofJSON.size());
				}
			}

		}

	}

}