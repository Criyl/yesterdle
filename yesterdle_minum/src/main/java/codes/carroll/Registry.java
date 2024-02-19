package codes.carroll;

import com.renomad.minum.web.RequestLine;
import com.renomad.minum.Context;
import com.renomad.minum.database.Db;
import com.renomad.minum.templating.TemplateProcessor;
import com.renomad.minum.web.Request;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.WebFramework;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Collection;
import java.util.stream.Stream;

public class Registry {

        private final Context context;
        private final WebFramework webFramework;
        private final TemplateProcessor yesterdayTemplateProcessor;
        private final TemplateProcessor todayTemplateProcessor;
        private final TemplateProcessor historyTemplateProcessor;

        public Registry(Context context) {
                String base_template = context
                                .getFileUtils()
                                .readTextFile("src/main/webapp/templates/base.html");
                String today_template = context
                                .getFileUtils()
                                .readTextFile("src/main/webapp/templates/today.html");
                String yesterday_template = context
                                .getFileUtils()
                                .readTextFile("src/main/webapp/templates/yesterday.html");
                String history_template = context
                                .getFileUtils()
                                .readTextFile("src/main/webapp/templates/history.html");

                TemplateProcessor baseTemplateProcessor = TemplateProcessor.buildProcessor(base_template);

                this.todayTemplateProcessor = TemplateProcessor.buildProcessor(
                                baseTemplateProcessor.renderTemplate(
                                                Map.of("which", "Todaydle", "content", today_template)));

                this.yesterdayTemplateProcessor = TemplateProcessor.buildProcessor(
                                baseTemplateProcessor.renderTemplate(
                                                Map.of("which", "Yesterdle", "content", yesterday_template)));

                this.historyTemplateProcessor = TemplateProcessor.buildProcessor(
                                baseTemplateProcessor.renderTemplate(
                                                Map.of("which", "Histordle", "content", history_template)));

                this.context = context;
                this.webFramework = context.getFullSystem().getWebFramework();
        }

        public void registerDomains() {
                webFramework.registerPath(RequestLine.Method.GET, "", r -> Response.redirectTo("yesterday"));
                webFramework.registerPath(RequestLine.Method.GET, "yesterday", this::yesterday);
                webFramework.registerPath(RequestLine.Method.GET, "today", this::today);
                webFramework.registerPath(RequestLine.Method.GET, "history", this::history);
        }

        public Response history(Request r) {
                Db<WordModel> words = context.getDb("words", WordModel.EMPTY);
                Collection<WordModel> wordModelStream = words.values();

                if (wordModelStream.size() == 0) {
                        return Response.htmlOk(historyTemplateProcessor.renderTemplate(Map.of(
                                        "wordlist", "")));
                }

                String worldListHtml = wordModelStream.stream()
                                .sorted((x, y) -> y.timestamp.compareTo(x.timestamp))
                                .skip(1)
                                .map(word -> {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MM/dd/yyyy");
                                        String formattedString = word.timestamp.format(formatter);
                                        return "<li> <a id='word'>" + word.word + "</a></br><a id='date'>"
                                                        + formattedString
                                                        + "</a></li>";
                                })
                                .reduce("", (subtotal, element) -> subtotal + element);

                Map<String, String> templateValues = Map.of(
                                "wordlist", worldListHtml);
                return Response.htmlOk(historyTemplateProcessor.renderTemplate(templateValues));
        }

        public Response today(Request r) {
                String wordText = "No Data";
                ZonedDateTime wordDateTime = ZonedDateTime.now();

                try {
                        Db<WordModel> words = context.getDb("words", WordModel.EMPTY);
                        WordModel wordModel = words.values().stream().toList().getLast();
                        wordText = wordModel.word;
                        wordDateTime = wordModel.timestamp;
                } catch (Exception e) {
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                String formattedString = wordDateTime.format(formatter);

                Map<String, String> templateValues = Map.of(
                                "word", wordText,
                                "date", formattedString);
                return Response.htmlOk(todayTemplateProcessor.renderTemplate(templateValues));
        }

        public Response yesterday(Request r) {
                String wordText = "No Data";
                ZonedDateTime wordDateTime = ZonedDateTime.now();

                try {
                        Db<WordModel> words = context.getDb("words", WordModel.EMPTY);
                        List<WordModel> wordList = words.values().stream().toList();
                        WordModel wordModel = wordList.get(wordList.size() - 2);
                        wordText = wordModel.word;
                        wordDateTime = wordModel.timestamp;
                } catch (Exception e) {

                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                String formattedString = wordDateTime.format(formatter);

                Map<String, String> templateValues = Map.of(
                                "word", wordText,
                                "date", formattedString);
                return Response.htmlOk(yesterdayTemplateProcessor.renderTemplate(templateValues));
        }
}