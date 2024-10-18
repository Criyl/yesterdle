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

public class Main {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
    
    public static String findWord() throws IOException, JSONException {
        InputStream is = new URL("https://www.nytimes.com/svc/wordle/v2/"+dtf.format(ZonedDateTime.now(ZoneId.of("UTC")))+".json").openStream();

        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JSONObject json = new JSONObject(jsonText);
          return json.getString("solution").toUpperCase();
        } finally {
          is.close();
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        FullSystem fs = FullSystem.initialize();
        Db<WordModel> wordDB = fs.getContext().getDb("words", WordModel.EMPTY);
        String word = findWord();
        System.out.println(word);
        add(wordDB, word);
    }

}
