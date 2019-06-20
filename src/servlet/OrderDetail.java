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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(urlPatterns = "/OrderDetail")
public class OrderDetail extends HttpServlet {

    @Resource(name = "jdbc/finalpossystemproject")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonReader reader=null;
        JsonObject empObj = null;

        try {
            reader = Json.createReader(request.getReader());
            empObj=reader.readObject();

            String orderid = empObj.getString("orderid");
            String itemid = empObj.getString("itemid");
            int qty = Integer.parseInt(empObj.getString("qty"));
            double price = Double.parseDouble(empObj.getString("price"));

            Connection connection = dataSource.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO orderdetail VALUES (?,?,?,?)");
            pstm.setObject(1,orderid);
            pstm.setObject(2,itemid);
            pstm.setObject(3,qty);
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

            String sql= "select * from orderdetail";
            statement=connection.createStatement();

            resultSet=statement.executeQuery(sql);

            JsonArrayBuilder customers = Json.createArrayBuilder();

            while(resultSet.next()){

                String orderid=resultSet.getString(1);
                String itemid=resultSet.getString(2);
                int qty=resultSet.getInt(3);
                double price=resultSet.getDouble(4);

                JsonObject orderdetail = Json.createObjectBuilder()
                        .add("orderid", orderid)
                        .add("itemid", itemid)
                        .add("qty",qty)
                        .add("price", price)
                        .build();
                customers.add(orderdetail);
            }

            printWriter.println(customers.build().toString());
            connection.close();

        }catch (Exception ex){
            response.sendError(500, ex.getMessage());
            ex.printStackTrace();
        }

    }
}
