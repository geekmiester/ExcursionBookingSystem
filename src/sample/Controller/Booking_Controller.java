package sample.Controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Booking_Controller implements Initializable {
    private static String Excursion_ID, Port_ID, Excursion_Name, Booked_Seat, Booked_Date;
    private static ObservableList<booking_info> booking;

    @FXML
    private Label Cancel_ID;

    @FXML
    private StackPane userBooking_StackPane;

    @FXML
    private JFXTextField Update_textfield_Seat;

    @FXML
    private JFXButton cancelBooking_Button;

    @FXML
    private TableView<booking_info> tableView;

    @FXML
    private TableColumn<booking_info, String> ExcursionID_Column;

    @FXML
    private TableColumn<booking_info, String> PortID_Column;

    @FXML
    private TableColumn<booking_info, String> ExcursionName_Column;

    @FXML
    private TableColumn<booking_info, String> SeatNo_Column;

    @FXML
    private TableColumn<booking_info, String> BookedDate_Column;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sample.Controller.Login_Controller login_controller;
        login_controller = new sample.Controller.Login_Controller();

        ExcursionID_Column.setCellValueFactory(new PropertyValueFactory<booking_info, String>("Excursion_ID"));
        PortID_Column.setCellValueFactory(new PropertyValueFactory<booking_info, String>("Port_ID"));
        ExcursionName_Column.setCellValueFactory(new PropertyValueFactory<booking_info, String>("Excursion_Name"));
        SeatNo_Column.setCellValueFactory(new PropertyValueFactory<booking_info, String>("Booked_Seat"));
        BookedDate_Column.setCellValueFactory(new PropertyValueFactory<booking_info, String>("Booked_Date"));

        tableView.setItems(getBooking());

        try {
            Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
            Statement statement = myConn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `booking` WHERE `Booked By` = '" + login_controller.loggedInID + "' AND `Status` = 'Booked'");
            while (rs.next()) {
                Excursion_ID = rs.getString("Excursion ID");
                Port_ID = rs.getString("Port ID");
                Excursion_Name = rs.getString("Excursion Name");
                Booked_Seat = rs.getString("Booked Seat");
                Booked_Date = rs.getString("Booked Date");
                booking.add(new booking_info(Excursion_ID, Port_ID, Excursion_Name, Booked_Seat, Booked_Date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Cancel_ID.setText(tableView.getSelectionModel().getSelectedItem().getExcursion_ID());
            }
        });
    }

    public void cancelBooking_Button() throws SQLException {
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            System.out.println("Select a table");
        } else {
            userBooking_StackPane.setVisible(true);

            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("Cancel"));
            content.setBody(new Text("Booking that are cancelled are deleted forever. Are you sure you want to Cancel?"));
            JFXDialog dialog = new JFXDialog(userBooking_StackPane, content, JFXDialog.DialogTransition.CENTER);
            JFXButton closeButton = new JFXButton("Close");
            closeButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialog.close();
                    userBooking_StackPane.setVisible(false);
                }
            });

            JFXButton okayButton = new JFXButton("Okay");
            okayButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String bookedSeatByUser = null;
                    sample.Controller.Login_Controller login_controller;
                    login_controller = new sample.Controller.Login_Controller();
                    try {
                        Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
                        Statement statement = myConn.createStatement();
                        statement.execute("UPDATE `booking` SET `Status`='Cancelled' WHERE `Booked By`='" + login_controller.loggedInID + "' AND `Excursion ID`='" + tableView.getSelectionModel().getSelectedItem().getExcursion_ID() + "'");
                        ResultSet bookedSeatByUser_RS = statement.executeQuery("SELECT `Booked Seat` FROM `booking` WHERE `Booked By`='" + login_controller.loggedInID + "' AND `Excursion ID`='" + tableView.getSelectionModel().getSelectedItem().getExcursion_ID() + "'");
                        while (bookedSeatByUser_RS.next()) {
                            bookedSeatByUser = bookedSeatByUser_RS.getString("Booked Seat");
                        }
                        statement.execute("UPDATE `excursions` SET `Seat`=`Seat`+'" + Integer.parseInt(bookedSeatByUser) + "' WHERE `ID` = '" + tableView.getSelectionModel().getSelectedItem().getExcursion_ID() + "'");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    dialog.close();
                    userBooking_StackPane.setVisible(false);
                }
            });
            content.setActions(closeButton, okayButton);

            dialog.show();

        }

    }

    public void updateBooking_Add() throws SQLException {
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            System.out.println("Select a table");
        } else {
            if (Update_textfield_Seat.getText().isEmpty()) {
                System.out.println("TextField is Null");
            } else {
                if (Integer.parseInt(Update_textfield_Seat.getText()) > 32) {
                    System.out.println("The Seat limit is 32!");
                }
                else if (Integer.parseInt(Update_textfield_Seat.getText())<=0){
                    System.out.println("Seat cannot be 0");
                }
                else {
                    String exSeat = null;
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
                    Statement statement = con.createStatement();
                    ResultSet exSeatRS = statement.executeQuery("SELECT `Seat` FROM `excursions` WHERE `ID`='" + tableView.getSelectionModel().getSelectedItem().getExcursion_ID() + "'");
                    while (exSeatRS.next()) {
                        exSeat = exSeatRS.getString("Seat");
                    }
                    if (Integer.parseInt(exSeat) <= 0) {
                        System.out.println("Sorry no Seat Available");
                    } else if (Integer.parseInt(Update_textfield_Seat.getText()) > Integer.parseInt(exSeat)) {
                        System.out.println(exSeat + " seat available");
                    } else {
                        userBooking_StackPane.setVisible(true);

                        JFXDialogLayout content = new JFXDialogLayout();
                        content.setHeading(new Text("Update Seat"));
                        content.setBody(new Text("Confirm Adding " + Update_textfield_Seat.getText() + " Seats?"));
                        JFXDialog dialog = new JFXDialog(userBooking_StackPane, content, JFXDialog.DialogTransition.CENTER);

                        JFXButton closeButton = new JFXButton("Close");
                        closeButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                dialog.close();
                                userBooking_StackPane.setVisible(false);
                            }
                        });

                        JFXButton okayButton = new JFXButton("Okay");
                        okayButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                String bookedSeatByUser = null;
                                sample.Controller.Login_Controller login_controller;
                                login_controller = new sample.Controller.Login_Controller();
                                try {
                                    Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
                                    Statement statement = myConn.createStatement();
                                    statement.execute("UPDATE `booking` SET `Booked Seat`= `Booked Seat` +'" + Integer.parseInt(Update_textfield_Seat.getText()) + "'WHERE `Excursion ID`='" + tableView.getSelectionModel().getSelectedItem().getExcursion_ID() + "'AND `Booked By`='" + login_controller.loggedInID + "'");
                                    statement.execute("UPDATE `excursions` SET `Seat`=`Seat`-'" + Integer.parseInt(Update_textfield_Seat.getText()) + "' WHERE `ID` = '" + tableView.getSelectionModel().getSelectedItem().getExcursion_ID() + "'");
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                dialog.close();
                                userBooking_StackPane.setVisible(false);
                            }
                        });
                        content.setActions(closeButton, okayButton);

                        dialog.show();
                    }

                }
            }
        }

    }
    public void updateBooking_Sub()throws SQLException{
        if (tableView.getSelectionModel().getSelectedItem() == null){
            System.out.println("Select a table");
        }
        else{
            if (Update_textfield_Seat.getText().isEmpty()){
                System.out.println("TextField is Null");
            }
            else {
                if (Integer.parseInt(Update_textfield_Seat.getText()) > 32){
                    System.out.println("The Seat limit is 32!");
                }
                else if (Integer.parseInt(Update_textfield_Seat.getText())<=0){
                    System.out.println("Seat cannot be 0");
                }
                else {
                    String exSeat = null, boSeat = null;
                    sample.Controller.Login_Controller login_controller;
                    login_controller = new sample.Controller.Login_Controller();

                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs","root", "");
                    Statement statement = con.createStatement();
                    ResultSet boSeatRS = statement.executeQuery("SELECT `Booked Seat` FROM `booking` WHERE `Excursion ID`='"+tableView.getSelectionModel().getSelectedItem().getExcursion_ID()+"'AND `Booked By`='"+login_controller.loggedInID+"'");
                    while (boSeatRS.next()){
                        boSeat = boSeatRS.getString("Booked Seat");
                    }
                    if (Integer.parseInt(boSeat) <= 1){
                        System.out.println("Sorry Seat must be atleast 1.");
                    }
                    else if (Integer.parseInt(Update_textfield_Seat.getText())>=Integer.parseInt(boSeat)){
                        System.out.println(boSeat+" seat in your bookings.");
                    }
                    else {
                        userBooking_StackPane.setVisible(true);

                        JFXDialogLayout content = new JFXDialogLayout();
                        content.setHeading(new Text("Update Seat"));
                        content.setBody(new Text("Confirm Substract "+Update_textfield_Seat.getText()+" Seats?"));
                        JFXDialog dialog = new JFXDialog(userBooking_StackPane, content, JFXDialog.DialogTransition.CENTER);

                        JFXButton closeButton = new JFXButton("Close");
                        closeButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                dialog.close();
                                userBooking_StackPane.setVisible(false);
                            }
                        });

                        JFXButton okayButton = new JFXButton("Okay");
                        okayButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event){
                                String bookedSeatByUser = null;
                                sample.Controller.Login_Controller login_controller;
                                login_controller = new sample.Controller.Login_Controller();
                                try {
                                    Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs","root", "");
                                    Statement statement = myConn.createStatement();
                                    statement.execute("UPDATE `booking` SET `Booked Seat`= `Booked Seat` -'"+Integer.parseInt(Update_textfield_Seat.getText())+"'WHERE `Excursion ID`='"+tableView.getSelectionModel().getSelectedItem().getExcursion_ID()+"'AND `Booked By`='"+login_controller.loggedInID+"'");
                                    statement.execute("UPDATE `excursions` SET `Seat`=`Seat`+'"+Integer.parseInt(Update_textfield_Seat.getText())+"' WHERE `ID` = '"+tableView.getSelectionModel().getSelectedItem().getExcursion_ID()+"'");
                                }
                                catch (SQLException e){
                                    e.printStackTrace();
                                }
                                dialog.close();
                                userBooking_StackPane.setVisible(false);
                            }
                        });
                        content.setActions(closeButton, okayButton);

                        dialog.show();
                    }

                }
            }
        }


    }

    public ObservableList<booking_info> getBooking() {
        booking = FXCollections.observableArrayList();
        return booking;
    }

}
