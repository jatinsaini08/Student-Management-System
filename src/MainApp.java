import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;
import java.util.regex.Pattern;

public class MainApp {

    private static JFrame frame;
    private static CardLayout cardLayout = new CardLayout();
    private static JPanel pages = new JPanel(cardLayout);
    private static JPanel topNavBar;

    private static String currentAdminPassword = "admin";
    private static final String REGISTERED_EMAIL = "admin@poornima.org";
    private static String generatedOtp = "";

    private static DefaultTableModel tableModel;
    private static TableRowSorter<DefaultTableModel> sorter;
    private static JLabel totalCountLabel = new JLabel("Total students: 0");
    private static JTextField txtSearch = new JTextField(18);

    // Add form input fields
    private static JTextField txtAddRoll = new JTextField();
    private static JTextField txtAddName = new JTextField();
    private static JTextField txtAddParent = new JTextField();
    private static JTextField txtAddPhone = new JTextField();
    private static JTextField txtAddEmail = new JTextField();
    private static JComboBox<String> cmbAddCourse = new JComboBox<>(new String[]{"BCA", "BSc CS", "B.Tech CSE", "MCA", "BBA"});

    // Edit form input fields
    private static JLabel lblEditId = new JLabel();
    private static JTextField txtEditName = new JTextField();
    private static JTextField txtEditParent = new JTextField();
    private static JTextField txtEditPhone = new JTextField();
    private static JTextField txtEditEmail = new JTextField();
    private static JComboBox<String> cmbEditCourse = new JComboBox<>(new String[]{"BCA", "BSc CS", "B.Tech CSE", "MCA", "BBA"});

    private static JTextField txtForgotEmail = new JTextField();
    private static JTextField txtForgotOtp = new JTextField();
    private static JPasswordField txtForgotNewPass = new JPasswordField();
    private static JPasswordField txtForgotConfirmPass = new JPasswordField();
    private static JButton btnSendOtp;
    private static JButton btnResetPass;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Poornima Portal - Student Management System");
        frame.setSize(1000, 680);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        topNavBar = createTopNav();
        topNavBar.setVisible(false);

        pages.add(createLoginPage(), "LOGIN");
        pages.add(createDashboardPage(), "DASHBOARD");
        pages.add(createAddStudentPage(), "ADD");
        pages.add(createViewStudentsPage(), "VIEW");
        pages.add(createEditStudentPage(), "EDIT");
        pages.add(createAboutPage(), "ABOUT");
        pages.add(createChangePasswordPage(), "PASSWORD");
        pages.add(createForgotPasswordPage(), "FORGOT");

        frame.add(topNavBar, BorderLayout.NORTH);
        frame.add(pages, BorderLayout.CENTER);

