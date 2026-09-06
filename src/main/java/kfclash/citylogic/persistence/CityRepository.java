package kfclash.citylogic.persistence;

import java.io.IOException;
import java.nio.file.Path;

/** Repository abstraction for storing and retrieving complete city saves. */
public interface CityRepository {
    void save(Path path, CitySaveData city) throws IOException;

    CitySaveData load(Path path) throws IOException;
}
