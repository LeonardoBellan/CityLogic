package kfclash.citylogic.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/** Jackson-backed repository that persists city saves as readable JSON. */
public final class JsonCityRepository implements CityRepository {
    private final ObjectMapper objectMapper;

    public JsonCityRepository() {
        this(new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT));
    }

    private JsonCityRepository(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
    }

    @Override
    public void save(Path path, CitySaveData city) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(city, "city cannot be null");
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        objectMapper.writeValue(path.toFile(), city);
    }

    @Override
    public CitySaveData load(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        return objectMapper.readValue(path.toFile(), CitySaveData.class);
    }
}
