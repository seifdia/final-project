package com.example.demo1;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Order {

    private ArrayList<Restaurant> restaurants = Restaurant.initializeRestaurants();
    private ComboBox<String> categoryComboBox = new ComboBox<>();
    private ComboBox<String> restaurantComboBox = new ComboBox<>();
    private ComboBox<String> menuComboBox = new ComboBox<>();
    private ListView<String> orderListView = new ListView<>();
    private Label totalPriceLabel = new Label("Total Price: 0 EGP");


    private List<Food> selectedItems = new ArrayList<>();
    private double totalPrice = 0.0;
    public Order( List<Food> selectedItems, double totalPrice) {
        this.selectedItems = selectedItems;
        this.totalPrice = totalPrice;
    }
    public Order() {}



    private int getLastOrderId() {
        int lastOrderId = 0;
        File orderIdFile = new File("order_id.txt");

        if (orderIdFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(orderIdFile))) {
                String line = reader.readLine();
                if (line != null) {
                    lastOrderId = Integer.parseInt(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading order ID file: " + e.getMessage());
            }
        }

        return lastOrderId;
    }


    private void saveNewOrderId(int newOrderId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("order_id.txt"))) {
            writer.write(String.valueOf(newOrderId));
        } catch (IOException e) {
            System.out.println("Error writing new order ID: " + e.getMessage());
        }
    }


    private void saveOrderToFile(int orderId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("order.txt", true))) {

            writer.write("Order ID: " + orderId + "\n");
            writer.write("Order Details:\n");
            for (Food item : selectedItems) {
                writer.write(item.getName() + " - " + item.getPrice() + " EGP\n");
            }
            writer.write("\nTotal Price: " + totalPrice + " EGP\n");
            writer.write("--------------------------\n");
            writer.write("Order Created at: " + LocalDateTime.now() + "\n");
            writer.write("==========================\n\n");

            System.out.println("Order saved to order.txt successfully.");
        } catch (IOException e) {
            showAlert("Error", "Failed to save the order to file.");
        }
    }
    private void saveAssignedOrderToFile(DeliveryStaff staff, Order order,int newOrderId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(staff.getFirstName() + "_orders.txt", true))) {
            writer.write("Order ID: " + newOrderId + "\n");
            writer.write("Order Details:\n");
            for (Food item : order.selectedItems) {
                writer.write(item.getName() + " - " + item.getPrice() + " EGP\n");
            }
            writer.write("\nTotal Price: " + order.totalPrice + " EGP\n");
            writer.write("Assigned Time: " + LocalDateTime.now() + "\n");
            writer.write("--------------------------\n");
        } catch (IOException e) {
            System.err.println("Error writing assigned order to file: " + e.getMessage());
        }
    }


    private void createOrder(ArrayList<DeliveryStaff> staffList) {
        if (selectedItems.isEmpty()) {
            showAlert("No items selected", "Please select items to create an order.");
        } else {

            int lastOrderId = getLastOrderId();
            int newOrderId = lastOrderId + 1;
            Order current = new Order(selectedItems,totalPrice);

            saveNewOrderId(newOrderId);


            saveOrderToFile(newOrderId);


            showAlert("Order Created", "Your order has been created and saved with Order ID: " + newOrderId);



            String selectedRestaurantName = restaurantComboBox.getValue();
            if (selectedRestaurantName != null) {
                Restaurant selectedRestaurant = restaurants.stream()
                        .filter(r -> r.getName().equals(selectedRestaurantName))
                        .findFirst()
                        .orElse(null);
                String restaurantName = selectedRestaurantName;
                ArrayList<Restaurant> restaurants = Restaurant.initializeRestaurants();
                Restaurant matchingRestaurant = restaurants.stream()
                        .filter(r -> r.getName().equalsIgnoreCase(selectedRestaurantName))
                        .findFirst()
                        .orElse(null);
                if (matchingRestaurant == null) {
                    showAlert("Invalid Restaurant", "No restaurant found with the name: " + selectedRestaurantName);
                    return;
                }

                String restaurantLocation = matchingRestaurant.getLocation();
                DeliveryStaff staff = staffList.stream()
                        .filter(s -> s.getLocation().equalsIgnoreCase(restaurantLocation) && s.getStatus() == DeliveryStaff.Status.FREE)
                        .findFirst()
                        .orElse(null);
                if (staff != null) {
                    staff.setStatus(DeliveryStaff.Status.WORK); // Update status
                    saveAssignedOrderToFile(staff, current,newOrderId);
                    System.out.println("Assigned Orders for " + staff.getFirstName() + " saved to file.");
                } else {
                    System.out.println("No available staff found for location: " + restaurantLocation);
                }
            }
            selectedItems.clear();
            orderListView.getItems().clear();
            totalPrice = 0.0;
            totalPriceLabel.setText("Total Price: 0 EGP");
        }
    }
    public String toString() {
        return "Order ID: " + this.hashCode() + " | Total Price: " + totalPrice + " EGP";
    }

    private void removeItemFromOrder() {
        String selectedItem = orderListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String[] parts = selectedItem.split(" - ");
            String itemName = parts[0];
            double itemPrice = Double.parseDouble(parts[1].replace(" EGP", ""));
            selectedItems.removeIf(item -> item.getName().equals(itemName));
            orderListView.getItems().remove(selectedItem);
            totalPrice -= itemPrice;
            totalPriceLabel.setText("Total Price: " + totalPrice + " EGP");
        } else {
            showAlert("No item selected", "Please select an item to remove.");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void loadRestaurantReviewPage() {
        String selectedRestaurantName = restaurantComboBox.getValue();
        if (selectedRestaurantName != null) {
            // Find the selected restaurant object
            Restaurant selectedRestaurant = restaurants.stream()
                    .filter(r -> r.getName().equals(selectedRestaurantName))
                    .findFirst()
                    .orElse(null);
            String restaurantName = selectedRestaurantName;
            ArrayList<Restaurant> restaurants = Restaurant.initializeRestaurants();
            Restaurant matchingRestaurant = restaurants.stream()
                    .filter(r -> r.getName().equalsIgnoreCase(selectedRestaurantName))
                    .findFirst()
                    .orElse(null);
            if (matchingRestaurant == null) {
                showAlert("Invalid Restaurant", "No restaurant found with the name: " + selectedRestaurantName);
                return;
            }
            if (selectedRestaurant != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("RestaurantReview.fxml"));
                    Scene reviewScene = new Scene(loader.load(), 600, 600);


                    RestaurantReviewController controller = loader.getController();
                    controller.setRestaurant(selectedRestaurant,selectedRestaurantName);

                    Stage window = new Stage();
                    window.setScene(reviewScene);
                    window.setTitle("Restaurant Review");
                    window.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                showAlert("Restaurant not found", "The selected restaurant could not be found.");
            }
        } else {
            showAlert("No restaurant selected", "Please select a restaurant before adding a review.");
        }
    }

    public void start() {
        Stage primaryStage = new Stage();
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Pizza", "Fried Chicken", "Sea Food", "Dessert", "Burgers", "Café");

        categoryComboBox.setItems(categories);

        categoryComboBox.setOnAction(e -> updateRestaurantsByCategory());
        restaurantComboBox.setOnAction(e -> updateMenuByRestaurant());
        menuComboBox.setOnAction(e -> addToOrder());


        Button createOrderButton = new Button("Create Order");
        createOrderButton.setOnAction(e -> {
            DeliveryStaffController deliveryStaffController = new DeliveryStaffController();
            createOrder(deliveryStaffController.getStaffList());
        });



        Button removeItemButton = new Button("Remove Selected Item");
        removeItemButton.setOnAction(e -> removeItemFromOrder());


        Button addReviewButton = new Button("Add Review");
        addReviewButton.setOnAction(e -> loadRestaurantReviewPage());

        VBox categoryBox = new VBox(new Label("Select Category:"), categoryComboBox);
        categoryBox.setSpacing(10);
        categoryBox.setPadding(new Insets(10));

        VBox restaurantBox = new VBox(new Label("Select Restaurant:"), restaurantComboBox);
        restaurantBox.setSpacing(10);
        restaurantBox.setPadding(new Insets(10));

        VBox menuBox = new VBox(new Label("Select Menu Item:"), menuComboBox);
        menuBox.setSpacing(10);
        menuBox.setPadding(new Insets(10));


        VBox orderBox = new VBox(new Label("Your Order:"), orderListView, totalPriceLabel);
        orderBox.setSpacing(10);
        orderBox.setPadding(new Insets(10));


        HBox buttonBox = new HBox(createOrderButton, removeItemButton, addReviewButton);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        VBox mainLayout = new VBox(categoryBox, restaurantBox, menuBox, orderBox, buttonBox);
        mainLayout.setSpacing(20);
        mainLayout.setPadding(new Insets(20));

        Scene scene = new Scene(mainLayout, 500, 500);
        scene.getStylesheets().add("style.css");
        primaryStage.setTitle("Order Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateRestaurantsByCategory() {
        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null) {
            ObservableList<String> filteredRestaurants = FXCollections.observableArrayList(
                    restaurants.stream()
                            .filter(r -> r.getCategory().toLowerCase().contains(selectedCategory.toLowerCase()))
                            .map(Restaurant::getName)
                            .collect(Collectors.toList())
            );
            restaurantComboBox.setItems(filteredRestaurants);
            menuComboBox.getItems().clear();
        }
    }

    private void updateMenuByRestaurant() {
        String selectedRestaurantName = restaurantComboBox.getValue();
        if (selectedRestaurantName != null) {
            // Find the selected restaurant and get its menu
            Restaurant selectedRestaurant = restaurants.stream()
                    .filter(r -> r.getName().equals(selectedRestaurantName))
                    .findFirst()
                    .orElse(null);

            if (selectedRestaurant != null) {
                ObservableList<String> menuItems = FXCollections.observableArrayList(
                        selectedRestaurant.getMenu().stream()
                                .map(food -> food.getName() + " - " + food.getType() + " - " + food.getPrice() + " EGP")
                                .collect(Collectors.toList())
                );
                menuComboBox.setItems(menuItems);
            }
        }
    }

    private void addToOrder() {
        String selectedMenuItem = menuComboBox.getValue();
        if (selectedMenuItem != null) {
            String[] parts = selectedMenuItem.split(" - ");
            String itemName = parts[0];
            String itemType = parts[1];
            double itemPrice = Double.parseDouble(parts[2].replace(" EGP", ""));
            Food selectedFood = new Food(itemName, itemType, itemPrice);
            selectedItems.add(selectedFood);
            orderListView.getItems().add(itemName + " - " + itemPrice + " EGP");
            totalPrice += itemPrice;
            totalPriceLabel.setText("Total Price: " + totalPrice + " EGP");
        }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.start();
    }
}