        cardLayout.show(pages, "LOGIN");
        frame.setVisible(true);
    }

    private static JPanel createHeaderBanner() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        panel.setBackground(Color.WHITE);

        ImageIcon icon = new ImageIcon("pietlogo.png");
        if (icon.getIconWidth() <= 0) {
            icon = new ImageIcon("Poornima.jpeg");
        }

        if (icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(700, 140, Image.SCALE_SMOOTH);
            panel.add(new JLabel(new ImageIcon(img)));
        } else {
            JLabel textLabel = new JLabel("POORNIMA PORTAL");
            textLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
            textLabel.setForeground(new Color(0, 51, 153));
            panel.add(textLabel);
        }
        return panel;
    }

    private static JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        panel.setBackground(new Color(0, 51, 153));
        JLabel label = new JLabel("Developed By - Jatin Saini");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label);
        return panel;
    }

    private static JPanel createTopNav() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)));

        String[] items = {"Poornima Portal", "Add", "View All", "Update", "About", "Change Password", "Logout"};
        for (String item : items) {
            JButton btn = new JButton(item);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(new Color(0, 51, 153));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                if (item.equals("Poornima Portal")) {
                    clearAddForm();
                    cardLayout.show(pages, "DASHBOARD");
                } else if (item.equals("Add")) {
                    clearAddForm();
                    cardLayout.show(pages, "ADD");
                } else if (item.equals("View All")) {
                    loadStudentDataFromDB();
                    cardLayout.show(pages, "VIEW");
                } else if (item.equals("Update")) {
                    promptUpdate();
                } else if (item.equals("About")) {
                    cardLayout.show(pages, "ABOUT");
                } else if (item.equals("Change Password")) {
                    cardLayout.show(pages, "PASSWORD");
                } else if (item.equals("Logout")) {
                    logout();
                }
            });
            nav.add(btn);
        }
        return nav;
    }

    private static JPanel createLoginPage() {
        JPanel page = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(242, 246, 255));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(new Color(242, 246, 255));

        JLabel heading = new JLabel("LOGIN");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(new Color(0, 51, 153));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel box = new JPanel(new GridLayout(4, 2, 10, 12));
        box.setPreferredSize(new Dimension(380, 180));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JTextField userText = new JTextField();
        JPasswordField passText = new JPasswordField();
        JButton loginBtn = createPrimaryButton("LOGIN");
        JButton forgotBtn = createSecondaryButton("Forgot Password?");

        box.add(new JLabel("Username:")); box.add(userText);
        box.add(new JLabel("Password:")); box.add(passText);
        box.add(loginBtn);                box.add(forgotBtn);
        box.add(new JLabel("Default: admin / admin"));

        loginBtn.addActionListener(e -> {
            String u = userText.getText().trim();
            String p = new String(passText.getPassword()).trim();
            if (u.equals("admin") && p.equals(currentAdminPassword)) {
                topNavBar.setVisible(true);
                cardLayout.show(pages, "DASHBOARD");
                userText.setText("");
                passText.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        forgotBtn.addActionListener(e -> {
            resetForgotForm();
            cardLayout.show(pages, "FORGOT");
        });

        wrap.add(heading);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(box);

        center.add(wrap);
        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(createFooter(), BorderLayout.SOUTH);
        return page;
    }

    private static JPanel createForgotPasswordPage() {
        JPanel page = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(242, 246, 255));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(new Color(242, 246, 255));

        JLabel heading = new JLabel("Password Recovery via OTP");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(new Color(0, 51, 153));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setPreferredSize(new Dimension(480, 240));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        btnSendOtp = createPrimaryButton("Send OTP");
        btnResetPass = createPrimaryButton("Verify & Update");
        btnResetPass.setEnabled(false);
        JButton btnBack = createSecondaryButton("Back to Login");

        txtForgotOtp.setEnabled(false);
        txtForgotNewPass.setEnabled(false);
        txtForgotConfirmPass.setEnabled(false);

        btnSendOtp.addActionListener(e -> {
            String email = txtForgotEmail.getText().trim();
            if (!email.equalsIgnoreCase(REGISTERED_EMAIL)) {
                JOptionPane.showMessageDialog(frame, "Email not found!", "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }
            generatedOtp = String.valueOf(100000 + new Random().nextInt(900000));
            JOptionPane.showMessageDialog(frame, "OTP Sent Simulator: " + generatedOtp, "OTP Sent", JOptionPane.INFORMATION_MESSAGE);
            txtForgotEmail.setEditable(false);
            btnSendOtp.setEnabled(false);
            txtForgotOtp.setEnabled(true);
            txtForgotNewPass.setEnabled(true);
            txtForgotConfirmPass.setEnabled(true);
            btnResetPass.setEnabled(true);
        });

        btnResetPass.addActionListener(e -> {
            String enteredOtp = txtForgotOtp.getText().trim();
            String newP = new String(txtForgotNewPass.getPassword());
            if (!enteredOtp.equals(generatedOtp)) {
                JOptionPane.showMessageDialog(frame, "Incorrect OTP!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentAdminPassword = newP;
            JOptionPane.showMessageDialog(frame, "Password reset successful!");
            resetForgotForm();
            cardLayout.show(pages, "LOGIN");
        });

        btnBack.addActionListener(e -> {
            resetForgotForm();
            cardLayout.show(pages, "LOGIN");
        });

        form.add(new JLabel("Registered Email:"));
        JPanel emailPanel = new JPanel(new BorderLayout(5, 0));
        emailPanel.setBackground(Color.WHITE);
        emailPanel.add(txtForgotEmail, BorderLayout.CENTER);
        emailPanel.add(btnSendOtp, BorderLayout.EAST);
        form.add(emailPanel);

        form.add(new JLabel("Enter 6-Digit OTP:")); form.add(txtForgotOtp);
        form.add(new JLabel("New Password:"));     form.add(txtForgotNewPass);
        form.add(new JLabel("Confirm Password:"));  form.add(txtForgotConfirmPass);
        form.add(btnResetPass);                    form.add(btnBack);

        wrap.add(heading);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(form);
        center.add(wrap);
        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(createFooter(), BorderLayout.SOUTH);
        return page;
    }

    private static void resetForgotForm() {
        txtForgotEmail.setText("");
        txtForgotEmail.setEditable(true);
        txtForgotOtp.setText("");
        txtForgotOtp.setEnabled(false);
        txtForgotNewPass.setText("");
        txtForgotNewPass.setEnabled(false);
        txtForgotConfirmPass.setText("");
        txtForgotConfirmPass.setEnabled(false);
        btnSendOtp.setEnabled(true);
        btnResetPass.setEnabled(false);
        generatedOtp = "";
    }

    private static JPanel createDashboardPage() {
        JPanel page = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(242, 246, 255));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(new Color(242, 246, 255));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(0, 51, 153));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Welcome, admin (Role: ADMIN)   —   choose an option:");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(Color.DARK_GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(3, 2, 16, 12));
        grid.setPreferredSize(new Dimension(520, 150));
        grid.setBackground(new Color(242, 246, 255));

        JButton btnAdd = createPrimaryButton("Add Student");
        JButton btnView = createPrimaryButton("View / Search Students");
        JButton btnUpdate = createPrimaryButton("Update Student");
        JButton btnPass = createSecondaryButton("Change Password");
        JButton btnAbout = createSecondaryButton("About");
        JButton btnLogout = createSecondaryButton("Logout");

        grid.add(btnAdd);    grid.add(btnView);
        grid.add(btnUpdate); grid.add(btnPass);
        grid.add(btnAbout);  grid.add(btnLogout);

        btnAdd.addActionListener(e -> { clearAddForm(); cardLayout.show(pages, "ADD"); });
        btnView.addActionListener(e -> { loadStudentDataFromDB(); cardLayout.show(pages, "VIEW"); });
        btnUpdate.addActionListener(e -> promptUpdate());
        btnPass.addActionListener(e -> cardLayout.show(pages, "PASSWORD"));
        btnAbout.addActionListener(e -> cardLayout.show(pages, "ABOUT"));
        btnLogout.addActionListener(e -> logout());

        inner.add(title);
        inner.add(Box.createVerticalStrut(10));
        inner.add(sub);
        inner.add(Box.createVerticalStrut(25));
        inner.add(grid);
        center.add(inner);

        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(createFooter(), BorderLayout.SOUTH);
        return page;
    }

    private static JPanel createAboutPage() {
        JPanel page = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(242, 246, 255));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(new Color(242, 246, 255));

        JLabel heading = new JLabel("About This Application");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(new Color(0, 51, 153));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Updated About HTML reflecting MySQL backend and current features
        String infoHTML = "<html><body style='width: 750px; font-family: Segoe UI; color: #333333;'>"
                + "<h3 style='color: #003399; margin: 0 0 8px 0;'>Poornima Portal — Student Management System</h3>"
                + "<p style='font-size: 12px; line-height: 1.5; margin: 0 0 10px 0;'>"
                + "This software is built for collegiate administrative departments to maintain, organize, "
                + "and retrieve academic student profiles efficiently using a robust **MySQL Database** backend via JDBC."
                + "</p>"
                + "<b style='color: #003399; font-size: 12px;'>Key Features & Operations:</b>"
                + "<ul style='font-size: 11px; line-height: 1.6; margin-top: 5px; padding-left: 18px;'>"
                + "  <li><b>Add Student:</b> Enrolls new student records into the MySQL database with real-time validation (blocking numbers in names, enforcing a strict 10-digit phone limit, and checking valid email formats).</li>"
                + "  <li><b>View / Search Students:</b> Displays directory records fetched live from MySQL in a sortable table accompanied by an instant universal search filter.</li>"
                + "  <li><b>Update Student:</b> Loads existing student records by Roll Number from the database, permitting secure in-place edits without modifying primary keys.</li>"
                + "  <li><b>Delete Student:</b> Permanently purges student profiles from the database with safety confirmation prompts.</li>"
                + "  <li><b>Security & OTP Recovery:</b> Enforces admin authentication alongside a simulated 6-digit email OTP verification pipeline for forgotten credentials.</li>"
                + "</ul>"
                + "<div style='margin-top: 10px; font-size: 11px; border-top: 1px solid #E0E0E0; padding-top: 6px; color: #555555;'>"
                + "<b>Developer:</b> Jatin Saini &nbsp;|&nbsp; <b>Institution:</b> Poornima Institute of Engineering & Technology"
                + "</div>"
                + "</body></html>";

        JLabel contentLabel = new JLabel(infoHTML);
        card.add(contentLabel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setPreferredSize(new Dimension(540, 240));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 215, 230), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(12);

        JButton btnBack = createSecondaryButton("Back to Dashboard");
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> cardLayout.show(pages, "DASHBOARD"));

        wrap.add(heading);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(scrollPane);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(btnBack);

        center.add(wrap);
        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(createFooter(), BorderLayout.SOUTH);
        return page;
    }

    private static JPanel createAddStudentPage() {
        JPanel page = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(242, 246, 255));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setPreferredSize(new Dimension(460, 260));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        attachTextOnlyListener(txtAddName);
        attachTextOnlyListener(txtAddParent);
        attachPhoneListener(txtAddPhone);

        JButton btnSave = createPrimaryButton("SAVE");
        JButton btnBack = createSecondaryButton("BACK");

        form.add(new JLabel("Roll No (ID):"));  form.add(txtAddRoll);
        form.add(new JLabel("Student Name:"));   form.add(txtAddName);
        form.add(new JLabel("Parent's Name:"));  form.add(txtAddParent);
        form.add(new JLabel("Phone Number:"));   form.add(txtAddPhone);
        form.add(new JLabel("Email:"));          form.add(txtAddEmail);
        form.add(new JLabel("Course:"));         form.add(cmbAddCourse);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(new Color(242, 246, 255));
        btnPanel.add(btnSave);
        btnPanel.add(btnBack);

        btnSave.addActionListener(e -> {
            String roll = txtAddRoll.getText().trim();
            String name = txtAddName.getText().trim();
            String parent = txtAddParent.getText().trim();
            String phone = txtAddPhone.getText().trim();
            String email = txtAddEmail.getText().trim();
            String course = (String) cmbAddCourse.getSelectedItem();

            if (roll.isEmpty() || name.isEmpty() || parent.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (phone.length() != 10) {
                JOptionPane.showMessageDialog(frame, "Phone Number must be exactly 10 digits!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid email address (e.g., student@gmail.com)!", "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                saveStudentToDB(roll, name, parent, phone, email, course);
                JOptionPane.showMessageDialog(frame, "Student successfully saved with Roll No: " + roll);
                clearAddForm();
                loadStudentDataFromDB();
                cardLayout.show(pages, "VIEW");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error saving: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> cardLayout.show(pages, "DASHBOARD"));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(new Color(242, 246, 255));

        JLabel heading = new JLabel("Add Student");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(new Color(0, 51, 153));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        wrap.add(heading);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(form);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(btnPanel);

        center.add(wrap);
        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(createFooter(), BorderLayout.SOUTH);
        return page;
    }

    private static JPanel createEditStudentPage() {
        JPanel page = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(242, 246, 255));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setPreferredSize(new Dimension(460, 260));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        attachTextOnlyListener(txtEditName);
        attachTextOnlyListener(txtEditParent);
        attachPhoneListener(txtEditPhone);

        JButton btnUpdate = createPrimaryButton("SAVE CHANGES");
        JButton btnCancel = createSecondaryButton("CANCEL");

        lblEditId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(new JLabel("Roll No (ID):"));  form.add(lblEditId);
        form.add(new JLabel("Student Name:"));   form.add(txtEditName);
        form.add(new JLabel("Parent's Name:"));  form.add(txtEditParent);
        form.add(new JLabel("Phone Number:"));   form.add(txtEditPhone);
        form.add(new JLabel("Email:"));          form.add(txtEditEmail);
        form.add(new JLabel("Course:"));         form.add(cmbEditCourse);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(new Color(242, 246, 255));
        btnPanel.add(btnUpdate);
        btnPanel.add(btnCancel);

        btnUpdate.addActionListener(e -> {
            String roll = lblEditId.getText();
            String name = txtEditName.getText().trim();
            String parent = txtEditParent.getText().trim();
            String phone = txtEditPhone.getText().trim();
            String email = txtEditEmail.getText().trim();
            String course = (String) cmbEditCourse.getSelectedItem();

            if (name.isEmpty() || parent.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (phone.length() != 10) {
                JOptionPane.showMessageDialog(frame, "Phone Number must be exactly 10 digits!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid email address!", "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                updateStudentInDB(roll, name, parent, phone, email, course);
                JOptionPane.showMessageDialog(frame, "Student updated successfully!");
                loadStudentDataFromDB();
                cardLayout.show(pages, "VIEW");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error updating: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> cardLayout.show(pages, "DASHBOARD"));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(new Color(242, 246, 255));

        JLabel heading = new JLabel("Update Student");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(new Color(0, 51, 153));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        wrap.add(heading);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(form);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(btnPanel);

        center.add(wrap);
        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(createFooter(), BorderLayout.SOUTH);
        return page;
    }

    private static JPanel createViewStudentsPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(242, 246, 255));

        String[] columns = {"Roll No", "Student Name", "Parent's Name", "Phone", "Email", "Course"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        searchBarPanel.setBackground(new Color(242, 246, 255));
        searchBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel lblSearch = new JLabel("🔍 Search (Any Field):");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(new Color(0, 51, 153));

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = txtSearch.getText().trim();
                if (query.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
                }
            }
        });

        searchBarPanel.add(lblSearch);
        searchBarPanel.add(txtSearch);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 30, 10, 30));

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(new Color(242, 246, 255));
        tableContainer.add(searchBarPanel, BorderLayout.NORTH);
        tableContainer.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(242, 246, 255));
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 30, 10, 30));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setBackground(new Color(242, 246, 255));

        JButton btnEditSelected = createPrimaryButton("Edit Selected");
        JButton btnDelete = createPrimaryButton("Delete Selected");
        JButton btnRefresh = createSecondaryButton("Refresh");
        JButton btnBack = createSecondaryButton("Back to Dashboard");

        btns.add(btnEditSelected);
        btns.add(btnDelete);
        btns.add(btnRefresh);
        btns.add(btnBack);

        totalCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalCountLabel.setForeground(new Color(0, 51, 153));

        bottom.add(totalCountLabel, BorderLayout.WEST);
        bottom.add(btns, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            if (sorter != null) sorter.setRowFilter(null);
            loadStudentDataFromDB();
        });

        btnBack.addActionListener(e -> cardLayout.show(pages, "DASHBOARD"));

        btnEditSelected.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a student row to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String roll = tableModel.getValueAt(modelRow, 0).toString();
            openEditForStudent(roll);
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Select a student row first!");
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String roll = tableModel.getValueAt(modelRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete Roll No: " + roll + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteStudentFromDB(roll);
                loadStudentDataFromDB();
            }
        });

        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(tableContainer, BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private static JPanel createChangePasswordPage() {
        JPanel page = new JPanel(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(242, 246, 255));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setPreferredSize(new Dimension(420, 150));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        JPasswordField txtOld = new JPasswordField();
        JPasswordField txtNew = new JPasswordField();
        JPasswordField txtConfirm = new JPasswordField();

        form.add(new JLabel("Current Password:")); form.add(txtOld);
        form.add(new JLabel("New Password:"));     form.add(txtNew);
        form.add(new JLabel("Confirm New:"));      form.add(txtConfirm);

        JButton btnChange = createPrimaryButton("CHANGE");
        JButton btnBack = createSecondaryButton("BACK");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(new Color(242, 246, 255));
        btnPanel.add(btnChange);
        btnPanel.add(btnBack);

        btnChange.addActionListener(e -> {
            String oldP = new String(txtOld.getPassword());
            String newP = new String(txtNew.getPassword());
            String conP = new String(txtConfirm.getPassword());

            if (!oldP.equals(currentAdminPassword)) {
                JOptionPane.showMessageDialog(frame, "Current password incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentAdminPassword = newP;
            JOptionPane.showMessageDialog(frame, "Password changed successfully!");
            cardLayout.show(pages, "DASHBOARD");
        });

        btnBack.addActionListener(e -> cardLayout.show(pages, "DASHBOARD"));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(new Color(242, 246, 255));

        JLabel heading = new JLabel("Change Password");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(new Color(0, 51, 153));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        wrap.add(heading);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(form);
        wrap.add(Box.createVerticalStrut(15));
        wrap.add(btnPanel);

        center.add(wrap);
        page.add(createHeaderBanner(), BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(createFooter(), BorderLayout.SOUTH);
        return page;
    }

    private static void clearAddForm() {
        txtAddRoll.setText("");
        txtAddName.setText("");
        txtAddParent.setText("");
        txtAddPhone.setText("");
        txtAddEmail.setText("");
        cmbAddCourse.setSelectedIndex(0);
    }

    private static void attachTextOnlyListener(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                if ((!Character.isLetter(ch) && ch != ' ' && ch != KeyEvent.VK_BACK_SPACE) || ch == ',') {
                    e.consume();
                }
            }
        });
    }

    private static void attachPhoneListener(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                if (!Character.isDigit(ch) || field.getText().length() >= 10) {
                    if (ch != KeyEvent.VK_BACK_SPACE) {
                        e.consume();
                    }
                }
            }
        });
    }

    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return email != null && pat.matcher(email).matches();
    }

    private static void promptUpdate() {
        String rollStr = JOptionPane.showInputDialog(frame, "Enter Roll No to Update:");
        if (rollStr != null && !rollStr.trim().isEmpty()) {
            openEditForStudent(rollStr.trim());
        }
    }

    // =========================================================
    // DATABASE OPERATIONS (JDBC DAO)
    // =========================================================
    private static void saveStudentToDB(String roll, String name, String parent, String phone, String email, String course) throws Exception {
        String query = "INSERT INTO students (roll_no, student_name, parent_name, phone, email, course) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, roll);
            pst.setString(2, name);
            pst.setString(3, parent);
            pst.setString(4, phone);
            pst.setString(5, email);
            pst.setString(6, course);
            pst.executeUpdate();
        }
    }

    private static void loadStudentDataFromDB() {
        tableModel.setRowCount(0);
        String query = "SELECT * FROM students";
        int count = 0;
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                String roll = rs.getString("roll_no");
                String name = rs.getString("student_name");
                String parent = rs.getString("parent_name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String course = rs.getString("course");

                tableModel.addRow(new Object[]{roll, name, parent, phone, email, course});
                count++;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Database read error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        totalCountLabel.setText("Total students: " + count + "   (click column header to sort)");
    }

    private static void openEditForStudent(String roll) {
        String query = "SELECT * FROM students WHERE roll_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, roll);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    lblEditId.setText(rs.getString("roll_no"));
                    txtEditName.setText(rs.getString("student_name"));
                    txtEditParent.setText(rs.getString("parent_name"));
                    txtEditPhone.setText(rs.getString("phone"));
                    txtEditEmail.setText(rs.getString("email"));
                    cmbEditCourse.setSelectedItem(rs.getString("course"));
                    cardLayout.show(pages, "EDIT");
                } else {
                    JOptionPane.showMessageDialog(frame, "No student found with Roll No: " + roll, "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateStudentInDB(String roll, String name, String parent, String phone, String email, String course) throws Exception {
        String query = "UPDATE students SET student_name = ?, parent_name = ?, phone = ?, email = ?, course = ? WHERE roll_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, name);
            pst.setString(2, parent);
            pst.setString(3, phone);
            pst.setString(4, email);
            pst.setString(5, course);
            pst.setString(6, roll);
            pst.executeUpdate();
        }
    }

    private static void deleteStudentFromDB(String roll) {
        String query = "DELETE FROM students WHERE roll_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, roll);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Student deleted successfully!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Delete error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void logout() {
        topNavBar.setVisible(false);
        cardLayout.show(pages, "LOGIN");
    }

    private static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0, 51, 153));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(0, 51, 153));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 153), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}