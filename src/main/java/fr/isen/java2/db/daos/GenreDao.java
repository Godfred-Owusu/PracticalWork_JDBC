package fr.isen.java2.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;

public class GenreDao {

	/**
	 * Retrieves a list of all genres from the database.
	 *
	 * @return a list of Genre objects
	 * @throws RuntimeException if there is an error while listing genres
	 */
	public List<Genre> listGenres() {
		List<Genre> genres = new ArrayList<>();
		String sqlStatement = "SELECT * FROM genre";
		try(Connection cnx = DataSourceFactory.getDataSource().getConnection();
			PreparedStatement statement = cnx.prepareStatement(sqlStatement);
			ResultSet res = statement.executeQuery();)
		 {
			while (res.next()){
				Genre genre = new Genre();
				genre.setId(res.getInt("idgenre"));
				genre.setName(res.getString("name"));
				genres.add(genre);
			}
		}catch (SQLException e){
			throw new RuntimeException("Error listing genre into database");
		}
			return genres;
	}


	/**
	 * Retrieves a genre by its name from the database.
	 *
	 * @param name the name of the genre to retrieve
	 * @return the Genre object if found, otherwise null
	 * @throws RuntimeException if there is an error while getting the genre
	 */
	public Genre getGenre(String name) {
		String sqlSatement = "SELECT * FROM genre WHERE name = ?";
		try (Connection cnx = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = cnx.prepareStatement(sqlSatement)) {
			statement.setString(1, name);
			try (ResultSet res = statement.executeQuery()) {
				if (res.next()) {
					Genre genre = new Genre();
					genre.setId(res.getInt("idgenre"));
					genre.setName(res.getString("name"));

					return genre;
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error getting genre into database",e);
		}
		return null;
	}



	/**
	 * Adds a new genre to the database.
	 *
	 * @param name the name of the genre to add
	 * @throws RuntimeException if there is an error while inserting the genre
	 */
	public void addGenre(String name) {
		String sqlStatement = "INSERT INTO genre(name) VALUES(?)";
		try (Connection cnx = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = cnx.prepareStatement(sqlStatement, PreparedStatement.RETURN_GENERATED_KEYS)) {
			 statement.setString(1, name);
			 statement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("Error inserting genre into database", e);
		}
	}
}
