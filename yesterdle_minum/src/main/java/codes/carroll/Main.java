package codes.carroll;

import com.renomad.minum.web.FullSystem;
import com.renomad.minum.state.Context;

import codes.carroll.scrape.Scraper;

public class Main {

    public static void main(String[] args) {
        FullSystem fs = FullSystem.initialize();
        Context ctx = fs.getContext();

        Scraper scraper = new Scraper( ctx.getDb("words", WordModel.EMPTY) );
        scraper.start();

        new Registry(ctx).registerDomains();

        fs.block();
    }
}
