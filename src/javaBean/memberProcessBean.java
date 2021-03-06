
package javaBean;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.mindrot.jbcrypt.BCrypt;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import property.constString;

public class memberProcessBean {
	
	private static Connection conn = connectMysql.getConnector();
	private static memberDataBean instance = new memberDataBean();

	public static memberDataBean getInstance() {
		return instance;
	}

	public memberProcessBean() {
	}

	private Connection getConnection() throws Exception {
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		DataSource ds = (DataSource) envCtx.lookup("jdbc/basicjsp");
		return ds.getConnection();
	}
	

	/**
	   *   해당 테이블의 속성에 원하는 값이 존재하는지 여부만 조사 
    * select count(*) as size from user where name="hk*" 같은용도의 사용 
	 * @param tableName : 검색할 테이블
	 *	@param  attr : 속성 이름(들)
	 * @param  val : 값 이름(들)
	 * @return 각 속성에 대하여 중복이 발생시 중복결과에 맞게 배열로 그 결과들을 반환함.
	 */	
	public boolean isExist(String tableName, String attr ,Object val)
			throws Exception {

		boolean isDuplicated=false;
		PreparedStatement ps = null;
		ResultSet rs = null;
	
		String sql;

		try {
			switch(attr){
				
			
				case "Id" :
	
					sql = " SELECT count(*) FROM User WHERE Id = ? ";
					ps = conn.prepareStatement(sql);
					ps.setInt(1, (int)val);
					break;
				
				case "Nickname" :
					sql = " SELECT count(*) FROM User WHERE Nickname = ? ";
					ps = conn.prepareStatement(sql);
					
					ps.setString(1, (String)val);

				case "Email" :
					sql = " SELECT count(*) FROM UserMapping WHERE Email = ? ";
					ps = conn.prepareStatement(sql);
					
					ps.setString(1, (String)val);

					break;
					
				default :
					
					ps.setString(3, (String)val);
					
					break;
			
			}
								
				rs = ps.executeQuery();
				
				// 중복아이디가 있는지 결과를 저장
				rs.next();
				
				if ( rs.getInt(1) >=1)
					isDuplicated = true;
				else
					isDuplicated = false;

				
		}catch ( MySQLSyntaxErrorException e){
			System.out.println(e.getMessage());
			e.printStackTrace();			
		}		
		catch ( SQLException e){
			System.out.println(e.getMessage());
			e.printStackTrace();			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException ex) {
				}
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
		}

