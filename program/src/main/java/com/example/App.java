package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.function.UnaryOperator;

/**
 * JavaFX App
 */
public class App extends Application {

    Scene scene;
    VBox generalContainer;
    HBox containerOfAllTables;
    VBox alternativesContainer;
    VBox expertsContainer;
    VBox criteriaContainer;
    Button continueToNextStep;
    Label expertsContaindeTitle;
    Label alternativesContainerTitle;
    Label criteriaContainerTitle;

    MatrixManager gradesOfExperts;
    Vector<Double> competentionsCoefitients;
    Vector<Double> criteriaGrades;
    Vector<Vector<Double>> allScaleOfGrades;

    Matrix generalGrades; 
    Vector<Double> AdditiveConvolution;


    public static void main(String[] args) {
        launch();
    }


    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
        
        continueToNextStep = new Button("Продолжить");    
        continueToNextStep.setOnAction(e -> handleOfFinishFirstStep());
        expertsContainer = new VBox();
        criteriaContainer = new VBox();
        alternativesContainer = new VBox();
        expertsContaindeTitle = new Label("Эксперты:");
        expertsContaindeTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        alternativesContainerTitle = new Label("Альтернативы:");
        alternativesContainerTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        criteriaContainerTitle = new Label("Критерии:");
        criteriaContainerTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        
        addMoreAction(expertsContainer, "Введите имя эксперта");
        addMoreAction(alternativesContainer, "Введите название альтернативы");
        addMoreAction(criteriaContainer, "Введите название критерия");

        competentionsCoefitients = new Vector<Double>();

        generalContainer = new VBox(continueToNextStep, expertsContaindeTitle, expertsContainer, 
                                    alternativesContainerTitle,
                                    alternativesContainer, criteriaContainerTitle, criteriaContainer);
        ScrollPane scrollPane = new ScrollPane(generalContainer);
        scrollPane.setFitToWidth(true);

        stylingGeneralContainer();
          
