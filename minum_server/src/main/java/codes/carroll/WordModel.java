package codes.carroll;

import com.renomad.minum.database.DbData;

import static com.renomad.minum.utils.SerializationUtils.deserializeHelper;
import static com.renomad.minum.utils.SerializationUtils.serializeHelper;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class WordModel extends DbData<WordModel> {
    private long id;
    public final String word;
    public final ZonedDateTime timestamp;

    public static final WordModel EMPTY = new WordModel(0L, "piety");

    public WordModel(long id, String word) {
        super();

        this.id = id;
        this.word = word;
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public WordModel(long id, String word, ZonedDateTime timestamp) {
        super();

        this.id = id;
        this.word = word;
        this.timestamp = timestamp;
    }

    @Override
    protected long getIndex() {
        return this.id;
    }

    @Override
    protected void setIndex(long arg0) {
        this.id = arg0;
    }

    @Override
    public String serialize() {
        return serializeHelper(id, word, timestamp);
    }

    @Override
    public WordModel deserialize(String serializedText) {
        final var tokens = deserializeHelper(serializedText);
        return new WordModel(
                Long.parseLong(tokens.get(0)),
                tokens.get(1),
                ZonedDateTime.parse(Objects.requireNonNull(tokens.get(2))));
    }
}
