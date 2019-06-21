package servlet;

import javax.annotation.Resource;
import javax.json.*;
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

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("DO Working");
        Connection connection = null;
        resp.setContentType("application/json");

        PrintWriter out = resp.getWriter();



        try {

            JsonReader reader = Json.createReader(req.getReader());
            JsonObject JSON = reader.readObject();

            JsonObject order = JSON.getJsonObject("order");

            String orderID = order.getString("orderid");

            JsonArray orderDetails = JSON.getJsonArray("orderDetails");

            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            String sql = "INSERT INTO Orders VALUES (?,?,?)";
            PreparedStatement pstm = connection.prepareStatement(sql);
            pstm.setString(2, order.getString("date"));
            pstm.setString(3, order.getString("customerid"));
            pstm.setString(1, orderID);

            int affectedRows = pstm.executeUpdate();

            if (affectedRows == 0) {
                connection.rollback();
                return;
            } else {
                System.out.println("Order OK");
            }

            sql = "INSERT INTO  ItemDetail VALUES (?,?,?,?)";
            pstm = connection.prepareStatement(sql);

            for (int i = 0; i < orderDetails.size() - 1; i++) {

                JsonObject orderDetail = orderDetails.get(i).asJsonObject();

                pstm.setObject(1, orderID);
                pstm.setObject(2, orderDetail.getString("itemCode"));
                pstm.setObject(3, orderDetail.getInt("qty"));
                pstm.setObject(4, orderDetail.getString("subPrice"));

                affectedRows = pstm.executeUpdate();

                if (affectedRows == 0) {
                    connection.rollback();
                    return;
                }

                Statement stm = connection.createStatement();
                ResultSet rst = stm.executeQuery("SELECT * FROM Item WHERE code='" + orderDetail.getString("itemCode") + "'");

                int qtyOnHand = 0;

                if (rst.next()) {
                    qtyOnHand = rst.getInt("qtyOnHand");
                }

                PreparedStatement pstm2 = connection.prepareStatement("UPDATE Item SET qtyOnHand=? WHERE code=?");

                pstm2.setObject(1, qtyOnHand - orderDetail.getInt("qty"));
                pstm2.setObject(2, orderDetail.getString("itemCode"));

                affectedRows = pstm2.executeUpdate();

                if (affectedRows == 0) {
                    connection.rollback();
                    return;
                }
            }

            connection.commit();

            System.out.println("Order Success");

        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException ex1) {
                System.out.println(ex1);
            }
            System.out.println(ex);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
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
        System.out.println(req.getParameter("orderid"));
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            PreparedStatement pstm = connection.prepareStatement("delete from orders where orderid=?");
            pstm.setObject(1, req.getParameter("orderid"));
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

            String orderid = empObj.getString("orderid");
            String customerid = empObj.getString("customerid");
            String orderdate = empObj.getString("orderdate");
            double price = Double.parseDouble(empObj.getString("price"));

            System.out.println(orderid);

            Connection connection = dataSource.getConnection();
            PreparedStatement pstm = connection.prepareStatement("update orders set customerid=?, orderdate=?, price=? where orderid=?");
            pstm.setObject(1, customerid);
            pstm.setObject(2, orderdate);
            pstm.setObject(3, price);
            pstm.setObject(4, orderid);
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
