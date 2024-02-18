package codes.carroll;

import com.renomad.minum.web.FullSystem;

public class Main {

    public static void main(String[] args) {
        FullSystem fs = FullSystem.initialize();

        new Registry(fs.getContext()).registerDomains();

        fs.block();
    }
}
