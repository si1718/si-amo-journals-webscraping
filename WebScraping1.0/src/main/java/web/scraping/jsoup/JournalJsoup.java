package web.scraping.jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.HttpStatusException;
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

public class JournalJsoup {

	public static final String URL_SISIUS = "https://investigacion.us.es/sisius/sisius.php?inside=1&en=1&text2search=%%%";
	public static final String BASE_URL = "https://investigacion.us.es";

	public static final String MONGO_URI = "mongodb://andopr:andopr@ds261745.mlab.com:61745/si1718-amo-journals";

	private static List<org.bson.Document> arrayofJSON = new ArrayList<org.bson.Document>();

	public static List<String> lstTitles = new ArrayList<String>();

	public final static String SCIMAGO_SEARCH_URL = "http://www.scimagojr.com/journalsearch.php?q=";
	public final static String SCIMAGO_BASE_URL = "http://www.scimagojr.com/";

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
		// searchInScimagoFromSisiusTitle();

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
			} else {
			}
		}

		client.close();

	}

	private static void getJournalTitleFromSISIUS(List<String> researchersListURL, JSONObject jsonJournal)
			throws MalformedURLException, IOException {

		try {
			researchersListURL.subList(25, researchersListURL.size()).clear();
			System.out.println(researchersListURL.size());
			for (String str : researchersListURL) {
				Document doc2 = Jsoup.parse(new URL(BASE_URL + str), 25000);
				for (Iterator<Element> iter = doc2.getElementsByTag("em").iterator(); iter.hasNext();) {
					Element e3 = iter.next();
					String e4 = e3.text();
					String plainTitle = e4.replace("En: ", "");
					if (!lstTitles.contains(plainTitle)) {
						lstTitles.add(plainTitle);
						jsonJournal.put("title", plainTitle);
						String SEARCH_URL_WITH_TITLE = (SCIMAGO_SEARCH_URL + plainTitle).replaceAll(" ", "%20");
						Document searchDoc = Jsoup.parse(new URL(SEARCH_URL_WITH_TITLE), 5000);
						int i = 0;
						for (Iterator<Element> it = searchDoc.getElementsByClass("search_results").iterator(); it
								.hasNext();) {
							Element e = it.next();
							if (i % 2 == 0) {
								Elements e2 = e.getElementsByTag("a");
								Element first = e2.first();
								if (first != null) {
									String JOURNAL_URL_SCIMAGO = SCIMAGO_BASE_URL + first.attr("href");
									Document journalDoc = Jsoup.parse(new URL(JOURNAL_URL_SCIMAGO), 5000);
									for (Iterator<Element> iteror = journalDoc
											.getElementsByClass("journaldescription colblock").iterator(); iteror
													.hasNext();) {
										Element base = iteror.next();
										String editorial = base.getElementsByTag("td").get(6).text();
										String issn = base.getElementsByTag("td").get(10).text();
										if (issn.contains(", ")) {
											String onlyOneIssn = issn.substring(0, 8);
											jsonJournal.put("issn", onlyOneIssn);
											jsonJournal.put("idJournal", onlyOneIssn);
										} else {
											jsonJournal.put("idJournal", issn);
											jsonJournal.put("issn", issn);
										}
										String area = base.getElementsByTag("a").get(1).text();
										String keywords = base.getElementsByTag("a").get(2).text();
										List<String> keywordsList = new ArrayList<String>();
										if (keywords.contains("and") || keywords.contains(" ")) {
											String[] takeAndOut = keywords.split("and");
											for (String withSpaces : takeAndOut) {
												if (withSpaces.contains(" ")) {
													String[] splited = withSpaces.trim().split("\\s+");
													for (String k1 : splited) {
														keywordsList.add(k1);
													}
												} else {
													for (String k2 : takeAndOut) {
														keywordsList.add(k2);
													}
												}
											}

										}
										jsonJournal.put("keywords", keywordsList);
										jsonJournal.put("editorial", editorial);
										jsonJournal.put("area", area);
										org.bson.Document bson = org.bson.Document.parse(jsonJournal.toString());
										arrayofJSON.add(bson);

										System.out.println("Added: " + bson.get("title") + " with issn: "
												+ bson.get("issn") + " editorial " + bson.get("editorial") + " area: "
												+ bson.get("area") + "with keywords: " + bson.get(keywords));

										System.out.println(arrayofJSON.size());
									}
								} else {
									System.out.println("Journal not found in Scimago");
									/*
									 * jsonJournal.put("idJournal", "undefined"); jsonJournal.put("issn",
									 * "undefined"); jsonJournal.put("editorial", "undefined");
									 * jsonJournal.put("area", "undefined"); jsonJournal.put("keywords",
									 * "undefined"); org.bson.Document bson =
									 * org.bson.Document.parse(jsonJournal.toString()); arrayofJSON.add(bson);
									 * System.out.println(arrayofJSON.size());
									 */
								}
							}
							i++;
						}

					}

				}

			}
		} catch (HttpStatusException e) {
			System.out.println(e);
		}

	}

}