		return isDuplicated;
	}
	
	/**
	   *   해당 테이블의 속성에 원하는 값이 존재하는지 여부만 조사 
    *   select count(*) as size from user where name="hk*" 같은용도의 사용 
	 * @param  1 : 테이블 
	 *	@param  2 : 속성 이름(들)
	 * @param  3 : 값 이름(들)
	 * @return 각 속성에 대하여 중복이 발생시 중복결과에 맞게 배열로 그 결과들을 반환함.
	 */
	public boolean[] isExist(String tableName, String [] val, String [] attr)
			throws Exception {

		if(val.length != attr.length)
			throw new Exception("속성파라매터와 값 파라매터의 갯수가 일치하지 않습니다.");

		boolean []isDuplicated = new boolean[val.length];
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
		
			for(int i=0; i < val.length ; i++){
				
				ps = conn.prepareStatement(
						"select count(*) as size from ? where ? = ?");
				//ps.setString(1, tableName);
				ps.setString(2, attr[i]);
				switch (val[i]) {
	
					case "Id" :
			
						ps.setInt(3, Integer.parseInt(val[i]));
						break;
	
					case "Email" :
					case "Nickname" :
					
						ps.setString(3, val[i]);
						break;
	
					default :
				}
				
				rs = ps.executeQuery();
				rs.next();
				// 중복아이디가 있는지 결과를 저장
				if (rs.getInt("size")>=1)
					isDuplicated[i] = true;
				else
					isDuplicated[i] = false;
				
			}
		

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException ex) {
				}
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
		}

		return isDuplicated;
	}

	/**
	 *    기존에 존재하는 UserMapping의 속성 ID의 유일한 값을 반환한다. 
	 * @return 기본키에 중복되지 않는 값을 반환한다.
	 */
	private String getUniqueId(){
		
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		
		while (true) {

			Statement stmt;
			try {
				stmt = conn.createStatement();
		
			rs = stmt.executeQuery(
					"select count(*) as size from UserMapping");
			rs.next();
			if (rs.getLong("size") > 99999999L)
				throw new SQLException("UserMapping테이블이 꽉 찼습니다.");

			String tempID = String.valueOf( (new Random()).nextInt(90000000)
					+ 10000000);
			
			boolean isDuplicatedAry = 
					isExist("UserMapping", "Id", Integer.valueOf(tempID));
		
			if(isDuplicatedAry == false)
				return tempID;
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
	}

	
	/**
	 * 
	 * @param tableName 조회할 테이블 이름 
	 * @param attr			조회할 테이블의 속성명 
	 * @param val			조회할 테이블의 속성의 값.
	 * @return				일치여부가 파라매터 갯수만큼 각 결과가 배열로  리턴된다. 
	 */
	private String[][] isEqual(String tableName, String [] attr, String [] val){
		
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String [][]res = new String[attr.length][]; 
		try {

			if(attr.length != val.length)
				throw new Exception("매개변수의 수가 일치하지 않습니다");
			
			for(int i=0 ; i < attr.length ; i++){
				

				pstmt = conn.prepareStatement(
						"select ? from ? where ? = ?");
				pstmt.setString(1, attr[i]);
				pstmt.setString(2, tableName);
				pstmt.setString(3, val[i]);
				pstmt.setString(4, attr[i]);
				rs = pstmt.executeQuery();
				rs.last();
				res[i] = new String[rs.getRow()];	
				
				rs.first();
				while(rs.isLast() == false){
					res[i][rs.getRow()-1] = rs.getString(1);
					rs.next();
					
				}
				
			}
			
		}

		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
		}
		
	
		return res;
		
		
	}
	
	private String MailToId(String Email){
		
		
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		int id;
		try {


			pstmt = conn.prepareStatement("select Id from UserMapping where EMail = ? ");
			pstmt.setString(1, Email);
			rs = pstmt.executeQuery();
			rs.next();
			id = rs.getInt(1);
			if(id == 0)
				throw new SQLException("해당하는 메일이 존재하지 않습니다");
			
			
			
		}

		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
		}
		
		return String.valueOf(id);
		
		
	}

	/**
	 * @param member  가입할 유저의 정보의 객체 
	 * @param request jsp페이지로부터  넘어온 attribute값을 이용하기 위함 
	 * @return 가입이 무사히 성사됐는지 여부를 반환
	 * @throws SQLException 중복, 테이블이 꽉찼는지에 대한 예외가 발생됨 
	 * 홈페이지 내부가입자가 아닌 외부인증을 통하여 가입한 유저는 비밀번호가 공백이다.
	 */
	@SuppressWarnings("resource")
	public boolean joinMember(memberDataBean member) throws SQLException {

		ResultSet rs = null;
		PreparedStatement pstmt = null;

		try {
			conn.setAutoCommit(false);
			
			
			boolean isDuplicated = 
					isExist("UserMapping", "Email" ,member.getEmail() );
			
			if(isDuplicated){
				throw new Exception(member.getEmail() + " 메일이 중복됩니다");
			}
			
			// user매핑테이블 추가
			String uniqId = getUniqueId();
			member.setId(Integer.parseInt( uniqId));
		
			pstmt = conn.prepareStatement(
					"insert into UserMapping values (?,?)");
			pstmt.setInt(1, member.getId());
			pstmt.setString(2, member.getEmail());
			pstmt.executeUpdate();
			
			pstmt = conn.prepareStatement(
					"insert into User values (?,?,?,?,?)");
			
			if(member.getIdType().equals("inner")){
				constString s = constString.salt;
				int salt = Integer.valueOf(s.getString());
				String pw = BCrypt.hashpw( member.getPassword(), BCrypt.gensalt(salt));
				pstmt.setString(3, pw);
			}
			else
				pstmt.setString(3, "");
		
			pstmt.setInt(1, member.getId());
			pstmt.setString(2, member.getNickname());	
			pstmt.setString(4, member.getIdType());
			pstmt.setTimestamp(5, member.getReg_date());

			pstmt.executeUpdate();
		}
		catch(SQLException ex){
			if(conn!=null) 
				try{conn.rollback();}// Exception 발생시 rollback 한다.
					catch(SQLException ex1){
						System.out.println(ex1.getMessage());
						ex1.printStackTrace();
					}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ex) {
				}
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ex) {
				}
		}
		
		
		conn.commit();
		return true;
	}

	/**
	 * @param member  가입할 유저의 정보의 객체 
	 * @param request jsp페이지로부터  넘어온 attribute값을 이용하기 위함 
	 * @return 로그인 무사히 성사됐는지 여부를 반환
	 * @throws  
	 */
	public boolean logonMember(memberDataBean member) throws SQLException {
		
		Statement st;
		ResultSet rs;
		boolean res;
		try {
			res = isExist("UserMapping", "Email", member.getEmail());
			
			if(res == false){
				throw new SQLException("값이 존재하지 않습니다");
				
			}
			
			String uniqID = MailToId(member.getEmail());
			
			st = conn.createStatement();
			rs = st.executeQuery(" select Passwd from User where Id = " + uniqID);
			rs.next();
			String hasedpw =  rs.getString(1);
			if( !BCrypt.checkpw(member.getPassword(), hasedpw ) )
				res =  false;
			else
				res = true;
		
		}catch (SQLException e){
			e.printStackTrace();
			return false;
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	
		return res;
	}
	
}
