/*
 * Copyright (c) 2020. Thomas GUILLAUME & Gabriel DUGNY
 */

package com.bdd.psymeeting.controller.patients;

import com.bdd.psymeeting.App;
import com.bdd.psymeeting.controller.InitController;
import com.bdd.psymeeting.model.Job;
import com.bdd.psymeeting.model.Patient;
import com.bdd.psymeeting.model.User;
import com.jfoenix.controls.*;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PatientFormController implements Initializable, InitController {

    // Element javaFX
    public JFXTextField last_name_field;
    public JFXTextField email_field;
    public JFXTextField name_field;
    public JFXDatePicker birthday_field;
    public JFXComboBox<String> gender_field;
    public JFXComboBox<String> relation_field;
    public JFXComboBox<String> discovery_field;
    public JFXButton jobs_button;
    public JFXButton addPatientButtonForm;
    public JFXComboBox<String> jobs_list_field;
    public StackPane stackPane;
    public Label warring;


    private RequiredFieldValidator validator_field;

    private ArrayList<Job> jobs;

    public final Service<Boolean> addingPatientService = new Service<>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() {
                    try {
                        return submitPatient();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }
            };
        }
    };

    public static Service<Boolean> addingPatientStatic = null;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        addingPatientStatic = addingPatientService;

        jobs = new ArrayList<>();

        initServices();

        initListeners();

    }

    @Override
    public void initServices() {
        // Service settings
        addingPatientService.setOnSucceeded(event -> {
            System.out.println("Task adding patient to DataBase succeeded !");
            App.sceneMapping("patients_scene", "patients_scene");
            addingPatientService.reset();
        });

        addingPatientService.setOnFailed(event -> {
            /*warring = new Label("L'email renseigné est déjà pris");
            warring.getStyleClass().add("warring_label");
            stackPane.getChildren().add(stackPane.getChildren().size() - 2, warring);*/
            System.out.println("Task adding patient to DataBase failed !");
            addingPatientService.reset();
        });
    }

    @Override
    public void initListeners() {
        // validation settings
        validator_field = new RequiredFieldValidator();
        validator_field.setMessage("Le champs est obligatoire");

        addListenerValidationField(name_field);
        addListenerValidationField(last_name_field);
        addListenerValidationField(email_field);
        addListenerValidationField(birthday_field);

        gender_field.getItems().addAll("Homme", "Femme", "Non défini", "Autre");
        relation_field.getItems().addAll("Célibataire", "Couple", "Autre");
        discovery_field.getItems().addAll("Autre patient", "Pages Jaunes", "Internet", "Autres");

        // submit patient to database
        addPatientButtonForm.setOnAction(event -> {
            stackPane.getChildren().remove(warring);
            if (validField()) {
                if (addingPatientService.getState() == Task.State.READY) {
                    addingPatientService.start();
                }
            } else {
                validatePriorityField();
            }
        });

        jobs_button.setOnAction(event -> loadJobForm());
    }

    private void loadJobForm() {
        // create dialog layout
        JFXDialogLayout content = new JFXDialogLayout();
        // add heading
        content.setHeading(new Label("Ajout d'un métier"));

        // add body
        VBox jobForm = new VBox();
        jobForm.setSpacing(20);

        HBox jobNameBox = new HBox();
        jobNameBox.setSpacing(20);
        Label jobName = new Label("Nom du métier");

        Region space = new Region();
        space.setMinWidth(20);
        JFXTextField jobNameField = new JFXTextField();

        jobNameBox.getChildren().addAll(jobName, jobNameField);

        HBox jobDateBox = new HBox();
        jobDateBox.setSpacing(20);
        Label jobDate = new Label("Date");
        Region space2 = new Region();
        space.setMinWidth(20);
        JFXDatePicker jobDateField = new JFXDatePicker();
        jobDateBox.getChildren().addAll(jobDate, space2, jobDateField);

        addListenerValidationField(jobDateField);
        addListenerValidationField(jobNameField);

        jobForm.getChildren().addAll(jobNameBox, jobDateBox);
        content.setBody(jobForm);

        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton done = new JFXButton("Fermer");
        JFXButton submit = new JFXButton("Sauvegarder");

        submit.setOnAction(event -> {
            if (validJobField(jobNameField, jobDateField)) {
                jobs.add(new Job(jobNameField.getText().toUpperCase(), Date.valueOf(jobDateField.getValue())));
                jobs_list_field.getItems().add(jobs.get(jobs.size() - 1).getJobName());
                dialog.close();
            } else {
                validatePriorityJobField(jobNameField, jobDateField);
            }

        });

        done.setOnAction(event -> dialog.close());

        content.setActions(submit, done);

        dialog.show();
    }

    private boolean validField() { // check if fields are not empty
        return !name_field.getText().isEmpty()
                && !last_name_field.getText().isEmpty()
                && !email_field.getText().isEmpty()
                && birthday_field.getValue() != null;
    }

    private boolean validJobField(JFXTextField jobNameField, JFXDatePicker jobDateField) {
        return !jobNameField.getText().isEmpty() && jobDateField.getValue() != null;
    }

    private void validatePriorityJobField(JFXTextField jobNameField, JFXDatePicker jobDateField) { // add require validation if priority fields are empty
        if (jobNameField.getText().isEmpty()) {
            jobNameField.validate();
        }
        if (jobDateField.getValue() == null) {
            jobDateField.validate();
        }
    }

    private void validatePriorityField() { // add require validation if priority fields are empty
        if (name_field.getText().isEmpty()) {
            name_field.validate();
        }
        if (last_name_field.getText().isEmpty()) {
            last_name_field.validate();
        }
        if (email_field.getText().isEmpty()) {
            email_field.validate();
        }
        if (birthday_field.getValue() == null) {
            birthday_field.validate();
        }
    }

    private void addListenerValidationField(JFXTextField field) {
        field.getValidators().add(validator_field);
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                field.resetValidation();
            }
        });
    }

    private void addListenerValidationField(JFXDatePicker field) {
        field.getValidators().add(validator_field);
        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                field.resetValidation();
            }
        });

    }

    /**
     * Submit patient to Database
     *
     * @return true if succeeded !
     */
    private boolean submitPatient() throws SQLException {
        if (User.userExist(email_field.getText())) {
            return false;
        } else {
            /*
             *  Create patient, user and jobs => add to Database
             */
            int lastPatientID = Patient.getLastPrimaryKeyId();
            int lastUserId = User.getLastUserId();

            if (lastPatientID >= 0 && lastUserId >= 0) {
                Patient newPatient = new Patient(
                        lastPatientID + 1,
                        name_field.getText().toUpperCase(),
                        last_name_field.getText().toUpperCase(),
                        Date.valueOf(birthday_field.getValue()),
                        gender_field.getValue() == null ? "" : gender_field.getValue(),
                        relation_field.getValue() == null ? "" : relation_field.getValue(),
                        discovery_field.getValue() == null ? "" : discovery_field.getValue());

                User newUser = new User(lastUserId + 1, email_field.getText(), lastPatientID + 1, true);

                // insert job to database
                for (Job job : jobs) {
                    job.setPatientID(newPatient.getPatientId() + 1);
                }

                Patient.submitNewPatient(newPatient, newUser, jobs);
            }
        }
        return false;
    }


}
