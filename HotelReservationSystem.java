import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class HotelReservationSystem {

    // File paths for data storage (using simple text files)
    private static final String ROOMS_FILE = "rooms.txt";
    private static final String RESERVATIONS_FILE = "reservations.txt";

    // Date formatter for parsing and formatting dates
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Room class
    static class Room {
        private int roomId;
        private String category;
        private double price;
        private boolean available; // Simplified availability flag

        public Room(int roomId, String category, double price) {
            this.roomId = roomId;
            this.category = category;
            this.price = price;
            this.available = true;
        }

        // Constructor for loading from file
        public Room(int roomId, String category, double price, boolean available) {
            this.roomId = roomId;
            this.category = category;
            this.price = price;
            this.available = available;
        }

        // Getters and setters
        public int getRoomId() { return roomId; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }

        // Convert to string for file storage
        @Override
        public String toString() {
            return roomId + "," + category + "," + price + "," + available;
        }

        // Parse from string
        public static Room fromString(String line) {
            String[] parts = line.split(",");
            return new Room(Integer.parseInt(parts[0]), parts[1], Double.parseDouble(parts[2]), Boolean.parseBoolean(parts[3]));
        }
    }

    // Reservation class
    static class Reservation {
        private int reservationId;
        private String userName;
        private int roomId;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private double totalPrice;
        private String status; // 'confirmed' or 'cancelled'

        public Reservation(int reservationId, String userName, int roomId, LocalDate checkIn, LocalDate checkOut, double totalPrice) {
            this.reservationId = reservationId;
            this.userName = userName;
            this.roomId = roomId;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.totalPrice = totalPrice;
            this.status = "confirmed";
        }

        // Constructor for loading from file
        public Reservation(int reservationId, String userName, int roomId, LocalDate checkIn, LocalDate checkOut, double totalPrice, String status) {
            this.reservationId = reservationId;
            this.userName = userName;
            this.roomId = roomId;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.totalPrice = totalPrice;
            this.status = status;
        }

        // Getters and setters
        public int getReservationId() { return reservationId; }
        public String getUserName() { return userName; }
        public int getRoomId() { return roomId; }
        public LocalDate getCheckIn() { return checkIn; }
        public LocalDate getCheckOut() { return checkOut; }
        public double getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        // Convert to string for file storage
        @Override
        public String toString() {
            return reservationId + "," + userName + "," + roomId + "," + checkIn.format(DATE_FORMATTER) + "," + checkOut.format(DATE_FORMATTER) + "," + totalPrice + "," + status;
        }

        // Parse from string
        public static Reservation fromString(String line) {
            String[] parts = line.split(",");
            LocalDate checkIn = LocalDate.parse(parts[3], DATE_FORMATTER);
            LocalDate checkOut = LocalDate.parse(parts[4], DATE_FORMATTER);
            return new Reservation(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]), checkIn, checkOut, Double.parseDouble(parts[5]), parts[6]);
        }
    }

    // HotelReservationSystem class
    static class HotelSystem {
        private List<Room> rooms;
        private List<Reservation> reservations;
        private int nextReservationId;

        public HotelSystem() {
            rooms = new ArrayList<>();
            reservations = new ArrayList<>();
            nextReservationId = 1;
            loadData();
        }

        // Load data from files
        private void loadData() {
            // Load rooms
            File roomsFile = new File(ROOMS_FILE);
            if (roomsFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(roomsFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        rooms.add(Room.fromString(line));
                    }
                } catch (IOException e) {
                    System.err.println("Error loading rooms: " + e.getMessage());
                }
            } else {
                // Initialize with sample rooms
                rooms.add(new Room(1, "Standard", 100.0));
                rooms.add(new Room(2, "Standard", 100.0));
                rooms.add(new Room(3, "Deluxe", 150.0));
                rooms.add(new Room(4, "Deluxe", 150.0));
                rooms.add(new Room(5, "Suite", 250.0));
                rooms.add(new Room(6, "Suite", 250.0));
                saveRooms();
            }

            // Load reservations
            File reservationsFile = new File(RESERVATIONS_FILE);
            if (reservationsFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(reservationsFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        reservations.add(Reservation.fromString(line));
                    }
                    if (!reservations.isEmpty()) {
                        nextReservationId = reservations.stream().mapToInt(Reservation::getReservationId).max().orElse(0) + 1;
                    }
                } catch (IOException e) {
                    System.err.println("Error loading reservations: " + e.getMessage());
                }
            }
        }

        // Save rooms to file
        private void saveRooms() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
                for (Room room : rooms) {
                    writer.write(room.toString());
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error saving rooms: " + e.getMessage());
            }
        }

        // Save reservations to file
        private void saveReservations() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESERVATIONS_FILE))) {
                for (Reservation res : reservations) {
                    writer.write(res.toString());
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error saving reservations: " + e.getMessage());
            }
        }

        // Search available rooms based on category and dates
        public List<Room> searchRooms(String category, LocalDate checkIn, LocalDate checkOut) {
            List<Room> availableRooms = new ArrayList<>();
            for (Room room : rooms) {
                if (category != null && !room.getCategory().equalsIgnoreCase(category)) {
                    continue;
                }
                // Check if room is available for the dates
                boolean isAvailable = true;
                for (Reservation res : reservations) {
                    if (res.getRoomId() == room.getRoomId() && "confirmed".equals(res.getStatus())) {
                        // Overlap check: not available if existing reservation overlaps
                        if (!(res.getCheckOut().isBefore(checkIn) || res.getCheckIn().isAfter(checkOut))) {
                            isAvailable = false;
                            break;
                        }
                    }
                }
                if (isAvailable) {
                    availableRooms.add(room);
                }
            }
            return availableRooms;
        }

        // Make a reservation
        public String makeReservation(String userName, int roomId, LocalDate checkIn, LocalDate checkOut) {
            // Find the room
            Room room = rooms.stream().filter(r -> r.getRoomId() == roomId).findFirst().orElse(null);
            if (room == null) {
                return "Room not found.";
            }

            // Check availability
            List<Room> available = searchRooms(null, checkIn, checkOut);
            if (!available.contains(room)) {
                return "Room not available for selected dates.";
            }

            // Calculate total price (assuming price per night)
            long nights = checkIn.until(checkOut).getDays();
            if (nights <= 0) {
                return "Invalid dates: check-out must be after check-in.";
            }
            double totalPrice = room.getPrice() * nights;

            // Simulate payment
            System.out.println("Simulating payment of $" + String.format("%.2f", totalPrice) + " for " + nights + " nights.");
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter 'yes' to confirm payment: ");
            String paymentConfirm = scanner.nextLine().trim().toLowerCase();
            if (!"yes".equals(paymentConfirm)) {
                return "Payment failed. Reservation not made.";
            }

            // Create and add reservation
            Reservation reservation = new Reservation(nextReservationId, userName, roomId, checkIn, checkOut, totalPrice);
            reservations.add(reservation);
            nextReservationId++;
            saveReservations();
            return "Reservation made successfully. ID: " + reservation.getReservationId();
        }

        // Cancel a reservation
        public String cancelReservation(int reservationId, String userName) {
            Reservation reservation = reservations.stream()
                    .filter(r -> r.getReservationId() == reservationId && r.getUserName().equals(userName))
                    .findFirst().orElse(null);
            if (reservation == null) {
                return "Reservation not found.";
            }
            if ("cancelled".equals(reservation.getStatus())) {
                return "Reservation already cancelled.";
            }
            reservation.setStatus("cancelled");
            saveReservations();
            return "Reservation cancelled successfully.";
        }

        // View booking details
        public String viewBookingDetails(int reservationId, String userName) {
            Reservation reservation = reservations.stream()
                    .filter(r -> r.getReservationId() == reservationId && r.getUserName().equals(userName))
                    .findFirst().orElse(null);
            if (reservation == null) {
                return "Reservation not found.";
            }
            Room room = rooms.stream().filter(r -> r.getRoomId() == reservation.getRoomId()).findFirst().orElse(null);
            return String.format(
                    "Reservation ID: %d\nUser: %s\nRoom: %s (ID: %d)\nCheck-in: %s\nCheck-out: %s\nTotal Price: $%.2f\nStatus: %s",
                    reservation.getReservationId(),
                    reservation.getUserName(),
                    room.getCategory(),
                    room.getRoomId(),
                    reservation.getCheckIn().format(DATE_FORMATTER),
                    reservation.getCheckOut().format(DATE_FORMATTER),
                    reservation.getTotalPrice(),
                    reservation.getStatus()
            );
        }
    }

    public static void main(String[] args) {
        HotelSystem system = new HotelSystem();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nHotel Reservation System");
            System.out.println("1. Search Rooms");
            System.out.println("2. Make Reservation");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. View Booking Details");
            System.out.println("5. Exit");

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter category (Standard/Deluxe/Suite) or leave blank: ");
                    String category = scanner.nextLine().trim();
                    if (category.isEmpty()) category = null;
                    System.out.print("Enter check-in date (YYYY-MM-DD): ");
                    String checkInStr = scanner.nextLine().trim();
                    System.out.print("Enter check-out date (YYYY-MM-DD): ");
                    String checkOutStr = scanner.nextLine().trim();
                    try {
                        LocalDate checkIn = LocalDate.parse(checkInStr, DATE_FORMATTER);
                        LocalDate checkOut = LocalDate.parse(checkOutStr, DATE_FORMATTER);
                        List<Room> rooms = system.searchRooms(category, checkIn, checkOut);
                        if (!rooms.isEmpty()) {
                            for (Room room : rooms) {
                                System.out.printf("Room ID: %d, Category: %s, Price: $%.2f/night\n",
                                        room.getRoomId(), room.getCategory(), room.getPrice());
                            }
                        } else {
                            System.out.println("No rooms available.");
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Use YYYY-MM-DD.");
                    }
                    break;

                case "2":
                    System.out.print("Enter your name: ");
                    String userName = scanner.nextLine().trim();
                    System.out.print("Enter room ID: ");
                    int roomId;
                    try {
                        roomId = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid room ID.");
                        break;
                    }
                    System.out.print("Enter check-in date (YYYY-MM-DD): ");
                    String checkInStr2 = scanner.nextLine().trim();
                    System.out.print("Enter check-out date (YYYY-MM-DD): ");
                    String checkOutStr2 = scanner.nextLine().trim();
                    try {
                        LocalDate checkIn = LocalDate.parse(checkInStr2, DATE_FORMATTER);
                        LocalDate checkOut = LocalDate.parse(checkOutStr2, DATE_FORMATTER);
                        String result = system.makeReservation(userName, roomId, checkIn, checkOut);
                        System.out.println(result);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Use YYYY-MM-DD.");
                    }
                    break;

                case "3":
                    System.out.print("Enter reservation ID: ");
                    int reservationId;
                    try {
                        reservationId = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid reservation ID.");
                        break;
                    }
                    System.out.print("Enter your name: ");
                    String userNameCancel = scanner.nextLine().trim();
                    String resultCancel = system.cancelReservation(reservationId, userNameCancel);
                    System.out.println(resultCancel);
                    break;

                case "4":
                    System.out.print("Enter reservation ID: ");
                    int reservationIdView;
                    try {
                        reservationIdView = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid reservation ID.");
                        break;
                    }
                    System.out.print("Enter your name: ");
                    String userNameView = scanner.nextLine().trim();
                    String resultView = system.viewBookingDetails(reservationIdView, userNameView);
                    System.out.println(resultView);
                    break;

                case "5":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

}