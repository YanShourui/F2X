package GUI;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FbdToXmlConverter extends Application {

    //具体功能
    private TextArea fbdTextArea = new TextArea();

    private int chapterNumber = 1; // 记录章节编号

    private void openFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("FBD Files", "*.fbd"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                String content = readFileContent(selectedFile);
                fbdTextArea.setText(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile != null) {
            try {
                FileWriter writer = new FileWriter(selectedFile);

                // 获取 fbdTextArea 中的文本
                String fbdContent = fbdTextArea.getText();

                // 将文本按行分割
                String[] lines = fbdContent.split("/EEE");

                // 创建 StringBuilder 用于构建 XML 内容
                StringBuilder xmlBuilder = new StringBuilder();

                // 遍历每一行文本
                for (String line : lines) {
                    // 忽略空行
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    // 处理文本行并添加到 XML 内容中
                    String processedLine = processItem(line);
                    xmlBuilder.append(processedLine).append("\n");
                }

                // 将最终的 XML 内容写入文件
                writer.write(xmlBuilder.toString());
                writer.close();

                // 输出完成的消息
                String message = "输出已完成";

                // 创建一个信息对话框
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("输出结果");
                alert.setHeaderText(null); // 不显示头部文本
                alert.setContentText(message);

                // 显示对话框
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readFileContent(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(file.toURI()));
        return detectEncodingAndDecode(bytes);
    }

    private String detectEncodingAndDecode(byte[] bytes) throws UnsupportedEncodingException {
        String content = new String(bytes, StandardCharsets.UTF_8);

        // 检查BOM标记
        if (content.startsWith("\uFEFF")) {
            return content.substring(1);
        }

        // 尝试GBK编码解码
        try {
            return new String(bytes, "GBK");
        } catch (UnsupportedEncodingException e) {
            // 如果GBK解码失败，则直接使用UTF-8解码
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
    private String processItem(String item) {
        String processedItem = null;
        String result1 = "";
        String result2 = "";
        String prefix = null;
        String content = null;

        String input = item;
        int firstIndex = input.indexOf("〖KG*2〗");
        int secondIndex = input.indexOf("〖KG*2〗", firstIndex + 1);

        String part1 = input.substring(0, secondIndex + 6);
        String part2 = input.substring(secondIndex + 6);

        String regStr1 = "【〖CX2〗〖CT〗(.*?)〖CX〗】〖HT〗〖WTXT〗〖JP〗";
        String regStr2 = "〖KG\\*2〗(.*?)〖KG\\*2〗";

        Pattern pattern1 = Pattern.compile(regStr1);
        Matcher matcher1 = pattern1.matcher(part1);

        if (matcher1.find()) {
            result1 = "<chapter xml:id=\"chapter3-" + chapterNumber + "\" role=\"汉语辞书条目库\"><title xml:id=\"chapter3-" + chapterNumber + ".title\">" + matcher1.group(1) + "</title>" + "<info>" + "<title>" + matcher1.group(1) + "</title>";
        }
        chapterNumber++; // 每次处理完一个字符串后，章节编号自增
        pattern1 = Pattern.compile(regStr2);
        matcher1 = pattern1.matcher(part1);
        if (matcher1.find()) {
            result2 = "<releaseinfo role=\"拼音\">" + matcher1.group(1) + "</releaseinfo>" + "<releaseinfo role=\"来源图书\">新华成语词典</releaseinfo></info>";
        }

        String regex = "〖([^〖〗]+)〗([^〖〗]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(part2);
        StringBuilder contentBuilder = new StringBuilder();

        while (matcher.find()) {
            prefix = matcher.group(1);
            content = matcher.group(2);

            if (prefix.equals("HT8.25,8.5SS")) {
                content = "<para><emphasis role=\"italic\">" + content + "</emphasis></para>";
            } else {
                content = "<para>" + content + "</para>";
            }
            contentBuilder.append(content);
        }
        processedItem = result1 + result2 + contentBuilder.toString();
        return processedItem;
    }

    //---------------------------------------------------------------//

    //GUI界面
    @Override
    public void start(Stage primaryStage) {
        // 创建 ObservableList，并添加一些项目
        ObservableList<String> items = FXCollections.observableArrayList(
                "《新华成语》",
                "《新时代汉俄》",
                "《牛津高阶》",
                "《日语词典》",
                "《德语词典》",
                "《法语词典》",
                "《西班牙语词典》",
                "《乌尔都语词典》",
                "《希伯来语词典》"
        );
        // 创建 ListView，并设置 items 属性
        ListView<String> listView = new ListView<>();
        listView.setStyle("-fx-font-size: 20px;");
        listView.setItems(items);

        //左侧区域添加蓝色标签分组
        Label blueLable = new Label("文件类型");
        blueLable.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 25px");
        // 创建 VBox，并将 ListView 添加到 VBox 中
        VBox vBox = new VBox(blueLable, listView);
        vBox.setAlignment(Pos.CENTER); // 将 VBox 中的组件居中对齐
        vBox.setStyle("-fx-background-color: #5873fe;");
        VBox.setVgrow(listView, Priority.ALWAYS); // 将 ListView 填充整个 VBox 区域

        // 创建 BorderPane，并将 VBox 设置为左侧区域
        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(vBox);
        // 创建嵌套的 BorderPane，并设置背景颜色
        BorderPane nestedBorderPane = new BorderPane();
        nestedBorderPane.setStyle("-fx-background-color: white;");

        //创建嵌套的BorderPane中的button标签
        Button button = new Button("Fbd To Xml Converter");
        button.setStyle("-fx-background-color: white; -fx-background-insets: 0; -fx-padding: 0;-fx-font-size: 30px");
        // 创建图像对象并设置大小
        ImageView imageView = new ImageView("D:\\DAppSapce\\Code\\JavaProject\\XmlProject\\FbdToXml\\src\\main\\resources\\Logo.png"); // 替换为实际的图像路径
        imageView.setFitWidth(100); // 设置宽度
        imageView.setFitHeight(100); // 设置高度
        button.setGraphic(imageView);

        //创建废话图片
        ImageView imageViewTopDown = new ImageView("D:\\DAppSapce\\Code\\JavaProject\\XmlProject\\FbdToXml\\src\\main\\resources\\img.png"); // 替换为实际的图像路径
        imageViewTopDown.setFitWidth(750); // 设置宽度
        imageViewTopDown.setFitHeight(130); // 设置高度


        //在弄一个vBox布局，将button放入居中,再将vbox放入BorderPane的setTop中
        VBox vBox1=new VBox(button,imageViewTopDown);
        vBox1.setSpacing(20); // 设置子节点之间的间距为20像素
        vBox1.setPadding(new Insets(20)); // 设置内边距
        vBox1.setAlignment(Pos.CENTER);
        nestedBorderPane.setTop(vBox1);
        //添加了一个不显示的文本框
        nestedBorderPane.setCenter(fbdTextArea);

        // 将嵌套的 BorderPane 设置为中心区域
        Button button1=new Button("点击选择FBD文件");
        button1.setOnAction(e ->{
            openFile(primaryStage);
            saveFile(primaryStage);
        });
        button1.setStyle(
                "-fx-background-color: #5873fe;" + // 设置背景颜色为蓝色
                        "-fx-text-fill: white;" + // 设置文字颜色为白色
                        "-fx-font-size: 24px;" + // 设置字体大小为24px
                        "-fx-pref-width: 500px;" + // 设置按钮的宽度
                        "-fx-pref-height: 80px;" + // 设置按钮的高度
                        "-fx-background-radius: 20px;" // 设置按钮的圆角半径
        );
        Label labelCenterDown1 =new Label("1、选择文件---->2、点击【开始转换】---->3、转换成功");
        Label labelCenterDown2 =new Label("2、支持.fbd、.txt、.xml格式; 最大支持300M");
        labelCenterDown1.setStyle(
                "-fx-text-fill: #979295;" + // 设置字体颜色为蓝色
                        "-fx-font-size: 18px;" // 设置字体大小为24px
        );
        labelCenterDown2.setStyle(
                "-fx-text-fill: #979295;" + // 设置字体颜色为蓝色
                        "-fx-font-size: 18px;" // 设置字体大小为24px
        );

        //在弄一个VBox，将新vbox放入nestedBorderPane再将nestedBorderPane放入PaneborderPane的center中。f1f3fe
        VBox vBox2=new VBox();
        vBox2.setSpacing(20); // 设置子节点之间的间距为20像素
        vBox2.setAlignment(Pos.CENTER);
        vBox2.setStyle("-fx-background-color: #f1f3fe;");
        vBox2.getChildren().add(labelCenterDown1);
        vBox2.getChildren().add(button1);
        vBox2.getChildren().add(labelCenterDown2);
        nestedBorderPane.setCenter(vBox2);
        borderPane.setCenter(nestedBorderPane);

        // 创建场景，并将 BorderPane 添加到场景中
        Scene scene = new Scene(borderPane, 1100, 800);

        // 设置舞台的标题和场景
        primaryStage.setTitle("Fbd To Xml Converter");
        primaryStage.setScene(scene);

        //添加logo
        primaryStage.getIcons().add(new Image("D:\\DAppSapce\\Code\\JavaProject\\XmlProject\\FbdToXml\\src\\main\\resources\\Logo.png"));
        // 显示舞台
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}