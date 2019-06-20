package servlet;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(urlPatterns = "/Order")
public class Order extends HttpServlet {

    @Resource(name = "jdbc/finalpossystemproject")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonReader reader=null;
        JsonObject empObj = null;

        try {
            reader = Json.createReader(request.getReader());
            empObj=reader.readObject();

            String orderid = empObj.getString("orderid");
            String customerid = empObj.getString("customerid");
            String orderdate = empObj.getString("orderdate");
            double price = Double.parseDouble(empObj.getString("price"));
            JsonObject items = empObj.getJsonObject("items");

            Connection connection = dataSource.getConnection();

            connection.setAutoCommit(false);

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO order VALUES (?,?,?,?)");
            pstm.setObject(1,orderid);
            pstm.setObject(2,customerid);
            pstm.setObject(3,orderdate);
            pstm.setObject(4,price);
            boolean value=pstm.executeUpdate()>0;

            if (value){
                response.setStatus(HttpServletResponse.SC_CREATED);
            }else{
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        }catch (JsonParsingException | NullPointerException  ex){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (Exception ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter printWriter=response.getWriter();
        response.setContentType("application/json");

        Connection connection= null;
        Statement statement = null;
        ResultSet resultSet = null;

        try{

            connection=dataSource.getConnection();

            String sql= "select * from orders";
            statement=connection.createStatement();

            resultSet=statement.executeQuery(sql);

            JsonArrayBuilder customers = Json.createArrayBuilder();

            while(resultSet.next()){

                String orderid=resultSet.getString(1);
                String customerid=resultSet.getString(2);
                String orderdate=resultSet.getString(3);
                double price=resultSet.getDouble(4);

                JsonObject customer = Json.createObjectBuilder()
                        .add("orderid", orderid)
                        .add("customerid", customerid)
                        .add("orderdate",orderdate)
                        .add("price", price)
                        .build();
                customers.add(customer);
            }

            System.out.println(customers);
            printWriter.println(customers.build().toString());
            connection.close();

        }catch (Exception ex){
            response.sendError(500, ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement pstm = connection.prepareStatement("delete from Customer where cusid=?");
            pstm.setObject(1,req.getParameter("customerid"));
            boolean value=pstm.executeUpdate()>0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JsonReader reader=null;
        JsonObject empObj = null;

        try {
            reader = Json.createReader(req.getReader());
            empObj=reader.readObject();

            String cusid = empObj.getString("customerid");
            String firstname = empObj.getString("firstname");
            String lastname = empObj.getString("lastname");
            String address = empObj.getString("address");

            System.out.println(cusid);

            Connection connection = dataSource.getConnection();
            PreparedStatement pstm = connection.prepareStatement("update Customer set firstname=?, lastname=?, address=? where cusid=?");
            pstm.setObject(1,firstname);
            pstm.setObject(2,lastname);
            pstm.setObject(3,address);
            pstm.setObject(4,cusid);
            boolean value=pstm.executeUpdate()>0;

            if (value){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        }catch (JsonParsingException | NullPointerException  ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (Exception ex){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
