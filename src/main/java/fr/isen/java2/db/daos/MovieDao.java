package fr.isen.java2.db.daos;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

public class MovieDao {

	/**
	 * Retrieves a list of all movies from the database.
	 *
	 * @return a list of Movie objects
	 * @throws RuntimeException if there is an error while listing movies
	 */

	public List<Movie> listMovies() {
		List<Movie> movies = new ArrayList<>();
		String sql = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre";

		try (Connection connection = DataSourceFactory.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);
			 ResultSet res = stmt.executeQuery()) {

			while (res.next()) {
				// Create Genre object
				Genre genre = new Genre(res.getInt("idgenre"), res.getString("name"));

				// Create Movie object
				Movie movie = new Movie(
						res.getInt("idmovie"),
						res.getString("title"),
						res.getTimestamp("release_date") != null ?
								res.getTimestamp("release_date").toLocalDateTime().toLocalDate() : null,
						genre,  // Pass Genre object
						res.getInt("duration"),
						res.getString("director"),
						res.getString("summary")
				);
				movies.add(movie);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listing movies into database", e);
		}
		return movies;
	}


	/**
	 * Retrieves a list of all movies from the database by genre.
	 *
	 * @param genreName the name of the genre to retrieve
	 * @return a list of Movie objects
	 * @throws RuntimeException if there is an error while listing movies
	 */
	public List<Movie> listMoviesByGenre(String genreName) {
		List <Movie> movies = new ArrayList<>();
		String sqlStatement = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre WHERE genre.name = ?";
		try(Connection cnx = DataSourceFactory.getConnection();
			PreparedStatement statement = cnx.prepareStatement(sqlStatement))
		{
			statement.setString(1,genreName);
			try(ResultSet res = statement.executeQuery()){
				while(res.next()){
					Movie movie = new Movie();
					movie.setDuration(res.getInt("duration"));
					movie.setReleaseDate(res.getTimestamp("release_date").toLocalDateTime().toLocalDate());
					movie.setDirector(res.getString("director"));
					movie.setSummary(res.getString("summary"));
					movie.setTitle(res.getString("title"));
					Genre genre = new Genre();
					genre.setId(res.getInt("idgenre"));
					genre.setName(res.getString("name"));
					movie.setGenre(genre);
					movies.add(movie);
				}

			}

		}catch (SQLException e){
			throw new RuntimeException("Error listing movies by genre",e);
		}
		return movies;
	}



	/**
	 * Adds a movie to the database.
	 *
	 * @param movie the movie to add
	 * @return the movie object with its ID
	 * @throws RuntimeException if there is an error while adding the movie
	 */
	public Movie addMovie(Movie movie) {
		String sqlStatement = "INSERT INTO movie(title,release_date,genre_id,duration,director,summary) VALUES(?,?,?,?,?,?)";
		try (Connection conx = DataSourceFactory.getConnection();
			 PreparedStatement statement = conx.prepareStatement(sqlStatement, PreparedStatement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, movie.getTitle());
			statement.setDate(2, Date.valueOf(movie.getReleaseDate()));
			statement.setInt(3, movie.getGenre().getId());
			statement.setInt(4, movie.getDuration());
			statement.setString(5, movie.getDirector());
			statement.setString(6, movie.getSummary());

			int affectedRows = statement.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Adding movie failed, no rows affected.");
			}

			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					movie.setId(generatedKeys.getInt(1)); // Set the generated ID
				} else {
					throw new SQLException("Adding movie failed, no ID obtained.");
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error adding movie into database", e);
		}
		return movie;
	}

}
