import java.sql.*;
import java.util.*;

public class BankingManagement {
    private static final String url = "jdbc:mysql://localhost:3306/bankdb";
    private static final String username = "root";
    private static final String password = "123456789";

    public static void main(String[] args) {
        int choice = 0;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Scanner sc = new Scanner(System.in);
            Statement statement = connection.createStatement();

            do {
                printMenu();
                choice = Integer.parseInt(sc.nextLine().trim());
                switch (choice) {
                    case 1:
                        showCustomers(statement);
                        break;
                    case 2:
                        addCustomer(statement, sc);
                        break;
                    case 3:
                        deleteCustomer(statement, sc);
                        break;
                    case 4:
                        updateCustomer(statement, sc);
                        break;
                    case 5:
                        showAccountDetails(statement, sc);
                        break;
                    case 6:
                        showLoanDetails(statement, sc);
                        break;
                    case 7:
                        depositMoney(statement, sc);
                        break;
                    case 8:
                        withdrawMoney(statement, sc);
                        break;
                    case 9:
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Wrong choice—please pick 1 through 9.");
                }
            } while (choice != 9);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void withdrawMoney(Statement statement, Scanner sc) throws SQLException {
        System.out.print("Enter account_no: ");
        String wdrAcc = sc.nextLine().trim();
        System.out.print("Enter amount to withdraw: ");
        double wdrAmt = Double.parseDouble(sc.nextLine().trim());
        // check balance first
        ResultSet chk = statement.executeQuery("SELECT balance FROM account WHERE account_no='" + wdrAcc + "'");
        if (chk.next() && chk.getDouble("balance") < wdrAmt) {
            System.out.println("Insufficient funds!");
        } else {
            statement.executeUpdate("UPDATE account SET balance = balance - " + wdrAmt + " WHERE account_no='" + wdrAcc + "'");
            System.out.println("Withdrawal successful.");
        }
    }

    private static void depositMoney(Statement statement, Scanner sc) throws SQLException {
        System.out.print("Enter account_no: ");
        String depAcc = sc.nextLine().trim();
        System.out.print("Enter amount to deposit: ");
        double depAmt = Double.parseDouble(sc.nextLine().trim());
        statement.executeUpdate("UPDATE account SET balance = balance + " + depAmt + " WHERE account_no='" + depAcc + "'");
        System.out.println("Deposit successful.");
    }

    private static void showLoanDetails(Statement statement, Scanner sc) throws SQLException {
        System.out.print("Enter cust_no: ");
        String loanCust = sc.nextLine().trim();
        ResultSet rs6 = statement.executeQuery("SELECT l.loan_no, l.amount, " + "b.branch_code, b.branch_name, b.branch_city AS branch_city " + "FROM loan l " + "JOIN branch b ON l.branch_code=b.branch_code " + "WHERE l.cust_no='" + loanCust + "'");
        boolean anyLoan = false;
        System.out.println("Loan_No | Amount    | Br_Code | Br_Name       | City");
        System.out.println("--------+-----------+---------+---------------+-----");
        while (rs6.next()) {
            anyLoan = true;
            System.out.printf("%-7s | %9.2f | %-7s | %-13s | %s\n", rs6.getString("loan_no"), rs6.getDouble("amount"), rs6.getString("branch_code"), rs6.getString("branch_name"), rs6.getString("branch_city"));
        }
        if (!anyLoan) {
            System.out.println("Congratulations! No loans for this customer.");
        }
    }

    private static void showAccountDetails(Statement statement, Scanner sc) throws SQLException {
        System.out.print("Enter cust_no: ");
        String accCust = sc.nextLine().trim();
        String query = "SELECT a.account_no, a.type, a.balance, " +
                "       b.branch_code, b.branch_name, b.branch_city " +
                "FROM account a " +
                "JOIN depositor d ON a.account_no = d.account_no " +
                "JOIN branch b    ON a.branch_code = b.branch_code " +
                "WHERE d.cust_no = '" + accCust + "'";
        ResultSet rs5 = statement.executeQuery(query);
        System.out.println("Acc_No | Type | Balance   | Br_Code | Br_Name       | City");
        System.out.println("-------+------+-----------+---------+---------------+-----");
        while (rs5.next()) {
            System.out.printf("%-6s | %-4s | %9.2f | %-7s | %-13s | %s\n", rs5.getString("account_no"), rs5.getString("type"), rs5.getDouble("balance"), rs5.getString("branch_code"), rs5.getString("branch_name"), rs5.getString("branch_city"));
        }
    }

    private static void updateCustomer(Statement statement, Scanner sc) throws SQLException {
        System.out.print("Enter cust_no to update: ");
        String updNo = sc.nextLine().trim();
        System.out.println("Enter 1: For Name  2: For Phone no  3: For City");
        System.out.print("Choice: ");
        int sub = Integer.parseInt(sc.nextLine().trim());
        String col = switch (sub) {
            case 1 -> "name";
            case 2 -> "phone_no";
            case 3 -> "city";
            default -> null;
        };
        if (col == null) {
            System.out.println("Invalid sub-choice.");
        }
        System.out.print("Enter new " + col + ": ");
        String newVal = sc.nextLine().trim();
        statement.executeUpdate("UPDATE customer SET " + col + "='" + newVal + "' WHERE cust_no='" + updNo + "'");
        System.out.println("Customer updated.");
    }

    private static void deleteCustomer(Statement statement, Scanner sc) throws SQLException {
        System.out.println("Enter cust_no to delete");
        String no = sc.nextLine().trim();
        String query = "DELETE FROM customer WHERE cust_no = '" + no + "'";
        int count = statement.executeUpdate(query);
        System.out.println(count + " record(s) deleted.");
    }

    private static void addCustomer(Statement statement, Scanner sc) throws SQLException {
        System.out.println("Enter cust_no: ");
        String no = sc.nextLine().trim();
        System.out.println("Enter name: ");
        String name = sc.nextLine().trim();
        System.out.println("Enter phone_no: ");
        String ph = sc.nextLine().trim();
        System.out.println("Enter city: ");
        String city = sc.nextLine().trim();
        String query = "INSERT INTO customer VALUES ('" + no + "', '" + name + "', '" + ph + "', '" + city + "')";
        statement.executeQuery(query);
        System.out.println("Customer added.");
    }

    private static void showCustomers(Statement statement) throws SQLException {
        String query = "SELECT cust_no, name, phone_no, city FROM customer ORDER BY cust_no";
        ResultSet rs = statement.executeQuery(query);
        System.out.println("Cust_No | Name               | Phone         | City");
        System.out.println("--------+--------------------+---------------+-------");
        while (rs.next()) {
            System.out.printf("%-7s | %-18s | %-13s | %s\n", rs.getString("cust_no"), rs.getString("name"), rs.getString("phone_no"), rs.getString("city"));
        }
        rs.close();
    }

    private static void printMenu() {
        System.out.println("\n\n***** Banking Management System *****");
        System.out.println("1. Show Customer Records");
        System.out.println("3. Delete Customer Record");
        System.out.println("4. Update Customer Information");
        System.out.println("5. Show Account Details of a Customer");
        System.out.println("6. Show Loan Details of a Customer");
        System.out.println("7. Deposit Money");
        System.out.println("8. Withdraw Money");
        System.out.println("9. Exit");
        System.out.print("Enter your choice (1-9): ");
    }

}
