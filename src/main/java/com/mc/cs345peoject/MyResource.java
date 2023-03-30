package com.mc.cs345peoject;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Path("/event_server")
public class MyResource {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    //private final ReadWriteLock lock2 = new ReadWriteLock();
    //static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/event?useSSL=false&serverTimezone=UTC";
    static final String DB_USER = "root";
    static final String DB_PASS = "12345678";
    Connection conn = null;

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @GET
    @Path("/event/getAllEvent")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response getAllEvent() {
        ArrayList<Event> returnEventList = new ArrayList<>();
        try {

            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sql = "select * from event_table";
            lock.readLock().lock();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            lock.readLock().unlock();

            //false -> 返回集合为空
            if (rs.isBeforeFirst() == false) {
                conn.close();
                rs.close();
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            //读取rs中的每一个值
            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getInt("event_id"));
                event.setEventName(rs.getString("event_name"));
                event.setEventAuth(rs.getString("event_auth"));
                event.setEventTime(rs.getString("event_time"));
                event.setEventDesc(rs.getString("event_desc"));
                event.setEventLat(rs.getString("event_lat"));
                event.setEventLng(rs.getString("event_lng"));
                event.setEventMsg(rs.getString("event_msg"));
                returnEventList.add(event);
            }
            conn.close();
            rs.close();
            return Response.status(Response.Status.OK).entity(returnEventList).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @GET
    @Path("/event/getEventAroundMe/{lat}/{lng}/{scope}")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response getEventAroundMe(@PathParam("lat") Double lat,
            @PathParam("lng") Double lng,
            @PathParam("scope") Double scope) {
        ArrayList<Event> returnEventList = new ArrayList<>();
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement stmt = null;
            String sql = "SELECT event_table.*, event_tag.event_tag "
                    + "From event_table "
                    + "left join event_tag on event_table.event_id = event_tag.event_id "
                    + "where ((ACOS(SIN(? * PI() / 180) * SIN(event_lat * PI() / 180) + COS(? * PI() / 180) * COS(event_lat * PI() / 180) * COS((?- event_lng) * PI() / 180)) * 6371) <= ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lng);
            stmt.setDouble(4, scope);
            ResultSet rs = stmt.executeQuery();
            if (rs.isBeforeFirst() == false) {
                conn.close();
                rs.close();
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            while (rs.next()) {
                Event event = new Event();
                event.setEventId(rs.getInt("event_id"));
                event.setEventName(rs.getString("event_name"));
                event.setEventAuth(rs.getString("event_auth"));
                event.setEventTime(rs.getString("event_time"));
                event.setEventDesc(rs.getString("event_desc"));
                event.setEventLat(rs.getString("event_lat"));
                event.setEventLng(rs.getString("event_lng"));
                event.setEventMsg(rs.getString("event_msg"));
                event.setEventTag(rs.getString("event_tag"));
                returnEventList.add(event);
            }
            conn.close();
            rs.close();
            return Response.status(Response.Status.OK).entity(returnEventList).build();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MyResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MyResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @GET
    @Path("/tag/getAllTag")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response getAllTag() {
        ArrayList<EventTag> returnTagList = new ArrayList<>();
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sql = "select * from event_tag";
            lock.readLock().lock();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            lock.readLock().unlock();

            //false -> 返回集合为空
            if (rs.isBeforeFirst() == false) {
                conn.close();
                rs.close();
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            //读取rs中的每一个值
            while (rs.next()) {
                EventTag eventTag = new EventTag();
                eventTag.setEventId(rs.getInt("event_id"));
                eventTag.setEventTagId(rs.getInt("event_tag_id"));
                eventTag.setEventTag(rs.getString("event_tag"));
                returnTagList.add(eventTag);
            }
            conn.close();
            rs.close();
            return Response.status(Response.Status.OK).entity(returnTagList).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }


    /*
    URL: http://127.0.0.1:8080/webapi/event_server/event/createNewEvent
    Header: Content-Type, application/x-www-form-urlencoded
    body:eventName=Example+Event&eventAuth=Example+Auth&eventTime=2022-01-01
    &eventDesc=Example+Description&eventLat=12.345&eventLng=67.890
    &eventMsg=Example+Message&userUid=tiaugiausdhiau151uhasdad
    &exprieDate=2021-12-22+19:31:10
     */
    @POST
    @Path("/event/createNewEvent")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public synchronized Response createNewEvent(@FormParam("eventName") String eventName,
            @FormParam("eventAuth") @DefaultValue("Anonymous") String eventAuth,
            @FormParam("eventTime") String eventTime,
            @FormParam("eventDesc") String eventDesc,
            @FormParam("eventLat") String eventLat,
            @FormParam("eventLng") String eventLng,
            @FormParam("eventMsg") String eventMsg,
            @FormParam("userUid") String userUid,
            @FormParam("exprieDate") String exprieDate) {
        try {
            lock.writeLock().lock();
            // 1. 连接到数据库
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement stmt = null;

            // 2. 插入数据
            String sql = "insert into `event_table` \n"
                    + "(event_name, event_auth, event_time, event_desc, event_lat, event_lng, event_msg)\n"
                    + "values (?, ?, ?, ?, ?, ?, ?);\n";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, eventName);
            stmt.setString(2, eventAuth);
            stmt.setString(3, eventTime);
            stmt.setString(4, eventDesc);
            stmt.setString(5, eventLat);
            stmt.setString(6, eventLng);
            stmt.setString(7, eventMsg);
            //int rowsInserted = stmt.executeUpdate();

            if (stmt.executeUpdate() != 0) {
                sql = "select event_id from event_table where event_name = ? \n"
                        + "and event_auth = ?\n"
                        + "and event_time = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, eventName);
                stmt.setString(2, eventAuth);
                stmt.setString(3, eventTime);
                //ResultSet rs = conn.createStatement().executeQuery(sql);
                ResultSet rs = stmt.executeQuery();
                int returnEventId = 0;
                while (rs.next()) {
                    returnEventId = rs.getInt("event_id");
                }
                sql = "insert into `event_publish` (user_id, event_id, publish_at, start_date, exprie_at)\n"
                        + "values(?, ?, ?, ?, ?);";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, userUid);
                stmt.setInt(2, returnEventId);
                LocalDateTime now = LocalDateTime.now();
                String format = "yyyy-MM-dd HH:mm:ss";
                String formattedDate = now.format(java.time.format.DateTimeFormatter.ofPattern(format));
                stmt.setString(3, formattedDate);
                stmt.setString(4, eventTime);
                stmt.setString(5, exprieDate);
                if (stmt.executeUpdate() != 0) {
                    conn.close();
                    return Response.status(Response.Status.OK).entity(returnEventId).build();//数据插入成功
                } else {
                    conn.close();
                    return Response.status(Response.Status.SERVICE_UNAVAILABLE).build(); //插入数据失败
                }
            } else {
                conn.close();
                return Response.status(Response.Status.NOT_ACCEPTABLE).build(); //插入数据失败
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_IMPLEMENTED).build(); // 数据库驱动程序未找到
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();// 数据库操作失败
        } finally {
            lock.writeLock().unlock();
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @POST
    @Path("/event/tag/createNewTag")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public synchronized Response createNewTag(@FormParam("event_id") int eventId,
            @FormParam("event_tag") String eventTag) {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement stmt = null;

            // 2. 插入数据
            String sql = "insert into `event_tag`\n"
                    + "(`event_id`, `event_tag`)\n"
                    + "values (?, ?);\n";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, eventId);
            stmt.setString(2, eventTag);

            if (stmt.executeUpdate() != 0) {
                conn.close();
                return Response.status(Response.Status.OK).build();//数据插入成功
            } else {
                conn.close();
                return Response.status(Response.Status.NOT_ACCEPTABLE).build(); //插入数据失败
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_IMPLEMENTED).build(); // 数据库驱动程序未找到
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();// 数据库操作失败
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @GET
    @Path("/event/getEventAroundMe/{token}/{lat}/{lag}/{range}")
    public synchronized Response getEventAroundMe(@PathParam("token") @DefaultValue("Any") String token,
            @PathParam("lat") @DefaultValue("Any") String lat,
            @PathParam("lag") @DefaultValue("Any") String lag,
            @PathParam("range") @DefaultValue("Any") String rang) {
        Auth auth = new Auth();
        if (auth.verifyUserWithToken(token)) {
            System.out.println("OK");
        }
        return Response.status(Response.Status.OK).build();
    }

}
