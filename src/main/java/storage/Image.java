package storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

public class Image {

    private final byte[] raw;
    private final String filename;
    private final String url;

    public Image(byte[] raw, String filename) {
        this.raw = raw;
        this.filename = filename;
        this.url = UUID.randomUUID().toString();
    }

    public static Image fromInputStream(InputStream inputStream, String filename) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return new Image(buffer.toByteArray(), filename);
    }

    public byte[] raw() {
        return raw;
    }

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(url, image.url);
    }

}
