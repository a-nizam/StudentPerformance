package su.logix.studperformance.main.Core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import su.logix.studperformance.main.Models.FacultyModel;
import su.logix.studperformance.main.Models.StudentModel;
import su.logix.studperformance.main.Models.SubjectModel;

import java.sql.SQLException;

public class FieldsController extends Controller {

    private static final Logger log = Logger.getLogger(FieldsController.class);

    private ObservableList<FacultyModel> facultyList;
    private ObservableList<Integer> semesterList;
    private ObservableList<String> groupList;
    private ObservableList<SubjectModel> subjectList;

    protected void initFaculties(ComboBox<FacultyModel> cbFaculty) {
        facultyList = FXCollections.observableArrayList();
        try {
            facultyList.addAll(FacultyModel.getList());
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("loading faculty list error", e);
        }
        cbFaculty.setItems(facultyList);
        Callback<ListView<FacultyModel>, ListCell<FacultyModel>> cellFactory = new Callback<>() {
            @NotNull
            @Contract(value = "_ -> new", pure = true)
            @Override
            public ListCell<FacultyModel> call(ListView<FacultyModel> p) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(FacultyModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };
        cbFaculty.setCellFactory(cellFactory);
        cbFaculty.setButtonCell(cellFactory.call(null));
    }

    protected void initSubjects(ComboBox<SubjectModel> cbSubject) {
        subjectList = FXCollections.observableArrayList();
        cbSubject.setItems(subjectList);

        Callback<ListView<SubjectModel>, ListCell<SubjectModel>> cellFactory = new Callback<>() {
            @NotNull
            @Contract(value = "_ -> new", pure = true)
            @Override
            public ListCell<SubjectModel> call(ListView<SubjectModel> p) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(SubjectModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        };
        cbSubject.setCellFactory(cellFactory);
        cbSubject.setButtonCell(cellFactory.call(null));
    }

    protected void initSemesters(ComboBox<Integer> cbSemester) {
        semesterList = FXCollections.observableArrayList();
        semesterList.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        cbSemester.setItems(semesterList);
    }

    protected void initGroups(ComboBox<String> cbGroup) {
        groupList = FXCollections.observableArrayList();
        cbGroup.setItems(groupList);
    }

    protected void loadSubjects(ComboBox<FacultyModel> cbFaculty, ComboBox<Integer> cbSemester) {
        if (!(cbFaculty.getSelectionModel().isEmpty() || cbSemester.getSelectionModel().isEmpty())) {
            subjectList.clear();
            try {
                subjectList.addAll(SubjectModel.getListByFacultyAndSemester(cbFaculty.getValue().getCode(), cbSemester.getValue()));
            } catch (SQLException e) {
                log.error("loading subjects list error", e);
            }
        }
    }

    protected void loadGroups(ComboBox<FacultyModel> cbFaculty, ComboBox<Integer> cbSemester) {
        if (!(cbFaculty.getSelectionModel().isEmpty() || cbSemester.getSelectionModel().isEmpty())) {
            groupList.clear();
            try {
                groupList.addAll(StudentModel.getGroupsByFacultyAndSemester(cbFaculty.getValue().getCode(), cbSemester.getValue()));
            } catch (SQLException e) {
                e.printStackTrace();
                log.error("loading groups list error", e);
            }
        }
    }
}
