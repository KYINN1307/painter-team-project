package com.example.painter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.File;



public class HelloController implements Initializable {

    @FXML
    private AnchorPane mainMenu;
    @FXML
    private AnchorPane shapeMenu;
    @FXML
    private AnchorPane lineMenu;
    @FXML
    private AnchorPane helpMenu;
    @FXML
    private Canvas drawingCanvas;
    private double startX, startY, endX, endY;
    private GraphicsContext gc;
    private List<Shape> shapes = new ArrayList<>();
    private Shape currentShape;
    private boolean selectMode;
    private Shape copyShape = null;
    private boolean isReadyToCopy = false;
    @FXML
    private TextField lineWeightTextField;
    @FXML
    private ColorPicker colorPicker;


    private Shape getSelectedShape() {
        for (Shape shape : shapes) {
            if (shape.isSelected()) {
                return shape;
            }
        }
        return null;
    }

    private void drawSelectionRectangle(double x, double y) {
        double size = 5;
        gc.clearRect(x - size / 2, y - size / 2, size, size);
        gc.setFill(Color.BLACK);
        gc.fillRect(x - size / 2, y - size / 2, size, size);
    }

    public void initialize(URL url, ResourceBundle rb) {
        gc = drawingCanvas.getGraphicsContext2D();
        drawingCanvas.setOnMouseReleased(this::mouseReleased);
    }

    @FXML
    private void goToMain(MouseEvent event) {
        mainMenu.toFront();
    }

    @FXML
    private void goToMainWindow(MouseEvent event) {helpMenu.setVisible(false);}

    @FXML
    private void goToLine(MouseEvent event) {

        lineMenu.toFront();
        startX = event.getX();
        startY = event.getY();
        endX = event.getX();
        endY = event.getY();
        currentShape = new Line();
    }

    @FXML
    private void goToShape(MouseEvent event) {
        shapeMenu.toFront();
    }



    @FXML
    private void drawCircle(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        endX = event.getX();
        endY = event.getY();
        currentShape = new Circle();
        setShapeColor(currentShape);

    }

    @FXML
    private void drawTriangle(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        endX = event.getX();
        endY = event.getY();
        currentShape = new Triangle();
        setShapeColor(currentShape);

    }

    @FXML
    private void drawRectangle(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        endX = event.getX();
        endY = event.getY();
        currentShape = new Rectangle();
        setShapeColor(currentShape);

    }

    @FXML
    private void mousePressed(MouseEvent event) {
        if (this.currentShape != null) {
            this.currentShape.startX = event.getX();
            this.currentShape.startY = event.getY();
            this.currentShape.endX = event.getX();
            this.currentShape.endY = event.getY();

            if (lineWeightTextField.getText() != null && !lineWeightTextField.getText().isEmpty()) {
                double lineWidth = Double.parseDouble(lineWeightTextField.getText());
                ((Line) currentShape).setLineWidth(lineWidth);
            }
        }
        double offsetX;
        double offsetY;

        if (this.selectMode) {
            offsetX= event.getX();
            offsetY = event.getY();
            Shape clickedShape = null;
            for (int i = this.shapes.size() - 1; i >= 0; i--) {
                Shape shape = this.shapes.get(i);
                if (shape.isClicked(offsetX, offsetY)) {
                    clickedShape = shape;
                    break;
                }
            }

            // 모든 도형의 선택 상태를 해제
            for (Shape shape : this.shapes) {
                shape.setSelected(false);
            }

            // 클릭된 도형이 있다면 그 도형만 선택 상태로 만듬
            if (clickedShape != null) {
                clickedShape.setSelected(true);
            }

            selectMode = false; // 도형 선택 후 혹은 도형이 없는 곳을 클릭한 후 selectMode를 false로 설정
            this.redraw();
        } else if (this.currentShape != null) {
            this.currentShape.startX = event.getX();
            this.currentShape.startY = event.getY();
        }

        if (this.isReadyToCopy && this.copyShape != null) {
            offsetX = event.getX() - this.copyShape.startX;
            offsetY = event.getY() - this.copyShape.startY;
            Shape var10000 = this.copyShape;
            var10000.startX += offsetX;
            var10000 = this.copyShape;
            var10000.startY += offsetY;
            var10000 = this.copyShape;
            var10000.endX += offsetX;
            var10000 = this.copyShape;
            var10000.endY += offsetY;
            this.shapes.add(this.copyShape);
            this.copyShape = null;
            this.isReadyToCopy = false;
            this.redraw();
        }
    }

