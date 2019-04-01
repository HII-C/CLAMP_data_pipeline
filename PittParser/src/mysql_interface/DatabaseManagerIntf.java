package mysql_interface;

public interface DatabaseManagerIntf {
    void pushSqlQuery(String aQuery);
    void openConnection();
    void closeConnection();
}
