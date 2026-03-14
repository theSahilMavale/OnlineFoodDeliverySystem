import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class OnlineFoodDelivery {

    public static void main(String[] args) {
        new LoginPage();
    }
}

class DBConnection {

    static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/fooddelivery",
                    "root",
                    "YOUR_PASSWORD"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

class LoginPage extends JFrame implements ActionListener {

    JTextField user;
    JPasswordField pass;
    JButton login;

    LoginPage() {

        setTitle("Online Food Delivery");
        setSize(400,300);
        setLayout(null);

        getContentPane().setBackground(new Color(255,180,120));

        JLabel l1=new JLabel("Username");
        l1.setBounds(50,80,100,30);
        add(l1);

        user=new JTextField();
        user.setBounds(150,80,150,30);
        add(user);

        JLabel l2=new JLabel("Password");
        l2.setBounds(50,130,100,30);
        add(l2);

        pass=new JPasswordField();
        pass.setBounds(150,130,150,30);
        add(pass);

        login=new JButton("Login");
        login.setBounds(150,190,100,35);
        add(login);

        login.addActionListener(this);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        try {

            Connection con=DBConnection.getConnection();

            PreparedStatement ps=con.prepareStatement(
                    "select * from users where username=? and password=?"
            );

            ps.setString(1,user.getText());
            ps.setString(2,pass.getText());

            ResultSet rs=ps.executeQuery();

            if(rs.next()){
                new MenuPage();
                dispose();
            }
            else{
                JOptionPane.showMessageDialog(this,"Invalid Login");
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

class MenuPage extends JFrame implements ActionListener {

    JButton pizza,burger,pasta,view;

    MenuPage(){

        setTitle("Food Menu");
        setSize(500,420);
        setLayout(null);

        getContentPane().setBackground(new Color(255,210,140));

        JLabel title=new JLabel("Select Your Food");
        title.setFont(new Font("Arial",Font.BOLD,18));
        title.setBounds(170,30,200,30);
        add(title);

        pizza=new JButton("Pizza - ₹200");
        pizza.setBounds(150,100,200,40);
        add(pizza);

        burger=new JButton("Burger - ₹120");
        burger.setBounds(150,160,200,40);
        add(burger);

        pasta=new JButton("Pasta - ₹150");
        pasta.setBounds(150,220,200,40);
        add(pasta);

        view=new JButton("View Orders");
        view.setBounds(150,280,200,40);
        add(view);

        pizza.addActionListener(this);
        burger.addActionListener(this);
        pasta.addActionListener(this);
        view.addActionListener(this);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e){

        if(e.getSource()==pizza)
            new OrderPage("Pizza",200);

        if(e.getSource()==burger)
            new OrderPage("Burger",120);

        if(e.getSource()==pasta)
            new OrderPage("Pasta",150);

        if(e.getSource()==view)
            new ViewOrders();
    }
}

class OrderPage extends JFrame implements ActionListener {

    JTextField qty;
    JButton order;
    String food;
    int price;

    OrderPage(String food,int price){

        this.food=food;
        this.price=price;

        setTitle("Place Order");
        setSize(400,300);
        setLayout(null);

        getContentPane().setBackground(new Color(255,200,150));

        JLabel f=new JLabel("Food : "+food);
        f.setBounds(120,60,200,30);
        add(f);

        JLabel q=new JLabel("Quantity");
        q.setBounds(60,120,100,30);
        add(q);

        qty=new JTextField();
        qty.setBounds(150,120,100,30);
        add(qty);

        order=new JButton("Order Now");
        order.setBounds(130,180,120,40);
        add(order);

        order.addActionListener(this);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e){

        try{

            int quantity=Integer.parseInt(qty.getText());
            int total=quantity*price;

            Connection con=DBConnection.getConnection();

            PreparedStatement ps=con.prepareStatement(
                    "insert into orders(food_name,quantity,price,total) values(?,?,?,?)"
            );

            ps.setString(1,food);
            ps.setInt(2,quantity);
            ps.setInt(3,price);
            ps.setInt(4,total);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"Order Saved In Database");

            new BillPage(food,quantity,total);

            dispose();

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}

class BillPage extends JFrame {

    BillPage(String food,int qty,int total){

        setTitle("Bill");
        setSize(400,300);
        setLayout(null);

        getContentPane().setBackground(new Color(255,230,180));

        JLabel l1=new JLabel("Food : "+food);
        l1.setBounds(120,80,200,30);
        add(l1);

        JLabel l2=new JLabel("Quantity : "+qty);
        l2.setBounds(120,120,200,30);
        add(l2);

        JLabel l3=new JLabel("Total : ₹"+total);
        l3.setBounds(120,160,200,30);
        add(l3);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}

class ViewOrders extends JFrame {

    JTable table;
    DefaultTableModel model;

    ViewOrders(){

        setTitle("Previous Orders");
        setSize(500,400);
        setLayout(new BorderLayout());

        model=new DefaultTableModel();
        table=new JTable(model);

        model.addColumn("Order ID");
        model.addColumn("Food");
        model.addColumn("Quantity");
        model.addColumn("Price");
        model.addColumn("Total");

        add(new JScrollPane(table),BorderLayout.CENTER);

        loadOrders();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    void loadOrders(){

        try{

            Connection con=DBConnection.getConnection();

            Statement st=con.createStatement();

            ResultSet rs=st.executeQuery("select * from orders");

            while(rs.next()){

                model.addRow(new Object[]{
                        rs.getInt("order_id"),
                        rs.getString("food_name"),
                        rs.getInt("quantity"),
                        rs.getInt("price"),
                        rs.getInt("total")
                });

            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}