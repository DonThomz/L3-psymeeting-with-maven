/*
 * Copyright (c) 2020. Thomas GUILLAUME & Gabriel DUGNY
 */

package com.bdd.psymeeting.controller.consultations;

import com.bdd.psymeeting.App;
import com.bdd.psymeeting.controller.MainController;
import com.bdd.psymeeting.model.Consultation;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXSpinner;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ConsultationHistoric {

    protected static Consultation consultationModified;
    protected final Service<Boolean> loadConsultations = new Service<>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() {
                    return setupBoxConsultations(); // init consultation
                }
            };
        }
    };
    public static Service<Boolean> loadConsultationsStatic = null;

    // FXML
    @FXML
    public StackPane stackPane;
    @FXML
    public VBox box_consultations;
    protected Calendar date_today;
    protected ArrayList<Consultation> consultationArrayList;
    protected Consultation consultationToBeRemove;
    protected final Service<Boolean> removeConsultationService = new Service<>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<>() {
                @Override
                protected Boolean call() {
                    return removeConsultation(consultationToBeRemove);
                }
            };
        }
    };

    protected JFXSpinner spinner;

    protected ConsultationHistoric() {

        loadConsultationsStatic = loadConsultations;

        removeConsultationService.setOnSucceeded(event -> {
            if (removeConsultationService.getValue()) {
                System.out.print("Task Remove consultation succeeded !");
                refresh();
            } else {
                System.out.print("Task succeeded but remove consultation from database failed !");
            }
            removeConsultationService.reset();
        });

        removeConsultationService.setOnFailed(event -> {
            System.out.print("Task Remove consultation failed !");
            removeConsultationService.reset();
        });

    }

    /**
     * method call when we need to refresh scene
     */
    protected void refresh() {
    }


    private boolean removeConsultation(Consultation consultation) {
        return Consultation.removeConsultation(consultation);
    }

    public void spinnerLoading(Pane pane) {
        spinner = new JFXSpinner();
        spinner.setPrefSize(50, 50);
        pane.getChildren().add(spinner);
    }

    public void removeSpinner(Pane pane) {
        pane.getChildren().remove(spinner);
    }

    // --------------------
    //  Consultation methods
    // --------------------

    protected boolean setupBoxConsultations() {
        return true;
    }

    protected void createBoxConsultations(String styleClass) {
        for (Consultation c : consultationArrayList
        ) {
            if (c.getDate().compareTo(date_today) < 0) {
                c.getConsultation_button().setStyle("-fx-background-color:  #eceff1;");
            }
            c.getConsultation_button().getStyleClass().add(styleClass);
            box_consultations.getChildren().add(c.getConsultation_button());
        }
    }

    protected Consultation buildConsultationButton(Consultation consultation) {

        // init button
        JFXButton consultation_button = new JFXButton();
        // setting button
        consultation_button.setId("consultation-button-id-" + consultation.getConsultationID());


        // Setup content
        VBox box = new VBox();

        assert consultation.getDate() != null;
        @SuppressWarnings("SpellCheckingInspection") String timeStamp = new SimpleDateFormat(
                "EEEE dd MMMM, yyyy à HH:mm",
                Locale.FRANCE).format(consultation.getDate().getTime());
        Label title = new Label("Consultation : "
                                        + "\n\t" + timeStamp);

        box.getChildren().add(title);

        // create label and add patients
        Label patient_list = new Label();
        patient_list.getStyleClass().add("content_text");
        StringBuilder content = new StringBuilder();
        consultation.getPatients().forEach((id, fullName) -> content.append("| ").append(fullName.get(0)).append(" ").append(fullName.get(1)).append("\n"));
        patient_list.setText(String.valueOf(content));
        box.getChildren().add(patient_list);


        // add action on button
        consultation_button.setOnAction(event -> loadConsultationInfo(consultation));

        // add to the button
        consultation_button.setGraphic(box);

        // add JFXButton to consultation
        consultation.setConsultation_button(consultation_button);

        return consultation;
    }

    protected void loadConsultationInfo(Consultation consultation) {
        // create dialog layout
        JFXDialogLayout content = new JFXDialogLayout();

        // add heading
        content.setHeading(createTitle(consultation.getDate()));

        // add body
        content.setBody(createBody(consultation));

        JFXDialog dialog = new JFXDialog(MainController.stackPaneStatic, content, JFXDialog.DialogTransition.CENTER);
        JFXButton done = new JFXButton("Fermer");
        JFXButton modify = new JFXButton("Modifier");

        // check if consultation is passed
        LocalDate localDate = LocalDateTime.ofInstant(consultation.getDate().toInstant(), consultation.getDate().getTimeZone().toZoneId()).toLocalDate();
        JFXButton remove = new JFXButton("Supprimer");
        if (LocalDate.now().compareTo(localDate) <= 0) {
            content.getActions().add(remove);
        }

        done.setOnAction(event -> dialog.close());

        modify.setOnAction(event -> {
            dialog.close();
            loadModifyDialogPane(consultation);
        });

        remove.setOnAction(event -> {
            dialog.close();
            if (removeConsultationService.getState() == Task.State.READY) {
                consultationToBeRemove = consultation;
                removeConsultationService.start();
            }
        });

        content.getActions().addAll(modify, done);

        dialog.show();
    }


    /**
     * create the dialog pane to modify a consultation
     *
     * @param consultation selected by the user
     */
    protected void loadModifyDialogPane(Consultation consultation) {

        consultationModified = consultation;

        // check if consultation is passed
        /*LocalDate localDate = LocalDateTime.ofInstant(consultation.getDate().toInstant(), consultation.getDate().getTimeZone().toZoneId()).toLocalDate();
        if (LocalDate.now().compareTo(localDate) < 0)
            consultationVBox.getChildren().add(dateBox(consultation));
        consultationVBox.getChildren().add(patientsBox(consultation));*/

        //modifyFrom.setBody(consultationVBox);

        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        try {
            StackPane object = FXMLLoader.load(App.class.getResource("views/consultations/modification_consultation_form.fxml"));

            dialogLayout.setBody(object);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        JFXDialog dialog = new JFXDialog(MainController.stackPaneStatic, dialogLayout, JFXDialog.DialogTransition.CENTER);

        JFXButton done = new JFXButton("Annuler");
        JFXButton save = new JFXButton("Sauvegarder");

        dialog.setOnDialogClosed(event -> dialog.close());

        done.setOnAction(event -> dialog.close());

        save.setOnAction(event -> {
            System.out.println(ModifConsultationController.commentaryAreaStatic);
            dialog.close();
        });

        dialogLayout.setActions(save, done);

        dialog.show();


    }



    /*
        Info consultations methods
     */

    protected TextArea createBody(Consultation consultation) {

        // print age


        // get Patients Info
        StringBuilder patientsInfo = new StringBuilder();

        consultation.getPatients().forEach((id, patient_info) -> patientsInfo.append("| ")
                .append(patient_info.get(0)).append(" ")
                .append(patient_info.get(1)).append(" ")
                .append(patient_info.get(4)).append("\n"));
        if (consultation.isInRelation() && consultation.getPatients().size() > 1) {
            patientsInfo.append("Les patients sont venues en couple");
        }

        // get Feedback Info
        StringBuilder feedbackInfo = new StringBuilder();

        // info price and pay mode
        feedbackInfo.append("Prix : ").append(consultation.getPrice()).append(" €, payé avec : ").append(consultation.getPayMode());
        // info feedback commentary, key words, postures, indicator
        feedbackInfo.append("\n\nRetour de séance").append("\n\n\tCommentaire : \n").append(consultation.getFeedback().getCommentary());
        if (consultation.getFeedback().getKeyword() != null) {
            feedbackInfo.append("\n\n\tMots clés :").append(consultation.getFeedback().getKeyword());
        }
        if (consultation.getFeedback().getPosture() != null) {
            feedbackInfo.append("\n\n\tPosture :").append(consultation.getFeedback().getPosture());
        }
        if (consultation.getFeedback().getIndicator() != 0) {
            feedbackInfo.append("\n\n\tIndicateur :").append(consultation.getFeedback().getIndicator());
        }

        TextArea textArea = new TextArea("Patients :\n"
                                                 + patientsInfo + "\n"
                                                 + feedbackInfo + "\n"
        );
        textArea.setWrapText(true);
        textArea.setEditable(false);
        return textArea;
    }

    protected Label createTitle(Calendar date) {
        @SuppressWarnings("SpellCheckingInspection") String format_date = new SimpleDateFormat(
                "EEEE dd MMMM, yyyy à HH:mm",
                Locale.FRANCE).format(date.getTime());
        return new Label("Consultation du " + format_date);
    }


}
