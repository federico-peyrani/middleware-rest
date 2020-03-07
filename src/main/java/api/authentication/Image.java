package api.authentication;

import api.APIManager;
import api.resources.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Resource(href = APIManager.API_PROTECTED_IMAGE + "/${id}", templated = true)
public class Image {

    private final byte[] raw;
    @Resource.Property(key = "filename")
    private final String filename;
    @Resource.Property(key = "id")
    private final String id;

    public Image(byte[] raw, String filename) {
        this.raw = raw;
        this.filename = filename;
        this.id = UUID.randomUUID().toString();
    }

    public Image(byte[] raw, String filename, String id) {
        this.raw = raw;
        this.filename = filename;
        this.id = id;
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

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(id, image.id);
    }

}
