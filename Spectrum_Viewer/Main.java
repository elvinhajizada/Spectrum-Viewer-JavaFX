package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

import javafx.util.Duration;
import jssc.*;

public class Main extends Application {
    public static XYChart.Series seriesSpectrum = new XYChart.Series();
    static int [] YData = new int [128];
    Button button;
    private static ObservableList<XYChart.Data> data = FXCollections.observableArrayList();
    @Override public void start(Stage stage) {

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(100),
                ae -> getSerialData()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        button = new Button();
        button.setText("Refresh");
        stage.setTitle("Area Chart Sample");
        button.setOnAction(e -> getSerialData());
        final NumberAxis xAxis = new NumberAxis(1, 128, 4);
        final NumberAxis yAxis = new NumberAxis(0,255,5);
        yAxis.setAutoRanging(false);
        final AreaChart<Number,Number> ac =
                new AreaChart<Number,Number>(xAxis,yAxis);
        ac.setTitle("Temperature Monitoring (in Degrees C)");
        ac.setAnimated(false);


        seriesSpectrum.setName("Spectrum");
        StackPane layout = new StackPane();
        layout.getChildren().addAll(ac,button);
        Scene scene  = new Scene(layout,800,600);
        ac.getData().add(seriesSpectrum);
        stage.setScene(scene);
        stage.show();
        getSerialData();
    }


    public static void main(String[] args) {
        launch(args);

    }

    public static void getSerialData () {
        String[] portNames = SerialPortList.getPortNames();
        byte [] buffer = new byte [500];
        int c = 0;
        if (portNames.length == 0) {
            System.out.println("There are no serial-ports :( You can use an emulator, such ad VSPE, to create a virtual serial port.");
            System.out.println("Press Enter to exit...");
            try {
                System.in.read();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        for (int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }

        SerialPort serialPort = new SerialPort(portNames[0]);
        try {
            //Open port
            serialPort.openPort();
            //We expose the settings. You can also use this line - serialPort.setParams(9600, 8, 1, 0);
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Writes data to port
            //serialPort.writeBytes("Test");
            //Read the data of 10 bytes. Be careful with the method readBytes(), if the number of bytes in the input buffer
            //is less than you need, then the method will wait for the right amount. Better to use it in conjunction with the
            //interface SerialPortEventListener.
            buffer = serialPort.readBytes(500);
            //Closing the port
            serialPort.closePort();

        }

        catch (SerialPortException ex) {
            System.out.println(ex);
        }
        while (c < 300)
        {
            int a = buffer[c] & 0xff;
            if (a > 254)
            {
                int b = buffer[c+1] & 0xff;

                if (b < 140)
                {
                    break;
                }
            }
            c++;
        }

        for (int i = 0; i < 128; i++){
            int k = 0;
            //if (i != 0) {
                k = buffer[c + i + 1] & 0xff;

            System.out.println(k);
            YData [i] = k;
        }


        if (!data.isEmpty())
        {
        data.remove(0, data.size()) ;
        }
        for (int i = 0; i < 128; i++) {
            data.add(new XYChart.Data(i, YData[i]));

        }
        seriesSpectrum.setData(data);

    }
}
