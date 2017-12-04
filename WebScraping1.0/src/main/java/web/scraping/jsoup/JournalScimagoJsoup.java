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

public class JournalScimagoJsoup {
	public final static String SCIMAGO_URL_BASE = "http://www.scimagojr.com/journalrank.php";

	public static List<String> getJournalsTitlesFromScimago(String[] args) throws MalformedURLException, IOException {

		List<String> searchURLsList = new ArrayList<String>();
		int i = 0;
		int pageNum = 28606;
		int result;
		for (i = 1; i < 10; i++) {
			result = i;
			String FORMED_URL_FOR_SEARCH = SCIMAGO_URL_BASE + "?page=" + i + "&total_size=28606";
			searchURLsList.add(FORMED_URL_FOR_SEARCH);
		}

		List<String> journalsTitleList = new ArrayList<String>();
		for (String url : searchURLsList) {
			Document doc = Jsoup.parse(new URL(url), 5000);

			int j = 0;
			Iterator<Element> it = doc.getElementsByClass("tit").iterator();
			while (it.hasNext()) {
				Element e = it.next();
				// System.out.println(e);
				Elements e2 = e.getElementsByTag("a");
				journalsTitleList.add(e2.text());
			}
			j++;
		}
		
		 System.out.println(journalsTitleList);
		return journalsTitleList;
	}
}
