package uk.ac.glasgow.scclippy.plugin.search;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.glasgow.scclippy.plugin.lucene.File;
import uk.ac.glasgow.scclippy.plugin.settings.Settings;
import uk.ac.glasgow.scclippy.plugin.util.URLProcessing;

import java.net.URLEncoder;

/**
 * Class for searching with a web service
 */
public class WebServiceSearch extends Search {

    /**
     * Performs search by querying app server using RESTful services
     * @See Search#search
     */
    public void search(@NotNull String query, int posts) throws Exception {
        if (!inputValidator(query, posts)) {
            files = null;
            return;
        }
        query = query.trim();

        query = URLEncoder.encode(query, "UTF-8");
        JSONObject json = URLProcessing.readJsonFromUrl(Settings.webServiceURI[0] + query + "?posts=" + posts);

        if (json == null) {
            throw new Exception("Query failed. Check connection to server.");
        }

        JSONArray items = json.getJSONArray("results");
        if (items.length() == 0) {
            files = null;
            return;
        }

        files = new File[items.length()];
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = (JSONObject) items.get(i);
            String parentid = item.get("parentid").equals(null) ? "" : item.get("parentid") + "#";
            files[i] = new File("" + parentid + item.getString("id"), item.getString("body"), (int) item.get("score"));
        }
        Search.currentSearchType = SearchType.WEB_SERVICE;
    }
}
