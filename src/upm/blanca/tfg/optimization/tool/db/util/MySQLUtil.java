package upm.blanca.tfg.optimization.tool.db.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import upm.blanca.tfg.optimization.tool.main.MainInterface;
import upm.blanca.tfg.optimization.tool.util.QueryBean;

import com.mysql.jdbc.PreparedStatement;
public class MySQLUtil {

	public static Connection getConnectionMySQL() throws SQLException{

		System.out.println("-------- MySQL JDBC Connection Testing ------------");

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return null;
		}

		System.out.println("MySQL JDBC Driver Registered!");
		Connection connection = null;
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/BBDD_interna","root", "root");	

		return connection;

	}

	public static void populateDB(QueryBean queryBean) throws SQLException{
		//hace conexion y redistribuye
		Connection connection = getConnectionMySQL();
		int idBBDD = insertIntoBBDD(connection, queryBean);
		int idDescription = insertIntoQueryDescription(connection, queryBean);
		int idSQLQuery = insertIntoQuerySQL(connection, queryBean, idBBDD, idDescription);
		insertIntoExecution(connection, queryBean, idSQLQuery);
		connection.close();
	}

	public static int insertIntoBBDD(Connection connection, QueryBean queryBean) throws SQLException{
		int claveGenerada = 0;
		long idResult = 0L;
		String nameBBDD = "";
		String userBBDD = "";
		String passBBDD = "";
		String servicioBBDD = "";

		if(connection != null){

			Statement s = connection.createStatement(); 
			ResultSet rs = s.executeQuery (("SELECT * FROM BBDD WHERE ServicioBBDD = '") + queryBean.getBbddService() + ("'"));

			//Guardo los valores del resultado de la consulta
			while (rs.next()){ 
				claveGenerada = rs.getInt(1);
				nameBBDD = rs.getString(2);
				userBBDD = rs.getString(3);
				passBBDD = rs.getString(4);
				servicioBBDD = rs.getString(5);

			}

			if(nameBBDD.equals("")){
				PreparedStatement pstm = null;
				String query = "INSERT INTO BBDD (NombreBBDD, UserBBDD, PassBBDD, ServicioBBDD) VALUES(?,?,?,?);";

				pstm = (PreparedStatement) connection.prepareStatement(query);
				pstm.setString(1, queryBean.getBbddName());
				pstm.setString(2, queryBean.getBbddUser());
				pstm.setString(3, queryBean.getBbddPass());
				pstm.setString(4, queryBean.getBbddService());
				pstm.execute();
				idResult = pstm.getLastInsertID();
			}
			else{
				System.out.println("BBDD ya creada!!!!");
				idResult = claveGenerada;
			}
		}
		return (int) idResult;
	}
	public static int insertIntoQueryDescription(Connection connection, QueryBean queryBean) throws SQLException{
		int claveGenerada = 0;
		String description = "";
		long idResult = 0L;

		if(connection != null){

			Statement s = connection.createStatement(); 
			ResultSet rs = s.executeQuery ("SELECT * FROM QueryDescription where Descripcion = '" + queryBean.getQueryDescription() + ("'"));

			//Guardo los valores del resultado de la consulta
			while (rs.next()){ 
				claveGenerada = rs.getInt(1);
				description = rs.getString(2);
			}

			if(!description.equals(queryBean.getQueryDescription())){
				PreparedStatement pstm = null;
				String query = "INSERT INTO QueryDescription (Descripcion) VALUES(?);";

				pstm = (PreparedStatement) connection.prepareStatement(query);
				pstm.setString(1, queryBean.getQueryDescription());
				pstm.execute();
				idResult = pstm.getLastInsertID();

			}
			else{
				System.out.println("Descripción ya creada!!!!");
				idResult = claveGenerada;
			}

		}		
		return (int) idResult;
	}

	/**
	 * 
	 * @param connection
	 * @param queryBean
	 * @param idBBDD
	 * @param idDescription
	 * @return int the primary key
	 * @throws SQLException sqle
	 */
	public static int insertIntoQuerySQL(Connection connection, QueryBean queryBean, int idBBDD, int idDescription) throws SQLException{
		int claveGenerada = 0;
		String querySQL = "";
		long idResult = 0L;

		if(connection != null){

			Statement s = connection.createStatement(); 
			ResultSet rs = s.executeQuery ("SELECT * FROM QuerySQL where query = '" + StringEscapeUtils.escapeSql(queryBean.getQueryString()) + ("'"));

			//Guardo los valores del resultado de la consulta
			while (rs.next()){ 
				claveGenerada = rs.getInt(1);
				querySQL = rs.getString(2);
			}

			if(!querySQL.equals(queryBean.getQueryString())){
				PreparedStatement pstm = null;
				String query = "INSERT INTO QuerySQL (query, idBBDD, idQueryDescription) VALUES(?,?,?);";

				pstm = (PreparedStatement) connection.prepareStatement(query);
				pstm.setString(1, queryBean.getQueryString());
				pstm.setInt(2, idBBDD);
				pstm.setInt(3, idDescription);
				pstm.execute();
				idResult = pstm.getLastInsertID();

			}
			else{
				System.out.println("Descripción ya creada!!!!");				
				idResult = claveGenerada;
			}

		}		
		return (int) idResult;
	}
	public static void insertIntoExecution(Connection connection, QueryBean queryBean, int idSQLQuery) throws SQLException{

		if(connection != null){

			PreparedStatement pstm = null;
			String query = "INSERT INTO Execution (Fecha, HoraInicio, HoraFin, Tiempo, idQuerySQL, numFilas) VALUES(?,?,?,?,?,?);";

			pstm = (PreparedStatement) connection.prepareStatement(query);
			pstm.setString(1, queryBean.getQueryDate());
			pstm.setString(2, queryBean.getQueryStartTime());
			pstm.setString(3, queryBean.getQueryEndTime());
			pstm.setInt(4, (queryBean.getTotalTime()));
			pstm.setInt(5, idSQLQuery);
			pstm.setInt(6,queryBean.getNumRows());
			pstm.execute();
		}		
	}
	public static List<String> getDescriptions(Connection connection) throws SQLException{

		List<String> lista=new ArrayList<String>();

		if(connection != null){

			java.sql.PreparedStatement consulta1 = connection.prepareStatement("SELECT Descripcion FROM QueryDescription");
			ResultSet result1 = consulta1.executeQuery();
			while(result1.next()){

				String nombre= result1.getString(1);

				//Creas un objeto del tipo que te estas trayendo de la bd
				lista.add(nombre); //agregas ese objeto a la lista
			}
		}
		return lista;
	}

	public static List<String> getBbdd (Connection connection) throws SQLException{

		List<String> lista=new ArrayList<String>();

		if(connection != null){

			java.sql.PreparedStatement consulta1 = connection.prepareStatement("SELECT NombreBBDD FROM BBDD");
			ResultSet result1 = consulta1.executeQuery();
			while(result1.next()){

				String nombre= result1.getString(1);

				//Creas un objeto del tipo que te estas trayendo de la bd
				String k=new String(nombre);//le mandas los parametros necesarios al constructor
				lista.add(k); //agregas ese objeto a la lista
			}
		}
		return lista;
	}

	public static List<String> getSelectedSqlQuery(String descriptionSelected) throws SQLException {

		Connection connection = MySQLUtil.getConnectionMySQL();
		List<String> lista=new ArrayList<String>();

		if(connection != null){

			java.sql.PreparedStatement consulta1 = connection.prepareStatement("select query from QuerySQL where idQueryDescription = (select idQueryDescription from QueryDescription where Descripcion = '" + descriptionSelected + "')");
			ResultSet result1 = consulta1.executeQuery();
			while(result1.next()){

				String querySql= result1.getString(1);
				lista.add(querySql);
			}
		}
		return lista;
	}
	public static void getSelectedDataBase(String descriptionSelected) throws SQLException {

		String service;
		String user;
		String pass;
		
		Connection connection = MySQLUtil.getConnectionMySQL();

		if(connection != null){

			java.sql.PreparedStatement consulta1 = connection.prepareStatement("select * from BBDD where NombreBBDD = '" + descriptionSelected + "'");
			ResultSet result1 = consulta1.executeQuery();
			while(result1.next()){

				user = result1.getString("UserBBDD");
				pass = result1.getString("PassBBDD");
				service = result1.getString("ServicioBBDD");
				MainInterface.queryBean.setBbddService(service);
				MainInterface.queryBean.setBbddUser(user);
				MainInterface.queryBean.setBbddPass(pass);
			}
		}
	}
	


}