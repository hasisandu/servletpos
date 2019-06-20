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

@WebServlet(urlPatterns = "/Item")
public class Item extends HttpServlet {

    @Resource(name = "jdbc/finalpossystemproject")
    private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonReader reader=null;
        JsonObject itemObj = null;

        try {
            reader = Json.createReader(request.getReader());
            itemObj=reader.readObject();

            String itemid = itemObj.getString("itemid");
            String itemname = itemObj.getString("itemname");
            String discription = itemObj.getString("discription");
            int qtyonhand = Integer.parseInt(itemObj.getString("qtyonhand"));
            double unitprice = Double.parseDouble(itemObj.getString("unitprice"));


            Connection connection = dataSource.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO item VALUES (?,?,?,?,?)");
            pstm.setObject(1,itemid);
            pstm.setObject(2,itemname);
            pstm.setObject(3,discription);
            pstm.setObject(4,qtyonhand);
            pstm.setObject(5,unitprice);
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

            String sql= "select * from item";
            statement=connection.createStatement();

            resultSet=statement.executeQuery(sql);

            JsonArrayBuilder customers = Json.createArrayBuilder();

            while(resultSet.next()){

                String itemid=resultSet.getString(1);
                String itemname=resultSet.getString(2);
                String discription=resultSet.getString(3);
                int qtyonhand=resultSet.getInt(4);
                double unitprice=resultSet.getDouble(5);

                JsonObject item = Json.createObjectBuilder()
                        .add("itemid", itemid)
                        .add("itemname", itemname)
                        .add("discription",discription)
                        .add("qtyonhand", qtyonhand)
                        .add("unitprice", unitprice)
                        .build();
                customers.add(item);
            }

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
            PreparedStatement pstm = connection.prepareStatement("delete from item where itemid=?");
            pstm.setObject(1,req.getParameter("itemid"));
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

            String itemid = empObj.getString("itemid");
            String itemname = empObj.getString("itemname");
            String discription = empObj.getString("discription");
            int qtyonhand = Integer.parseInt(empObj.getString("qtyonhand"));
            double unitprice = Double.parseDouble(empObj.getString("unitprice"));

            Connection connection = dataSource.getConnection();
            PreparedStatement pstm = connection.prepareStatement("update item set itemname=?, discription=?, qtyonhand=?, unitprice=? where itemid=?");
            pstm.setObject(1,itemname);
            pstm.setObject(2,discription);
            pstm.setObject(3,qtyonhand);
            pstm.setObject(4,unitprice);
            pstm.setObject(5,itemid);
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
