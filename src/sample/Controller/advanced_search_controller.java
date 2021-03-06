package sample.Controller;

import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXSlider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import sample.Main;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class advanced_search_controller implements Initializable {

    private String Seat = null, Total_Seat = null;

    @FXML
    private Label ExcursionName_BasicSearch, ExcursionID_BasicSearch, PortID_BasicSearch, sliderNo, RemainingSeat;

    @FXML
    private JFXSlider Booking_NoOfSeat;

    @FXML
    private AnchorPane Pane_ADVSearchResult, home_recommended_place, contain_place2, contain_place1;

    @FXML
    private Pane Places_Pane1, Places_Pane2;

    @FXML
    public ComboBox PortID_box_ID, ExcursionID_box_ID, Excursionname_box_ID;


    ObservableList<String> PortID_box_List = FXCollections.observableArrayList(PortID_comboresult());


    public void ExcursionName_Clicked(){
        ObservableList<String> ExcursionName_box_List = FXCollections.observableArrayList(ExcursionName_comboresult());
        Excursionname_box_ID.setItems(ExcursionName_box_List);
    }

    public void ExcursionID_Clicked(){
        ObservableList<String> ExcursionID_box_List = FXCollections.observableArrayList(ExcursionID_comboresult());
        ExcursionID_box_ID.setItems(ExcursionID_box_List);
    }

    public String[] PortID_comboresult(){
        ArrayList<String> results = new ArrayList<String>();
        try {
            Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
            Statement statement = myConn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT DISTINCT `PORT_ID` FROM `excursions`");
            while (rs.next()){
                results.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String[] dbresult = new String[results.size()];
        dbresult = results.toArray(dbresult);
        return dbresult;
    }

    public String[] ExcursionName_comboresult(){
        ArrayList<String> results = new ArrayList<String>();
        try {
            Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
            Statement statement = myConn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT `NAME` FROM `excursions` WHERE `PORT_ID` ='"+PortID_box_ID.getValue()+"'");
            while (rs.next()){
                results.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String[] dbresult = new String[results.size()];
        dbresult = results.toArray(dbresult);
        return dbresult;
    }

    public String[] ExcursionID_comboresult(){
        ArrayList<String> results = new ArrayList<String>();
        try {
            Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
            Statement statement = myConn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT `ID` FROM `excursions` WHERE `NAME` ='"+Excursionname_box_ID.getValue()+"'");
            while (rs.next()){
                results.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String[] dbresult = new String[results.size()];
        dbresult = results.toArray(dbresult);
        return dbresult;
    }

    @FXML
    public void search_basic_change(MouseEvent event) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../FXML/welcome.fxml"));
        Main.stage.getScene().setRoot(root);
    }

    @FXML
    public void SearchResult_Basic(MouseEvent event) throws SQLException{
        Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
        Statement statement = myConn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT `Seat` FROM `excursions` WHERE `ID` ='"+ExcursionID_box_ID.getValue()+"'");
        while (rs.next()) {
            Seat = rs.getString("Seat");
        }
        home_recommended_place.setVisible(false);
        Pane_ADVSearchResult.setVisible(true);

        ExcursionName_BasicSearch.setText(Excursionname_box_ID.getValue().toString());
        ExcursionID_BasicSearch.setText(ExcursionID_box_ID.getValue().toString());
        PortID_BasicSearch.setText(PortID_box_ID.getValue().toString());
        RemainingSeat.setText(Seat);

    }

    @FXML
    void sliderclicked(MouseEvent event) {
        Double noofseat = Booking_NoOfSeat.getValue();
        sliderNo.setText(""+noofseat.intValue());
    }
    @FXML
    private void BookBtn_BasicSearch(MouseEvent event){
        Double noofseat = Booking_NoOfSeat.getValue();
        sample.Controller.welcome_controller welcomeController;
        welcomeController = new sample.Controller.welcome_controller();

        sample.Controller.Login_Controller login_controller;
        login_controller = new sample.Controller.Login_Controller();

        if (welcomeController.isLoggedIn== false){
            welcomeController.Error_doLogin.setVisible(true);
        }
        else if(welcomeController.isLoggedIn == true){
            try {
                Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebs", "root", "");
                Statement statement = myConn.createStatement();
                ResultSet rs = statement.executeQuery("SELECT `Seat` FROM `excursions` WHERE `ID` ='"+ExcursionID_box_ID.getValue()+"'");
                while (rs.next()) {
                    Total_Seat = rs.getString("Seat");
                }
                if (Integer.parseInt(Total_Seat)-noofseat.intValue() < 0){
                    System.out.println("Sorry all the seat of the Excursion is booked. Do you want to be in the waiting list?");
                }
                else{
                    String sql = "INSERT INTO `booking`(`Excursion Name`, `Excursion ID`, `Port ID`, `Booked Seat`, `Booked By`, `Booked Date`)"+"values(?,?,?,?,?,current_timestamp)";
                    Statement stmt = myConn.createStatement();
                    String updateSeat = "UPDATE `excursions` SET `Seat`=`Seat`-'"+noofseat.intValue()+"' WHERE `ID` = '"+ExcursionID_box_ID.getValue()+"'";
                    stmt.executeUpdate(updateSeat);
                    PreparedStatement mySt = myConn.prepareStatement(sql);
                    mySt.setString(1, Excursionname_box_ID.getValue().toString());
                    mySt.setString(2, ExcursionID_box_ID.getValue().toString());
                    mySt.setString(3, PortID_box_ID.getValue().toString());
                    mySt.setString(4, String.valueOf(noofseat.intValue()));
                    mySt.setString(5, login_controller.loggedInID);
                    mySt.executeUpdate();
                    mySt.close();
                    System.out.println("Data sucessfully Inserted");
                    RemainingSeat.setText("");
                    RemainingSeat.setText(Total_Seat);
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        JFXRippler rippler1 = new JFXRippler(Places_Pane1);
        JFXRippler rippler2 = new JFXRippler(Places_Pane2);
        contain_place2.getChildren().add(rippler2);
        contain_place1.getChildren().add(rippler1);
        PortID_box_ID.setItems(PortID_box_List);
    }
}
