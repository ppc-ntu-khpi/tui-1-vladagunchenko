package com.mybank.tui;

import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

// Імпорти з MyBank.jar для роботи з даними банку
import com.mybank.domain.Bank;
import com.mybank.domain.Customer;
import com.mybank.domain.Account;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.SavingsAccount;
import com.mybank.data.DataSource;

/**
 *
 * @author Alexander 'Taurus' Babich
 * @modified by Vladislava Hunchenko
 */
public class TUIdemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUST_INFO = 2010;

    public static void main(String[] args) throws Exception {
        // Перед запуском інтерфейсу завантажуємо дані з файлу test.dat
        try {
            DataSource dataSource = new DataSource("data/test.dat");
            dataSource.loadData();
        } catch (Exception e) {
            System.out.println("Помилка завантаження даних банку: " + e.getMessage());
            // Резервне створення клієнтів, якщо файл не знайдено
            initializeFallbackData();
        }

        TUIdemo tdemo = new TUIdemo();
        (new Thread(tdemo)).start();
    }

   public TUIdemo() throws Exception {
    super(System.in, System.out);

        addToolMenu();
        
        // Створення кастомного меню 'File'
        TMenu fileMenu = addMenu("&File");
        fileMenu.addItem(CUST_INFO, "&Customer Info");
        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);

        addWindowMenu();

        // Створення кастомного меню 'Help'
        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(ABOUT_APP, "&About...");

        setFocusFollowsMouse(true);
        
        // Відкриваємо вікно деталей клієнта автоматично при старті
        ShowCustomerDetails();
    }

    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About", "\t\t\t\t\t   Just a simple Jexer demo.\n\nCopyright \u00A9 2019 Alexander \'Taurus\' Babich").show();
            return true;
        }
        if (menu.getId() == CUST_INFO) {
            ShowCustomerDetails();
            return true;
        }
        return super.onMenu(menu);
    }

    private void ShowCustomerDetails() {
        TWindow custWin = addWindow("Customer Window", 2, 1, 40, 12, TWindow.NOZOOMBOX);
        custWin.newStatusBar("Enter valid customer number and press Show...");

        custWin.addLabel("Enter customer number: ", 2, 2);
        TField custNo = custWin.addField(24, 2, 3, false);
        
        // Текстове поле для виведення інформації про клієнта
        TText details = custWin.addText("Owner Name: \nAccount Type: \nAccount Balance: ", 2, 4, 36, 6);
        
        custWin.addButton("&Show", 28, 2, new TAction() {
            @Override
            public void DO() {
                try {
                    int custNum = Integer.parseInt(custNo.getText());
                    
                    // Перевіряємо наявність клієнта за індексом
                    if (custNum >= 0 && custNum < Bank.getNumberOfCustomers()) {
                        Customer customer = Bank.getCustomer(custNum);
                        
                        String name = customer.getLastName() + ", " + customer.getFirstName();
                        String accType = "No Account";
                        String balance = "0.00";
                        
                        // Перевіряємо перший рахунок клієнта
                        if (customer.getNumberOfAccounts() > 0) {
                            Account account = customer.getAccount(0);
                            balance = String.format("%.2f", account.getBalance());
                            
                            // Визначаємо тип рахунку через instanceof
                            if (account instanceof CheckingAccount) {
                                accType = "Checking";
                            } else if (account instanceof SavingsAccount) {
                                accType = "Savings";
                            } else {
                                accType = "General Account";
                            }
                        }
                        
                        // Оновлюємо інформацію на екрані (ЗАМІТКУ ПРИБРАНО)
                        details.setText("Owner Name: " + name + " (id=" + custNum + ")\n" +
                                        "Account Type: " + accType + "\n" +
                                        "Account Balance: $" + balance);
                    } else {
                        messageBox("Error", "Customer ID out of bounds! Total customers: " + Bank.getNumberOfCustomers()).show();
                    }
                    
                } catch (NumberFormatException e) {
                    messageBox("Error", "You must provide a valid integer customer number!").show();
                } catch (Exception e) {
                    messageBox("Error", "An error occurred: " + e.getMessage()).show();
                }
            }
        });
    }

    // Резервний метод створення клієнтів (якщо test.dat чомусь не завантажився)
    private static void initializeFallbackData() {
        if (Bank.getNumberOfCustomers() == 0) {
            Bank.addCustomer("Jane", "Simms");
            Customer jane = Bank.getCustomer(0);
            jane.addAccount(new CheckingAccount(200.00, 500.00)); 

            Bank.addCustomer("Owen", "Bryant");
            Customer owen = Bank.getCustomer(1);
            owen.addAccount(new SavingsAccount(500.00, 0.05));
        }
    }
}