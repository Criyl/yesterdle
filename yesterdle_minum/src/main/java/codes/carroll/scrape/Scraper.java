package codes.carroll.scrape;

import com.renomad.minum.web.FullSystem;
import com.renomad.minum.database.Db;

import org.json.JSONException;
import org.json.JSONObject;

import codes.carroll.WordModel;

import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import java.util.List;
import java.lang.Thread;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.lang.InterruptedException;

public class Scraper extends Thread  {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long INTERVAL = Long.parseLong(System.getenv().getOrDefault("INTERVAL", "30")) * 60 * 1000;

    private Db<WordModel> wordDatabase;

    public Scraper(Db<WordModel> wordDatabase) {
        this.wordDatabase = wordDatabase;
    }

    private static void add(Db<WordModel> db, String word) {
        if (word.length() != 5) {
            return;
        }

        for (WordModel model : db.values()) {
            if (model.word.matches(word)) {
                return;
            }
        }

        WordModel wordModel = new WordModel(0, word);
        db.write(wordModel);
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
    }
    
    public void findWordAndPopulate() throws IOException, JSONException {
        System.out.println("Finding Word");
        InputStream is = new URL("https://www.nytimes.com/svc/wordle/v2/"+dtf.format(ZonedDateTime.now(ZoneId.of("UTC")))+".json").openStream();

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            String word = json.getString("solution").toUpperCase();
            System.out.println(word);
            add(this.wordDatabase, word);
        } finally {
          is.close();
        }
    }
    
    @Override
    public void run() {
        if(INTERVAL <= 0){
            System.err.println("Cannot Start Scraper with an INTERVAL less than 1 minute");
            return;
        }
        while (true) {
            try {
                System.out.println("Scraping Started");

                if ( wordDatabase.values().size() == 0 ){
                    System.out.println("Database Empty");
                    findWordAndPopulate();
                    Thread.sleep(INTERVAL);
                    continue;
                }

                ZonedDateTime wordDateTime = ZonedDateTime.now();

                WordModel wordModel = wordDatabase.values().stream().toList().getLast();
                String wordText = wordModel.word;
                ZonedDateTime lastWordDateTime = wordModel.timestamp;

                if( ZonedDateTime.now().getDayOfMonth() != lastWordDateTime.getDayOfMonth() ){
                    findWordAndPopulate();
                } else {
                    System.out.println("Word Already Found Today");
                }
                Thread.sleep(INTERVAL);
            } catch (InterruptedException | IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
