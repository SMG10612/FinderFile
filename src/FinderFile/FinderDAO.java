package FinderFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class FinderDAO {
	String url = "jdbc:mysql://localhost:3306/dbpath?serverTimezone=UTC";
	
	Connection conn;
	
    String id = "root";   // DB에 로그인할 ID
    String pw = "1234";   // MYSQL 설정 시 입력한 PASSWORD
    
    PreparedStatement pstmt;
	ResultSet result;
	
	String sql;
	
	// DB연결 메소드
	public void connectDB() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(url,id,pw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	// DB 데이터 삽입
	public void insertPath(Finder finder){
		connectDB();
	    
	    sql = "insert into FileData values(?,?,?,?,?)";
	    try {
	    	pstmt = conn.prepareStatement(sql);
	    	pstmt.setObject(1, finder.getParentPath());
	    	pstmt.setObject(2, finder.getFileName());
	    	pstmt.setObject(3, finder.getFileSize());
	    	pstmt.setObject(4, finder.getLastModifiedTime());
	    	pstmt.setObject(5, finder.getFileName());
	    	pstmt.executeUpdate();
	    	
	    	pstmt.close();
	    	conn.close(); 
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	// 행 별명 업데이트
	public void updateNickName(String input, String selectName) {
		connectDB();
	
		sql = "UPDATE FileData SET NickName = ? WHERE FileName = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setObject(1, input);
			pstmt.setObject(2, selectName);
        
			pstmt.executeUpdate();

			pstmt.close();
			conn.close();   
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	// DB 데이터 삭제
	public int deletData(String selectName) {
		int deletedRows = 0;
		connectDB();
		sql = "DELETE FROM FileData WHERE FileName = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, selectName);
			deletedRows = pstmt.executeUpdate(); // pstmt.executeUpdate()의 반환값 : 실행된 쿼리에 의해 영향을 받은 행의 수
         
			pstmt.close();
			conn.close(); 
         
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return deletedRows;
	}
	
	// 테이블 내용 업데이트
	public void UpdateTable(DefaultTableModel tableModel) {
		connectDB();
		tableModel.setRowCount(0); // 기존 데이터 지우기
		try {
            sql = "SELECT * FROM FileData";
            pstmt = conn.prepareStatement(sql);
            result = pstmt.executeQuery();

            while (result.next()) { // 현재 행이 존재하는 동안 반복
                Object[] rowData = { 
                      result.getString("NickName"),   // 해당 데이터의 별명
                      result.getString("ParentPath"), 
                      result.getString("FileName"),  
                      result.getLong("FileSize"),     
                      result.getString("LastModifiedTime")
                }; // 현재 result 행에서 각 열의 데이터를 추출하여 rowData 배열에 저장
                tableModel.addRow(rowData); // tableModel에 행 추가
            }
            result.close();
            pstmt.close();
            conn.close();
            System.out.println("테이블 업데이트 완료!!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
}
