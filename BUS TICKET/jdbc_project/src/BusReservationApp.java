import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class BusReservationApp extends JFrame {

    private static final String URL = "jdbc:mysql://localhost:3306/bus_reservation";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection conn;
    private JTable table;
    private DefaultTableModel tableModel;
    private String currentUserRole = "";
    private String currentUserEmail = "";

    private static final String SESSION_FILE = "session.txt";

    public BusReservationApp() {
        loginTypeSelectionGUI();
    }

    // ---------------- SESSION MANAGEMENT ----------------
    private void saveSession(String email, String role) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SESSION_FILE))) {
            bw.write(email + ":" + role);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String[] loadSession() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && line.contains(":")) return line.split(":");
            return null;
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    private void clearSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) file.delete();
    }

    // ---------------- LOGIN TYPE SELECTION ----------------
    private void loginTypeSelectionGUI() {
        String[] options = {"Admin Login", "User Login"};
        int choice = JOptionPane.showOptionDialog(null,
                "Select login type",
                "Bus Reservation Login",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[1]);

        if (choice == 0) adminLoginGUI();
        else if (choice == 1) userLoginGUI();
        else System.exit(0);
    }

    // ---------------- ADMIN LOGIN ----------------
    private void adminLoginGUI() {
        String[] session = loadSession();
        if (session != null && session[1].equals("admin")) {
            currentUserRole = "admin";
            currentUserEmail = "admin@bus.com";
            mainGUI();
            return;
        }

        JFrame frame = new JFrame("Admin Login");
        frame.setSize(350, 220);
        frame.setLayout(new GridLayout(4, 2, 10, 10));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(230, 240, 255));

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Arial", Font.BOLD, 16));
        JTextField txtUser = new JTextField();
        txtUser.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Arial", Font.BOLD, 16));
        JPasswordField txtPass = new JPasswordField();
        txtPass.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel lblCaptcha = new JLabel();
        lblCaptcha.setFont(new Font("Arial", Font.BOLD, 16));
        String captcha = generateCaptcha();
        lblCaptcha.setText("Captcha: " + captcha);

        JTextField txtCaptcha = new JTextField();
        txtCaptcha.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(100, 180, 255));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));

        frame.add(lblUser); frame.add(txtUser);
        frame.add(lblPass); frame.add(txtPass);
        frame.add(lblCaptcha); frame.add(txtCaptcha);
        frame.add(new JLabel()); frame.add(btnLogin);
        frame.setVisible(true);

        btnLogin.addActionListener(e -> {
            String username = txtUser.getText();
            String password = new String(txtPass.getPassword());
            String captchaInput = txtCaptcha.getText();
            if(!captchaInput.equals(captcha)){
                JOptionPane.showMessageDialog(frame,"Captcha incorrect!");
                return;
            }
            if (username.equals("admin") && password.equals("admin")) {
                currentUserRole = "admin";
                currentUserEmail = "admin@bus.com";
                saveSession(currentUserEmail, currentUserRole);
                JOptionPane.showMessageDialog(frame, "Admin login successful!");
                frame.dispose();
                mainGUI();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid admin credentials!");
            }
        });
    }

    // ---------------- USER LOGIN / REGISTER ----------------
    private void userLoginGUI() {
        String[] session = loadSession();
        if (session != null && session[1].equals("user")) {
            try {
                String sql = "SELECT role FROM Users WHERE email=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, session[0]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentUserRole = rs.getString("role");
                    currentUserEmail = session[0];
                    JOptionPane.showMessageDialog(null, "Welcome back, " + currentUserEmail);
                    mainGUI();
                    return;
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        JFrame loginFrame = new JFrame("User Login/Register");
        loginFrame.setSize(450, 300);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setLayout(new GridLayout(5, 2, 10, 10));
        loginFrame.getContentPane().setBackground(new Color(255, 240, 230));

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 16));
        JTextField txtEmail = new JTextField();
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 16));
        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel lblCaptcha = new JLabel();
        lblCaptcha.setFont(new Font("Arial", Font.BOLD, 16));
        String captcha = generateCaptcha();
        lblCaptcha.setText("Captcha: " + captcha);

        JTextField txtCaptcha = new JTextField();
        txtCaptcha.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(255, 180, 180));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        JButton btnRegister = new JButton("Register");
        btnRegister.setBackground(new Color(180, 255, 180));
        btnRegister.setFont(new Font("Arial", Font.BOLD, 16));

        loginFrame.add(lblEmail); loginFrame.add(txtEmail);
        loginFrame.add(lblPassword); loginFrame.add(txtPassword);
        loginFrame.add(lblCaptcha); loginFrame.add(txtCaptcha);
        loginFrame.add(btnLogin); loginFrame.add(btnRegister);
        loginFrame.setVisible(true);

        btnLogin.addActionListener(e -> {
            try {
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                String captchaInput = txtCaptcha.getText();
                if(!captchaInput.equals(captcha)){
                    JOptionPane.showMessageDialog(loginFrame,"Captcha incorrect!");
                    return;
                }

                String sql = "SELECT role FROM Users WHERE email=? AND password=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentUserRole = rs.getString("role");
                    currentUserEmail = email;
                    saveSession(email, "user");
                    JOptionPane.showMessageDialog(loginFrame, "Login successful!");
                    loginFrame.dispose();
                    mainGUI();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid credentials!");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnRegister.addActionListener(e -> {
            try {
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                String name = JOptionPane.showInputDialog(loginFrame, "Enter your Name:");
                if(name==null || name.isEmpty()) return;
                String role = "user";

                String sql = "INSERT INTO Users (name,email,password,role) VALUES (?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, role);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(loginFrame, "Registration successful! You can login now.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(loginFrame, "Email already registered!");
            }
        });
    }

    // ---------------- CAPTCHA GENERATOR ----------------
    private String generateCaptcha() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for(int i=0;i<6;i++){
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ---------------- MAIN GUI ----------------
    private void mainGUI() {
        setTitle("Bus Reservation System - " + currentUserRole.toUpperCase());
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().removeAll();
        repaint();
        revalidate();

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 245, 245));

        JButton showBusesBtn = new JButton("Show Buses");
        JButton reserveBtn = new JButton("Make Reservation");
        JButton showResBtn = new JButton("Show Reservations");
        JButton cancelBtn = new JButton("Cancel Reservation");
        JButton adminManageBtn = new JButton("Admin Panel");
        JButton logoutBtn = new JButton("Logout");

        for(JButton btn: new JButton[]{showBusesBtn,reserveBtn,showResBtn,cancelBtn,adminManageBtn,logoutBtn}){
            btn.setFont(new Font("Arial",Font.BOLD,16));
            btn.setBackground(new Color(200,220,255));
        }

        panel.add(showBusesBtn);
        if (currentUserRole.equals("user")) {
            panel.add(reserveBtn);
            panel.add(showResBtn);
            panel.add(cancelBtn);
        }
        if (currentUserRole.equals("admin")) {
            panel.add(adminManageBtn);
        }
        panel.add(logoutBtn);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        showBusesBtn.addActionListener(e -> showBusesGUI());
        if (currentUserRole.equals("user")) {
            reserveBtn.addActionListener(e -> makeReservationGUI());
            showResBtn.addActionListener(e -> showReservationsGUIForUser());
            cancelBtn.addActionListener(e -> cancelReservationGUI());
        }
        if (currentUserRole.equals("admin")) {
            adminManageBtn.addActionListener(e -> adminManageGUI());
        }

        logoutBtn.addActionListener(e -> {
            clearSession();
            dispose();
            loginTypeSelectionGUI();
        });

        setVisible(true);
    }

    // ---------------- SHOW BUSES ----------------
    private void showBusesGUI() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnIdentifiers(new Object[]{"Bus Number","Bus Type","Available Seats","Total Seats","Route","Fare","Departure Time","Arrival Time"});

            String sql = "SELECT b.busNumber,b.busType,b.totalSeats,b.availableSeats,r.routeName,r.fare,b.departureTime,b.arrivalTime " +
                    "FROM Buses b LEFT JOIN Routes r ON b.routeId=r.routeId";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("busNumber"),
                        rs.getString("busType"),
                        rs.getInt("availableSeats"),
                        rs.getInt("totalSeats"),
                        rs.getString("routeName"), // Display Route Name instead of ID
                        rs.getDouble("fare"),
                        rs.getTime("departureTime"),
                        rs.getTime("arrivalTime")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error fetching buses!"); }
    }

    // ---------------- ADMIN PANEL ----------------
    private void adminManageGUI() {
        JFrame adminFrame = new JFrame("Admin Panel");
        adminFrame.setSize(500, 350);
        adminFrame.setLayout(new GridLayout(5,1,5,5));
        adminFrame.getContentPane().setBackground(new Color(220, 245, 220));

        // <--- CENTER THE ADMIN PANEL FRAME
        adminFrame.setLocationRelativeTo(null);

        JButton addBusBtn = new JButton("Add Bus");
        JButton removeBusBtn = new JButton("Remove Bus");
        JButton updateFareBtn = new JButton("Update Fare");
        JButton viewAllResBtn = new JButton("View All Reservations");
        JButton addRouteBtn = new JButton("Add Route");

        for(JButton btn:new JButton[]{addBusBtn,removeBusBtn,updateFareBtn,viewAllResBtn,addRouteBtn}){
            btn.setFont(new Font("Arial",Font.BOLD,16));
            btn.setBackground(new Color(180,220,255));
        }

        adminFrame.add(addBusBtn);
        adminFrame.add(removeBusBtn);
        adminFrame.add(updateFareBtn);
        adminFrame.add(viewAllResBtn);
        adminFrame.add(addRouteBtn);
        adminFrame.setVisible(true);

        // ---------------- ADD BUS ----------------
        addBusBtn.addActionListener(e -> {
            try {
                // Fetch routes
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT routeId, routeName FROM Routes");
                ArrayList<String> routeOptions = new ArrayList<>();
                while(rs.next()){
                    routeOptions.add(rs.getInt("routeId") + " -> " + rs.getString("routeName"));
                }

                if(routeOptions.isEmpty()){
                    JOptionPane.showMessageDialog(adminFrame,"No routes available. Add routes first!");
                    return;
                }

                String busNumber = JOptionPane.showInputDialog(adminFrame,"Bus Number:");
                if(busNumber==null || busNumber.isEmpty()) return;

                String busType = JOptionPane.showInputDialog(adminFrame,"Bus Type:");
                if(busType==null || busType.isEmpty()) return;

                int totalSeats = Integer.parseInt(JOptionPane.showInputDialog(adminFrame,"Total Seats:"));

                JComboBox<String> routeCombo = new JComboBox<>(routeOptions.toArray(new String[0]));
                int option = JOptionPane.showConfirmDialog(adminFrame, routeCombo, "Select Route", JOptionPane.OK_CANCEL_OPTION);
                if(option != JOptionPane.OK_OPTION) return;

                String selected = (String) routeCombo.getSelectedItem();
                int routeId = Integer.parseInt(selected.split(" -> ")[0]);

                String departureTime = JOptionPane.showInputDialog(adminFrame,"Departure Time (HH:MM:SS):");
                String arrivalTime = JOptionPane.showInputDialog(adminFrame,"Arrival Time (HH:MM:SS):");

                String insertBus = "INSERT INTO Buses (busNumber,busType,totalSeats,availableSeats,routeId,departureTime,arrivalTime) VALUES (?,?,?,?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(insertBus);
                ps.setString(1,busNumber);
                ps.setString(2,busType);
                ps.setInt(3,totalSeats);
                ps.setInt(4,totalSeats);
                ps.setInt(5,routeId);
                ps.setString(6,departureTime);
                ps.setString(7,arrivalTime);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(adminFrame,"Bus added successfully!");
            } catch(Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(adminFrame,"Failed to add bus!"); }
        });

        // ---------------- REMOVE BUS ----------------
        removeBusBtn.addActionListener(e -> {
            try {
                String busNumber = JOptionPane.showInputDialog(adminFrame,"Bus Number to remove:");
                String deleteBus = "DELETE FROM Buses WHERE busNumber=?";
                PreparedStatement ps = conn.prepareStatement(deleteBus);
                ps.setString(1,busNumber);
                int rows = ps.executeUpdate();
                if(rows>0) JOptionPane.showMessageDialog(adminFrame,"Bus removed successfully!");
                else JOptionPane.showMessageDialog(adminFrame,"Bus not found!");
            } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(adminFrame,"Failed to remove bus!"); }
        });

        // ---------------- UPDATE FARE ----------------
        updateFareBtn.addActionListener(e -> {
            try {
                // Fetch routes
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT routeId, routeName FROM Routes");
                ArrayList<String> routeOptions = new ArrayList<>();
                while(rs.next()){
                    routeOptions.add(rs.getInt("routeId") + " -> " + rs.getString("routeName"));
                }
                if(routeOptions.isEmpty()){
                    JOptionPane.showMessageDialog(adminFrame,"No routes available!");
                    return;
                }

                JComboBox<String> routeCombo = new JComboBox<>(routeOptions.toArray(new String[0]));
                int option = JOptionPane.showConfirmDialog(adminFrame, routeCombo, "Select Route to Update Fare", JOptionPane.OK_CANCEL_OPTION);
                if(option != JOptionPane.OK_OPTION) return;

                String selected = (String) routeCombo.getSelectedItem();
                int routeId = Integer.parseInt(selected.split(" -> ")[0]);

                double newFare = Double.parseDouble(JOptionPane.showInputDialog(adminFrame,"Enter new fare for selected route:"));
                String sql = "UPDATE Routes SET fare=? WHERE routeId=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDouble(1,newFare);
                ps.setInt(2,routeId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(adminFrame,"Fare updated successfully!");
            } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(adminFrame,"Failed to update fare!"); }
        });

        // ---------------- VIEW RESERVATIONS ----------------
        viewAllResBtn.addActionListener(e -> showAllReservationsForAdmin());

        // ---------------- ADD ROUTE ----------------
        addRouteBtn.addActionListener(e -> {
            try {
                String source = JOptionPane.showInputDialog(adminFrame,"Source:");
                String destination = JOptionPane.showInputDialog(adminFrame,"Destination:");
                double fare = Double.parseDouble(JOptionPane.showInputDialog(adminFrame,"Fare:"));

                String insertRoute = "INSERT INTO Routes (routeName,fare) VALUES (?,?)";
                PreparedStatement ps = conn.prepareStatement(insertRoute);
                ps.setString(1,source + " -> " + destination);
                ps.setDouble(2,fare);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(adminFrame,"Route added successfully!");
            } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(adminFrame,"Failed to add route!"); }
        });
    }

    // ---------------- SHOW ALL RESERVATIONS (ADMIN) ----------------
    private void showAllReservationsForAdmin() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnIdentifiers(new Object[]{"Reservation ID","Passenger Name","Age","User Email","Bus Number","Seat Number","Journey Date","Status"});
            String sql = "SELECT r.reservationId,p.name,p.age,r.userEmail,r.busNumber,r.seatNumber,r.journeyDate,r.status " +
                    "FROM Reservations r JOIN Passengers p ON r.passengerId=p.passengerId";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                tableModel.addRow(new Object[]{
                        rs.getInt("reservationId"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("userEmail"),
                        rs.getString("busNumber"),
                        rs.getInt("seatNumber"),
                        rs.getDate("journeyDate"),
                        rs.getString("status")
                });
            }
        } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Error fetching reservations!"); }
    }

    // ---------------- MAKE RESERVATION ----------------
    private void makeReservationGUI() {
        try {
            String busNumber = JOptionPane.showInputDialog(this, "Enter Bus Number:");
            if (busNumber == null || busNumber.isEmpty()) return;

            String journeyDateStr = JOptionPane.showInputDialog(this, "Enter Journey Date (YYYY-MM-DD):");
            if (journeyDateStr == null || journeyDateStr.isEmpty()) return;
            java.sql.Date journeyDate = java.sql.Date.valueOf(journeyDateStr);

            String busSQL = "SELECT totalSeats FROM Buses WHERE busNumber=?";
            PreparedStatement psBus = conn.prepareStatement(busSQL);
            psBus.setString(1, busNumber);
            ResultSet rsBus = psBus.executeQuery();
            if (!rsBus.next()) { JOptionPane.showMessageDialog(this, "Bus not found!"); return; }

            int totalSeats = rsBus.getInt("totalSeats");
            boolean[] booked = new boolean[totalSeats + 1];
            String bookedSQL = "SELECT seatNumber FROM Reservations WHERE busNumber=? AND journeyDate=? AND status='Booked'";
            PreparedStatement psB = conn.prepareStatement(bookedSQL);
            psB.setString(1, busNumber);
            psB.setDate(2, journeyDate);
            ResultSet rsB = psB.executeQuery();
            while (rsB.next()) booked[rsB.getInt("seatNumber")] = true;

            String contactNumber = JOptionPane.showInputDialog(this, "Enter contact number for passenger(s):");
            if(contactNumber==null || contactNumber.isEmpty()) return;

            JPanel seatsPanel = new JPanel(new GridLayout((totalSeats/4)+1,4,5,5));
            ArrayList<Integer> selectedSeats = new ArrayList<>();
            JButton[] seatButtons = new JButton[totalSeats+1];
            for(int i=1;i<=totalSeats;i++){
                JButton btn = new JButton(""+i);
                btn.setFont(new Font("Arial",Font.BOLD,14));
                seatButtons[i]=btn;
                if(booked[i]){ btn.setBackground(Color.RED); btn.setEnabled(false); }
                else btn.setBackground(Color.GREEN);
                int seatNo=i;
                btn.addActionListener(e -> {
                    if(selectedSeats.contains(seatNo)){
                        selectedSeats.remove((Integer)seatNo);
                        btn.setBackground(Color.GREEN);
                    } else{
                        selectedSeats.add(seatNo);
                        btn.setBackground(Color.YELLOW);
                    }
                });
                seatsPanel.add(btn);
            }

            JPanel confirmPanel = new JPanel();
            JCheckBox paymentCheckBox = new JCheckBox("Agree to the terms and conditions");
            paymentCheckBox.setFont(new Font("Arial", Font.BOLD, 14));
            JButton confirmBtn = new JButton("Confirm Reservation");
            confirmBtn.setFont(new Font("Arial",Font.BOLD,16));
            confirmBtn.setBackground(new Color(180,220,255));
            confirmPanel.add(paymentCheckBox);
            confirmPanel.add(confirmBtn);

            JFrame seatFrame = new JFrame("Select Seats - Bus " + busNumber);
            seatFrame.setSize(700,500);
            seatFrame.setLayout(new BorderLayout());
            seatFrame.add(new JScrollPane(seatsPanel), BorderLayout.CENTER);
            seatFrame.add(confirmPanel, BorderLayout.SOUTH);
            seatFrame.setVisible(true);

            confirmBtn.addActionListener(e -> {
                try {
                    if(selectedSeats.size()==0){ JOptionPane.showMessageDialog(seatFrame,"Select at least one seat"); return; }
                    if(!paymentCheckBox.isSelected()){ JOptionPane.showMessageDialog(seatFrame,"Please agree the terms and conditions to continue booking!"); return; }

                    for(int seat:selectedSeats){
                        String passengerName = JOptionPane.showInputDialog(seatFrame,"Enter passenger name for seat "+seat+":");
                        if(passengerName==null || passengerName.isEmpty()) return;
                        int age = Integer.parseInt(JOptionPane.showInputDialog(seatFrame,"Enter age for "+passengerName+":"));

                        String insertPassenger = "INSERT INTO Passengers(name,age,contact) VALUES (?,?,?)";
                        PreparedStatement psP = conn.prepareStatement(insertPassenger, Statement.RETURN_GENERATED_KEYS);
                        psP.setString(1, passengerName);
                        psP.setInt(2, age);
                        psP.setString(3, contactNumber);
                        psP.executeUpdate();

                        ResultSet keys = psP.getGeneratedKeys();
                        int passengerId = -1;
                        if(keys.next()) passengerId = keys.getInt(1);

                        String insertRes = "INSERT INTO Reservations(passengerId,userEmail,busNumber,seatNumber,journeyDate,status) VALUES (?,?,?,?,?,?)";
                        PreparedStatement psR = conn.prepareStatement(insertRes);
                        psR.setInt(1, passengerId);
                        psR.setString(2,currentUserEmail);
                        psR.setString(3,busNumber);
                        psR.setInt(4,seat);
                        psR.setDate(5,journeyDate);
                        psR.setString(6,"Booked");
                        psR.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(seatFrame,"Reservation successful!");
                    seatFrame.dispose();
                } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(seatFrame,"Reservation failed!"); }
            });
        } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Error making reservation!"); }
    }

    // ---------------- SHOW RESERVATIONS (USER) ----------------
    private void showReservationsGUIForUser() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnIdentifiers(new Object[]{"Reservation ID","Passenger Name","Age","Bus Number","Seat Number","Journey Date","Status"});
            String sql = "SELECT r.reservationId,p.name,p.age,r.busNumber,r.seatNumber,r.journeyDate,r.status " +
                    "FROM Reservations r JOIN Passengers p ON r.passengerId=p.passengerId WHERE r.userEmail=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,currentUserEmail);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                tableModel.addRow(new Object[]{
                        rs.getInt("reservationId"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("busNumber"),
                        rs.getInt("seatNumber"),
                        rs.getDate("journeyDate"),
                        rs.getString("status")
                });
            }
        } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Error fetching reservations!"); }
    }

    // ---------------- CANCEL RESERVATION ----------------
    private void cancelReservationGUI() {
        try {
            int resId = Integer.parseInt(JOptionPane.showInputDialog(this,"Enter Reservation ID to cancel:"));
            String sql = "UPDATE Reservations SET status='Cancelled' WHERE reservationId=? AND userEmail=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,resId);
            ps.setString(2,currentUserEmail);
            int rows = ps.executeUpdate();
            if(rows>0) JOptionPane.showMessageDialog(this,"Reservation cancelled successfully!");
            else JOptionPane.showMessageDialog(this,"Reservation not found or not yours!");
        } catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Error cancelling reservation!"); }
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        try { conn = DriverManager.getConnection(URL, USER, PASSWORD); }
        catch (Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(null,"Database connection failed!"); return; }

        SwingUtilities.invokeLater(() -> new BusReservationApp());
    }
}
