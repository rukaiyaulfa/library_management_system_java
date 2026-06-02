package library_management_system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryManagementGUI extends JFrame {

    // কালার এবং ফন্ট সেটআপ
    private final Color COLOR_BG = new Color(18, 18, 18);
    private final Color COLOR_PANEL = new Color(30, 30, 30);
    private final Color COLOR_ACCENT = new Color(138, 43, 226);
    private final Color COLOR_ACCENT_HOVER = new Color(153, 50, 204);
    private final Color COLOR_TEXT = new Color(230, 230, 230);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.BOLD, 15);

    // -------------------- ডেটাবেস ক্লাস (ছোট লাইনে ভাঙা) --------------------
    static class Book implements Serializable {
        int id; 
        String title; 
        String author; 
        boolean issued; 
        int issuedToMemberId;

        public Book(int id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.issued = false;
            this.issuedToMemberId = -1;
        }
    }

    static class Member implements Serializable {
        int id; 
        String name; 
        String password;

        public Member(int id, String name, String password) {
            this.id = id;
            this.name = name;
            this.password = password;
        }
    }

    static class Admin implements Serializable {
        int id; 
        String name; 
        String password; 
        boolean isMainAdmin;

        public Admin(int id, String name, String password, boolean isMainAdmin) {
            this.id = id;
            this.name = name;
            this.password = password;
            this.isMainAdmin = isMainAdmin;
        }
    }

    static class IssueRequest implements Serializable {
        int bookId; 
        int memberId; 
        String bookTitle; 
        String memberName;

        public IssueRequest(int bId, int mId, String bTitle, String mName) {
            this.bookId = bId;
            this.memberId = mId;
            this.bookTitle = bTitle;
            this.memberName = mName;
        }
    }

    static class Library implements Serializable {
        List<Book> books = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        List<Admin> admins = new ArrayList<>();
        List<IssueRequest> requests = new ArrayList<>();
        private final String DATA_FILE = "library_data_final.dat";

        @SuppressWarnings("unchecked")
        public void loadData() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                books = (List<Book>) ois.readObject();
                members = (List<Member>) ois.readObject();
                admins = (List<Admin>) ois.readObject();
                requests = (List<IssueRequest>) ois.readObject();
            } catch (Exception ignored) {
            }
            
            boolean hasMain = false;
            for (Admin a : admins) {
                if (a.isMainAdmin) {
                    hasMain = true;
                }
            }
            
            if (!hasMain) {
                admins.add(new Admin(1001, "Rukaiya Rafiq Ulfa", "ulfa123", true));
            }
        }

        public void saveData() {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                oos.writeObject(books);
                oos.writeObject(members);
                oos.writeObject(admins);
                oos.writeObject(requests);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------- ভেরিয়েবল ডিক্লেয়ারেশন --------------------
    private Library library;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private int loggedInUserId = -1;
    private int loggedInAdminId = -1;

    private DefaultTableModel adminBookTableModel;
    private DefaultTableModel adminMemberTableModel;
    private DefaultTableModel adminRequestTableModel;
    private DefaultTableModel adminListTableModel;
    private DefaultTableModel userBookTableModel;

    // -------------------- মেইন কনস্ট্রাক্টর --------------------
    public LibraryManagementGUI() {
        library = new Library();
        library.loadData();
        setupGlobalTheme();

        setTitle("CENTRAL LIBRARY | Modern Library System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(COLOR_BG);

        mainPanel.add(createHomePagePanel(), "HomePage");
        mainPanel.add(createAdminLoginPanel(), "AdminLogin");
        mainPanel.add(createUserLoginPanel(), "UserLogin");
        mainPanel.add(createUserRegisterPanel(), "UserRegister");
        mainPanel.add(createAdminDashboard(), "AdminDashboard");
        mainPanel.add(createUserDashboard(), "UserDashboard");

        add(mainPanel);
        cardLayout.show(mainPanel, "HomePage");
    }

    // -------------------- থিম এবং বাটন ডিজাইন --------------------
    private void setupGlobalTheme() {
        UIManager.put("Panel.background", COLOR_BG);
        UIManager.put("OptionPane.background", COLOR_PANEL);
        UIManager.put("OptionPane.messageForeground", COLOR_TEXT);
        UIManager.put("TabbedPane.background", Color.WHITE);
        UIManager.put("TabbedPane.foreground", Color.BLACK);
        UIManager.put("TabbedPane.selected", new Color(200, 200, 200));
    }

    private JButton createModernButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(220, 220, 220));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    private JTextField createModernTextField() {
        JTextField tf = new JTextField(15);
        tf.setFont(MAIN_FONT);
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setCaretColor(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 2),
                new EmptyBorder(5, 10, 5, 10)));
        return tf;
    }

    private JPasswordField createModernPasswordField() {
        JPasswordField pf = new JPasswordField(15);
        pf.setFont(MAIN_FONT);
        pf.setBackground(Color.WHITE);
        pf.setForeground(Color.BLACK);
        pf.setCaretColor(Color.BLACK);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 2),
                new EmptyBorder(5, 10, 5, 10)));
        return pf;
    }

    private JScrollPane styleTable(JTable table) {
        table.setBackground(COLOR_PANEL);
        table.setForeground(COLOR_TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setGridColor(new Color(60, 60, 60));
        table.setSelectionBackground(COLOR_ACCENT);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(COLOR_ACCENT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(COLOR_PANEL);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
        return scroll;
    }

    // -------------------- প্যানেল ডিজাইন --------------------
    private JPanel createHomePagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);

        JLabel title = new JLabel("<html><center><h1 style='color:#9b59b6; margin-top:50px; font-family:Segoe UI;'>CENTRAL LIBRARY</h1></center></html>", JLabel.CENTER);
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 150));
        btnPanel.setBackground(COLOR_BG);

        JButton btnAdmin = createModernButton("✦ ADMIN PORTAL");
        JButton btnUser = createModernButton("✧ USER PORTAL");
        JButton btnExit = createModernButton("✖ SYSTEM EXIT");

        btnAdmin.setPreferredSize(new Dimension(240, 60));
        btnUser.setPreferredSize(new Dimension(240, 60));
        btnExit.setPreferredSize(new Dimension(240, 60));

        btnAdmin.addActionListener(e -> cardLayout.show(mainPanel, "AdminLogin"));
        btnUser.addActionListener(e -> cardLayout.show(mainPanel, "UserLogin"));
        btnExit.addActionListener(e -> {
            library.saveData();
            System.exit(0);
        });

        btnPanel.add(btnAdmin);
        btnPanel.add(btnUser);
        btnPanel.add(btnExit);
        panel.add(btnPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdminLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 20));
        form.setBackground(COLOR_PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 2),
                new EmptyBorder(30, 30, 30, 30)));

        JTextField idField = createModernTextField();
        JPasswordField passField = createModernPasswordField();
        JButton btnLogin = createModernButton("AUTHENTICATE");
        JButton btnBack = createModernButton("BACK");

        JLabel lblId = new JLabel("ADMIN ID:");
        lblId.setForeground(COLOR_TEXT);
        JLabel lblPass = new JLabel("PASSWORD:");
        lblPass.setForeground(COLOR_TEXT);

        form.add(lblId);
        form.add(idField);
        form.add(lblPass);
        form.add(passField);
        form.add(btnBack);
        form.add(btnLogin);

        btnLogin.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String pass = new String(passField.getPassword());
                boolean success = false;
                for (Admin a : library.admins) {
                    if (a.id == id && a.password.equals(pass)) {
                        loggedInAdminId = id;
                        success = true;
                        idField.setText("");
                        passField.setText("");
                        refreshAdminTables();
                        cardLayout.show(mainPanel, "AdminDashboard");
                        break;
                    }
                }
                if (!success) {
                    JOptionPane.showMessageDialog(this, "❌ Access Denied!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid Format!");
            }
        });
        
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "HomePage"));
        panel.add(form);
        return panel;
    }

    private JPanel createUserLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 15));
        form.setBackground(COLOR_PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 2),
                new EmptyBorder(30, 30, 30, 30)));

        JTextField idField = createModernTextField();
        JPasswordField passField = createModernPasswordField();
        JButton btnLogin = createModernButton("LOGIN");
        JButton btnRegister = createModernButton("SIGN UP");
        JButton btnBack = createModernButton("BACK");

        JLabel lblId = new JLabel("MEMBER ID:");
        lblId.setForeground(COLOR_TEXT);
        JLabel lblPass = new JLabel("PASSWORD:");
        lblPass.setForeground(COLOR_TEXT);

        form.add(lblId);
        form.add(idField);
        form.add(lblPass);
        form.add(passField);
        form.add(btnRegister);
        form.add(btnLogin);
        form.add(new JLabel(""));
        form.add(btnBack);

        btnLogin.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String pass = new String(passField.getPassword());
                boolean found = false;
                for (Member m : library.members) {
                    if (m.id == id && m.password.equals(pass)) {
                        loggedInUserId = id;
                        found = true;
                        idField.setText("");
                        passField.setText("");
                        refreshUserTables();
                        cardLayout.show(mainPanel, "UserDashboard");
                        break;
                    }
                }
                if (!found) {
                    JOptionPane.showMessageDialog(this, "❌ Account not found!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid ID format!");
            }
        });
        
        btnRegister.addActionListener(e -> cardLayout.show(mainPanel, "UserRegister"));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "HomePage"));
        panel.add(form);
        return panel;
    }

    private JPanel createUserRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 15));
        form.setBackground(COLOR_PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 2),
                new EmptyBorder(30, 30, 30, 30)));

        JTextField idField = createModernTextField();
        JTextField nameField = createModernTextField();
        JPasswordField passField = createModernPasswordField();
        JButton btnSubmit = createModernButton("REGISTER");
        JButton btnBack = createModernButton("BACK");

        JLabel lblId = new JLabel("SET ID:");
        lblId.setForeground(COLOR_TEXT);
        JLabel lblName = new JLabel("FULL NAME:");
        lblName.setForeground(COLOR_TEXT);
        JLabel lblPass = new JLabel("SET PASS:");
        lblPass.setForeground(COLOR_TEXT);

        form.add(lblId);
        form.add(idField);
        form.add(lblName);
        form.add(nameField);
        form.add(lblPass);
        form.add(passField);
        form.add(btnBack);
        form.add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                String pass = new String(passField.getPassword()).trim();

                if (name.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "❌ Form incomplete!");
                    return;
                }
                for (Member m : library.members) {
                    if (m.id == id) {
                        JOptionPane.showMessageDialog(this, "❌ ID taken!");
                        return;
                    }
                }

                library.members.add(new Member(id, name, pass));
                library.saveData();
                JOptionPane.showMessageDialog(this, "✅ Successfully Registered!");
                idField.setText("");
                nameField.setText("");
                passField.setText("");
                cardLayout.show(mainPanel, "UserLogin");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid ID format!");
            }
        });
        
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "UserLogin"));
        panel.add(form);
        return panel;
    }

    private JPanel createAdminDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_BG);
        JLabel header = new JLabel("<html><h2 style='color:#9b59b6;'>✦ COMMAND CENTER</h2></html>");
        JButton btnLogout = createModernButton("LOGOUT");
        
        btnLogout.addActionListener(e -> {
            library.saveData();
            loggedInAdminId = -1;
            cardLayout.show(mainPanel, "HomePage");
        });
        
        topPanel.add(header, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(Color.BLACK);

        adminBookTableModel = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Status"}, 0);
        tabs.addTab("📖 Books", styleTable(new JTable(adminBookTableModel)));

        adminMemberTableModel = new DefaultTableModel(new String[]{"Member ID", "Name", "Password"}, 0);
        tabs.addTab("👥 Members", styleTable(new JTable(adminMemberTableModel)));

        adminRequestTableModel = new DefaultTableModel(new String[]{"Book ID", "Book Title", "User ID", "User Name"}, 0);
        tabs.addTab("🔔 Requests", styleTable(new JTable(adminRequestTableModel)));

        adminListTableModel = new DefaultTableModel(new String[]{"Admin ID", "Admin Name", "Privilege"}, 0);
        tabs.addTab("🛡️ Admins", styleTable(new JTable(adminListTableModel)));

        panel.add(tabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        bottomPanel.setBackground(COLOR_BG);

        JButton btnAddBook = createModernButton("ADD BOOK");
        JButton btnAddMember = createModernButton("ADD MEMBER");
        JButton btnAddAdmin = createModernButton("ADD ADMIN");
        JButton btnHome = createModernButton("HOME");
        JButton btnIssue = createModernButton("ISSUE BOOK");
        JButton btnReturn = createModernButton("RETURN BOOK");
        JButton btnApprove = createModernButton("APPROVE REQ");

        btnAddBook.addActionListener(e -> addBookAction());
        btnAddMember.addActionListener(e -> addMemberAction());
        btnAddAdmin.addActionListener(e -> addAdminAction());
        btnIssue.addActionListener(e -> issueBookAction());
        btnReturn.addActionListener(e -> returnBookAction());
        btnApprove.addActionListener(e -> approveRequestAction());
        btnHome.addActionListener(e -> {
            library.saveData();
            loggedInAdminId = -1;
            cardLayout.show(mainPanel, "HomePage");
        });

        bottomPanel.add(btnAddBook);
        bottomPanel.add(btnAddMember);
        bottomPanel.add(btnAddAdmin);
        bottomPanel.add(btnHome);
        bottomPanel.add(btnIssue);
        bottomPanel.add(btnReturn);
        bottomPanel.add(btnApprove);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createUserDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(COLOR_BG);
        JLabel header = new JLabel("<html><h2 style='color:#3498db;'>✧ MEMBER VIEW</h2></html>");
        JButton btnLogout = createModernButton("LOGOUT");
        
        btnLogout.addActionListener(e -> {
            loggedInUserId = -1;
            library.saveData();
            cardLayout.show(mainPanel, "HomePage");
        });
        
        topPanel.add(header, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        userBookTableModel = new DefaultTableModel(new String[]{"Book ID", "Title", "Author", "Status"}, 0);
        panel.add(styleTable(new JTable(userBookTableModel)), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(COLOR_BG);

        JButton btnRequest = createModernButton("APPLY FOR BOOK");
        JButton btnHome = createModernButton("HOME");

        btnRequest.addActionListener(e -> requestIssueAction());
        btnHome.addActionListener(e -> {
            library.saveData();
            loggedInUserId = -1;
            cardLayout.show(mainPanel, "HomePage");
        });

        bottomPanel.add(btnRequest);
        bottomPanel.add(btnHome);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // -------------------- কাজের মেথডগুলো --------------------
    private void setDialogColor() {
        UIManager.put("Panel.background", COLOR_PANEL);
        UIManager.put("OptionPane.background", COLOR_PANEL);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
    }

    private void addBookAction() {
        setDialogColor();
        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        p.setBackground(COLOR_PANEL);
        JTextField idF = createModernTextField();
        JTextField titleF = createModernTextField();
        JTextField authorF = createModernTextField();
        
        JLabel l1 = new JLabel("Book ID:");
        l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Title:");
        l2.setForeground(Color.WHITE);
        JLabel l3 = new JLabel("Author:");
        l3.setForeground(Color.WHITE);
        
        p.add(l1); p.add(idF);
        p.add(l2); p.add(titleF);
        p.add(l3); p.add(authorF);

        if (JOptionPane.showConfirmDialog(this, p, "Add Book", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idF.getText().trim());
                String title = titleF.getText().trim();
                String author = authorF.getText().trim();
                
                if (title.isEmpty() || author.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "❌ Incomplete details.");
                    return;
                }
                for (Book b : library.books) {
                    if (b.id == id) {
                        JOptionPane.showMessageDialog(this, "❌ ID Already Exists.");
                        return;
                    }
                }
                
                library.books.add(new Book(id, title, author));
                library.saveData();
                refreshAdminTables();
                JOptionPane.showMessageDialog(this, "✅ Book Registered.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid ID.");
            }
        }
    }

    private void addMemberAction() {
        setDialogColor();
        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        p.setBackground(COLOR_PANEL);
        JTextField idF = createModernTextField();
        JTextField nameF = createModernTextField();
        JTextField passF = createModernTextField();
        
        JLabel l1 = new JLabel("Member ID:");
        l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Name:");
        l2.setForeground(Color.WHITE);
        JLabel l3 = new JLabel("Password:");
        l3.setForeground(Color.WHITE);
        
        p.add(l1); p.add(idF);
        p.add(l2); p.add(nameF);
        p.add(l3); p.add(passF);

        if (JOptionPane.showConfirmDialog(this, p, "Add Member", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idF.getText().trim());
                String name = nameF.getText().trim();
                String pass = passF.getText().trim();
                
                if (name.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "❌ Incomplete details.");
                    return;
                }
                for (Member m : library.members) {
                    if (m.id == id) {
                        JOptionPane.showMessageDialog(this, "❌ ID Already Exists.");
                        return;
                    }
                }
                
                library.members.add(new Member(id, name, pass));
                library.saveData();
                refreshAdminTables();
                JOptionPane.showMessageDialog(this, "✅ Member Added.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Invalid ID.");
            }
        }
    }

    private void addAdminAction() {
        boolean isMain = false;
        for (Admin a : library.admins) {
            if (a.id == loggedInAdminId && a.isMainAdmin) {
                isMain = true;
            }
        }
        
        if (!isMain) {
            JOptionPane.showMessageDialog(this, "❌ Access Denied! Only Main Admin can do this.");
            return;
        }

        setDialogColor();
        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        p.setBackground(COLOR_PANEL);
        JTextField idF = createModernTextField();
        JTextField nameF = createModernTextField();
        JTextField passF = createModernTextField();
        
        JLabel l1 = new JLabel("New Admin ID:");
        l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel("Name:");
        l2.setForeground(Color.WHITE);
        JLabel l3 = new JLabel("Password:");
        l3.setForeground(Color.WHITE);
        
        p.add(l1); p.add(idF);
        p.add(l2); p.add(nameF);
        p.add(l3); p.add(passF);

        if (JOptionPane.showConfirmDialog(this, p, "Register Admin", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idF.getText().trim());
                String name = nameF.getText().trim();
                String pass = passF.getText().trim();
                
                if (name.isEmpty() || pass.isEmpty()) return;
                for (Admin a : library.admins) {
                    if (a.id == id) return;
                }
                
                library.admins.add(new Admin(id, name, pass, false));
                library.saveData();
                refreshAdminTables();
                JOptionPane.showMessageDialog(this, "✅ Admin Created.");
            } catch (Exception ex) {
            }
        }
    }

    private void issueBookAction() {
        setDialogColor();
        String bStr = JOptionPane.showInputDialog(this, "Enter Book ID:");
        if (bStr == null) return;
        
        String mStr = JOptionPane.showInputDialog(this, "Enter Member ID:");
        if (mStr == null) return;
        
        try {
            int bId = Integer.parseInt(bStr.trim());
            int mId = Integer.parseInt(mStr.trim());
            boolean memExists = false;
            
            for (Member m : library.members) {
                if (m.id == mId) {
                    memExists = true;
                }
            }
            
            if (!memExists) {
                JOptionPane.showMessageDialog(this, "❌ Member not found!");
                return;
            }

            for (Book b : library.books) {
                if (b.id == bId) {
                    if (b.issued) {
                        JOptionPane.showMessageDialog(this, "❌ Book already issued.");
                        return;
                    }
                    b.issued = true;
                    b.issuedToMemberId = mId;
                    library.saveData();
                    refreshAdminTables();
                    JOptionPane.showMessageDialog(this, "✅ Handover Done.");
                    return;
                }
            }
        } catch (Exception ex) {
        }
    }

    private void returnBookAction() {
        setDialogColor();
        String bStr = JOptionPane.showInputDialog(this, "Enter Book ID to Restore:");
        if (bStr == null) return;
        
        try {
            int bId = Integer.parseInt(bStr.trim());
            for (Book b : library.books) {
                if (b.id == bId) {
                    if (!b.issued) {
                        JOptionPane.showMessageDialog(this, "❌ Not issued.");
                        return;
                    }
                    b.issued = false;
                    b.issuedToMemberId = -1;
                    library.saveData();
                    refreshAdminTables();
                    JOptionPane.showMessageDialog(this, "✅ Returned successfully.");
                    return;
                }
            }
        } catch (Exception ex) {
        }
    }

    private void approveRequestAction() {
        setDialogColor();
        if (library.requests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No queue entries.");
            return;
        }
        
        String bStr = JOptionPane.showInputDialog(this, "Enter Book ID to Approve:");
        if (bStr == null) return;
        
        try {
            int bId = Integer.parseInt(bStr.trim());
            IssueRequest targetReq = null;
            
            for (IssueRequest req : library.requests) {
                if (req.bookId == bId) {
                    targetReq = req;
                    break;
                }
            }
            
            if (targetReq != null) {
                for (Book b : library.books) {
                    if (b.id == bId) {
                        if (b.issued) {
                            JOptionPane.showMessageDialog(this, "❌ Book occupied.");
                            return;
                        }
                        b.issued = true;
                        b.issuedToMemberId = targetReq.memberId;
                        library.requests.remove(targetReq);
                        library.saveData();
                        refreshAdminTables();
                        JOptionPane.showMessageDialog(this, "✅ Authorized!");
                        return;
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    private void requestIssueAction() {
        setDialogColor();
        String bStr = JOptionPane.showInputDialog(this, "Enter Book ID to Apply:");
        if (bStr == null) return;
        
        try {
            int bId = Integer.parseInt(bStr.trim());
            Book targetBook = null;
            
            for (Book b : library.books) {
                if (b.id == bId) {
                    targetBook = b;
                }
            }
            
            if (targetBook == null) {
                JOptionPane.showMessageDialog(this, "❌ Book ID error!");
                return;
            }
            if (targetBook.issued) {
                JOptionPane.showMessageDialog(this, "❌ Out of stock.");
                return;
            }
            for (IssueRequest req : library.requests) {
                if (req.bookId == bId) {
                    JOptionPane.showMessageDialog(this, "❌ Already requested.");
                    return;
                }
            }
            
            String mName = "";
            for (Member m : library.members) {
                if (m.id == loggedInUserId) {
                    mName = m.name;
                }
            }
            
            library.requests.add(new IssueRequest(bId, loggedInUserId, targetBook.title, mName));
            library.saveData();
            JOptionPane.showMessageDialog(this, "✅ Application forwarded.");
        } catch (Exception ex) {
        }
    }

    // -------------------- টেবিল রিফ্রেশ --------------------
    private void refreshAdminTables() {
        adminBookTableModel.setRowCount(0);
        for (Book b : library.books) {
            String status = b.issued ? "Issued to " + b.issuedToMemberId : "Available";
            adminBookTableModel.addRow(new Object[]{b.id, b.title, b.author, status});
        }
        
        adminMemberTableModel.setRowCount(0);
        for (Member m : library.members) {
            adminMemberTableModel.addRow(new Object[]{m.id, m.name, m.password});
        }
        
        adminRequestTableModel.setRowCount(0);
        for (IssueRequest r : library.requests) {
            adminRequestTableModel.addRow(new Object[]{r.bookId, r.bookTitle, r.memberId, r.memberName});
        }
        
        adminListTableModel.setRowCount(0);
        for (Admin a : library.admins) {
            String role = a.isMainAdmin ? "Main Admin" : "Co-Admin";
            adminListTableModel.addRow(new Object[]{a.id, a.name, role});
        }
    }

    private void refreshUserTables() {
        userBookTableModel.setRowCount(0);
        for (Book b : library.books) {
            String status = b.issued ? "Unavailable" : "Available";
            userBookTableModel.addRow(new Object[]{b.id, b.title, b.author, status});
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new LibraryManagementGUI().setVisible(true));
    }
}