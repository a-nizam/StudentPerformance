package su.logix.studperformance.main.Core;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

public class UploadController extends Controller {
    protected void showFileDialog(TextField tfFile, FileChooser.ExtensionFilter extFilter) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбор файла");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(tfFile.getScene().getWindow());
        if (file != null) {
            tfFile.setText(file.getPath());
        }
    }

    private InputStream fileInputStream;

    protected XMLStreamReader getXmlStreamReader(String filePath) throws XMLStreamException {
        try {
            fileInputStream = new BufferedInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            showMessage(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть файл");
            e.printStackTrace();
        }
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        return xmlInputFactory.createXMLStreamReader(fileInputStream, "UTF-8");
    }

    protected void closeInputStream() throws IOException {
        fileInputStream.close();
    }
}