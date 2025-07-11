package evoplan.services.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import evoplan.main.DatabaseConnection;

public class StatsService {

    private final Connection connection;

    public StatsService() {
        this.connection = DatabaseConnection.getInstance().getCnx();
    }

    public Map<String, Integer> getVisitsOverTime(int eventId, String period) {
        Map<String, Integer> visitData = new LinkedHashMap<>();
        String query = getQueryForPeriod("visite", "date_visite", eventId, period);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                visitData.put(rs.getString("date"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return visitData;
    }

    public Map<String, Integer> getReservationsFromVisits(int eventId, String period) {
        Map<String, Integer> reservationData = new LinkedHashMap<>();
        String query = """
        SELECT DATE(v.date_visite) as date, COUNT(DISTINCT r.id_reservation) as count
        FROM visite v
        LEFT JOIN reservation r ON r.id_event = v.event_id AND r.id_client = v.client_id
        WHERE v.event_id = ? AND %s
        GROUP BY DATE(v.date_visite)
        ORDER BY DATE(v.date_visite);
        """.formatted(getDateCondition("v.date_visite", period));

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reservationData.put(rs.getString("date"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservationData;
    }


    private String getQueryForPeriod(String tableName, String dateColumn, int eventId, String period) {
        return """
                SELECT DATE(%s) as date, COUNT(*) as count
                FROM %s
                WHERE event_id = ? AND %s
                GROUP BY DATE(%s)
                ORDER BY DATE(%s);
                """.formatted(dateColumn, tableName, getDateCondition(dateColumn, period), dateColumn, dateColumn);
    }

    private String getDateCondition(String dateColumn, String period) {
        return switch (period) {
            case "Dernière semaine" -> dateColumn + " >= CURDATE() - INTERVAL 7 DAY";
            case "Dernier mois" -> dateColumn + " >= CURDATE() - INTERVAL 1 MONTH";
            case "3 derniers mois" -> dateColumn + " >= CURDATE() - INTERVAL 3 MONTH";
            default -> "1=1";
        };
    }

    public void addVisit(int eventId, int clientId) {
        String query = "INSERT INTO visite (event_id, date_visite, client_id) VALUES (?, NOW(), ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ps.setInt(2, clientId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
