package fr.isen.java2.db.daos;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

public class MovieDao {

//	public List<Movie> listMovies() {
//		List<Movie> movies = new ArrayList<>();
//		String sqlQuery = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre";
//		try(Connection cnx = DataSourceFactory.getDataSource().getConnection();
//			PreparedStatement statement = cnx.prepareStatement(sqlQuery);
//			ResultSet res = statement.executeQuery()){
//
//			while (res.next()){
//				Movie movie = new Movie();
//				movie.setId(res.getInt("idmovie"));
//				movie.setTitle(res.getString("title"));
////				movie.setReleaseDate(LocalDate.from(res.getTimestamp("release_date").toLocalDateTime()));
//				if (res.getTimestamp("release_date") != null) {
//					movie.setReleaseDate(res.getTimestamp("release_date").toLocalDateTime().toLocalDate());
//				}
//				movie.setDuration(res.getInt("duration"));
//				movie.setDirector(res.getString("director"));
//				movie.setSummary(res.getString("summary"));
//
//				Genre genre = new Genre(res.getInt("idgenre"), res.getString("genre_name"));
//				movie.setGenre(genre);
//
//				movies.add(movie);
//			}
//
//		}catch (SQLException e){
//			throw new RuntimeException("Method is not yet implemented", e);
//		}
//		return movies;
//	}

	public List<Movie> listMovies() {
		List<Movie> movies = new ArrayList<>();
		String sql = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre";

		try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				// Create Genre object
				Genre genre = new Genre(rs.getInt("idgenre"), rs.getString("name"));

				// Create Movie object
				Movie movie = new Movie(
						rs.getInt("idmovie"),
						rs.getString("title"),
						rs.getTimestamp("release_date") != null ?
								rs.getTimestamp("release_date").toLocalDateTime().toLocalDate() : null,
						genre,  // Pass Genre object
						rs.getInt("duration"),
						rs.getString("director"),
						rs.getString("summary")
				);
				movies.add(movie);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return movies;
	}


	public List<Movie> listMoviesByGenre(String genreName) {
		List <Movie> movies = new ArrayList<>();
		String sqlStatement = "SELECT * FROM movie JOIN genre ON movie.genre_id = genre.idgenre WHERE genre.name = ?";
		try(Connection cnx = DataSourceFactory.getDataSource().getConnection();
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
			throw new RuntimeException("Method is not yet implemented", e);
		}
		return movies;
	}



	public Movie addMovie(Movie movie) {
		String sqlStatement = "INSERT INTO movie(title,release_date,genre_id,duration,director,summary) VALUES(?,?,?,?,?,?)";
		try (Connection conx = DataSourceFactory.getDataSource().getConnection();
			 PreparedStatement statement = conx.prepareStatement(sqlStatement)) {
			statement.setString(1, movie.getTitle());
			statement.setDate(2, Date.valueOf(movie.getReleaseDate())); // Convert LocalDate to SQL Date
			statement.setInt(3, movie.getGenre().getId()); // Get genre ID
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
			throw new RuntimeException("Method is not yet implemented", e);
		}
		return movie;
	}

}
