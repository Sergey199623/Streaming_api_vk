import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private String path = "src/main/resources/config.properties";

    public ConfigReader() {

    }

    public ConfigReader(String path) {
        this.path = path;
    }

    public String getProp(String propName) {
        String result = "";
        try (InputStream input = new FileInputStream(this.path)) {
            Properties prop = new Properties();
            prop.load(input);
            result = prop.getProperty(propName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