        Scene scene = new Scene(scrollPane);
        stage.setScene(scene);
        stage.setTitle("Метод групового многокритериального непосредственного оценивания");
        stage.setWidth(1920);
        stage.setHeight(1080);
        stage.setResizable(false);
        stage.show();
    }


    
    public void handleOfFinishFirstStep() {
        if(!validateContainer(alternativesContainer) || !validateContainer(criteriaContainer)){
            showErrorDialog("Сначала нужно заполнить все поля.");
            return;
        }

        expertsContaindeTitle.setText("Эксперты и коэффициенты их компетентности:");

        disableContainer(expertsContainer);
        disableContainer(alternativesContainer);
        disableContainer(criteriaContainer);

        addInputFieldsToContainer(expertsContainer, "Впишите коэффициент компетентности", 
                    "Нужно вписать коэффициент компетентности, в диапазоне от 0 до 1.");

        addInputFieldsToContainer(criteriaContainer, "Впишите вес критерия", 
                    "Нужно вписать вес критерия по используемой вами шкале.");

        addInputFieldsToContainer(criteriaContainer, "Минимальная оценка", 
                    "Нужно вписать минимальное значение оценки альтернативы по данному критерию, по используемой вами шкале.");
        
        addInputFieldsToContainer(criteriaContainer, "Максимальная оценка", 
                    "Нужно вписать максимальное значение оценки альтернативы по данному критерию, по используемой вами шкале.");

        criteriaContainerTitle.setText("Критерии.\t\tОценки самих критерев.\t\tШкалы оценивания альтернатив по критериям.\t\tВыбор типа критерия:");

        addCheckBoxesToContainer(criteriaContainer);


        continueToNextStep.setOnAction(null);
        continueToNextStep.setOnAction(e -> handleOfFinishSecondStep());
    }

    public void handleOfFinishSecondStep(){
        if(!validateWeights(expertsContainer) || !validateWeights(criteriaContainer)){
            return;
        }
        if(!validateCompetentions()){
            showErrorDialog("Сумма коэффициентов компетентностей не должна превышать 1");
            return;
        }

        allScaleOfGrades = new Vector<Vector<Double>>();
        criteriaGrades = new Vector<Double>();
        gradesOfExperts = new MatrixManager();

        containerOfAllTables = new HBox();

        disableContainer(expertsContainer);
        disableContainer(criteriaContainer);

        generalContainer.getChildren().add(containerOfAllTables);

        makeAllExpertsTables();

        continueToNextStep.setOnAction(null);
        continueToNextStep.setOnAction(e -> handleOfFinishThirtStep());

    }

    public void handleOfFinishThirtStep(){
        System.out.println();
        System.out.println();
        parseAllTables();
        for(int i = 0; i < gradesOfExperts.getCount(); i++){
            gradesOfExperts.getMatrix(i).print();
            System.out.println();
        }

        continueToNextStep.setOnAction(null);
        continueToNextStep.setOnAction(e -> handleOfFinishFourthStep());

        makeCriteriaGradesAndAllScaleOfGrades();

    }

    public void handleOfFinishFourthStep(){
        if(!validateConsistency()){
            gradesOfExperts.clear();
            criteriaGrades.clear();
            allScaleOfGrades.clear();
            handleOfFinishThirtStep();
            return;
        }

        makeNormaOfWeights();

        int countOfCriterials = criteriaGrades.size();
        int countOfAlternatives = alternativesContainer.getChildren().size();

        generalGrades = new Matrix(countOfAlternatives, countOfCriterials);
        makeGeneralGrades();
        AdditiveConvolution = new Vector<>(generalGrades.getRows());
        makeNormaOfGrades();
        makeAdditiveConvolutionToTable();

        continueToNextStep.setOnAction(null);
        generalContainer.getChildren().remove(0);
    }

    @SuppressWarnings("exports")
    public void handleOfChooseCheckBox(CheckBox selectedCheckBox, CheckBox otherCheckBox){
        if(selectedCheckBox.isSelected()){
            otherCheckBox.setSelected(false);
        }
        else{
            otherCheckBox.setSelected(true);
        }

    }


    public void addMoreAction(@SuppressWarnings("exports") VBox container, String promptText){

        TextField name = new TextField();
        name.setMinSize(300, 20);
        name.setPromptText(promptText);
        Button addMore = new Button("Добавить ещё");
        addMore.setOnAction(e -> addMoreAction(container, promptText));

        HBox newSmallContainer = new HBox(name, addMore);

        container.getChildren().add(newSmallContainer);

        int sizeOfContainer = container.getChildren().size();
        if((!container.getChildren().isEmpty()) && sizeOfContainer >= 2){
            ((HBox)container.getChildren().get(sizeOfContainer - 2)).getChildren().remove(1);
        }
    }

    public void addInputFieldsToContainer(@SuppressWarnings("exports") VBox container, String promptText, String tooltipText){
        int sizeOfContainer = container.getChildren().size();
        for(int i = 0; i < sizeOfContainer; i++){
            TextField weightInput = new TextField();
            setDoubleFormatForTextField(weightInput);
            weightInput.setPromptText(promptText);
            if(tooltipText != null){
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setShowDelay(javafx.util.Duration.millis(50));
                tooltip.setShowDelay(javafx.util.Duration.millis(100));
                Tooltip.install(weightInput, tooltip);
            }
            HBox box = (HBox)container.getChildren().get(i);
            box.getChildren().add(weightInput);
        }
    }

    public void addCheckBoxesToContainer(@SuppressWarnings("exports") VBox container){
        int sizeOfContainer = container.getChildren().size();
        for(int i = 0; i < sizeOfContainer; i++){
            HBox box = (HBox)container.getChildren().get(i);
            CheckBox checkBox1 = new CheckBox("максимизация");
            CheckBox checkBox2 = new CheckBox("минимизация");
            checkBox1.setOnAction(e -> handleOfChooseCheckBox(checkBox1, checkBox2));
            checkBox2.setOnAction(e -> handleOfChooseCheckBox(checkBox2, checkBox1));
            box.getChildren().addAll(checkBox1, checkBox2);
        }
    }



    public Matrix makeMatrixFromTable(@SuppressWarnings("exports") GridPane table){
        int columsInGrid = table.getColumnCount();
        int rowsInGrid = table.getRowCount();
        
        Matrix grades = new Matrix(rowsInGrid-1, columsInGrid-1);

        for(int i = 1; i < rowsInGrid; i++){
            for(int j = 1; j < columsInGrid; j++){
                Node node = getNodeFromGridPane(table, i, j);
                if(node instanceof TextField){
                    TextField field = (TextField) node;
                    if(!field.getText().isEmpty()){
                        grades.setValue(i-1, j-1, Double.parseDouble(field.getText()));
                    }
                    else{
                        grades.setValue(i-1, j-1, 0);
                        field.setText("0");
                    }
                }
            }
        }
        return grades;
    }

    public void makeCriteriaGradesAndAllScaleOfGrades(){
        int sizeOfCriteriaContainer = criteriaContainer.getChildren().size();
        for(int i = 0; i < sizeOfCriteriaContainer; i++){
            Node nodeBox = criteriaContainer.getChildren().get(i);
            if(nodeBox instanceof HBox){
                HBox box = (HBox) nodeBox;
                TextField criterialGrade = (TextField) box.getChildren().get(1);
                TextField minGrade = (TextField) box.getChildren().get(2);
                TextField maxGrade = (TextField) box.getChildren().get(3);

                criteriaGrades.add(Double.parseDouble(criterialGrade.getText()));
                Vector<Double> scale = new Vector<Double>();
                scale.add(Double.parseDouble(minGrade.getText()));
                scale.add(Double.parseDouble(maxGrade.getText()));
                allScaleOfGrades.add(scale);
            }
        }
    }

    private void parseAllTables(){
        int countOfTables = containerOfAllTables.getChildren().size();
        for(int i = 0; i < countOfTables; i++){
            Node node = containerOfAllTables.getChildren().get(i);
            if(node instanceof GridPane){
                GridPane table = (GridPane) node;
                gradesOfExperts.addMatrix(makeMatrixFromTable(table));
            }
        }
    }

    public Matrix makeGeneralGrades() {
        int countOfExperts = gradesOfExperts.getCount();
        int countOfCriterials = criteriaGrades.size();
        int countOfAlternatives = alternativesContainer.getChildren().size();
    
        for (int j = 0; j < countOfAlternatives; j++) {
            for (int i = 0; i < countOfCriterials; i++) {
                double result = 0.0;
                for (int k = 0; k < countOfExperts; k++) {
                    double expertGrade = gradesOfExperts.getMatrix(k).getValue(j, i);
                    double competenceCoefficient = competentionsCoefitients.get(k);
                    result += expertGrade * competenceCoefficient;
                    
                    System.out.printf("Expert %d: Grade = %.2f, Coefficient = %.2f\n", k, expertGrade, competenceCoefficient);
                }
                generalGrades.setValue(j, i, result);
                System.out.printf("General Grade for Alternative %d and Criterion %d: %.2f\n", j, i, result);
            }
        }
    
        System.out.println("Содержимое generalGrades:");
        generalGrades.print();

        return generalGrades;
    }
    

    public void makeNormaOfWeights(){
        int countOfCriterials = criteriaGrades.size();
        double sumOfCriterias = 0.0;
        
        for(int i = 0; i < countOfCriterials; i++){
            sumOfCriterias += criteriaGrades.get(i);
        }

        if(sumOfCriterias > 0){
            for(int i = 0; i < countOfCriterials; i++){
                criteriaGrades.set(i, criteriaGrades.get(i)/sumOfCriterias);
            }
        }
    }

    public boolean getCheckBoxValue(int Position){
        Node node = criteriaContainer.getChildren().get(Position);
        if(node instanceof HBox){
            HBox box = (HBox) node;
            Node nodeCheck = box.getChildren().get(box.getChildren().size()-1);
            if(nodeCheck instanceof CheckBox){
                if(((CheckBox) nodeCheck).isSelected()){
                    return false;
                }
                else{
                    return true;
                }
            }
        }
        return false;
    }

    public void makeNormaOfGrades() {

        if (generalGrades == null) {
            System.out.println("Ошибка: не инициализирован или пуст.");
            return;
        }
    
        int rows = generalGrades.getRows();
        int cols = generalGrades.getCols();
        
        Double max = generalGrades.getValue(0, 0);
        Double min = generalGrades.getValue(0, 0);
    
        System.out.println("Содержимое generalGrades перед нормализацией:");
        generalGrades.print();
    
        Matrix normal = new Matrix(rows, cols);
    
        for (int k = 0; k < rows; k++) {
            for (int j = 0; j < cols; j++) {
                Double value = generalGrades.getValue(k, j);
                System.out.println(value);
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }

        System.out.println();
        System.out.println();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (getCheckBoxValue(j)) {
                    normal.setValue(i, j, (generalGrades.getValue(i, j) - min) / (max - min));
                } else {
                    normal.setValue(i, j, (max - generalGrades.getValue(i, j)) / (max - min));
                }
                System.out.println(normal.getValue(i, j));
            }
        }
    
        generalGrades = normal;
    
        System.out.println("Содержимое generalGrades после нормализации:");
        generalGrades.print();
    }
    

    @SuppressWarnings("exports")
    public GridPane makeAdditiveConvolutionToTable() {
        int rows = generalGrades.getRows();
        int cols = generalGrades.getCols();
        
        AdditiveConvolution = new Vector<>(rows);
    
        for (int i = 0; i < rows; i++) {
            Double value = 0.0;
            for (int j = 0; j < cols; j++) {
                value += (Double) generalGrades.getValue(i, j) * (Double) criteriaGrades.get(j);
            }
            AdditiveConvolution.add(value);
        }
    
        System.out.println();
        System.out.println();
        for (int i = 0; i < AdditiveConvolution.size(); i++) {
            System.out.print(AdditiveConvolution.get(i));
            System.out.print(" ");
        }
    
        GridPane lastGrid = new GridPane();
        lastGrid.setPadding(new Insets(10));
        lastGrid.setVgap(5);
        lastGrid.setHgap(5);
    

        ArrayList<String> alternativeNames = new ArrayList<>();
        
        int countAlternatives = alternativesContainer.getChildren().size();
        for(int i = 0; i < countAlternatives; i++){
            Node nodeBox = alternativesContainer.getChildren().get(i);
            if(nodeBox instanceof HBox){
                HBox box = (HBox) nodeBox;
                Node fieldNode = box.getChildren().get(0);
                if(fieldNode instanceof TextField){
                    TextField field = (TextField) fieldNode;
                    alternativeNames.add(field.getText());
                }
            }
        }
    
        for (int i = 0; i < rows; i++) {
            Label alternativeLabel = new Label(alternativeNames.get(i)); 
            lastGrid.add(alternativeLabel, 0, i);
        }
    
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Double value = generalGrades.getValue(i, j);
                String formattedValue = String.format("%.2f", value);
                TextField textField = new TextField(formattedValue);
                textField.setEditable(false);
                lastGrid.add(textField, j + 1, i);
            }
        }
        
        for (int i = 0; i < rows; i++) {
            Double additiveValue = AdditiveConvolution.get(i);
            String formattedAdditiveValue = String.format("%.2f", additiveValue);
            TextField textField = new TextField(formattedAdditiveValue);
            textField.setEditable(false);
            lastGrid.add(textField, cols + 1, i);
        }
        
    
        double maxAdditiveValue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < AdditiveConvolution.size(); i++) {
            if (AdditiveConvolution.get(i) > maxAdditiveValue) {
                maxAdditiveValue = AdditiveConvolution.get(i);
            }
        }
    
        Label maxLabel = new Label("Максимальная альтернатива:");
        lastGrid.add(maxLabel, 0, rows + 1);
        
        int index = AdditiveConvolution.indexOf(maxAdditiveValue);
        
        TextField maxTextField = new TextField(alternativeNames.get(index));
        maxTextField.setEditable(false);
        maxTextField.setMinSize(350, 80);
        lastGrid.add(maxTextField, 1, rows + 1);
    
        generalContainer.getChildren().add(lastGrid);
        stylingGeneralContainer();
    
        return lastGrid;
    }
    
    


    public void makeAllExpertsTables(){
        int countOfExperts = expertsContainer.getChildren().size();
        for(int i = 0; i < countOfExperts; i++){
            GridPane tableOfGrades = new GridPane();
            String tableName = "";
            Node nodeBox = expertsContainer.getChildren().get(i);
            if(nodeBox instanceof HBox){
                HBox box = (HBox) nodeBox;
                Node nodeField = box.getChildren().get(0);
                if(nodeField instanceof TextField){
                    TextField field = (TextField) nodeField;
                    tableName += field.getText();
                }
            }
            TableOfGrades(tableOfGrades, alternativesContainer, tableName);
        }
    }

    public void GeneralGradesOnTable(@SuppressWarnings("exports") GridPane grid){
        int columsInGrid = grid.getColumnCount();
        int rowsInGrid = grid.getRowCount();
        grid.add(newStylishLabel("Общий балл"), columsInGrid, 0);
        for(int i = 1; i < rowsInGrid; i++){
            double grade = 0;
            for(int j = 1; j < columsInGrid; j++){
                Node node = getNodeFromGridPane(grid, i, j);
                if(node instanceof TextField){
                    Node nodeBox = criteriaContainer.getChildren().get(j - 1);
                    if(nodeBox instanceof HBox){
                        Node nodeField = ((HBox)nodeBox).getChildren().get(1);
                        if(nodeField instanceof TextField){
                            grade += (Double.parseDouble(((TextField)node).getText())) * (Double.parseDouble(((TextField)nodeField).getText()));
                        }
                    }

                }
            }
            grid.add(newStylishLabel(Double.toString(grade)), columsInGrid, i);
        }
    }

    public void TableOfGrades(@SuppressWarnings("exports") GridPane grid, @SuppressWarnings("exports") VBox box, String title){
        int sizeOfGoalsContainer = box.getChildren().size();
        int sizeOfCriteriaContainer = criteriaContainer.getChildren().size();

        grid.add(newStylishLabel(title), 0, 0);
        for(int i = 0; i < sizeOfGoalsContainer + 1; i++){
            for(int j = 0; j < sizeOfCriteriaContainer + 1; j++){
                if((i == 0) || (j == 0)){
                    String text = "";
                    if(i == 0 && j > 0){
                        HBox newHBox = (HBox)criteriaContainer.getChildren().get(j - 1);
                        TextField newTextField = (TextField)newHBox.getChildren().get(0);
                        text = newTextField.getText();
                    }
                    else if(j == 0 && i > 0){
                        HBox newHBox = (HBox)box.getChildren().get(i - 1);
                        TextField newTextField = (TextField)newHBox.getChildren().get(0);
                        text = newTextField.getText();
                    }
                    grid.add(newStylishLabel(text), j, i);
                    
                }
                else{
                    grid.add(StylishTextField(), j, i);
                }
            }
        }

        containerOfAllTables.getChildren().add(grid);
        Label emptyLabel = new Label("\t\t\t");
        containerOfAllTables.getChildren().add(emptyLabel);
        stylingGeneralContainer();
    }
    
    public void makeBestGrade(@SuppressWarnings("exports") GridPane grid) {
        int indexOfGeneralGrade = grid.getColumnCount() - 1;
        int rowsInGrid = grid.getRowCount();
        grid.add(newStylishLabel("Наиболее приоритетная:"), 0, rowsInGrid);
        
        Node node = getNodeFromGridPane(grid, 1, indexOfGeneralGrade);
        double maxWeight = Double.NEGATIVE_INFINITY;
        Label maxWeightLabel = new Label();
        int maxWeightRowIndex = -1;
    
        if (node instanceof Label) {
            Label label = (Label) node;
            try {
                maxWeight = Double.parseDouble(label.getText());
                maxWeightLabel.setText(label.getText());
                maxWeightRowIndex = 1;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка преобразования числа: " + e.getMessage());
                return;
            }
        }
    
        for (int i = 2; i < rowsInGrid; i++) {
            node = getNodeFromGridPane(grid, i, indexOfGeneralGrade);
            if (node instanceof Label) {
                Label label = (Label) node;
                try {
                    double weight;
                    if(label.getText() == null || label.getText().trim().isEmpty()){
                        weight = 0.0;
                    }
                    else{
                        weight = Double.parseDouble(label.getText());
                    }
                    if (weight > maxWeight) {
                        maxWeight = weight;
                        maxWeightLabel.setText(label.getText());
                        maxWeightRowIndex = i;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка преобразования числа: " + e.getMessage());
                }
            }
        }
    
        Node bestNode = getNodeFromGridPane(grid, maxWeightRowIndex, 0);
        String bestLabelText = "";
        
        if (bestNode instanceof Label) {
            bestLabelText = ((Label) bestNode).getText();
        }
    
        grid.add(newStylishLabel(bestLabelText), 1, rowsInGrid);
    }    

    
    
    public void showErrorDialog(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }



    public void stylingGeneralContainer(){
        int sizeOfContainer = generalContainer.getChildren().size();
        for(int i = 0; i < sizeOfContainer; i++){
            VBox.setMargin(generalContainer.getChildren().get(i), new Insets(10, 10, 10, 10));
        }
    }

    public void disableContainer(@SuppressWarnings("exports") VBox container) {
        if (!container.getChildren().isEmpty()) {
            HBox lastBox = (HBox) container.getChildren().get(container.getChildren().size() - 1);
            if (lastBox.getChildren().size() > 1) {
                TextField field = (TextField) lastBox.getChildren().get(0);
                if(!field.isDisabled()){
                    lastBox.getChildren().remove(1);
                }
            }
        }
    
        for (Node node : container.getChildren()) {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                if(box.getChildren().size() == 1){
                    Node firstChild = box.getChildren().isEmpty() ? null : box.getChildren().get(0);
                    if (firstChild instanceof TextField) {
                        ((TextField) firstChild).setDisable(true);
                    }
                }
                else{
                    box.setDisable(true);
                }
            }
        }
    }
    
    public void disableWeightes(){
        for (Node node : criteriaContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                Node secondChild = box.getChildren().isEmpty() ? null : box.getChildren().get(1);
                if (secondChild instanceof TextField) {
                    ((TextField) secondChild).setDisable(true);
                }
            }
        }
    }

    

    private Label newStylishLabel(String includedText){
        Label label = new Label(includedText);
        label.setMinSize(150, 80);
        label.setMaxSize(150, 80);
        label.setWrapText(true);
        label.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 5px;");
        return label;
    }

    private TextField StylishTextField(){
        TextField textField = new TextField();
        textField.setMinSize(150, 80);
        textField.setMaxSize(150, 80);
        textField.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 5px;");
        setDoubleFormatForTextField(textField);
        return textField;
    }

    private void setDoubleFormatForTextField(TextField field){
        // Фильтр для ввода только чисел типа double
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?\\d*(\\.\\d*)?")) { // Проверка на допустимый формат
                return change; // Если формат корректный, возвращаем изменение
            }
            return null; // В противном случае игнорируем изменение
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }
    
    

    private boolean validateWeights(VBox container) {
    
        for (Node node : container.getChildren()) {
            if (node instanceof HBox) {
                HBox box = (HBox) node;
                if (!box.getChildren().isEmpty()) {
                    for(Node elem : box.getChildren()){
                        if(elem instanceof TextField){
                            TextField field = (TextField) elem;
                            if(field.getText().isEmpty()){
                                showErrorDialog("Все поля должны быть заполнены.");
                                return false;
                            }
                        }
                    }
                }
            }
        }
    
        return true;
    }
    
    private boolean validateContainer(VBox container){
        int sizeOfContainer = container.getChildren().size();
        for(int i = 0; i < sizeOfContainer; i++){
            if((!container.getChildren().isEmpty()) && sizeOfContainer >= 1){
                Node node = container.getChildren().get(i);
                if(node instanceof HBox){
                    HBox box = (HBox) node;
                    Node nodeInBox = box.getChildren().get(0);
                    if(nodeInBox instanceof TextField){
                        TextField field = (TextField) nodeInBox;
                        if(field.getText().isEmpty()){
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean validateCompetentions(){
        int sizeOfExpertsContainer = expertsContainer.getChildren().size();
        double generalCompetention = 0;
        for(int i = 0; i < sizeOfExpertsContainer; i++){
            Node nodeBox = expertsContainer.getChildren().get(i);
            if(nodeBox instanceof HBox){
                HBox box = (HBox) nodeBox;
                Node nodeField = box.getChildren().get(1);
                if (nodeField instanceof TextField) {
                    TextField field = (TextField) nodeField;
                    double competention = Double.parseDouble(field.getText());
                    generalCompetention += competention;
                    competentionsCoefitients.add(competention);
                    if(generalCompetention > 1){
                        competentionsCoefitients.clear();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean validateConsistency(){
        int countOfCriterials = criteriaGrades.size();
        for(int j = 0; j < countOfCriterials; j++){
            if(!validateConsistencyForOneCriterial(j)){
                String errorMessage = "Требуется провести переоценку альтернатив по критерию №" + (j+1) + ".";
                errorMessage += "\n";
                errorMessage += "K = 1414.21%";
                showErrorDialog(errorMessage);
                return false;
            }
        }
        return true;
    }

    private boolean validateConsistencyForOneCriterial(int numberOfCriterial){
        int countOfAlternatives = alternativesContainer.getChildren().size();
        for(int k = 0; k < countOfAlternatives; k++){
            double middleOfChoosingValue = middleOfChoosing(gradesOfExperts, k, numberOfCriterial);

            if (middleOfChoosingValue == 0.0) {
                System.out.println("Middle of choosing value is zero for alternative " + k);
                return false;
            }    

            double dispersionValue = dispersion(gradesOfExperts, k, numberOfCriterial, middleOfChoosingValue);
            double variationCoeficient = (Math.sqrt(dispersionValue)/middleOfChoosingValue)*100.0;

            System.out.println("variationCoeficient: ");
            System.out.print(variationCoeficient);
            System.out.println();
            System.out.println();
            
            if((Double)variationCoeficient >= (Double)33.0){
                return false;
            }

        }
        return true;
    }



    private double middleOfChoosing(MatrixManager Experts, int rowIndex, int columnIndex){
        double result = 0.0;
        int countOfExperts = Experts.getCount();
        for(int h = 0; h < countOfExperts; h++){
            result += Experts.getMatrix(h).getValue(rowIndex, columnIndex);
        }
        result /= countOfExperts;
        System.out.println("middleOfChoosing result: ");
        System.out.print(result);
        System.out.println();
        return result;
    }

    private double dispersion(MatrixManager Experts, int rowIndex, int columnIndex, double middleOfChoosingValue){
        double result = 0.0;
        int countOfExperts = Experts.getCount();
        for(int h = 0; h < countOfExperts; h++){
            result += Math.pow((Experts.getMatrix(h).getValue(rowIndex, columnIndex) - middleOfChoosingValue), 2);
        }
        result /= countOfExperts;
        System.out.println("dispersion result: ");
        System.out.print(result);
        System.out.println();
        return result;
    }
    


    private Node getNodeFromGridPane(GridPane gridPane, int rowIndex, int columnIndex) {
        for (Node node : gridPane.getChildren()) {
            Integer row = GridPane.getRowIndex(node);
            Integer column = GridPane.getColumnIndex(node);

            if ((row == null ? 0 : row) == rowIndex && (column == null ? 0 : column) == columnIndex) {
                return node;
            }
        }
        return null;
    }
    
}