    @FXML
    private void mouseDragged(MouseEvent event) {
        if (currentShape != null) {
            currentShape.endX = event.getX();
            currentShape.endY = event.getY();
            setShapeColor(currentShape);
            redraw();
        }
    }

    @FXML
    private void mouseReleased(MouseEvent event) {
        if (currentShape != null) {
            Command command = new DrawCommand(currentShape, shapes);
            command.execute();
            history.push(command);

            currentShape = null;
            updateShapesListView();
            redraw();
        }
    }

    private void redraw() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        for (Shape shape : shapes) {
            shape.draw(gc);
            if (shape.isSelected()) {
                if(shape instanceof Circle) {
                    Circle circle = (Circle) shape;
                    double radius = Math.sqrt(Math.pow(circle.endX - circle.startX, 2) + Math.pow(circle.endY - circle.startY, 2));
                    drawSelectionRectangle(circle.startX - radius, circle.startY - radius); // 왼쪽 상단
                    drawSelectionRectangle(circle.startX + radius, circle.startY - radius); // 오른쪽 상단
                    drawSelectionRectangle(circle.startX - radius, circle.startY + radius); // 왼쪽 하단
                    drawSelectionRectangle(circle.startX + radius, circle.startY + radius); // 오른쪽 하단
                } else {
                    drawSelectionRectangle(shape.startX, shape.startY);
                    drawSelectionRectangle(shape.startX, shape.endY);
                    drawSelectionRectangle(shape.endX, shape.startY);
                    drawSelectionRectangle(shape.endX, shape.endY);
                }
            }
        }
        if (currentShape != null) {
            currentShape.draw(gc);
        }
    }

    // Inner Shape classes
    class Circle extends Shape {
        private Color innerColor; // Default inner color
        Circle(){
            this.innerColor = Color.RED;
        }
        void setInnerColor(Color color) {
            innerColor = color;
        }
        @Override
        void draw(GraphicsContext gc) {
            double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
            gc.setFill(innerColor);
            gc.fillOval(startX - radius, startY - radius, 2 * radius, 2 * radius);
        }
        @Override
        boolean isClicked(double x, double y) {
            double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
            double distance = Math.sqrt(Math.pow(x - startX, 2) + Math.pow(y - startY, 2));
            return distance <= radius;
        }
    }

    class Rectangle extends Shape {
        private Color innerColor; // Default inner color

        Rectangle(){
            this.innerColor = Color.BLUE;
        }
        void setInnerColor(Color color) {
            innerColor = color;
        }
        @Override
        void draw(GraphicsContext gc) {
            gc.setFill(innerColor);
            if (endX > startX & endY > startY) gc.fillRect(startX, startY, endX - startX, endY - startY);
            else if (endX > startX & endY < startY) gc.fillRect(startX, endY, endX - startX, startY - endY);
            else if (endX < startX & endY > startY) gc.fillRect(endX, startY, startX - endX, endY - startY);
            else gc.fillRect(endX, endY, startX - endX, startY - endY);
        }
        @Override
        boolean isClicked(double x, double y) {
            return (x >= Math.min(startX, endX) && x <= Math.max(startX, endX) &&
                    y >= Math.min(startY, endY) && y <= Math.max(startY, endY));
        }
    }

    class Triangle extends Shape {
        private Color innerColor; // Default inner color

        Triangle(){
            this.innerColor = Color.GREEN;
        }
        void setInnerColor(Color color) {
            innerColor = color;
        }
        @Override
        void draw(GraphicsContext gc) {
            gc.setFill(innerColor);
            gc.fillPolygon(new double[]{startX, endX, (startX + endX) / 2},
                    new double[]{endY, endY, startY}, 3);
        }
        @Override
        boolean isClicked(double x, double y) {
            // 사각형과 동일한 클릭 판단 로직? 삼각형이랑 구분 할건지
            return (x >= Math.min(startX, endX) && x <= Math.max(startX, endX) &&
                    y >= Math.min(startY, endY) && y <= Math.max(startY, endY));
        }
    }


    class Line extends Shape{
        private double lineWidth = 1.0; // Default line width

        void setLineWidth(double width) {
            lineWidth = width;
        }
        void draw(GraphicsContext gc){
            gc.setLineWidth(lineWidth);
            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.lineTo(endX, endY);
            gc.stroke();
            gc.closePath();
            updateShapesListView();
        }
    }

    abstract class Shape {
        double startX, startY, endX, endY;
        boolean selected;
        abstract void draw(GraphicsContext gc);
        boolean isClicked(double x, double y) {
            return (x >= Math.min(startX, endX) && x <= Math.max(startX, endX) &&
                    y >= Math.min(startY, endY) && y <= Math.max(startY, endY));
        }

        // 선택 상태를 설정하는 메소드
        void setSelected(boolean selected) {
            this.selected = selected;
        }

        // 선택 상태를 반환하는 메소드
        boolean isSelected() {
            return selected;
        }
    }
    @FXML
    private void adjustLineWeight() {
        String input = lineWeightTextField.getText();
        double lineWidth = Double.parseDouble(input);
        gc.setLineWidth(lineWidth);
        redraw();
    }


    @FXML
    private void select(MouseEvent event) {
        selectMode = true; // 'select' 버튼을 눌렀을 때 selectMode를 true로 설정
    }

    @FXML
    private void copy(MouseEvent event) {
        Shape selectedShape = getSelectedShape();
        if (selectedShape != null && !isReadyToCopy) {
            isReadyToCopy = true;
            if(selectedShape instanceof Circle) {
                copyShape = new Circle();
            } else if(selectedShape instanceof Rectangle) {
                copyShape = new Rectangle();
            } else if(selectedShape instanceof Triangle) {
                copyShape = new Triangle();
            } else if(selectedShape instanceof Line){
                copyShape = new Line();
            }
            if(copyShape != null) {
                copyShape.startX = selectedShape.startX;
                copyShape.startY = selectedShape.startY;
                copyShape.endX = selectedShape.endX;
                copyShape.endY = selectedShape.endY;

                if (selectedShape instanceof Circle && copyShape instanceof Circle) {
                    ((Circle) copyShape).setInnerColor(((Circle) selectedShape).innerColor);
                } else if (selectedShape instanceof Rectangle && copyShape instanceof Rectangle) {
                    ((Rectangle) copyShape).setInnerColor(((Rectangle) selectedShape).innerColor);
                } else if (selectedShape instanceof Triangle && copyShape instanceof Triangle) {
                    ((Triangle) copyShape).setInnerColor(((Triangle) selectedShape).innerColor);
                } else if (selectedShape instanceof  Line && copyShape instanceof Line) {
                    ((Line) copyShape).setLineWidth(((Line)selectedShape).lineWidth);
                }
            }
            Command copyCommand = new CopyCommand(copyShape, shapes);
            history.push(copyCommand);
        }
    }

    private void setShapeColor(Shape shape){
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            circle.setInnerColor(colorPicker.getValue());
        } else if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            rectangle.setInnerColor(colorPicker.getValue());
        } else if (shape instanceof Triangle) {
            Triangle triangle = (Triangle) shape;
            triangle.setInnerColor(colorPicker.getValue());
        }
    }

    @FXML
    private void delete(MouseEvent event) {
        Shape selectedShape = getSelectedShape();
        if (selectedShape != null) {
            Command command = new DeleteCommand(selectedShape, shapes);
            command.execute();
            history.push(command);
            updateShapesListView();
            redraw();
        }
    }
    @FXML
    private void borderColorChange(MouseEvent event){

    }
    @FXML
    private Text colorCode;
    @FXML
    private javafx.scene.shape.Rectangle innerColorBox;
    @FXML
    private void innerColorChange(MouseEvent event) {
        if (colorPicker == null) {
            colorPicker = new ColorPicker(); // ColorPicker 객체가 생성되어 있지 않은 경우에만 새로운 객체 생성
        }

        colorPicker.setOnAction(e -> {
            Color selectedColor = colorPicker.getValue();
            String hexColor = String.format( "#%02X%02X%02X",
                    (int)( selectedColor.getRed() * 255 ),
                    (int)( selectedColor.getGreen() * 255 ),
                    (int)( selectedColor.getBlue() * 255 ) );
            colorCode.setText(hexColor);
            innerColorBox.setFill(Color.web(hexColor));
            Shape selectedShape = getSelectedShape();
            Color oldColor = null;
            if (selectedShape != null) {
                if (selectedShape instanceof Circle) {
                    oldColor = ((Circle) selectedShape).innerColor;
                    ((Circle) selectedShape).setInnerColor(selectedColor);
                } else if (selectedShape instanceof Rectangle) {
                    oldColor = ((Rectangle) selectedShape).innerColor;
                    ((Rectangle) selectedShape).setInnerColor(selectedColor);
                } else if (selectedShape instanceof Triangle) {
                    oldColor = ((Triangle) selectedShape).innerColor;
                    ((Triangle) selectedShape).setInnerColor(selectedColor);
                }
                Command command = new colorChangeCommand(selectedShape, oldColor, selectedColor);
                command.execute();
                history.push(command);
                redraw();
            }
        });

        colorPicker.show();
    }

    @FXML
    private void changeLineColor(MouseEvent event){

    }

    @FXML
    private ListView<String> shapesListView;
    private void updateShapesListView() {
        List<String> shapeNames = shapes.stream()
                .map(shape -> shape.getClass().getSimpleName())
                .collect(Collectors.toList());
        Collections.reverse(shapeNames);
        shapesListView.getItems().setAll(shapeNames);
    }


    @FXML
    private void moveToFront() {
        int shapeIndex;
        Shape selectedShape = getSelectedShape();
        shapeIndex = shapes.indexOf(selectedShape);
        if (selectedShape != null && shapeIndex != (shapes.size() - 1)) {
            shapes.remove(shapes.indexOf(selectedShape));
            shapes.add(shapeIndex + 1, selectedShape);
            redraw();
            Command command = new ForwardCommand(selectedShape, shapes);
            history.push(command);
        }
        updateShapesListView();
    }

    @FXML
    private void moveToBack() {
        Shape selectedShape = getSelectedShape();
        int shapeIndex = shapes.indexOf(selectedShape);
        if (selectedShape != null && shapeIndex != 0) {
            shapes.remove(selectedShape);
            shapes.add(shapeIndex - 1, selectedShape);
            redraw();
            Command command = new BackwordCommand(selectedShape, shapes);
            history.push(command);
        }
        updateShapesListView();
    }

    //Top MenuBar
    @FXML
    private void save(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        // 파일 확장자 필터 설정
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        // 파일 저장 다이얼로그 보기
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                // 캔버스에 그려진 내용을 WritableImage 객체로 가져옴
                WritableImage writableImage = new WritableImage((int)drawingCanvas.getWidth(), (int)drawingCanvas.getHeight());
                drawingCanvas.snapshot(null, writableImage);
                // WritableImage 객체를 파일로 저장
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    @FXML
    private void undo(MouseEvent event) {
        if (!history.undoHistory.isEmpty()) {
            Command command = history.undo();
            if (command != null) {
                command.undo();
                updateShapesListView();
                redraw();
            }
        }
    }
    @FXML
    private void redo(MouseEvent event) {
        if (!history.redoHistory.isEmpty()) {
            Command command = history.redo();
            if (command != null) {
                command.execute();
                updateShapesListView();
                redraw();
            }
        }
    }
    @FXML
    private void help(MouseEvent event){
        helpMenu.setVisible(true);
    }
    interface Command {
        void execute();
        void undo();
    }

    class DrawCommand implements Command {
        private Shape shape;
        private List<Shape> shapes;
        DrawCommand(Shape shape, List<Shape> shapes) {
            this.shape = shape;
            this.shapes = shapes;
        }
        @Override
        public void execute() {
            shapes.add(shape);
        }
        @Override
        public void undo() { shapes.remove(shape); }

    }

    class CopyCommand implements Command {
        private Shape shape;
        private List<Shape> shapes;

        CopyCommand(Shape shape, List<Shape> shapes) {
            this.shape = shape;
            this.shapes = shapes;
        }

        @Override
        public void execute() {
            shapes.add(shape);
        }

        @Override
        public void undo() {
            shapes.remove(shape);
        }
    }

    class DeleteCommand implements Command {
        private Shape shape;
        private List<Shape> shapes;
        private int index;

        DeleteCommand(Shape shape, List<Shape> shapes) {
            this.shape = shape;
            this.shapes = shapes;
            this.index = shapes.indexOf(shape);
        }

        @Override
        public void execute() { shapes.remove(shape); }

        @Override
        public void undo() { shapes.add(index, shape); }
    }
    class ForwardCommand implements Command {
        private Shape shape;
        private List<Shape> shapes;
        private int index;

        ForwardCommand(Shape shape, List<Shape> shapes) {
            this.shape = shape;
            this.shapes = shapes;
            this.index = shapes.indexOf(shape);
        }
        @Override
        public void execute() {
            shapes.remove(shape);
            shapes.add(index, shape);
        }
        @Override
        public void undo() {
            shapes.remove(shape);
            if (index == 0) shapes.add(index, shape);
            else shapes.add(index - 1, shape);
        }
    }

    class BackwordCommand implements Command {
        private Shape shape;
        private List<Shape> shapes;
        private int index;

        BackwordCommand(Shape shape, List<Shape> shapes) {
            this.shape = shape;
            this.shapes = shapes;
            this.index = shapes.indexOf(shape);
        }
        @Override
        public void execute() {
            shapes.remove(shape);
            shapes.add(index, shape);
        }
        @Override
        public void undo() {
            shapes.remove(shape);
            if (index == shapes.size()) shapes.add(index, shape);
            else shapes.add(index + 1, shape);
        }
    }

    class colorChangeCommand implements Command {

        private Shape shape;
        private Color oldColor;
        private Color newColor;

        colorChangeCommand(Shape shape, Color oldColor, Color newColor) {
            this.shape = shape;
            this.oldColor = oldColor;
            this.newColor = newColor;
        }

        @Override
        public void execute() {
            if (shape instanceof Circle) {
                ((Circle) shape).setInnerColor(newColor);
            } else if (shape instanceof Rectangle) {
                ((Rectangle) shape).setInnerColor(newColor);
            } else if (shape instanceof Triangle) {
                ((Triangle) shape).setInnerColor(newColor);
            }
        }

        @Override
        public void undo() {
            if (shape instanceof Circle) {
                ((Circle) shape).setInnerColor(oldColor);
            } else if (shape instanceof Rectangle) {
                ((Rectangle) shape).setInnerColor(oldColor);
            } else if (shape instanceof Triangle) {
                ((Triangle) shape).setInnerColor(oldColor);
            }
        }


    }
    class CommandHistory {
        private Stack<Command> undoHistory = new Stack<>();
        private Stack<Command> redoHistory = new Stack<>();

        void push(Command command) {
            undoHistory.push(command);
            redoHistory.clear(); // 새로운 명령을 실행하면 redo 이력을 초기화합니다.
        }

        Command undo() {
            if (!undoHistory.isEmpty()) {
                Command command = undoHistory.pop();
                redoHistory.push(command); // 실행 취소한 명령을 redo 스택에 추가합니다.
                return command;
            }
            return null;
        }

        Command redo() {
            if (!redoHistory.isEmpty()) {
                Command command = redoHistory.pop();
                undoHistory.push(command); // 다시 실행한 명령을 undo 스택에 추가합니다.
                return command;
            }
            return null;
        }
    }
    private CommandHistory history = new CommandHistory();
}