package fr.isen.java2.db.daos;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.tuple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import fr.isen.java2.db.entities.Genre;
import fr.isen.java2.db.entities.Movie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class MovieDaoTestCase {
	private final MovieDao movieDao = new MovieDao();
	@BeforeEach
	public void initDb() throws Exception {
		Connection connection = DataSourceFactory.getDataSource().getConnection();
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(
				"CREATE TABLE IF NOT EXISTS genre (idgenre INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , name VARCHAR(50) NOT NULL);");
		stmt.executeUpdate(
                """
                        CREATE TABLE IF NOT EXISTS movie (\r
                          idmovie INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r
                          title VARCHAR(100) NOT NULL,\r
                          release_date DATETIME NULL,\r
                          genre_id INT NOT NULL,\r
                          duration INT NULL,\r
                          director VARCHAR(100) NOT NULL,\r
                          summary MEDIUMTEXT NULL,\r
                          CONSTRAINT genre_fk FOREIGN KEY (genre_id) REFERENCES genre (idgenre));""");
		stmt.executeUpdate("DELETE FROM movie");
		stmt.executeUpdate("DELETE FROM genre");
		stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='movie'");
		stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='genre'");
		stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (1,'Drama')");
		stmt.executeUpdate("INSERT INTO genre(idgenre,name) VALUES (2,'Comedy')");
		stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (1, 'Title 1', '2015-11-26 12:00:00.000', 1, 120, 'director 1', 'summary of the first movie')");
		stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (2, 'My Title 2', '2015-11-14 12:00:00.000', 2, 114, 'director 2', 'summary of the second movie')");
		stmt.executeUpdate("INSERT INTO movie(idmovie,title, release_date, genre_id, duration, director, summary) "
				+ "VALUES (3, 'Third title', '2015-12-12 12:00:00.000', 2, 176, 'director 3', 'summary of the third movie')");
		stmt.close();
		connection.close();
	}
	
	 @Test
	 public void shouldListMovies() {
//		When
		 List<Movie> movies = movieDao.listMovies();
//		 Then
		 assertThat(movies).hasSize(3);
		 assertThat(movies).extracting("title", "releaseDate","genre.id", "duration", "director", "summary")
				 .containsOnly(
						 tuple("Title 1", LocalDate.of(2015, 11, 26), 1, 120, "director 1", "summary of the first movie"),
						 tuple("My Title 2", LocalDate.of(2015, 11, 14), 2, 114, "director 2",  "summary of the second movie"),
						 tuple("Third title", LocalDate.of(2015, 12, 12), 2, 176, "director 3", "summary of the third movie")
				 );
	 }

	 @Test
	 public void shouldListMoviesByGenre() {
//		when
		  List<Movie> movies = movieDao.listMoviesByGenre("Drama");
		 assertThat(movies).hasSize(1);
		 assertThat(movies).extracting("title", "releaseDate", "duration", "director", "summary")
				 .containsOnly(
						 tuple("Title 1", LocalDate.of(2015, 11, 26), 120, "director 1", "summary of the first movie")
				 );
		 assertThat(movies.getFirst().getGenre().getName()).isEqualTo("Drama");
	 }


	 @Test
	 public void shouldAddMovie() throws Exception {

		 // GIVEN
		 Movie movie = new Movie();
		 movie.setTitle("Test Movie");
		 movie.setReleaseDate(LocalDate.of(2024, 2, 8)); // Use a valid date
		 movie.setGenre(new Genre(1, "Drama")); // Ensure this genre exists in the DB
		 movie.setDuration(120);
		 movie.setDirector("Test Director");
		 movie.setSummary("Test Summary");

		 // WHEN
		 movieDao.addMovie(movie);

		 // THEN
		 try (Connection connection = DataSourceFactory.getDataSource().getConnection();
			  PreparedStatement statement = connection.prepareStatement("SELECT * FROM movie WHERE title = ?")) {

			 statement.setString(1, "Test Movie");
			 try (ResultSet resultSet = statement.executeQuery()) {
				 assertThat(resultSet.next()).isTrue();
				 assertThat(resultSet.getString("title")).isEqualTo("Test Movie");
				 assertThat(resultSet.getInt("duration")).isEqualTo(120);
				 assertThat(resultSet.getString("director")).isEqualTo("Test Director");
				 assertThat(resultSet.getString("summary")).isEqualTo("Test Summary");
				 assertThat(resultSet.next()).isFalse();
			 }
		 }
	}
}





